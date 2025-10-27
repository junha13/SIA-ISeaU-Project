package lx.iseau.feature.sos.jellyfishreport;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root = Path.of("uploads");
    private final String publicUrlPrefix = "/files";

    public String store(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) return null;

            LocalDate today = LocalDate.now();
            Path dir = root.resolve(Path.of(
                    "jellyfish",
                    String.valueOf(today.getYear()),
                    String.format("%02d", today.getMonthValue())
            ));
            Files.createDirectories(dir);

            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.lastIndexOf('.') >= 0) {
                ext = original.substring(original.lastIndexOf('.')); // ".jpg"
            }
            String savedName = UUID.randomUUID() + ext;
            Path target = dir.resolve(savedName);

            // ★ 디버그 로그: 실제 저장 경로 확인
            System.out.println("[FileStorageService] save to: " + target.toAbsolutePath());

            // transferTo 대신 Files.copy 사용 (임시폴더/권한 이슈 회피)
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, REPLACE_EXISTING);
            }

            return publicUrlPrefix + "/jellyfish/" + today.getYear() + "/"
                    + String.format("%02d", today.getMonthValue()) + "/" + savedName;
        } catch (Exception e) {
            // 원인 메시지 함께 로깅
            e.printStackTrace();
            throw new RuntimeException("파일 저장 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
        }
    }
}
