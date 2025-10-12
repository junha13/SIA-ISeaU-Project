import { defineStore } from 'pinia'
import { getGroupMembers } from '@/api/user'

export const useGroupStore = defineStore('group', {
    state: () => ({
        groupId: 'default',     // 필요 시 라우트/설정으로 교체
        members: [],
    }),
    actions: {
        async fetchMembers() {
            this.members = await getGroupMembers(this.groupId)
            return this.members
        },
    },
})
