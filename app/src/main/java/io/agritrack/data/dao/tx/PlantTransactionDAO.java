package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.PlantTransaction;

@Dao
public interface PlantTransactionDAO {
    @Query("SELECT * from plant_transaction")
    LiveData<List<PlantTransaction>> getAll();

    @Query("SELECT * from plant_transaction where id=:plantTransactionId LIMIT 1")
    PlantTransaction getById(Long plantTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PlantTransaction... plantTransactions);

    @Delete
    void delete(PlantTransaction plantTransaction);

    @Query("DELETE from plant_transaction")
    void deleteAll();

    @Update
    void update(PlantTransaction plantTransaction);
}
