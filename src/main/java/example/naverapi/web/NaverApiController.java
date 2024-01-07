package example.naverapi.web;

import example.naverapi.service.NaverApiService;
import example.naverapi.web.dto.NaverResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/naver-api")
@RequiredArgsConstructor
public class NaverApiController {
    private final NaverApiService service;
    @GetMapping("/findCredential")
    ResponseEntity<NaverResponse<String>> findOrders() {
        String findCredential = service.showIdAndSecret();
        return ResponseEntity.ok(new NaverResponse<>(HttpStatus.OK.value(), "SUCCESS", findCredential));
    }

    @PostMapping("/getToken")
    ResponseEntity<NaverResponse<String>> getToken() {
        String tokenResponse = service.getToken();
        System.out.println("tokenResponse = " + tokenResponse);
        return ResponseEntity.ok(new NaverResponse<>(HttpStatus.OK.value(), "SUCCESS", tokenResponse));
    }
}
