package gift;

import gift.auth.LoginMember;
import gift.auth.LoginMemberArgumentResolver;
import gift.dto.api.WishRequestDto;
import gift.entity.Member;
import gift.entity.Product;
import gift.exception.InvalidAuthorizationHeaderException;
import gift.exception.MissingAuthorizationHeaderException;
import gift.repository.MemberRepository;
import gift.repository.ProductRepository;
import gift.repository.WishRepository;
import gift.service.WishService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.NativeWebRequest;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class WishViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WishRepository wishRepository;

    @Autowired
    private WishService wishService;

    @MockitoBean
    private LoginMemberArgumentResolver loginMemberArgumentResolver;

    Member member;        // 매 테스트에서 사용할 로그인 회원

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
    @DisplayName("POST /wishes/{id} – 정상 추가 ⇒ Found")
    void addWish_success_redirect() throws Exception {
        // given 상품 1개 저장
        Product saved = productRepository.save(
            new Product("초콜릿", 1000, "https://image.com/choco.png"));

        // when & then
        mockMvc.perform(post("/wishes/{id}", saved.getId())
                .header("Authorization", "Bearer dummy-token"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/products"));
    }

    @Test
    @DisplayName("POST /wishes/{id} – 헤더 누락 ⇒ Unauthorized")
    void addWish_withoutHeader_unauthorized() throws Exception {
        Product saved = productRepository.save(
            new Product("사탕", 500, "https://image.com/candy.png"));

        mockMvc.perform(post("/wishes/{id}", saved.getId()))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("Authorization 헤더가 필요합니다."));
    }

    @Test
    @DisplayName("POST /wishes/{id} – 잘못된 프리픽스 ⇒ Unauthorized")
    void addWish_badPrefix_unauthorized() throws Exception {
        Product saved = productRepository.save(
            new Product("쿠키", 800, "https://image.com/cookie.png"));

        mockMvc.perform(post("/wishes/{id}", saved.getId())
                .header("Authorization", "Basic abc123"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("Authorization 헤더 형식이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("POST /wishes/{id} – 없는 상품 ⇒ Not Found")
    void addWish_nonexistentProduct_notFound() throws Exception {
        mockMvc.perform(post("/wishes/{id}", 999L)
                .header("Authorization", "Bearer dummy-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                .value("상품을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("GET /wishes – 아이템 1건 ⇒ 200 OK + 상품명 표시")
    void list_withItem_ok() throws Exception {
        Product p = productRepository.save(
            new Product("초콜릿", 1000, "https://image.com/choco.png"));
        wishService.addWishItemForMember(member, new WishRequestDto(p.getId(), 1));

        mockMvc.perform(get("/wishes")
                        .header("Authorization", "Bearer dummy")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "id,desc")
                        .accept(MediaType.TEXT_HTML)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("wishes/list"))
                .andExpect(model().attributeExists("wishPage"))
                .andExpect(model().attribute("wishPage",
                        hasProperty("content", hasSize(1))
                ))
                .andExpect(model().attribute("wishPage",
                        hasProperty("content", contains(
                                hasProperty("name", is("초콜릿"))
                        ))
                ))
                // price
                .andExpect(model().attribute("wishPage",
                        hasProperty("content", contains(
                                hasProperty("price", is(1000))
                        ))
                ))
                // imageUrl
                .andExpect(model().attribute("wishPage",
                        hasProperty("content", contains(
                                hasProperty("imageUrl", is("https://image.com/choco.png"))
                        ))
                ))
                // quantity
                .andExpect(model().attribute("wishPage",
                        hasProperty("content", contains(
                                hasProperty("quantity", is(1))
                        ))
                ));
    }

    @Test
    @DisplayName("GET /wishes – 2 건 ⇒ 200 OK + 모델 검증")
    void list_withTwoItems_modelCheck() throws Exception {
        // given: 상품 2개 + wish_items 2건
        Product p1 = productRepository.save(
            new Product("초콜릿", 1000, "https://image.com/choco.png"));
        Product p2 = productRepository.save(
            new Product("캔디", 500, "https://image.com/candy.png"));

        wishService.addWishItemForMember(member, new WishRequestDto(p1.getId(), 1));
        wishService.addWishItemForMember(member, new WishRequestDto(p2.getId(), 2));

        // when & then
        mockMvc.perform(get("/wishes")
                .header("Authorization", "Bearer dummy")
                .param("page", "0")
                .param("size", "5")
                .param("sort", "id,desc")
                .accept(MediaType.TEXT_HTML)
            )
            .andExpect(status().isOk())
            .andExpect(view().name("wishes/list"))
                .andExpect(model().attributeExists("wishPage"))
                .andExpect(model().attribute("wishPage",
                        hasProperty("content", hasSize(2))
                ))
                .andExpect(model().attribute("wishPage",
                        hasProperty("content", contains(
                                hasProperty("name", is("캔디")),
                                hasProperty("name", is("초콜릿"))
                        ))
                ))
                // price
                .andExpect(model().attribute("wishPage",
                        hasProperty("content", contains(
                                hasProperty("price", is(500)),
                                hasProperty("price", is(1000))
                        ))
                ))
                // imageUrl
                .andExpect(model().attribute("wishPage",
                        hasProperty("content", contains(
                                hasProperty("imageUrl", is("https://image.com/candy.png")),
                                hasProperty("imageUrl", is("https://image.com/choco.png"))
                        ))
                ))
                // quantity
                .andExpect(model().attribute("wishPage",
                        hasProperty("content", contains(
                                hasProperty("quantity", is(2)),
                                hasProperty("quantity", is(1))
                        ))
                ));
    }

    @Test
    @DisplayName("GET /wishes – 헤더 누락 ⇒ Unauthorized")
    void list_withoutHeader_unauthorized() throws Exception {
        mockMvc.perform(get("/wishes"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("Authorization 헤더가 필요합니다."));
    }

    @Test
    @DisplayName("POST /wishes/{id}/delete – 정상 ⇒ Found")
    void delete_ok_redirect() throws Exception {
        Product p = productRepository.save(
            new Product("사탕", 500, "https://image.com/candy.png"));
        wishService.addWishItemForMember(member, new WishRequestDto(p.getId(), 1));

        mockMvc.perform(post("/wishes/{productId}/delete", p.getId())
                .header("Authorization", "Bearer dummy"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/wishes"));
    }

    @Test
    @DisplayName("POST /wishes/{id}/delete – Basic 프리픽스 ⇒ Unauthorized")
    void delete_badPrefix_unauthorized() throws Exception {
        Product p = productRepository.save(
            new Product("초코파이", 1200, "https://image.com/chocoPie.png"));

        mockMvc.perform(post("/wishes/{id}/delete", p.getId())
                .header("Authorization", "Basic abc"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("Authorization 헤더 형식이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("POST /wishes/{id}/delete – 없는 항목 ⇒ Not Found")
    void delete_notFound() throws Exception {
        Product p = productRepository.save(
            new Product("젤리", 700, "https://image.com/jelly.png"));

        // wishRepository 에는 넣지 않아 존재하지 않는 상태
        mockMvc.perform(post("/wishes/{productId}/delete", p.getId())
                .header("Authorization", "Bearer dummy"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                .value("위시 목록에 없는 상품입니다."));
        ;
    }
}
