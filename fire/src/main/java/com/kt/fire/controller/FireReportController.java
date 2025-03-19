package com.kt.fire.controller;

import com.kt.fire.entity.FireReports;
import com.kt.fire.service.FireReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.List;

@RestController
@RequestMapping("/api/fire-reports")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class FireReportController {

    private final FireReportService fireReportService;
    private final ZoneId KOREA_TIMEZONE = ZoneId.of("Asia/Seoul");

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<FireReports>> getFireReportsForUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(fireReportService.getFireReportsForUser(userId, pageable));
    }

    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<?> getRecentFireReportsForUser(
            @PathVariable Long userId,
            @PageableDefault(size = 5, sort = "occurredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            System.out.println("Fetching recent fire reports for user: " + userId);
            
            if (userId == null) {
                System.err.println("User ID is null");
                return ResponseEntity.badRequest().body("User ID is required");
            }

            Page<FireReports> reports = fireReportService.getRecentFireReportsForUser(userId, pageable);
            
            if (reports.isEmpty()) {
                System.out.println("No reports found for user: " + userId);
                return ResponseEntity.ok()
                    .body(Page.empty()); 
            }
            
            System.out.println("Found " + reports.getContent().size() + " reports");
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            System.err.println("Error fetching fire reports: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok()
                .body(Page.empty());
        }
    }

    @GetMapping("/user/{userId}/search")
    public ResponseEntity<?> getFireReportsByDateRange(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @PageableDefault(size = 5, sort = "occurredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            System.out.println("Received date range search request - userId: " + userId + ", dates: " + startDate + " to " + endDate);
            
            if (userId == null) {
                return ResponseEntity.badRequest().body("사용자 ID가 필요합니다.");
            }
            
            if (startDate == null || endDate == null) {
                return ResponseEntity.badRequest().body("시작일과 종료일을 모두 입력해주세요.");
            }

            try {
                LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
                LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
                
                if (end.isBefore(start)) {
                    return ResponseEntity.badRequest().body("종료일이 시작일보다 빠를 수 없습니다.");
                }

                Page<FireReports> reports = fireReportService.getFireReportsByDateRange(userId, start, end, pageable);
                System.out.println("Found " + reports.getTotalElements() + " reports for date range");
                return ResponseEntity.ok(reports);
            } catch (Exception e) {
                System.err.println("Date parsing error: " + e.getMessage());
                return ResponseEntity.badRequest().body("날짜 형식이 올바르지 않습니다.");
            }
        } catch (Exception e) {
            System.err.println("Error searching fire reports: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("화재 정보 검색 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/user/{userId}/alerts")
    public ResponseEntity<?> getFireAlerts(@PathVariable Long userId) {
        try {
            System.out.println("=== Fire Alerts API Request ===");
            
            if (userId == null) {
                return ResponseEntity.badRequest().body("사용자 ID가 필요합니다.");
            }

            try {
                ZonedDateTime nowKst = ZonedDateTime.now(KOREA_TIMEZONE);
                
                ZonedDateTime oneHourAgoKst = nowKst.minusHours(1);
                
                LocalDateTime start = oneHourAgoKst.toLocalDateTime();
                LocalDateTime end = nowKst.toLocalDateTime();
                
                System.out.println("Checking alerts for last hour in KST");
                System.out.println("From: " + start);
                System.out.println("To: " + end);

                List<FireReports> alerts = fireReportService.getFireAlerts(userId, start, end);
                System.out.println("Found " + alerts.size() + " alerts in the last hour");
                return ResponseEntity.ok(alerts);
            } catch (Exception e) {
                System.err.println("Error processing time: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.badRequest().body("시간 처리 중 오류가 발생했습니다.");
            }
        } catch (Exception e) {
            System.err.println("Error fetching fire alerts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("화재 경보를 불러오는데 실패했습니다.");
        }
    }
} 