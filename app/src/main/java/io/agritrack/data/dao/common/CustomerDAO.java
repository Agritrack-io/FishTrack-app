package io.agritrack.data.dao.common;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.common.Customer;

@Dao
public interface CustomerDAO {

    @Query("SELECT * from customer")
    List<Customer> getAll();

    @Query("SELECT * from customer where id=:customerId LIMIT 1")
    Customer getById(Long customerId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Customer... customers);

    @Delete
    void delete(Customer customer);

    @Query("DELETE from customer")
    void deleteAll();

    @Update
    void update(Customer customer);
}
