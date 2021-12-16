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

import io.agritrack.data.model.tx.ConsumableTransaction;
import io.agritrack.data.model.tx.items.IncomingTxWithItems;

@Dao
public interface ConsumableTransactionDAO {

    @Query("SELECT * from consumable_transaction")
    LiveData<List<ConsumableTransaction>> getAll();

    @Transaction
    @Query("SELECT * from consumable_transaction")
    LiveData<List<IncomingTxWithItems>> getAllIncoming();

    @Query("SELECT * from consumable_transaction where id=:consumableTransactionId LIMIT 1")
    ConsumableTransaction getById(Long consumableTransactionId);

    @Transaction
    @Query("SELECT * from consumable_transaction where id=:consumableTransactionId LIMIT 1")
    IncomingTxWithItems getByIdIncoming(Long consumableTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ConsumableTransaction... consumableTransactions);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long[] insertIncoming(ConsumableTransaction... consumableTransactions);

    @Delete
    void delete(ConsumableTransaction consumableTransaction);

    @Query("DELETE from consumable_transaction")
    void deleteAll();

    @Update
    void update(ConsumableTransaction consumableTransaction);
}
