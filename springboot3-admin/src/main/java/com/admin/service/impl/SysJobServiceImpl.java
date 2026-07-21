package com.admin.service.impl;

import com.admin.entity.SysJob;
import com.admin.entity.SysJobLog;
import com.admin.mapper.SysJobLogMapper;
import com.admin.mapper.SysJobMapper;
import com.admin.service.SysJobService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements SysJobService {

    private final SysJobLogMapper jobLogMapper;
    private final ApplicationContext applicationContext;
    private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

    /** 正在运行的任务 future 缓存 */
    private final Map<Long, ScheduledFuture<?>> runningTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        taskScheduler.setPoolSize(10);
        taskScheduler.initialize();
        // 启动所有运行状态的任务
        List<SysJob> jobs = baseMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysJob>()
                        .eq(SysJob::getStatus, 1));
        jobs.forEach(this::startJob);
    }

    @Override
    public void run(Long jobId) {
        SysJob job = baseMapper.selectById(jobId);
        if (job == null) return;
        executeJob(job);
    }

    /** 调度启动单个任务 */
    public void startJob(SysJob job) {
        // 先停止已有
        stopJob(job.getId());
        try {
            ScheduledFuture<?> future = taskScheduler.schedule(
                    () -> executeJob(job),
                    new CronTrigger(job.getCronExpression()));
            runningTasks.put(job.getId(), future);
        } catch (Exception e) {
            log.error("任务 [{}] cron 表达式错误: {}", job.getJobName(), e.getMessage());
        }
    }

    /** 停止单个任务 */
    public void stopJob(Long jobId) {
        ScheduledFuture<?> future = runningTasks.remove(jobId);
        if (future != null) {
            future.cancel(false);
        }
    }

    /** 执行任务 */
    private void executeJob(SysJob job) {
        long start = System.currentTimeMillis();
        SysJobLog log = new SysJobLog();
        log.setJobId(job.getId());
        log.setJobName(job.getJobName());
        log.setInvokeTarget(job.getInvokeTarget());
        log.setExecTime(LocalDateTime.now());

        try {
            String target = job.getInvokeTarget();
            // 格式: beanName.methodName
            String[] parts = target.split("\\.");
            if (parts.length != 2) throw new RuntimeException("调用目标格式错误，需为 beanName.methodName");

            Object bean = applicationContext.getBean(parts[0]);
            Method method = bean.getClass().getMethod(parts[1]);
            method.invoke(bean);

            log.setStatus(0);
            log.setDuration(System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.setStatus(1);
            log.setDuration(System.currentTimeMillis() - start);
            log.setErrorMsg(e.getMessage());
        }
        jobLogMapper.insert(log);
    }
}
