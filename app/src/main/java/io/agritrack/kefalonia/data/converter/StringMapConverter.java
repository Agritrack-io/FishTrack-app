package io.agritrack.kefalonia.data.converter;

import androidx.room.TypeConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.util.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringMapConverter {
    @TypeConverter
    public static Map<String, List<String>> fromString(String value) {
        HashMap result = null;
        if (Strings.isEmptyOrWhitespace(value)) {
            return new HashMap<>();
        }
        try {
            result = new ObjectMapper().readValue(value, HashMap.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }

    @TypeConverter
    public static String fromArrayList(Map<String, List<String>> map) {
        String result = null;
        ObjectMapper objectMapper = new ObjectMapper();
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            result = objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
