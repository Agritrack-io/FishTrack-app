package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.StorageTransaction;
import io.agritrack.data.model.tx.items.StorageTxWithItems;

@Dao
public interface StorageTransactionDAO {

    @Transaction
    @Query("SELECT * from storage_transaction")
    LiveData<List<StorageTxWithItems>> getAll();

    @Transaction
    @Query("SELECT * from storage_transaction where id=:storageTransactionId LIMIT 1")
    StorageTxWithItems getById(Long storageTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long[] insert(StorageTransaction... storageTransactions);

    @Delete
    void delete(StorageTransaction storageTransaction);

    @Query("DELETE from storage_transaction")
    void deleteAll();

    @Update
    void update(StorageTransaction storageTransaction);
}
