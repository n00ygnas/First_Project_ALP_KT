<template>
  <div class="dashboard">
    <div class="header">
      <div class="header-title">
        <img src="@/assets/02_KT Wordmark (Standard)_02.png" alt="KT Logo" class="header-logo">
        <h2>화재 발생 현황</h2>
      </div>
      <div class="header-buttons">
        <button @click="showEditDistricts" class="btn-edit">관심 지역 수정</button>
        <button @click="logout" class="btn-logout">로그아웃</button>
      </div>
    </div>
    
    <div class="main-content">
      <!-- 좌측 패널 -->
      <div class="time-section">
        <div class="current-time-box">
          <h3>현재 시간</h3>
          <div class="time">{{ currentTime }}</div>
          <div class="date">{{ currentDate }}</div>
        </div>
        <div class="alerts-section">
          <div class="alerts-header">
            <h3>실시간 화재 경보 <span class="alert-badge" v-if="alerts.length > 0">{{ alerts.length }}</span></h3>
            <button @click="fetchAlerts" class="btn-refresh" title="새로고침">
              ↻
            </button>
          </div>
          <div class="alerts-container" v-if="alerts.length > 0">
            <div v-for="alert in alerts" :key="alert.id" class="alert-card">
              <div class="alert-header">
                <span class="alert-time">{{ formatTimeAgo(alert.occurredAt) }}</span>
                <span class="alert-status">화재 발생</span>
              </div>
              <div class="alert-location">{{ alert.region?.provinceName }} {{ alert.region?.districtName || '위치 정보 없음' }}</div>
              <div class="alert-details">
                <div>화재 규모: {{ alert.scale || '정보 없음' }}</div>
                <div>피해 상황: {{ alert.damage || '정보 없음' }}</div>
              </div>
            </div>
          </div>
          <div v-else class="no-alerts">
            최근 1시간 동안 발생한 화재가 없습니다.
          </div>
        </div>
      </div>

      <!-- 우측 패널 -->
      <div class="reports-section">
        <!-- 검색 섹션 -->
        <div class="search-section">
          <div class="date-range">
            <div class="form-group">
              <label>조회 기간:</label>
              <div class="date-inputs">
                <input 
                  type="date" 
                  v-model="startDate" 
                  :max="endDate"
                  class="form-control"
                >
                <span>~</span>
                <input 
                  type="date" 
                  v-model="endDate" 
                  :min="startDate"
                  :max="today"
                  class="form-control"
                >
              </div>
              <div class="search-buttons">
                <button @click="searchByDate" class="btn-search">검색</button>
                <button @click="resetSearch" class="btn-reset">초기화</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 선택된 지역 표시 -->
        <div class="selected-districts-display">
          <h4>관심 지역 목록</h4>
          <div class="districts-list">
            <span v-if="isAllDistrictsSelected" class="district-tag">
              모든 지역
            </span>
            <template v-else>
              <template v-if="!showAllDistricts && selectedDistrictNames.length > 3">
                <span v-for="name in selectedDistrictNames.slice(0, 3)" 
                      :key="name" 
                      class="district-tag">
                  {{ name }}
                  <button 
                    class="btn-remove-district" 
                    @click.stop="removeDistrict(name)"
                    title="지역 제거">
                    ×
                  </button>
                </span>
                <span 
                  class="district-tag clickable"
                  @click="showAllDistrictsModal">
                  외 {{ selectedDistrictNames.length - 3 }}개 지역
                </span>
              </template>
              <template v-else>
                <span v-for="name in selectedDistrictNames" 
                      :key="name" 
                      class="district-tag">
                  {{ name }}
                  <button 
                    class="btn-remove-district" 
                    @click.stop="removeDistrict(name)"
                    title="지역 제거">
                    ×
                  </button>
                </span>
              </template>
            </template>
          </div>
        </div>

        <!-- 보고서 그리드 -->
        <div class="reports-container">
          <div v-if="loading" class="loading">로딩중...</div>
          <div v-else-if="error" class="error">{{ error }}</div>
          <div v-else-if="reports.length === 0" class="no-data">
            선택하신 지역의 화재 정보가 없습니다.
          </div>
          <template v-else>
            <div class="reports-grid">
              <div v-for="report in reports" 
                   :key="report.id" 
                   class="report-card"
                   @click="showReportDetails(report)">
                <div class="report-header">
                  <div class="report-line">
                    <span class="date">{{ formatDateOnly(report.occurredAt) }}</span>
                    <span class="province">{{ report.region?.provinceName }}</span>
                  </div>
                  <div class="report-line">
                    <span class="time">{{ formatTime(report.occurredAt) }}</span>
                    <span class="district">{{ report.region?.districtName || '알 수 없는 위치' }}</span>
                  </div>
                </div>
              </div>
            </div>
            <!-- 페이지네이션 -->
            <div class="pagination">
              <button 
                :disabled="currentPage === 0" 
                @click="changePage(currentPage - 1)" 
                class="btn-page"
              >
                이전
              </button>
              <span class="page-info">
                {{ currentPage + 1 }} / {{ totalPages }} 페이지
              </span>
              <button 
                :disabled="currentPage >= totalPages - 1" 
                @click="changePage(currentPage + 1)" 
                class="btn-page"
              >
                다음
              </button>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- 보고서 상세 정보 모달 -->
    <div v-if="selectedReport" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>화재 상세 정보</h3>
          <button @click="closeReportDetails" class="btn-close">×</button>
        </div>
        <div class="report-details">
          <p><strong>발생 시각:</strong> {{ formatDateTime(selectedReport.occurredAt) }}</p>
          <p><strong>위치:</strong> {{ selectedReport.region?.provinceName }} {{ selectedReport.region?.districtName }}</p>
          <p><strong>화재 유형:</strong> {{ selectedReport.fireType || '정보 없음' }}</p>
          <p><strong>발화 원인:</strong> {{ selectedReport.ignitionCauseSub || '정보 없음' }}</p>
          <p><strong>화재 발생 장소:</strong> {{ selectedReport.locationCategorySub || '정보 없음' }}</p>
          <p><strong>인명 피해:</strong> 총 {{ selectedReport.casualtyTotal || 0 }}명 (사망 {{ selectedReport.death || 0 }}명, 부상 {{ selectedReport.injury || 0 }}명)</p>
          <p><strong>재산 피해:</strong> {{ formatNumber(selectedReport.propertyDamageTotal) }}</p>
        </div>
      </div>
    </div>

    <!-- 지역 수정 모달 -->
    <div v-if="showingEditModal" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>관심 지역 수정</h3>
          <div class="modal-buttons">
            <button @click="selectAllDistricts" class="btn-select-all">전체 선택</button>
            <button @click="resetDistrictSelection" class="btn-reset">초기화</button>
          </div>
        </div>
        <div class="district-selection">
          <div class="form-group">
            <label>시/도:</label>
            <select v-model="selectedProvince" @change="loadDistricts" class="form-control">
              <option value="">시/도 선택</option>
              <option v-for="province in provinces" :key="province" :value="province">
                {{ province }}
              </option>
            </select>
          </div>

          <div class="form-group" v-if="districts.length > 0">
            <label>시/군/구:</label>
            <div class="districts-grid">
              <div v-for="district in districts" :key="district.districtId" class="district-checkbox">
                <input
                  type="checkbox"
                  :id="district.districtId"
                  :value="district.districtId"
                  v-model="modalSelectedDistrictIds"
                >
                <label :for="district.districtId">{{ district.districtName }}</label>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="saveDistrictChanges" class="btn-save">저장</button>
          <button @click="closeEditModal" class="btn-cancel">취소</button>
        </div>
      </div>
    </div>

    <!-- 모든 지역 보기 모달 -->
    <div v-if="showingAllDistrictsModal" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>선택된 관심 지역 목록</h3>
          <button @click="closeAllDistrictsModal" class="btn-close">×</button>
        </div>
        <div class="all-districts-list">
          <div v-for="name in selectedDistrictNames" 
               :key="name" 
               class="district-item">
            {{ name }}
            <button 
              class="btn-remove-district" 
              @click="removeDistrict(name)"
              title="지역 제거">
              ×
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { fireReportService, regionService, userService } from '@/services/api'

