package gift;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.dto.api.MemberRegisterRequestDto;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 성공 → 201, 토큰 반환")
    void register_success() throws Exception {
        MemberRegisterRequestDto req = new MemberRegisterRequestDto("newuser@example.com",
            "password123");

        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.token").isString())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("회원가입 실패 – 이메일 중복 → 409 Conflict + 메시지")
    void register_duplicateEmail() throws Exception {
        // 먼저 한 번 가입시켜 둠
        MemberRegisterRequestDto req = new MemberRegisterRequestDto("dup@example.com",
            "password123");

        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());

        // 같은 이메일로 다시 시도
        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error")
                .value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("회원가입 실패 – 잘못된 요청 바디(이메일 포맷 오류) - 이메일 없음 → 400 Bad Request")
    void register_fail_emailMissing() throws Exception {
        MemberRegisterRequestDto req = new MemberRegisterRequestDto("", "1234567890");

        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.email")
                .value("이메일은 필수입니다."));
    }

    @Test
    @DisplayName("회원가입 실패 – 잘못된 요청 바디(이메일 포맷 오류) - 이메일 형식 오류 → 400 Bad Request")
    void register_fail_invalidEmailFormat() throws Exception {
        MemberRegisterRequestDto req = new MemberRegisterRequestDto("email", "1234567890");

        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.email")
                .value("유효한 이메일 형식이 아닙니다."));
    }

    @Test
    @DisplayName("회원가입 실패 – 잘못된 요청 바디(이메일 포맷 오류) - 6자 미만 비밀번호 → 400 Bad Request")
    void register_fail_passwordTooShort() throws Exception {
        MemberRegisterRequestDto req = new MemberRegisterRequestDto("newuser@example.com", "123");

        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.password")
                .value("비밀번호는 최소 6자 이상이어야 합니다."));
    }

    private void register(String email, String password) throws Exception {
        var req = new MemberRegisterRequestDto(email, password);

        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("로그인 성공 → 200 OK + token 반환")
    void login_success() throws Exception {
        String email = "user@example.com";
        String pw = "password123";
        register(email, pw);

        String credentials = Base64.getEncoder()
            .encodeToString((email + ":" + pw).getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/members/login")
                .header("Authorization", "Basic " + credentials))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.token").isString())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("로그인 실패 – 헤더 누락 → 401 Unauthorized + 메시지")
    void login_fail_missingHeader() throws Exception {
        mockMvc.perform(post("/api/members/login"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("Authorization 헤더가 필요합니다."));
    }

    @Test
    @DisplayName("로그인 실패 – 잘못된 프리픽스 → 401 Unauthorized + 메시지")
    void login_fail_invalidPrefix() throws Exception {
        mockMvc.perform(post("/api/members/login")
                .header("Authorization", "Bearer sometoken"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("Authorization 헤더 형식이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("로그인 실패 – 디코딩 오류/구분자 누락 → 401 Unauthorized + 메시지")
    void login_fail_invalidFormat() throws Exception {
        // Base64로 인코딩했지만 콜론(:)이 없음
        String bad = Base64.getEncoder()
            .encodeToString("noconstr".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/members/login")
                .header("Authorization", "Basic " + bad))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error")
                .value("Authorization 헤더 형식이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("로그인 실패 – 잘못된 비밀번호 → 403 Forbidden + 메시지")
    void login_fail_wrongCredentials() throws Exception {
        String email = "test@example.com";
        String pw = "correctpw";
        register(email, pw);

        String wrong = Base64.getEncoder()
            .encodeToString((email + ":wrongpw").getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/members/login")
                .header("Authorization", "Basic " + wrong))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error")
                .value("이메일 또는 비밀번호가 올바르지 않습니다."));
    }
}
