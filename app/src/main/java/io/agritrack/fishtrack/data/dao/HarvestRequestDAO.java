package io.agritrack.fishtrack.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.HarvestRequest;

@Dao
public interface HarvestRequestDAO {

    @Query("SELECT * from harvest_request")
    LiveData<List<HarvestRequest>> getAll();

    @Query("SELECT * from harvest_request where id=:harvestRequestId LIMIT 1")
    HarvestRequest getById(Long harvestRequestId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HarvestRequest... harvestRequests);

    @Delete
    void delete(HarvestRequest harvestRequest);

    @Query("DELETE from harvest_request")
    void deleteAll();

    @Update
    void update(HarvestRequest harvestRequest);
}
