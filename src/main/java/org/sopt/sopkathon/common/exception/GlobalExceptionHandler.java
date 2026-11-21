package org.sopt.sopkathon.common.exception;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.sopt.sopkathon.common.code.GlobalErrorCode;
import org.sopt.sopkathon.common.dto.CommonResponse;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("[BusinessException] {}", e.getErrorCode().name());
        return CommonResponse.error(e.getErrorCode());
    }

    // @RequestBody DTO 검증 실패 (MethodArgumentNotValidException 포함)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<CommonResponse<Void>> handleBindException(BindException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(GlobalErrorCode.INVALID_INPUT.getMessage());
        return CommonResponse.error(GlobalErrorCode.INVALID_INPUT, message);
    }

    // @RequestParam, @PathVariable에 붙인 검증 어노테이션 실패 (Spring 6.1+)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<CommonResponse<Void>> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        String message = e.getAllErrors()
                .stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(GlobalErrorCode.INVALID_INPUT.getMessage());
        return CommonResponse.error(GlobalErrorCode.INVALID_INPUT, message);
    }

    // 예: sort=ABC 처럼 enum으로 변환할 수 없는 값
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CommonResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = "'" + e.getName() + "' 파라미터의 형식이 올바르지 않습니다.";
        return CommonResponse.error(GlobalErrorCode.INVALID_PARAMETER_TYPE, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CommonResponse<Void>> handleMissingParameter(MissingServletRequestParameterException e) {
        String message = "'" + e.getParameterName() + "' 파라미터는 필수입니다.";
        return CommonResponse.error(GlobalErrorCode.MISSING_PARAMETER, message);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<CommonResponse<Void>> handleMissingHeader(MissingRequestHeaderException e) {
        String message = "'" + e.getHeaderName() + "' 헤더는 필수입니다.";
        return CommonResponse.error(GlobalErrorCode.MISSING_PARAMETER, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        return CommonResponse.error(GlobalErrorCode.INVALID_INPUT);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CommonResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return CommonResponse.error(GlobalErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<CommonResponse<Void>> handleNoResource(NoResourceFoundException e) {
        return CommonResponse.error(GlobalErrorCode.NOT_FOUND);
    }

    // 예상하지 못한 예외: 로그에만 상세를 남기고 응답에는 일반 메시지만 노출
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
        log.error("[UnexpectedException]", e);
        return CommonResponse.error(GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }
}
