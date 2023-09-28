package welldressedmen.narispringboot.service.WeatherParser;

import org.json.JSONArray;
import org.json.JSONObject;
import welldressedmen.narispringboot.Exception.TemporalErrorException;

import static welldressedmen.narispringboot.service.WeatherService.weatherUltraShort;


public class WeatherParserForUSN {
    public static void parseWeatherDataForUSN(String resData, short regionId){
        JSONObject jObject = new JSONObject(resData);
        JSONObject response = jObject.getJSONObject("response");
//        System.out.println("response : "+response);
        JSONObject header = response.getJSONObject("header");
        String resultCode = header.getString("resultCode");

        if(!resultCode.equals("00")) throw new TemporalErrorException();
        /*
            예외 발생 가능
            response = {"header":{"resultCode":"03","resultMsg":"NO_DATA"}} - 발생상황 : 일시적 발생 가능 오류
            response = {"header":{"resultCode":"99","resultMsg":"UNKOWN_ERROR"}} //에러메세지는 다를수도 있음(현재 실제 발생하는 resultMsg와 api명세에 적혀있는 메세지가 다른 상태(실제 : NO_DATA, 명세 : NODATA_ERROR));
            response = {"header":{"resultCode":"21","resultMsg":"TEMPORARILY_DISABLE_THE_SERVICEKEY_ERROR"}} - 발생상황 : 일시적 발생 가능 오류
         */
        JSONObject body = response.getJSONObject("body");
        JSONObject items = body.getJSONObject("items");
        JSONArray jArray = items.getJSONArray("item");

        short baseDate, baseTime;
        short fcstDate, fcstTime, temp, rainAmount, windSpeed, humid=0, rainType= 0, rainPercentage;

        short[] key = new short[3]; //regionId, 발표날짜, 발표시각
        short[][] value = new short[1][8]; //'예보날짜', '예보시각', '기온' '강수량' '풍속' '습도' '강수형태+하늘' 강수확률. ''-> 초단기실황조회에서 가져올 정보


        for(int i = 0; i < jArray.length(); i++) {
            JSONObject obj = jArray.getJSONObject(i);
//            System.out.println("obj: " + obj);

            key[0] = regionId;
            baseDate = (short) (obj.getInt("baseDate") % 10000); //뒤 네자리 추출
            key[1] = baseDate;
            baseTime = (short) (obj.getInt("baseTime"));
            key[2] = baseTime;
            fcstDate = baseDate;
            value[0][0] = fcstDate;
            fcstTime = baseTime;
            value[0][1] = fcstTime;

            String category = obj.getString("category");
            //가져오고자하는 값이 아니면 등록하지 않는다.
            if(!(category.equals("T1H") || category.equals("RN1")|| category.equals("WSD")|| category.equals("REH")|| category.equals("PTY"))) continue;
            switch (category) {
                case "T1H": //기온
                    temp = (short)(obj.getDouble("obsrValue")*10);
                    value[0][2] = temp;
                    break;
                case "RN1": //강수량
                    String rainAmountData = obj.getString("obsrValue");
                    switch (rainAmountData) {
                        case "0" :
                            rainAmount = 0;
                            break;
                        case "1.0mm 미만":
                            rainAmount = 1; //현재 임의로 정한 값
                            break;
                        case "30.0~50.0mm":
                            rainAmount = 40; //현재 임의로 정한 값
                            break;
                        case "50.0mm 이상":
                            rainAmount = 65; //현재 임의로 정한 값
                            break;
                        default:
                            rainAmount = 15; //현재 임의로 정한 값
                    }
                    value[0][3] = rainAmount;
                    break;
                case "WSD": //풍속
                    windSpeed = (short)(obj.getDouble("obsrValue")*10);
                    value[0][4] = windSpeed;
                    break;
                case "REH": //습도
                    humid = (short)((obj.getInt("obsrValue")));
                    value[0][5] = humid;
                    break;
                case "PTY": //(하늘+)강수형태
                    rainType = (short) obj.getInt("obsrValue"); //초단기실황조회에서는 "SKY" category가 없음
                    value[0][6] += rainType;
            }
        }
        weatherUltraShort.put(key, value);
    }
}
