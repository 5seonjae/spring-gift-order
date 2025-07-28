package gift.service;

import gift.dto.view.OrderViewResponseDto;
import gift.entity.Member;
import gift.exception.InvalidMemberException;
import gift.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public Page<OrderViewResponseDto> getOrderListForMember(Member member, Pageable pageable) {
        validateMember(member);
        return orderRepository
            .findByMemberId(member.getId(), pageable)
            .map(OrderViewResponseDto::of);
    }

    private void validateMember(Member member) {
        if (member == null)
            throw new InvalidMemberException("유효하지 않은 회원입니다.");
    }
}
