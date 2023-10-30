package welldressedmen.narispringboot.service;


import lombok.extern.slf4j.Slf4j;
import welldressedmen.narispringboot.domain.Weather;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static welldressedmen.narispringboot.service.WeatherService.*;
import static welldressedmen.narispringboot.service.WeatherServiceUtility.*;

@Slf4j
public class WeatherResponseSet {
    /*
        현재~현재로부터 6시간후 초단기날씨정보를(시간별 기온, 강수량, 풍속, 습도, 하늘상태, 강수확률) 전달하기위한 리스트 생성
        - weatherUltraShort(초단기날씨정보)는 지역별로 1.초단기실황조회로부터 얻은 날씨데이터 1개, 2.초단기예보조회로부터 얻은 날씨데이터 6개로 구성되어있다.
     */
    static List<Weather.UltraShort> createUltraShortWeatherList(short regionId){
        List<Weather.UltraShort> result = new ArrayList<>();

        short[] searchKeyForUSN = getStdTimeKeyForUSN(regionId); //초단기실황조회 기준시각 key생성
        short[] searchKeyForUSF = getStdTimeKeyForUSF(regionId); //초단기예보조회 기준시각 key생성

        Iterator<short[]> keyIterator = weatherUltraShort.keySet().iterator();
        while(keyIterator.hasNext()){
            short[] key = keyIterator.next();

            if(isStandardData(searchKeyForUSN, key) || isStandardData(searchKeyForUSF, key)){
                short[][] values = weatherUltraShort.get(key);

                //7개의 초단기날씨정보 생성(1개 from 초단기실황조회, 6개 from 초단기예보조회)
                int limit = isStandardData(searchKeyForUSN, key) ? 1 : 6;
                for(int i=0;i<limit;i++){
                    short[] value = values[i];
                    Weather.UltraShort item = buildUltraShortItem(value);
                    result.add(item);
                }
            } else if(isOutdatedData(searchKeyForUSN, key) && isOutdatedData(searchKeyForUSF, key)){ //초단기실황조회 현재 기준시각, 초단기예보조회 현재 기준시각에 모두 해당하지 않으면 -> 유효하지 않는 데
                keyIterator.remove();
            }
        }

        return result;
    };

    /*
        현재로부터 7시간후~23시간후의 단기날씨정보를(시간별 기온, 강수량, 풍속, 습도, 하늘상태, 강수확률) 전달하기위한 리스트 생성
        (현재로부터 1시간후~25시간후의 데이터를 프론트에 전달하고, 프론트에서 상황에 따라서 적절하게 추출)
     */
    static List<Weather.Short> createShortWeatherList(short regionId){
        List<Weather.Short> result = new ArrayList<>();

        //단기예보조회를 통해 weatherShort에 저장한 정보를 객체에 담아서 리스트에 저장
        short[] searchKeyForVF = getStdTimeKeyForVF(regionId);

        Iterator<short[]> keyIterator = weatherShort.keySet().iterator();
        while(keyIterator.hasNext()){
            short[] key = keyIterator.next();

            if(isStandardData(searchKeyForVF, key)){
                short[][] values = weatherShort.get(key);

                //25개의 단기날씨정보 생성
                for(int i=0;i<25;i++){
                    short[] value = values[i];
                    Weather.Short item = buildShortWeatherItem(value);
                    result.add(item);
                }
            }else if(isOutdatedData(searchKeyForVF, key)){
                keyIterator.remove();
            }
        }

        return result;
    };

    //1~6일후의 중기날씨정보를(하루의 오전오후 강수확률, 하늘, 최저최고기온) 전달하기위한 리스트 생성
    static List<Weather.Mid> createMidWeatherList(short regionId) {
        List<Weather.Mid> result = new ArrayList<>();

        short[] searchKeyForVF = getStdTimeKeyForVF(regionId);

        // 2개의 중기날씨정보 생성(육상 관련)(from 단기예보조회)
        Iterator<short[]> midLandKeyIteratorForVF = weatherMidLand.keySet().iterator();
        while (midLandKeyIteratorForVF.hasNext()) {
            short[] key = midLandKeyIteratorForVF.next();
            if (isStandardData(searchKeyForVF, key)) {
                short[][] values = weatherMidLand.get(key);
                for (int i = 0; i < 2; i++) {
                    short[] value = values[i];
                    Weather.Mid item = buildMidWeatherItem(value);
                    result.add(item);
                }
            } else if (isOutdatedData(searchKeyForVF, key)) {
                midLandKeyIteratorForVF.remove();
            }
        }

        // 2개의 중기날씨정보 완성(기온 관련)(from 단기예보조회)
        Iterator<short[]> midTempKeyIteratorForVF = weatherMidTemp.keySet().iterator();
        while (midTempKeyIteratorForVF.hasNext()) {
            short[] key = midTempKeyIteratorForVF.next();
            if (isStandardData(searchKeyForVF, key)) {
                short[][] values = weatherMidTemp.get(key);
                for (int i = 0; i < 2; i++) {
                    short[] value = values[i];
                    result.get(i).setTempLowest(value[1]);
                    result.get(i).setTempHighest(value[2]);
                }
            } else if (isOutdatedData(searchKeyForVF, key)) {
                midTempKeyIteratorForVF.remove();
            }
        }

        short[] searchKeyForMF = getStdTimeKeyForMF(regionId);

        // 2개의 중기날씨정보 생성(육상 관련)(from 중기육상조회)
        Iterator<short[]> midLandKeyIteratorForMF = weatherMidLand.keySet().iterator();
        while (midLandKeyIteratorForMF.hasNext()) {
            short[] key = midLandKeyIteratorForMF.next();
            if (isStandardData(searchKeyForMF, key)) {
                short[][] values = weatherMidLand.get(key);
                for (int i = 0; i < 4; i++) {
                    short[] value = values[i];
                    Weather.Mid item = buildMidWeatherItem(value);
                    result.add(item);
                }
            } else if (isOutdatedData(searchKeyForMF, key)) {
                midLandKeyIteratorForMF.remove();
            }
        }

        // 2개의 중기날씨정보 생성(기온 관련)(from 중기기온조회)
        Iterator<short[]> midTempKeyIteratorForMF = weatherMidTemp.keySet().iterator();
        while (midTempKeyIteratorForMF.hasNext()) {
            short[] key = midTempKeyIteratorForMF.next();
            if (isStandardData(searchKeyForMF, key)) {
                short[][] values = weatherMidTemp.get(key);
                for (int i = 0; i < 4; i++) {
                    short[] value = values[i];
                    result.get(i+2).setTempLowest(value[1]);
                    result.get(i+2).setTempHighest(value[2]);
                }
            } else if (isOutdatedData(searchKeyForMF, key)) {
                midTempKeyIteratorForMF.remove();
            }
        }

        return result;
    }


