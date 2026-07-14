package com.admin.controller;

import com.admin.common.result.Result;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/common")
public class FileController {

    @Value("${upload.path:./uploads/}")
    private String uploadPath;

    /**
     * 启动时将上传目录转为绝对路径，避免 Tomcat 相对路径解析到临时目录
     */
    @PostConstruct
    public void init() {
        File dir = new File(uploadPath);
        if (!dir.isAbsolute()) {
            uploadPath = new File(uploadPath).getAbsolutePath();
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 通用文件上传，返回可访问的 URL
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.fail(400, "文件不能为空");
        }

        // 上传目录
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 生成唯一文件名
        String originalName = file.getOriginalFilename();
        String suffix = "";
        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        }
        String newName = UUID.randomUUID().toString().replace("-", "") + suffix;

        // 保存文件
        File dest = new File(uploadDir, newName);
        file.transferTo(dest);

        // 返回访问 URL
        Map<String, String> data = new HashMap<>();
        data.put("url", "/uploads/" + newName);
        data.put("name", originalName);
        return Result.success(data);
    }
}
