package io.agritrack.fishtrack.data.dao.common;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.common.FishSpecies;

@Dao
public interface FishSpeciesDAO {

    @Query("SELECT * from species")
    LiveData<List<FishSpecies>> getAll();

    @Query("SELECT * from species where id=:fishSpeciesId LIMIT 1")
    FishSpecies getById(Long fishSpeciesId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FishSpecies... fishSpeciess);

    @Delete
    void delete(FishSpecies fishSpecies);

    @Query("DELETE from species")
    void deleteAll();

    @Update
    void update(FishSpecies fishSpecies);
}
