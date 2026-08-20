package com.admin.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 客户端配置，应用始终使用 MinIO 作为文件存储后端。
 */
@Configuration
public class MinioConfig {

    /**
     * 根据配置创建 MinIO Java 客户端。
     *
     * @param endpoint MinIO API 地址
     * @param accessKey MinIO 访问密钥
     * @param secretKey MinIO 秘密密钥
     * @return MinIO 客户端
     */
    @Bean
    public MinioClient minioClient(
            @Value("${minio.endpoint}") String endpoint,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
