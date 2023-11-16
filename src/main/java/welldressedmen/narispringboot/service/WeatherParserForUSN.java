package welldressedmen.narispringboot.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import welldressedmen.narispringboot.exception.TemporalErrorException;

import static welldressedmen.narispringboot.service.WeatherService.weatherUltraShort;


@Slf4j
@Component
public class WeatherParserForUSN {
    public void parse(String resData, short regionId) {
        JSONObject response = extractResponse(new JSONObject(resData));
        validateResponse(response);

        JSONArray weatherDataArray = response.getJSONObject("body").getJSONObject("items").getJSONArray("item");
        short[] key = buildKey(regionId, weatherDataArray.getJSONObject(0));
        short[][] value = buildValue(weatherDataArray);

        processWeatherDataArray(weatherDataArray, value);

        weatherUltraShort.put(key, value);
    }

    private static JSONObject extractResponse(JSONObject jsonObject) {
        return jsonObject.getJSONObject("response");
    }

    private static void validateResponse(JSONObject response) {
        JSONObject header = response.getJSONObject("header");
        String resultCode = header.getString("resultCode");
        if (!"00".equals(resultCode)) {
            throw new TemporalErrorException();
        }
    }

    private static short[] buildKey(short regionId, JSONObject firstItem) {
        short baseDate = (short) (firstItem.getInt("baseDate") % 10000);
        short baseTime = (short) (firstItem.getInt("baseTime"));
        return new short[]{regionId, baseDate, baseTime};
    }

    private static short[][] buildValue(JSONArray weatherDataArray) {
        short[][] value = new short[1][8];
        for (int i = 0; i < weatherDataArray.length(); i++) {
            JSONObject weatherData = weatherDataArray.getJSONObject(i);
            processSingleWeatherData(weatherData, value[0]);
        }
        return value;
    }

    private static void processWeatherDataArray(JSONArray weatherDataArray, short[][] value) {
        for (int i = 0; i < weatherDataArray.length(); i++) {
            JSONObject weatherData = weatherDataArray.getJSONObject(i);
            if (i == 0) {
                setFcstDateTime(weatherData, value[0]);
            }
            processSingleWeatherData(weatherData, value[0]);
        }
    }
    private static void setFcstDateTime(JSONObject weatherData, short[] valueArray) {
        short fcstDate = (short) (weatherData.getInt("baseDate") % 10000); // 예보 날짜
        short fcstTime = (short) (weatherData.getInt("baseTime")); // 예보 시각
        valueArray[0] = fcstDate;
        valueArray[1] = fcstTime;
    }
    private static void processSingleWeatherData(JSONObject weatherData, short[] valueArray) {
        String category = weatherData.getString("category");
        switch (category) {
            case "T1H": // 기온
                valueArray[2] = parseTemperature(weatherData);
                break;
            case "RN1": // 강수량
                valueArray[3] = parseRainfall(weatherData);
                break;
            case "WSD": // 풍속
                valueArray[4] = (short) (weatherData.getDouble("obsrValue") * 10);
                break;
            case "REH": // 습도
                valueArray[5] = (short) weatherData.getInt("obsrValue");
                break;
            case "PTY": // (하늘+)강수형태
                valueArray[6] += (short) weatherData.getInt("obsrValue");
                break;
        }
    }

    private static short parseTemperature(JSONObject weatherData) {
        return (short) (weatherData.getDouble("obsrValue") * 10);
    }

    private static short parseRainfall(JSONObject weatherData) {
        String rainfallData = weatherData.getString("obsrValue");
        switch (rainfallData) {
            case "0":
                return 0;
            case "1.0mm 미만":
                return 1;
            case "30.0~50.0mm":
                return 40;
            case "50.0mm 이상":
                return 65;
            default:
                return 15;
        }
    }

}
