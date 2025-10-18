// src/api/sos.js
import { useApi } from '@/utils/useApi.js';

// 해파리 제보 API (POST)
const { execute: submitJellyfishReport } = useApi('post', '/report/jellyfish');

// 간편 신고 API (POST)
const { execute: submitSimpleReport } = useApi('post', '/report/simple');

// 119/122 신고 기록 API (POST)
const { execute: logEmergencyCall } = useApi('post', '/report/emergency');

export const sosApi = {
    submitJellyfishReport,
    submitSimpleReport,
    logEmergencyCall
};
