package com.yibei.supporttrack.entity.vo;

import lombok.Getter;

@Getter
public enum  ResultCode implements IErrorCode {
    /**
     *
     */

    SUCCESS(200, "操作成功"),

    BAD_REQUEST(400, "请求错误,"),
    UNAUTHORIZED(401, "未登录或登录令牌已经过期,请重新登录"),
    FORBIDDEN(403, "没有相关权限,"),
    NOT_FOUND(404, "未找到该资源,"),
    METHOD_NOT_ALLOWED(405, "不支持当前请求方法,"),
    Internal_SERVER_ERROR(500, "服务器内部错误,");

    private final long code;
    private final String message;

    private ResultCode(long code, String message) {
        this.code = code;
        this.message = message;
    }

}