package io.agritrack.fishtrack.data.dao.wh;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.wh.RFIDInventoryItem;

@Dao
public interface RFIDInventoryItemDAO {
    
    @Query("SELECT * from rfid_inventory_item")
    LiveData<List<RFIDInventoryItem>> getAll();

    @Query("SELECT * from rfid_inventory_item where id=:rFIDInventoryItemId LIMIT 1")
    RFIDInventoryItem getById(Long rFIDInventoryItemId);

    @Insert
    void insert(RFIDInventoryItem... rFIDInventoryItems);

    @Delete
    void delete(RFIDInventoryItem rFIDInventoryItem);

    @Query("DELETE from rfid_inventory_item")
    void deleteAll();

    @Update
    void update(RFIDInventoryItem rFIDInventoryItem);
}
