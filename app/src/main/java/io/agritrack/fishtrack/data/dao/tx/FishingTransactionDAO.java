package io.agritrack.fishtrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.tx.FishingTransaction;

@Dao
public interface FishingTransactionDAO {

    @Query("SELECT * from fishing_transaction")
    LiveData<List<FishingTransaction>> getAll();

    @Query("SELECT * from fishing_transaction where id=:fishingTransactionId LIMIT 1")
    FishingTransaction getById(Long fishingTransactionId);

    @Query("SELECT * from fishing_transaction where status='NONE' or status='PENDING' LIMIT 1")
    FishingTransaction getMostRecentOpenTx();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FishingTransaction... fishingTransactions);

    @Delete
    void delete(FishingTransaction fishingTransaction);

    @Query("DELETE from fishing_transaction")
    void deleteAll();

    @Update
    void update(FishingTransaction fishingTransaction);
}
