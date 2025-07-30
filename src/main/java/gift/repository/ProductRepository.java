package gift.repository;

import gift.entity.Product;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "options")
    Optional<Product> findById(Long id);
}
