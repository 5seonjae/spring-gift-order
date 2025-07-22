package gift.dto.view;

import jakarta.validation.constraints.*;

public class ProductViewRequestDto {

    @NotBlank(message = "상품명을 입력해주세요.")
    @Size(max = 15, message = "최대 15자까지 가능합니다.")
    @Pattern(
            regexp = "^[a-zA-Z0-9가-힣()\\[\\]+\\-&/_ ]*$",
            message = "유효한 특수문자 ( '( )', '[ ]', '+', '-', '&', '/', '_' ) 가 아닙니다."
    )
    private String name;

    @NotNull(message = "가격을 입력해주세요.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    @Pattern(
        regexp = "^(http|https)://.*$",
        message = "유효한 이미지 URL이 아닙니다."
    )
    @NotBlank(message = "이미지 URL을 입력해주세요.")
    private String imageUrl;

    // 기본 생성자
    public ProductViewRequestDto() {
    }

    // Getter / Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
