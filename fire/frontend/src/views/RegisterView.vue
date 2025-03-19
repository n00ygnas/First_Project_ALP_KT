<template>
  <div class="register">
    <div class="register-container">
      <h2>회원 가입</h2>
      <form @submit.prevent="handleRegister" class="register-form">
        <!-- Name Field -->
        <div class="form-group">
          <label>Name:</label>
          <input 
            type="text" 
            v-model="name" 
            required 
            class="form-control"
          >
          <span class="validity-notice" :class="{ 'invalid': name.trim() && !isNameValid }">
            이름을 입력해주세요
          </span>
        </div>

        <!-- Email Field -->
        <div class="form-group">
          <label>Email:</label>
          <input 
            type="email" 
            v-model="email" 
            required 
            class="form-control"
          >
          <span class="validity-notice" :class="{ 'invalid': email.trim() && !isEmailValid }">
            이메일 주소를 입력해주세요
          </span>
        </div>
        
        <!-- Password Field -->
        <div class="form-group">
          <label>Password:</label>
          <input 
            type="password" 
            v-model="password" 
            required 
            class="form-control"
          >
          <span class="validity-notice" :class="{ 'invalid': password && !isPasswordValid }">
            비밀번호는 최소 6자 이상이어야 합니다
          </span>
        </div>

        <!-- Region Selection Section -->
        <div class="region-section">
          <h3 class="region-description">관심 지역을 하나 이상 선택해주세요.</h3>

          <!-- Province Selection -->
          <div class="form-group">
            <label>시/도:</label>
            <select 
              v-model="selectedProvince" 
              class="form-control"
              @change="loadDistricts"
            >
              <option value="">시/도 선택</option>
              <option v-for="province in provinces" :key="province" :value="province">
                {{ province }}
              </option>
            </select>
          </div>

          <!-- District Selection -->
          <div class="form-group">
            <label>시/군/구:</label>
            <div class="districts-grid" v-if="districts.length > 0">
              <label v-for="district in districts" :key="district.districtId" class="district-option">
                <input 
                  type="checkbox" 
                  :value="district.districtId" 
                  v-model="selectedDistricts"
                >
                {{ district.districtName }}
              </label>
            </div>
            <p v-else class="hint-text">시/도를 선택하시면 시/군/구 목록이 나타납니다</p>
            
            <!-- District Selection Buttons -->
            <div class="district-selection-buttons">
              <button type="button" @click="selectAllDistricts" class="btn-select-all">모든 지역 선택</button>
              <button type="button" @click="resetDistrictSelection" class="btn-reset">초기화</button>
            </div>
          </div>
        </div>

        <!-- Error Message -->
        <p v-if="error" class="error">{{ error }}</p>

        <!-- Submit Button -->
        <button 
          type="submit" 
          class="btn-register"
          :disabled="loading || !isFormValid"
        >
          {{ loading ? 'Creating Account...' : '회원 가입' }}
        </button>
        
        <!-- Login Link -->
        <div class="login-link">
          이미 계정이 있으신가요? 
          <router-link to="/">로그인하기</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { userService, regionService } from '@/services/api'

