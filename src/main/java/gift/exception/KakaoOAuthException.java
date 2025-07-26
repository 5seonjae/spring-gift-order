package gift.exception;

import org.springframework.http.HttpStatusCode;

public class KakaoOAuthException extends RuntimeException {
    private final HttpStatusCode statusCode;

    public KakaoOAuthException(HttpStatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
