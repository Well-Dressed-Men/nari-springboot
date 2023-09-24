package welldressedmen.narispringboot.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseDTO {
    private WeatherResponse weatherResponse;
    private String message;
}
