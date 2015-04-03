package com.messagebus.common;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yanghua on 4/1/15.
 */
public class GsonUtil {

    /**
     * convert json-object string to map
     * @param jsonObjStr the string representation of json-object
     * @return the map object
     */
    public static Map jsonStrToMap(String jsonObjStr) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Object.class, new NaturalDeserializer());
        Gson gson = gsonBuilder.create();

        return gson.fromJson(jsonObjStr, Map.class);
    }

    public static List jsonStrToList(String jsonObjStr) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Object.class, new NaturalDeserializer());
        Gson gson = gsonBuilder.create();

        return gson.fromJson(jsonObjStr, List.class);
    }

    /**
     * inner static class : implement JsonDeserializer interface
     * which overrides the default implementation
     */
    private static class NaturalDeserializer implements JsonDeserializer<Object> {

        @Override
        public Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            if (jsonElement.isJsonNull()) return null;
            else if (jsonElement.isJsonPrimitive()) return handlePrimitive(jsonElement.getAsJsonPrimitive());
            else if (jsonElement.isJsonArray()) return handleArray(jsonElement.getAsJsonArray(), context);
            else return handleObject(jsonElement.getAsJsonObject(), context);
        }

        private Object handlePrimitive(JsonPrimitive json) {
            if (json.isBoolean())
                return json.getAsBoolean();
            else if (json.isString())
                return json.getAsString();
            else {
                BigDecimal bigDec = json.getAsBigDecimal();
                // Find out if it is an int type
                try {
                    bigDec.toBigIntegerExact();
                    try {
                        return bigDec.intValueExact();
                    } catch (ArithmeticException e) {
                    }
                    return bigDec.longValue();
                } catch (ArithmeticException e) {
                }
                // Just return it as a double
                return bigDec.doubleValue();
            }
        }

        private Object handleArray(JsonArray json, JsonDeserializationContext context) {
            Object[] array = new Object[json.size()];
            for (int i = 0; i < array.length; i++)
                array[i] = context.deserialize(json.get(i), Object.class);
            return array;
        }

        private Object handleObject(JsonObject json, JsonDeserializationContext context) {
            Map<String, Object> map = new HashMap<String, Object>();
            for (Map.Entry<String, JsonElement> entry : json.entrySet())
                map.put(entry.getKey(), context.deserialize(entry.getValue(), Object.class));
            return map;
        }
    }

}
