package gift.exception;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

    // PathVariable/RequestParam 타입 불일치 시 예외를 처리할 핸들러
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(
        MethodArgumentTypeMismatchException e) {
        Map<String, String> errors = new HashMap<>();
        String field = e.getName();             // "id" 같은 파라미터 이름
        String requiredType = e.getRequiredType() != null
            ? e.getRequiredType().getSimpleName()
            : "유효한 타입";
        errors.put("error",
            String.format("'%s' 파라미터는 %s 형식이어야 합니다.", field, requiredType));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // JSON 바디 파싱 오류(HttpMessageNotReadableException) 예외 처리 핸들러
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(
        HttpMessageNotReadableException e) {
        Map<String, String> errors = new HashMap<>();

        String defaultMsg = "요청 JSON 형식이 잘못되었습니다.";
        Throwable cause = e.getCause();

        if (cause instanceof com.fasterxml.jackson.databind.exc.MismatchedInputException mie) {
            // Jackson이 파싱에 실패한 필드 이름 추출
            String field = mie.getPath().stream()
                .map(Reference::getFieldName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("."));
            // 기대 타입 이름
            String targetType = (mie.getTargetType() != null)
                ? mie.getTargetType().getSimpleName()
                : "유효한 타입";
            errors.put("error", String.format("'%s' 필드는 %s 형식이어야 합니다.", field, targetType));
        } else {
            errors.put("error", defaultMsg);
        }

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // 유효성 검사 실패 시 예외를 처리할 핸들러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
        MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
            .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // 중복 이메일 예외를 처리할 핸들러
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEmail(DuplicateKeyException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // 이메일, 비밀번호 불일치 예외를 처리할 핸들러
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(
        InvalidCredentialsException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // 헤더 누락 예외를 처리할 핸들러
    @ExceptionHandler(MissingAuthorizationHeaderException.class)
    public ResponseEntity<Map<String, String>> handleMissingHeader(
        MissingAuthorizationHeaderException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // 헤더 형식 오류 예외를 처리할 핸들러
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
}
