package gift.exception;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.DuplicateKeyException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(
        MethodArgumentTypeMismatchException e) {
        Map<String, String> errors = new HashMap<>();
        String field = e.getName();
        String requiredType = e.getRequiredType() != null
            ? e.getRequiredType().getSimpleName()
            : "유효한 타입";
        errors.put("error",
            String.format("'%s' 파라미터는 %s 형식이어야 합니다.", field, requiredType));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(
        HttpMessageNotReadableException e) {
        Map<String, String> errors = new HashMap<>();

        String defaultMsg = "요청 JSON 형식이 잘못되었습니다.";
        Throwable cause = e.getCause();

        if (cause instanceof com.fasterxml.jackson.databind.exc.MismatchedInputException mie) {

            String field = mie.getPath().stream()
                .map(Reference::getFieldName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("."));

            String targetType = (mie.getTargetType() != null)
                ? mie.getTargetType().getSimpleName()
                : "유효한 타입";
            errors.put("error", String.format("'%s' 필드는 %s 형식이어야 합니다.", field, targetType));
        } else {
            errors.put("error", defaultMsg);
        }

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
        MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
            .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEmail(DuplicateKeyException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(
        InvalidCredentialsException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MissingAuthorizationHeaderException.class)
    public ResponseEntity<Map<String, String>> handleMissingHeader(
        MissingAuthorizationHeaderException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidAuthorizationHeaderException.class)
    public ResponseEntity<Map<String, String>> handleMissingHeader(
        InvalidAuthorizationHeaderException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidMemberException.class)
    public ResponseEntity<Map<String, String>> handleInvalidMember(InvalidMemberException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientStock(InsufficientStockException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(OptionProductMismatchException.class)
    public ResponseEntity<Map<String, String>> handleOptionProductMismatch(OptionProductMismatchException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(KakaoOAuthException.class)
    public ResponseEntity<ProblemDetail> handleKakaoOAuth(KakaoOAuthException ex) {
        HttpStatusCode upstream = ex.getStatusCode();
        HttpStatus status = mapToStatus(upstream);

        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle("카카오 OAuth 처리 오류");
        pd.setDetail(ex.getMessage());
        return ResponseEntity.status(status).body(pd);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ProblemDetail> handleTimeout(ResourceAccessException ex) {
        if (ex.getCause() instanceof SocketTimeoutException) {
            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.GATEWAY_TIMEOUT);
            pd.setTitle("카카오 OAuth 타임아웃");
            pd.setDetail("카카오 API 호출이 시간 내에 응답하지 못했습니다.");
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(pd);
        }
        return handleInternal(ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleInternal(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("서버 내부 오류");
        pd.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }

    private HttpStatus mapToStatus(HttpStatusCode upstream) {
        if (upstream.is4xxClientError()) {
            return (upstream.value() == HttpStatus.UNAUTHORIZED.value()
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.BAD_REQUEST);
        }
        if (upstream.is5xxServerError()) {
            return HttpStatus.BAD_GATEWAY;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @ExceptionHandler(ReauthorizeRequiredException.class)
    public ResponseEntity<Map<String, String>> handleReauthorizeRequired(ReauthorizeRequiredException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }
}
