import { defineStore } from 'pinia'
import { getGroupMembers } from '@/api/user'

export const useGroupStore = defineStore('group', {
    state: () => ({
        groupId: 'default',
        members: [],
        isLoading: false,
        error: null,
        lastFetched: null,
        ttl: 15000, // 15초 캐시
    }),
    actions: {
        async fetchMembers(force = false) {
            if (!force && this.lastFetched && Date.now() - this.lastFetched < this.ttl) {
                console.log('[GroupStore] Using cached members')
                return this.members
            }
            this.isLoading = true
            this.error = null
            try {
                this.members = await getGroupMembers(this.groupId)
                this.lastFetched = Date.now()
            } catch (e) {
                this.error = e.message || '그룹 구성원 정보 불러오기 실패'
            } finally {
                this.isLoading = false
            }
            return this.members
        },
    },
})
