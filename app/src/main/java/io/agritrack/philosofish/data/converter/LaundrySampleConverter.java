package io.agritrack.philosofish.data.converter;

import androidx.room.TypeConverter;

import com.google.android.gms.common.util.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import io.agritrack.philosofish.data.model.common.LaundrySample;

public class LaundrySampleConverter {

    @TypeConverter
    public static List<LaundrySample> fromString(String value) {
        if (Strings.isEmptyOrWhitespace(value)) {
            return new LinkedList<>();
        }
        Type laundrySampleType = new TypeToken<List<LaundrySample>>() {
        }.getType();
        List<LaundrySample> laundrySample = new Gson().fromJson(value, laundrySampleType);

        return laundrySample;
    }

    @TypeConverter
    public static String listToString(List<LaundrySample> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Gson gson = new Gson();
        return gson.toJson(list);
    }
}
