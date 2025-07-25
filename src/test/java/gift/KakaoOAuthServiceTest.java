package gift;

import static org.assertj.core.api.Assertions.*;

import gift.dto.api.KakaoTokenResponseDto;
import gift.dto.api.KakaoUserResponseDto;
import gift.exception.KakaoOAuthException;
import gift.service.KakaoOAuthService;
import java.io.IOException;

import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class KakaoOAuthServiceTest {

    private MockWebServer server;
    private KakaoOAuthService service;

    private static final String CLIENT_ID    = "TEST-CLIENT";
    private static final String REDIRECT_URI = "http://localhost/callback";

    @BeforeEach
    void startServer() throws IOException {
        server = new MockWebServer();
        server.start();

        WebClient.Builder builder = WebClient
            .builder()
            .baseUrl(server.url("/").toString());;

        service = new KakaoOAuthService(
            CLIENT_ID,
            server.url("/oauth").toString(),   // authUrl
            server.url("/v2/user").toString(), // apiUrl
            REDIRECT_URI,
            builder
        );
    }

    @AfterEach
    void shutdown() throws IOException {
        server.shutdown();
    }

    @Test
    void exchangeCodeForToken_success() {
        server.enqueue(new MockResponse()
            .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .setBody("""
              {
                "access_token":"AAA",
                "refresh_token":"RRR",
                "expires_in":3600,
                "token_type":"Bearer"
              }
            """));

        KakaoTokenResponseDto dto = service.exchangeCodeForToken("CODE123");

        assertThat(dto.accessToken()).isEqualTo("AAA");
        assertThat(dto.refreshToken()).isEqualTo("RRR");
        assertThat(dto.expiresIn()).isEqualTo(3600);
    }

    @Test
    void fetchUserInfo_success() {
        server.enqueue(new MockResponse()
            .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .setBody("""
              {
                "id":999,
                "kakao_account":{
                  "profile":{"nickname":"Neo"}
                }
              }
            """));

        KakaoUserResponseDto user = service.fetchUserInfo("AAA");

        assertThat(user.id()).isEqualTo(999L);
        assertThat(user.nickname()).isEqualTo("Neo");
    }

    @Test
    void exchangeCodeForToken_error() {
        server.enqueue(new MockResponse().setResponseCode(400).setBody("bad grant"));

        assertThatThrownBy(() -> service.exchangeCodeForToken("BAD"))
            .isInstanceOf(KakaoOAuthException.class)
            .hasMessageContaining("토큰 교환 실패");
    }

    @Test
    void fetchUserInfo_error() {
        server.enqueue(new MockResponse().setResponseCode(401).setBody("unauthorized"));

        assertThatThrownBy(() -> service.fetchUserInfo("WRONG"))
            .isInstanceOf(KakaoOAuthException.class)
            .hasMessageContaining("유저 정보 조회 실패");
    }
}
