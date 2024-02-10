package io.agritrack.kefalonia.data.dao.wh;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.kefalonia.data.model.wh.FoodSku;

@Dao
public interface FoodSkuDAO {

    @Query("SELECT * from food_sku")
    LiveData<List<FoodSku>> getAll();

    @Query("SELECT * from food_sku where gtin=:gtin LIMIT 1")
    FoodSku getByGtin(String gtin);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FoodSku... foodSkus);

    @Delete
    void delete(FoodSku foodSku);

    @Query("DELETE from food_sku")
    void deleteAll();

    @Update
    void update(FoodSku foodSku);
}
