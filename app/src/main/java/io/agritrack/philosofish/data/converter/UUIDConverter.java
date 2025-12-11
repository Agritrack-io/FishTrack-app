package io.agritrack.philosofish.data.converter;

import androidx.room.TypeConverter;

import java.util.UUID;

public class UUIDConverter {
    @TypeConverter
    public static String fromUUID(UUID uuid) {
        if (uuid != null) {
            return uuid.toString();
        } else {
            return null;
        }
    }

    @TypeConverter
    public static UUID uuidFromString(String string) {
        return UUID.fromString(string);
    }
}
