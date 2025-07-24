package gift.controller.view;

import gift.dto.api.MemberRegisterRequestDto;
import gift.exception.InvalidCredentialsException;
import gift.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
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

    /**
     * 카카오 OAuth 콜백 처리
     * - Redirect URI로 "/" 만 등록했을 때
     * - code 파라미터가 있으면 토큰 교환 → JWT 발급 → HttpOnly 쿠키에 담아 루트로 리다이렉트
     * - 없으면 로그인 페이지로 리다이렉트
     */
    @GetMapping("/")
    public void kakaoCallback(
        @RequestParam(value = "code", required = false) String code,
        HttpServletResponse response
    ) throws IOException {
        if (code != null) {
            // 1) 인가 코드 → JWT
            String jwt = memberService.loginWithKakao(code);

            // 2) HttpOnly 쿠키에 JWT 담기
            Cookie authCookie = new Cookie("AUTH", jwt);
            authCookie.setHttpOnly(true);
            authCookie.setPath("/");
            // 필요하다면 secure, maxAge 등 추가 설정
            response.addCookie(authCookie);

            // 3) 로그인 후 메인 페이지(또는 원하는 URL)로
            response.sendRedirect("/products");
        } else {
            // code 없으면 /login 화면으로
            response.sendRedirect("/login");
        }
    }
}
