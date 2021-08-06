package io.agritrack.fishtrack.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.Flot;

@Dao
public interface FlotDAO {

    @Query("SELECT * from flot")
    LiveData<List<Flot>> getAll();

    @Query("SELECT * from flot where id=:flotId LIMIT 1")
    Flot getById(Long flotId);

    @Insert
    void insert(Flot... flots);

    @Delete
    void delete(Flot flot);

    @Query("DELETE from flot")
    void deleteAll();

    @Update
    void update(Flot flot);
}
