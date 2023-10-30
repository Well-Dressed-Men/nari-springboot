package welldressedmen.narispringboot.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendRequest {
    private WeatherInfo weatherInfo;
    private MemberInfo memberInfo;

}
