package io.agritrack.fishtrack.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.fishtrack.enums.AssetType;

public class FishTrackUtils {
    public static String[] assetTypes(String firstItem) {
        List<String> assetTypeList = Arrays.stream(AssetType.values()).map(x -> x.name()).collect(Collectors.toList());
        assetTypeList.add(0, firstItem);
        return assetTypeList.toArray(new String[assetTypeList.size()]);
    }
}
