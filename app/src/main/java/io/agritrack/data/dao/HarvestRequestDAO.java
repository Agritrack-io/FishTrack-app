package io.agritrack.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.HarvestRequest;

@Dao
public interface HarvestRequestDAO {

    @Query("SELECT * from harvest_request")
    List<HarvestRequest> getAll();

    @Query("SELECT * from harvest_request where DATE(harvest_date) = DATE('now')")
    List<HarvestRequest> getTodayRecord();

    @Query("SELECT * from harvest_request where DATE(harvest_date) = DATE('now','-1 day')")
    List<HarvestRequest> getYesterdayRecord();

    @Query("SELECT * from harvest_request where harvest_date LIKE '%' || :date || '%'")
    List<HarvestRequest> getByDate(String date);

    @Query("SELECT * from harvest_request WHERE DATE(harvest_date) < DATE('now','-1 day') ORDER BY harvest_date DESC")
    List<HarvestRequest> getPreviousRecord();

    @Query("SELECT * from harvest_request where request_id=:harvestRequestId LIMIT 1")
    HarvestRequest getById(String harvestRequestId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HarvestRequest... harvestRequests);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(HarvestRequest harvestRequest);

    @Delete
    void delete(HarvestRequest harvestRequest);

    @Query("DELETE from harvest_request")
    void deleteAll();

    @Update
    void update(HarvestRequest harvestRequest);
}
