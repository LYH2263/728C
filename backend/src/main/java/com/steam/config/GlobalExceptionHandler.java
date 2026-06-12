package com.steam.config;

import com.steam.dto.Result;
import com.steam.enums.ErrorCode;
import com.steam.exception.BaseException;
import com.steam.exception.BusinessException;
import com.steam.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: [code={}] {}", e.getCode(), e.getMessage());
        Result<Void> body = Result.error(e.getCode(), e.getMessage());
        return ResponseEntity.status(adaptHttpStatus(e.getHttpStatus())).body(body);
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Result<Void>> handleSystemException(SystemException e) {
        log.error("系统异常: [code={}] {}", e.getCode(), e.getMessage(), e);
        Result<Void> body = Result.error(ErrorCode.SYSTEM_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Result<Void>> handleBaseException(BaseException e) {
        log.warn("基础异常: [code={}] {}", e.getCode(), e.getMessage());
        Result<Void> body = Result.error(e.getCode(), e.getMessage());
        return ResponseEntity.status(adaptHttpStatus(e.getHttpStatus())).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        Result<Void> body = Result.error(ErrorCode.PARAM_VALIDATION_FAILED, message);
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        Result<Void> body = Result.error(ErrorCode.PARAM_BIND_FAILED, message);
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        Result<Void> body = Result.error(ErrorCode.BAD_REQUEST, e.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> handleRuntimeException(RuntimeException e) {
        log.error("未分类运行时异常（请迁移为 BusinessException）: {}", e.getMessage(), e);
        Result<Void> body = Result.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("未捕获异常: ", e);
        Result<Void> body = Result.error(ErrorCode.SYSTEM_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private HttpStatus adaptHttpStatus(HttpStatus status) {
        if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN
                || status == HttpStatus.TOO_MANY_REQUESTS) {
            return status;
        }
        return HttpStatus.OK;
    }
}
