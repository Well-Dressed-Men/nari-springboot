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
import welldressedmen.narispringboot.service.MemberService;
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
    private MemberService memberService;
    @Autowired
    private RecommendService recommendService;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${app.version}")
    private String latestVersion;

    @PostMapping("users")
    public ResponseEntity<ResponseDTO> updateUserInfo(Authentication authentication, @RequestBody UpdateRequestDTO updateRequestDTO) {
        String userId = getAuthenticatedUserId(authentication);
        memberService.updateMemberInfo(updateRequestDTO, userId);
        ResponseDTO responseDTO = buildResponseDTO(null, null, "유저 정보를 성공적으로 저장했습니다");
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    @GetMapping("/version")
    public ResponseEntity<ResponseDTO> checkVersion(double currentVersion) {
        String message = isLatestVersion(currentVersion) ? "최신버전 입니다." : "지난버전 입니다.";
        ResponseDTO responseDTO = buildResponseDTO(null, null, message);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    @GetMapping("weather-clothing-infos")
    public ResponseEntity<ResponseDTO> getTotalInfo(Authentication authentication, @RequestParam short regionId, @RequestParam short nx, @RequestParam short ny, @RequestParam String midLandCode, @RequestParam String midTempCode, @RequestParam String stationName) throws IOException {

        Region region = buildRegion(regionId, nx, ny, midLandCode, midTempCode, stationName);

        WeatherInfo weatherInfo =  weatherService.getRegionWeather(region);

        MemberInfo memberInfo = getMemberInfo(authentication);

        FashionInfo fashionInfo = recommendService.getFashion(weatherInfo, memberInfo);

        ResponseDTO responseDTO = buildResponseDTO(weatherInfo, fashionInfo, "날씨 정보 요청 성공");

        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    private String getAuthenticatedUserId(Authentication authentication) {
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        return principal.getUsername(); //PrincipalDetails는 UserDetils를 상속받기때문에 userId정보를 getUsername으로 얻어야함
    }
    private MemberInfo getMemberInfo(Authentication authentication){
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        String userId = principal.getUsername(); //PrincipalDetails는 UserDetils를 상속받기때문에 userId정보를 getUsername으로 얻어야함
        return memberService.getUserInfo(userId); //userId -> User(사용자정보) -> UserInfo(사용자 정보(패션추천에 필요한))
    }

    private boolean isLatestVersion(double currentVersion){
        return currentVersion == Double.parseDouble(latestVersion);
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

}