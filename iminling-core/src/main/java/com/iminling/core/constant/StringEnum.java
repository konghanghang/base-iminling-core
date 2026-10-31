package com.iminling.core.constant;

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
    TICKET("ticket"),
    AUTHORIZATION("Authorization"),
    REQUEST_DATA_KEY("global.requestData"),
    SPRING("spring");
    private final String desc;

    StringEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
