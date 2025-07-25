package gift;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private KakaoOAuthService kakaoOAuthService;

    @MockitoBean
    private TokenService tokenService;

    @Test
    void loginWithKakao_newMember() {
        String code = "code123";
        given(kakaoOAuthService.exchangeCodeForToken(code))
            .willReturn(new KakaoTokenResponseDto("AT","RT",3600,"Bearer"));
        Map<String,Object> accountMap = Map.of(
            "profile", Map.of("nickname","Neo")
        );
        given(kakaoOAuthService.fetchUserInfo("AT"))
            .willReturn(new KakaoUserResponseDto(999L, accountMap));

        given(memberRepository.findByKakaoId(anyLong()))
            .willReturn(Optional.empty());
        given(memberRepository.save(any(Member.class)))
            .willAnswer(inv -> inv.getArgument(0));

        given(tokenService.generateToken(any(), anyString()))
            .willReturn("JWT_TOKEN");

        String jwt = memberService.loginWithKakao(code);

        assertThat(jwt).isEqualTo("JWT_TOKEN");
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        then(memberRepository).should().save(captor.capture());
        Member saved = captor.getValue();
        assertThat(saved.getNickname()).isEqualTo("Neo");
    }

    @Test
    void loginWithKakao_existingMember() {
        String code = "code456";
        given(kakaoOAuthService.exchangeCodeForToken(code))
            .willReturn(new KakaoTokenResponseDto("AT2","RT2",3600,"Bearer"));
        Map<String,Object> accountMap = Map.of(
            "profile", Map.of("nickname","Old")
        );
        given(kakaoOAuthService.fetchUserInfo("AT2"))
            .willReturn(new KakaoUserResponseDto(123L, accountMap));

        Member existing = new Member(123L, "Old");
        given(memberRepository.findByKakaoId(123L))
            .willReturn(Optional.of(existing));

        given(tokenService.generateToken(any(), anyString()))
            .willReturn("JWT_OLD");

        String jwt = memberService.loginWithKakao(code);

        assertThat(jwt).isEqualTo("JWT_OLD");
        then(memberRepository).should(never()).save(any());
    }
}
