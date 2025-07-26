package gift.service;

import gift.dto.api.KakaoTokenResponseDto;
import gift.dto.api.KakaoUserResponseDto;
import gift.exception.KakaoOAuthException;
import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Service
public class KakaoOAuthService {

    @Value("${kakao.client-id}")
    private final String clientId;

    @Value("${kakao.auth-url}")
    private final String authUrl;

    @Value("${kakao.api-url}")
    private final String apiUrl;

    @Value("${kakao.redirect-uri}")
    private final String redirectUri;

    private final WebClient webClient;

    public KakaoOAuthService(
        @Value("${kakao.client-id}") String clientId,
        @Value("${kakao.auth-url}") String authUrl,
        @Value("${kakao.api-url}") String apiUrl,
        @Value("${kakao.redirect-uri}") String redirectUri,
        WebClient.Builder builder
    ) {
        this.clientId   = clientId;
        this.authUrl    = authUrl;
        this.apiUrl     = apiUrl;
        this.redirectUri= redirectUri;
        this.webClient = builder
            .clientConnector(new ReactorClientHttpConnector(
                HttpClient.create()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
                    .responseTimeout(Duration.ofSeconds(3))
            ))
            .build();
    }

    public KakaoTokenResponseDto exchangeCodeForToken(String code) {
        return webClient.post()
            .uri(authUrl + "/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters
                .fromFormData("grant_type", "authorization_code")
                .with("client_id", clientId)
                .with("redirect_uri", redirectUri)
                .with("code", code))
            .retrieve()
            .onStatus(HttpStatusCode::isError, resp ->
                resp.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new KakaoOAuthException(
                        resp.statusCode(),
                        "토큰 교환 실패: " + resp.statusCode() + " - " + body
                    )))
            )
            .bodyToMono(KakaoTokenResponseDto.class)
            .timeout(Duration.ofSeconds(3))
            .block();
    }

    public KakaoUserResponseDto fetchUserInfo(String accessToken) {
        return webClient.get()
            .uri(apiUrl + "/v2/user/me")
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .onStatus(HttpStatusCode::isError, resp ->
                resp.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new KakaoOAuthException(
                        resp.statusCode(),
                        "유저 정보 조회 실패: " + resp.statusCode() + " - " + body
                    )))
            )
            .bodyToMono(KakaoUserResponseDto.class)
            .timeout(Duration.ofSeconds(3))
            .block();
    }
}
