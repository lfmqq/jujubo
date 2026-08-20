package com.admin.service.impl;

import com.admin.service.FileStorageService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * MinIO 对象存储实现，使用服务端桶保存上传文件。
 */
@Service
public class MinioFileStorageService implements FileStorageService {

    private final MinioClient minioClient;
    private final String bucket;

    /**
     * 创建 MinIO 文件存储服务。
     *
     * @param minioClient MinIO 客户端
     * @param bucket      文件存储桶名称
     */
    public MinioFileStorageService(MinioClient minioClient,
                                   @Value("${minio.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    /**
     * 启动时确保配置的存储桶已经存在。
     *
     * @throws IOException 存储桶检查或创建失败时抛出
     */
    @PostConstruct
    public void init() throws IOException {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build());
            // 首次部署自动创建业务桶，后续重启直接复用已有桶。
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new IOException("MinIO 存储桶初始化失败: " + bucket, e);
        }
    }

    /**
     * 将上传文件写入 MinIO 存储桶。
     *
     * @param file       待保存的上传文件
     * @param objectName MinIO 对象名
     * @throws IOException 对象写入失败时抛出
     */
    @Override
    public void store(MultipartFile file, String objectName) throws IOException {
        String contentType = file.getContentType();
        // 浏览器未携带类型时使用通用二进制类型，避免 MinIO 返回错误的内容类型。
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }
        try (InputStream inputStream = file.getInputStream()) {
            // 文件大小已知时交给 SDK 自动选择分片大小，避免把文件整体读入内存。
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new IOException("MinIO 文件上传失败: " + objectName, e);
        }
    }

    /**
     * 从 MinIO 读取对象及其元数据。
     *
     * @param objectName MinIO 对象名
     * @return 文件输入流及其元数据
     * @throws IOException 对象不存在或读取失败时抛出
     */
    @Override
    public StoredFile load(String objectName) throws IOException {
        try {
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            String contentType = stat.contentType();
            // 桶内对象没有类型元数据时仍按二进制流返回。
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }
            return new StoredFile(inputStream, contentType, stat.size());
        } catch (ErrorResponseException e) {
            // MinIO 对象不存在时转换为通用文件未找到异常，由控制器返回 404。
            if ("NoSuchKey".equals(e.errorResponse().code())
                    || "NoSuchObject".equals(e.errorResponse().code())) {
                throw new FileNotFoundException("文件不存在: " + objectName);
            }
            throw new IOException("MinIO 文件读取失败: " + objectName, e);
        } catch (Exception e) {
            throw new IOException("MinIO 文件读取失败: " + objectName, e);
        }
    }
}
