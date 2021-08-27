package io.agritrack.fishtrack.data.converter;

import androidx.room.TypeConverter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LongListConverter {
    @TypeConverter
    public static List<Long> fromString(String value) {
        return Stream.of(value.substring(value.indexOf(",") + 1, value.lastIndexOf(",")).split(",", -1))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    @TypeConverter
    public static String fromArrayList(List<Long> list) {
        return "," + list.stream().map(String::valueOf).collect(Collectors.joining(",")) + ",";
    }
}
