package io.agritrack.fishtrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.tx.RepairTransaction;

@Dao
public interface RepairTransactionDAO {

    @Query("SELECT * from repair_transaction")
    LiveData<List<RepairTransaction>> getAll();

    @Query("SELECT * from repair_transaction where id=:repairTransactionId LIMIT 1")
    RepairTransaction getById(Long repairTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RepairTransaction... repairTransactions);

    @Delete
    void delete(RepairTransaction repairTransaction);

    @Query("DELETE from repair_transaction")
    void deleteAll();

    @Update
    void update(RepairTransaction repairTransaction);
}
