package io.agritrack.fishtrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.tx.AssetTransaction;

@Dao
public interface AssetTransactionDAO {

    @Query("SELECT * from asset_transaction")
    LiveData<List<AssetTransaction>> getAll();

    @Query("SELECT * from asset_transaction where id=:assetTransactionId LIMIT 1")
    AssetTransaction getById(Long assetTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AssetTransaction... assetTransactions);

    @Delete
    void delete(AssetTransaction assetTransaction);

    @Query("DELETE from asset_transaction")
    void deleteAll();

    @Update
    void update(AssetTransaction assetTransaction);
}
