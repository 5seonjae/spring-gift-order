package gift.dto.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OptionSubtractRequestDto {

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    @Max(value = 100_000_000, message = "수량은 1억 개 미만이어야 합니다.")
    Integer quantity;

    public OptionSubtractRequestDto(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
