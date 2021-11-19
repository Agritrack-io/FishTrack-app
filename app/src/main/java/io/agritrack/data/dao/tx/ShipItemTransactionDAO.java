package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.ShipItemTransaction;

@Dao
public interface ShipItemTransactionDAO {
    @Query("SELECT * from ship_item_tx")
    LiveData<List<ShipItemTransaction>> getAll();

    @Query("SELECT * from ship_item_tx where id=:shipItemTransactionId LIMIT 1")
    ShipItemTransaction getById(Long shipItemTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ShipItemTransaction... shipItemTransactions);

    @Delete
    void delete(ShipItemTransaction shipItemTransaction);

    @Query("DELETE from ship_item_tx")
    void deleteAll();

    @Update
    void update(ShipItemTransaction shipItemTransaction);
}
