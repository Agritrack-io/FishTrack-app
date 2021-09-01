package io.agritrack.fishtrack.data.converter;

import androidx.room.TypeConverter;

import com.google.android.gms.common.util.Strings;

import io.agritrack.fishtrack.enums.AssetType;

public class AssetTypeConverter {
    @TypeConverter
    public static AssetType fromString(String assetTp) {
        return !Strings.isEmptyOrWhitespace(assetTp) ? AssetType.valueOf(assetTp) : null;
    }

    @TypeConverter
    public static String fromEnum(AssetType assetTp) {
        return assetTp == null ? "NONE" : assetTp.name();
    }
}
