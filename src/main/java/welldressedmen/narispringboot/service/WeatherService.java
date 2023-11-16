package welldressedmen.narispringboot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import welldressedmen.narispringboot.domain.Region;
import welldressedmen.narispringboot.domain.Weather;
import welldressedmen.narispringboot.dto.WeatherInfo;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static welldressedmen.narispringboot.service.WeatherRequest.*;
import static welldressedmen.narispringboot.service.WeatherResponseSet.*;
import static welldressedmen.narispringboot.service.WeatherServiceUtility.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherService {
    public static Map<short[], short[][]> weatherUltraShort = new ConcurrentHashMap<>(); // key : 지역고유번호, 발표날짜, 발표시각
                                                                                         // value : 예보날짜, 예보시간, 기온, 강수량, 풍속, 습도, 강수형태+하늘, 강수확률
    public static Map<short[], short[][]> weatherShort = new ConcurrentHashMap<>(); // key : 지역고유번호, 발표날짜, 발표시각
                                                                                    // value : 예보날짜, 예보시간, 기온, 강수량, 풍속, 습도, 강수형태+하늘, 강수확률
    public static Map<short[], short[][]> weatherMidLand = new ConcurrentHashMap<>(); // key : 지역고유번호, 발표날짜, 발표시각
                                                                                      // value : 예보날짜,      오전강수확률+오후강수확률
    public static Map<short[], short[][]> weatherMidTemp = new ConcurrentHashMap<>(); // key : 지역고유번호, 발표날짜, 발표시각
                                                                                       // value : 예보날짜,      최저기온, 최고온도
    public static Map<short[], short[][]> weatherAP = new ConcurrentHashMap<>(); // key : 지역고유번호, 발표날짜, 발표시각
                                                                                 // value : 예보날짜, 예보시각, 미세먼지농도, 초미세먼지농도
    private final WeatherParserForAP parserForAP;

    private final WeatherParserForUSN parserForUSN;
    private final WeatherParserForUSF parserForUSF;
    private final WeatherParserForVF parserForVF;
    private final WeatherParserForMFL parserForMFL;
    private final WeatherParserForMFT parserForMFT;

    @Value("${weatherApi.serviceKey}")
    private String serviceKey;
    public WeatherInfo getRegionWeather(Region region) throws IOException{
        getUSN(region, serviceKey);
        getUSF(region, serviceKey);
        getVF(region, serviceKey);
        getMFLand(region, serviceKey);
        getMFTemp(region, serviceKey);
        getAP(region, serviceKey);

        List<Weather.UltraShort> WUS = createUltraShortWeatherList(region.getId()); // 초단기 날씨정보 세팅
        List<Weather.Short> WS = createShortWeatherList(region.getId()); // 단기 날씨정보 세팅
        List<Weather.Mid> WM = createMidWeatherList(region.getId()); // 중기 예보 세팅
        List<Weather.AP> WAP = createAirPollutionList(region.getId()); // 미세먼지 정보 세팅

        return WeatherInfo.builder()
                .weatherUltraShort(WUS)
                .weatherShort(WS)
                .weatherMid(WM)
                .weatherAP(WAP)
                .build();
    }

    private void getUSN(Region region, String serviceKey) throws IOException {
        //1.데이터 보유 확인
        if(haveAlready(region.getId(), "USN")) return;
        //2.요청 url설정
        URL url = setURL(serviceKey, region, "USN");
        //3.요청
        String resData = requestWeather(url);
        //4.날씨저장
        parserForUSN.parse(resData, region.getId());
    };

    private void getUSF(Region region, String serviceKey) throws IOException{
        //1.데이터 보유 확인
        if(haveAlready(region.getId(), "USF")) return;
        //2.요청 url설정
        URL url = setURL(serviceKey, region, "USF");
        //3.요청
        String resData = requestWeather(url);
        //4.날씨저장
        parserForUSF.parse(resData, region.getId());
    };


    private void getVF(Region region, String serviceKey) throws IOException{
        //1.데이터 보유 확인
        if(haveAlready(region.getId(), "VF")) return;
        //2.요청 url설정
        URL url = setURL(serviceKey, region, "VF");
        //3.요청
        String resData = requestWeather(url);
        //4.날씨저장
        parserForVF.parse(resData, region.getId());
    };

    private void getMFLand(Region region, String serviceKey) throws IOException{
        //1.데이터 보유 확인
        if(haveAlready(region.getId(), "MFL")) return;
        //2.요청 url설정
        URL url = setURL(serviceKey, region, "MFL");
        //3.요청
        String resData = requestWeather(url);
        //4.날씨저장
        parserForMFL.parse(resData, region.getId());
    };

    private void getMFTemp(Region region, String serviceKey) throws IOException{
        //1.데이터 보유 확인
        if(haveAlready(region.getId(), "MFT")) return;
        //2.요청 url설정
        URL url = setURL(serviceKey, region, "MFT");
        //3.요청
        String resData = requestWeather(url);
        //4.날씨저장
        parserForMFT.parse(resData, region.getId());
    };
    static boolean haveAlready(short regionId, String type){
        //기준시각 설정
        short[] stdTime = null;
        switch(type){
            case "USN" :
                stdTime = getStdTimeKeyForUSN(regionId);
                //해당 지역날씨, 기준시각 데이터 있는지 확인
                if(!isExists(weatherUltraShort, stdTime)) return false;
                break;
            case "USF" :
                stdTime = getStdTimeKeyForUSF(regionId);
                //해당 지역날씨, 기준시각 데이터 있는지 확인
                if(!isExists(weatherUltraShort, stdTime)) return false;
                break;
            case "VF" :
                stdTime = getStdTimeKeyForVF(regionId);
                //해당 지역날씨, 기준시각 데이터 있는지 확인
                if(!isExists(weatherShort, stdTime)) return false;
                break;
            case "MFL" : //중기육상조회
                stdTime = getStdTimeKeyForMF(regionId);
                //해당 지역날씨, 기준시각 데이터 있는지 확인
                if(!isExists(weatherMidLand, stdTime)) return false;
                break;
            case "MFT" : //중기기온조회
                stdTime = getStdTimeKeyForMF(regionId);
                //해당 지역날씨, 기준시각 데이터 있는지 확인
                if(!isExists(weatherMidTemp, stdTime)) return false;
                break;
            case "AP" : //대기오염조회
                stdTime = getStdTimeKeyForAP(regionId);
                //해당 지역날씨, 기준시각 데이터 있는지 확인
                if(!isExists(weatherAP, stdTime)) return false;
                break;
        }
        return true;
    };

    //대기오염 정보 조회(Air Pollution)
    private void getAP(Region region, String serviceKey) throws IOException {
        //1.데이터 보유 확인
        if(haveAlready(region.getId(), "AP")) return;
        //2.요청 url설정
        URL url = setURLForAP(serviceKey, region);
        //3.요청
        String resData = requestWeather(url);
        //4.날씨저장
        parserForAP.parse(resData, region.getId());
    }

}
