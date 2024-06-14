package io.agritrack.philosofish.data.converter;

import androidx.room.TypeConverter;

import com.google.android.gms.common.util.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import io.agritrack.philosofish.fish.ui.bo.BinWeightRecord;

public class BinRecordConverter {
    @TypeConverter
    public static List<BinWeightRecord.BinRecord> fromString(String value) {
        if (Strings.isEmptyOrWhitespace(value)) {
            return new LinkedList<>();
        }
        Type binRecordType = new TypeToken<List<BinWeightRecord.BinRecord>>() {
        }.getType();
        List<BinWeightRecord.BinRecord> binsInfo = new Gson().fromJson(value, binRecordType);

        return binsInfo;
    }

    @TypeConverter
    public static String listToString(List<BinWeightRecord.BinRecord> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Gson gson = new Gson();
        return gson.toJson(list);
    }
}
