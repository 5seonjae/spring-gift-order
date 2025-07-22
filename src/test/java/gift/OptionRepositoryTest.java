package gift;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import gift.entity.Option;
import gift.entity.Product;
import gift.repository.OptionRepository;
import gift.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@DataJpaTest
public class OptionRepositoryTest {

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private ProductRepository productRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        optionRepository.deleteAll();

        product = new Product("초콜릿", 1000, "http://choco.png");
        productRepository.save(product);
    }

    @ParameterizedTest(name = "[page={0}] optionName={1}, optionQuantity={2}, first={3}, prev={4}, next={5}")
    @CsvSource({
        // page, optionName, optionQuantity, first, prev, next
        "0, 아몬드 초콜릿, 4, true, false, true",
        "1, 화이트 초콜릿, 3, false, true, true",
        "2, 다크 초콜릿, 2, false, true, false"
    })
    @DisplayName("상품 옵션 페이지네이션 조회")
    void findAllByProductId_shouldReturnPagedOptions(
        int pageIndex,
        String expectedOptionName,
        int expectedOptionQty,
        boolean expectedFirst,
        boolean expectedPrev,
        boolean expectedNext
    ) {
        // given ─ 옵션 3개 저장
        optionRepository.saveAll(List.of(
            new Option(product, "다크 초콜릿", 2),
            new Option(product, "화이트 초콜릿", 3),
            new Option(product, "아몬드 초콜릿", 4)
        ));

        // when ─ pageSize=1, id desc
        Pageable pageable = PageRequest.of(pageIndex, 1, Sort.by("id").descending());
        Page<Option> page = optionRepository.findAllByProductId(product.getId(), pageable);

        // then ─ 메타데이터
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getNumber()).isEqualTo(pageIndex);
        assertThat(page.getSize()).isEqualTo(1);
        assertThat(page.isFirst()).isEqualTo(expectedFirst);
        assertThat(page.hasPrevious()).isEqualTo(expectedPrev);
        assertThat(page.hasNext()).isEqualTo(expectedNext);

        // then ─ 콘텐츠 검증
        assertThat(page.getContent())
            .hasSize(1)
            .extracting(Option::getName, Option::getQuantity)
            .containsExactly(tuple(expectedOptionName, expectedOptionQty));
    }

    @Test
    @DisplayName("옵션이 없으면 빈 페이지를 반환")
    void returnsEmptyPageWhenNotFound() {
        Pageable pageable = PageRequest.of(0, 5);

        Page<Option> page = optionRepository.findAllByProductId(-999L, pageable);

        assertThat(page).isEmpty();
        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getTotalPages()).isZero();
    }

    @Test
    @DisplayName("상품 ID + 옵션명으로 옵션을 조회")
    void returnsSingleOption() {
        optionRepository.saveAll(List.of(
            new Option(product, "다크 초콜릿", 2),
            new Option(product, "화이트 초콜릿", 3),
            new Option(product, "아몬드 초콜릿", 4)
        ));

        Optional<Option> option =
            optionRepository.findByProductIdAndName(product.getId(), "화이트 초콜릿");

        assertThat(option)
            .isPresent()
            .get()
            .satisfies(o -> assertThat(o.getQuantity()).isEqualTo(3));
    }

    @Test
    @DisplayName("해당 조합이 없으면 Optional.empty()를 반환")
    void returnsEmptyOptionalWhenNotExists() {
        Optional<Option> option =
            optionRepository.findByProductIdAndName(product.getId(), "두바이 초콜릿");

        assertThat(option).isNotPresent();
    }
}
