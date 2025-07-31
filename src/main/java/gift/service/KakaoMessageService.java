package gift.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.entity.Option;
import gift.entity.Order;
import gift.entity.Product;
import gift.exception.ExternalServiceException;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class KakaoMessageService {

    private final WebClient kakaoClient;
    private final ObjectMapper objectMapper;
    private final Long templateId;
    private static final int KAKAO_SUCCESS_CODE = 0;

    public KakaoMessageService(
        @Value("${kakao.api-url}") String apiUrl,
        @Value("${kakao.template-id}") Long templateId,
        WebClient.Builder builder,
        ObjectMapper objectMapper
    ) {
        this.kakaoClient = builder.baseUrl(apiUrl).build();
        this.objectMapper = objectMapper;
        this.templateId = templateId;
    }

    public void sendOrderMemo(Order order, String accessToken) {
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("template_id", String.valueOf(templateId));
            form.add("template_args", buildTemplateArgs(order));

            Map<String, Object> resp = kakaoClient.post()
                .uri("/v2/api/talk/memo/send")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

            Integer resultCode = (Integer) resp.get("result_code");
            if (!Objects.equals(resultCode, KAKAO_SUCCESS_CODE)) {
                throw new ExternalServiceException("카카오톡 전송 실패: " + resp);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("template_args 직렬화 실패");
        }
    }

    private String buildTemplateArgs(Order order) throws JsonProcessingException {
        Option option   = order.getOption();
        Product product = option.getProduct();

        Map<String, Object> args = Map.of(
            "PRODUCT_TOTAL_PRICE", product.getPrice() * order.getQuantity(),
            "PRODUCT_PRICE", product.getPrice(),
            "ORDER_TIME", order.getOrderDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            "PRODUCT_IMAGE_URL", product.getImageUrl(),
            "PRODUCT_NAME", product.getName(),
            "OPTION_NAME", option.getName(),
            "ORDER_QUANTITY", order.getQuantity(),
            "ORDER_MESSAGE", order.getMessage()
        );

        return objectMapper.writeValueAsString(args);
    }
}
