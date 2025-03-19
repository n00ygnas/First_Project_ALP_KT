package com.kt.fire.service;

import com.kt.fire.entity.Regions;
import com.kt.fire.repository.RegionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RegionService {

    @Autowired
    private RegionsRepository regionsRepository;

    // 시/도 목록 조회
    public List<String> getAllProvinces() {
        try {
            System.out.println("시/도 목록 조회 시도 중");
            List<String> provinces = regionsRepository.findDistinctProvinceName();
            System.out.println("조회된 시/도 목록: " + provinces);
            return provinces;
        } catch (Exception e) {
            System.err.println("시/도 목록 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 특정 시/도의 시/군/구 목록 조회
    public List<Regions> getDistrictsInProvince(String provinceName) {
        return regionsRepository.findByProvinceNameOrderByDistrictName(provinceName);
    }

    // 시/도와 시/군/구 기준으로 정렬된 모든 지역 조회
    public List<Regions> getAllRegionsOrdered() {
        return regionsRepository.findAllRegionsOrdered();
    }
} 