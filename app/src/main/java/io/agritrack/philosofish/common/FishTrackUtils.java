package io.agritrack.philosofish.common;

import android.annotation.SuppressLint;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.regex.Pattern;

import io.agritrack.philosofish.enums.AssetType;

public class FishTrackUtils {
    private static final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

    public static String[] assetTypes(String firstItem) {
        List<String> assetTypeList = AssetType.values();
        assetTypeList.add(0, firstItem);
        return assetTypeList.toArray(new String[assetTypeList.size()]);
    }

    @SuppressLint("NewApi")
    public static LocalDateTime LotToDate(String lot) {
        WeekFields weekFields = WeekFields.of(Constants.Greek_Locale);
        String week = lot.substring(0, 2);
        String day = Character.toString(lot.charAt(2));
        Long longWeek = Long.valueOf(week);
        Long longDay = Long.valueOf(day);
        LocalDateTime dateStart = LocalDateTime.now()
                .with(weekFields.weekOfYear(), longWeek)
                .with(weekFields.dayOfWeek(), longDay)
                .withHour(0).withMinute(0).withSecond(0);

        return dateStart;
    }
}
