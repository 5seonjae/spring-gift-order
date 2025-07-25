package gift.repository;

import gift.entity.WishItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface WishRepository extends JpaRepository<WishItem, Long> {

    @EntityGraph(attributePaths = "product")
    Page<WishItem> findAllByMemberId(Long memberId, Pageable pageable);

    @EntityGraph(attributePaths = "product")
    List<WishItem> findAllByMemberId(Long memberId);

    int deleteByMemberIdAndProductId(Long memberId, Long productId);
}