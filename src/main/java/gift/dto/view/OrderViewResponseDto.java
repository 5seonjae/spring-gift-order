package gift.dto.view;

import gift.entity.Order;
import java.time.LocalDateTime;

public record OrderViewResponseDto (
    Long id,
    String productName,
    String optionName,
    int quantity,
    String message,
    LocalDateTime orderDateTime
) {

    public Long getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public String getOptionName() {
        return optionName;
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

    public static OrderViewResponseDto from(Order order) {
        return new OrderViewResponseDto(
            order.getId(),
            order.getOption().getName(),
            order.getOption().getProduct().getName(),
            order.getQuantity(),
            order.getMessage(),
            order.getOrderDateTime()
        );
    }
}
