package gift;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import gift.dto.api.KakaoTokenResponseDto;
import gift.dto.api.KakaoUserResponseDto;
import gift.entity.Member;
import gift.repository.MemberRepository;
import gift.service.KakaoOAuthService;
import gift.service.MemberService;
import gift.service.TokenService;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private KakaoOAuthService kakaoOAuthService;

    @Mock
    private TokenService tokenService;

    @Test
    @DisplayName("[성공] 신규 카카오 회원 로그인 시 DB에 저장하고 JWT 반환")
    void loginWithKakao_newMember() {
        String code = "code123";
        given(kakaoOAuthService.exchangeCodeForToken(code))
            .willReturn(new KakaoTokenResponseDto(
                "AT",
                "RT",
                3600,
                "Bearer"
            ));

        Map<String, Object> accountMap = Map.of(
            "profile", Map.of("nickname", "Neo")
        );
        given(kakaoOAuthService.fetchUserInfo("AT"))
            .willReturn(new KakaoUserResponseDto(999L, accountMap));

        given(memberRepository.findByKakaoId(999L)).willReturn(Optional.empty());
        given(memberRepository.save(any(Member.class)))
            .willAnswer(inv -> inv.getArgument(0));

        given(tokenService.generateToken(any(), anyString()))
            .willReturn("JWT_TOKEN");

        String jwt = memberService.loginWithKakao(code);

        assertThat(jwt).isEqualTo("JWT_TOKEN");
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        then(memberRepository).should().save(captor.capture());
        assertThat(captor.getValue().getNickname()).isEqualTo("Neo");
    }

    @Test
    @DisplayName("[성공] 기존 카카오 회원 로그인 시 DB 저장 없이 JWT 반환")
    void loginWithKakao_existingMember() {
        String code = "code456";
        given(kakaoOAuthService.exchangeCodeForToken(code))
            .willReturn(new KakaoTokenResponseDto(
                "AT2",
                "RT2",
                3600,
                "Bearer"
            ));

        Map<String, Object> accountMap = Map.of(
            "profile", Map.of("nickname", "Old")
        );
        given(kakaoOAuthService.fetchUserInfo("AT2"))
            .willReturn(new KakaoUserResponseDto(123L, accountMap));

        Member existing = new Member(123L, "Old");
        given(memberRepository.findByKakaoId(123L)).willReturn(Optional.of(existing));

        given(tokenService.generateToken(any(), anyString()))
            .willReturn("JWT_OLD");

        String jwt = memberService.loginWithKakao(code);

        assertThat(jwt).isEqualTo("JWT_OLD");
        then(memberRepository).should(never()).save(any());
    }
}