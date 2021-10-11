package io.agritrack.data.dao.wh;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.wh.ExpectedRFIDInventory;

@Dao
public interface ExpectedRFIDInventoryDAO {

    @Query("SELECT * from expected_rfid_inventory")
    LiveData<List<ExpectedRFIDInventory>> getAll();

    @Query("SELECT * from expected_rfid_inventory where id=:expectedRFIDInventoryId LIMIT 1")
    ExpectedRFIDInventory getById(Long expectedRFIDInventoryId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ExpectedRFIDInventory... expectedRFIDInventorys);

    @Delete
    void delete(ExpectedRFIDInventory expectedRFIDInventory);

    @Query("DELETE from expected_rfid_inventory")
    void deleteAll();

    @Update
    void update(ExpectedRFIDInventory expectedRFIDInventory);
}
