package io.agritrack.data.dao.tx;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.FishingTransaction;

@Dao
public interface FishingTransactionDAO {

    @Query("SELECT * from fishing_transaction")
    List<FishingTransaction> getAll();

    @Query("SELECT * from fishing_transaction where id=:fishingTransactionId LIMIT 1")
    FishingTransaction getById(Long fishingTransactionId);

    @Query("SELECT * from fishing_transaction where user_name=:userName and status='NONE' or status='PENDING' LIMIT 1")
    FishingTransaction getMostRecentOpenTx(String userName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FishingTransaction... fishingTransactions);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FishingTransaction fishingTransaction);

    @Delete
    void delete(FishingTransaction fishingTransaction);

    @Query("DELETE from fishing_transaction")
    int deleteAll();

    @Update
    void update(FishingTransaction fishingTransaction);
}
