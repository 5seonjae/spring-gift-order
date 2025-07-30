package gift.controller.view;

import static org.springframework.data.domain.Sort.Direction.DESC;

import gift.auth.LoginMember;
import gift.dto.api.OrderRequestDto;
import gift.dto.view.OrderViewResponseDto;
import gift.entity.Member;
import gift.entity.Option;
import gift.entity.Product;
import gift.repository.OptionRepository;
import gift.service.OrderService;
import gift.service.ProductService;
import jakarta.validation.Valid;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderViewController {

    private final OrderService orderService;
    private final ProductService productService;
    private final OptionRepository optionRepository;

    public OrderViewController(
        OrderService orderService,
        ProductService productService,
        OptionRepository optionRepository
    ) {
        this.orderService = orderService;
        this.productService = productService;
        this.optionRepository = optionRepository;
    }

    @GetMapping("/orders/form")
    public String orderForm(
        @RequestParam Long productId,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Product product = productService.getProductById(productId);
        if (product.getOptions().isEmpty()) {
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "상품에 옵션이 없어서 주문을 할 수 없습니다. 옵션이 추가되면 주문해주세요."
            );
            return "redirect:/products/" + productId;
        }

        Long defaultOptionId = product.getOptions().get(0).getId();

        model.addAttribute("product", product);
        model.addAttribute(
            "orderReq",
            OrderRequestDto.defaultOrderRequestDto(defaultOptionId)
        );
        return "orders/form";
    }

    @PostMapping("/orders")
    public String createOrder(
        @Valid @ModelAttribute("orderReq") OrderRequestDto dto,
        BindingResult br,
        @LoginMember Member member,
        RedirectAttributes rt,
        Model model
    ) {
        if (br.hasErrors()) {
            Option option = optionRepository.findById(dto.getOptionId())
                .orElseThrow(() -> new NoSuchElementException("옵션이 존재하지 않습니다."));
            Product product = productService.getProductById(option.getProduct().getId());
            model.addAttribute("product", product);
            return "orders/form";
        }

        orderService.addOrderForMember(member, dto);

        rt.addFlashAttribute("msg", "주문이 완료되었습니다! 카카오톡을 확인하세요.");
        return "redirect:/orders/success";
    }

    @GetMapping("/orders/success")
    public String orderSuccess() {
        return "orders/success";
    }

    @GetMapping("/orders")
    public String orderList(
        @LoginMember Member member,
        @PageableDefault(size = 5, sort = "id", direction = DESC) Pageable pageable,
        Model model
    ) {
        Page<OrderViewResponseDto> orders = orderService.getOrderListForMember(member, pageable);

        model.addAttribute("orders", orders);
        return "orders/list";
    }
}
