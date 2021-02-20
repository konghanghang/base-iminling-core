package com.iminling.common.file;

import com.iminling.core.util.WebUtils;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DownFileUtil {

    private DownFileUtil(){}

    /**
     * 文件转byte数组
     * @param file 文件
     * @return 字节数字
     * @throws IOException io异常
     */
    public static byte[] file2ByteArray(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file);
             ByteArrayOutputStream out = new ByteArrayOutputStream(1024)) {
            byte[] temp = new byte[1024];
            int size;
            while ((size = in.read(temp)) != -1) {
                out.write(temp, 0, size);
            }
            byte[] bytes = out.toByteArray();
            return bytes;
        }
    }

    /**
     * 获取响应entity
     * @param os  OutputStream
     * @param fileName 文件名称
     * @return ResponseEntity
     */
    public static ResponseEntity<byte[]> getResponseEntity(ByteArrayOutputStream os, String fileName) {
        return WebUtils.createAttachmentResponseEntity(fileName, os.toByteArray());
    }

}
