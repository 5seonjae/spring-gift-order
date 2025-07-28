package gift.dto.api;

import gift.entity.Order;
import java.time.LocalDateTime;

public record OrderResponseDto (
    Long id,
    Long optionId,
    int quantity,
    LocalDateTime orderDateTime,
    String message
) {

    public Long getId() {
        return id;
    }

    public Long getOptionId() {
        return optionId;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getOrderDateTime() {
        return orderDateTime;
    }

    public String getMessage() {
        return message;
    }

    public static OrderResponseDto of(Order order) {
        return new OrderResponseDto(
            order.getId(),
            order.getOption().getId(),
            order.getQuantity(),
            order.getOrderDateTime(),
            order.getMessage()
        );
    }
}