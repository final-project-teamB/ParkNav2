package com.sparta.parknav._global.security;

import com.sparta.parknav._global.exception.ErrorType;
import com.sparta.parknav.user.entity.User;
import com.sparta.parknav.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorType.NOT_FOUND_USER.getMsg()));   // 사용자가 DB 에 없으면 예외처리

        return new UserDetailsImpl(user, user.getUserId());   // 사용자 정보를 UserDetails 로 반환
    }

}