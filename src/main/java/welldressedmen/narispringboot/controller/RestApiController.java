package welldressedmen.narispringboot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import welldressedmen.narispringboot.config.auth.PrincipalDetails;
import welldressedmen.narispringboot.domain.Region;
import welldressedmen.narispringboot.dto.*;
import welldressedmen.narispringboot.service.RecommendService;
import welldressedmen.narispringboot.service.UserService;
import welldressedmen.narispringboot.service.WeatherService;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RestApiController {
    @Autowired
    private WeatherService weatherService;
    @Autowired
    private UserService userService;
    @Autowired
    private RecommendService recommendService;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.version}")
    private String latestVersion;

    @PostMapping("users")
    public ResponseEntity<ResponseDTO> updateUserInfo(Authentication authentication, @RequestBody UpdateRequestDTO updateRequestDTO) {
        try {
            String updateRequestJson = objectMapper.writeValueAsString(updateRequestDTO);
            log.info("RecommendRequest JSON = {}", updateRequestJson);
        } catch (Exception e) {
            log.error("Error converting RecommendRequest to JSON", e);
        }

        //사용자 정보를 saveRequestDTO에 설정
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        String userId = principal.getUsername(); //PrincipalDetails는 UserDetils를 상속받기때문에 userId정보를 getUsername으로 얻어야함
        try {
            String principalJson = objectMapper.writeValueAsString(principal);
            log.info("principal JSON = {}", principalJson);
        } catch (Exception e) {
            log.error("Error converting principal to JSON", e);
        }

        //userSex, userCold, userHot, userPreferences를 저장
        userService.updateMemberInfo(updateRequestDTO, userId);

        ResponseDTO responseDTO = buildResponseDTO(null, null, "유저 정보를 성공적으로 저장했습니다");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDTO);
    }

    @GetMapping("/version")
    public ResponseEntity<ResponseDTO> checkVersion(double currentVersion) {
        ResponseDTO responseDTO;
        if(Double.parseDouble(latestVersion) == currentVersion){
            responseDTO = buildResponseDTO(null, null, "최신버전 입니다.");
        }else{
            responseDTO = buildResponseDTO(null, null, "지난버전 입니다.");
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDTO);
    }

    @GetMapping("weather-clothing-infos")
    public ResponseEntity<ResponseDTO> getTotalInfo(Authentication authentication, @RequestParam short regionId, @RequestParam short nx, @RequestParam short ny, @RequestParam String midLandCode, @RequestParam String midTempCode, @RequestParam String stationName) throws IOException {

        Region region = buildRegion(regionId, nx, ny, midLandCode, midTempCode, stationName);

        WeatherInfo weatherInfo =  weatherService.getRegionWeather(region);

        MemberInfo memberInfo = getMemberInfo(authentication);

        FashionInfo fashionInfo = recommendService.getFashion(weatherInfo, memberInfo);
//        FashionInfo fashionInfo = recommendService.getFashionForTesting(weatherInfo, memberInfo);

        ResponseDTO responseDTO = buildResponseDTO(weatherInfo, fashionInfo, "날씨 정보 요청 성공");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDTO);
    }
    private MemberInfo getMemberInfo(Authentication authentication){
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        String userId = principal.getUsername(); //PrincipalDetails는 UserDetils를 상속받기때문에 userId정보를 getUsername으로 얻어야함
        //userId -> User(사용자정보) -> UserInfo(사용자 정보(추천용))
        return userService.getUserInfo(userId);
    }
    private ResponseDTO buildResponseDTO(WeatherInfo weatherInfo, FashionInfo fashionInfo, String message) {
        return ResponseDTO.builder()
                .weatherInfo(weatherInfo)
                .fashionInfo(fashionInfo)
                .version(latestVersion)
                .message(message)
                .build();
    }
    private static Region buildRegion(short regionId, short nx, short ny, String midLandCode, String midTempCode, String stationName) {
        return Region.builder()
                .id(regionId)
                .nx(nx)
                .ny(ny)
                .midLandCode(midLandCode)
                .midTempCode(midTempCode)
                .stationName(stationName)
                .build();
    }

    private void logObject(Object object) {
        try {
            String objectJson = objectMapper.writeValueAsString(object);
            log.info("objectJson JSON = {}", objectJson);
        } catch (Exception e) {
            log.error("Error converting objectJson to JSON", e);
        }
    }
}