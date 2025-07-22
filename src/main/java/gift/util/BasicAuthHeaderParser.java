package gift.util;

import gift.exception.InvalidAuthorizationHeaderException;
import gift.exception.MissingAuthorizationHeaderException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class BasicAuthHeaderParser {

    public Credentials parse(String authorizationHeader) {
        if (authorizationHeader == null) {
            // 헤더 누락 → 401 Unauthorized
            throw new MissingAuthorizationHeaderException("Authorization 헤더가 필요합니다.");
        }
        if (!authorizationHeader.startsWith("Basic ")) {
            // 헤더 형식 오류 → 401 Unauthorized
            throw new InvalidAuthorizationHeaderException("Authorization 헤더 형식이 올바르지 않습니다.");
        }

        // Base64 디코딩
        String base64Cred = authorizationHeader.substring(6).trim();
        byte[] decoded = Base64.getDecoder().decode(base64Cred);
        String cred = new String(decoded, StandardCharsets.UTF_8);
        String[] parts = cred.split(":", 2);
        if (parts.length != 2) {
            // 디코딩 결과 형식 오류 → 401
            throw new InvalidAuthorizationHeaderException("Authorization 헤더 형식이 올바르지 않습니다.");
        }

        String email = parts[0];
        String rawPassword = parts[1];

        return new Credentials(email, rawPassword);
    }
}
