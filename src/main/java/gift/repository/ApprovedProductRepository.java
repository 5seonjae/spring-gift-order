package gift.repository;

import gift.entity.ApprovedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovedProductRepository extends JpaRepository<ApprovedProduct, Long> {

    // 승인 여부 확인
    boolean existsByName(String name);
}
