import { defineStore } from 'pinia'
import { getDepthMap, getRiskAreas } from '@/api/safety'

export const useMapStore = defineStore('map', {
    state: () => ({
        depthMap: null,
        riskZones: [],
        selectedBeachId: null,
        isLoading: false,
        error: null,
        lastFetched: null,
        ttl: 30000, // 30초 캐싱
    }),
    actions: {
        async fetchDepthMap(beachId) {
            if (this.lastFetched && Date.now() - this.lastFetched < this.ttl && this.depthMap) {
                console.log('[MapStore] Using cached depthMap')
                return this.depthMap
            }
            this.isLoading = true
            this.error = null
            try {
                const res = await getDepthMap(beachId)
                this.depthMap = res?.geoJson || null
                this.selectedBeachId = beachId
                this.lastFetched = Date.now()
            } catch (e) {
                this.error = e.message || '수심 지도 불러오기 실패'
            } finally {
                this.isLoading = false
            }
            return this.depthMap
        },
        async fetchRiskZones(beachId) {
            this.isLoading = true
            this.error = null
            try {
                const res = await getRiskAreas(beachId)
                this.riskZones = (res?.geoJson?.features || []).map(f => ({
                    lat: f.geometry.coordinates[1],
                    lng: f.geometry.coordinates[0],
                    label: f.properties?.name || '위험 구역',
                    depth: f.properties?.depth || 1.5,
                }))
            } catch (e) {
                this.error = e.message || '위험 구역 정보 불러오기 실패'
            } finally {
                this.isLoading = false
            }
            return this.riskZones
        },
    },
})
