package com.lx.iseau;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings; // WebSettings 사용을 위해 필요
import android.webkit.WebView;
import android.webkit.WebViewClient; // WebViewClient 사용을 위해 필요
import android.webkit.SslErrorHandler; // SslErrorHandler 사용을 위해 필요
import android.net.http.SslError; // SslError 사용을 위해 필요

import android.content.Context;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 웹 뷰 찾아서 설정하기
        WebView webview = findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        // 💡 추가된 설정: HTTPS 페이지에서 HTTP 콘텐츠 로딩을 허용합니다. (Ngrok 테스트 환경에 필요)
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // SSL 인증서 오류 무시 (운영에서는 X, 지금은 테스트용)
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        // ✅ 여기서 JS ↔ 네이티브 브리지 연결
        // LoginPage.vue 에서 window.AndroidBridge.setUserNumber(...) 호출하면
        // 아래 AndroidBridge.setUserNumber()가 실행됨
        webview.addJavascriptInterface(new AndroidBridge(this), "AndroidBridge");

        // 페이지 띄우기
        //webview.loadUrl("https://iseau.kr");
        webview.loadUrl("https://hellokiyo.ngrok.io/");
        //webview.loadUrl("http://m.naver.com");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    /**
     * ✅ WebView(JS) → 안드로이드 네이티브로 값 전달하는 브리지
     * - Vue(LoginPage.vue)에서 window.AndroidBridge.setUserNumber(user_number) 호출 시 실행됨
     */
    public static class AndroidBridge {

        private final Context appContext;

        public AndroidBridge(Context context) {
            // 메모리 릭 방지를 피하려고 ApplicationContext 사용
            this.appContext = context.getApplicationContext();
        }

        @JavascriptInterface
        public void setUserNumber(int userNumber) {
            Log.d(TAG, "📲 JS에서 전달받은 userNumber: " + userNumber);

            // 1) 폰 내부 세션 저장 (Kotlin object UserSessionManager)
            UserSessionManager.INSTANCE.saveUserNumber(appContext, userNumber);
            Log.d(TAG, "✅ UserSessionManager에 userNumber 저장 완료: " + userNumber);

            // 2) 워치로도 동기화 (폰 → 워치 Data Layer)
            WearSyncManager.INSTANCE.sendUserNumberToWatch(appContext, userNumber);
            Log.d(TAG, "✅ WearSyncManager로 워치 전송 요청 완료: " + userNumber);
        }
    }
}