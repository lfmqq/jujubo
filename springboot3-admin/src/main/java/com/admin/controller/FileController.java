package com.admin.controller;

import com.admin.common.result.Result;
import com.admin.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * 通用文件上传，返回原有格式的可访问 URL。
     *
     * @param file 待上传文件
     * @return 文件访问地址和原始文件名
     * @throws IOException 文件存储失败时抛出
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        // 先拒绝空文件，避免在 MinIO 中产生无效对象。
        if (file.isEmpty()) {
            return Result.fail(400, "文件不能为空");
        }

        // 生成唯一文件名
        String originalName = file.getOriginalFilename();
        String suffix = extractSafeSuffix(originalName);
        String newName = UUID.randomUUID().toString().replace("-", "") + suffix;

        // 将文件流写入 MinIO，控制器只负责参数校验和响应组装。
        fileStorageService.store(file, newName);

        // 返回访问 URL
        Map<String, String> data = new HashMap<>();
        data.put("url", "/uploads/" + newName);
        data.put("name", originalName);
        return Result.success(data);
    }

    /**
     * 从原始文件名提取安全扩展名，避免将路径分隔符带入对象名。
     *
     * @param originalName 客户端提供的原始文件名
     * @return 规范化后的扩展名，没有合法扩展名时返回空字符串
     */
    private String extractSafeSuffix(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "";
        }
        int separatorIndex = Math.max(originalName.lastIndexOf('/'), originalName.lastIndexOf('\\'));
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex <= separatorIndex || dotIndex == originalName.length() - 1) {
            return "";
        }
        String suffix = originalName.substring(dotIndex);
        // 扩展名限制为常见字母数字格式，避免控制字符或特殊路径片段进入文件名。
        return suffix.matches("\\.[A-Za-z0-9]{1,16}")
                ? suffix.toLowerCase(Locale.ROOT)
                : "";
    }
}
