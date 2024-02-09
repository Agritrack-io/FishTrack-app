package io.agritrack.data.dao.iotlogger;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;
import java.util.UUID;

import io.agritrack.data.model.common.Measurement;
import io.agritrack.data.model.common.TemperatureTimeSeries;

@Dao
public interface MeasurementsDAO {

    @Transaction
    @Query("SELECT * from measurements")
    List<TemperatureTimeSeries> getAll();

    @Transaction
    @Query("SELECT * from measurements where id=:measurementsId LIMIT 1")
    TemperatureTimeSeries getById(UUID measurementsId);

    @Transaction
    @Query("SELECT * from measurements where asset_rfid=:epc order by retrieved_at desc LIMIT 1")
    TemperatureTimeSeries getByEPC(String epc);

    @Transaction
    @Query("SELECT * from measurements where retrieved_at=:retrievedAt")
    LiveData<List<TemperatureTimeSeries>> getByRetrievalDate(Long retrievedAt);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Measurement... measurements);

    @Delete
    void delete(Measurement measurement);

    @Query("DELETE from measurements")
    int deleteAll();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Measurement measurement);
}
