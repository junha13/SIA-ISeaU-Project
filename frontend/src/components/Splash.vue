<template>
  <div class="splash-full">
    <img src="/iseau.png" class="logo-img" />

    <h1 class="title">I Sea U</h1>

    <p class="subtitle">단 하나의 생명도 놓치지 않습니다.</p>
  </div>
</template>

<script setup></script>

<style scoped>
.splash-full {
  position: fixed;
  inset: 0;
  background: #EFF2F7;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 18px;
  text-align: center;
  z-index: 9999;
}
.logo-img {
  max-width: 150px;
}
.title {
  font-size: clamp(46px, 7vw, 60px);
  font-weight: 700;
  color: #0c1a47;
  letter-spacing: -0.02em;
}
.subtitle {
  font-size: 14px;
  font-weight: 600;
  color: #0c1a47;
}
</style>