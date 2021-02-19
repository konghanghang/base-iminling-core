package com.iminling.model.common;

/**
 * 字符串枚举类,列举常用的字符串
 * @author yslao@outlook.com
 */
public enum StringEnum {

    ACCESS_TOKEN("ACCESS_TOKEN"),
    ERR_CODE("errcode"),
    CODE("code"),
    APP_ID("APPID"),
    REDIRECT_URI("REDIRECT_URI"),
    SCOPE("SCOPE"),
    STATE("STATE"),
    SECRET("SECRET"),
    OPENID("OPENID"),
    ERR_MSG("errmsg"),
    APP_SECRET("APPSECRET"),
    EXPIRES_IN("expires_in"),
    TICKET("ticket")
    ;

    private String name;

    StringEnum(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
