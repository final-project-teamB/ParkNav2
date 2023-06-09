package com.sparta.parknav.user.service;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;
import com.sparta.parknav._global.jwt.JwtUtil;
import com.sparta.parknav.user.dto.LoginRequestDto;
import com.sparta.parknav.user.dto.SignupRequestDto;
import com.sparta.parknav.user.entity.Admin;
import com.sparta.parknav.user.entity.User;
import com.sparta.parknav.user.repository.AdminRepository;
import com.sparta.parknav.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public void signup(SignupRequestDto requestDto) {

        String encodePw = passwordEncoder.encode(requestDto.getPassword());

        Optional<User> foundById = userRepository.findByUserId(requestDto.getUserId());
        if (foundById.isPresent()) {
            throw new CustomException(ErrorType.DUPLICATED_USERID);
        }

        User user = User.of(requestDto.getUserId(), encodePw);
        userRepository.save(user);
    }

    public void login(LoginRequestDto requestDto, HttpServletResponse response) {

        String userId = requestDto.getUserId();
        String password = requestDto.getPassword();

        User user = userRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(ErrorType.NOT_MATCHING_INFO)
        );

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException(ErrorType.NOT_MATCHING_INFO);
        }

        String token = jwtUtil.createToken(user.getUserId());
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, token);
    }

    public void loginAdmin(LoginRequestDto requestDto, HttpServletResponse response) {

        String adminId = requestDto.getUserId();
        String password = requestDto.getPassword();

        Admin admin = adminRepository.findByAdminId(adminId).orElseThrow(
                () -> new CustomException(ErrorType.NOT_MATCHING_INFO)
        );

        if (!password.equals(admin.getPassword())) {
            throw new CustomException(ErrorType.NOT_MATCHING_INFO);
        }

        String token = jwtUtil.createToken(admin.getAdminId());
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, token);
    }
}
