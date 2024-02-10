package io.agritrack.kefalonia.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.kefalonia.data.model.CageDetails;

@Dao
public interface CageDetailsDAO {

    @Query("SELECT * from cage_details")
    LiveData<List<CageDetails>> getAll();

    @Query("SELECT * from cage_details where id=:detailId LIMIT 1")
    CageDetails getById(Long detailId);

    @Query("SELECT * from cage_details where asset_rfid=:rfId LIMIT 1")
    CageDetails getByRFId(String rfId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CageDetails... details);

    @Delete
    void delete(CageDetails detail);

    @Query("DELETE from cage_details")
    void deleteAll();

    @Update
    void update(CageDetails detail);
}
