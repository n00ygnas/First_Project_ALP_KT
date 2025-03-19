package com.kt.fire.service;

import com.kt.fire.entity.FireReports;
import com.kt.fire.repository.FireReportsRepository;
import com.kt.fire.repository.UserInterestsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FireReportService {

    private final FireReportsRepository fireReportsRepository;
    private final UserInterestsRepository userInterestsRepository;
    private final UserService userService;

    // 사용자의 관심 지역에 대한 화재 보고서 조회
    public Page<FireReports> getFireReportsForUser(Long userId, Pageable pageable) {
        List<String> districtIds = userInterestsRepository.findDistrictIdsByUserId(userId);
        return fireReportsRepository.findByDistrictIdsOrderByOccurredAtDesc(districtIds, pageable);
    }

    // 사용자의 관심 지역에 대한 최근 화재 보고서 조회
    public Page<FireReports> getRecentFireReportsForUser(Long userId, Pageable pageable) {
        try {
            if (userId == null) {
                System.err.println("사용자 ID가 null입니다");
                return Page.empty();
            }

            System.out.println("사용자 ID에 대한 지역 ID 조회 중: " + userId);
            List<String> districtIds = userInterestsRepository.findDistrictIdsByUserId(userId);
            System.out.println("조회된 지역 ID 목록: " + districtIds);
            
            if (districtIds == null || districtIds.isEmpty()) {
                System.out.println("해당 사용자의 관심 지역이 없습니다: " + userId);
                return Page.empty();
            }

            System.out.println("다음 지역들의 화재 보고서 조회 중: " + districtIds);
            try {
                Page<FireReports> reports = fireReportsRepository.findRecentByDistrictIds(districtIds, pageable);
                System.out.println("조회된 화재 보고서 수: " + reports.getContent().size());
                return reports;
            } catch (Exception e) {
                System.err.println("화재 보고서 쿼리 실행 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("화재 보고서 조회 중 데이터베이스 오류 발생", e);
            }
        } catch (Exception e) {
            System.err.println("최근 화재 보고서 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("화재 보고서 조회 실패: " + e.getMessage(), e);
        }
    }

    // 사용자의 관심 지역에 대한 특정 기간 화재 보고서 조회
    public Page<FireReports> getFireReportsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        try {
            if (userId == null) {
                System.err.println("사용자 ID가 null입니다");
                return Page.empty();
            }

            System.out.println("사용자 ID에 대한 지역 ID 조회 중: " + userId);
            List<String> districtIds = userInterestsRepository.findDistrictIdsByUserId(userId);
            System.out.println("조회된 지역 ID 목록: " + districtIds);
            
            if (districtIds == null || districtIds.isEmpty()) {
                System.out.println("해당 사용자의 관심 지역이 없습니다: " + userId);
                return Page.empty();
            }

            System.out.println("다음 지역들의 화재 보고서 조회 중: " + districtIds);
            System.out.println("조회 기간: " + startDate + " ~ " + endDate);
            
            return fireReportsRepository.findByDistrictIdsAndDateRange(
                districtIds,
                startDate,
                endDate,
                pageable
            );
        } catch (Exception e) {
            System.err.println("기간별 화재 보고서 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("화재 보고서 조회 실패: " + e.getMessage());
        }
    }

    // 실시간 화재 경보 조회
    public List<FireReports> getFireAlerts(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            System.out.println("=== 화재 경보 디버그 정보 ===");
            System.out.println("사용자 ID: " + userId);
            System.out.println("시작 시간: " + startTime);
            System.out.println("종료 시간: " + endTime);

            // 사용자의 관심 지역 ID 목록 조회
            List<String> userDistrictIds = userService.getUserDistrictIds(userId);
            System.out.println("사용자 관심 지역 ID 목록: " + userDistrictIds);
            
            if (userDistrictIds.isEmpty()) {
                System.out.println("해당 사용자의 관심 지역이 없습니다: " + userId);
                return new ArrayList<>();
            }

            System.out.println("다음 기간 동안의 화재 보고서 조회 중: " + startTime + " ~ " + endTime);
            List<FireReports> alerts = fireReportsRepository.findByDistrictIdInAndOccurredAtBetweenOrderByOccurredAtDesc(
                userDistrictIds,
                startTime,
                endTime
            );
            
            System.out.println("조회된 경보 수: " + alerts.size());
            if (!alerts.isEmpty()) {
                System.out.println("첫 번째 경보 시간: " + alerts.get(0).getOccurredAt());
                System.out.println("마지막 경보 시간: " + alerts.get(alerts.size() - 1).getOccurredAt());
            }
            
            return alerts;
        } catch (Exception e) {
            System.err.println("화재 경보 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
} 