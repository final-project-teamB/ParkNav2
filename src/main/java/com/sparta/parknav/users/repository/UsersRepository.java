package com.sparta.parknav.users.repository;

import com.sparta.parknav.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByUsersId(String usersId);
}
