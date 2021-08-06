package io.agritrack.fishtrack.data.dao.wh;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.wh.OrderItem;

@Dao
public interface OrderItemDAO {

    @Query("SELECT * from order_item")
    LiveData<List<OrderItem>> getAll();

    @Query("SELECT * from order_item where id=:orderItemId LIMIT 1")
    OrderItem getById(Long orderItemId);

    @Insert
    void insert(OrderItem... orderItems);

    @Delete
    void delete(OrderItem orderItem);

    @Query("DELETE from order_item")
    void deleteAll();

    @Update
    void update(OrderItem orderItem);
}
