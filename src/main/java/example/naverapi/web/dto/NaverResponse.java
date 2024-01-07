package example.naverapi.web.dto;

public record NaverResponse<T> (Integer status, String code, T data) {}
