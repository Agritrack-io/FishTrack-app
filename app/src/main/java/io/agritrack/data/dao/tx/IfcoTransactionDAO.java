package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.IfcoTransaction;

@Dao
public interface IfcoTransactionDAO {

    @Query("SELECT * from ifco_transaction")
    LiveData<List<IfcoTransaction>> getAll();

    @Query("SELECT * from ifco_transaction where id=:shipItemTransactionId LIMIT 1")
    IfcoTransaction getById(Long shipItemTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(IfcoTransaction... ifcoTransactions);

    @Delete
    void delete(IfcoTransaction ifcoTransaction);

    @Query("DELETE from ifco_transaction")
    void deleteAll();

    @Update
    void update(IfcoTransaction ifcoTransaction);
}
