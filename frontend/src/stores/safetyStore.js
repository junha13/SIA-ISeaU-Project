import { defineStore } from 'pinia'
import { getDrowningAlert, getRealtimeWarning } from '@/api/safety'

export const useSafetyStore = defineStore('safety', {
    state: () => ({
        drowningAlert: null,
        realtimeWarning: null,
        isLoading: false,
        error: null,
        lastUpdated: null,
        ttl: 10000, // 10초 내 재요청 방지 (TTL 캐시)
    }),
    actions: {
        async fetchDrowningAlert(cctvId) {
            if (this.lastUpdated && Date.now() - this.lastUpdated < this.ttl) {
                console.log('[SafetyStore] TTL cache active, skipping fetch.')
                return this.drowningAlert
            }
            this.isLoading = true
            this.error = null
            try {
                this.drowningAlert = await getDrowningAlert(cctvId)
                this.lastUpdated = Date.now()
            } catch (e) {
                console.error('[SafetyStore] fetchDrowningAlert failed:', e)
                this.error = e.message || 'AI 감지 데이터 요청 실패'
            } finally {
                this.isLoading = false
            }
            return this.drowningAlert
        },
        async fetchRealtimeWarning(lat, lng) {
            this.isLoading = true
            this.error = null
            try {
                this.realtimeWarning = await getRealtimeWarning(lat, lng)
            } catch (e) {
                console.error('[SafetyStore] fetchRealtimeWarning failed:', e)
                this.error = e.message || '실시간 위험정보 요청 실패'
            } finally {
                this.isLoading = false
            }
            return this.realtimeWarning
        },
    },
})
