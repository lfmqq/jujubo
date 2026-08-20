package com.admin.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * MinIO 文件存储抽象，封装对象上传和读取操作。
 */
public interface FileStorageService {

    /**
     * 将上传文件保存到指定对象名。
     *
     * @param file       待保存的上传文件
     * @param objectName 存储对象名，必须是安全的单层文件名
     * @throws IOException 保存失败时抛出
     */
    void store(MultipartFile file, String objectName) throws IOException;

    /**
     * 读取指定对象，调用方负责关闭返回对象中的输入流。
     *
     * @param objectName 存储对象名
     * @return 文件流、内容类型和文件大小
     * @throws IOException 对象不存在或读取失败时抛出
     */
    StoredFile load(String objectName) throws IOException;

    /**
     * 文件读取结果，封装响应所需的流信息。
     *
     * @param inputStream 文件输入流
     * @param contentType 文件内容类型
     * @param contentLength 文件字节数
     */
    record StoredFile(java.io.InputStream inputStream, String contentType, long contentLength) {
    }
}
