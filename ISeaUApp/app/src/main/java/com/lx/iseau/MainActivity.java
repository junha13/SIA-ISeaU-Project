package com.lx.iseau;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.os.Build;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// FCM 관련 임포트
import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;

    // 발급받은 FCM 토큰을 임시 저장할 변수
    private static String fcmToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 앱 실행 시 권한 요청하기 (위치 + 알림)
        checkAndRequestPermissions();

        // 2. 앱 실행 시 FCM 토큰 가져오기
        fetchFcmToken();

        // 웹 뷰 찾아서 설정하기
        WebView webview = findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        // 위치 정보 사용 설정 (필수)
        webSettings.setGeolocationEnabled(true);

        // HTTPS 페이지에서 HTTP 콘텐츠 로딩 허용
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // SSL 인증서 오류 무시 (개발용)
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        // WebChromeClient 설정 (위치 권한 팝업 자동 허용)
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        // JS ↔ 네이티브 브리지 연결
        webview.addJavascriptInterface(new AndroidBridge(this), "AndroidBridge");

        // 페이지 띄우기
        //webview.loadUrl("https://iseau.kr");
            webview.loadUrl("https://hellokiyo.ngrok.io");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * FCM 토큰을 발급받아 변수에 저장하는 함수
     */
    private void fetchFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "❌ FCM 토큰 가져오기 실패", task.getException());
                        return;
                    }
                    // 새 토큰 가져오기
                    fcmToken = task.getResult();
                    Log.d(TAG, "🔥 FCM 토큰 발급 성공: " + fcmToken);
                });
    }

    // 권한 체크 및 요청 함수
    private void checkAndRequestPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS // 알림 권한
            };
        } else {
            permissions = new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }

        boolean allGranted = true;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * WebView(JS) → 안드로이드 네이티브로 값 전달하는 브리지
     */
    public static class AndroidBridge {

        private final Context appContext;

        private static final String SERVER_URL = "https://hellokiyo.ngrok.io";
        private static final String SAVE_TOKEN_URL = SERVER_URL+"/api/fcm/save-token";

        public AndroidBridge(Context context) {
            this.appContext = context.getApplicationContext();
        }

        @JavascriptInterface // 💡 이 어노테이션이 필수입니다.
        public void setUserNumber(int userNumber) {
            Log.d(TAG, "📲 JS에서 로그인 정보 수신. userNumber: " + userNumber);

            // 1. 로컬 세션 저장
            UserSessionManager.INSTANCE.saveUserNumber(appContext, userNumber);
            Log.d(TAG, "✅ UserSessionManager 저장 완료");

            // 2. 워치 동기화
            WearSyncManager.INSTANCE.sendUserNumberToWatch(appContext, userNumber);
            Log.d(TAG, "✅ WearSyncManager 전송 요청 완료");

            // 3. 서버로 FCM 토큰 전송 (백그라운드 스레드)
            if (fcmToken != null && !fcmToken.isEmpty()) {
                sendTokenToServer(userNumber, fcmToken);
            } else {
                Log.e(TAG, "❌ 전송 실패: FCM 토큰이 아직 발급되지 않았습니다.");
            }
        }

        /**
         * 서버 API 호출하여 DB에 토큰 저장
         */
        private void sendTokenToServer(int userNumber, String token) {
            new Thread(() -> {
                try {
                    Log.d(TAG, "🚀 서버로 토큰 전송 시작... User: " + userNumber);

                    URL url = new URL(SAVE_TOKEN_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);

                    // JSON Body 생성
                    JSONObject json = new JSONObject();
                    json.put("userNumber", String.valueOf(userNumber));
                    json.put("token", token);

                    // 데이터 전송
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        Log.d(TAG, "✅ 토큰 서버 저장 성공!");
                    } else {
                        Log.e(TAG, "❌ 토큰 서버 저장 실패. 응답 코드: " + responseCode);
                    }
                    conn.disconnect();

                } catch (Exception e) {
                    Log.e(TAG, "❌ 토큰 전송 중 에러 발생", e);
                }
            }).start();
        }
    } // AndroidBridge 끝
} // MainActivity 끝