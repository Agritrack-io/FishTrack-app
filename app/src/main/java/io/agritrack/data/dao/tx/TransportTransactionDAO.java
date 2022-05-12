package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.FishingTransaction;
import io.agritrack.data.model.tx.TransportTransaction;

@Dao
public interface TransportTransactionDAO {

    @Query("SELECT * from transport_transaction")
    LiveData<List<TransportTransaction>> getAll();

    @Query("SELECT * from transport_transaction where id=:transportId LIMIT 1")
    TransportTransaction getById(Long transportId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TransportTransaction... transports);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TransportTransaction transportTransaction);

    @Delete
    void delete(TransportTransaction transport);

    @Query("DELETE from transport_transaction")
    void deleteAll();

    @Update
    void update(TransportTransaction transport);
}
