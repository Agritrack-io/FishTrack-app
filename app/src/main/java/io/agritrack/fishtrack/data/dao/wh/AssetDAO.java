package io.agritrack.fishtrack.data.dao.wh;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.wh.Asset;

@Dao
public interface AssetDAO {

    @Query("SELECT * from asset")
    List<Asset> getAll();

    @Query("SELECT * from asset where asset_type=:assetType LIMIT 100")
    List<Asset> getAssetsForType(String assetType);

    @Query("SELECT * from asset where id=:assetId LIMIT 1")
    Asset getById(Long assetId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Asset... assets);

    @Delete
    void delete(Asset asset);

    @Query("DELETE from asset")
    void deleteAll();

    @Update
    void update(Asset asset);
}
