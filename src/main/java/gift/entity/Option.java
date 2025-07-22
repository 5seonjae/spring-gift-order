package gift.entity;

import gift.exception.InsufficientStockException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Objects;

@Entity
@Table(name = "options")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    @NotBlank(message = "옵션 이름은 필수입니다.")
    @Size(max = 50, message = "최대 50자까지 가능합니다.")
    @Pattern(
        regexp = "^[a-zA-Z0-9가-힣()\\[\\]+\\-&/_ ]*$",
        message = "유효한 특수문자 ( '( )', '[ ]', '+', '-', '&', '/', '_' ) 가 아닙니다."
    )
    private String name;

    @Column(nullable = false)
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    @Max(value = 100_000_000, message = "수량은 1억 개 미만이어야 합니다.")
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    protected Option() {}

    public Option(Long id, Product product, String name, int quantity) {
        this.id = id;
        this.product = product;
        this.name = name;
        this.quantity = quantity;
    }

    public Option(Product product, String name, int quantity) {
        this(null, product, name, quantity);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void subtract(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("차감 수량은 1 이상이어야 합니다.");
        }
        if (quantity > this.quantity) {
            throw new InsufficientStockException("재고가 부족합니다.");
        }
        this.quantity -= quantity;
    }

    public boolean belongsTo(Long productId) {
        return Objects.equals(this.product.getId(), productId);
    }
}
