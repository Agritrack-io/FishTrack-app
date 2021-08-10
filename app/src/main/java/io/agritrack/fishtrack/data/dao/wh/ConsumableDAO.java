package io.agritrack.fishtrack.data.dao.wh;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.wh.Consumable;

@Dao
public interface ConsumableDAO {

    @Query("SELECT * from consumable")
    LiveData<List<Consumable>> getAll();

    @Query("SELECT * from consumable where id=:consumableId LIMIT 1")
    Consumable getById(Long consumableId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Consumable... consumables);

    @Delete
    void delete(Consumable consumable);

    @Query("DELETE from consumable")
    void deleteAll();

    @Update
    void update(Consumable consumable);
}
