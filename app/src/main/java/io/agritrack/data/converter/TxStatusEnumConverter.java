package io.agritrack.data.converter;

import androidx.room.TypeConverter;

import com.google.android.gms.common.util.Strings;

import io.agritrack.enums.TxStatus;

public class TxStatusEnumConverter {
    @TypeConverter
    public static TxStatus fromString(String status) {
        return !Strings.isEmptyOrWhitespace(status) ? TxStatus.valueOf(status) : TxStatus.NONE;
    }

    @TypeConverter
    public static String fromEnum(TxStatus status) {
        return status == null ? "NONE" : status.name();
    }
}
