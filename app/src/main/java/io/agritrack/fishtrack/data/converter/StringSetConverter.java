package io.agritrack.fishtrack.data.converter;

import androidx.room.TypeConverter;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringSetConverter {
    @TypeConverter
    public static Set<String> fromString(String value) {
        return Stream.of(value.split(",")).map(x -> new String(x)).collect(Collectors.toSet());
    }

    @TypeConverter
    public static String fromArrayList(Set<String> list) {
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
