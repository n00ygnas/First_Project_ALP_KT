package com.kt.fire.repository;

import com.kt.fire.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    
    // 로그인 및 이메일 중복 확인을 위한 사용자 조회
    Optional<Users> findByEmailAndPassword(String email, String password);

    // 이메일 중복 확인
    boolean existsByEmail(String email);

    // 사용자와 선택한 지역 정보 함께 조회
    @Query("select u from Users u left join fetch u.userInterests where u.id = :userId")
    Optional<Users> findUserWithInterests(@Param("userId") Long userId);
} 