package com.iminling.common.file;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

public class WebUtils {

    private WebUtils(){}

    public static HttpHeaders createAttachmentHttpHeaders(String fileName) {
        return createContentDispositionHttpHeaders("attachment", fileName);
    }

    public static HttpHeaders createInlineHttpHeaders(String fileName) {
        return createContentDispositionHttpHeaders("inline", fileName);
    }

    /**
     * Content-Disposition告诉浏览器下载文件的名称,是否在浏览器中内嵌显示.
     * Content-disposition: inline 表示浏览器内嵌显示一个文件
     * Content-disposition: attachment 表示会下载文件
     * 内嵌显示还是下载,那一定是针对可内嵌显示的类型,例如"image/jpeg","image/png"
     * @param type
     * @param fileName
     * @return
     */
    private static HttpHeaders createContentDispositionHttpHeaders(String type, String fileName) {
        ContentDisposition.Builder builder = ContentDisposition.builder(type);
        if (StringUtils.isNotEmpty(fileName)) {
            builder.filename(new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        }
        ContentDisposition contentDisposition = builder.build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        return headers;
    }

    public static <T> ResponseEntity <T> createAttachmentResponseEntity(String fileName, T body) {
        return ResponseEntity.ok().headers(createAttachmentHttpHeaders(fileName)).body(body);
    }

    public static <T> ResponseEntity <T> createInlineResponseEntity(String fileName, T body) {
        return ResponseEntity.ok().headers(createInlineHttpHeaders(fileName)).body(body);
    }

}
