package io.agritrack.data.dao.wh;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.wh.Asset;

@Dao
public interface AssetDAO {

    @Query("SELECT * from asset")
    List<Asset> getAll();

    @Query("SELECT * from asset where upper(asset_type)=:assetType LIMIT 100")
    List<Asset> getAssetsForType(String assetType);

    @Query("SELECT * from asset where id=:assetId LIMIT 1")
    Asset getById(Long assetId);

    @Query("SELECT * from asset where rfid_barcode=:epcStr LIMIT 1")
    Asset getAssetByEpc(String epcStr);

    @Query("SELECT id, rfid from asset where rfid=:epcStr LIMIT 1")
    Cursor getAssetCursorByEpc(String epcStr);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Asset... assets);

    @Delete
    void delete(Asset asset);

    @Query("DELETE from asset")
    void deleteAll();

    @Update
    void update(Asset asset);
}
