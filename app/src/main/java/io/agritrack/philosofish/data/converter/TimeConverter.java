package io.agritrack.philosofish.data.converter;

import androidx.room.TypeConverter;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TimeConverter {

    @TypeConverter
    public  static Time toTime(String timeString) {

        DateFormat formatter = new SimpleDateFormat("HH:mm");
        Time time = null;
        try {
             time = new Time(formatter.parse(timeString).getTime());
        } catch (Exception e) {
        }
        return time;
    }

    @TypeConverter
    public static String timeToString(Time time) {
        if (time == null) {
            return null;
        } else {
            return time.toString();
        }
    }
}
