package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.CorrelationTransaction;

@Dao
public interface CorrelationTransactionDAO {

    @Query("SELECT * from correlation_transaction")
    LiveData<List<CorrelationTransaction>> getAll();

    @Query("SELECT * from correlation_transaction where id=:correlationTransactionId LIMIT 1")
    CorrelationTransaction getById(Long correlationTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CorrelationTransaction... correlationTransactions);

    @Delete
    void delete(CorrelationTransaction correlationTransaction);

    @Query("DELETE from correlation_transaction")
    void deleteAll();

    @Update
    void update(CorrelationTransaction correlationTransaction);
}
