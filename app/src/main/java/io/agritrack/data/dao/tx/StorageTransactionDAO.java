package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.StorageTransaction;

@Dao
public interface StorageTransactionDAO {

    @Query("SELECT * from storage_transaction")
    LiveData<List<StorageTransaction>> getAll();

    @Query("SELECT * from storage_transaction where id=:storageTransactionId LIMIT 1")
    StorageTransaction getById(Long storageTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StorageTransaction... storageTransactions);

    @Delete
    void delete(StorageTransaction storageTransaction);

    @Query("DELETE from storage_transaction")
    void deleteAll();

    @Update
    void update(StorageTransaction storageTransaction);
}
