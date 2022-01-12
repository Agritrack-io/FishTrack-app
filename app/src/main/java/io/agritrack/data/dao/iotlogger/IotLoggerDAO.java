package io.agritrack.data.dao.iotlogger;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.common.IotLogger;

@Dao
public interface IotLoggerDAO {
    @Query("SELECT * from iot_logger")
    LiveData<List<IotLogger>> getAll();

    @Query("SELECT * from iot_logger where id=:iotLoggerId LIMIT 1")
    IotLogger getById(Long iotLoggerId);

    @Query("SELECT * from iot_logger where asset_rfid LIKE '%' || :epc || '%' LIMIT 1")
    IotLogger getByAssetRFID(String epc);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(IotLogger... iotLoggers);

    @Delete
    void delete(IotLogger iotLogger);

    @Query("DELETE from iot_logger")
    void deleteAll();

    @Update
    void update(IotLogger iotLogger);
}
