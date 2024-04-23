package io.github.zhangwei1989.zwregistry.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * common registry exception handler
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/23
 */
@RestControllerAdvice
public class RegistryExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ExceptionResponse handle(RuntimeException e) {
        return new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

}
