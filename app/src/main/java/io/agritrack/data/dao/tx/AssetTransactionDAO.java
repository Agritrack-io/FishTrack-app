package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.AssetTransaction;
import io.agritrack.data.model.tx.TransportTransaction;

@Dao
public interface AssetTransactionDAO {

    @Query("SELECT * from asset_transaction")
    List<AssetTransaction> getAll();

    @Query("SELECT * from asset_transaction where id=:assetTransactionId LIMIT 1")
    AssetTransaction getById(Long assetTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AssetTransaction... assetTransactions);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AssetTransaction assetTransaction);

    @Delete
    void delete(AssetTransaction assetTransaction);

    @Query("DELETE from asset_transaction")
    int deleteAll();

    @Update
    void update(AssetTransaction assetTransaction);
}
