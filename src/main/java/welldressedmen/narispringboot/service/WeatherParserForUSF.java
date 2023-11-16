package welldressedmen.narispringboot.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import welldressedmen.narispringboot.exception.TemporalErrorException;

import static welldressedmen.narispringboot.service.WeatherService.weatherUltraShort;

@Component
public class WeatherParserForUSF {
    public void parse(String resData, short regionId) {
        JSONObject jObject = new JSONObject(resData);
        JSONObject response = jObject.getJSONObject("response");

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
        short fcstDate, fcstTime, temp, rainAmount, windSpeed, humid=0, rainType, sky = 0, rainPercentage;


        short[] key = new short[3]; //regionId, 발표날짜, 발표시각
        short[][] value = new short[6][8]; //'예보날짜', '예보시각', '기온' '강수량' '풍속' '습도' '강수형태+하늘' 강수확률

        //키 설정
        JSONObject firstObj = jArray.getJSONObject(0);
        key[0] = regionId;
        baseDate = (short) (firstObj.getInt("baseDate") % 10000); // 뒤 네자리 추출 -  값 경우의수(종류) :
        key[1] = baseDate;
        baseTime = (short) (firstObj.getInt("baseTime")); // 값 경우의수(4종류) : 0, 30, 100, 2330
        key[2] = baseTime;

        int row=0;
        for (int jsonIndex = 0; jsonIndex < jArray.length(); jsonIndex++) {
            JSONObject obj = jArray.getJSONObject(jsonIndex);
//            System.out.println("obj: " + obj);

            fcstDate = (short) (obj.getInt("fcstDate") % 10000); //뒤 네자리 추출
            value[row][0] = fcstDate;
            fcstTime = (short) (obj.getInt("fcstTime"));
            value[row][1] = fcstTime;

            String category = obj.getString("category");
            //가져오고자하는 값이 아니면 등록하지 않는다.
            if(!(category.equals("T1H") || category.equals("RN1")|| category.equals("WSD")|| category.equals("REH")|| category.equals("PTY")|| category.equals("SKY")|| category.equals("POP"))) continue;
            switch (category) {
                case "T1H": //기온
                    temp = (short)(obj.getDouble("fcstValue")*10);
                    value[row][2] = temp;
                    row++;
                    break;
                case "RN1": //강수량
                    String rainAmountData = obj.getString("fcstValue");
                    switch (rainAmountData) {
                        case "강수없음" : //초단기예보에서는 "강수없음", 초단기실황에서는 "0"
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
                    value[row][3] = rainAmount;
                    row++;
                    break;
                case "WSD": //풍속
                    windSpeed = (short)(obj.getDouble("fcstValue")*10);
                    value[row][4] = windSpeed;
                    row++;
                    break;
                case "REH": //습도
                    humid = (short)((obj.getInt("fcstValue")));
                    value[row][5] = humid;
                    row++;
                    break;
                case "SKY": //하늘(+강수형태)
                    sky = (short)(obj.getInt("fcstValue")*10);
                    value[row][6] += sky;
                    row++;
                    break;
                case "PTY": //강수형태(+하늘)
                    rainType = (short)obj.getInt("fcstValue"); //초단기실황조회에서는 "SKY" category가 없음
                    value[row][6] += rainType;
                    row++;
                    break;
                case "POP": //강수확률
                    rainPercentage = (short) obj.getInt("fcstValue");
                    value[row][7] = rainPercentage;
                    row++;
                    break;
            }
            if(row%6==0) row=0;
        }
        weatherUltraShort.put(key, value);
    }
}
