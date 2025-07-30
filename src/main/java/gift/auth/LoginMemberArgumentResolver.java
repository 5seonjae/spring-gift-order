package gift.auth;

import gift.entity.Member;
import gift.repository.MemberRepository;
import gift.service.MemberExtractor;
import gift.service.TokenService;
import gift.util.BearerAuthHeaderParser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginMemberArgumentResolver
    implements HandlerMethodArgumentResolver {

    private final MemberExtractor memberExtractor;

    public LoginMemberArgumentResolver(
        MemberExtractor memberExtractor
    ) {
        this.memberExtractor = memberExtractor;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && parameter.getParameterType().equals(Member.class);
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest req = webRequest.getNativeRequest(HttpServletRequest.class);
        return memberExtractor.extractMember(req);
    }
}
