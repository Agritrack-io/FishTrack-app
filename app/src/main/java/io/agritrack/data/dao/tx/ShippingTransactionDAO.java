package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.ShippingTransaction;
import io.agritrack.data.model.tx.items.ShippingTxWithItems;

@Dao
public interface ShippingTransactionDAO {

    @Transaction
    @Query("SELECT * from shipping_transaction")
    LiveData<List<ShippingTxWithItems>> getAll();

    @Transaction
    @Query("SELECT * from shipping_transaction where id=:shippingTransactionId LIMIT 1")
    ShippingTxWithItems getById(Long shippingTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long[] insert(ShippingTransaction... shippingTransactions);

    @Delete
    void delete(ShippingTransaction shippingTransaction);

    @Query("DELETE from shipping_transaction")
    void deleteAll();

    @Update
    void update(ShippingTransaction shippingTransaction);
}
