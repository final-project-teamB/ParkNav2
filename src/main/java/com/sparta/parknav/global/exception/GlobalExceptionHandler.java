package com.sparta.parknav.global.exception;
import com.sparta.parknav.global.response.ApiResponseDto;
import com.sparta.parknav.global.response.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = MethodArgumentNotValidException.class )
    public ResponseEntity<ApiResponseDto<Void>> methodValidException(MethodArgumentNotValidException e) {
        ErrorResponse responseDto = makeErrorResponse(e.getBindingResult());
        log.error("methodValidException throw Exception : {}", e.getBindingResult());
        return ResponseEntity.badRequest().body(ResponseUtils.error(responseDto));
    }

    @ExceptionHandler(value = CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("handleDataException throw Exception : {}", e.getErrorType());
        return ResponseEntity.badRequest().body(ErrorResponse.of(e.getErrorType()));
    }

    private ErrorResponse makeErrorResponse(BindingResult bindingResult) {
        String message = "";
        if (bindingResult.hasErrors()) {
            message = bindingResult.getAllErrors().get(0).getDefaultMessage();
        }
        return ErrorResponse.of(message, HttpStatus.BAD_REQUEST.value());
    }
}
