package com.kt.fire.service;

import com.kt.fire.entity.Users;
import com.kt.fire.entity.UserInterests;
import com.kt.fire.repository.UsersRepository;
import com.kt.fire.repository.UserInterestsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UsersRepository usersRepository;
    private final UserInterestsRepository userInterestsRepository;

    /**
     * 관심 지역과 함께 새로운 사용자 등록
     */
    @Transactional
    public Users register(String email, String password, String name, List<String> districtIds) {
        if (usersRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다");
        }

        Users user = usersRepository.save(Users.builder()
            .email(email)
            .password(password)
            .name(name)
            .build());

        for (String districtId : districtIds) {
            UserInterests interest = UserInterests.builder()
                .userId(user.getId())
                .districtId(districtId)
                .build();
            userInterestsRepository.save(interest);
        }

        return user;
    }

    /**
     * 사용자 로그인
     */
    public Optional<Users> login(String email, String password) {
        return usersRepository.findByEmailAndPassword(email, password);
    }

    /**
     * 사용자와 관심 지역 정보 조회
     */
    public List<String> getUserDistrictIds(Long userId) {
        return userInterestsRepository.findDistrictIdsByUserId(userId);
    }

    @Transactional
    public void updateUserInterests(Long userId, List<String> districtIds) {
        try {
            // 사용자 존재 여부 확인
            if (!usersRepository.existsById(userId)) {
                throw new IllegalArgumentException("사용자를 찾을 수 없습니다");
            }

            // 기존 관심 지역 조회
            List<String> existingDistrictIds = userInterestsRepository.findDistrictIdsByUserId(userId);
            
            // 선택 해제된 관심 지역 삭제
            for (String existingId : existingDistrictIds) {
                if (!districtIds.contains(existingId)) {
                    userInterestsRepository.deleteByUserIdAndDistrictId(userId, existingId);
                }
            }
            
            // 새로운 관심 지역 추가
            for (String districtId : districtIds) {
                if (!existingDistrictIds.contains(districtId)) {
                    UserInterests interest = UserInterests.builder()
                        .userId(userId)
                        .districtId(districtId)
                        .build();
                    userInterestsRepository.save(interest);
                }
            }
            
            System.out.println("사용자 관심 지역 업데이트 완료: " + userId);
        } catch (Exception e) {
            System.err.println("사용자 관심 지역 업데이트 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("사용자 관심 지역 업데이트 실패: " + e.getMessage());
        }
    }
} 