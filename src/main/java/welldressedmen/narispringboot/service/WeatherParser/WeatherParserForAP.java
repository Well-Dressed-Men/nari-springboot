package welldressedmen.narispringboot.service.WeatherParser;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import welldressedmen.narispringboot.Exception.TemporalErrorException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static welldressedmen.narispringboot.service.WeatherService.*;


@Slf4j
public class WeatherParserForAP {
    public static void parseWeatherDataForAP(String resData, short regionId) {
        JSONObject jObject = new JSONObject(resData);
        JSONObject response = jObject.getJSONObject("response");

//        log.info("[in WeatherParserForAP] response = {}", response);

        JSONObject header = response.getJSONObject("header");
        String resultCode = header.getString("resultCode");

        if(!resultCode.equals("00")) throw new TemporalErrorException();
        /*
            예외 발생 가능()
            response = {"header":{"resultCode":"03","resultMsg":"NO_DATA"}} - 발생상황 : 일시적 발생 가능 오류
            response = {"header":{"resultCode":"99","resultMsg":"UNKOWN_ERROR"}} //에러메세지는 다를수도 있음(현재 실제 발생하는 resultMsg와 api명세에 적혀있는 메세지가 다른 상태(실제 : NO_DATA, 명세 : NODATA_ERROR));
            response = {"header":{"resultCode":"21","resultMsg":"TEMPORARILY_DISABLE_THE_SERVICEKEY_ERROR"}} - 발생상황 : 일시적 발생 가능 오류
         */
        JSONObject body = response.getJSONObject("body");
        JSONArray items = body.getJSONArray("items");

        short[] key = new short[3]; //regionId, 발표날짜, 발표시각
        short[][] value = new short[1][4]; //'예보날짜', '예보시각', 미세먼지농도, 초미세먼지농도

        //키 설정
        JSONObject firstObj = items.getJSONObject(0);
        key[0] = regionId;

        log.info("대기오염정보조회 제공시간 : {}", firstObj.getString("dataTime"));
        String date = firstObj.getString("dataTime").split(" ")[0];
        short baseDate = (short) (Integer.parseInt(date.split("-")[1])*100+Integer.parseInt(date.split("-")[2])); //"2023-09-27" -> 927
        key[1] = baseDate;

        String time = firstObj.getString("dataTime").split(" ")[1];
        int hour = time.split(":")[0].equals("24") ? 0 : Integer.parseInt(time.split(":")[0]);
        short baseTime = (short) (hour*100+Integer.parseInt(time.split(":")[1])); // "19:00" -> 1900, 값 경우의수(4종류) : 0, 30, 100, 2330
        key[2] = baseTime;

        //value 설정
        value[0][0] = baseDate; //value[0][0] : fcstDate
        value[0][1] = baseTime; //value[0][1] : fcstTime
        value[0][2] = (short) firstObj.getInt("pm10Value"); //value[0][2] :
        value[0][3] = (short) firstObj.getInt("pm25Value"); //value[0][3] :

        weatherAP.put(key, value);
    }
}
