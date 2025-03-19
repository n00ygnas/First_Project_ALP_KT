package com.kt.fire.repository;

import com.kt.fire.entity.FireReports;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FireReportsRepository extends JpaRepository<FireReports, Long> {
    
    // 사용자가 선택한 지역의 화재 보고서 조회 (페이지네이션)
    @Query("select distinct f from FireReports f " +
           "left join fetch f.region r " +
           "where f.districtId in :districtIds " +
           "order by f.occurredAt desc")
    Page<FireReports> findByDistrictIdsOrderByOccurredAtDesc(
        @Param("districtIds") List<String> districtIds, 
        Pageable pageable
    );

    // 사용자가 선택한 지역의 최근 화재 보고서 조회
    @Query("select distinct f from FireReports f " +
           "left join fetch f.region r " +
           "where f.districtId in :districtIds " +
           "order by f.occurredAt desc")
    Page<FireReports> findRecentByDistrictIds(
        @Param("districtIds") List<String> districtIds,
        Pageable pageable
    );

    // 사용자가 선택한 지역의 특정 기간 내 화재 보고서 조회
    @Query("select distinct f from FireReports f " +
           "left join fetch f.region r " +
           "where f.districtId in :districtIds " +
           "and f.occurredAt between :startDate AND :endDate " +
           "order by f.occurredAt desc")
    Page<FireReports> findByDistrictIdsAndDateRange(
        @Param("districtIds") List<String> districtIds,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    // 지역별 화재 보고서 수 집계
    @Query("select f.districtId, count(f) as count " +
           "from FireReports f " +
           "where f.districtId in :districtIds " +
           "and f.occurredAt between :startDate and :endDate " +
           "group by f.districtId")
    List<Object[]> countFireReportsByDistrict(
        @Param("districtIds") List<String> districtIds,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // 특정 시점 이후의 화재 보고서 조회
    @Query("select distinct f from FireReports f " +
           "left join fetch f.region r " +
           "where f.districtId in :districtIds " +
           "AND f.occurredAt > :after " +
           "order by f.occurredAt desc")
    List<FireReports> findByDistrictIdInAndOccurredAtAfterOrderByOccurredAtDesc(
        @Param("districtIds") List<String> districtIds,
        @Param("after") LocalDateTime after
    );

    // 특정 기간 내의 화재 보고서 조회
    @Query("select distinct f from FireReports f " +
           "left join fetch f.region r " +
           "where f.districtId in :districtIds " +
           "and f.occurredAt between :startTime AND :endTime " +
           "order by f.occurredAt desc")
    List<FireReports> findByDistrictIdInAndOccurredAtBetweenOrderByOccurredAtDesc(
        @Param("districtIds") List<String> districtIds,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
} 