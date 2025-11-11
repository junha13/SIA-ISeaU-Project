package com.lx.iseau.presentation

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

/**
 * 앱 전체의 상태와 ViewModel을 유지하기 위한 사용자 지정 Application 클래스입니다.
 */
class ISeaUApp : Application(), ViewModelStoreOwner {

    // Application의 수명 주기에 바인딩되는 ViewModelStore를 생성합니다.
    override val viewModelStore = androidx.lifecycle.ViewModelStore()

    // 앱 전체에서 공유되는 HealthDataViewModel 인스턴스
    val healthViewModel: HealthDataViewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this))[HealthDataViewModel::class.java]
    }

    // Java 코드에서 ViewModel에 접근하기 위한 public getter
    fun getHealthViewModel(): HealthDataViewModel {
        return healthViewModel
    }
}