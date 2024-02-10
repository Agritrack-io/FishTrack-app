package io.agritrack.kefalonia.data.dao.iotlogger;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.kefalonia.data.model.common.TemperatureData;

@Dao
public interface TemperatureDataDAO {

    @Query("SELECT * from temperature_data")
    LiveData<List<TemperatureData>> getAll();

    @Query("SELECT * from temperature_data where measurement_id=:mId")
    List<TemperatureData> getByMeasurementId(Long mId);

    @Query("SELECT * from temperature_data where id=:temperatureId LIMIT 1")
    TemperatureData getById(Long temperatureId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insert(TemperatureData... items);

    @Delete
    void delete(TemperatureData item);

    @Query("DELETE from temperature_data")
    void deleteAll();

    @Query("DELETE from temperature_data where measurement_id=:mId")
    void deleteByMeasurementId(Long mId);

    @Update
    void update(TemperatureData item);
}
