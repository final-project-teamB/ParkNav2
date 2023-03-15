package com.sparta.parknav.user.service;

import com.sparta.parknav.global.exception.CustomException;
import com.sparta.parknav.global.exception.ErrorType;
import com.sparta.parknav.global.response.ApiResponseDto;
import com.sparta.parknav.global.response.MsgType;
import com.sparta.parknav.global.response.ResponseUtils;
import com.sparta.parknav.user.dto.SignupRequestDto;
import com.sparta.parknav.user.entity.User;
import com.sparta.parknav.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public ApiResponseDto<Void> signup(SignupRequestDto requestDto) {

        String encodePw = passwordEncoder.encode(requestDto.getPassword());

        Optional<User> foundById = userRepository.findByUserId(requestDto.getUserId());
        if (foundById.isPresent()) {
            throw new CustomException(ErrorType.DUPLICATED_USERID);
        }

        User user = User.of(requestDto.getUserId(), encodePw);
        userRepository.save(user);

        return ResponseUtils.ok(MsgType.SIGNUP_SUCCESSFULLY);
    }

}
