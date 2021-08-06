package io.agritrack.fishtrack.data.dao.wh;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.wh.Consumption;

@Dao
public interface ConsumptionDAO {

    @Query("SELECT * from consumption")
    LiveData<List<Consumption>> getAll();

    @Query("SELECT * from consumption where id=:consumptionId LIMIT 1")
    Consumption getById(Long consumptionId);

    @Insert
    void insert(Consumption... consumptions);

    @Delete
    void delete(Consumption consumption);

    @Query("DELETE from consumption")
    void deleteAll();

    @Update
    void update(Consumption consumption);
}
