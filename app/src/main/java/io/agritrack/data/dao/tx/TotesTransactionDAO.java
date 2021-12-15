package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.TotesTransaction;

@Dao
public interface TotesTransactionDAO {
    @Query("SELECT * from totes_transaction")
    LiveData<List<TotesTransaction>> getAll();

    @Query("SELECT * from totes_transaction where id=:shipItemTransactionId LIMIT 1")
    TotesTransaction getById(Long shipItemTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TotesTransaction... totesTransactions);

    @Delete
    void delete(TotesTransaction totesTransaction);

    @Query("DELETE from totes_transaction")
    void deleteAll();

    @Update
    void update(TotesTransaction totesTransaction);
}
