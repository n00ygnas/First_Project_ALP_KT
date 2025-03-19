package com.kt.fire.repository;

import com.kt.fire.entity.Regions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionsRepository extends JpaRepository<Regions, String> {
    
    // 회원가입 시 지역 선택을 위한 시/도별로 정렬된 모든 지역 조회
    @Query("select r from Regions r order by r.provinceName, r.districtName")
    List<Regions> findAllRegionsOrdered();

    // 시/도 이름으로 지역 조회
    List<Regions> findByProvinceNameOrderByDistrictName(String provinceName);

    // 시/도와 시/군/구 이름으로 지역 조회
    Optional<Regions> findByProvinceNameAndDistrictName(String provinceName, String districtName);

    // 지역 ID 목록으로 모든 지역 조회
    List<Regions> findByDistrictIdIn(List<String> districtIds);

    // 모든 시/도 이름 조회
    @Query("select distinct r.provinceName from Regions r order by r.provinceName")
    List<String> findDistinctProvinceName();
} 