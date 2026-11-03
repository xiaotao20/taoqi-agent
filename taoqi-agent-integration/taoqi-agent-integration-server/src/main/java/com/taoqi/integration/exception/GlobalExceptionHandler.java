package com.taoqi.integration.exception;

import com.taoqi.integration.api.dto.EmailSendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理：把参数校验、请求体解析等异常统一转换为 {@code EmailSendResult}。
 *
 * @author xiaotao
 * @date 2026-11-03
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public EmailSendResult handleValidation(MethodArgumentNotValidException e) {
        return EmailSendResult.fail(e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; ")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public EmailSendResult handleUnreadableMessage(HttpMessageNotReadableException e) {
        return EmailSendResult.fail("请求体格式不正确: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public EmailSendResult handleException(Exception e) {
        log.error("请求处理失败", e);
        return EmailSendResult.fail(e.getMessage());
    }
}
