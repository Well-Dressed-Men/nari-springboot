package welldressedmen.narispringboot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import welldressedmen.narispringboot.dto.FashionInfo;
import welldressedmen.narispringboot.dto.RecommendRequest;
import welldressedmen.narispringboot.dto.MemberInfo;
import welldressedmen.narispringboot.dto.WeatherInfo;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendService {
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    public FashionInfo getFashion(WeatherInfo weatherInfo, MemberInfo memberInfo){
        WebClient webClient = webClientBuilder.baseUrl("http://13.209.116.90:8000/").build();

        RecommendRequest recommendRequest = RecommendRequest.builder()
                .weatherInfo(weatherInfo)
                .memberInfo(memberInfo)
                .build();

        try {
            String recommendRequestJson = objectMapper.writeValueAsString(recommendRequest);
            log.info("RecommendRequest JSON = {}", recommendRequestJson);
        } catch (Exception e) {
            log.error("Error converting RecommendRequest to JSON", e);
        }

        return webClient.post()
                .uri("/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(recommendRequest), RecommendRequest.class)
                .retrieve() //http요청 시작(by webClient인스턴스)
                .bodyToMono(FashionInfo.class) //서버에서 반환된 응답을 FashionInfo객체에 담아서 return
                .block(); // block()을 사용하여 동기로 Mono의 결과를 얻습니다.(추후 비동기 통신 업데이트 예정)
    }

    public FashionInfo getFashionForTesting(WeatherInfo weatherInfo, MemberInfo memberInfo){
        WebClient webClient = webClientBuilder.baseUrl("http://localhost:8000/").build();

        RecommendRequest recommendRequest = RecommendRequest.builder()
                .weatherInfo(weatherInfo)
                .memberInfo(memberInfo)
                .build();

        return webClient.post()
                .uri("/fashions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(recommendRequest), RecommendRequest.class)
                .retrieve() //http요청 시작(by webClient인스턴스)
                .bodyToMono(FashionInfo.class) //서버에서 반환된 응답을 FashionInfo객체에 담아서 return
                .block(); // block()을 사용하여 동기로 Mono의 결과를 얻습니다.(추후 비동기 통신 업데이트 예정)
    }
}
