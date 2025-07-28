package gift.service;

import gift.dto.api.OrderRequestDto;
import gift.dto.view.OrderViewResponseDto;
import gift.entity.Member;
import gift.entity.Option;
import gift.entity.Order;
import gift.exception.InvalidMemberException;
import gift.repository.OptionRepository;
import gift.repository.OrderRepository;
import gift.repository.WishRepository;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OptionRepository optionRepository;
    private final WishRepository wishRepository;
    private final OptionService optionService;

    public OrderService(
        OrderRepository orderRepository,
        OptionRepository optionRepository,
        WishRepository wishRepository,
        OptionService optionService
    ) {
        this.orderRepository = orderRepository;
        this.optionRepository = optionRepository;
        this.wishRepository = wishRepository;
        this.optionService = optionService;
    }

    @Transactional(readOnly = true)
    public Page<OrderViewResponseDto> getOrderListForMember(Member member, Pageable pageable) {
        validateMember(member);
        return orderRepository
            .findByMemberId(member.getId(), pageable)
            .map(OrderViewResponseDto::of);
    }

    @Transactional
    public Order addOrderForMember(Member member, OrderRequestDto orderRequestDto) {
        validateMember(member);
        Option option = optionRepository.findById(orderRequestDto.getOptionId())
            .orElseThrow(() -> new NoSuchElementException("옵션을 찾을 수 없습니다."));
        optionService.subtractQuantity(option.getId(), orderRequestDto.getQuantity());
        Order saved = orderRepository.save(
            new Order(
                orderRequestDto.getQuantity(),
                orderRequestDto.getMessage(),
                member,
                option
            )
        );
        wishRepository.deleteByMemberIdAndProductId(member.getId(), option.getProduct().getId());
        return saved;
    }

    public void deleteOrderForMember(Member member, Long id) {
        validateMember(member);
        orderRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다."));
        orderRepository.deleteById(id);
    }

    private void validateMember(Member member) {
        if (member == null)
            throw new InvalidMemberException("유효하지 않은 회원입니다.");
    }
}
