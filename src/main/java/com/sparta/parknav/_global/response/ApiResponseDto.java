package com.sparta.parknav._global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.parknav._global.exception.ErrorResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {

    private T data;
    private String msg;
    private ErrorResponse error;

    @Builder
    public ApiResponseDto(T data, String msg, ErrorResponse error) {
        this.data = data;
        this.msg = msg;
        this.error = error;
    }
}
