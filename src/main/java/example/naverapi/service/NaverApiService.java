package example.naverapi.service;

import example.naverapi.config.NaverProperties;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
public class NaverApiService {
    private final NaverProperties properties;
    public String showIdAndSecret() {
        return properties.getApplicationId() + ", " + properties.getApplicationSecret();
    }

//    public String getToken() {
//        String appId = properties.getApplicationId();
//        Long currentTime = System.currentTimeMillis();
//        String clientSecretSign = createSignature(currentTime);
//        String grantType = "client_credentials";
//        String type = "SELF";
//        String accountId = "wngml613";
//
//        // Set the request URL
//        String baseUrl = "https://api.commerce.naver.com/external";
//        String tokenEndpoint = "/v1/oauth2/token";
//        String url = baseUrl + tokenEndpoint;
//
//        // Create WebClient instance
//        WebClient webClient = WebClient.create();
//
//        // Build the request body
//        String requestBody = "client_id=" + appId +
//                "&timestamp=" + String.valueOf(currentTime) +
//                "&client_secret_sign=" + clientSecretSign +
//                "&grant_type=" + grantType +
//                "&type=" + type +
//                "&account_id=" + accountId;
//
//        System.out.println("requestBody = " + requestBody);
//
//        // Make the POST request for the OAuth2 token
//        String responseBody = webClient.post()
//                .uri(url)
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//                .body(BodyInserters.fromValue(requestBody))
//                .retrieve()
//                .bodyToMono(String.class)
//                .block(); // blocking call, handle this appropriately in a reactive environment
//
//        // Handle the response
//        return responseBody;
//    }

    public String getToken() {
        String clientId = properties.getApplicationId();
        Long currentTime = System.currentTimeMillis();
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
        System.out.println("Built URI: " + builtUri);

        // Make the POST request for the OAuth2 token
        // Handle the response
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
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // blocking call, handle this appropriately in a reactive environment

            // Handle the response
            return responseBody;
        } catch (WebClientResponseException e) {
            HttpHeaders headers = e.getHeaders();
            for (String s : headers.keySet()) {
                System.out.println(s + " = " + headers.get(s));
            }
            // Print error details including status code and response body
            System.out.println("HTTP Status Code: " + e.getRawStatusCode());
            System.out.println("Response Body: " + e.getResponseBodyAsString());
            throw e;  // Re-throw the exception if needed
        }
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
}
