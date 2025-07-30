package gift.controller.api;

import static org.springframework.data.domain.Sort.Direction.DESC;

import gift.auth.LoginMember;
import gift.dto.api.OrderRequestDto;
import gift.dto.api.OrderResponseDto;
import gift.dto.view.OrderViewResponseDto;
import gift.entity.Member;
import gift.entity.Order;
import gift.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Page<OrderViewResponseDto>> getAllOrders(
        @LoginMember Member member,
        @PageableDefault(size = 5, sort = "id", direction = DESC) Pageable pageable
    ) {
        Page<OrderViewResponseDto> orders = orderService.getOrderListForMember(member, pageable);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
        @LoginMember Member member,
        @RequestBody @Valid OrderRequestDto orderRequestDto
    ) {
        Order saved = orderService.addOrderForMember(member, orderRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponseDto.from(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
        @LoginMember Member member,
        @PathVariable Long id
    ) {
        orderService.deleteOrderForMember(member, id);
        return ResponseEntity.noContent().build();
    }
}
