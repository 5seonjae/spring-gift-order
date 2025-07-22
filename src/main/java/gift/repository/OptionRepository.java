package gift.repository;

import gift.entity.Option;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionRepository extends JpaRepository<Option, Long> {

    @EntityGraph(attributePaths = "product")
    Page<Option> findAllByProductId(Long productId, Pageable pageable);

    @EntityGraph(attributePaths = "product")
    Optional<Option> findByProductIdAndName(Long productId, String optionName);
}