export default {
  setup() {
    const router = useRouter()
    const reports = ref([])
    const loading = ref(true)
    const error = ref('')
    const showingEditModal = ref(false)
    const showingAllDistrictsModal = ref(false)
    const provinces = ref([])
    const districts = ref([])
    const allDistricts = ref([])
    const selectedProvince = ref('')
    const selectedDistrictIds = ref([])
    const modalSelectedDistrictIds = ref([])
    const selectedDistrictNames = ref([])
    const startDate = ref('')
    const endDate = ref('')
    const currentPage = ref(0)
    const totalPages = ref(0)
    const pageSize = 4
    const alerts = ref([])
    const currentTime = ref('')
    const currentDate = ref('')
    let clockInterval
    let alertInterval
    const selectedReport = ref(null)
    const showAllDistricts = ref(false)
    
    const today = computed(() => {
      const date = new Date()
      return date.toISOString().split('T')[0]
    })

    const isAllDistrictsSelected = computed(() => {
      if (!allDistricts.value.length || !selectedDistrictIds.value.length) return false
      
      // 정렬된 배열 비교를 위해 두 배열을 정렬
      const selectedIds = [...selectedDistrictIds.value].sort()
      const allIds = allDistricts.value.map(d => d.districtId).sort()
      
      // 배열의 길이가 같고 모든 요소가 일치하는지 확인
      return selectedIds.length === allIds.length && 
             selectedIds.every((id, index) => id === allIds[index])
    })

    const loadSelectedDistrictNames = async () => {
      try {
        const userId = localStorage.getItem('userId')
        const userInterests = await userService.getUserInterests(userId)
        const districtIds = userInterests.data
        
        // 모든 지역 정보 가져오기
        const allRegions = await regionService.getAllRegions()
        allDistricts.value = allRegions.data
        const regionsMap = new Map(
          allRegions.data.map(region => [region.districtId, {
            provinceName: region.provinceName,
            districtName: region.districtName
          }])
        )
        
        // 선택된 지역 ID 업데이트
        selectedDistrictIds.value = districtIds
        
        // 지역 ID를 전체 지역명으로 변환 (시/도가 먼저 오도록)
        selectedDistrictNames.value = districtIds
          .map(id => {
            const region = regionsMap.get(id)
            return region ? `${region.provinceName} ${region.districtName}` : null
          })
          .filter(name => name) // null 값 제거
          .sort((a, b) => { // 시/도 이름으로 정렬
            const provinceA = a.split(' ')[0]
            const provinceB = b.split(' ')[0]
            return provinceA.localeCompare(provinceB)
          })
      } catch (err) {
        console.error('지역 이름 로드 중 오류:', err)
      }
    }

    const loadReports = async () => {
      const userId = localStorage.getItem('userId')
      if (!userId) {
        router.push('/')
        return
      }

      try {
        const response = await fireReportService.getRecentReports(userId, currentPage.value, 4)
        reports.value = response.content
        totalPages.value = response.totalPages
        await loadSelectedDistrictNames()
      } catch (err) {
        error.value = '화재 정보를 불러오는데 실패했습니다.'
      } finally {
        loading.value = false
      }
    }

    const showEditDistricts = async () => {
      try {
        loading.value = true
        error.value = ''
        
        // 모든 지역 정보를 한 번에 로드
        const allRegions = await regionService.getAllRegions()
        allDistricts.value = allRegions.data
        districts.value = allRegions.data
        
        // 고유한 시/도 이름 추출
        provinces.value = [...new Set(allRegions.data.map(region => region.provinceName))]

        const userId = localStorage.getItem('userId')
        if (!userId) {
          router.push('/login')
          return
        }

        const userInterests = await userService.getUserInterests(userId)
        modalSelectedDistrictIds.value = userInterests.data || []
        selectedDistrictIds.value = userInterests.data || []

        showingEditModal.value = true
      } catch (err) {
        error.value = '데이터를 불러오는데 실패했습니다.'
      } finally {
        loading.value = false
      }
    }

    const loadDistricts = () => {
      if (!selectedProvince.value) {
        districts.value = allDistricts.value // 시/도가 선택되지 않았을 때 모든 지역 표시
        return
      }
      
      // 선택된 시/도에 해당하는 지역만 필터링
      districts.value = allDistricts.value.filter(
        district => district.provinceName === selectedProvince.value
      )
    }

    const saveDistrictChanges = async () => {
      try {
        const userId = localStorage.getItem('userId')
        await userService.updateUserInterests(userId, modalSelectedDistrictIds.value)
        selectedDistrictIds.value = modalSelectedDistrictIds.value
        showingEditModal.value = false
        await loadReports()
      } catch (err) {
        error.value = err.response?.data || '변경사항 저장에 실패했습니다.'
      }
    }

    const closeEditModal = () => {
      showingEditModal.value = false
      selectedProvince.value = ''
      districts.value = []
      modalSelectedDistrictIds.value = [...selectedDistrictIds.value]
    }

    const formatDate = (dateString) => {
      const date = new Date(dateString)
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      const year = date.getFullYear()
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      
      return `${hours}시 ${minutes}분, ${year}년 ${month}월 ${day}일`
    }

    const formatNumber = (number) => {
      if (!number) return '0만원'
      const actualValue = number * 10000
      const manWon = Math.floor(actualValue / 10000)
      return `${manWon.toLocaleString()}만원`
    }

    const logout = () => {
      localStorage.removeItem('userId')
      router.push('/')
    }

    const searchByDate = async () => {
      try {
        if (!startDate.value || !endDate.value) {
          error.value = '시작일과 종료일을 모두 선택해주세요.'
          return
        }

        loading.value = true
        error.value = ''
        
        const userId = localStorage.getItem('userId')
        if (!userId) {
          router.push('/')
          return
        }

        const response = await fireReportService.getReportsByDateRange(
          userId,
          startDate.value,
          endDate.value,
          currentPage.value,
          4
        )
        
        if (response.content.length === 0) {
          error.value = '선택하신 기간에 발생한 화재 정보가 없습니다.'
        } else {
          reports.value = response.content
          totalPages.value = response.totalPages
          error.value = ''
        }
      } catch (err) {
        error.value = err.response?.data || '화재 정보를 불러오는데 실패했습니다.'
      } finally {
        loading.value = false
      }
    }

    const changePage = async (newPage) => {
      currentPage.value = newPage
      if (startDate.value && endDate.value) {
        await searchByDate()
      } else {
        await loadReports()
      }
    }

    const resetSearch = async () => {
      startDate.value = ''
      endDate.value = ''
      currentPage.value = 0
      await loadReports()
    }

    const fetchAlerts = async () => {
      try {
        const userId = localStorage.getItem('userId')
        if (!userId) return
        
        // 알림과 보고서만 가져오고, 지역 선택은 업데이트하지 않음
        const [alertsResponse, reportsResponse] = await Promise.all([
          fireReportService.getFireAlerts(userId),
          (!startDate.value && !endDate.value) ? 
            fireReportService.getRecentReports(userId, currentPage.value, 4) : 
            null
        ].filter(Boolean))

        alerts.value = alertsResponse

        // 날짜 검색 모드가 아닐 때만 보고서 업데이트
        if (reportsResponse && !startDate.value && !endDate.value) {
          reports.value = reportsResponse.content
          totalPages.value = reportsResponse.totalPages
        }
      } catch (error) {
        console.error('업데이트 가져오기 오류:', error)
      }
    }

    const formatTimeAgo = (dateString) => {
      const date = new Date(dateString)
      const now = new Date()
      const diffMinutes = Math.floor((now - date) / (1000 * 60))
      
      if (diffMinutes < 1) return '방금 전'
      if (diffMinutes < 60) return `${diffMinutes}분 전`
      return `1시간 이내`
    }

    const updateDateTime = () => {
      const now = new Date()
      
      const hours = String(now.getHours()).padStart(2, '0')
      const minutes = String(now.getMinutes()).padStart(2, '0')
      const seconds = String(now.getSeconds()).padStart(2, '0')
      currentTime.value = `${hours}:${minutes}:${seconds}`
      
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const weekDay = new Intl.DateTimeFormat('ko-KR', { weekday: 'long' }).format(now)
      currentDate.value = `${year}년 ${month}월 ${day}일 (${weekDay})`
    }

    const resetDistrictSelection = () => {
      modalSelectedDistrictIds.value = []
    }

    const selectAllDistricts = () => {
      if (selectedProvince.value) {
        // 시/도가 선택된 경우 해당 시/도의 모든 지역 선택
        const provinceDistricts = allDistricts.value
          .filter(district => district.provinceName === selectedProvince.value)
          .map(district => district.districtId)
        modalSelectedDistrictIds.value = [...new Set([...modalSelectedDistrictIds.value, ...provinceDistricts])]
      } else {
        // 시/도가 선택되지 않은 경우 전체 목록에서 모든 지역 선택
        modalSelectedDistrictIds.value = allDistricts.value.map(district => district.districtId)
      }
    }

    const showReportDetails = (report) => {
      selectedReport.value = report
    }

    const closeReportDetails = () => {
      selectedReport.value = null
    }

    const formatTime = (dateString) => {
      const date = new Date(dateString)
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      return `${hours}시 ${minutes}분`
    }

    const formatDateOnly = (dateString) => {
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      return `${year}년 ${month}월 ${day}일`
    }

    const showAllDistrictsModal = () => {
      showingAllDistrictsModal.value = true
      showAllDistricts.value = true
    }

    const closeAllDistrictsModal = () => {
      showingAllDistrictsModal.value = false
      showAllDistricts.value = false
    }

    const removeDistrict = async (districtName) => {
      try {
        // 지역명에서 지역 ID 찾기
        const [provinceName, ...districtNameParts] = districtName.split(' ')
        const districtNameOnly = districtNameParts.join(' ')
        
        const districtToRemove = allDistricts.value.find(
          d => d.provinceName === provinceName && d.districtName === districtNameOnly
        )

        if (!districtToRemove) return

        const userId = localStorage.getItem('userId')
        if (!userId) {
          router.push('/login')
          return
        }

        // selectedDistrictIds에서 제거
        selectedDistrictIds.value = selectedDistrictIds.value.filter(
          id => id !== districtToRemove.districtId
        )

        // 백엔드에 변경사항 저장
        await userService.updateUserInterests(userId, selectedDistrictIds.value)
        
        // 지역 이름 목록 새로고침
        await loadSelectedDistrictNames()
        
        // 업데이트된 지역으로 보고서 새로고침
        await loadReports()
      } catch (err) {
        console.error('지역 제거 중 오류:', err)
        error.value = '지역 삭제에 실패했습니다.'
      }
    }

    const formatDateTime = (timestamp) => {
      const date = new Date(timestamp);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      
      return `${year}년 ${month}월 ${day}일 ${hours}시 ${minutes}분`;
    };

    onMounted(async () => {
      updateDateTime()
      await loadReports()
      await loadSelectedDistrictNames()
      await fetchAlerts()

      clockInterval = setInterval(updateDateTime, 1000)
      alertInterval = setInterval(fetchAlerts, 10000)
    })

    onUnmounted(() => {
      if (clockInterval) clearInterval(clockInterval)
      if (alertInterval) clearInterval(alertInterval)
    })

    return {
      reports,
      loading,
      error,
      showingEditModal,
      showingAllDistrictsModal,
      provinces,
      districts,
      allDistricts,
      selectedProvince,
      selectedDistrictIds,
      modalSelectedDistrictIds,
      selectedDistrictNames,
      formatDate,
      formatTime,
      formatDateOnly,
      formatNumber,
      logout,
      showEditDistricts,
      loadDistricts,
      saveDistrictChanges,
      closeEditModal,
      startDate,
      endDate,
      today,
      currentPage,
      totalPages,
      changePage,
      resetSearch,
      searchByDate,
      alerts,
      formatTimeAgo,
      currentTime,
      currentDate,
      resetDistrictSelection,
      selectAllDistricts,
      isAllDistrictsSelected,
      selectedReport,
      showReportDetails,
      closeReportDetails,
      showAllDistricts,
      showAllDistrictsModal,
      closeAllDistrictsModal,
      removeDistrict,
      formatDateTime,
    }
  }
}
</script>

