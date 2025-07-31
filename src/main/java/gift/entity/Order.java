package gift.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime orderDateTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id")
    private Option option;

    protected Order() {}

    public Order(
        int quantity,
        String message,
        LocalDateTime orderDateTime,
        Member member,
        Option option
    ) {
        validate(quantity, message, member, option);

        this.quantity = quantity;
        this.message = message;
        this.orderDateTime = orderDateTime;
        this.member = member;
        this.option = option;
    }

    public Order(int quantity, String message, Member member, Option option) {
        this(quantity, message, LocalDateTime.now(), member, option);
    }

    private void validate(
        int quantity,
        String message,
        Member member,
        Option option
    ) {
        if (member == null) {
            throw new IllegalArgumentException("회원 정보는 필수입니다.");
        }
        if (option == null) {
            throw new IllegalArgumentException("옵션 정보는 필수입니다.");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        if (quantity > 100_000_000) {
            throw new IllegalArgumentException("수량은 1억 개 미만이어야 합니다.");
        }
        if (message.length() > 200) {
            throw new IllegalArgumentException("요청 메시지는 200자 이하만 가능합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getMessage() {
        return message;
    }

    public Member getMember() {
        return member;
    }

    public Option getOption() {
        return option;
    }

    public LocalDateTime getOrderDateTime() {
        return orderDateTime;
    }
}
