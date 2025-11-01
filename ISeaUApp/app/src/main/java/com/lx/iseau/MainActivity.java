package com.lx.iseau;

import android.os.Bundle;
import android.webkit.WebSettings; // WebSettings 사용을 위해 필요
import android.webkit.WebView;
import android.webkit.WebViewClient; // WebViewClient 사용을 위해 필요
import android.webkit.SslErrorHandler; // SslErrorHandler 사용을 위해 필요
import android.net.http.SslError; // SslError 사용을 위해 필요

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

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

        // --- 💡 여기에 WebViewClient 추가 ---
        webview.setWebViewClient(new WebViewClient() {
            // SSL 인증서 오류 발생 시 처리
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // 경고: 실제 운영 환경에서는 사용하면 안 됩니다.
                // Ngrok 테스트를 위해 모든 SSL 오류를 무시하고 계속 진행합니다.
                handler.proceed();
            }
        });

        // 페이지 띄우기
        webview.loadUrl("https://iseau.kr");
        //webview.loadUrl("https://hellokiyo.ngrok.io/");
        //webview.loadUrl("http://m.naver.com");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}