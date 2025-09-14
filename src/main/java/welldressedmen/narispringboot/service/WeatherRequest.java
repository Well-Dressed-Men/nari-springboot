package welldressedmen.narispringboot.service;


import welldressedmen.narispringboot.domain.Region;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherRequest {
    static URL setURL(String serviceKey, Region region, String type) throws UnsupportedEncodingException, MalformedURLException {
        int nx = region.getNx();
        int ny = region.getNy();
        String midLandCode = region.getMidLandCode();
        String midTempCode = region.getMidTempCode();

        StringBuilder urlBuilder =  new StringBuilder("http://apis.data.go.kr/1360000/");

        //초단기실황, 초단기예보, 단기예보 URL 패턴 -> &base_date=20210628&base_time=0630&nx=55&ny=127
        //중기육상조회, 중기기온조회 URL 패턴 -> &regId=11B00000&tmFc=201310171800
        String[] stdTimeStrs=new String[2]; //stdTimeStrs : 기준시간 문자열(예보날짜,예보시각)(for 초단기실황, 초단기예보, 단기예보)
        String stdTimeStr=null; //stdTimeStr : 기준시간 문자열(예보날짜+예보시각)(for 중기육상예보, 중기기온조회)
        switch(type){
            case "USN" :
                urlBuilder.append("VilageFcstInfoService_2.0/getUltraSrtNcst");
                stdTimeStrs= getStdTimeStringForUSN(); //ex : 816 1600
                break;
            case "USF" :
                urlBuilder.append("VilageFcstInfoService_2.0/getUltraSrtFcst");
                stdTimeStrs= getStdTimeStringForUSF(); //ex : 816 1630
                break;
            case "VF" :
                urlBuilder.append("VilageFcstInfoService_2.0/getVilageFcst");
                stdTimeStrs= getStdTimeStringForVF();
                break;
            case "MFL" :
                urlBuilder.append("MidFcstInfoService/getMidLandFcst");
                stdTimeStr= getStdTimeStringForMF();
                break;
            case "MFT" :
                urlBuilder.append("MidFcstInfoService/getMidTa");
                stdTimeStr= getStdTimeStringForMF();
                break;
        }

        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + serviceKey);
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("1000", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode("JSON", "UTF-8")); /*요청자료형식(XML/JSON) Default: XML*/

        String baseDate;
        String baseTime;
        String regId;
        String baseDateTime;
        switch (type){
            case "USN" :
            case "USF" :
            case "VF" :
                baseDate = stdTimeStrs[0];
                baseTime = stdTimeStrs[1];

                urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8"));
                urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8"));
                urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(nx), "UTF-8"));
                urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(ny), "UTF-8"));
                break;
            case "MFL" :
                regId = midLandCode;
                baseDateTime = stdTimeStr;

                urlBuilder.append("&" + URLEncoder.encode("regId", "UTF-8") + "=" + URLEncoder.encode(regId, "UTF-8"));
                urlBuilder.append("&" + URLEncoder.encode("tmFc", "UTF-8") + "=" + URLEncoder.encode(baseDateTime, "UTF-8"));
                break;
            case "MFT" :
                regId = midTempCode;
                baseDateTime = stdTimeStr;

                urlBuilder.append("&" + URLEncoder.encode("regId", "UTF-8") + "=" + URLEncoder.encode(regId, "UTF-8"));
                urlBuilder.append("&" + URLEncoder.encode("tmFc", "UTF-8") + "=" + URLEncoder.encode(baseDateTime, "UTF-8"));
                break;
        }

        URL url = new URL(urlBuilder.toString());
        return url;
    };
    // 기준시각을 직접 지정해서 VF URL 생성 (USN/USF도 원하면 동일 패턴으로 확장 가능)
    static URL setURL(String serviceKey, Region region, String type, String baseDate, String baseTime)
            throws UnsupportedEncodingException, MalformedURLException {

        if (!"VF".equals(type)) {
            // 필요 시 USN/USF도 확장. 지금은 VF만 받도록 방어.
            return setURL(serviceKey, region, type); // 기존 로직으로 fallback
        }

        int nx = region.getNx();
        int ny = region.getNy();

        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1360000/");
        urlBuilder.append("VilageFcstInfoService_2.0/getVilageFcst");

        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + serviceKey);
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("1000", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode("JSON", "UTF-8"));

        urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(nx), "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(ny), "UTF-8"));

        return new URL(urlBuilder.toString());
    }


    //USN기준 기준시각 문자열배열 return
    static String[] getStdTimeStringForUSN(){
        LocalDateTime now = LocalDateTime.now();
        System.out.println(now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:HH:mm")));

        if(now.getMinute() < 40){
            now = now.minusHours(1); //130 ->030
        }

        String hour = String.valueOf(now.getHour());
        hour = hour.length()<2 ? "0"+hour : hour;
        String min = "00";

        String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = hour+min;

        String[] stdTimeString = new String[]{baseDate, baseTime};
        return stdTimeString;
    };

    //USF기준 기준시각 문자열배열 return
    static String[] getStdTimeStringForUSF(){
        LocalDateTime now = LocalDateTime.now();
        System.out.println(now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:HH:mm")));

        if(now.getMinute() < 45){
            now = now.minusHours(1); //130 ->030
        }

        String hour = String.valueOf(now.getHour());
        hour = hour.length()<2 ? "0"+hour : hour;
        String min = "30";

        String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = hour+min;

        String[] stdTimeString = new String[]{baseDate, baseTime};
        return stdTimeString;
    };

    //VF기준 기준시각 문자열배열 return
    static String[] getStdTimeStringForVF(){
        LocalDateTime stdTime = LocalDateTime.now();
        /*
            2,5,8,11,14,17,20,23시 발표자료를
            2:10,5:10,8:10,11:10,14:10,17:10,20:10,23:10 부터 제공
        */
        stdTime = stdTime.minusMinutes(10);
        if((stdTime.getHour()+1)%3!=0){
            stdTime = stdTime.minusHours((stdTime.minusMinutes(10).getHour()+1)%3); //130 ->030
        }

        String hour = String.valueOf(stdTime.getHour());
        hour = hour.length()<2 ? "0"+hour : hour;
        String min = "00";

        String baseDate = stdTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = hour+min;

        String[] stdTimeString = new String[]{baseDate, baseTime};
        return stdTimeString;
    };
    // 현재 VF 기준 산출 방식과 동일하게 정렬한 뒤, 정확히 3시간 이전으로 이동
    static String[] getStdTimeStringForVFRetry() {
        String[] base = getStdTimeStringForVF();
        String baseDate = base[0]; // yyyyMMdd
        String baseTime = base[1]; // HHmm

        LocalDateTime dt = LocalDateTime.parse(
                baseDate + baseTime,
                DateTimeFormatter.ofPattern("yyyyMMddHHmm")
        );

        // 3시간 이전 이동
        dt = dt.minusHours(3);

        String newDate = dt.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String newTime = dt.format(DateTimeFormatter.ofPattern("HHmm"));
        return new String[]{ newDate, newTime };
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

    static URL setURLForAP(String serviceKey, Region region) throws UnsupportedEncodingException, MalformedURLException {
        String stationName = region.getStationName();

        StringBuilder urlBuilder =  new StringBuilder("http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty");

        System.out.println(stationName);

        urlBuilder.append("?" + URLEncoder.encode("stationName", "UTF-8") + "=" + URLEncoder.encode(stationName, "UTF-8")); /*페이지번호*/

        urlBuilder.append("&" + URLEncoder.encode("dataTerm", "UTF-8") + "=" + URLEncoder.encode("daily", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("100", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("returnType", "UTF-8") + "=" + URLEncoder.encode("JSON", "UTF-8")); /*요청자료형식(XML/JSON) Default: XML*/
        urlBuilder.append("&" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + serviceKey);
        urlBuilder.append("&" + URLEncoder.encode("ver", "UTF-8") + "=" + URLEncoder.encode("1.0", "UTF-8"));

        URL url = new URL(urlBuilder.toString());
        return url;
    };

    static String requestWeather(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        String data = sb.toString();

        return data;
    };
}
