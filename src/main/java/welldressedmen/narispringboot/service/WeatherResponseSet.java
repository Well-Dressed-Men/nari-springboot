package welldressedmen.narispringboot.service;


import welldressedmen.narispringboot.domain.Weather;

import java.util.ArrayList;
import java.util.List;

import static welldressedmen.narispringboot.service.WeatherService.*;
import static welldressedmen.narispringboot.service.WeatherServiceUtility.*;


public class WeatherResponseSet {
    /*
        현재~현재로부터 6시간후 초단기날씨정보를(시간별 기온, 강수량, 풍속, 습도, 하늘상태, 강수확률) 전달하기위한 리스트 생성
     */
    static List<Weather.UltraShort> createUltraShortWeatherList(short regionId){
        List<Weather.UltraShort> result = new ArrayList<>();

        //초단기실황조회를 통해 weatherUltraShort에 저장한 정보를 객체에 담아서 리스트에 저장
        short[] searchKeyForUSN = getStdTimeKeyForUSN(regionId);
        for(short[] key : weatherUltraShort.keySet()){
            if(arrEquals(searchKeyForUSN, key)){
                short[][] values = weatherUltraShort.get(key);

                for(int i=0;i<1;i++){ //초단기실황 데이터 1개 Weather.Short 객체 세팅
                    short[] value = values[i];
                    Weather.UltraShort item = Weather.UltraShort.builder()
                            .fcstDate(value[0])
                            .fcstTime(value[1])
                            .temp(value[2])
                            .rainAmount(value[3])
                            .windSpeed(value[4])
                            .humid(value[5])
                            .sky(value[6])
                            .build();
                    result.add(item);
                }
            }
        }

        //초단기예보조회를 통해 weatherUltraShort에 저장한 정보를 객체에 담아서 리스트에 저장
        short[] searchKeyForUSF = getStdTimeKeyForUSF(regionId);
        for(short[] key : weatherUltraShort.keySet()){
            if(arrEquals(searchKeyForUSF, key)){
                short[][] values = weatherUltraShort.get(key);

                for(int i=0;i<6;i++){
                    short[] value = values[i]; //초단기예보 데이터 6개 Weather.Short 객체 세팅
                    Weather.UltraShort item = Weather.UltraShort.builder()
                            .fcstDate(value[0])
                            .fcstTime(value[1])
                            .temp(value[2])
                            .rainAmount(value[3])
                            .windSpeed(value[4])
                            .humid(value[5])
                            .sky(value[6])
                            .build();
                    result.add(item);
                }
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
        for(short[] key : weatherShort.keySet()){
            if(arrEquals(searchKeyForVF, key)){
                short[][] values = weatherShort.get(key);

                for(int i=0;i<25;i++){
                    short[] value = values[i]; //Weather.Short 세팅
                    Weather.Short item = Weather.Short.builder()
                            .fcstDate(value[0])
                            .fcstTime(value[1])
                            .temp(value[2])
                            .rainAmount(value[3])
                            .windSpeed(value[4])
                            .humid(value[5])
                            .sky(value[6])
                            .rainPercentage(value[7])
                            .build(); // builder를 통해 객체 생성
                    result.add(item);
                }
            }
        }
        return result;
    };

    //1~6일후의 중기날씨정보를(하루의 오전오후 강수확률, 하늘, 최저최고기온) 전달하기위한 리스트 생성
    static List<Weather.Mid> createMidWeatherList(short regionId){
        List<Weather.Mid> result = new ArrayList<>();

        //단기예보를 통해 weatherMidLand에 저장한 정보를 객체에 담음
        short[] searchKeyForVF = getStdTimeKeyForVF(regionId);
        for(short[] key : weatherMidLand.keySet()){

            if(arrEquals(searchKeyForVF, key)){
                short[][] values = weatherMidLand.get(key);

                //Weather.Mid 생성
                for(int i=0;i<2;i++){
                    short[] value = values[i];
                    Weather.Mid item = Weather.Mid.builder()
                            .fcstDate(value[0])

                            .rainPercentageAm(value[1])
                            .rainPercentagePm(value[2])
                            .skyAm(value[3])
                            .skyPm(value[4])
                            .build(); // builder를 통해 객체 생성
                    result.add(item);
                }
            }
        }

        //단기예보를 통해 weatherMidTemp에 저장한 정보를 객체에 담음
        for(short[] key : weatherMidTemp.keySet()){
            if(arrEquals(searchKeyForVF, key)){
                short[][] values = weatherMidTemp.get(key);

                //Weather.Mid에 최저기온, 최고기온 정보 추가
                for(int i=0;i<2;i++){
                    short[] value = values[i];

                    result.get(i).setTempLowest(value[1]);
                    result.get(i).setTempHighest(value[2]);

                }
            }
        }

        //중기육상조회를 통해 weatherMidLand에 저장한 정보를 객체에 담음
        short[] searchKeyForMF = getStdTimeKeyForMF(regionId);

        for(short[] key : weatherMidLand.keySet()){
            if(arrEquals(searchKeyForMF, key)){
                short[][] values = weatherMidLand.get(key);
                //Weather.Mid 생성
                for(int i=0;i<4;i++){
                    short[] value = values[i];
                    Weather.Mid item = Weather.Mid.builder()
                            .fcstDate(value[0])

                            .rainPercentageAm(value[1])
                            .rainPercentagePm(value[2])
                            .skyAm(value[3])
                            .skyPm(value[4])
                            .build(); // builder를 통해 객체 생성
                    result.add(item);
                }
            }
        }

        //중기기온조회를 통해 weatherMidTemp에 저장한 정보를 객체에 담음
        for(short[] key : weatherMidTemp.keySet()){
            if(arrEquals(searchKeyForMF, key)){
                short[][] values = weatherMidTemp.get(key);
                //Weather.Mid에 최저최고기온 정보 추가
                for(int i=0;i<4;i++){
                    short[] value = values[i];

                    result.get(i+2).setTempLowest(value[1]); //3번째 객체부터 3일후의 정보가 담겨있음
                    result.get(i+2).setTempHighest(value[2]);
                }
            }
        }
        return result;
    };
}
