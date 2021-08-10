package io.agritrack.fishtrack.data.dao.wh;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.Site;
import io.agritrack.fishtrack.data.model.wh.Asset;

@Dao
public interface AssetDAO {

    @Query("SELECT * from asset")
    LiveData<List<Asset>> getAll();

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
