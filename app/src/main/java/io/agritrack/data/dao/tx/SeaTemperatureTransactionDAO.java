package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.FishingRequest;
import io.agritrack.data.model.tx.SeaTemperatureTransaction;

@Dao
public interface SeaTemperatureTransactionDAO {

    @Query("SELECT * from sea_temperature_transaction")
    LiveData<List<SeaTemperatureTransaction>> getAll();

    @Query("SELECT * from sea_temperature_transaction where id=:seaTempTransactionId LIMIT 1")
    SeaTemperatureTransaction getById(Long seaTempTransactionId);

    @Query("SELECT * from sea_temperature_transaction WHERE DATE(timestamp) >= DATE('now','-3 day') ORDER BY timestamp DESC")
    List<SeaTemperatureTransaction> getLastThreeDaysRecord();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SeaTemperatureTransaction... seaTemperatureTransactions);

    @Delete
    void delete(SeaTemperatureTransaction seaTemperatureTransaction);

    @Query("DELETE from sea_temperature_transaction")
    void deleteAll();

    @Update
    void update(SeaTemperatureTransaction seaTemperatureTransaction);
}
