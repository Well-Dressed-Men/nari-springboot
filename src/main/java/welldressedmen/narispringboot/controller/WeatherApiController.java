package welldressedmen.narispringboot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherApiController {
    private final EntityManager em;
    @Autowired
    private WeatherService weatherService;

    @GetMapping
    @Transactional
    public ResponseEntity<ResponseDTO> getTotalInfo(@RequestParam Long regionId) throws UnsupportedEncodingException, IOException {


        Region region = em.find(Region.class, regionId); // 날씨 정보를 요청한 지역 조회

        Map<String, Object> result =  weatherService.getRegionWeather(region);
        List<Weather.UltraShort> WUS = (List<Weather.UltraShort>) result.get("weatherUltraShort");
        List<Weather.Short> WS = (List<Weather.Short>) result.get("weatherShort");
        List<Weather.Mid> WM = (List<Weather.Mid>) result.get("weatherMid");

        WeatherResponse response = WeatherResponse.builder()
                .weatherUltraShort(WUS)
                .weatherShort(WS)
                .weatherMid(WM).build();

        ResponseDTO responseDTO = ResponseDTO.builder()
                .weatherResponse(response)
                .message("날씨 정보 요청 성공")
                .build();

//        weatherService.getFashion();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDTO);
    }

}