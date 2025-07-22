package gift.repository;

import gift.entity.WishItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface WishRepository extends JpaRepository<WishItem, Long> {

    // 페이징용 메서드
    @EntityGraph(attributePaths = "product")
    Page<WishItem> findAllByMemberId(Long memberId, Pageable pageable);

    // 리스트 조회용 메서드
    @EntityGraph(attributePaths = "product")
    List<WishItem> findAllByMemberId(Long memberId);

    int deleteByMemberIdAndProductId(Long memberId, Long productId);
}