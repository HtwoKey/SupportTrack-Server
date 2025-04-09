package com.yibei.supporttrack.entity.vo;

public interface IErrorCode {
    /**
     * 返回码
     * @return long
     */
    long getCode();

    /**
     * 返回信息
     * @return String
     */
    String getMessage();
}
