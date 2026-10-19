package com.energy.User.Service.repository;

import com.energy.User.Service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long userId); // Does another user already have this email?
}
