package io.agritrack.philosofish.data.dao.tx;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.UUID;

import io.agritrack.philosofish.data.model.tx.FishingTransaction;

@Dao
public interface FishingTransactionDAO {

    @Query("SELECT * from fishing_transaction")
    List<FishingTransaction> getAll();

    @Query("SELECT * from fishing_transaction where status='COMPLETED'")
    List<FishingTransaction> getAllCompleted();

    @Query("SELECT * from fishing_transaction where id=:fishingTransactionId LIMIT 1")
    FishingTransaction getById(UUID fishingTransactionId);

    @Query("SELECT * from fishing_transaction where user_name=:userName and status='NONE' or status='PENDING' order by created_at desc LIMIT 1")
    FishingTransaction getMostRecentOpenTx(String userName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FishingTransaction... fishingTransactions);

    @Delete
    void delete(FishingTransaction fishingTransaction);

    @Query("DELETE from fishing_transaction")
    int deleteAll();

    @Query("DELETE from fishing_transaction where status='COMPLETED'")
    int deleteAllCompleted();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(FishingTransaction fishingTransaction);
}
