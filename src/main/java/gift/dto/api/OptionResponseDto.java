package gift.dto.api;

import gift.entity.Option;

public record OptionResponseDto (
    Long id,
    String name,
    int quantity
) {
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public static OptionResponseDto of(Option option) {
        return new OptionResponseDto(
            option.getId(),
            option.getName(),
            option.getQuantity()
        );
    }
}
