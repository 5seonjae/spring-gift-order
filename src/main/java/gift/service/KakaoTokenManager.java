package gift.service;

import gift.dto.api.KakaoTokenResponseDto;
import gift.entity.Member;
import gift.exception.ReauthorizeRequiredException;
import gift.repository.MemberRepository;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KakaoTokenManager {

    private final KakaoOAuthService kakaoOAuthService;
    private final MemberRepository memberRepository;

    public KakaoTokenManager(
        KakaoOAuthService kakaoOAuthService,
        MemberRepository memberRepository
    ) {
        this.kakaoOAuthService = kakaoOAuthService;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public String ensureAccessToken(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new NoSuchElementException("회원이 없습니다."));

        if (member.accessTokenExpired()) {
            if (member.refreshTokenExpired()) {
                throw new ReauthorizeRequiredException("Refresh token이 만료되어, 사용자의 재승인이 필요합니다.");
            }
            KakaoTokenResponseDto dto = kakaoOAuthService.refreshTokenGrant(
                member.getRefreshToken()
            );
            member.updateAccessToken(dto.accessToken(), dto.expiresIn());
        }
        return member.getAccessToken();
    }
}
