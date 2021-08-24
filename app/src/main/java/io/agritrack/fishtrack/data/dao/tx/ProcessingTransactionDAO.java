package io.agritrack.fishtrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.tx.ProcessingTransaction;

@Dao
public interface ProcessingTransactionDAO {

    @Query("SELECT * from process_transaction")
    LiveData<List<ProcessingTransaction>> getAll();

    @Query("SELECT * from process_transaction where id=:processingTxId LIMIT 1")
    ProcessingTransaction getById(Long processingTxId);

    @Insert
    void insert(ProcessingTransaction... processingTxs);

    @Delete
    void delete(ProcessingTransaction processingTx);

    @Query("DELETE from process_transaction")
    void deleteAll();

    @Update
    void update(ProcessingTransaction processingTx);
}
