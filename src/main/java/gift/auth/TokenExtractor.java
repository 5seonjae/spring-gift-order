package gift.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

@Component
public class TokenExtractor {

    public String extractBearerHeader(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) return header;
        Cookie cookie = WebUtils.getCookie(req, "AUTH");
        if (cookie != null && StringUtils.hasText(cookie.getValue())) {
            return "Bearer " + cookie.getValue();
        }
        return null;
    }
}
