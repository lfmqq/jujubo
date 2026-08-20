package com.admin.controller;

import com.admin.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

/**
 * 文件访问接口，代理 MinIO 对象并保持原有 /uploads URL 不变。
 */
@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class FileResourceController {

    private final FileStorageService fileStorageService;

    /**
     * 流式返回上传文件，避免将大文件一次性加载到内存。
     *
     * @param objectName 存储对象名
     * @return 文件流响应，文件不存在时返回 404
     * @throws IOException 文件读取失败时抛出
     */
    @GetMapping("/{objectName:.+}")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable String objectName) throws IOException {
        // URL 中的对象名只允许单层文件，先拦截路径穿越和非法分隔符。
        if (!isSafeObjectName(objectName)) {
            return ResponseEntity.badRequest().build();
        }
        final FileStorageService.StoredFile storedFile;
        try {
            storedFile = fileStorageService.load(objectName);
            // 不存在的对象统一返回 404，保持静态文件访问的语义。
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
        MediaType contentType;
        try {
            contentType = MediaType.parseMediaType(storedFile.contentType());
            // 存储中的类型可能来自客户端，解析失败时降级为二进制流。
        } catch (IllegalArgumentException e) {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }
        StreamingResponseBody responseBody = outputStream -> {
            // 响应完成后关闭底层连接，避免 MinIO HTTP 连接泄漏。
            try (InputStream inputStream = storedFile.inputStream()) {
                inputStream.transferTo(outputStream);
            }
        };
        return ResponseEntity.ok()
                .contentType(contentType)
                .contentLength(storedFile.contentLength())
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable())
                .body(responseBody);
    }

    /**
     * 校验对象名只能访问上传根目录下的单层文件。
     *
     * @param objectName 待校验的对象名
     * @return 对象名是否合法
     */
    private boolean isSafeObjectName(String objectName) {
        return objectName != null && !objectName.isBlank()
                && !objectName.contains("..")
                && !objectName.contains("/")
                && !objectName.contains("\\");
    }
}
