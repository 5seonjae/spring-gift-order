package gift.dto.api;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoTokenResponseDto(
    String accessToken,
    String refreshToken,
    long expiresIn,
    String tokenType
) {}
