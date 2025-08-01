package gift.entity;

import gift.dto.api.KakaoTokenResponseDto;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Entity
@Table(name = "members")
public class Member {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile(".+@.+\\..+");
    private static final int PASSWORD_MIN_LENGTH = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false;

    @Column(unique = true)
    private Long kakaoId;

    private String nickname;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "accessToken", column = @Column(length = 2048)),
        @AttributeOverride(name = "refreshToken", column = @Column(length = 2048))
    })
    private KakaoTokens kakaoTokens;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WishItem> wishItems = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    public Member(Long id, String email, String password) {
        validate(email, password);

        this.id = id;
        this.email = email;
        this.password = password;
    }

    protected Member() {}

    public Member(String email, String password) {
        this(null, email, password);
    }

    public Member(Long kakaoId, String nickname) {
        if (kakaoId == null) {
            throw new IllegalArgumentException("카카오 ID는 필수입니다.");
        }
        this.kakaoId  = kakaoId;
        this.nickname = nickname;
    }

    public Member(Long kakaoId, String nickname, KakaoTokens kakaoTokens) {
        if (kakaoId == null) {
            throw new IllegalArgumentException("카카오 ID는 필수입니다.");
        }
        this.kakaoId  = kakaoId;
        this.nickname = nickname;
        this.kakaoTokens = kakaoTokens;
    }

    private void validate(String email, String password) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("유효한 이메일 형식이 아닙니다.");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }

        if (password.length() < PASSWORD_MIN_LENGTH) {
            throw new IllegalArgumentException("비밀번호는 최소 6자 이상이어야 합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public Long getKakaoId() {
        return kakaoId;
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isKakaoUser() {
        return kakaoTokens != null;
    }

    public String getAccessToken() {
        return kakaoTokens.getAccessToken();
    }

    public String getRefreshToken() {
        return kakaoTokens.getRefreshToken();
    }

    public boolean accessTokenExpired() {
        return kakaoTokens.isAceessExpired();
    }

    public boolean refreshTokenExpired() {
        return kakaoTokens.isRefreshExpired();
    }

    public void refreshTokens(KakaoTokenResponseDto dto) {
        this.kakaoTokens = KakaoTokens.from(dto);
    }

    public void updateAccessToken(String newAccessToken, long expiresIn) {
        this.kakaoTokens = kakaoTokens.withNewAccessToken(newAccessToken, expiresIn);
    }
}
