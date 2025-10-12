// src/stores/marineStore.js
import { defineStore } from 'pinia'
import { useApi } from '@/composables/useApi'

export const useMarineStore = defineStore('marine', {
    state: () => ({
        weather: null,
        tide: null,
        loading: false,
        error: null
    }),
    actions: {
        async fetchWeather(beachId = 1) {
            this.loading = true
            const api = useApi()
            try {
                const res = await api.get(`/api/marine/weather?beachId=${beachId}`)
                this.weather = res.data
            } catch (err) {
                this.error = err
            } finally {
                this.loading = false
            }
        },
        async fetchTide(beachId = 1) {
            this.loading = true
            const api = useApi()
            try {
                const res = await api.get(`/api/marine/tide?beachId=${beachId}`)
                this.tide = res.data
            } catch (err) {
                this.error = err
            } finally {
                this.loading = false
            }
        }
    }
})
