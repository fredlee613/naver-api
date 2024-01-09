package example.naverapi.web.dto;

import jakarta.annotation.Nullable;

public record NaverResponse<T> (Integer status, String code, T data, @Nullable Integer size) {
    public NaverResponse(Integer status, String code, T data) {
        this(status, code, data, null);
    }
}
