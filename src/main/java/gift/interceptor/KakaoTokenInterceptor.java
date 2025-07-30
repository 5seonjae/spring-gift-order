package gift.interceptor;

import gift.auth.LoginMember;
import gift.auth.TokenExtractor;
import gift.entity.Member;
import gift.repository.MemberRepository;
import gift.service.KakaoTokenManager;
import gift.service.MemberExtractor;
import gift.service.TokenService;
import gift.util.BearerAuthHeaderParser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class KakaoTokenInterceptor implements HandlerInterceptor {

    private final KakaoTokenManager tokenManager;
    private final MemberExtractor memberExtractor;

    public KakaoTokenInterceptor(
        KakaoTokenManager tokenManager,
        MemberExtractor memberExtractor
    ) {
        this.tokenManager = tokenManager;
        this.memberExtractor = memberExtractor;
    }

    @Override
    public boolean preHandle(
        HttpServletRequest req,
        HttpServletResponse res,
        Object handler
    ) {
        if (!(handler instanceof HandlerMethod hm) ||
            Arrays.stream(hm.getMethodParameters())
                .noneMatch(p -> p.hasParameterAnnotation(LoginMember.class))) {
            return true;
        }

        Member member = memberExtractor.extractMember(req);
        if (member.isKakaoUser()) {
            tokenManager.ensureAccessToken(member.getId());
        }
        return true;
    }
}
