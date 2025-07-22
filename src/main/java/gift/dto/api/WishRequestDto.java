package gift.dto.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WishRequestDto(
    @NotNull(message = "상품 ID는 필수입니다.")
    Long productId,

    @NotNull(message = "상품 수량은 필수입니다.")
    @Min(value = 1, message = "한 개 이상 담을 수 있습니다.")
    Integer quantity
) {

}
