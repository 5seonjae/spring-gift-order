package gift.controller.api;

import gift.dto.api.MemberRegisterRequestDto;
import gift.dto.api.MemberRegisterResponseDto;
import gift.exception.InvalidCredentialsException;
import gift.service.MemberService;
import gift.util.BasicAuthHeaderParser;
import gift.util.Credentials;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final BasicAuthHeaderParser basicAuthHeaderParser;

    public MemberController(MemberService memberService,
                            BasicAuthHeaderParser basicAuthHeaderParser) {
        this.memberService = memberService;
        this.basicAuthHeaderParser = basicAuthHeaderParser;
    }

    // 회원 생성
    @PostMapping()
    public ResponseEntity<MemberRegisterResponseDto> createMember(
        @RequestBody @Valid MemberRegisterRequestDto requestDto
    ) {
        String token = memberService.registerMember(requestDto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new MemberRegisterResponseDto(token));
    }

    @PostMapping("/login")
    public ResponseEntity<MemberRegisterResponseDto> login(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) throws InvalidCredentialsException {
        Credentials cred = basicAuthHeaderParser.parse(authorizationHeader);

        String token = memberService.loginMember(cred.email(), cred.password());
        return ResponseEntity.ok(new MemberRegisterResponseDto(token));
    }
}
