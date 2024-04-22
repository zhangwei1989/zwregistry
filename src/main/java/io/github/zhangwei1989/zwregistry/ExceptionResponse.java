package io.github.zhangwei1989.zwregistry;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * Description for this class.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/22
 */
@Data
public class ExceptionResponse {

    private HttpStatus httpStatus;

    private String message;

    public ExceptionResponse(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

}
