package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.HarvestTransaction;

@Dao
public interface HarvestTransactionDAO {

    @Query("SELECT * from harvest_transaction")
    LiveData<List<HarvestTransaction>> getAll();

    @Query("SELECT * from harvest_transaction where id=:harvestTransactionId LIMIT 1")
    HarvestTransaction getById(Long harvestTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HarvestTransaction... harvestTransactions);

    @Delete
    void delete(HarvestTransaction harvestTransaction);

    @Query("DELETE from harvest_transaction")
    void deleteAll();

    @Update
    void update(HarvestTransaction harvestTransaction);
}
