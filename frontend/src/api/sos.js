// src/api/sos.js
import { useApi } from '@/utils/useApi.js';

// 해파리 제보 API (POST)
const { execute: submitJellyfishReport } = useApi('post', '/report/jellyfish');

// 간편 신고 API (POST)
//const { execute: submitSimpleReport } = useApi('post', '/report/simple');
// 간편 신고 -> 응급 대처법 바뀌었기에 주석처리

// 응급 대처법 조회 (GET)
const { execute: getFirstAidByCaseNum } = useApi('get', '/report/first-aid');

// 응급 상황 목록 조회: GET /report/first-aid/cases
const { execute: listFirstAidCases } = useApi('get', '/report/first-aid/cases');

// 119/122 신고 기록 API (POST)
const { execute: logEmergencyCall } = useApi('post', '/report/emergency');

export const sosApi = {
    submitJellyfishReport,
    //submitSimpleReport,
    showFirstAid,
    logEmergencyCall
};
