package io.github.zhangwei1989.zwregistry.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * exception for response
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/23
 */
@Data
@AllArgsConstructor
public class ExceptionResponse {

    private HttpStatus httpStatus;

    private String message;

}
