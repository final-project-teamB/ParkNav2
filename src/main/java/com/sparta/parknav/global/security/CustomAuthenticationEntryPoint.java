package com.sparta.parknav.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.parknav.global.exception.ErrorResponse;
import com.sparta.parknav.global.exception.ErrorType;
import com.sparta.parknav.global.response.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ErrorType exception = (ErrorType) request.getAttribute("exception");

        if (exception.equals(ErrorType.NOT_TOKEN)) {
            exceptionHandler(response, ErrorType.NOT_TOKEN);
            return;
        }

        if (exception.equals(ErrorType.NOT_VALID_TOKEN)) {
            exceptionHandler(response, ErrorType.NOT_VALID_TOKEN);
            return;
        }

        if (exception.equals(ErrorType.NOT_FOUND_USER)) {
            exceptionHandler(response, ErrorType.NOT_FOUND_USER);
        }
    }

    public void exceptionHandler(HttpServletResponse response, ErrorType error) {
        response.setStatus(error.getCode());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            String json = new ObjectMapper().writeValueAsString(ResponseUtils.error(ErrorResponse.of(error)));
            response.getWriter().write(json);
            log.error(error.getMsg());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
