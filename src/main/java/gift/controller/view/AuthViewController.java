package gift.controller.view;

import gift.dto.api.MemberRegisterRequestDto;
import gift.exception.InvalidCredentialsException;
import gift.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthViewController {

    private final MemberService memberService;

    public AuthViewController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("form", new MemberRegisterRequestDto("", ""));
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute("form") @Valid MemberRegisterRequestDto form,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        memberService.registerMember(form);
        redirectAttributes.addFlashAttribute("joined", true);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }

    @Value("${jwt.expiration-ms}")
    private long jwtExpiryMs;

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        try {
            String jwt = memberService.loginMember(email, password);
            ResponseCookie cookie = ResponseCookie.from("AUTH", jwt)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMillis(jwtExpiryMs))
                .build();
            response.addHeader("Set-Cookie", cookie.toString());
            if (memberService.isAdmin(email)) {
                return "redirect:/admin/products";
            }
            return "redirect:/products";
        } catch (InvalidCredentialsException e) {
            redirectAttributes.addFlashAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
            return "redirect:/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse httpServletResponse) {
        httpServletResponse.addHeader("Set-Cookie",
            ResponseCookie.from("AUTH", "")
                .maxAge(0)
                .path("/")
                .build()
                .toString());
        return "redirect:/login";
    }
}
