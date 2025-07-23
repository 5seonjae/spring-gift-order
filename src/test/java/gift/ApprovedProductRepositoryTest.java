package gift;

import gift.entity.ApprovedProduct;
import gift.repository.ApprovedProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ApprovedProductRepositoryTest {

    @Autowired
    private ApprovedProductRepository approvedProductRepository;

    @Test
    @DisplayName("existsByName: 저장된 이름에 대해 true 반환")
    void existsByName_shouldReturnTrue_whenNameExists() {
        // given
        ApprovedProduct product = new ApprovedProduct("TestProduct");
        approvedProductRepository.save(product);

        // when
        boolean exists = approvedProductRepository.existsByName("TestProduct");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByName: 저장되지 않은 이름에 대해 false 반환")
    void existsByName_shouldReturnFalse_whenNameNotExists() {
        boolean exists = approvedProductRepository.existsByName("NoSuchProduct");
        assertThat(exists).isFalse();
    }
}
