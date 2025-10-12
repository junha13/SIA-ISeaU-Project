import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { useSafetyStore } from '@/stores/safetyStore'

export function useGeolocation() {
    const userStore = useUserStore()
    const safetyStore = useSafetyStore()

    const latitude = ref(null)
    const longitude = ref(null)
    const error = ref(null)

    const getLocation = () => {
        if (!navigator.geolocation) {
            error.value = '위치 정보를 지원하지 않습니다.'
            return
        }

        navigator.geolocation.watchPosition(
            async (pos) => {
                latitude.value = pos.coords.latitude
                longitude.value = pos.coords.longitude
                await userStore.updateLocation(latitude.value, longitude.value)
                await safetyStore.fetchRealtimeWarning(latitude.value, longitude.value)
            },
            (err) => {
                error.value = err.message
            },
            { enableHighAccuracy: true, maximumAge: 10000, timeout: 10000 }
        )
    }

    onMounted(() => {
        getLocation()
    })

    return { latitude, longitude, error }
}
