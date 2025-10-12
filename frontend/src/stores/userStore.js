import { defineStore } from 'pinia'
import { putUserLocation, postWarningMessage } from '@/api/user'

export const useUserStore = defineStore('user', {
    state: () => ({
        currentLocation: null,  // {lat, lng}
        warningMessages: [],
        isLoading: false,
        error: null,
    }),
    actions: {
        async updateLocation(lat, lng) {
            this.isLoading = true
            this.error = null
            try {
                await putUserLocation(lat, lng)
                this.currentLocation = { lat, lng }
            } catch (e) {
                this.error = e.message || '위치 업데이트 실패'
            } finally {
                this.isLoading = false
            }
        },
        async sendInitialWarning(zoneId, message) {
            this.isLoading = true
            this.error = null
            try {
                await postWarningMessage(zoneId, message)
                this.warningMessages.push({ zoneId, message, ts: Date.now() })
            } catch (e) {
                this.error = e.message || '경고 메시지 전송 실패'
            } finally {
                this.isLoading = false
            }
        },
    },
})
