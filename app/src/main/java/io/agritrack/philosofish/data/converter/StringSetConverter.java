package io.agritrack.philosofish.data.converter;

import androidx.room.TypeConverter;

import com.google.android.gms.common.util.Strings;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringSetConverter {
    @TypeConverter
    public static Set<String> fromString(String value) {
        if (Strings.isEmptyOrWhitespace(value)) {
            return new HashSet<>();
        }
        return Stream.of(value.split(",")).map(x -> x).collect(Collectors.toSet());
    }

    @TypeConverter
    public static String fromSet(Set<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
