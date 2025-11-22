package lx.iseau.feature.voice;  // 네가 쓴 패키지

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.HashMap;
import java.util.Map;

@Component   // ✅ 이거 추가!!
public class VoiceWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("[VoiceWS] 클라이언트 접속: " + session.getId());
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,
                                      org.springframework.web.socket.CloseStatus status) throws Exception {
        System.out.println("[VoiceWS] 클라이언트 접속 종료: " + session.getId());
        sessions.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("[VoiceWS] 수신 메시지 (무시): " + message.getPayload());
    }

    // 관제에서 방송 발송할 때 호출될 메서드
    public void broadcastTts(String cctvName, String messageText, String audioBase64) {
        if (audioBase64 == null || audioBase64.isEmpty()) {
            System.out.println("[VoiceWS] 보낼 오디오가 없습니다.");
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "TTS");
            payload.put("cctvName", cctvName);
            payload.put("message", messageText);
            payload.put("audioContent", audioBase64);

            String json = objectMapper.writeValueAsString(payload);
            TextMessage textMessage = new TextMessage(json);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }

            System.out.println("[VoiceWS] TTS 방송 전송 완료. 세션 수 = " + sessions.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
