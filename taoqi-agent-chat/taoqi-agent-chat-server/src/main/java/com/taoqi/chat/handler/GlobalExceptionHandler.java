package com.taoqi.chat.handler;

import java.util.stream.Collectors;

import com.taoqi.common.exception.RateLimitExceededException;
import com.taoqi.common.result.R;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 聊天模块的全局异常处理：把限流异常转换为 429 响应。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 请求参数校验失败时返回 400 与统一错误结构。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return R.fail(HttpStatus.BAD_REQUEST.value(), message);
    }

    /**
     * 触发限流时返回 429，并通过 Retry-After 告知调用方建议的重试等待秒数。
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<R<Void>> handleRateLimitExceeded(RateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()))
                .body(R.fail(HttpStatus.TOO_MANY_REQUESTS.value(), ex.getMessage()));
    }
}
