package welldressedmen.narispringboot.dto;


import lombok.*;
import welldressedmen.narispringboot.domain.Weather;

import javax.persistence.Embeddable;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Embeddable //대응하는 DB테이블이 만들어지지 않도록
public class WeatherInfo {

    private List<Weather.UltraShort> weatherUltraShort;
    private List<Weather.Short> weatherShort;
    private List<Weather.Mid> weatherMid;
    private List<Weather.AP> weatherAP;

}