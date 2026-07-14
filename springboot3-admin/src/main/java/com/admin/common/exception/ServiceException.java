package com.admin.common.exception;

import com.admin.common.result.ResultCodeEnum;
import lombok.Data;

@Data
public class ServiceException extends RuntimeException {
    private Integer code;

    public ServiceException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

    public ServiceException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMsg());
        this.code = resultCodeEnum.getCode();
    }
}