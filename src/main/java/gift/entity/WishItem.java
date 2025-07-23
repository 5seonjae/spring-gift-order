package gift.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "wish_items")
public class WishItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private int quantity;

    protected WishItem() {
    }

    public WishItem(Long id,
                    Member member,
                    Product product,
                    int quantity) {
        this.id = id;
        this.member = member;
        this.product = product;
        this.quantity = quantity;
    }

    public WishItem(Member member,
                    Product product,
                    int quantity) {
        this(null, member, product, quantity);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void addQuantity(Integer quantity) {
        this.quantity += quantity;
    }
}
