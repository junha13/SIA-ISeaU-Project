import { useApi } from '@/composables/useApi'

export const putUserLocation = (lat, lng) => {
    const { execute } = useApi('put', '/api/user/location')
    return execute({ lat, lng })
}

export const postWarningMessage = (zoneId, message) => {
    const { execute } = useApi('post', '/api/user/warning-message')
    return execute({ zoneId, message })
}

export const postSOS = (lat, lng, detail) => {
    const { execute } = useApi('post', '/api/emergency/sos')
    return execute({ lat, lng, detail })
}

export const getGroupMembers = (groupId) => {
    const { execute } = useApi('get', `/api/group/${groupId}/members`)
    return execute()
}
