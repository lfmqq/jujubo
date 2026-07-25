package com.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
@MapperScan("com.admin.mapper")
public class AdminApplication {
	public static void main(String[] args) {
		// 强制设置 JVM 默认时区为 Asia/Shanghai，解决 Docker/1Panel 容器中时间慢 8 小时的问题
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		SpringApplication.run(AdminApplication.class, args);
	}
}