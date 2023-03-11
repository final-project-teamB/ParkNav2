package com.sparta.parknav.global.security;

import com.sparta.parknav.global.exception.ErrorType;
import com.sparta.parknav.users.entity.Users;
import com.sparta.parknav.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String usersId) throws UsernameNotFoundException {
        Users user = usersRepository.findByUsersId(usersId)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorType.NOT_FOUND_USER.getMsg()));   // 사용자가 DB 에 없으면 예외처리

        return new UserDetailsImpl(user, user.getUsersId());   // 사용자 정보를 UserDetails 로 반환
    }

}