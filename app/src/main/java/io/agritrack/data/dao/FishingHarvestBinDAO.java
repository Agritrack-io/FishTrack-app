package io.agritrack.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.FishingHarvestBin;

@Dao
public interface FishingHarvestBinDAO {

    @Query("SELECT * from fishing_harvest_bin")
    LiveData<List<FishingHarvestBin>> getAll();

    @Query("SELECT * from fishing_harvest_bin where id=:fishingHarvestBinId LIMIT 1")
    FishingHarvestBin getById(Long fishingHarvestBinId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FishingHarvestBin... fishingHarvestBins);

    @Delete
    void delete(FishingHarvestBin fishingHarvestBin);

    @Query("DELETE from fishing_harvest_bin")
    void deleteAll();

    @Update
    void update(FishingHarvestBin fishingHarvestBin);
}
