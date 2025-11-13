package com.lx.iseau.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.widget.Button
import android.widget.LinearLayout

/**
 * ✅ 심플 스타터: 권한 확인 후 업로드 서비스 시작/중지
 * - 기존 ISeaUWear.kt 를 유지하면서도, 독립적으로 실행 가능하도록 제공
 */
class UploadStarterActivity : ComponentActivity() {
    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { startIfGranted() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 80, 32, 32)
        }
        val startBtn = Button(this).apply { text = "심박 업로드 시작" }
        val stopBtn  = Button(this).apply { text = "심박 업로드 중지" }

        startBtn.setOnClickListener { ensurePermissions() }
        stopBtn.setOnClickListener { HeartRateUploadService.stop(this) }

        layout.addView(startBtn)
        layout.addView(stopBtn)
        setContentView(layout)
    }

    private fun ensurePermissions() {
        val needs = mutableListOf(Manifest.permission.BODY_SENSORS)
        val requires = needs.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (requires) requestPermissions.launch(needs.toTypedArray())
        else startIfGranted()
    }

    private fun startIfGranted() {
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            HeartRateUploadService.start(this)
        }
    }
}
