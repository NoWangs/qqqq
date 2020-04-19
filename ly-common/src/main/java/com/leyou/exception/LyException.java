package com.leyou.exception;

import com.leyou.enums.ExceptionEnum;
import lombok.Getter;

@Getter
public class LyException extends RuntimeException {
    //增加状态码
    private int status;

    public LyException(int status,String message) {
        super(message);
        this.status=status;
    }
    public LyException(ExceptionEnum exceptionEnum){
        super(exceptionEnum.getMessage());
        this.status=exceptionEnum.getStatus();
    }
}
