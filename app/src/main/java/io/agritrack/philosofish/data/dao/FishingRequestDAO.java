package io.agritrack.philosofish.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.philosofish.data.model.FishingRequest;

@Dao
public interface FishingRequestDAO {

    @Query("SELECT * from fishing_request")
    List<FishingRequest> getAll();

    @Query("SELECT * from fishing_request where DATE(harvest_date) = DATE('now')")
    List<FishingRequest> getTodayRecord();

    @Query("SELECT * from fishing_request where DATE(harvest_date) = DATE('now','-1 day')")
    List<FishingRequest> getYesterdayRecord();

    @Query("SELECT * from fishing_request where harvest_date LIKE '%' || :date || '%'")
    List<FishingRequest> getByDate(String date);

    @Query("SELECT * from fishing_request WHERE DATE(harvest_date) < DATE('now','-1 day') ORDER BY harvest_date DESC")
    List<FishingRequest> getPreviousRecord();

    @Query("SELECT * from fishing_request where request_id=:fishingRequestId LIMIT 1")
    FishingRequest getById(String fishingRequestId);

    @Query("SELECT * from fishing_request where request_id LIKE '%' || :harvReq || '%'")
    List<FishingRequest> getAllFishReqWithSameHarvReq(String harvReq);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FishingRequest... fishingRequests);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FishingRequest fishingRequest);

    @Delete
    void delete(FishingRequest fishingRequest);

    @Query("DELETE from fishing_request")
    void deleteAll();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(FishingRequest fishingRequest);
}
