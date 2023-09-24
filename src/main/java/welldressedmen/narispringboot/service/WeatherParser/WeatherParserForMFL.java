package welldressedmen.narispringboot.service.WeatherParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static welldressedmen.narispringboot.service.WeatherService.weatherMidLand;


public class WeatherParserForMFL {
    /*
        발표날짜 기준 3~6일후의 오전강수확률, 오후 강수확률 데이터를 추출해서 wheatherMidLand에 저장
        (발표날짜 기준 1~2일후의 오전강수확률, 오후 강수확률 데이터는 단기예보로부터 추출해서 wheatherMidLand에 저장)
    */
    public static void parseWeatherDataForMFL(String resData, short regionId) { //
        JSONObject jObject = new JSONObject(resData);
        JSONObject response = jObject.getJSONObject("response");
        JSONObject body = response.getJSONObject("body");
        JSONObject items = body.getJSONObject("items");
        JSONArray jArray = items.getJSONArray("item");

        short baseDate, baseTime;
        short fcstDate,           rainAm, rainPm, skyAm, skyPm;

        short[] key = new short[3]; //regionId, 발표날짜, 발표시각
        short[][] value = new short[4][5]; //'예보날짜',       , 오전강수확률, 오후강수확률, 오전하늘상태, 오후하늘상태

        String baseDateTime = getStdTimeStringForMF(); //ex : 202308270600

        //키 설정
        key[0] = regionId;
        baseDate = (short) (Long.parseLong(baseDateTime)%100000000/10000); //뒤 5~8번째 숫자 추출 -> 827
        key[1] = baseDate;
        baseTime = (short) (Long.parseLong(baseDateTime) % 10000);//뒤 4자리 추출 -> 600
        key[2] = baseTime;

        JSONObject obj = jArray.getJSONObject(0);
        System.out.println("obj: " + obj);

        //value설정
        setFcstDateForMF(value); //value배열에 예보날짜 정보 세팅

        for (int day = 3; day <= 6; day++) {
            //오전, 오후 강수확률 정보 parsing
            String keyForRainAm = "rnSt" + day + "Am";
            String keyForRainPm = "rnSt" + day + "Pm";
            rainAm = (short) obj.getInt(keyForRainAm);;
            rainPm=(short) obj.getInt(keyForRainPm);;
            value[day - 3][1] = rainAm;
            value[day - 3][2] = rainPm;

            //오전, 오후 하늘상태 정보 parsing
            String keyForSkyAm = "wf" + day + "Am";
            String keyForSkyPm = "wf" + day + "Pm";
            skyAm = changeToWeatherCode(obj.getString(keyForSkyAm));
            skyPm = changeToWeatherCode(obj.getString(keyForSkyPm));
            value[day - 3][3] =skyAm;
            value[day - 3][4] =skyPm;
        }
        weatherMidLand.put(key, value);
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

    static short changeToWeatherCode(String skyAm){
        switch(skyAm){
            case "맑음" :
                return 10;
            case "소나기" :
                return 14;
            //15, 16, 17 코드는 단기예보->WM(중기날씨정보)에서만 추출할 수 있는 정보로 MFT파싱에서는 해당 코드를 return하지 않는다.
            case "구름많음" :
                return 30;
            case "구름많고 비" :
                return 31;
            case "구름많고 눈" :
                return 32;
            case "구름많고 비/눈" :
                return 33;
            case "구름많고 소나기" :
                return 34;
            case "흐림" :
                return 40;
            case "흐리고 비" :
                return 41;
            case "흐리고 눈" :
                return 42;
            case "흐리고 비/눈" :
                return 43;
            case "흐리고 소나기" :
                return 44;
        }
        return -1; //error
    };
}
