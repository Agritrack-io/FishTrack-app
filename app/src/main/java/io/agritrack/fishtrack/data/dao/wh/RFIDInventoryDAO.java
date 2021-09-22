package io.agritrack.fishtrack.data.dao.wh;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.wh.RFIDInventory;

@Dao
public interface RFIDInventoryDAO {

    @Query("SELECT * from rfid_inventory")
    LiveData<List<RFIDInventory>> getAll();

    @Query("SELECT * from rfid_inventory where id=:inventoryId LIMIT 1")
    RFIDInventory getById(Long inventoryId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RFIDInventory... inventorys);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(RFIDInventory inventory);

    @Delete
    void delete(RFIDInventory inventory);

    @Query("DELETE from rfid_inventory")
    void deleteAll();

    @Update
    void update(RFIDInventory inventory);
}
