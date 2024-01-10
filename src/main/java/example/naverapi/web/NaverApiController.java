package example.naverapi.web;

import example.naverapi.service.NaverApiService;
import example.naverapi.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/naver-api")
@RequiredArgsConstructor
public class NaverApiController {
    private final NaverApiService service;

    @GetMapping("/auth/signature")
    ResponseEntity<NaverResponse<SignatureDto>> getSignature() {
        SignatureDto signature = service.getSignature();
        return ResponseEntity.ok(new NaverResponse<>(HttpStatus.OK.value(), "SUCCESS", signature, null));
    }

    @GetMapping("/auth/token")
    ResponseEntity<NaverResponse<String>> getToken() {
        String findToken = service.getToken();
        return ResponseEntity.ok(new NaverResponse<>(HttpStatus.OK.value(), "SUCCESS", findToken, null));
    }
    @GetMapping("/orders/{orderId}")
    ResponseEntity<NaverResponse<String>> findSingleOrder(@PathVariable(value = "orderId") String orderId) {
        JSONArray singleOrder = service.findSingleOrder(orderId);
        return ResponseEntity.ok(new NaverResponse<>(HttpStatus.OK.value(), "SUCCESS", singleOrder.toString(), singleOrder.length()));
    }

    @PostMapping("/orders/orderItems")
    ResponseEntity<NaverResponse<String>> findOrderItems(@RequestBody OrderIdDto dto) {
        System.out.println("NaverApiController.findOrderItems");
        System.out.println("dto = " + dto);
        JSONArray singleOrder = service.findOrderItems(dto);
        return ResponseEntity.ok(new NaverResponse<>(HttpStatus.OK.value(), "SUCCESS", singleOrder.toString(), singleOrder.length()));
    }

    @PostMapping("/orders")
    ResponseEntity<NaverResponse<String>> findOrders(@RequestBody OrderSearchDto dto) throws Exception {
        JSONArray array = service.findOrders(dto);
        System.out.println("size = " + array.length());
        for (Object o : array) {
            System.out.println("order = " + o);
        }
        return ResponseEntity.ok(new NaverResponse<>(HttpStatus.OK.value(), "SUCCESS", array.toString(), array.length()));
    }

    @PostMapping("/pay-settle")
    ResponseEntity<NaverResponse<String>> findPaySettle(@RequestBody PaySettleDto dto) {
        JSONArray paySettle = service.findPaySettle(dto);
        return ResponseEntity.ok(new NaverResponse<>(HttpStatus.OK.value(), "SUCCESS", paySettle.toString(), paySettle.length()));
    }
}
