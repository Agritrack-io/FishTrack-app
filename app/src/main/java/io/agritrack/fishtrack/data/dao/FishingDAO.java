package io.agritrack.fishtrack.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.Fishing;

@Dao
public interface FishingDAO {

    @Query("SELECT * from fishing")
    LiveData<List<Fishing>> getAll();

    @Query("SELECT * from fishing where id=:fishingId LIMIT 1")
    Fishing getById(Long fishingId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Fishing... fishings);

    @Delete
    void delete(Fishing fishing);

    @Query("DELETE from fishing")
    void deleteAll();

    @Update
    void update(Fishing fishing);
}
