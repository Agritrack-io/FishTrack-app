package io.agritrack.kefalonia.data.converter;

import androidx.room.TypeConverter;

import com.google.android.gms.common.util.Strings;

import io.agritrack.kefalonia.enums.ConsumableType;

public class ConsumableTypeConverter {
    @TypeConverter
    public static ConsumableType fromString(String consumableTp) {
        return !Strings.isEmptyOrWhitespace(consumableTp) ? ConsumableType.valueOf(consumableTp) : null;
    }

    @TypeConverter
    public static String fromEnum(ConsumableType consumableTp) {
        return consumableTp == null ? ConsumableType.ALL.name() : consumableTp.name();
    }
}
