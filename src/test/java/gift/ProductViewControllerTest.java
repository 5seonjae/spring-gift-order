package gift;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import gift.entity.Product;
import gift.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("[VIEW] 상품 목록 조회 - GET /products")
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

        mockMvc.perform(get("/products/{id}", id))
            .andExpect(status().isOk())
            .andExpect(view().name("products/user/detail"))
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
        mockMvc.perform(get("/products/{id}", 9999L))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/products"))
            .andExpect(flash().attribute("errorMsg", "상품을 찾을 수 없습니다."));
    }
}
