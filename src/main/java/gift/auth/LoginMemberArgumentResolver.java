package gift.auth;

import gift.entity.Member;
import gift.repository.MemberRepository;
import gift.service.TokenService;
import gift.util.BearerAuthHeaderParser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.util.WebUtils;

@Component
public class LoginMemberArgumentResolver
    implements HandlerMethodArgumentResolver {

    private final TokenService tokenService;
    private final MemberRepository memberRepository;
    private final BearerAuthHeaderParser authHeaderParser;
    private final TokenExtractor tokenExtractor;

    public LoginMemberArgumentResolver(TokenService tokenService,
                                       MemberRepository memberRepository,
                                       BearerAuthHeaderParser authHeaderParser,
        TokenExtractor tokenExtractor) {
        this.tokenService = tokenService;
        this.memberRepository = memberRepository;
        this.authHeaderParser = authHeaderParser;
        this.tokenExtractor = tokenExtractor;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && parameter.getParameterType().equals(Member.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory) {
        HttpServletRequest r = webRequest.getNativeRequest(HttpServletRequest.class);

        String header = tokenExtractor.extractBearerHeader(r);
        String token = authHeaderParser.extractBearerToken(header);
        Claims claims = tokenService.parseClaims(token);
        Long memberId = Long.valueOf(claims.getSubject());

        return memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원입니다."));
    }
}
