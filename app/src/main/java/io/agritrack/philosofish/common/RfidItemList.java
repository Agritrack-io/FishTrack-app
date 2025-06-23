package io.agritrack.philosofish.common;

import androidx.room.TypeConverters;

import java.util.List;

import io.agritrack.philosofish.data.converter.StringListConverter;


public class RfidItemList {
    @TypeConverters(StringListConverter.class)
    public List<String> rfid_items;
}