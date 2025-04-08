package com.yibei.supporttrack.entity.vo;

import com.yibei.supporttrack.exception.ApiException;

public class Asserts {
    public static void fail(String message) {
        throw new ApiException(message);
    }

    public static void fail(IErrorCode errorCode) {
        throw new ApiException(errorCode);
    }
}
