package io.agritrack.data.dao.common;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.common.Species;

@Dao
public interface SpeciesDAO {

    @Query("SELECT * from Species where type=:product order by id")
    List<Species> getAll(String product);

    @Query("SELECT * from Species where id=:speciesId LIMIT 1")
    Species getById(Long speciesId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Species... species);

    @Delete
    void delete(Species species);

    @Query("DELETE from Species")
    void deleteAll();

    @Update
    void update(Species species);
}
