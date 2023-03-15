package com.sparta.parknav.global.security;

import com.sparta.parknav.global.exception.ErrorType;
import com.sparta.parknav.user.entity.Admin;
import com.sparta.parknav.user.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDetailsServiceImpl implements UserDetailsService {

    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String adminId) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorType.NOT_FOUND_USER.getMsg()));   // 사용자가 DB 에 없으면 예외처리

        return new AdminDetailsImpl(admin, admin.getAdminId());   // 사용자 정보를 UserDetails 로 반환
    }

}
