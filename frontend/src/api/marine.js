import { useApi } from '@/composables/useApi'

export const getWeather = (beachId) => {
    const { execute } = useApi('get', '/api/marine/weather')
    return execute({ beachId })
}

export const getTide = (beachId) => {
    const { execute } = useApi('get', '/api/marine/tide')
    return execute({ beachId })
}
