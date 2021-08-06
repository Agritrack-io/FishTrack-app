package io.agritrack.fishtrack.data.dao.wh;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.wh.ExpectedCoInventory;

@Dao
public interface ExpectedCoInventoryDAO {

    @Query("SELECT * from expected_co_inventory")
    LiveData<List<ExpectedCoInventory>> getAll();

    @Query("SELECT * from expected_co_inventory where id=:expectedCoInventoryId LIMIT 1")
    ExpectedCoInventory getById(Long expectedCoInventoryId);

    @Insert
    void insert(ExpectedCoInventory... expectedCoInventorys);

    @Delete
    void delete(ExpectedCoInventory expectedCoInventory);

    @Query("DELETE from expected_co_inventory")
    void deleteAll();

    @Update
    void update(ExpectedCoInventory expectedCoInventory);
}
