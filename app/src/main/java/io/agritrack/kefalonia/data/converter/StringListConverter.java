package io.agritrack.kefalonia.data.converter;

import androidx.room.TypeConverter;

import com.google.android.gms.common.util.Strings;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringListConverter {
    @TypeConverter
    public static List<String> fromString(String value) {
        if (Strings.isEmptyOrWhitespace(value)) {
            return new LinkedList<>();
        }
        return Stream.of(value.split(",")).map(x -> x).collect(Collectors.toList());
    }

    @TypeConverter
    public static String fromArrayList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
