package io.agritrack.philosofish.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.philosofish.data.model.tx.AssetTxItem;

@Dao
public interface AssetTxItemDAO {

    @Query("SELECT * from asset_tx_item")
    LiveData<List<AssetTxItem>> getAll();

    @Query("SELECT * from asset_tx_item where itmId=:assetTxItemId LIMIT 1")
    AssetTxItem getById(Long assetTxItemId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AssetTxItem... assetTxItems);

    @Delete
    void delete(AssetTxItem assetTxItem);

    @Query("DELETE from asset_tx_item")
    void deleteAll();

    @Update
    void update(AssetTxItem assetTxItem);
}
