package gift;

import gift.entity.Product;
import gift.repository.ProductRepository;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("save & findById: 저장된 상품을 조회할 수 있다")
    void save_and_findById_shouldWork() {
        // given
        Product p = new Product("Chocolate", 1000, "http://example.com/img.png");
        productRepository.save(p);

        // when
        Product found = productRepository.findById(p.getId()).orElseThrow();

        // then
        assertThat(found.getName()).isEqualTo("Chocolate");
        assertThat(found.getPrice()).isEqualTo(1000);
        assertThat(found.getImageUrl()).isEqualTo("http://example.com/img.png");
    }

    @ParameterizedTest(name = "[page={0}] name={1}, price={2}, url={3}")
    @CsvSource({
        // page, name, price, imageUrl, isFirst, hasPrevious, hasNext
        "0, C, 3, http://exampleC.com/img.png, true,  false, true",
        "1, B, 2, http://exampleB.com/img.png, false, true,  true",
        "2, A, 1, http://exampleA.com/img.png, false, true,  false"
    })
    @DisplayName("findAll: 저장된 상품 리스트를 모두 반환")
    void findAll_shouldReturnAllSaved(int pageIndex,
        String expectedName,
        int expectedPrice,
        String expectedImageUrl,
        boolean expectedFirst,
        boolean expectedPrev,
        boolean expectedNext
    ) {
        // given — 기존 데이터 삭제 후 3개 상품 저장
        productRepository.deleteAll();
        productRepository.saveAll(List.of(
                new Product("A", 1, "http://exampleA.com/img.png"),
                new Product("B", 2, "http://exampleB.com/img.png"),
                new Product("C", 3, "http://exampleC.com/img.png")
        ));

        // when — 페이지 크기 1, id 내림차순 정렬
        Pageable pageable = PageRequest.of(
            pageIndex,                             // page number
            1,                                        // page size
            Sort.by("id").descending()      // sort by id desc
        );

        Page<Product> page = productRepository.findAll(pageable);

        // then — 메타데이터 검증 (공통값)
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getNumber()).isEqualTo(pageIndex);
        assertThat(page.getSize()).isEqualTo(1);

        // then — 콘텐츠 검증
        assertThat(page.getContent())
            .extracting(Product::getName)
            .containsExactly(expectedName);
        assertThat(page.getContent())
            .extracting(Product::getPrice)
            .containsExactly(expectedPrice);
        assertThat(page.getContent())
            .extracting(Product::getImageUrl)
            .containsExactly(expectedImageUrl);

        // then — 네비게이션 플래그 검증
        assertThat(page.isFirst()).isEqualTo(expectedFirst);
        assertThat(page.hasPrevious()).isEqualTo(expectedPrev);
        assertThat(page.hasNext()).isEqualTo(expectedNext);
    }
}
