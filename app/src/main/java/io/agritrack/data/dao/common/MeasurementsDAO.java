package io.agritrack.data.dao.common;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import io.agritrack.data.model.common.Measurements;

@Dao
public interface MeasurementsDAO {

    @Query("SELECT * from measurements")
    LiveData<List<Measurements>> getAll();

    @Query("SELECT * from measurements where id=:measurementsId LIMIT 1")
    Measurements getById(Long measurementsId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Measurements... measurementss);

    @Delete
    void delete(Measurements measurements);

    @Query("DELETE from measurements")
    void deleteAll();

    @Update
    void update(Measurements measurements);
}
