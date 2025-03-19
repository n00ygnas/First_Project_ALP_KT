package com.kt.fire.service;

import com.kt.fire.entity.FireReports;
import com.kt.fire.repository.FireReportsRepository;
import com.kt.fire.repository.RegionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class FireIncidentGenerator {

    private final FireReportsRepository fireReportsRepository;
    private final RegionsRepository regionsRepository;
    private final Random random = new Random();

    private static final String[] FIRE_TYPES = {
            "건축, 구조물",
            "임야",
            "자동차, 철도차량",
            "선박, 항공기",
            "위험물,가스 제조소 등",
            "기타(쓰레기 화재 등)"
    };

    private static final String[] IGNITION_CAUSES = {
            "폭죽놀이",
            "미상",
            "자연발화",
            "절연열화에 의한 단락",
            "음식물 조리중",
            "방화",
            "담배꽁초",
            "가연물 근접방치",
            "반단선",
            "과열, 과부하",
            "미확인단락",
            "불씨,불꽃,화원방치",
            "기타(전기적요인)",
            "과부하/과전류",
            "트래킹에 의한 단락",
            "접촉불량에 의한 단락",
            "압착,손상에 의한 단락",
            "쓰레기 소각",
            "기타(부주의)",
            "혼촉발화",
            "불장난",
            "누전,지락",
            "용접, 절단, 연마",
            "논,임야태우기",
            "기타(기계적요인)",
            "방화의심",
            "자동제어 실패",
            "노후",
            "오일,연료누설",
            "유류 취급중",
            "기타(화학적요인)",
            "교통사고",
            "정비불량",
            "기타",
            "가스누출(폭발)",
            "수동제어 실패",
            "빨래삶기",
            "역화",
            "기타(자연적인요인)",
            "화학적 폭발",
            "화학적 발화(유증기 확산)",
            "층간단락",
            "금수성물질의 물과 접촉",
            "돋보기 효과",
            "자연적 재해",
            "기기(전기, 기계 등) 사용.설치부주의",
            "설계상결함",
            "기타(제품결함)",
            "제조상결함"
    };

    private static final String[] LOCATION_CATEGORIES = {
            "기타야외",
            "단독주택",
            "승용자동차",
            "야적장",
            "기타 건축물",
            "아파트",
            "기타 음식점",
            "쓰레기",
            "한식",
            "여관",
            "제재 및 목공업",
            "특수자동차",
            "횟집",
            "치킨,족발",
            "일반주점",
            "카센터",
            "일반빌딩",
            "모텔",
            "일식",
            "테니스장, 정구장",
            "사찰",
            "다가구주택",
            "기타 들불",
            "기타 동식물시설",
            "숲",
            "사유림",
            "기타 작업장",
            "기타 발전시설",
            "연립주택",
            "상가빌딩",
            "식료품공업",
            "기타도로",
            "과수원",
            "화물자동차",
            "공터",
            "금속기계 및 기구공업",
            "대학교",
            "논밭두렁",
            "고등학교",
            "기타 창고",
            "병원",
            "호프집",
            "일반상점",
            "한식(휴게)",
            "주상복합아파트",
            "분식",
            "목욕장(사우나, 찜질방)",
            "간이음식점",
            "경로당",
            "중식",
            "가로등",
            "창고, 물품저장소",
            "화학공업",
            "컨테이너(주거용)",
            "기타 일상서비스",
            "그 밖의 공업",
            "치킨,족발(휴게)",
            "방직공업",
            "오토바이",
            "다세대주택",
            "시장",
            "철공소",
            "버섯재배사",
            "기타 교정시설",
            "전통시장",
            "상가주택(주택부분에서 화재가 발생한 경우에만 해당)",
            "돈사",
            "어선",
            "우사",
            "일반회사",
            "금융기관(농협 등)",
            "버스",
            "전봇대",
            "다중주택",
            "기타 의료시설",
            "백화점",
            "기타 농업기계",
            "기타 공동주택",
            "자동차검사장, 매매장, 부속상, 정비공장",
            "목공소, 건재상",
            "농예용 온실",
            "단란주점",
            "유흥주점",
            "기타 단독주택",
            "폐기물재활용시설",
            "오피스텔",
            "이미용실",
            "인쇄업",
            "원자력발전소",
            "초등학교",
            "덤프트럭",
            "기타 역사,터미널",
            "마사지(안마, 지압 등)",
            "기타 선박",
            "공업사",
            "기타 운동시설",
            "전기, 전자공업",
            "상점가",
            "패스트푸드",
            "수리,A/S,상하수도",
            "사당",
            "기타 판매시설",
            "세차장, 폐차장",
            "교회",
            "일반학원",
            "할인점(마트)",
            "기타 일반업무시설",
            "굴삭기",
            "기타 주택",
            "트랙터",
            "폐기물처리시설",
            "커피숍,다방,카페(휴게)",
            "고시원(원룸텔)",
            "계사",
            "기타 자동차시설",
            "모닥불",
            "여인숙",
            "모델하우스",
            "유치원",
            "화장품",
            "공중화장실",
            "국가 및 지방자치단체 청사",
            "기타 건설기계",
            "고철처리소, 고물상",
            "들판",
            "기타 승합자동차",
            "종합학원",
            "목초지",
            "비닐하우스",
            "소형 승합차",
            "꽃집(화원)",
            "펜션",
            "농장",
            "볏짚",
            "광업",
            "노래연습장",
            "캠핑용 자동차,트레일러",
            "PC방(인터넷게임제공업)",
            "펄프 및 제지공업",
            "기타휴게음식점",
            "세탁소",
            "기타 관람장",
            "노상매점",
            "어린이집",
            "지하철역사",
            "외국어학원",
            "묘지",
            "양식",
            "공유림",
            "요업 및 토석공업",
            "기타 위락시설",
            "석유공업",
            "노인복지시설",
            "체력단련장(헬스장)",
            "간이음식점(휴게)",
            "특수학교",
            "국유림",
            "커피숍,다방,카페",
            "화장장",
            "바지선",
            "기타 학교",
            "옥내저장소",
            "기타 위생시설",
            "기타 숙박시설",
            "일식(휴게)",
            "목장",
            "기타 군사시설",
            "부동산 중개업소",
            "주차장",
            "철물점",
            "냉장, 냉동창고",
            "의류",
            "호텔",
            "제과점,빵집,떡집(휴게)",
            "의원",
            "요양시설",
            "종합병원",
            "변압기",
            "기타 공공기관",
            "공사",
            "치과병원",
            "연구소",
            "서점",
            "분식(휴게)",
            "기타 전시장",
            "예식장",
            "제과점",
            "수영장",
            "중식(휴게)",
            "기타 종교시설",
            "중학교",
            "유조선",
            "포장마차",
            "승마장",
            "기숙사",
            "장애인재활시설",
            "게임제공업",
            "탁구장",
            "편의점",
            "휴게텔(수면방)",
            "스크린골프연습장",
            "극장",
            "분뇨시설",
            "동물병원",
            "당구장",
            "복합영상물제공업",
            "이동탱크저장소",
            "기타 청소년시설",
            "시계,안경,금속점",
            "관리사무소",
            "기타 오락시설",
            "아동복지시설",
            "예체능학원",
            "기타 연구,학원",
            "비디오대여점",
            "사진관",
            "한의원",
            "요양병원",
            "골프연습장",
            "장례식장",
            "기도원",
            "독서실",
            "쇼핑센터",
            "인쇄소",
            "도서관",
            "경기장",
            "도계장",
            "기타 항만시설",
            "수상레저기구(보트등)",
            "요양소",
            "국가지정문화재",
            "마을회관",
            "주유취급소",
            "콘도미니엄",
            "체육도장(태권도, 검도 등)",
            "교육원",
            "양식(휴게)",
            "볼링장",
            "경운기",
            "골프장",
            "근로복지시설",
            "문방구",
            "특수작업선(해양관측선등)",
            "제조소",
            "생활권수련시설(청소년수련관, 청소년문화의집 등)",
            "기타 건강시설",
            "횟집(휴게)",
            "여객자동차터미널",
            "기타 군용차량",
            "한방병원",
            "수녀원",
            "기타 지중시설",
            "약국",
            "민박",
            "군.경사격장",
            "정미소",
            "발전소(수력, 화력)",
            "산장",
            "사회복지시설",
            "패스트푸드(휴게)",
            "산후조리원",
            "일반취급소",
            "체육관",
            "박물관",
            "국유림",
            "공원"
    };

    @Transactional
    public void generateRandomFireIncident() {
        log.info("무작위 화재 사고 생성 시작...");

        try {
            // 지역 테이블에서 모든 지역 ID 가져오기
            List<String> allDistrictIds = regionsRepository.findAll()
                    .stream()
                    .map(region -> region.getDistrictId())
                    .toList();

            if (allDistrictIds.isEmpty()) {
                log.warn("데이터베이스에서 지역을 찾을 수 없습니다!");
                return;
            }

            log.info("{}개의 지역을 찾았습니다", allDistrictIds.size());

            // 무작위 값 생성
            String districtId = getRandomElement(allDistrictIds);
            String fireType = getRandomElement(FIRE_TYPES);
            String ignitionCause = getRandomElement(IGNITION_CAUSES);
            String locationCategory = getRandomElement(LOCATION_CATEGORIES);
            int casualtyTotal = random.nextInt(5);
            int death = random.nextInt(2);
            int injury = casualtyTotal - death;
            long propertyDamage = random.nextInt(1000) + 1;

            // 무작위 화재 사고 생성
            FireReports newIncident = FireReports.builder()
                    .districtId(districtId)
                    .fireType(fireType)
                    .occurredAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                    .ignitionCauseSub(ignitionCause)
                    .locationCategorySub(locationCategory)
                    .casualtyTotal(casualtyTotal)
                    .death(death)
                    .injury(injury)
                    .propertyDamageTotal(propertyDamage)
                    .build();

            // 데이터베이스에 저장
            FireReports savedIncident = fireReportsRepository.save(newIncident);
            log.info("새로운 화재 사고가 생성되었습니다: id={}, 지역={}, 유형={}, 인명피해={}",
                    savedIncident.getId(),
                    savedIncident.getDistrictId(),
                    savedIncident.getFireType(),
                    savedIncident.getCasualtyTotal());

        } catch (Exception e) {
            log.error("화재 사고 생성 실패", e);
            throw e; // 트랜잭션 롤백을 위해 예외 다시 던지기
        }
    }

    private <T> T getRandomElement(T[] array) {
        return array[random.nextInt(array.length)];
    }

    private <T> T getRandomElement(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    // 다음 화재 사고 스케줄링
    @Scheduled(fixedDelay = 1000) // 1초 초기 지연
    public void scheduleNextFireIncident() {
        try {
            // 1분에서 10분 사이의 무작위 지연 시간 생성 (밀리초 단위)
            long randomDelay = (random.nextInt(2 * 60 * 1000) + 60 * 1000); 
            Thread.sleep(randomDelay);
            generateRandomFireIncident();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("화재 사고 생성이 중단되었습니다", e);
        }
    }
}