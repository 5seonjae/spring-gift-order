package gift.util;

import gift.exception.InvalidAuthorizationHeaderException;
import gift.exception.MissingAuthorizationHeaderException;
import org.springframework.stereotype.Component;

@Component
public class BearerAuthHeaderParser {

    public String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null) {
            // 헤더 누락 → 401 Unauthorized
            throw new MissingAuthorizationHeaderException("Authorization 헤더가 필요합니다.");
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            // 헤더 형식 오류 → 401 Unauthorized
            throw new InvalidAuthorizationHeaderException("Authorization 헤더 형식이 올바르지 않습니다.");
        }

        return authorizationHeader.substring(7).trim();
    }
}
