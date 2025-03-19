package com.kt.fire.repository;

import com.kt.fire.entity.UserInterests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInterestsRepository extends JpaRepository<UserInterests, Long> {
    
    // 사용자의 모든 관심 지역 ID 조회
    @Query("select ui.districtId from UserInterests ui where ui.userId = :userId")
    List<String> findDistrictIdsByUserId(@Param("userId") Long userId);

    // 사용자 ID로 모든 관심 지역 조회
    List<UserInterests> findByUserId(Long userId);

    // 사용자 ID로 모든 관심 지역 삭제
    void deleteByUserId(Long userId);

    // 사용자 ID와 지역 ID로 특정 관심 지역 삭제
    void deleteByUserIdAndDistrictId(Long userId, String districtId);

    // 사용자가 특정 지역에 관심이 있는지 확인
    boolean existsByUserIdAndDistrictId(Long userId, String districtId);

    // 각 지역별 관심 있는 사용자 수 집계
    @Query("select ui.districtId, count(ui) from UserInterests ui group by ui.districtId")
    List<Object[]> countUsersByDistrict();
} 