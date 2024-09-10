package io.agritrack.philosofish.data.converter;

import androidx.room.TypeConverter;

import com.google.android.gms.common.util.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import io.agritrack.philosofish.data.model.common.TonneSample;

public class TonneSampleConverter {

    @TypeConverter
    public static List<TonneSample> fromString(String value) {
        if (Strings.isEmptyOrWhitespace(value)) {
            return new LinkedList<>();
        }
        Type tonneSampleType = new TypeToken<List<TonneSample>>() {
        }.getType();
        List<TonneSample> tonneSample = new Gson().fromJson(value, tonneSampleType);

        return tonneSample;
    }

    @TypeConverter
    public static String listToString(List<TonneSample> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Gson gson = new Gson();
        return gson.toJson(list);
    }
}
