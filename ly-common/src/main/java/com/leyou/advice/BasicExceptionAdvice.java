package com.leyou.advice;

import com.leyou.exception.ExceptionResult;
import com.leyou.exception.LyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice//凡是经过controller的都拦截
public class BasicExceptionAdvice {
    @ExceptionHandler(LyException.class)//拦截LyExcepition类型(RuntimeExcepton)
    public ResponseEntity<ExceptionResult> handleException(LyException e){
        return ResponseEntity.status(e.getStatus()).body(new ExceptionResult(e));
    }
}