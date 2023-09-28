package welldressedmen.narispringboot.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class WeatherServiceUtility {
    static boolean isExists(Map<short[], short[][]> weatherMap, short[] stdTime){
        for(short[] searchKey : weatherMap.keySet()){
            if(arrEquals(searchKey, stdTime)) return true;
        }
        return false;
    }

    //동등성 비교 for short[]
    public static boolean arrEquals(short[] a, short[] b) {
        if (a.length != b.length) {
            return false;
        }

        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        //원소의 값이 전부다 같으면 -> 동일하다고 판단
        return true;
    }

    static short[] getStdTimeKeyForUSN(short regionId){
        LocalDateTime now = LocalDateTime.now();
        System.out.println(now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:HH:mm")));

        if(now.getMinute() < 40){
            now = now.minusHours(1); //130 ->030
        }

        short hour = (short)now.getHour();
        short min = 0;

        System.out.println(now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd:HH:mm")));
        short baseDate = Short.parseShort(now.format(DateTimeFormatter.ofPattern("MMdd")));
        short baseTime = (short)(hour*100+min);

        short[] stdTime = new short[]{regionId, baseDate, baseTime};
        return stdTime;
    }

    static short[] getStdTimeKeyForUSF(short regionId){
        LocalDateTime now = LocalDateTime.now();
        if(now.getMinute() < 45){
            now = now.minusHours(1);
        }

        short hour = (short)now.getHour();
        short min = 30;

        short baseDate = Short.parseShort(now.format(DateTimeFormatter.ofPattern("MMdd")));
        short baseTime = (short)(hour*100+min);

        short[] stdTime = new short[]{regionId, baseDate, baseTime};
        return stdTime;
    }

    static short[] getStdTimeKeyForVF(short regionId){
        LocalDateTime now = LocalDateTime.now();
        /*
        2,5,8,11,14,17,20,23시 발표자료를
        2:10,5:10,8:10,11:10,14:10,17:10,20:10,23:10 부터 제공
         */
        now = now.minusMinutes(10);
        if((now.getHour()+1)%3!=0){
            now = now.minusHours((now.minusMinutes(10).getHour()+1)%3); //130 ->030
        }

        short hour = (short)now.getHour();
        short min = 0;

        short baseDate = Short.parseShort(now.format(DateTimeFormatter.ofPattern("MMdd")));
        short baseTime = (short)(hour*100+min);
        short[] stdTime = new short[]{regionId, baseDate, baseTime};
        return stdTime;
    }

    static short[] getStdTimeKeyForMF(short regionId){
        LocalDateTime now = LocalDateTime.now();
        /*
        6,18시 발표자료를
        6:00, 18:00 부터 제공(확인 필요)(API명세에는 따로 언급X)
         */
        if((now.getHour()+6)%12!=0){
            now = now.minusHours((now.getHour()+6)%12); //기준시각을 6:00, 18:00으로 조정
        }

        short hour = (short)now.getHour();
        short min = 0;

        short baseDate = Short.parseShort(now.format(DateTimeFormatter.ofPattern("MMdd")));
        short baseTime = (short)(hour*100+min);
        short[] stdTime = new short[]{regionId, baseDate, baseTime};
        return stdTime;
    }

    static short[] getStdTimeKeyForAP(short regionId){
        LocalDateTime now = LocalDateTime.now();

        /*
        정시 발표자료를
        정시부터 제공(확인 필요)(API명세에는 따로 언급X)
         */

        short hour = (short)now.getHour();
        short min = 0;

        short baseDate = Short.parseShort(now.format(DateTimeFormatter.ofPattern("MMdd")));
        short baseTime = (short)(hour*100+min);
        short[] stdTime = new short[]{regionId, baseDate, baseTime};
        return stdTime;
    }

    //정시 이전시각 자료
    static short[] getSubsequentTimeKeyForAP(short regionId){
        LocalDateTime now = LocalDateTime.now();
        now = now.minusHours(1);

        short hour = (short)now.getHour();
        short min = 0;

        short baseDate = Short.parseShort(now.format(DateTimeFormatter.ofPattern("MMdd")));
        short baseTime = (short)(hour*100+min);
        short[] stdTime = new short[]{regionId, baseDate, baseTime};
        return stdTime;
    }
}
