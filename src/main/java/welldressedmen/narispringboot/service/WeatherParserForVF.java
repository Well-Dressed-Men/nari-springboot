package welldressedmen.narispringboot.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import welldressedmen.narispringboot.exception.TemporalErrorException;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static welldressedmen.narispringboot.service.WeatherService.*;

@Slf4j
@Component
public class WeatherParserForVF {
    public void parse(String resData, short regionId) {
        JSONObject jObject = new JSONObject(resData);
        JSONObject response = jObject.getJSONObject("response");

        JSONObject header = response.getJSONObject("header");
        String resultCode = header.getString("resultCode");
        String resultMsg = header.getString("resultMsg");

        if(!resultCode.equals("00")){
            log.info("resultCode = {}, resultMsg = {}", resultCode, resultMsg);
            throw new TemporalErrorException();
        }
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
        short fcstDate=0, fcstTime=0, temp, rainAmount, windSpeed, humid=0, rainType = 0, sky = 0, rainPercentage;

        short[] keyForWS = new short[3]; //regionId, 발표날짜, 발표시각
        short[][] valueForWS = new short[25][8]; //'예보날짜', '예보시각', '(1시간)기온' '강수량' '풍속' '습도' '강수형태+하늘' 강수확률

        short[] keyForWML = new short[3]; //regionId, 발표날짜, 발표시각
        short[][] valueForWML = new short[2][5]; //'예보날짜',         , 오전강수확률, 오후강수확률, 오전하늘상태, 오후하늘상태

        short[] keyForWMT = new short[3]; //regionId, 발표날짜, 발표시각
        short[][] valueForWMT = new short[2][3]; //'예보날짜',         , 최저기온, 최고기온

        //키 설정
        JSONObject firstObj = jArray.getJSONObject(0);
        keyForWS[0] = regionId;
        keyForWML[0] = regionId;
        keyForWMT[0] = regionId;

        baseDate = (short) (firstObj.getInt("baseDate") % 10000); //뒤 4자리 추출
        keyForWS[1] = baseDate;
        keyForWML[1] = baseDate;
        keyForWMT[1] = baseDate;

        baseTime = (short) (firstObj.getInt("baseTime"));
        keyForWS[2] = baseTime;
        keyForWML[2] = baseTime;
        keyForWMT[2] = baseTime;

        short currentDay=0;
        short change=-1;
        int row=0;

        short rainPercentageAmongAmAfterOneday = Short.MIN_VALUE;;
        short rainPercentageAmongPmAfterOneday = Short.MIN_VALUE;;
        short rainPercentageAmongAmAfterTwoday = Short.MIN_VALUE;;
        short rainPercentageAmongPmAfterTwoday = Short.MIN_VALUE;;

        short tempLowestAfterOneday = Short.MAX_VALUE;
        short tempHighestAfterOneday = Short.MIN_VALUE;
        short tempLowestAfterTwoday = Short.MAX_VALUE;;
        short tempHighestAfterTwoday = Short.MIN_VALUE;

        short[][] skyRainTypeCountAmAfterOneday = new short[2][8]; //[0] -> skyCount, [1]->rainType Count
        short[][] skyRainTypeCountPmAfterOneday = new short[2][8];
        short[][] skyRainTypeCountAmAfterTwoday = new short[2][8];
        short[][] skyRainTypeCountPmAfterTwoday = new short[2][8];

        for (int jsonIndex = 0; jsonIndex < jArray.length(); jsonIndex++) {

            JSONObject obj = jArray.getJSONObject(jsonIndex);
//            System.out.println("obj: " + obj);

            if(jsonIndex%12==0){ //특정 시각에 관한 예측값을 모두 저장했을때, 다음 시각정보를 저장
                fcstDate = (short) (obj.getInt("fcstDate") % 10000);
                fcstTime = (short) (obj.getInt("fcstTime"));

                if(row<25){ //현재시각+25시간후의 날씨정보만 WS에 저장한다
                    valueForWS[row][0] = fcstDate;
                    valueForWS[row][1] = fcstTime;
                }

                if(currentDay != fcstDate){
                    currentDay = fcstDate;
                    change++;
                }
            }

            if(change==3){ //현재날짜+3일후의 데이터는 집계하지 않음(MFL, MFT로부터 간편하게 얻음)
                short skyRainTypeAmAfterOneday = getMostFrom(skyRainTypeCountAmAfterOneday); //1일후 오전 하늘상태+강수형태에서 max찾기
                short skyRainTypePmAfterOneday = getMostFrom(skyRainTypeCountPmAfterOneday); //1일후 오전 하늘상태+강수형태에서 max찾기
                short skyRainTypeAmAfterTwoday = getMostFrom(skyRainTypeCountAmAfterTwoday); //2일후 오후 하늘상태+강수형태에서 max찾기
                short skyRainTypePmAfterTwoday = getMostFrom(skyRainTypeCountPmAfterTwoday); //2일후 오후 하늘상태+강수형태에서 max찾기

                //WML에 put할 임시배열 값 세팅
                valueForWML = setFcstDateForVF(valueForWML); //날짜값 세팅
                valueForWML = setRainAmAndPmForVF(valueForWML, rainPercentageAmongAmAfterOneday, rainPercentageAmongPmAfterOneday, rainPercentageAmongAmAfterTwoday, rainPercentageAmongPmAfterTwoday);
                valueForWML = setSkyAmAndPmForVF(valueForWML, skyRainTypeAmAfterOneday, skyRainTypePmAfterOneday, skyRainTypeAmAfterTwoday, skyRainTypePmAfterTwoday);

                //WMT에 추가할 임시배열 값 세팅
                valueForWMT = setFcstDateForVF(valueForWMT);
                valueForWMT = setTempLowestAndHighestForVF(valueForWMT, tempLowestAfterOneday, tempHighestAfterOneday, tempLowestAfterTwoday, tempHighestAfterTwoday);

                //WML, WMT에 추가할 1~2일후의 오전오후 강수확률, 오전오후하늘상태, 최저최고기온 정보배열을 setting한후 파싱for문 종료
                //break이후 맨아래줄로 이동해서 WS, WML, WMT맵에 키-값쌍을 put
                break;
            }

            String category = obj.getString("category");
            //해당카테 고리일때만 switch문을 실행한다.
            if(category.equals("TMP") || category.equals("PCP")|| category.equals("WSD")|| category.equals("REH")|| category.equals("PTY")|| category.equals("SKY")|| category.equals("POP")){
                //현재시각+25시간후 날씨정보중 PCP(강수량), WSD(풍속), REH(습도)은 파싱할 필요 없음
                if(row>=25 && (category.equals("PCP") || category.equals("WSD") || category.equals("REH"))) continue;
                switch (category) {
                    case "TMP": //(1시간) 기온
                        temp = (short)(obj.getDouble("fcstValue")*10);
                        // 필요한 데이터는 현재시각+23시간후 날씨까지이고 단기예보 발표시각이 3시간단위(2:00->5:00)인 것을 고려하여 현재시각+25시간후까지만 저장
                        // 파싱과정 전체를 중단하지않고 임시배열에 저장하는 행위만 중단하는 이유는 1~2일후의 23시데이터까지 모으기 위함
                        if(row<25){
                            valueForWS[row][2] = temp; //WeatherShort맵에 저장하기위해 우선 임시배열에 저장
                        }
                        if(change==1){ //하루뒤 최저최고기온 갱신
                            if(tempLowestAfterOneday > temp) tempLowestAfterOneday = temp;
                            if(tempHighestAfterOneday < temp) tempHighestAfterOneday = temp;
                        }else if(change==2){ //이틀뒤 최저최고기온 갱신
                            if(tempLowestAfterTwoday > temp) tempLowestAfterTwoday = temp;
                            if(tempHighestAfterTwoday < temp) tempHighestAfterTwoday = temp;
                        }
                        break;
                    case "PCP": //강수량
                        String rainAmountData = obj.getString("fcstValue");
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
                        valueForWS[row][3] = rainAmount;
                        break;
                    case "WSD": //풍속
                        windSpeed = (short)(obj.getDouble("fcstValue")*10);
                        valueForWS[row][4] = windSpeed;
                        break;
                    case "REH": //습도
                        humid = (short)((obj.getInt("fcstValue")));
                        valueForWS[row][5] = humid;
                        break;
                    case "SKY": //하늘(+강수형태)
                        sky = (short)(obj.getInt("fcstValue"));

                        if(change==1){
                            if(fcstTime < 1200){
                                skyRainTypeCountAmAfterOneday[0][sky]++;
                            }else{
                                skyRainTypeCountPmAfterOneday[0][sky]++;
                            }
                        }else if(change==2){
                            if(fcstTime < 1200){
                                skyRainTypeCountAmAfterTwoday[0][sky]++;
                            }else{
                                skyRainTypeCountPmAfterTwoday[0][sky]++;
                            }
                        }
                        if(row<25) {
                            valueForWS[row][6] += sky * 10;
                        }
                        break;
                    case "PTY": //강수형태(+하늘)
                        rainType = (short)obj.getInt("fcstValue"); //초단기실황조회에서는 "SKY" category가 없음
                        if(change==1){
                            if(fcstTime < 1200){
                                skyRainTypeCountAmAfterOneday[1][rainType]++;
                            }else{
                                skyRainTypeCountPmAfterOneday[1][rainType]++;
                            }
                        }else if(change==2){
                            if(fcstTime < 1200){
                                skyRainTypeCountAmAfterTwoday[1][rainType]++;
                            }else{
                                skyRainTypeCountPmAfterTwoday[1][rainType]++;
                            }
                        }
                        if(row<25) {
                            valueForWS[row][6] += rainType;
                        }
                        break;
                    case "POP": //강수확률
                        rainPercentage = (short) obj.getInt("fcstValue");
                        if(row<25) {
                            valueForWS[row][7] = rainPercentage;
                        }
                        //오전오후 시간대중 가장 강수확률이 높은 시간대의 강수확률을 채택함으로써 보수적인 예보를 할 수 있도록함.
                        if(change==1){ //하루뒤 오전오후 강수확률 갱신
                            //오전
                            if(fcstTime < 1200 && rainPercentageAmongAmAfterOneday < rainPercentage) rainPercentageAmongAmAfterOneday = rainPercentage;
                            //오후
                            if(fcstTime >= 1200 && rainPercentageAmongPmAfterOneday < rainPercentage) rainPercentageAmongPmAfterOneday = rainPercentage;
                        }else if(change==2){ //이틀뒤 오전오후 강수확률 갱신
                            //오전
                            if(rainPercentageAmongAmAfterTwoday < rainPercentage) rainPercentageAmongAmAfterTwoday = rainPercentage;
                            //오후
                            if(rainPercentageAmongPmAfterTwoday < rainPercentage) rainPercentageAmongPmAfterTwoday = rainPercentage;
                        }
                }
            }
            if(jsonIndex%12==11) row++; //0~11 반복한뒤 다음줄 채우기
        }

        weatherShort.put(keyForWS, valueForWS);
        weatherMidLand.put(keyForWML, valueForWML);
        weatherMidTemp.put(keyForWMT, valueForWMT);
    }

