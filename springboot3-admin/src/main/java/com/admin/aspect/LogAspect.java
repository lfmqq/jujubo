package com.admin.aspect;

import com.admin.common.annotation.Log;
import com.admin.common.enums.BusinessType;
import com.admin.common.security.LoginUser;
import com.admin.common.util.IpUtil;
import com.admin.entity.SysOperLog;
import com.admin.service.OperLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 操作日志记录切面：拦截所有标注 {@link Log} 的 Controller 方法，
 * 自动记录操作人、真实 IP、请求参数、响应结果等到 sys_oper_log 表。
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    private static final int MAX_LENGTH = 2000;

    private final OperLogService operLogService;
    private final ObjectMapper objectMapper;

    public LogAspect(OperLogService operLogService, ObjectMapper objectMapper) {
        this.operLogService = operLogService;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(com.admin.common.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            try {
                long costTime = System.currentTimeMillis() - startTime;
                handleLog(joinPoint, exception, result, costTime);
            } catch (Exception e) {
                log.error("操作日志记录异常", e);
            }
        }
    }

    private void handleLog(JoinPoint joinPoint, Exception e, Object jsonResult, long costTime) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Log controllerLog = method.getAnnotation(Log.class);

        HttpServletRequest request = getRequest();
        SysOperLog operLog = new SysOperLog();
        operLog.setOperTime(LocalDateTime.now());

        BusinessType bt = controllerLog.businessType();
        operLog.setTitle(controllerLog.title());
        operLog.setBusinessType(bt.getCode());
        operLog.setOperatorType(controllerLog.operatorType().getCode());

        if (request != null) {
            operLog.setRequestMethod(request.getMethod());
            operLog.setOperUrl(request.getRequestURI());
            String ip = IpUtil.getIpAddr(request);
            operLog.setOperIp(ip);
            operLog.setOperLocation(IpUtil.getRealAddressByIp(ip));
            if (controllerLog.isSaveRequestData()) {
                setRequestData(joinPoint, request, operLog);
            }
        } else {
            operLog.setRequestMethod("");
            operLog.setOperUrl("");
        }

        // 操作人员（登录用户优先，其次尝试从参数中取用户名，再次匿名）
        setOperator(operLog, joinPoint.getArgs());

        // 请求方法全名
        String className = joinPoint.getTarget().getClass().getName();
        operLog.setMethod(className + "." + method.getName() + "()");

        // 执行结果
        if (e != null) {
            operLog.setStatus(1);
            operLog.setErrorMsg(subStr(e.getMessage(), MAX_LENGTH));
        } else {
            operLog.setStatus(0);
            if (controllerLog.isSaveResponseData()) {
                operLog.setJsonResult(subStr(maskSensitive(toJson(jsonResult)), MAX_LENGTH));
            }
        }

        operLogService.save(operLog);
    }

    private void setRequestData(JoinPoint joinPoint, HttpServletRequest request, SysOperLog operLog) {
        try {
            String query = request.getQueryString();
            String body = argsToJson(joinPoint.getArgs());
            StringBuilder sb = new StringBuilder();
            if (query != null) {
                sb.append("Query: ").append(query);
            }
            if (body != null) {
                if (sb.length() > 0) {
                    sb.append(" | ");
                }
                sb.append("Body: ").append(body);
            }
            operLog.setOperParam(subStr(sb.toString(), MAX_LENGTH));
        } catch (Exception ex) {
            log.debug("解析请求参数失败", ex);
        }
    }

    private String argsToJson(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        List<Object> filtered = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            Class<?> c = arg.getClass();
            if (HttpServletRequest.class.isAssignableFrom(c)
                    || HttpServletResponse.class.isAssignableFrom(c)
                    || MultipartFile.class.isAssignableFrom(c)
                    || c.getName().contains("BindingResult")) {
                continue;
            }
            filtered.add(arg);
        }
        if (filtered.isEmpty()) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(filtered.size() == 1 ? filtered.get(0) : filtered);
            return maskSensitive(json);
        } catch (Exception e) {
            return filtered.toString();
        }
    }

    private void setOperator(SysOperLog operLog, Object[] args) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser
                    && loginUser.getSysUser() != null) {
                operLog.setOperName(loginUser.getSysUser().getUsername());
                if (loginUser.getSysUser().getDeptId() != null) {
                    operLog.setDeptName(String.valueOf(loginUser.getSysUser().getDeptId()));
                }
                return;
            }
        } catch (Exception ignored) {
            // ignore
        }
        // 未登录时（如登录接口），尝试从参数中取用户名
        if (args != null) {
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                try {
                    Method getUsername = arg.getClass().getMethod("getUsername");
                    Object username = getUsername.invoke(arg);
                    if (username != null) {
                        operLog.setOperName(String.valueOf(username));
                        return;
                    }
                } catch (Exception ignored) {
                    // 参数无 getUsername，跳过
                }
            }
        }
        operLog.setOperName("匿名");
    }

    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    private String subStr(String str, int max) {
        if (str == null) {
            return null;
        }
        return str.length() > max ? str.substring(0, max) : str;
    }

    /** 需脱敏的字段名正则（密码、token、密钥等） */
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(?i)password|passwd|pwd|oldpassword|newpassword|confirmpassword|token|secret|salt|authorization|accesskey|secretkey|cookie"
    );

    /** 对 JSON 中的敏感字段值做脱敏，避免密码 / token 等写入日志 */
    private String maskSensitive(String json) {
        if (json == null || json.isBlank()) {
            return json;
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            maskNode(root);
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return json;
        }
    }

    private void maskNode(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            List<String> fields = new ArrayList<>();
            obj.fieldNames().forEachRemaining(fields::add);
            for (String field : fields) {
                JsonNode child = obj.get(field);
                if (child.isTextual() && SENSITIVE_PATTERN.matcher(field).find()) {
                    obj.put(field, "******");
                } else {
                    maskNode(child);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                maskNode(item);
            }
        }
    }
}
