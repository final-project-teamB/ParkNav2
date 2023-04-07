package com.sparta.parknav.user.service;

import com.sparta.parknav._global.exception.CustomException;
import com.sparta.parknav._global.exception.ErrorType;
import com.sparta.parknav._global.jwt.JwtUtil;
import com.sparta.parknav.parking.entity.ParkInfo;
import com.sparta.parknav.user.dto.LoginRequestDto;
import com.sparta.parknav.user.dto.SignupRequestDto;
import com.sparta.parknav.user.entity.Admin;
import com.sparta.parknav.user.entity.User;
import com.sparta.parknav.user.repository.AdminRepository;
import com.sparta.parknav.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("유저 테스트")
class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AdminRepository adminRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("회원가입-성공")
    void signupTest() {
        SignupRequestDto signupRequestDto = SignupRequestDto.of("test1", "123123");
        userService.signup(signupRequestDto);

    }

    @Test
    @DisplayName("회원가입-실패")
    void signupFailTest() {
        SignupRequestDto signupRequestDto = SignupRequestDto.of("test1", "123123");
        User user = User.of("test1", "1234");
        when(userRepository.findByUserId(any())).thenReturn(Optional.of(user));
        Exception exception = assertThrows(CustomException.class, () -> {
            userService.signup(signupRequestDto);
        });
        assertEquals(exception.getMessage(), ErrorType.DUPLICATED_USERID.getMsg());

    }

    @Test
    @DisplayName("로그인-성공")
    void loginTest() {
        LoginRequestDto loginRequestDto = LoginRequestDto.of("test1", "123123");
        HttpServletResponse responseMock = mock(HttpServletResponse.class);
        User user = User.of("test1", "1234");
        when(userRepository.findByUserId(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        userService.login(loginRequestDto, responseMock);
    }

    @Test
    @DisplayName("로그인-아이디-실패")
    void loginFailIdTest() {
        LoginRequestDto loginRequestDto = LoginRequestDto.of("test1", "123123");
        HttpServletResponse responseMock = mock(HttpServletResponse.class);
        when(userRepository.findByUserId(any())).thenReturn(Optional.empty());
        Exception exception = assertThrows(CustomException.class, () ->
                userService.login(loginRequestDto, responseMock)
        );
        assertEquals(exception.getMessage(), ErrorType.NOT_MATCHING_INFO.getMsg());

    }

    @Test
    @DisplayName("로그인-패스워드-실패")
    void loginFailPwTest() {
        LoginRequestDto loginRequestDto = LoginRequestDto.of("test1", "123123");
        HttpServletResponse responseMock = mock(HttpServletResponse.class);
        User user = User.of("test1", passwordEncoder.encode("5678"));
        when(userRepository.findByUserId(any())).thenReturn(Optional.of(user));
        Exception exception = assertThrows(CustomException.class, () -> {
            userService.login(loginRequestDto, responseMock);
        });
        assertEquals(exception.getMessage(), ErrorType.NOT_MATCHING_INFO.getMsg());
    }
    
    @Test
    @DisplayName("관리자로그인-성공")
    void adminLoginTest(){
        LoginRequestDto loginRequestDto = LoginRequestDto.of("admin","1234");
        ParkInfo parkInfo = ParkInfo.of("테스트주차장", "주소1", "주소2", "31","123");;
        Admin admin = Admin.of("admin","1234",parkInfo);
        when(adminRepository.findByAdminId(loginRequestDto.getUserId())).thenReturn(Optional.of(admin));
        adminRepository.save(admin);
        HttpServletResponse responseMock = mock(HttpServletResponse.class);
        userService.loginAdmin(loginRequestDto,responseMock);
    }

    @Test
    @DisplayName("관리자로그인-아이디-실패")
    void adminLoginFailIdTest(){
        LoginRequestDto loginRequestDto = LoginRequestDto.of("admin2","1234");
        ParkInfo parkInfo = ParkInfo.of("테스트주차장", "주소1", "주소2", "31","123");;
        Admin admin = Admin.of("admin","1234",parkInfo);
        adminRepository.save(admin);
        HttpServletResponse responseMock = mock(HttpServletResponse.class);
        Exception exception = assertThrows(CustomException.class, () -> {
            userService.loginAdmin(loginRequestDto,responseMock);
        });
        assertEquals(exception.getMessage(), ErrorType.NOT_MATCHING_INFO.getMsg());
    }

    @Test
    @DisplayName("관리자로그인-패스워드-실패")
    void adminLoginFailPwTest(){
        LoginRequestDto loginRequestDto = LoginRequestDto.of("admin","12345");
        ParkInfo parkInfo = ParkInfo.of("테스트주차장", "주소1", "주소2", "31","123");;
        Admin admin = Admin.of("admin", "1234",parkInfo);
        adminRepository.save(admin);
        when(adminRepository.findByAdminId(any())).thenReturn(Optional.of(admin));
        HttpServletResponse responseMock = mock(HttpServletResponse.class);
        Exception exception = assertThrows(CustomException.class, () -> {
            userService.loginAdmin(loginRequestDto,responseMock);
        });
        assertEquals(exception.getMessage(), ErrorType.NOT_MATCHING_INFO.getMsg());
    }
}