export default {
  name: 'RegisterView',
  setup() {
    const router = useRouter()
    const name = ref('')
    const email = ref('')
    const password = ref('')
    const selectedProvince = ref('')
    const selectedDistricts = ref([])
    const provinces = ref([])
    const districts = ref([])
    const error = ref('')
    const loading = ref(false)

    // 유효성 검사를 위한 computed 속성들
    const isNameValid = computed(() => name.value.trim().length > 0)
    
    const isEmailValid = computed(() => {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      return emailRegex.test(email.value.trim())
    })
    
    const isPasswordValid = computed(() => password.value.length >= 6)

    // 폼 유효성 검사
    const isFormValid = computed(() => {
      return isNameValid.value && 
             isEmailValid.value && 
             isPasswordValid.value && 
             selectedDistricts.value.length > 0
    })

    // 컴포넌트 마운트 시 시/도 목록 로드
    const loadProvinces = async () => {
      try {
        const response = await regionService.getAllProvinces()
        provinces.value = response.data
      } catch (err) {
        error.value = '시/도 목록을 불러오는데 실패했습니다'
        console.error('시/도 목록 로드 실패:', err)
      }
    }

    // 시/도 선택 시 해당 시/군/구 목록 로드
    const loadDistricts = async () => {
      if (!selectedProvince.value) {
        districts.value = []
        return
      }
      try {
        const response = await regionService.getDistrictsInProvince(selectedProvince.value)
        districts.value = response.data
        selectedDistricts.value = [] // 선택된 지역 초기화
      } catch (err) {
        error.value = '시/군/구 목록을 불러오는데 실패했습니다'
        console.error('시/군/구 목록 로드 실패:', err)
      }
    }

    // 회원가입 처리
    const handleRegister = async () => {
      if (!isFormValid.value) {
        error.value = '모든 필수 항목을 입력하고 최소 한 개의 지역을 선택해주세요'
        return
      }

      try {
        loading.value = true
        error.value = ''
        
        // 지역 ID를 문자열로 변환
        const districtIdsAsStrings = selectedDistricts.value.map(id => String(id))
        
        const response = await userService.register({
          name: name.value.trim(),
          email: email.value.trim(),
          password: password.value,
          districtIds: districtIdsAsStrings
        })

        console.log('회원가입 응답:', response)
        console.log('사용자 ID:', response.data.id)

        // 사용자 정보 저장 및 리다이렉트
        localStorage.setItem('userId', response.data.id)
        const storedUserId = localStorage.getItem('userId')
        console.log('저장된 사용자 ID:', storedUserId)
        
        router.push('/dashboard')
      } catch (err) {
        console.error('회원가입 오류:', err)
        error.value = err.response?.data?.message || '회원가입에 실패했습니다. 다시 시도해주세요.'
      } finally {
        loading.value = false
      }
    }

    const resetDistrictSelection = () => {
      selectedDistricts.value = []
    }

    const selectAllDistricts = async () => {
      try {
        if (selectedProvince.value) {
          // 시/도가 선택된 경우 해당 시/도의 모든 지역 선택
          selectedDistricts.value = districts.value.map(district => district.districtId)
        } else {
          // 시/도가 선택되지 않은 경우 모든 시/도의 지역 가져오기
          const response = await regionService.getAllRegions()
          selectedDistricts.value = response.data.map(district => district.districtId)
          districts.value = response.data
        }
      } catch (err) {
        error.value = '지역 정보를 불러오는데 실패했습니다'
        console.error('전체 지역 로드 실패:', err)
      }
    }

    // 컴포넌트 마운트 시 시/도 목록 로드
    loadProvinces()

    return {
      name,
      email,
      password,
      selectedProvince,
      selectedDistricts,
      provinces,
      districts,
      error,
      loading,
      isFormValid,
      isNameValid,
      isEmailValid,
      isPasswordValid,
      loadDistricts,
      handleRegister,
      resetDistrictSelection,
      selectAllDistricts
    }
  }
}
</script>

<style scoped>
.register {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background-color: #FFF0F0;
}

.register-container {
  width: 100%;
  max-width: 400px;
  background: #FFFFFF;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border: 2px solid #000000;
}

h2 {
  color: #000000;
  margin-bottom: 20px;
  text-align: center;
}

.region-section {
  margin-top: 20px;
}

.region-section .form-group + .form-group {
  margin-top: 30px;
}

.form-group {
  margin-bottom: 15px;
}

.region-description {
  color: #2c3e50;
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 1rem 0;
  text-align: left;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-group label {
  color: #000000;
  font-weight: 600;
}

.form-control {
  padding: 10px;
  border: 1px solid #000000;
  border-radius: 4px;
  font-size: 16px;
  color: #000000;
}

.form-control:focus {
  border-color: #ED1C24;
  outline: none;
  box-shadow: 0 0 0 2px rgba(237, 28, 36, 0.1);
}

.districts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 0.5rem;
  max-height: 200px;
  overflow-y: auto;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.district-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: normal;
  padding: 0.25rem;
}

.district-option input[type="checkbox"] {
  width: 16px;
  height: 16px;
}

.hint-text {
  color: #666;
  font-size: 0.9rem;
  margin: 0;
}

.btn-register {
  padding: 12px;
  background-color: #ED1C24;
  color: #FFFFFF;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: background-color 0.2s;
}

.btn-register:hover {
  background-color: #d11920;
}

.error {
  color: #ED1C24;
  text-align: center;
  font-weight: 500;
}

.login-link {
  text-align: center;
  margin-top: 10px;
}

.login-link a {
  color: #000000;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}

.login-link a:hover {
  color: #ED1C24;
}

.validity-notice {
  font-size: 0.85rem;
  color: #666;
  margin-top: 0.25rem;
  transition: color 0.3s;
}

.validity-notice.invalid {
  color: #e74c3c;
}

.form-control.invalid {
  border-color: #e74c3c;
}

.district-selection-buttons {
  display: flex;
  gap: 10px;
  margin-top: 15px;
  justify-content: center;
}

.btn-select-all {
  padding: 8px 16px;
  background-color: #ED1C24;
  color: #FFFFFF;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.3s;
}

.btn-select-all:hover {
  background-color: #d11920;
}

.btn-reset {
  padding: 8px 16px;
  background-color: #000000;
  color: #FFFFFF;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.3s;
}

.btn-reset:hover {
  background-color: #333333;
}
</style> 