package welldressedmen.narispringboot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import welldressedmen.narispringboot.domain.Region;
import welldressedmen.narispringboot.domain.Weather;
import welldressedmen.narispringboot.dto.ResponseDTO;
import welldressedmen.narispringboot.dto.WeatherResponse;
import welldressedmen.narispringboot.service.WeatherService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class WeatherApiController {
    @Autowired
    private WeatherService weatherService;

    @Value("${app.version}")
    private String appVersion;

    @GetMapping("weather-clothihng-infos")
    @Transactional
    public ResponseEntity<ResponseDTO> getTotalInfo(@RequestParam short regionId, @RequestParam short nx, @RequestParam short ny, @RequestParam String midLandCode, @RequestParam String midTempCode, @RequestParam String stationName, @RequestParam String ver) throws IOException {

        //최초로그인이거나 최신버전이 아닐 경우 -> 지역정보를 새로 다운받게 한다(by 버전다름 res보냄 -> 프론트에서 지역정보다운로드api에 요청을 보냄 -> 새로운 지역정보 서버 제공 -> 해당정보를 토대로 재요청)

        Region region =  Region.builder()
                .id(regionId)
                .nx(nx)
                .ny(ny)
                .midLandCode(midLandCode)
                .midTempCode(midTempCode)
                .stationName(stationName)
                .build();

        Map<String, Object> result =  weatherService.getRegionWeather(region);
        List<Weather.UltraShort> WUS = (List<Weather.UltraShort>) result.get("weatherUltraShort");
        List<Weather.Short> WS = (List<Weather.Short>) result.get("weatherShort");
        List<Weather.Mid> WM = (List<Weather.Mid>) result.get("weatherMid");
        List<Weather.AP> WAP = (List<Weather.AP>) result.get("weatherAP");

        WeatherResponse response = WeatherResponse.builder()
                .weatherUltraShort(WUS)
                .weatherShort(WS)
                .weatherMid(WM)
                .weatherAP(WAP)
                .build();

        ResponseDTO responseDTO = ResponseDTO.builder()
                .weatherResponse(response)
                .version(appVersion)
                .message("날씨 정보 요청 성공")
                .build();

//        weatherService.getFashion();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDTO);
    }

}