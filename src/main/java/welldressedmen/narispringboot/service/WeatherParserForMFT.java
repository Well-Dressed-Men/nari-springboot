package welldressedmen.narispringboot.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static welldressedmen.narispringboot.service.WeatherService.weatherMidTemp;

@Component
public class WeatherParserForMFT {
    /*
        발표날짜 기준 3~6일후의 최저기온, 최고기온 데이터를 추출해서 wheatherMidTemp에 저장
        (발표날짜 기준 1~2일후의 최저기온, 최고기온 데이터는 단기예보로부터 추출해서 wheatherMidTemp에 저장)
    */
    public void parse(String resData, short regionId) {
        JSONObject response = new JSONObject(resData).getJSONObject("response");
        JSONArray items = response.getJSONObject("body").getJSONObject("items").getJSONArray("item");
        JSONObject firstItem = items.getJSONObject(0);

        short[] key = buildKeyForMFT(regionId);
        short[][] value = buildValueForMFT(firstItem);

        weatherMidTemp.put(key, value);
    }

    private static short[] buildKeyForMFT(short regionId) {
        String baseDateTime = getStdTimeStringForMF();
        short baseDate = (short) (Long.parseLong(baseDateTime.substring(4, 8)));
        short baseTime = (short) (Long.parseLong(baseDateTime.substring(8, 12)));

        return new short[] {regionId, baseDate, baseTime};
    }

    private static short[][] buildValueForMFT(JSONObject firstItem) {
        short[][] value = setFcstDateForMF(new short[4][3]);

        for (int day = 3; day <= 6; day++) {
            value[day - 3][1] = parseTemperature(firstItem, "taMin" + day);
            value[day - 3][2] = parseTemperature(firstItem, "taMax" + day);
        }

        return value;
    }

    private static short parseTemperature(JSONObject item, String key) {
        return (short) (item.getInt(key) * 10);
    }



    //MF기준 기준시각 문자열 return ex : 202308270600
    static String getStdTimeStringForMF(){
        LocalDateTime now = LocalDateTime.now();
        /*
        6,18시 발표자료를
        6:00, 18:00 부터 제공(확인 필요)(API명세에는 따로 언급X)
         */
        if((now.getHour()+6)%12!=0){
            now = now.minusHours((now.getHour()+6)%12); //기준시각을 6:00, 18:00으로 조정
        }

        String hour = String.valueOf(now.getHour());
        hour = hour.length()<2 ? "0"+hour : hour;
        String min = "00";

        String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = hour+min;

        String stdTimeString = baseDate+baseTime;
        return stdTimeString;
    };

    /* MF로부터 얻은 정보를 value배열에 예보날짜 정보 세팅 */
    static short[][] setFcstDateForMF(short[][] value){
        LocalDate stdTime = getStdTimeForMF();

        for(int day=3;day<=6;day++){
            stdTime.plusDays(day); //day일 후의 날짜 계산 for(0731->0803, 1231->0103)
            short dayAfterN = Short.parseShort(stdTime.format(DateTimeFormatter.ofPattern("MMdd"))); //월일 정보만 추출
            value[day-3][0] = dayAfterN;
        }
        return value;
    };

    static LocalDate getStdTimeForMF(){
        LocalDateTime stdTime = LocalDateTime.now();
        /*
        6,18시 발표자료를
        6:00, 18:00 부터 제공(확인 필요)(API명세에는 따로 언급X)
         */
        if((stdTime.getHour()+6)%12!=0){
            stdTime = stdTime.minusHours((stdTime.getHour()+6)%12); //기준시각을 6:00, 18:00으로 조정
        }
        return stdTime.toLocalDate();
    }

}
