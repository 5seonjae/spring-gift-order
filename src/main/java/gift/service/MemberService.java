package gift.service;

import gift.dto.api.KakaoTokenResponseDto;
import gift.dto.api.KakaoUserResponseDto;
import gift.dto.api.MemberRegisterRequestDto;
import gift.entity.KakaoTokens;
import gift.entity.Member;
import gift.exception.InvalidCredentialsException;
import gift.repository.MemberRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenService tokenService;
    private final KakaoOAuthService kakaoOAuthService;

    public MemberService(
        MemberRepository memberRepository,
        TokenService tokenService,
        KakaoOAuthService kakaoOAuthService
    ) {
        this.memberRepository = memberRepository;
        this.tokenService = tokenService;
        this.kakaoOAuthService = kakaoOAuthService;
    }

    // 회원 등록
    public String registerMember(MemberRegisterRequestDto requestDto) {
        // 이메일 중복 검사
        if (memberRepository.existsByEmail(requestDto.getEmail())) {
            throw new DuplicateKeyException("이미 사용 중인 이메일입니다.");
        }

        // 회원 저장
        Member member = new Member(requestDto.getEmail(), requestDto.getPassword());
        Member saved = memberRepository.save(member);

        // JWT 토큰 생성
        return tokenService.generateToken(saved.getId(), saved.getEmail());
    }

    public String loginMember(String email, String password) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!member.getPassword().equals(password)) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return tokenService.generateToken(member.getId(), member.getEmail());
    }

    public boolean isAdmin(String email) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        return member.getIsAdmin();
    }

    @Transactional
    public String loginWithKakao(String code) {

        KakaoTokenResponseDto tokenDto = kakaoOAuthService.exchangeCodeForToken(code);
        KakaoUserResponseDto userDto  = kakaoOAuthService.fetchUserInfo(tokenDto.accessToken());

        Long kakaoId   = userDto.id();
        String nickname = userDto.nickname();

        Member member = memberRepository.findByKakaoId(kakaoId)
            .orElseGet(() ->
                memberRepository.save(new Member(kakaoId, nickname, KakaoTokens.from(tokenDto)))
            );

        member.refreshTokens(tokenDto);

        return tokenService.generateToken(member.getId(), nickname);
    }
}
