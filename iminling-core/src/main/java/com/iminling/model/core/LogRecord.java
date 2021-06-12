package com.iminling.model.core;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author konghang
 * @since 2020-07-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class LogRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Long accountId;

    /**
     * 请求ip
     */
    private String ip;

    /**
     * 请求url
     */
    private String uri;

    /**
     * 请求状态
     */
    private Integer responseStatus;

    /**
     * 请求参数
     */
    private String param;

    /**
     * 模块id
     */
    private Integer module;

    /**
     * 说明
     */
    private String description;

    /**
     * 请求时间
     */
    private Long requestTime;

    /**
     * 执行时长
     */
    private Long executeTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
