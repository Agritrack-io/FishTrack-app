package io.agritrack.data.dao.tx;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.FishingTransaction;
import io.agritrack.data.model.tx.QualityTransaction;

@Dao
public interface QualityTransactionDAO {

    @Query("SELECT * from quality_transaction")
    List<QualityTransaction> getAll();

    @Query("SELECT * from quality_transaction where id=:qualityTxId LIMIT 1")
    QualityTransaction getById(Long qualityTxId);

    @Query("SELECT * from quality_transaction where user_name=:userName and status='NONE' or status='PENDING' order by timestamp desc LIMIT 1")
    QualityTransaction getMostRecentOpenTx(String userName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(QualityTransaction... qualityTxs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(QualityTransaction qualityTx);

    @Delete
    void delete(QualityTransaction qualityTx);

    @Query("DELETE from quality_transaction")
    int deleteAll();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(QualityTransaction qualityTx);
}
