import { useApi } from '@/composables/useApi'

export const getDrowningAlert = (cctvId) => {
    const { execute } = useApi('get', '/api/safety/drowning-alert')
    return execute({ cctvId })
}

export const getRealtimeWarning = (lat, lng) => {
    const { execute } = useApi('get', '/api/safety/realtime-warning')
    return execute({ lat, lng })
}

export const getDepthMap = (beachId) => {
    const { execute } = useApi('get', '/api/map/depth')
    return execute({ beachId })
}

export const getRiskAreas = (beachId) => {
    const { execute } = useApi('get', '/api/map/risk-area')
    return execute({ beachId })
}
