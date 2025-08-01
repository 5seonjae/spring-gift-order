package gift.config;

import gift.interceptor.KakaoTokenInterceptor;
import gift.auth.LoginMemberArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginMemberArgumentResolver loginMemberArgumentResolver;
    private final KakaoTokenInterceptor kakaoTokenInterceptor;

    public WebConfig(
        LoginMemberArgumentResolver loginMemberArgumentResolver,
        KakaoTokenInterceptor kakaoTokenInterceptor
    ) {
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
        this.kakaoTokenInterceptor = kakaoTokenInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(loginMemberArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(kakaoTokenInterceptor)
            .addPathPatterns(
                "/api/wishes/**",
                "/api/orders/**",
                "/api/products/**"
            )
            .order(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET","POST","PUT","DELETE","OPTIONS","HEAD")
            .allowedHeaders(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cookie"
            )
            .allowCredentials(true)
            .maxAge(1800);
    }
}
