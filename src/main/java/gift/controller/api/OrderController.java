package gift.controller.api;

import static org.springframework.data.domain.Sort.Direction.DESC;

import gift.auth.LoginMember;
import gift.dto.view.OrderViewResponseDto;
import gift.entity.Member;
import gift.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
