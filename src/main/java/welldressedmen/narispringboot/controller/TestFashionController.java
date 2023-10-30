package welldressedmen.narispringboot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import welldressedmen.narispringboot.config.auth.PrincipalDetails;
import welldressedmen.narispringboot.dto.FashionInfo;
import welldressedmen.narispringboot.dto.ResponseDTO;
import welldressedmen.narispringboot.dto.UpdateRequestDTO;
import welldressedmen.narispringboot.dto.WeatherInfo;

import java.util.Arrays;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class TestFashionController {

    @PostMapping("fashions")
    public ResponseEntity<ResponseDTO> TestRecommendFashion() {
        List<String> recommendedFashions = Arrays.asList(
                "56-h_anchovy_.jpg",
                "58-h_anchovy_.jpg",
                "83-h_anchovy_.jpg",
                "114-h_anchovy_.jpg",
                "5-daily_bang_228.jpg"
        );

        FashionInfo fashionInfo = new FashionInfo(recommendedFashions);

        ResponseDTO responseDTO = buildResponseDTO(fashionInfo, "추천 패션 정보입니다");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDTO);
    }

    private ResponseDTO buildResponseDTO(FashionInfo fashionInfo, String message) {
        return ResponseDTO.builder()
                .fashionInfo(fashionInfo)
                .message(message)
                .build();
    }

}
