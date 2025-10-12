import { defineStore } from 'pinia'
import { getDrowningAlert, getRealtimeWarning } from '@/api/safety'

export const useSafetyStore = defineStore('safety', {
    state: () => ({
        drowningAlert: null,
        realtimeWarning: null,
    }),
    actions: {
        async fetchDrowningAlert(cctvId) {
            this.drowningAlert = await getDrowningAlert(cctvId)
            return this.drowningAlert
        },
        async fetchRealtimeWarning(lat, lng) {
            this.realtimeWarning = await getRealtimeWarning(lat, lng)
            return this.realtimeWarning
        },
    },
})
