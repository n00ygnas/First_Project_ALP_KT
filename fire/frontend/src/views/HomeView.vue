<template>
  <div class="home">
    <div class="logo-container">
      <img src="@/assets/01_KT Wordmark (Standard)_01.png" alt="KT Logo" class="logo">
      <h1>화재 모니터링 시스템</h1>
    </div>
    <div class="login-container">
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label>이메일:</label>
          <input type="email" v-model="email" required class="form-control">
        </div>
        
        <div class="form-group">
          <label>비밀번호:</label>
          <input type="password" v-model="password" required class="form-control">
        </div>

        <button type="submit" class="btn-login">로그인</button>
        <p v-if="error" class="error">{{ error }}</p>
        
        <div class="register-link">
          <router-link to="/register">계정이 없으신가요? 회원가입</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { userService } from '@/services/api'

export default {
  name: 'HomeView',
  setup() {
    const router = useRouter()
    const email = ref('')
    const password = ref('')
    const error = ref('')

    const handleLogin = async () => {
      try {
        const response = await userService.login(email.value, password.value)
        localStorage.setItem('userId', response.data.id)
        router.push('/dashboard')
      } catch (err) {
        error.value = '이메일 또는 비밀번호가 올바르지 않습니다.'
      }
    }

    return {
      email,
      password,
      error,
      handleLogin
    }
  }
}
</script>

<style scoped>
.home {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  background-color: #FFF0F0;
}

.logo-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40px;
}

.logo {
  width: auto;
  height: 60px;
  margin-bottom: 20px;
  object-fit: contain;
}

h1 {
  color: #000000;
  text-align: center;
}

.login-container {
  width: 100%;
  max-width: 400px;
  background: #FFFFFF;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border: 2px solid #000000;
}

.login-form {
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

.btn-login {
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

.btn-login:hover {
  background-color: #d11920;
}

.error {
  color: #ED1C24;
  text-align: center;
  font-weight: 500;
}

.register-link {
  text-align: center;
  margin-top: 10px;
}

.register-link a {
  color: #000000;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}

.register-link a:hover {
  color: #ED1C24;
}
</style> 