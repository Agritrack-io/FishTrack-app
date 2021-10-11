package io.agritrack.data.converter;

import androidx.room.TypeConverter;

import com.google.android.gms.common.util.Strings;

import io.agritrack.enums.AssetType;

public class AssetTypeConverter {
    @TypeConverter
    public static AssetType fromString(String assetTp) {
        return !Strings.isEmptyOrWhitespace(assetTp) ? AssetType.valueOf(assetTp) : null;
    }

    @TypeConverter
    public static String fromEnum(AssetType assetTp) {
        return assetTp == null ? "ALL" : assetTp.name();
    }
}
