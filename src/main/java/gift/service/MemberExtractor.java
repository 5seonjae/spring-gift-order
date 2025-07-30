package gift.service;

import gift.auth.TokenExtractor;
import gift.entity.Member;
import gift.repository.MemberRepository;
import gift.util.BearerAuthHeaderParser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class MemberExtractor {

    private final TokenService tokenService;
    private final MemberRepository memberRepository;
    private final BearerAuthHeaderParser bearerAuthHeaderParser;
    private final TokenExtractor tokenExtractor;

    public MemberExtractor(
        TokenService tokenService,
        MemberRepository memberRepository,
        BearerAuthHeaderParser bearerAuthHeaderParser,
        TokenExtractor tokenExtractor
    ) {
        this.tokenService = tokenService;
        this.memberRepository = memberRepository;
        this.bearerAuthHeaderParser = bearerAuthHeaderParser;
        this.tokenExtractor = tokenExtractor;
    }

    public Member extractMember(HttpServletRequest req) {
        String header = tokenExtractor.extractBearerHeader(req);
        String token = bearerAuthHeaderParser.extractBearerToken(header);
        Claims claims = tokenService .parseClaims(token);
        Long memberId = Long.valueOf(claims.getSubject());
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원입니다."));
    }
}
