package gift.dto.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record KakaoUserResponseDto (
    long id,
    @JsonProperty("kakao_account") Map<String,Object> account
) {

    public String nickname() {
        Map<String,Object> profile = (Map<String,Object>) account.get("profile");
        return profile == null ? null : (String) profile.get("nickname");
    }
}
