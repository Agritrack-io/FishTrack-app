package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.CollectTransaction;

@Dao
public interface CollectTransactionDAO {

    @Query("SELECT * from collect_transaction")
    LiveData<List<CollectTransaction>> getAll();

    @Query("SELECT * from collect_transaction where id=:collectingTransactionId LIMIT 1")
    CollectTransaction getById(Long collectingTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CollectTransaction... collectTransactions);

    @Delete
    void delete(CollectTransaction collectTransaction);

    @Query("DELETE from collect_transaction")
    void deleteAll();

    @Update
    void update(CollectTransaction collectTransaction);
}

