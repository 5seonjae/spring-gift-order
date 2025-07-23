package gift.service;

import gift.dto.api.WishRequestDto;
import gift.dto.api.WishResponseDto;
import gift.entity.Member;
import gift.entity.Product;
import gift.entity.WishItem;
import gift.exception.InvalidMemberException;
import gift.repository.ProductRepository;
import gift.repository.WishRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.NoSuchElementException;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WishService {

    private final WishRepository wishRepository;
    private final ProductRepository productRepository;

    public WishService(WishRepository wishRepository,
        ProductRepository productRepository) {
        this.wishRepository = wishRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<WishResponseDto> getWishListForMember(Member member, Pageable pageable) {
        validateMember(member);
        return wishRepository
            .findAllByMemberId(member.getId(), pageable)
            .map(WishResponseDto::of);
    }

    public void addWishItemForMember(Member member, WishRequestDto wishRequestDto) {
        validateMember(member);

        Product product = productRepository.findById(wishRequestDto.productId()).orElseThrow(
            () -> new NoSuchElementException("상품을 찾을 수 없습니다."));

        wishRepository.findAllByMemberId(member.getId()).stream()
            .filter(w -> Objects.equals(w.getProduct().getId(), wishRequestDto.productId()))
            .findFirst()
            .ifPresentOrElse(
                w -> { w.addQuantity(wishRequestDto.quantity()); },
                () -> { wishRepository.save(new WishItem(member, product, wishRequestDto.quantity())); }
            );
    }

    public void removeWishItemForMember(Member member, Long productId) {
        validateMember(member);
        if (productRepository.findById(productId).isEmpty()) {
            throw new NoSuchElementException("상품을 찾을 수 없습니다.");
        }

        int rows = wishRepository.deleteByMemberIdAndProductId(member.getId(), productId);

        if (rows == 0) {                               // ← 위시 항목 없었음
            throw new NoSuchElementException("위시 목록에 없는 상품입니다.");
        }
    }

    private void validateMember(Member member) {
        if (member == null)
            throw new InvalidMemberException("유효하지 않은 회원입니다.");
    }
}
