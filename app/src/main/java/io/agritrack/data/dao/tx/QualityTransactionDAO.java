package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.QualityTransaction;

@Dao
public interface QualityTransactionDAO {

    @Query("SELECT * from quality_transaction")
    LiveData<List<QualityTransaction>> getAll();

    @Query("SELECT * from quality_transaction where id=:qualityTxId LIMIT 1")
    QualityTransaction getById(Long qualityTxId);

    @Insert
    void insert(QualityTransaction... qualityTxs);

    @Delete
    void delete(QualityTransaction qualityTx);

    @Query("DELETE from quality_transaction")
    void deleteAll();

    @Update
    void update(QualityTransaction qualityTx);
}
