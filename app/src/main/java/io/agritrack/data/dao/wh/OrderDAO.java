package io.agritrack.data.dao.wh;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.wh.Order;

@Dao
public interface OrderDAO {

    @Query("SELECT * from order")
    LiveData<List<Order>> getAll();

    @Query("SELECT * from order where id=:orderId LIMIT 1")
    Order getById(Long orderId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Order... orders);

    @Delete
    void delete(Order order);

    @Query("DELETE from order")
    void deleteAll();

    @Update
    void update(Order order);
}
