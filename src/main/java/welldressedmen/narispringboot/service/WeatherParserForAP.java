package welldressedmen.narispringboot.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import welldressedmen.narispringboot.exception.TemporalErrorException;

import static welldressedmen.narispringboot.service.WeatherService.*;
@Slf4j
@Component
public class WeatherParserForAP {
    public void parse(String resData, short regionId) {
        JSONObject response = extractResponseFromJson(resData);
        validateResponseCode(response);

        JSONArray items = response.getJSONObject("body").getJSONArray("items");
        JSONObject firstObj = items.getJSONObject(0);

        short[] key = buildKey(regionId, firstObj);
        short[][] value = buildValue(firstObj, key);

        weatherAP.put(key, value);
    }

    private static JSONObject extractResponseFromJson(String resData) {
        return new JSONObject(resData).getJSONObject("response");
    }

    private static void validateResponseCode(JSONObject response) {
        String resultCode = response.getJSONObject("header").getString("resultCode");
        if (!resultCode.equals("00")) {
            throw new TemporalErrorException();
        }
    }

    private static short[] buildKey(short regionId, JSONObject firstObj) {
        short baseDate = extractDate(firstObj.getString("dataTime"));
        short baseTime = extractTime(firstObj.getString("dataTime"));
        return new short[] {regionId, baseDate, baseTime};
    }

    private static short[][] buildValue(JSONObject firstObj, short[] key) {
        Object pm10Flag = firstObj.get("pm10Flag");
        Object pm25Flag = firstObj.get("pm25Flag");

        if (!JSONObject.NULL.equals(pm10Flag) || !JSONObject.NULL.equals(pm25Flag))
            throw new TemporalErrorException();

        return new short[][] {{key[1], key[2],
                (short) firstObj.getInt("pm10Value"),
                (short) firstObj.getInt("pm25Value")}};
    }

    private static short extractDate(String dateTime) {
        String date = dateTime.split(" ")[0];
        return (short) (Integer.parseInt(date.split("-")[1]) * 100
                + Integer.parseInt(date.split("-")[2]));
    }

    private static short extractTime(String dateTime) {
        String time = dateTime.split(" ")[1];
        int hour = time.split(":")[0].equals("24") ? 0 : Integer.parseInt(time.split(":")[0]);
        return (short) (hour * 100 + Integer.parseInt(time.split(":")[1]));
    }
}
