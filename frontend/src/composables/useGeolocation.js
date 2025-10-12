// 위치 정보 가져오기 composable
import { ref } from 'vue'

export function useGeolocation() {
    const position = ref({ lat: null, lng: null })
    const error = ref(null)
    const loading = ref(false)

    const getLocation = () => {
        loading.value = true
        error.value = null

        return new Promise((resolve, reject) => {
            if (!navigator.geolocation) {
                error.value = '이 브라우저에서는 위치 정보 기능을 지원하지 않습니다.'
                loading.value = false
                reject(error.value)
                return
            }

            navigator.geolocation.getCurrentPosition(
                (pos) => {
                    position.value = {
                        lat: pos.coords.latitude,
                        lng: pos.coords.longitude
                    }
                    loading.value = false
                    resolve(position.value)
                },
                (err) => {
                    error.value = `위치 정보를 가져올 수 없습니다: ${err.message}`
                    loading.value = false
                    reject(error.value)
                },
                { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
            )
        })
    }

    return { position, error, loading, getLocation }
}
