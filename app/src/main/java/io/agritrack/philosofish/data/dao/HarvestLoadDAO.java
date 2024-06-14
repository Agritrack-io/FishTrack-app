package io.agritrack.philosofish.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.philosofish.data.model.HarvestLoad;

@Dao
public interface HarvestLoadDAO {

    @Query("SELECT * from harvest_load")
    LiveData<List<HarvestLoad>> getAll();

    @Query("SELECT * from harvest_load where id=:harvestLoadId LIMIT 1")
    HarvestLoad getById(Long harvestLoadId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HarvestLoad... harvestLoads);

    @Delete
    void delete(HarvestLoad harvestLoad);

    @Query("DELETE from harvest_load")
    void deleteAll();

    @Update
    void update(HarvestLoad harvestLoad);
}
