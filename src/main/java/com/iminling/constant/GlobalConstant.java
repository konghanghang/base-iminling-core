package com.iminling.constant;

public class GlobalConstant {

    private GlobalConstant(){}

    public static final String AUTHORIZATION = "Authorization";

    /**
     * 用户类型，user代表c端，account代表后台
     */
    public static final String USER_TYPE_KEY = "userType";
    public static final String USER_TYPE_USER = "user";
    public static final String USER_TYPE_ACCOUNT = "account";

    /**
     * 日记公开状态
     */
    public static final int DIARY_STATUS_OPEN = 1;
    public static final int DIARY_STATUS_NOT_OPEN = 0;

    public static final String MINI_PROGRESS_ACCESS_TOKEN_KEY = "miniprogressaccesstoken";

}
