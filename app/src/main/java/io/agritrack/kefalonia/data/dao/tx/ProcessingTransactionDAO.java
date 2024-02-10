package io.agritrack.kefalonia.data.dao.tx;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.UUID;

import io.agritrack.kefalonia.data.model.tx.ProcessingTransaction;

@Dao
public interface ProcessingTransactionDAO {

    @Query("SELECT * from process_transaction")
    List<ProcessingTransaction> getAll();

    @Query("SELECT * from process_transaction where id=:processingTxId LIMIT 1")
    ProcessingTransaction getById(UUID processingTxId);

    @Query("SELECT * from process_transaction where hash_code=:hashCode LIMIT 1")
    ProcessingTransaction getByHash(Integer hashCode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProcessingTransaction... processingTxs);

    @Delete
    void delete(ProcessingTransaction processingTx);

    @Query("DELETE from process_transaction")
    int deleteAll();

    @Update
    void update(ProcessingTransaction processingTx);

    @Query("SELECT count(*) FROM process_transaction WHERE hash_code=:hashCode")
    int countByHash(Integer hashCode);
}
