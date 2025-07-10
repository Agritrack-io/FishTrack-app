package io.agritrack.philosofish.data.dao.wh;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.philosofish.data.model.wh.RFIDInventory;

@Dao
public interface RFIDInventoryDAO {

    @Query("SELECT * from rfid_inventory")
    List<RFIDInventory> getAll();

    @Query("SELECT * from rfid_inventory where uid=:inventoryId LIMIT 1")
    RFIDInventory getById(String inventoryId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RFIDInventory... inventorys);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertInvTotes(RFIDInventory... inventorys);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RFIDInventory inventory);

    @Delete
    void delete(RFIDInventory inventory);

    @Query("DELETE from rfid_inventory")
    int deleteAll();

    @Update
    void update(RFIDInventory inventory);

    @Query("DELETE FROM rfid_inventory WHERE uid = :inventoryId")
    int deleteById(String inventoryId);

}
