package gift.entity;

import gift.exception.InsufficientStockException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Entity
@Table(name = "options")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    protected Option() {}

    public Option(Long id, Product product, String name, int quantity) {
        validate(product, name, quantity);

        this.id = id;
        this.product = product;
        this.name = name;
        this.quantity = quantity;
    }

    public Option(Product product, String name, int quantity) {
        this(null, product, name, quantity);
    }

    private static final Pattern NAME_PATTERN =
        Pattern.compile("^[a-zA-Z0-9가-힣()\\[\\]+\\-&/_ ]*$");

    private void validate(Product product, String name, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("상품은 필수입니다.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("옵션 이름은 필수입니다.");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("최대 50자까지 가능합니다.");
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException(
                "유효한 특수문자 ( '( )', '[ ]', '+', '-', '&', '/', '_' ) 가 아닙니다."
            );
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("수량은 0개 이상이어야 합니다.");
        }
        if (quantity > 100_000_000) {
            throw new IllegalArgumentException("수량은 1억 개 미만이어야 합니다.");
        }
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
