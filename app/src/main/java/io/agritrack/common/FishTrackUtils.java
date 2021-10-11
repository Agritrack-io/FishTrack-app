package io.agritrack.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.enums.AssetType;

public class FishTrackUtils {
    public static String[] assetTypes(String firstItem) {
        List<String> assetTypeList = Arrays.stream(AssetType.values()).map(x -> x.name()).collect(Collectors.toList());
        assetTypeList.add(0, firstItem);
        return assetTypeList.toArray(new String[assetTypeList.size()]);
    }

    public static String detectAssetType (String epc){
        if(epc.startsWith(Filters.RFID_BIN))
            return AssetType.HARVEST_BIN.name();
        else if(epc.startsWith(Filters.RFID_CAGE))
            return AssetType.CAGE.name();
        else if(epc.startsWith(Filters.RFID_NET))
            return AssetType.NET.name();
        else if(epc.startsWith(Filters.RFID_PLATFORM))
            return AssetType.PLATFORM.name();

        return "UNKNOWN";
    }
}
