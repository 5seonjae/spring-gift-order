package gift;

import gift.auth.LoginMember;
import gift.auth.LoginMemberArgumentResolver;
import gift.config.WebConfig;
import gift.dto.api.WishRequestDto;
import gift.entity.Member;
import gift.entity.Product;
import gift.exception.InvalidAuthorizationHeaderException;
import gift.exception.MissingAuthorizationHeaderException;
import gift.repository.MemberRepository;
import gift.repository.ProductRepository;
import gift.service.WishService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.NativeWebRequest;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class WishControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WishService wishService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private LoginMemberArgumentResolver loginMemberArgumentResolver;

    Member member; // 매 테스트마다 사용할 로그인 회원

    @BeforeEach
    void setUp() {
        // (1) 테스트용 회원 저장
        member = memberRepository.save(new Member("test@example.com", "pwd1234"));

        // (2) ArgumentResolver 목 스텁
        given(loginMemberArgumentResolver.supportsParameter(any(MethodParameter.class)))
                .willAnswer(invocation -> {
                    MethodParameter param = invocation.getArgument(0);
                    // 오직 @LoginMember가 붙은 Member 파라미터만 true
                    return param.hasParameterAnnotation(LoginMember.class)
                            && Member.class.equals(param.getParameterType());
                });

        given(loginMemberArgumentResolver.resolveArgument(any(), any(), any(), any()))
            .willAnswer(invocation -> {
                NativeWebRequest req = invocation.getArgument(2);
                String header = req.getHeader("Authorization");
                if (header == null) {
                    // 헤더 누락 → 401 Unauthorized
                    throw new MissingAuthorizationHeaderException("Authorization 헤더가 필요합니다.");
                }
                if (!header.startsWith("Bearer ")) {
                    // 헤더 형식 오류 → 401 Unauthorized
                    throw new InvalidAuthorizationHeaderException(
                        "Authorization 헤더 형식이 올바르지 않습니다.");
                }
                return member; // 항상 로그인 성공
            });
    }

    @Test
    @DisplayName("GET /api/wishes – 정상 조회 시 200 OK + JSON 배열 반환")
    void list_withValidAuth_shouldReturnWishItems() throws Exception {
        // given
        var sampleProduct = new Product("초콜릿", 1000, "https://image.com/choco.png");
        Product saved = productRepository.save(sampleProduct);
        var sampleWishRequestDto = new WishRequestDto(saved.getId(), 2);
        wishService.addWishItemForMember(member, sampleWishRequestDto);

        // when & then
        mockMvc.perform(get("/api/wishes")
                        .header("Authorization", "Bearer dummy-token")  // resolver가 mock이면 토큰 내용 무관
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "id,desc")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 실제 리스트 값 검증
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].productId").value(saved.getId()))
                .andExpect(jsonPath("$.content[0].name").value("초콜릿"))
                .andExpect(jsonPath("$.content[0].price").value(1000))
                .andExpect(jsonPath("$.content[0].imageUrl").value("https://image.com/choco.png"))
                .andExpect(jsonPath("$.content[0].quantity").value(2));
    }

    @Test
    @DisplayName("GET /api/wishes – 아이템 없으면 빈 배열")
    void list_empty_shouldReturnEmptyArray() throws Exception {
        mockMvc.perform(get("/api/wishes")
                        .header("Authorization", "Bearer dummy-token")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "id,desc")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                // 페이징된 결과의 content 배열이 비어 있는지 확인
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                // 페이징 메타데이터 검증
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.empty").value(true));
    }

    @Test
    @DisplayName("GET /api/wishes – 여러 아이템 조회 (페이징)")
    void list_multipleItems_withPaging_shouldReturnAll() throws Exception {
        // given
        var p1 = productRepository.save(new Product("초콜릿", 1000, "https://image.com/choco.png"));
        var p2 = productRepository.save(new Product("사탕",   500,  "https://image.com/candy.png"));
        wishService.addWishItemForMember(member, new WishRequestDto(p1.getId(), 1));
        wishService.addWishItemForMember(member, new WishRequestDto(p2.getId(), 3));

        // when & then
        mockMvc.perform(get("/api/wishes")
                        .header("Authorization", "Bearer dummy-token")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "id,desc")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())

                // — 메타데이터 검증 —
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.numberOfElements").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))

                // — content 리스트 검증 (id 내림차순) —
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].productId").value(p2.getId()))
                .andExpect(jsonPath("$.content[0].name").value("사탕"))
                .andExpect(jsonPath("$.content[0].price").value(500))
                .andExpect(jsonPath("$.content[0].imageUrl").value("https://image.com/candy.png"))
                .andExpect(jsonPath("$.content[0].quantity").value(3))

                .andExpect(jsonPath("$.content[1].productId").value(p1.getId()))
                .andExpect(jsonPath("$.content[1].name").value("초콜릿"))
                .andExpect(jsonPath("$.content[1].price").value(1000))
                .andExpect(jsonPath("$.content[1].imageUrl").value("https://image.com/choco.png"))
                .andExpect(jsonPath("$.content[1].quantity").value(1));
    }

    @Test
    @DisplayName("GET /api/wishes – 인증 헤더 없으면 401 Unauthorized")
    void list_withoutAuthHeader_unauthorized() throws Exception {
        mockMvc.perform(get("/api/wishes"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("Authorization 헤더가 필요합니다."));
    }

    @Test
    @DisplayName("GET /api/wishes – 잘못된 Authorization 프리픽스")
    void list_badAuthPrefix_unauthorized() throws Exception {
        mockMvc.perform(get("/api/wishes")
                .header("Authorization", "Basic abc123"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("Authorization 헤더 형식이 올바르지 않습니다."));
    }


    @Test
    @DisplayName("POST /api/wishes – productId null → Bad Request")
    void add_nullProductId_badRequest() throws Exception {
        String body = """
            { "productId": "", "quantity": 2 }
            """;

        mockMvc.perform(post("/api/wishes")
                .header("Authorization", "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.productId")
                .value("상품 ID는 필수입니다."));
    }

    @Test
    @DisplayName("POST /api/wishes – 없는 상품 → 404")
    void add_nonexistentProduct_notFound() throws Exception {
        String body = """
            { "productId": 999, "quantity": 1 }
            """;

        mockMvc.perform(post("/api/wishes")
                .header("Authorization", "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                .value("상품을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("POST /api/wishes – quantity null → 400")
    void add_nullQuantity_badRequest() throws Exception {
        String body = """
            { "productId": 5, "quantity": "" }
            """;

        mockMvc.perform(post("/api/wishes")
                .header("Authorization", "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.quantity")
                .value("상품 수량은 필수입니다."));
    }

    @Test
    @DisplayName("POST /api/wishes – quantity 0 이하 → 400")
    void add_invalidQuantity_badRequest() throws Exception {
        String body = """
            { "productId": 5, "quantity": 0 }
            """;

        mockMvc.perform(post("/api/wishes")
                .header("Authorization", "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.quantity")
                .value("한 개 이상 담을 수 있습니다."));
    }

    @Test
    @DisplayName("POST /api/wishes – 인증 헤더 누락 → 401")
    void add_withoutAuth_unauthorized() throws Exception {
        String body = """
            { "productId": 5, "quantity": 2 }
            """;

        mockMvc.perform(post("/api/wishes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("Authorization 헤더가 필요합니다."));
    }

    @Test
    @DisplayName("POST /api/wishes – 잘못된 프리픽스 → 401")
    void add_badPrefix_unauthorized() throws Exception {
        String body = """
            { "productId": 5, "quantity": 2 }
            """;

        mockMvc.perform(post("/api/wishes")
                .header("Authorization", "Basic abc123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("Authorization 헤더 형식이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("POST /api/wishes – productId 누락 → 400")
    void add_missingProductId_badRequest() throws Exception {
        String body = """
            { productId : , quantity : 2 }
            """;

        mockMvc.perform(post("/api/wishes")
                .header("Authorization", "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("요청 JSON 형식이 잘못되었습니다."));
    }

    @Test
    @DisplayName("DELETE /api/wishes – 정상 ⇒ No Content")
    void delete_success_noContent() throws Exception {
        var sampleProduct = new Product("초콜릿", 1000, "https://image.com/choco.png");
        Product saved = productRepository.save(sampleProduct);
        var sampleWishRequestDto = new WishRequestDto(saved.getId(), 2);
        wishService.addWishItemForMember(member, sampleWishRequestDto);

        mockMvc.perform(delete("/api/wishes/products/{productId}", saved.getId())
                .header("Authorization", "Bearer dummy-token"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/wishes – 없는 상품 ⇒ Not Found")
    void delete_nonexistent_notFound() throws Exception {
        mockMvc.perform(delete("/api/wishes/products/{productId}", 999L)
                .header("Authorization", "Bearer dummy-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                .value("상품을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("DELETE /api/wishes – 헤더 누락 ⇒ Unauthorized")
    void delete_withoutAuth_unauthorized() throws Exception {
        var sampleProduct = new Product("초콜릿", 1000, "https://image.com/choco.png");
        Product saved = productRepository.save(sampleProduct);
        var sampleWishRequestDto = new WishRequestDto(saved.getId(), 2);
        wishService.addWishItemForMember(member, sampleWishRequestDto);

        mockMvc.perform(delete("/api/wishes/products/{productId}", saved.getId()))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("Authorization 헤더가 필요합니다."));
    }

    @Test
    @DisplayName("DELETE /api/wishes – 잘못된 프리픽스 ⇒ Unauthorized")
    void delete_badPrefix_unauthorized() throws Exception {
        var sampleProduct = new Product("초콜릿", 1000, "https://image.com/choco.png");
        Product saved = productRepository.save(sampleProduct);
        var sampleWishRequestDto = new WishRequestDto(saved.getId(), 2);
        wishService.addWishItemForMember(member, sampleWishRequestDto);

        mockMvc.perform(delete("/api/wishes/products/{productId}", saved.getId())
                .header("Authorization", "Basic abc123"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("Authorization 헤더 형식이 올바르지 않습니다."));
    }
}
