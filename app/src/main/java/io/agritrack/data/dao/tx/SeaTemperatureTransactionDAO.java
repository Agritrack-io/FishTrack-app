package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.agritrack.data.model.FishingRequest;
import io.agritrack.data.model.tx.SeaTemperatureTransaction;

@Dao
public interface SeaTemperatureTransactionDAO {

    @Query("SELECT * from sea_temperature_transaction order by timestamp desc")
    List<SeaTemperatureTransaction> getAll();

    @Query("SELECT * from sea_temperature_transaction where id=:seaTempTransactionId LIMIT 1")
    SeaTemperatureTransaction getById(Long seaTempTransactionId);

    @Query("SELECT avg(ref_temperature) as av from sea_temperature_transaction where timestamp >= :millis ORDER by timestamp DESC")
    Double getAv(Long millis);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SeaTemperatureTransaction... seaTemperatureTransactions);

    @Delete
    void delete(SeaTemperatureTransaction seaTemperatureTransaction);

    @Query("DELETE from sea_temperature_transaction")
    void deleteAll();

    @Update
    void update(SeaTemperatureTransaction seaTemperatureTransaction);
}
