package io.agritrack.data.dao.wh;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.items.CoInventoryTxWithItems;
import io.agritrack.data.model.wh.CoInventory;

@Dao
public interface CoInventoryDAO {

    @Query("SELECT * from co_Inventory")
    LiveData<List<CoInventory>> getAll();

    @Transaction
    @Query("SELECT * from co_Inventory")
    LiveData<List<CoInventoryTxWithItems>> getAllInvIfco();

    @Query("SELECT * from co_Inventory where id=:coInventoryId LIMIT 1")
    CoInventory getById(Long coInventoryId);

    @Transaction
    @Query("SELECT * from co_Inventory where id=:coInventoryId LIMIT 1")
    CoInventoryTxWithItems getInvIfcoById(Long coInventoryId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CoInventory... coInventorys);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long[] insertInvIfco(CoInventory... coInventorys);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CoInventory coInventory);

    @Delete
    void delete(CoInventory coInventory);

    @Query("DELETE from co_Inventory")
    void deleteAll();

    @Update
    void update(CoInventory coInventory);
}
