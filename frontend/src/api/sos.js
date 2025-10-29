// src/api/sos.js
import { useApi } from '@/utils/useApi.js';

// 해파리 제보 API (POST)
const { execute: submitJellyfishReport } = useApi('post', '/sos/jellyfish-report');

// 응급 대처법 조회 (GET)
const { execute: getFirstAidByCaseNum } = useApi('get', '/sos/first-aid');

// 응급 상황 목록 조회 (GET) 
const { execute: listFirstAidCases } = useApi('get', '/sos/first-aid/cases');

// 119/122 신고 기록 API (POST) <== 보류
//const { execute: logEmergencyCall } = useApi('post', '/api/report/emergency');


export const sosApi = {
    submitJellyfishReport,
    getFirstAidByCaseNum,
    listFirstAidCases,
    //logEmergencyCall
};