<style scoped>
.dashboard {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  background-color: #FFFFFF;
}

.header {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background-color: #000000;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  color: #FFFFFF;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-logo {
  height: 30px;
  width: auto;
  object-fit: contain;
}

.header h2 {
  color: #FFFFFF;
  margin: 0;
}

.reports-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  min-height: 400px;
}

.report-card {
  border: 1px solid #000000;
  border-radius: 8px;
  padding: 20px;
  background: #FFFFFF;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.report-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.2);
  border-color: #ED1C24;
}

.report-header {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.report-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time {
  color: #000000;
  font-weight: 500;
  font-size: 1.2rem;
}

.date {
  color: #000000;
  font-weight: 500;
  font-size: 1rem;
}

.district {
  color: #000000;
  font-weight: 600;
  font-size: 1.2rem;
}

.province {
  color: #000000;
  font-weight: 600;
  font-size: 1rem;
}

.report-content {
  color: #2c3e50;
}

.report-content p {
  margin: 10px 0;
  line-height: 1.5;
  font-size: 0.95rem;
}

.report-content strong {
  color: #1a1a1a;
  display: inline-block;
  width: 100px;
  margin-right: 10px;
}

.btn-logout {
  padding: 8px 16px;
  background-color: #ED1C24;
  color: #FFFFFF;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.2s ease;
}

.btn-logout:hover {
  background-color: #d11920;
}

.loading {
  text-align: center;
  padding: 20px;
  color: #2c3e50;
  font-weight: 500;
}

.error {
  text-align: center;
  padding: 20px;
  color: #dc3545;
  font-weight: 500;
}

.no-data {
  text-align: center;
  padding: 20px;
  color: #2c3e50;
  font-weight: 500;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.btn-edit {
  padding: 8px 16px;
  background-color: #000000;
  color: #FFFFFF;
  border: 2px solid #FFFFFF;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;
}

.btn-edit:hover {
  background-color: #FFFFFF;
  color: #000000;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: #FFFFFF;
  padding: 20px;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  border: 2px solid #000000;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 15px;
  margin-bottom: 15px;
  border-bottom: 2px solid #000000;
}

.modal-header h3 {
  margin: 0;
  color: #000000;
}

.modal-buttons {
  display: flex;
  gap: 10px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px solid #000000;
}

.district-selection {
  flex: 1;
  overflow-y: auto;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  color: #2c3e50;
  font-weight: 600;
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
}

.btn-select-all, .btn-reset, .btn-save, .btn-cancel {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.btn-select-all {
  background-color: #ED1C24;
  color: #FFFFFF;
}

.btn-reset {
  background-color: #000000;
  color: #FFFFFF;
}

.btn-save {
  background-color: #ED1C24;
  color: #FFFFFF;
}

.btn-cancel {
  background-color: #000000;
  color: #FFFFFF;
}

.btn-select-all:hover, .btn-save:hover {
  background-color: #d11920;
}

.btn-reset:hover, .btn-cancel:hover {
  background-color: #333333;
}

.districts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
  margin-top: 10px;
}

.district-checkbox {
  display: flex;
  align-items: center;
  gap: 5px;
}

.selected-districts {
  width: 100%;
  background-color: #f8f9fa;
  padding: 20px;
  margin-bottom: 20px;
  border-radius: 8px;
  text-align: center;
  color: #2c3e50;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.selected-districts p {
  margin: 0;
}

.search-section {
  width: 100%;
  background-color: #fff;
  padding: 20px;
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.date-range {
  display: flex;
  align-items: center;
  gap: 15px;
}

.date-inputs {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.date-inputs input[type="date"] {
  padding: 8px;
  border: 1px solid #000000;
  border-radius: 4px;
  width: 150px;
}

.date-inputs input[type="date"]:focus {
  border-color: #ED1C24;
  outline: none;
}

.district-checkbox input[type="checkbox"] {
  accent-color: #ED1C24;
}

.search-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.btn-search, .btn-refresh, .btn-page {
  padding: 8px 16px;
  background-color: #ED1C24;
  color: #FFFFFF;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.2s ease;
}

.btn-search:hover, .btn-refresh:hover, .btn-page:hover {
  background-color: #d11920;
}

.btn-page:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  gap: 20px;
}

.page-info {
  font-size: 1rem;
  color: #2c3e50;
  font-weight: 500;
}

.main-content {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  margin-top: 20px;
}

.time-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.current-time-box {
  background: #000000;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  text-align: center;
  margin-bottom: 1rem;
}

.current-time-box .time {
  font-size: 2.5rem;
  font-weight: bold;
  color: #FFFFFF;
  margin: 0.5rem 0;
}

.current-time-box .date {
  font-size: 1.2rem;
  color: #FFFFFF;
}

.current-time-box h3 {
  color: #FFFFFF;
  margin: 0;
}

.alerts-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.alerts-section h3 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #2c3e50;
}

.alerts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.btn-refresh {
  width: 32px;
  height: 32px;
  padding: 0;
  background-color: #ED1C24;
  color: #FFFFFF;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-weight: bold;
  font-size: 1.2em;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease;
}

.btn-refresh:hover {
  background-color: #d11920;
  transform: rotate(180deg);
}

.alert-badge {
  background: #ED1C24;
  color: #FFFFFF;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.8em;
}

.alerts-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 600px;
  overflow-y: auto;
  padding: 4px;
}

.alert-card {
  background: #FFF0F0;
  border-left: 4px solid #ED1C24;
  padding: 15px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(237, 28, 36, 0.1);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.alert-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(237, 28, 36, 0.15);
}

.alert-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.alert-time {
  color: #000000;
  font-size: 0.9em;
  font-weight: 500;
}

.alert-status {
  background: #ED1C24;
  color: #FFFFFF;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 0.8em;
  font-weight: 600;
}

.alert-location {
  font-weight: 600;
  margin-bottom: 12px;
  color: #000000;
}

.alert-details {
  font-size: 0.9em;
  color: #000000;
  line-height: 1.5;
}

.alert-details div {
  margin-bottom: 4px;
}

.no-alerts {
  text-align: center;
  color: #000000;
  padding: 20px;
  background: #FFF0F0;
  border-radius: 4px;
  font-weight: 500;
}

.reports-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.report-details {
  padding: 20px;
}

.report-details p {
  margin: 10px 0;
  line-height: 1.6;
}

.report-details strong {
  display: inline-block;
  width: 120px;
  color: #2c3e50;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 0;
  margin: 0;
}

.btn-close:hover {
  color: #333;
}

.districts-message {
  color: #000000;
  font-size: 0.95rem;
  margin: 10px 0;
  padding: 0 10px;
}

.selected-districts-display {
  background: #FFFFFF;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.selected-districts-display h4 {
  margin: 0 0 15px 0;
  color: #000000;
  font-size: 1.1rem;
}

.districts-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.district-tag {
  background: #FFF0F0;
  color: #000000;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 0.9rem;
  border: 1px solid #ED1C24;
  display: flex;
  align-items: center;
  gap: 8px;
}

.district-tag.clickable {
  cursor: pointer;
  background: #FFE0E0;
  transition: background-color 0.2s;
}

.district-tag.clickable:hover {
  background: #FFD0D0;
}

.districts-message {
  display: none;
}

.all-districts-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
  max-height: 400px;
  overflow-y: auto;
}

.district-item {
  background: #FFF0F0;
  color: #000000;
  padding: 12px;
  border-radius: 4px;
  font-size: 1rem;
  border: 1px solid #ED1C24;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.btn-remove-district {
  background: none;
  border: none;
  color: #666;
  font-size: 16px;
  cursor: pointer;
  padding: 0;
  margin: 0;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.btn-remove-district:hover {
  background: rgba(0, 0, 0, 0.1);
  color: #333;
}
</style> 