    static short getMostFrom(short[][] arr){
        short skyMaxCount=0;
        short skyMost=0;
        for(short i=0;i<arr[0].length;i++){ //most sky수치 추출
            short count = arr[0][i];

            if(skyMaxCount < count){
                skyMaxCount = count;
                skyMost = i;
            }
        }

        short rainTypeMaxCount=0;
        short rainTypeMost=0;
        for(short i=0;i<arr[1].length;i++){ //most rainType수치 추출
            short count = arr[1][i];

            if(rainTypeMaxCount < count){
                rainTypeMaxCount = count;
                rainTypeMost = i;
            }
        }

        return (short)(skyMost*10+rainTypeMost);
    };

    /* VF로부터 얻은 정보를 WML value배열에 예보날짜 정보 세팅 */
    static short[][] setFcstDateForVF(short[][] valueForWML){
        LocalDate stdTime = getStdTimeForVF();

        for(int day=1;day<=2;day++){
            stdTime.plusDays(day); //day일 후의 날짜 계산 for(0731->0801, 1231->0101)
            short dayAfterN = Short.parseShort(stdTime.format(DateTimeFormatter.ofPattern("MMdd"))); //월일 정보만 추출
            valueForWML[day-1][0] = dayAfterN;
        }
        return valueForWML;
    };

