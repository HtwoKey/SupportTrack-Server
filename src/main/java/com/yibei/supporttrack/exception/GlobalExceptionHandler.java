package com.yibei.supporttrack.exception;

import com.yibei.supporttrack.entity.vo.CommonResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = ApiException.class)
    public CommonResult<?> handle(ApiException e) {
        if (e.getErrorCode() != null) {
            return CommonResult.failed(e.getErrorCode());
        }
        return CommonResult.failed(e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public CommonResult<?> handleValidException(BindException e) {
        String message = extractErrorMessage(e.getBindingResult());
        return CommonResult.validateFailed(message);
    }

    private String extractErrorMessage(BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) return null;
        
        FieldError fieldError = bindingResult.getFieldError();
        if (fieldError == null) return null;
        
        return fieldError.getField() + fieldError.getDefaultMessage();
    }

    @ResponseBody
    @ExceptionHandler(value = NoHandlerFoundException.class)
    public CommonResult<?> defaultErrorHandler(HttpServletRequest req, HttpServletResponse resp, NoHandlerFoundException e) {
        log.warn("Resource not found: {} {}", req.getMethod(), req.getRequestURI());
        return CommonResult.notFound(e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public CommonResult<?> handle(Exception e) {
        log.error("Internal Server Error: ", e); // 记录完整堆栈
        return CommonResult.internalServerError("Server busy, please try again later");
    }
}
