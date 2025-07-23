package gift;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.dto.api.OptionRequestDto;
import gift.dto.api.OptionSubtractRequestDto;
import gift.entity.Option;
import gift.entity.Product;
import gift.repository.OptionRepository;
import gift.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private ProductRepository productRepository;

    Product product;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        optionRepository.deleteAll();

        product = productRepository.save(new Product("초콜릿", 1000, "http://choco/png"));
    }

    @Test
    @DisplayName("GET /api/products/{id}/options : 옵션 배열 반환")
    void listOptions() throws Exception {
        optionRepository.save(new Option(product, "다크 초콜릿", 10));
        optionRepository.save(new Option(product, "화이트 초콜릿", 8));

        mockMvc.perform(get("/api/products/{productId}/options", product.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("화이트 초콜릿"))
            .andExpect(jsonPath("$[0].quantity").value(8))
            .andExpect(jsonPath("$[1].name").value("다크 초콜릿"))
            .andExpect(jsonPath("$[1].quantity").value(10));
    }

    @Nested
    class CreateOption {

        @Test
        @DisplayName("[API] 옵션 생성 성공 201 Created + Location")
        void createOption_success() throws Exception {
            var dto = new OptionRequestDto("다크 초콜릿", 10);
            mockMvc.perform(post("/api/products/{productId}/options", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                    "Location",
                    org.hamcrest.Matchers.matchesRegex(
                        "/api/products/" + product.getId() + "/options/\\d+")
                ))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.quantity").value(dto.getQuantity()));

            assertThat(optionRepository.findByProductIdAndName(product.getId(),
                "다크 초콜릿")).isPresent();
        }

        @Test
        @DisplayName("[Api] 옵션 생성 실패 - 상품 이름 중복 409 Conflict")
        void createOption_duplicate() throws Exception {
            optionRepository.save(new Option(product, "다크 초콜릿", 10));
            var dto = new OptionRequestDto("다크 초콜릿", 5);

            mockMvc.perform(post("/api/products/{productId}/options", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("이미 존재하는 옵션입니다."));
        }

        /* ───── 유효성 시나리오 ───── */
        @Test
        @DisplayName("[API] 옵션 생성 실패 - 빈 옵션명 400 Bad Request")
        void create_blankName() throws Exception {
            var dto = new OptionRequestDto("", 10);

            mockMvc.perform(post("/api/products/{productId}/options", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("옵션 이름은 필수입니다."));
        }

        @Test
        @DisplayName("[API] 옵션 생성 실패 - 옵션명 50자 초과 400 Bad Request")
        void create_nameTooLong() throws Exception {
            var dto = new OptionRequestDto(
                "123456789_123456789_123456789_123456789_123456789_1",
                10);

            mockMvc.perform(post("/api/products/{productId}/options", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("옵션 이름은 최대 50자까지 가능합니다."));
        }

        @Test
        @DisplayName("[API] 옵션 생성 실패 - 옵션명에 허용되지 않은 특수문자 사용 400 Bad Request")
        void create_invalidChar() throws Exception {
            var dto = new OptionRequestDto(
                "다크 초콜릿!",
                10);

            mockMvc.perform(post("/api/products/{productId}/options", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name")
                    .value("유효한 특수문자 ( '( )', '[ ]', '+', '-', '&', '/', '_' ) 가 아닙니다."));
        }

        @Test
        @DisplayName("[API] 옵션 생성 실패 - 수량 < 1 400 Bad Request")
        void create_quantityZero() throws Exception {
            var dto = new OptionRequestDto(
                "다크 초콜릿",
                -10);

            mockMvc.perform(post("/api/products/{productId}/options", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantity")
                    .value("수량은 1개 이상이어야 합니다."));
        }

        @Test
        @DisplayName("[API] 옵션 생성 실패 - 수량 > 1억 400 Bad Request")
        void create_quantityTooLarge() throws Exception {
            var dto = new OptionRequestDto(
                "다크 초콜릿",
                100_000_001);

            mockMvc.perform(post("/api/products/{productId}/options", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantity")
                    .value("수량은 1억 개 미만이어야 합니다."));
        }

        @Test
        @DisplayName("[API] 옵션 생성 실패 - 값 누락 400 Bad Request")
        void create_missingName() throws Exception {
            String badJson = "{"
                + "\"name\": ,"
                + "\"quantity\": 10"
                + "}";

            mockMvc.perform(post("/api/products/{productId}/options", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                    .value("요청 JSON 형식이 잘못되었습니다."));
        }

        /* ───── JSON 필드 누락 ───── */
        @Test @DisplayName("[API] 옵션 생성 실패 - 수량에 문자를 넣는 경우 400 Bad Request")
        void create_missingQuantity() throws Exception {
            String badJson = "{"
                + "\"name\": \"다크 초콜릿\","
                + "\"quantity\": \"과자\""
                + "}";

            mockMvc.perform(post("/api/products/{productId}/options", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                    .value("'quantity' 필드는 Integer 형식이어야 합니다."));
        }

        /* ───── 리소스 시나리오 ───── */
        @Test @DisplayName("[API] 옵션 생성 실패 - 상품 없음 404 Not Found")
        void create_nonExistingProduct() throws Exception {
            var dto = new OptionRequestDto("다크 초콜릿", 10);

            mockMvc.perform(post("/api/products/{productId}/options", 9999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("상품을 찾을 수 없습니다."));
        }
    }

    @Nested
    class UpdateOption {

        Option option;

        @BeforeEach
        void init() {
            option = optionRepository.save(new Option(product, "다크 초콜릿", 10));
            // duplicate 용
            optionRepository.save(new Option(product, "화이트 초콜릿", 5));
        }

        @Test
        @DisplayName("[API] 옵션 수정 성공 - 이름·수량 수정 → 200 Ok + Body")
        void update_success() throws Exception {
            var dto = new OptionRequestDto("아몬드 초콜릿", 7);

            mockMvc.perform(put(
                    "/api/products/{productId}/options/{optionId}",
                    product.getId(),
                    option.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("아몬드 초콜릿"))
                .andExpect(jsonPath("$.quantity").value(7));

            Option changed = optionRepository.findById(option.getId()).orElseThrow();
            assertThat(changed.getName()).isEqualTo("아몬드 초콜릿");
            assertThat(changed.getQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("[API] 옵션 수정 실패 - 옵션명 중복 → 409 Conflict")
        void update_duplicateName() throws Exception {
            var dto = new OptionRequestDto("화이트 초콜릿", 7);

            mockMvc.perform(put(
                    "/api/products/{productId}/options/{optionId}",
                    product.getId(),
                    option.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                    .value("이미 존재하는 옵션입니다."));
        }

        /* ───── 유효성 시나리오 ───── */
        @Test
        @DisplayName("[API] 옵션 수정 실패 - 빈 옵션명 400 Bad Request")
        void update_blankName() throws Exception {
            var dto = new OptionRequestDto("", 7);

            mockMvc.perform(put(
                    "/api/products/{productId}/options/{optionId}",
                    product.getId(),
                    option.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name")
                    .value("옵션 이름은 필수입니다."));
        }

        @Test
        @DisplayName("[API] 옵션 수정 실패 - 옵션명 50자 초과 400 Bad Request")
        void update_nameTooLong() throws Exception {
            var dto = new OptionRequestDto(
                "123456789_123456789_123456789_123456789_123456789_1",
                7
            );

            mockMvc.perform(put(
                    "/api/products/{productId}/options/{optionId}",
                    product.getId(),
                    option.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name")
                    .value("옵션 이름은 최대 50자까지 가능합니다."));
        }

        @Test
        @DisplayName("[API] 옵션 수정 실패 - 옵션명에 허용되지 않은 특수문자 사용 400 Bad Request")
        void update_invalidChar() throws Exception {
            var dto = new OptionRequestDto(
                "다크 초콜릿!",
                10
            );

            mockMvc.perform(put(
                    "/api/products/{productId}/options/{optionId}",
                    product.getId(),
                    option.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name")
                    .value("유효한 특수문자 ( '( )', '[ ]', '+', '-', '&', '/', '_' ) 가 아닙니다."));
        }

        @Test
        @DisplayName("[API] 옵션 수정 실패 - 수량 < 1 400 Bad Request")
        void update_quantityZero() throws Exception {
            var dto = new OptionRequestDto(
                "다크 초콜릿",
                -10
            );

            mockMvc.perform(put(
                    "/api/products/{productId}/options/{optionId}",
                    product.getId(),
                    option.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantity")
                    .value("수량은 1개 이상이어야 합니다."));
        }

        @Test
        @DisplayName("[API] 옵션 수정 실패 - 수량 > 1억 400 Bad Request")
        void update_quantityTooLarge() throws Exception {
            var dto = new OptionRequestDto(
                "다크 초콜릿",
                100_000_001
            );

            mockMvc.perform(put(
                    "/api/products/{productId}/options/{optionId}",
                    product.getId(),
                    option.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantity")
                    .value("수량은 1억 개 미만이어야 합니다."));
        }

        /* ───── JSON 필드 누락 ───── */
        @Test @DisplayName("[API] 옵션 수정 실패 - 값 누락 400 Bad Request")
        void update_missingName() throws Exception {
            String badJson = "{"
                + "\"name\": ,"
                + "\"quantity\": 10"
                + "}";

            mockMvc.perform(put(
                    "/api/products/{productId}/options/{optionId}",
                    product.getId(),
                    option.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                    .value("요청 JSON 형식이 잘못되었습니다."));
        }

        @Test @DisplayName("[API] 옵션 수정 실패 - 수량에 문자를 넣는 경우 400 Bad Request")
        void update_missingQuantity() throws Exception {
            String badJson = "{"
                + "\"name\": \"아몬드 초콜릿\","
                + "\"quantity\": \"과자\","
                + "}";

            mockMvc.perform(put(
                    "/api/products/{productId}/options/{optionId}",
                    product.getId(),
                    option.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                    .value("'quantity' 필드는 Integer 형식이어야 합니다."));
        }

        /* ───── 리소스 시나리오 ───── */
        @Test @DisplayName("[API] 옵션 수정 실패 - 상품 없음 404 Not Found")
        void update_productNotFound() throws Exception {
            var dto = new OptionRequestDto("아몬드 초콜릿", 7);
            mockMvc.perform(put(
                    "/api/products/{productId}/options/{optionId}",
                    999L,
                    option.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                    .value("상품을 찾을 수 없습니다."));
        }

        @Test @DisplayName("[API] 옵션 수정 실패 - 옵션 없음 404 Not Found")
        void update_optionNotFound() throws Exception {
            var dto = new OptionRequestDto("아몬드 초콜릿", 7);
            mockMvc.perform(put(
                    "/api/products/{productId}/options/{optionId}",
                    product.getId(),
                    999L
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                    .value("옵션을 찾을 수 없습니다."));
        }

        @Test @DisplayName("[API] 옵션 수정 실패 - 상품 옵션 불일치 409 Conflict")
        void update_productMismatch() throws Exception {
            Product otherProduct = productRepository.save(
                new Product("김밥", 3000, "http://kimbab.png")
            );
            Option otherOption = optionRepository.save(
                new Option(otherProduct, "꼬마 김밥", 4000)
            );
            var dto = new OptionRequestDto("Z", 5);
            mockMvc.perform(put(
                    "/api/products/{productId}/options/{optionId}",
                    product.getId(),
                    otherOption.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                    .value(otherOption.getName() + " 옵션은 " + product.getName() + " 상품에 속하지 않습니다."));
        }
    }

    @Nested
    class SubtractQuantity {
        Option option;

        @BeforeEach
        void initOption() {
            option = optionRepository.save(new Option(product, "다크 초콜릿", 10));
        }

        @Test
        @DisplayName("[API] 옵션 수량 감소 성공 - 204 No Content + 수량 감소")
        void subtract_success() throws Exception {
            var dto = new OptionSubtractRequestDto(3);

            mockMvc.perform(patch(
                    "/api/products/{productId}/options/{optionId}/subtract",
                    product.getId(),
                    option.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isNoContent());

            assertThat(
                optionRepository
                    .findById(option.getId())
                    .orElseThrow().getQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("[API] 옵션 수량 감소 실패 - 수량 부족 409 Conflict")
        void subtract_insufficient() throws Exception {
            var dto = new OptionRequestDto("다크 초콜릿", 999);

            mockMvc.perform(patch(
                    "/api/products/{productId}/options/{optionId}/subtract",
                    product.getId(),
                    option.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                    .value("재고가 부족합니다."));
        }

        @Test
        @DisplayName("[API] 옵션 수량 감소 실패 - 수량 < 1 400 Bad Request")
        void subtract_quantityZero() throws Exception {
            var dto = new OptionRequestDto(
                "다크 초콜릿",
                -10
            );

            mockMvc.perform(patch(
                    "/api/products/{productId}/options/{optionId}/subtract",
                    product.getId(),
                    option.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantity")
                    .value("수량은 1개 이상이어야 합니다."));
        }

        @Test
        @DisplayName("[API] 옵션 수량 감소 실패 - 수량 > 1억 400 Bad Request")
        void subtract_quantityTooLarge() throws Exception {
            var dto = new OptionRequestDto(
                "다크 초콜릿",
                100_000_001
            );

            mockMvc.perform(patch(
                    "/api/products/{productId}/options/{optionId}/subtract",
                    product.getId(),
                    option.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.quantity")
                    .value("수량은 1억 개 미만이어야 합니다."));
        }

        @Test @DisplayName("[API] 옵션 수량 감소 실패 - 수량에 문자를 넣는 경우 400 Bad Request")
        void subtract_missingQuantity() throws Exception {
            String badJson = "{"
                + "\"quantity\": \"과자\","
                + "}";

            mockMvc.perform(patch(
                    "/api/products/{productId}/options/{optionId}/subtract",
                    product.getId(),
                    option.getId()
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                    .value("'quantity' 필드는 Integer 형식이어야 합니다."));
        }
    }

    @Nested
    class DeleteOption {
        @Test
        @DisplayName("[API] 옵션 제거 성공 - 204 No Content + removed")
        void delete_success() throws Exception {
            Option option = optionRepository.save(new Option(product, "다크 초콜릿", 10));

            mockMvc.perform(delete(
                    "/api/products/{productId}/options/{optionId}",
                    product.getId(),
                    option.getId())
                )
                .andExpect(status().isNoContent());

            assertThat(optionRepository.findById(option.getId())).isNotPresent();
        }

        @Test
        @DisplayName("[API] 옵션 제거 실패 - 존재하지 않는 옵션 404 Not Found")
        void delete_notFound() throws Exception {
            mockMvc.perform(delete(
                    "/api/products/{productId}/options/{optionId}",
                    product.getId(),
                    999L)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("옵션을 찾을 수 없습니다."));
        }
    }
}
