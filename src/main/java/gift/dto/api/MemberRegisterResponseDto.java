package gift.dto.api;

public class MemberRegisterResponseDto {

    private String token;

    public MemberRegisterResponseDto(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
