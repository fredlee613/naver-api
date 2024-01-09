package example.naverapi.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrderSearchDto(@JsonProperty Integer year, @JsonProperty Integer month, @JsonProperty Integer date, @JsonProperty String type) {
}
