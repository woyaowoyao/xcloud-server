package com.xxx.xcloud.common.exception;

import com.xxx.xcloud.common.ApiResult;
import com.xxx.xcloud.common.ReturnCode;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

/**
 * 全局异常处理类
 *
 * @author mengaijun
 * @date: 2019年2月28日 下午5:49:07
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 用来处理bean validation异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ApiResult resolveConstraintViolationException(ConstraintViolationException ex) {
        ApiResult apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "");
        Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
        if (!CollectionUtils.isEmpty(constraintViolations)) {
            StringBuilder msgBuilder = new StringBuilder();
            for (ConstraintViolation constraintViolation : constraintViolations) {
                msgBuilder.append(constraintViolation.getMessage()).append(",");
            }
            String errorMessage = msgBuilder.toString();
            if (errorMessage.length() > 1) {
                errorMessage = errorMessage.substring(0, errorMessage.length() - 1);
            }
            apiResult.setMessage(errorMessage);
            return apiResult;
        }
        apiResult.setMessage(ex.getMessage());
        return apiResult;
    }

    /**
     * 用来处理参数 validation异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ApiResult resolveMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ApiResult apiResult = new ApiResult(ReturnCode.CODE_CHECK_PARAM_IS_NOT_FORMAT, "");
        List<ObjectError> objectErrors = ex.getBindingResult().getAllErrors();
        if (!CollectionUtils.isEmpty(objectErrors)) {
            StringBuilder msgBuilder = new StringBuilder();
            for (ObjectError objectError : objectErrors) {
                msgBuilder.append(objectError.getDefaultMessage()).append(",");
            }
            String errorMessage = msgBuilder.toString();
            if (errorMessage.length() > 1) {
                errorMessage = errorMessage.substring(0, errorMessage.length() - 1);
            }
            apiResult.setMessage(errorMessage);
            return apiResult;
        }
        apiResult.setMessage(ex.getMessage());
        return apiResult;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ApiResult resolveHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        ApiResult apiResult = new ApiResult(HttpStatus.METHOD_NOT_ALLOWED.value(), "");
        apiResult.setMessage(ex.getMessage());
        return apiResult;
    }

}
