package gift.dto.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class OrderRequestDto {

    @NotNull(message = "옵션 ID는 필수입니다.")
    Long optionId;

    @NotNull(message = "옵션 수량은 필수입니다.")
    @Min(value = 1, message = "한 개 이상 담을 수 있습니다.")
    Integer quantity;

    @Size(max = 200,message = "요청 메시지는 200자 이하만 가능합니다.")
    String message;

    public OrderRequestDto(Long optionId, Integer quantity, String message) {
        this.optionId = optionId;
        this.quantity = quantity;
        this.message = message;
    }

    public Long getOptionId() {
        return optionId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getMessage() {
        return message;
    }
}
