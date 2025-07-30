package gift.interceptor;

import gift.auth.LoginMemberArgumentResolver;
import gift.entity.Member;
import gift.service.KakaoTokenManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class KakaoTokenInterceptor implements HandlerInterceptor {

    private final KakaoTokenManager tokenManager;
    private final LoginMemberArgumentResolver resolver;

    public KakaoTokenInterceptor(
        KakaoTokenManager tokenManager,
        LoginMemberArgumentResolver resolver
    ) {
        this.tokenManager = tokenManager;
        this.resolver = resolver;
    }

    @Override
    public boolean preHandle(
        HttpServletRequest req,
        HttpServletResponse res,
        Object handler
    ) {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        boolean requiresAuth = Arrays.stream(hm.getMethodParameters())
            .anyMatch(resolver::supportsParameter);
        if (!requiresAuth) {
            return true;
        }

        Member member = resolver.resolve(req);
        if (member.isKakaoUser()) {
            tokenManager.ensureAccessToken(member.getId());
        }
        return true;
    }
}
