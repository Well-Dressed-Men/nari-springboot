package welldressedmen.narispringboot.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseDTO {
    private WeatherInfo weatherInfo;
    private FashionInfo fashionInfo;
    private String version;
    private String message;
}
