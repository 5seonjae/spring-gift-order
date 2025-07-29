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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    @Max(value = 100_000_000, message = "수량은 1억 개 미만이어야 합니다.")
    private int quantity;

    @Column(nullable = false)
    @Size(max = 200, message = "요청 메시지는 200자 이하만 가능합니다.")
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
        this.quantity = quantity;
        this.message = message;
        this.orderDateTime = orderDateTime;
        this.member = member;
        this.option = option;
    }

    public Order(int quantity, String message, Member member, Option option) {
        this(quantity, message, LocalDateTime.now(), member, option);
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
