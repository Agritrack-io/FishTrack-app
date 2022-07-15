package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.ProcessingTransaction;

@Dao
public interface ProcessingTransactionDAO {

    @Query("SELECT * from process_transaction")
    List<ProcessingTransaction> getAll();

    @Query("SELECT * from process_transaction where id=:processingTxId LIMIT 1")
    ProcessingTransaction getById(Long processingTxId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProcessingTransaction... processingTxs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ProcessingTransaction processingTxs);

    @Delete
    void delete(ProcessingTransaction processingTx);

    @Query("DELETE from process_transaction")
    int deleteAll();

    @Update
    void update(ProcessingTransaction processingTx);
}
