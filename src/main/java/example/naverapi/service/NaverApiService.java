package example.naverapi.service;

import example.naverapi.config.NaverProperties;
import example.naverapi.web.dto.OrderIdDto;
import example.naverapi.web.dto.OrderSearchDto;
import example.naverapi.web.dto.PaySettleDto;
import example.naverapi.web.dto.SignatureDto;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NaverApiService {

    private final NaverProperties properties;

    public String getToken() {
        String clientId = properties.getApplicationId();
        Long currentTime = System.currentTimeMillis()-1000;
//        String clientSecretSign = createSignatureByUsingNativeJava(currentTime);
        String clientSecretSign = createSignature(currentTime);
        String grantType = "client_credentials";
        String type = "SELF";

        // Set the request URL
        String baseUrl = "https://api.commerce.naver.com/external";
        String tokenEndpoint = "/v1/oauth2/token";

        // Build the request body
        String requestBody = "client_id=" + clientId +
                "&timestamp=" + String.valueOf(currentTime) +
                "&client_secret_sign=" + clientSecretSign +
                "&grant_type=" + grantType +
                "&type=" + type;
//                "&account_id=" + accountId;

        // Create WebClient instance
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();


        UriBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl)
                .path(tokenEndpoint)
                .queryParam("client_id", clientId)
                .queryParam("timestamp", String.valueOf(currentTime))
                .queryParam("client_secret_sign", clientSecretSign)
                .queryParam("grant_type", grantType)
                .queryParam("type", type);

        String builtUri = uriBuilder.build().toString();

        // Make the POST request for the OAuth2 token
        // Handle the response
        String token;
        try {
            String responseBody = webClient.post()
                    .uri(builder -> builder
                            .path(tokenEndpoint)
                            .queryParam("client_id", clientId)
                            .queryParam("timestamp", String.valueOf(currentTime))
                            .queryParam("client_secret_sign", clientSecretSign)
                            .queryParam("grant_type", grantType)
                            .queryParam("type", type)
//                            .queryParam("account_id", accountId)
                            .build())
//                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // blocking call, handle this appropriately in a reactive environment

            // Handle the response
            token = responseBody.split(",")[0].split(":")[1].replace("\"", "");
        } catch (WebClientResponseException e) {
            HttpHeaders headers = e.getHeaders();
            throw e;  // Re-throw the exception if needed
        }
        return token;
    }

    public SignatureDto getSignature() {
        long currentTime = System.currentTimeMillis() - 1000;
        return new SignatureDto(createSignature(currentTime), currentTime);
    }

    private String createSignature(Long timestamp) {
        String appId = properties.getApplicationId();
        String appSecret = properties.getApplicationSecret();
        StringJoiner joiner = new StringJoiner("_");
        joiner.add(appId);
        joiner.add(String.valueOf(timestamp));
        String password = joiner.toString();
        String hashedPassword = BCrypt.hashpw(password, appSecret);
        return Base64.getUrlEncoder().encodeToString(hashedPassword.getBytes(StandardCharsets.UTF_8));
    }

    public JSONArray findOrderItems(OrderIdDto dto) {
        String token = getToken();

        // Set the request URL
        String baseUrl = "https://api.commerce.naver.com/external";
        String endpoint = "/v1/pay-order/seller/product-orders/query";

        // Create WebClient instance
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token) // Bearer 토큰 추가
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
//        String requestBody = "productOrderIds="+toJsonArray(dto.productOrderIds());

        Map<String, List<String>> requestBodyMap = new HashMap<>();
        requestBodyMap.put("productOrderIds", dto.productOrderIds());

        // Make the POST request for the OAuth2 token
        // Handle the response
        try {
            String responseBody = webClient.post()
                    .uri(builder -> builder
                            .path(endpoint)
                            .build())
                    .body(BodyInserters.fromValue(requestBodyMap))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // blocking call, handle this appropriately in a reactive environment

            // Handle the response
            JSONObject mainObject = new JSONObject(responseBody);
            JSONArray data = new JSONArray();
            if (mainObject.has("data")) {
                data = mainObject.getJSONArray("data");
            }
            return data;
        } catch (WebClientResponseException e) {
            HttpHeaders headers = e.getHeaders();
            throw e;  // Re-throw the exception if needed
        }
    }

    public JSONArray findSingleOrder(String orderId) {
        String token = getToken();

        String baseUrl = "https://api.commerce.naver.com/external";
        String tokenEndpoint = "/v1/pay-order/seller/orders/" + orderId + "/product-order-ids";

        // Create WebClient instance
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token) // Bearer 토큰 추가
                .build();


        UriBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl)
                .path(tokenEndpoint);
        String builtUri = uriBuilder.build().toString();

        // Make the GET request for fetch order list
        // Handle the response
        try {
            String responseBody = webClient.get()
                    .uri(builder -> builder
                            .path(tokenEndpoint)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // blocking call, handle this appropriately in a reactive environment

            // Handle the response

            JSONObject mainObject = new JSONObject(responseBody);
            JSONArray data = new JSONArray();
            if (mainObject.has("data")) {
                data = mainObject.getJSONArray("data");
            }
            return data;
        } catch (WebClientResponseException e) {
            HttpHeaders headers = e.getHeaders();
            throw e;  // Re-throw the exception if needed
        }

    }

    public JSONArray findOrders(OrderSearchDto dto) throws JSONException {
        String token = getToken();
        LocalDateTime expectedDate = LocalDateTime.of(dto.year(), dto.month(), dto.date(), 0, 0);
        if (expectedDate.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("현재 이전의 기록들만 조회할 수 있습니다.");
        }
        // 시간 부분을 자정으로 설정
        OffsetDateTime nowWithOffset = OffsetDateTime.of(dto.year(), dto.month(), dto.date(), 0, 0, 0, 0, ZoneOffset.ofHours(9));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        // 원하는 형식으로 포맷팅
        String formattedMidnight = nowWithOffset.format(formatter);


        // Set the request URL
        String baseUrl = "https://api.commerce.naver.com/external";
        String tokenEndpoint = "/v1/pay-order/seller/product-orders/last-changed-statuses";

        // Create WebClient instance
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token) // Bearer 토큰 추가
                .build();


        UriBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl)
                .path(tokenEndpoint)
                .queryParam("lastChangedFrom", formattedMidnight);

        String builtUri = uriBuilder.build().toString();

        // Make the GET request for fetch order list
        // Handle the response
        try {
            String responseBody = webClient.get()
                    .uri(builder -> builder
                            .path(tokenEndpoint)
                            .queryParam("lastChangedFrom", "{date}")
                            .queryParam("lastChangedType", "{type}")
                            .build(formattedMidnight, dto.type()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // blocking call, handle this appropriately in a reactive environment

            // Handle the response

            JSONObject mainObject = new JSONObject(responseBody);
            JSONArray lastChangeStatuses = new JSONArray();
            if (mainObject.has("data")) {
                JSONObject dataObject = mainObject.getJSONObject("data");
                lastChangeStatuses = dataObject.getJSONArray("lastChangeStatuses");
            }
            return lastChangeStatuses;
        } catch (WebClientResponseException e) {
            HttpHeaders headers = e.getHeaders();
            throw e;  // Re-throw the exception if needed
        }
    }

    public JSONArray findPaySettle(PaySettleDto dto) throws JSONException {
        String token = getToken();
        // Set the request URL
        String baseUrl = "https://api.commerce.naver.com/external";
        String endpoint = "/v1/pay-settle/settle/daily";

        // Create WebClient instance
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token) // Bearer 토큰 추가
                .build();

        UriBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl)
                .path(endpoint)
                .queryParam("startDate", dto.startDate())
                .queryParam("endDate", dto.endDate())
                .queryParam("pageNumber", dto.pageNumber())
                .queryParam("pageSize", dto.pageSize());

        String builtUri = uriBuilder.build().toString();

        // Make the GET request for fetch order list
        // Handle the response
        try {
            String responseBody = null;
            if (dto.startDate() != null) {
                responseBody = webClient.get()
                        .uri(builder -> builder
                                .path(endpoint)
                                .queryParam("startDate", dto.startDate())
                                .queryParam("endDate", dto.endDate())
                                .queryParam("pageNumber", dto.pageNumber())
                                .queryParam("pageSize", dto.pageSize())
                                .build())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            }
            // Handle the response

            JSONObject mainObject = new JSONObject(responseBody);
            JSONArray paySettle = new JSONArray();
            if (mainObject.has("elements")) {
                paySettle = mainObject.getJSONArray("elements");
            }
            return paySettle;
        } catch (WebClientResponseException e) {
            HttpHeaders headers = e.getHeaders();
            throw e;  // Re-throw the exception if needed
        }
    }

    private String createSignatureByUsingNativeJava(Long timestamp) {
        String appId = properties.getApplicationId();
        String appSecret = properties.getApplicationSecret();
        StringJoiner joiner = new StringJoiner("_");
        joiner.add(appId);
        joiner.add(String.valueOf(timestamp));
        joiner.add(appSecret);
        String password = joiner.toString();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            for (byte hashByte : hashBytes) {
                stringBuilder.append(String.format("%02x", hashByte));
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return stringBuilder.toString();
    }

}
