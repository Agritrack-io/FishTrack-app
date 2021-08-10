package io.agritrack.fishtrack.data.dao.wh;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.wh.Inventory;

@Dao
public interface InventoryDAO {

    @Query("SELECT * from inventory")
    LiveData<List<Inventory>> getAll();

    @Query("SELECT * from inventory where id=:inventoryId LIMIT 1")
    Inventory getById(Long inventoryId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Inventory... inventorys);

    @Delete
    void delete(Inventory inventory);

    @Query("DELETE from inventory")
    void deleteAll();

    @Update
    void update(Inventory inventory);
}
