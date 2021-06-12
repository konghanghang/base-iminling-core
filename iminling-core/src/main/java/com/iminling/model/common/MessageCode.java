package com.iminling.model.common;

import com.iminling.common.json.JsonUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Hashtable;

/**
 * 状态枚举类
 * @author yslao@outlook.com
 */
public enum MessageCode {

    RESULT_OK(200, "请求成功"),
    RESULT_FAIL(500, "系统异常"),

    INTERNET_ERROR_400(400, "Bad Request!"),
    INTERNET_ERROR_405(405, "Method Not Allowed"),
    INTERNET_ERROR_406(406, "Not Acceptable"),

    /*token*/
    TOKEN_NULL(401, "token为空"),
    TOKEN_EXPIRED(401, "token过期"),
    TOKEN_INVALID_CLAIM(401, "token过期(Claim),请重新登录"),
    TOKEN_ERROR(401, "token校验失败"),
    TOKEN_SIGNATURE_ERROR(401, "签名校验失败"),
    PERMISSION_DENY(403, "无权操作"),

    /*参数*/
    PARAM_IS_NULL(800, "参数为空"),

    /*图片*/
    IMAGE_FORMAT_ERROR(900, "图片格式不正确"),

    /*用户模块开始*/
    USER_MODEL_BEGIN(1000, "用户模块错误码开始"),
    USER_NOT_ACTIVATION(1001, "用户还没有激活,请查看邮件激活用户."),
    USER_UNKNOWN_ACCOUNT(401, "未知账户"),
    USER_ERROR_PASSWORD(401, "错误的凭证,密码错误"),
    USER_ERROR_PASSWORD_MUCH(1004, "用户名或密码错误次数过多"),
    USER_ACCOUNT_LOCKED(1005, "账户已锁定"),
    USER_NAME_EXIST(1007, "用户名已存在"),
    USER_EMAIL_EXIST(1008, "邮箱已存在"),
    USER_UNAUTHORIZED(1009, "授权失败"),
    USER_CREATE_ERROR(1010, "用户创建失败"),
    USER_ROLE_CREATE_ERROR(1011, "用户角色添加失败"),
    USER_MODEL_END(1999, "用户模块错误码结束"),
    /*用户模块结束*/

    /*文章模块开始*/
    ARTICLE_MODEL_BEGIN(2000, "文章模块错误码开始"),
    ARTICLE_ID_ERROR(2000, "无效文章ID"),
    ARTICLE_MODEL_END(2999, "文章模块错误码开始"),
    /*文章模块结束*/

    JOB_BEGIN(3000, "任务模块开始"),
    JOB_NOT_FIND(3001, "没有找到任务"),
    JOB_ADD_ERROR(3002, "添加任务失败"),
    JOB_PAUSE_ERROR(3003, "暂停任务失败"),
    JOB_RESUME_ERROR(3004, "恢复任务失败"),
    JOB_EXIST(3005, "任务已存在"),
    JOB_END(3999, "任务模块开始"),

    SYSTEM_BEGIN(91000, "系统模块开始"),

    TRANSACTION_ERROR(92000, "事务异常"),

    SYSTEM_ERROR(91500, "系统错误"),

    SYSTEM_FAIL(91599, "系统相关"),

    ERROR_MAX(999999999, "错误");

    @Setter @Getter private int code;
    @Setter @Getter private String message;

    MessageCode(int id, String msg) {
        this.code = id;
        this.message = msg;
    }

    public Hashtable<Object, Object> toJson() {
        Hashtable<Object, Object> json = new Hashtable<Object, Object>();
        json.put("error", code);
        json.put("msg", message);
        return json;
    }

    @Override
    public String toString() {
        return JsonUtil.obj2Str(toJson());
    }

    public static Integer getErrorIdByErrorMsg(String errorMsg) {
        for (MessageCode messageCode : MessageCode.values()) {
            if (errorMsg.equals(messageCode.getMessage())) {
                return messageCode.getCode();
            }
        }
        return null;
    }
}
