package gift.dto.api;

import gift.entity.WishItem;
import java.util.List;

public record WishResponseDto(
    Long productId,
    String name,
    int price,
    String imageUrl,
    int quantity,
    List<OptionResponseDto> options
) {

    public Long getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public List<OptionResponseDto> getOptions() {
        return options;
    }

    public static WishResponseDto from(WishItem wi) {
        return new WishResponseDto(
            wi.getProduct().getId(),
            wi.getProduct().getName(),
            wi.getProduct().getPrice(),
            wi.getProduct().getImageUrl(),
            wi.getQuantity(),
            wi.getProduct().getOptions().stream().map(
                option -> new OptionResponseDto(option.getId(), option.getName(),
                    option.getQuantity())).toList()
        );
    }
}
