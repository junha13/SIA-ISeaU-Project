import { defineStore } from 'pinia'
import { getDepthMap, getRiskAreas } from '@/api/safety'

export const useMapStore = defineStore('map', {
    state: () => ({
        depthMap: null,          // GeoJSON
        riskZones: [],           // [{lat,lng,label,depth}, ...]
        selectedBeachId: null,
    }),
    actions: {
        async fetchDepthMap(beachId) {
            this.selectedBeachId = beachId
            const res = await getDepthMap(beachId)
            this.depthMap = res?.geoJson || null
            return this.depthMap
        },
        async fetchRiskZones(beachId) {
            const res = await getRiskAreas(beachId)
            // GeoJSON -> 간단 배열로 변환
            this.riskZones = (res?.geoJson?.features || []).map(f => ({
                lat: f.geometry.coordinates[1],
                lng: f.geometry.coordinates[0],
                label: f.properties?.name || '위험구역',
                depth: f.properties?.depth || 1.5,
            }))
            return this.riskZones
        },
        clearRiskZones() { this.riskZones = [] },
    },
})
