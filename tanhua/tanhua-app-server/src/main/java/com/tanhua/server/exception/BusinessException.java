package com.tanhua.server.exception;

import lombok.Data;
import vo.ErrorResult;

/**
 * 自定义异常类
 */
@Data
public class BusinessException extends RuntimeException {

    private ErrorResult errorResult;

    public BusinessException(ErrorResult errorResult) {
        super(errorResult.getErrMessage());
        this.errorResult = errorResult;
    }
}