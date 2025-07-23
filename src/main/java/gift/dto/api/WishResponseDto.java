package gift.dto.api;

import gift.entity.WishItem;

public record WishResponseDto(
    Long productId,
    String name,
    int price,
    String imageUrl,
    int quantity
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

    public static WishResponseDto of(WishItem wi) {
        return new WishResponseDto(
            wi.getProduct().getId(),
            wi.getProduct().getName(),
            wi.getProduct().getPrice(),
            wi.getProduct().getImageUrl(),
            wi.getQuantity()
        );
    }
}
