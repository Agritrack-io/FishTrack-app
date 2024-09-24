package io.agritrack.philosofish.data.converter;

import androidx.room.TypeConverter;

import com.google.android.gms.common.util.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import io.agritrack.philosofish.data.model.common.SortingSample;

public class SortingSampleConverter {

    @TypeConverter
    public static List<SortingSample> fromString(String value) {
        if (Strings.isEmptyOrWhitespace(value)) {
            return new LinkedList<>();
        }
        Type sortingSampleType = new TypeToken<List<SortingSample>>() {
        }.getType();
        List<SortingSample> sortingSample = new Gson().fromJson(value, sortingSampleType);

        return sortingSample;
    }

    @TypeConverter
    public static String listToString(List<SortingSample> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Gson gson = new Gson();
        return gson.toJson(list);
    }
}
