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

import io.agritrack.data.model.tx.CollectTransaction;
import io.agritrack.data.model.tx.items.CollectionTxWithItems;

@Dao
public interface CollectTransactionDAO {

    @Transaction
    @Query("SELECT * from collect_transaction")
    LiveData<List<CollectionTxWithItems>> getAll();

    @Transaction
    @Query("SELECT * from collect_transaction where id=:collectingTransactionId LIMIT 1")
    CollectionTxWithItems getById(Long collectingTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long[] insert(CollectTransaction... collectTransactions);

    @Delete
    void delete(CollectTransaction collectTransaction);

    @Query("DELETE from collect_transaction")
    void deleteAll();

    @Update
    void update(CollectTransaction collectTransaction);
}

