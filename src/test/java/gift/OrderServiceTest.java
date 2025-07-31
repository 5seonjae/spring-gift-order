package gift;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gift.dto.api.KakaoTokenResponseDto;
import gift.dto.api.OrderRequestDto;
import gift.dto.view.OrderViewResponseDto;
import gift.entity.KakaoTokens;
import gift.entity.Member;
import gift.entity.Option;
import gift.entity.Order;
import gift.entity.Product;
import gift.exception.InvalidMemberException;
import gift.repository.OptionRepository;
import gift.repository.OrderRepository;
import gift.repository.WishRepository;
import gift.service.KakaoMessageService;
import gift.service.OptionService;
import gift.service.OrderService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OptionRepository optionRepository;

    @Mock
    private WishRepository wishRepository;

    @Mock
    private OptionService optionService;

    @Mock
    private KakaoMessageService kakaoMessageService;

    @InjectMocks
    private OrderService orderService;


    KakaoTokenResponseDto dummyDto = new KakaoTokenResponseDto(
        "dummy-access-token",
        "dummy-refresh-token",
        3600,
        2_592_000,
        "Bearer"
    );
    private final Member member = new Member(1234L, "테스트유저", KakaoTokens.from(dummyDto));
    private final Product product = new Product(1L, "초콜릿", 1000, "http://chocolate.png");
    private final Option option = new Option(1L, product, "다크 초콜릿", 10);

    @Test
    @DisplayName("[성공] 주문 전체 조회 ( 주문 없음 ) - 200 OK")
    void getOrderList_success_zero() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("id").descending());
        Page<Order> page = new PageImpl<>(List.of(), pageable, 0);
        when(orderRepository.findByMemberId(member.getId(), pageable)).thenReturn(page);

        Page<OrderViewResponseDto> result = orderService.getOrderListForMember(member, pageable);

        assertThat(result.getTotalElements()).isZero();
        verify(orderRepository).findByMemberId(member.getId(), pageable);
    }

    @Test
    @DisplayName("[성공] 주문 전체 조회 ( 주문 1개 ) - 200 OK")
    void getOrderList_success_one() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("id").descending());
        Order order = new Order(1, "msg", member, option);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findByMemberId(member.getId(), pageable)).thenReturn(page);

        Page<OrderViewResponseDto> result = orderService.getOrderListForMember(member, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().quantity()).isEqualTo(1);
        assertThat(result.getContent().getFirst().message()).isEqualTo("msg");
        verify(orderRepository).findByMemberId(member.getId(), pageable);
    }

    @ParameterizedTest(name = "[page={0}] quantity={1}, message={2}")
    @CsvSource({
        "0, 3, C, true,  false, true",
        "1, 2, B, false, true,  true",
        "2, 1, A, false, true,  false"
    })
    @DisplayName("[성공] 주문 전체 조회 ( 주문 2개 이상 ) - 200 OK")
    void getOrderList_success_multiple(
        int pageIndex,
        int expectedQuantity,
        String expectedMessage,
        boolean expectedFirst,
        boolean expectedPrev,
        boolean expectedNext
    ) {
        Order o1 = new Order(1, "A", member, option);
        Order o2 = new Order(2, "B", member, option);
        Order o3 = new Order(3, "C", member, option);
        List<Order> allOrders = List.of(o3, o2, o1);

        Pageable pageable = PageRequest.of(pageIndex, 1, Sort.by("id").descending());
        List<Order> pageContent = allOrders.subList(pageIndex, pageIndex + 1);
        Page<Order> stubPage = new PageImpl<>(pageContent, pageable, allOrders.size());

        when(orderRepository.findByMemberId(member.getId(), pageable)).thenReturn(stubPage);

        Page<OrderViewResponseDto> result = orderService.getOrderListForMember(member, pageable);

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(pageIndex);
        assertThat(result.getSize()).isEqualTo(1);

        assertThat(result.getContent())
            .extracting(OrderViewResponseDto::quantity)
            .containsExactly(expectedQuantity);
        assertThat(result.getContent())
            .extracting(OrderViewResponseDto::message)
            .containsExactly(expectedMessage);

        assertThat(result.isFirst()).isEqualTo(expectedFirst);
        assertThat(result.hasPrevious()).isEqualTo(expectedPrev);
        assertThat(result.hasNext()).isEqualTo(expectedNext);

        verify(orderRepository).findByMemberId(member.getId(), pageable);
    }

    @Test
    @DisplayName("[성공] 주문 추가 - 201 Created")
    void addOrder_removesWish() {
        OrderRequestDto dto = new OrderRequestDto(option.getId(), 2, "부탁해요");
        when(optionRepository.findById(option.getId()))
            .thenReturn(Optional.of(option));
        Order saved = new Order(dto.getQuantity(), dto.getMessage(), member, option);
        when(orderRepository.save(any(Order.class))).thenReturn(saved);
        doNothing().when(kakaoMessageService)
            .sendOrderMemo(any(Order.class), anyString());

        Order result = orderService.addOrderForMember(member, dto);

        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(result.getMessage()).isEqualTo("부탁해요");
        verify(optionService).subtractQuantity(option.getId(), 2);
        verify(orderRepository).save(any(Order.class));
        verify(wishRepository).deleteByMemberIdAndProductId(
            member.getId(),
            option.getProduct().getId()
        );
    }

    @Test
    @DisplayName("[실패] 주문 추가 - 옵션 없음 - 404 Not Found")
    void addOrder_optionNotFound_throws() {
        OrderRequestDto dto = new OrderRequestDto(999L, 2, "부탁해요");
        when(optionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.addOrderForMember(member, dto))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("옵션을 찾을 수 없습니다.");

        verify(optionService, never()).subtractQuantity(anyLong(), anyInt());
        verify(orderRepository, never()).save(any());
        verify(wishRepository, never()).deleteByMemberIdAndProductId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("[성공] 주문 삭제 - 204 No Content")
    void deleteOrder_success() {
        Long orderId = 1L;
        Order order = new Order(1, "부탁해요", member, option);
        when(orderRepository.findById(orderId))
            .thenReturn(Optional.of(order));

        orderService.deleteOrderForMember(member, orderId);

        verify(orderRepository).delete(order);
    }

    @Test
    @DisplayName("[실패] 주문 삭제 - 주문 없음 - 404 Not Found")
    void deleteOrder_orderNotFound_throws() {
        Long orderId = 42L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.deleteOrderForMember(member, orderId))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("주문을 찾을 수 없습니다.");

        verify(orderRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("[실패] 공통 - 회원 없음 - 404 Not Found")
    void anyMethod_nullMember_throwsInvalidMember() {
        Pageable pageable = PageRequest.of(0, 1);
        assertThatThrownBy(() -> orderService.getOrderListForMember(null, pageable))
            .isInstanceOf(InvalidMemberException.class)
            .hasMessage("유효하지 않은 회원입니다.");
    }
}