package lx.iseau.feature.controltower;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class TtsService {

    /**
     * 전달받은 텍스트를 Google TTS로 MP3 생성해서
     * Base64 문자열로 리턴
     */
    public String synthesizeToBase64(String text) throws Exception {

        // 클라이언트는 try-with-resources로 열고 닫기
        try (TextToSpeechClient client = TextToSpeechClient.create()) {

            // 1) 입력 텍스트
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(text)
                    .build();

            // 2) 음성 설정 (한국어 + 목소리 유형)
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("ko-KR")
                    // 콘솔에서 확인한 한국어 보이스 이름으로 바꿔도 됨
                    .setName("ko-KR-Neural2-A")
                    .build();

            // 3) 오디오 포맷 설정
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .build();

            // 4) TTS 호출
            SynthesizeSpeechResponse response =
                    client.synthesizeSpeech(input, voice, audioConfig);

            ByteString audioContents = response.getAudioContent();

            // 5) MP3 바이트를 Base64 문자열로 변환
            return Base64.getEncoder()
                    .encodeToString(audioContents.toByteArray());
        }
    }
}
