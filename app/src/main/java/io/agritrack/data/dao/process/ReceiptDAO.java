package io.agritrack.data.dao.process;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.process.Receipt;

@Dao
public interface ReceiptDAO {

    @Query("SELECT * from receipt")
    LiveData<List<Receipt>> getAll();

    @Query("SELECT * from receipt where id=:receiptId LIMIT 1")
    Receipt getById(Long receiptId);

    @Insert
    void insert(Receipt... receipts);

    @Delete
    void delete(Receipt receipt);

    @Query("DELETE from receipt")
    void deleteAll();

    @Update
    void update(Receipt receipt);
}
