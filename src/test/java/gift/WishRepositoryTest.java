package gift;

import gift.entity.Member;
import gift.entity.Product;
import gift.entity.WishItem;
import gift.repository.MemberRepository;
import gift.repository.ProductRepository;
import gift.repository.WishRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class WishRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WishRepository wishRepository;

    @Test
    @DisplayName("findAllByMemberId: 해당 회원의 찜 목록 조회")
    void findAllByMemberId_shouldReturnItems_forMember() {
        // given
        Member m = memberRepository.save(new Member("u1@example.com", "password"));
        Product p = productRepository.save(new Product("P1", 1000, "http://example.com/img.png"));
        WishItem wi = new WishItem(m, p, 3);
        wishRepository.save(wi);

        // when — 페이지 크기 5, id 내림차순, 0번 페이지 조회
        Pageable pageable = PageRequest.of(0, 5, Sort.by("id").descending());
        Page<WishItem> page = wishRepository.findAllByMemberId(m.getId(), pageable);

        // then — 페이징 메타데이터 검증
        assertThat(page.getTotalElements()).isEqualTo(1);    // 전체 아이템 수
        assertThat(page.getTotalPages()).isEqualTo(1);       // 전체 페이지 수
        assertThat(page.getNumber()).isZero();                       // 현재 페이지 인덱스
        assertThat(page.getSize()).isEqualTo(5);             // 페이지 크기
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();

        // then — 콘텐츠 검증
        List<WishItem> content = page.getContent();
        assertThat(content).hasSize(1);

        WishItem item = content.get(0);
        assertThat(item.getProduct().getName()).isEqualTo("P1");
        assertThat(item.getProduct().getPrice()).isEqualTo(1000);
        assertThat(item.getProduct().getImageUrl()).isEqualTo("http://example.com/img.png");
        assertThat(item.getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("deleteByMemberIdAndProductId: 해당 항목 삭제 후 영향 개수 반환")
    void deleteByMemberIdAndProductId_shouldDeleteAndReturnCount() {
        // given
        Member m = memberRepository.save(new Member("u2@example.com", "password"));
        Product p = productRepository.save(new Product("P2", 2000, "http://example.com/img.png"));
        WishItem wi = new WishItem(m, p, 5);
        wishRepository.save(wi);

        // when
        int deleted = wishRepository.deleteByMemberIdAndProductId(m.getId(), p.getId());

        // then
        assertThat(deleted).isEqualTo(1);
        assertThat(wishRepository.findAllByMemberId(m.getId())).isEmpty();
    }
}