    /*
        현재 미세먼지, 초미세먼지 날씨정보를 전달하기 위한 리스트 생성
    */
    static List<Weather.AP> createAirPollutionList(short regionId){
        List<Weather.AP> result = new ArrayList<>();

        short[] searchKeyForAPcurrent = getStdTimeKeyForAP(regionId);
        short[] searchKeyForAPbefore = getSubsequentTimeKeyForAP(regionId);

        //현재 기준시각에 해당하는 대기오염날씨정보 1개 생성
        Iterator<short[]> apKeyIterator = weatherAP.keySet().iterator();
        boolean foundCurrentTimeData = false;
        while (apKeyIterator.hasNext()) {
            short[] key = apKeyIterator.next();
            if(isStandardData(searchKeyForAPcurrent, key)){
                foundCurrentTimeData = true;
                short[][] values = weatherAP.get(key);

                short[] value = values[0];
                Weather.AP item = buildAirPollutionItem(value);
                result.add(item);
            }
        }

        // 현재기준시각에 대한 대기오염날씨정보가 없을때 비로소 이전시각 대기오염정보를 가져옴 - 이유 : 현재 대기오염정보조회API에서 발표시각을 공식적으로 밝히고 있는 내용이 없음
        if(!foundCurrentTimeData){
            log.info("이전시각 대기오염정보 조회 자료 가져옴");
            //이전 시각 자료 가져옴
            Iterator<short[]> beforeApKeyIterator = weatherAP.keySet().iterator();
            while (beforeApKeyIterator.hasNext()) {
                short[] key = beforeApKeyIterator.next();
                if(isStandardData(searchKeyForAPbefore, key)){
                    short[][] values = weatherAP.get(key);

                    short[] value = values[0];
                    Weather.AP item = buildAirPollutionItem(value);
                    result.add(item);
                }
            }
        }

        //현재기준시각, 이전기준시각 데이터가 모두 아닐시 유효하지 않은 데이터로 판단 -> 제거
        Iterator<short[]> removeApKeyIterator = weatherAP.keySet().iterator();
        while (removeApKeyIterator.hasNext()) {
            short[] key = removeApKeyIterator.next();
            if(isOutdatedData(searchKeyForAPcurrent, key) && isOutdatedData(searchKeyForAPbefore, key)){
                removeApKeyIterator.remove();
            }
        }

        return result;
    };

    private static Weather.UltraShort buildUltraShortItem(short[] value) {
        return Weather.UltraShort.builder()
                .fcstDate(value[0])
                .fcstTime(value[1])
                .temp(value[2])
                .rainAmount(value[3])
                .windSpeed(value[4])
                .humid(value[5])
                .sky(value[6])
                .build();
    }

    private static Weather.Short buildShortWeatherItem(short[] value) {
        return Weather.Short.builder()
                .fcstDate(value[0])
                .fcstTime(value[1])
                .temp(value[2])
                .rainAmount(value[3])
                .windSpeed(value[4])
                .humid(value[5])
                .sky(value[6])
                .rainPercentage(value[7])
                .build();
    }

    private static Weather.Mid buildMidWeatherItem(short[] value) {
        return Weather.Mid.builder()
                .fcstDate(value[0])
                .rainPercentageAm(value[1])
                .rainPercentagePm(value[2])
                .skyAm(value[3])
                .skyPm(value[4])
                .build();
    }

    private static Weather.AP buildAirPollutionItem(short[] value) {
        return Weather.AP.builder()
                .fcstDate(value[0])
                .fcstTime(value[1])
                .pm10Value(value[2])
                .pm25Value(value[3])
                .build();
    }

}