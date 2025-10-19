<template>
  <div class="cctv-player">
    <video ref="video" controls autoplay muted playsinline width="100%" height="360"></video>
  </div>
</template>

<script>
import Hls from 'hls.js';

export default {
  name: 'BeachCctvTab',
  props: {
    src: { type: String, required: true } // m3u8 전체 URL
  },
  mounted() {
    const video = this.$refs.video;
    const url = this.src;

    if (Hls.isSupported()) {
      const hls = new Hls();
      hls.loadSource(url);
      hls.attachMedia(video);
      hls.on(Hls.Events.MANIFEST_PARSED, () => video.play().catch(()=>{}));
      this._hls = hls;
    } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
      video.src = url;
    } else {
      console.error('브라우저가 HLS를 지원하지 않습니다.');
    }
  },
  beforeDestroy() {
    if (this._hls) this._hls.destroy();
  }
}
</script>

<style scoped>
.cctv-player { display:flex; justify-content:center; }
video { background:#000; }
</style>
