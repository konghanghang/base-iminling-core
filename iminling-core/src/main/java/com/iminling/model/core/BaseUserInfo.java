package com.iminling.model.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author yslao@outlook.com
 * @since 2021/2/19
 */
@Data
public class BaseUserInfo {

    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户密码
     */
    @JsonIgnore
    private String password;

    /**
     * 性别
     */
    private Byte sex;

    /**
     * 邮箱
     */
    private String email;

    /**
     *
     */
    private String headImage;

    /**
     * 创建ip
     */
    private String createIp;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
