package gift;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.contains;

import gift.dto.view.ProductViewRequestDto;
import gift.entity.ApprovedProduct;
import gift.entity.Product;
import gift.repository.ApprovedProductRepository;
import gift.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class AdminProductViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApprovedProductRepository approvedProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("[VIEW] 상품 등록 폼 진입 - GET /admin/products/new")
    void showCreateForm() throws Exception {
        mockMvc.perform(get("/admin/products/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeExists("productRequest"))
            .andExpect(
                model().attribute("productRequest", instanceOf(ProductViewRequestDto.class)));
    }

    @Test
    @DisplayName("[Form] 상품 등록 성공 - 일반 상품명")
    void createProduct_success_normalName() throws Exception {
        mockMvc.perform(post("/admin/products/new")
                .param("name", "초콜릿")
                .param("price", "1000")
                .param("imageUrl", "https://image.com/item.jpg")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/admin/products"));
    }

    @Test
    @DisplayName("[Form] 상품 등록 성공 - '카카오' 포함된 승인된 상품명")
    void createProduct_success_withApprovedName() throws Exception {

        // '카카오' 포함된 승인된 상품명 추가
        approvedProductRepository.save(new ApprovedProduct("카카오 프렌즈 볼펜"));

        // when & then
        mockMvc.perform(post("/admin/products/new")
                .param("name", "카카오 프렌즈 볼펜")
                .param("price", "15000")
                .param("imageUrl", "https://image.com/item.jpg")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/admin/products"));
    }

    @Test
    @DisplayName("[Form] 상품 등록 실패 - '카카오' 포함 & 승인되지 않은 상품명")
    void createProduct_fail_unapprovedKakaoName() throws Exception {
        mockMvc.perform(post("/admin/products/new")
                .param("name", "카카오 커피잔")
                .param("price", "12000")
                .param("imageUrl", "https://image.com/item.jpg")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("[Form] 상품 등록 실패 - 상품명 없음")
    void createProduct_fail_blankName() throws Exception {
        mockMvc.perform(post("/admin/products/new")
                .param("name", "")
                .param("price", "1000")
                .param("imageUrl", "https://image.com/item.jpg")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeHasFieldErrors("productRequest", "name"));
    }

    @Test
    @DisplayName("[Form] 상품 등록 실패 - 상품명 15자 초과")
    void createProduct_fail_nameTooLong() throws Exception {
        mockMvc.perform(post("/admin/products/new")
                .param("name", "1234567890123456") // 16자
                .param("price", "1000")
                .param("imageUrl", "https://image.com/item.jpg")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeHasFieldErrors("productRequest", "name"));
    }

    @Test
    @DisplayName("[Form] 상품 등록 실패 - 상품명에 허용되지 않은 문자 사용")
    void createProduct_fail_invalidNameCharacters() throws Exception {
        mockMvc.perform(post("/admin/products/new")
                .param("name", "초콜릿%")      // 허용되지 않은 문자 '%' 사용
                .param("price", "1000")
                .param("imageUrl", "https://image.com/item.jpg")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeHasFieldErrors("productRequest", "name"));
    }

    @Test
    @DisplayName("[Form] 상품 등록 실패 - 가격 음수")
    void createProduct_fail_negativePrice() throws Exception {
        mockMvc.perform(post("/admin/products/new")
                .param("name", "허용된 상품")
                .param("price", "-500")
                .param("imageUrl", "https://image.com/item.jpg")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeHasFieldErrors("productRequest", "price"));
    }

    @Test
    @DisplayName("[Form] 상품 등록 실패 - 가격 없음")
    void createProduct_fail_priceMissing() throws Exception {
        mockMvc.perform(post("/admin/products/new")
                .param("name", "허용된 상품")
                .param("price", "")
                .param("imageUrl", "https://image.com/item.jpg")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeHasFieldErrors("productRequest", "price"));
    }

    @Test
    @DisplayName("[Form] 상품 등록 실패 - 유효하지 않은 이미지 URL")
    void createProduct_fail_invalidImageUrl() throws Exception {
        mockMvc.perform(post("/admin/products/new")
                .param("name", "정상 상품명")
                .param("price", "1000")
                .param("imageUrl", "invalid-url") // http/https 아님
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeHasFieldErrors("productRequest", "imageUrl"));
    }

    @Test
    @DisplayName("[Form] 상품 등록 실패 - 이미지 URL 없음")
    void createProduct_fail_imageUrlMissing() throws Exception {
        mockMvc.perform(post("/admin/products/new")
                .param("name", "정상 상품명")
                .param("price", "1000")
                .param("imageUrl", "")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeHasFieldErrors("productRequest", "imageUrl"));
    }

    @Test
    @DisplayName("[VIEW] 상품 목록 조회 - GET /admin/products")
    void listProducts() throws Exception {
        productRepository.save(new Product("초콜릿", 1000, "https://image.com/choco.jpg"));
        productRepository.save(new Product("캔디", 500, "https://image.com/candy.jpg"));

        // 수행 & 검증
        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/admin/list"))
                .andExpect(model().attributeExists("page"))
                .andExpect(model().attribute("page",
                        hasProperty("content", hasSize(2))))
                .andExpect(model().attribute("page",
                        hasProperty("content", contains(
                                hasProperty("name", is("캔디")),
                                hasProperty("name", is("초콜릿"))
                        ))
                ))
                .andExpect(model().attribute("page",
                        hasProperty("content", contains(
                                hasProperty("price", is(500)),
                                hasProperty("price", is(1000))
                        ))
                ))
                .andExpect(model().attribute("page",
                        hasProperty("content", contains(
                                hasProperty("imageUrl", is("https://image.com/candy.jpg")),
                                hasProperty("imageUrl", is("https://image.com/choco.jpg"))
                        ))
                ))
        ;
    }

    @Test
    @DisplayName("[VIEW] 상품 상세 조회 - GET /admin/products/{id}")
    void showProductDetail_success() throws Exception {
        Product saved = productRepository.save(
            new Product("초콜릿", 1000, "https://image.com/choco.jpg")
        );
        Long id = saved.getId();

        mockMvc.perform(get("/admin/products/{id}", id))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/detail"))
            .andExpect(model().attributeExists("product"))
            .andExpect(model().attribute("product",
                hasProperty("id", is(id))
            ))
            .andExpect(model().attribute("product",
                hasProperty("name", is("초콜릿"))
            ))
            .andExpect(model().attribute("product",
                hasProperty("price", is(1000))
            ))
            .andExpect(model().attribute("product",
                hasProperty("imageUrl", is("https://image.com/choco.jpg"))
            ));
    }

    @Test
    @DisplayName("[VIEW] 상품 상세 조회 실패 - 리다이렉트+플래시")
    void showProductDetail_notFoundRedirect() throws Exception {
        mockMvc.perform(get("/admin/products/{id}", 9999L))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/admin/products"))
            .andExpect(flash().attribute("errorMsg", "상품을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("[VIEW] 상품 수정 폼 진입 - GET /admin/products/{id}/edit")
    void showUpdateForm() throws Exception {
        productRepository.deleteAll();
        Product saved = productRepository.save(
            new Product("초콜릿", 1000, "https://image.com/choco.jpg")
        );
        Long id = saved.getId();

        mockMvc.perform(get("/admin/products/{id}/edit", id))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeExists("productRequest"))
            .andExpect(model().attribute("productRequest",
                hasProperty("name", is("초콜릿"))
            ))
            .andExpect(model().attribute("productRequest",
                hasProperty("price", is(1000))
            ))
            .andExpect(model().attribute("productRequest",
                hasProperty("imageUrl", is("https://image.com/choco.jpg"))
            ));
    }

    @Test
    @DisplayName("[Form] 상품 수정 성공 - 유효한 데이터")
    void updateProduct_success() throws Exception {
        Product saved = productRepository.save(
            new Product("초콜릿", 1000, "https://image.com/choco.jpg")
        );
        Long id = saved.getId();

        mockMvc.perform(post("/admin/products/{id}/edit", id)
                .param("name", "다크 초콜릿")
                .param("price", "1500")
                .param("imageUrl", "https://image.com/dark.jpg")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/admin/products"));
    }

    @Test
    @DisplayName("[Form] 상품 수정 성공 - '카카오' 포함된 승인된 상품명")
    void updateProduct_success_withApprovedName() throws Exception {
        // '카카오' 포함된 승인된 상품명 추가
        approvedProductRepository.save(new ApprovedProduct("카카오 프렌즈 볼펜"));

        Product saved = productRepository.save(
            new Product("초콜릿", 1000, "https://image.com/choco.jpg")
        );
        Long id = saved.getId();

        mockMvc.perform(post("/admin/products/{id}/edit", id)
                .param("name", "카카오 프렌즈 볼펜")
                .param("price", "15000")
                .param("imageUrl", "https://image.com/kakaoPen.jpg")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/admin/products"));
    }

    @Test
    @DisplayName("[Form] 상품 수정 실패 - '카카오' 포함 & 승인되지 않은 상품명")
    void updateProduct_fail_unapprovedKakaoName() throws Exception {
        Product saved = productRepository.save(
            new Product("초콜릿", 1000, "https://image.com/choco.jpg")
        );
        Long id = saved.getId();

        mockMvc.perform(post("/admin/products/{id}/edit", id)
                .param("name", "카카오 지갑")
                .param("price", "15000")
                .param("imageUrl", "https://image.com/kakaoWallet.jpg")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("[Form] 상품 수정 실패 - 상품명 없음")
    void updateProduct_fail_blankName() throws Exception {
        Product saved = productRepository.save(
            new Product("초콜릿", 1000, "https://image.com/choco.jpg")
        );
        Long id = saved.getId();

        mockMvc.perform(post("/admin/products/{id}/edit", id)
                .param("name", "")
                .param("price", "1500")
                .param("imageUrl", "https://image.com/item.jpg")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeHasFieldErrors("productRequest", "name"));
    }

    @Test
    @DisplayName("[Form] 상품 수정 실패 - 상품명 15자 초과")
    void updateProduct_fail_nameTooLong() throws Exception {
        Product saved = productRepository.save(
            new Product("초콜릿", 1000, "https://image.com/choco.jpg")
        );
        Long id = saved.getId();

        mockMvc.perform(post("/admin/products/{id}/edit", id)
                .param("name", "1234567890123456")      // 16자
                .param("price", "1500")
                .param("imageUrl", "https://image.com/item.jpg")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeHasFieldErrors("productRequest", "name"));
    }

    @Test
    @DisplayName("[Form] 상품 수정 실패 - 상품명에 허용되지 않은 문자 사용")
    void updateProduct_fail_invalidNameCharacters() throws Exception {
        Product saved = productRepository.save(
            new Product("초콜릿", 1000, "https://image.com/choco.jpg")
        );
        Long id = saved.getId();

        mockMvc.perform(post("/admin/products/{id}/edit", id)
                .param("name", "초콜릿%")      // 허용되지 않은 문자 '%' 사용
                .param("price", "1500")
                .param("imageUrl", "https://image.com/item.jpg")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeHasFieldErrors("productRequest", "name"));
    }

    @Test
    @DisplayName("[Form] 상품 수정 실패 - 가격 없음")
    void updateProduct_fail_priceMissing() throws Exception {
        Product saved = productRepository.save(
            new Product("초콜릿", 1000, "https://image.com/choco.jpg")
        );
        Long id = saved.getId();

        mockMvc.perform(post("/admin/products/{id}/edit", id)
                .param("name", "다크 초콜릿")
                .param("price", "")
                .param("imageUrl", "https://image.com/item.jpg")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeHasFieldErrors("productRequest", "price"));
    }

    @Test
    @DisplayName("[Form] 상품 수정 실패 - 가격 음수")
    void updateProduct_fail_negativePrice() throws Exception {
        Product saved = productRepository.save(
            new Product("초콜릿", 1000, "https://image.com/choco.jpg")
        );
        Long id = saved.getId();

        mockMvc.perform(post("/admin/products/{id}/edit", id)
                .param("name", "다크 초콜릿")
                .param("price", "-1000")
                .param("imageUrl", "https://image.com/item.jpg")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeHasFieldErrors("productRequest", "price"));
    }

    @Test
    @DisplayName("[Form] 상품 수정 실패 - 이미지 URL 없음")
    void updateProduct_fail_imageUrlMissing() throws Exception {
        Product saved = productRepository.save(
            new Product("초콜릿", 1000, "https://image.com/choco.jpg")
        );
        Long id = saved.getId();

        mockMvc.perform(post("/admin/products/{id}/edit", id)
                .param("name", "다크 초콜릿")
                .param("price", "1000")
                .param("imageUrl", "")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeHasFieldErrors("productRequest", "imageUrl"));
    }

    @Test
    @DisplayName("[Form] 상품 수정 실패 - 유효하지 않은 이미지 URL")
    void updateProduct_fail_invalidImageUrl() throws Exception {
        Product saved = productRepository.save(
            new Product("초콜릿", 1000, "https://image.com/choco.jpg")
        );
        Long id = saved.getId();

        mockMvc.perform(post("/admin/products/{id}/edit", id)
                .param("name", "다크 초콜릿")
                .param("price", "1000")
                .param("imageUrl", "image.com/item.jpg")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("products/admin/form"))
            .andExpect(model().attributeHasFieldErrors("productRequest", "imageUrl"));
    }

    @Test
    @DisplayName("[Form] 상품 삭제 처리 - 성공")
    void deleteProduct_success() throws Exception {
        Product saved = productRepository.save(
            new Product("초콜릿", 1000, "https://image.com/choco.jpg")
        );
        Long id = saved.getId();

        mockMvc.perform(post("/admin/products/{id}/delete", id)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/admin/products"));
    }
}
