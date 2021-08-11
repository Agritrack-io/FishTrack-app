package io.agritrack.fishtrack.data;

import androidx.room.TypeConverter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringListConverter {
    @TypeConverter
    public static List<String> fromString(String value) {
        return Stream.of(value.split(",")).map(x -> new String(x)).collect(Collectors.toList());
    }

    @TypeConverter
    public static String fromArrayList(List<String> list) {
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
