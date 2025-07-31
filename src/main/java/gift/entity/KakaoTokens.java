package gift.entity;

import gift.dto.api.KakaoTokenResponseDto;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
public class KakaoTokens {

    private final String accessToken;
    private final String refreshToken;
    private final LocalDateTime accessExpiresAt;
    private final LocalDateTime refreshExpiresAt;

    protected KakaoTokens() {
        this.accessToken = null;
        this.refreshToken = null;
        this.accessExpiresAt = null;
        this.refreshExpiresAt = null;
    }

    private KakaoTokens(String accessToken, String refreshToken, LocalDateTime accessExpiresAt, LocalDateTime refreshExpiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessExpiresAt = accessExpiresAt;
        this.refreshExpiresAt = refreshExpiresAt;
    }

    public static KakaoTokens from(KakaoTokenResponseDto kakaoTokenResponseDto) {
        LocalDateTime now = LocalDateTime.now();
        return new KakaoTokens(
            kakaoTokenResponseDto.accessToken(),
            kakaoTokenResponseDto.refreshToken(),
            now.plusSeconds(kakaoTokenResponseDto.expiresIn()),
            now.plusDays(kakaoTokenResponseDto.refreshTokenExpiresIn())
        );
    }

    public KakaoTokens withNewAccessToken(String accessToken, long expiresIn) {
        return new KakaoTokens(
            accessToken,
            this.refreshToken,
            LocalDateTime.now().plusSeconds(expiresIn),
            this.refreshExpiresAt
        );
    }

    public boolean isAceessExpired() {
        return accessExpiresAt.isBefore(LocalDateTime.now().plusSeconds(60));
    }

    public boolean isRefreshExpired() {
        return refreshExpiresAt.isBefore(LocalDateTime.now());
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public LocalDateTime getAccessExpiresAt() {
        return accessExpiresAt;
    }

    public LocalDateTime getRefreshExpiresAt() {
        return refreshExpiresAt;
    }
}
