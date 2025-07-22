package gift.dto.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class OptionRequestDto {

    @NotBlank(message = "옵션 이름은 필수입니다.")
    @Size(max = 50, message = "옵션 이름은 최대 50자까지 가능합니다.")
    @Pattern(
        regexp = "^[a-zA-Z0-9가-힣()\\[\\]+\\-&/_ ]*$",
        message = "유효한 특수문자 ( '( )', '[ ]', '+', '-', '&', '/', '_' ) 가 아닙니다."
    )
    String name;

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    @Max(value = 100_000_000, message = "수량은 1억 개 미만이어야 합니다.")
    Integer quantity;

    public OptionRequestDto(String name, Integer quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
