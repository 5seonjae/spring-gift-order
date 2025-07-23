package gift.controller.api;

import static org.springframework.data.domain.Sort.Direction.DESC;

import gift.auth.LoginMember;
import gift.dto.api.WishRequestDto;
import gift.dto.api.WishResponseDto;
import gift.entity.Member;
import gift.service.WishService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishes")
public class WishController {

    private final WishService wishService;

    public WishController(WishService wishService) {
        this.wishService = wishService;
    }

    @GetMapping
    public Page<WishResponseDto> getWishList(
        @LoginMember Member member,
        @PageableDefault(size = 5, sort = "id", direction = DESC) Pageable pageable
    ) {
        return wishService.getWishListForMember(member, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addWishItem(
        @LoginMember Member member,
        @RequestBody @Valid WishRequestDto wishRequestDto
    ) {
        wishService.addWishItemForMember(member, wishRequestDto);
    }

    @DeleteMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWishItem(@LoginMember Member member,
        @PathVariable Long productId) {
        wishService.removeWishItemForMember(member, productId);
    }
}
