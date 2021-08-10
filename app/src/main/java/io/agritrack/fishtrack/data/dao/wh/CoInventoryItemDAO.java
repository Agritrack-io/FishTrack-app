package io.agritrack.fishtrack.data.dao.wh;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.wh.CoInventoryItem;

@Dao
public interface CoInventoryItemDAO {

    @Query("SELECT * from co_inventory_item")
    LiveData<List<CoInventoryItem>> getAll();

    @Query("SELECT * from co_inventory_item where id=:coInventoryItemId LIMIT 1")
    CoInventoryItem getById(Long coInventoryItemId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CoInventoryItem... coInventoryItems);

    @Delete
    void delete(CoInventoryItem coInventoryItem);

    @Query("DELETE from co_inventory_item")
    void deleteAll();

    @Update
    void update(CoInventoryItem coInventoryItem);
}
