package io.agritrack.fishtrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.tx.IncomingWHTransaction;

@Dao
public interface WHIncomingDAO {

    @Query("SELECT * from wh_incoming_transaction")
    LiveData<List<IncomingWHTransaction>> getAll();

    @Query("SELECT * from wh_incoming_transaction where id=:incomingWHTransactionId LIMIT 1")
    IncomingWHTransaction getById(Long incomingWHTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(IncomingWHTransaction... incomingWHTransactions);

    @Delete
    void delete(IncomingWHTransaction incomingWHTransaction);

    @Query("DELETE from harvest_transaction")
    void deleteAll();

    @Update
    void update(IncomingWHTransaction incomingWHTransaction);
}
