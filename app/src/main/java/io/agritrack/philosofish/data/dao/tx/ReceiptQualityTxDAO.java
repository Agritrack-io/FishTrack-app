package io.agritrack.philosofish.data.dao.tx;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.philosofish.data.model.tx.ReceiptQualityTransaction;

@Dao
public interface ReceiptQualityTxDAO {

    @Query("SELECT * from receipt_quality_transaction")
    List<ReceiptQualityTransaction> getAll();

    @Query("SELECT * from receipt_quality_transaction where lot=:lot LIMIT 1")
    ReceiptQualityTransaction getByLot(String lot);

    @Query("DELETE FROM receipt_quality_transaction WHERE lot=:lot")
    int deleteByLot(String lot);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReceiptQualityTransaction... qualityTxs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ReceiptQualityTransaction qualityTx);

    @Delete
    void delete(ReceiptQualityTransaction qualityTx);

    @Query("DELETE from receipt_quality_transaction")
    int deleteAll();

    @Query("DELETE from receipt_quality_transaction where created_at<:threeDaysAgo")
    int deleteThreeDaysOld(Long threeDaysAgo);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(ReceiptQualityTransaction qualityTx);
}