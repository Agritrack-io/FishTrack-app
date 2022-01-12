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

import io.agritrack.data.model.common.Measurement;
import io.agritrack.data.model.common.TemperatureTimeSeries;

@Dao
public interface MeasurementsDAO {

    @Transaction
    @Query("SELECT * from measurements")
    LiveData<List<TemperatureTimeSeries>> getAll();

    @Transaction
    @Query("SELECT * from measurements where id=:measurementsId LIMIT 1")
    TemperatureTimeSeries getById(Long measurementsId);

    @Transaction
    @Query("SELECT * from measurements where retrieved_at=:retrievedAt")
    LiveData<List<TemperatureTimeSeries>> getByRetrievalDate(Long retrievedAt);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Measurement... measurements);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Measurement measurement);

    @Delete
    void delete(Measurement measurement);

    @Query("DELETE from measurements")
    void deleteAll();

    @Update
    void update(Measurement measurement);
}
