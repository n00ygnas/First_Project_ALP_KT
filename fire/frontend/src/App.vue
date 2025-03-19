<script setup>
import HelloWorld from './components/HelloWorld.vue'
import TheWelcome from './components/TheWelcome.vue'
</script>

<template>
  <router-view></router-view>
</template>

<style>
body {
  margin: 0;
  font-family: Arial, sans-serif;
  background-color: #f5f5f5;
  color: #333;
}

/* Global styles for better visibility */
h1, h2, h3 {
  color: #2c3e50;
}

label {
  color: #2c3e50;
  font-weight: 600;
}

.form-control {
  color: #2c3e50;
  border-color: #ddd;
}

.error {
  color: #dc3545;
  font-weight: 500;
}
</style>
