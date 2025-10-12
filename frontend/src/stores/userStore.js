import { defineStore } from 'pinia'
import { putUserLocation, postWarningMessage } from '@/api/user'

export const useUserStore = defineStore('user', {
    state: () => ({
        currentLocation: null,    // {lat,lng}
        warningMessages: [],
    }),
    actions: {
        async updateLocation(lat, lng) {
            await putUserLocation(lat, lng)
            this.currentLocation = { lat, lng }
        },
        async sendInitialWarning(zoneId, message) {
            await postWarningMessage(zoneId, message)
            this.warningMessages.push({ zoneId, message, ts: Date.now() })
        },
    },
})
