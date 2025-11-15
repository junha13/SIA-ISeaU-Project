// com/lx/iseau/UserSessionManager.kt

package com.lx.iseau

import android.content.Context

/**
 * 로그인한 사용자 정보를 로컬에 저장/조회하는 헬퍼
 * - SharedPreferences에 user_number를 저장해서
 *   어디서든 꺼내 쓸 수 있게 함
 */
object UserSessionManager {

    private const val PREF_NAME = "iseau_prefs"
    private const val KEY_USER_NUMBER = "user_number"

    /**
     * 로그인 성공 후 서버에서 받은 userNumber를 저장
     */
    fun saveUserNumber(context: Context, userNumber: Int) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_USER_NUMBER, userNumber)
            .apply()
    }

    /**
     * 현재 로그인된 사용자 번호를 조회
     * - 저장된 값이 없으면 -1 반환
     */
    fun getUserNumber(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_USER_NUMBER, -1)
    }

    /**
     * 로그아웃 시 호출 (선택)
     */
    fun clearUser(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_USER_NUMBER)
            .apply()
    }
}