    static LocalDate getStdTimeForVF(){
        LocalDateTime stdTime = LocalDateTime.now();
        /*
            2,5,8,11,14,17,20,23시 발표자료를
            2:10,5:10,8:10,11:10,14:10,17:10,20:10,23:10 부터 제공
        */
        stdTime = stdTime.minusMinutes(10);
        if((stdTime.getHour()+1)%3!=0){
            stdTime = stdTime.minusHours((stdTime.minusMinutes(10).getHour()+1)%3); //130 ->030
        }

        return stdTime.toLocalDate();
    }

    static short[][] setRainAmAndPmForVF(short[][] valueForWML, short rainAmAfterOneday, short rainPmAfterOneday, short rainAmAfterTwoday, short rainPmAfterTwoday){
        valueForWML[0][1] = rainAmAfterOneday;
        valueForWML[0][2] = rainPmAfterOneday;
        valueForWML[1][1] = rainAmAfterTwoday;
        valueForWML[1][2] = rainPmAfterTwoday;
        return valueForWML;
    };

    static short[][] setSkyAmAndPmForVF(short[][] valueForWML, short skyRainTypeAmAfterOneday, short skyRainTypePmAfterOneday, short skyRainTypeAmAfterTwoday, short skyRainTypePmAfterTwoday){
        valueForWML[0][3] = skyRainTypeAmAfterOneday;
        valueForWML[0][4] = skyRainTypePmAfterOneday;
        valueForWML[1][3] = skyRainTypeAmAfterTwoday;
        valueForWML[1][4] = skyRainTypePmAfterTwoday;
        return valueForWML;
    };

    static short[][] setTempLowestAndHighestForVF(short[][] valueForWMT, short tempLowestAfterOneday, short tempHighestAfterOneday, short tempLowestAfterTwoday, short tempHighestAfterTwoday){
        valueForWMT[0][1] = tempLowestAfterOneday;
        valueForWMT[0][2] = tempHighestAfterOneday;
        valueForWMT[1][1] = tempLowestAfterTwoday;
        valueForWMT[1][2] = tempHighestAfterTwoday;
        return valueForWMT;
    };
}
