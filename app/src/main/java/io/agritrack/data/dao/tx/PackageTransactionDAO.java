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

import io.agritrack.data.model.tx.PackageTransaction;
import io.agritrack.data.model.tx.items.PackageTxWithItems;

@Dao
public interface PackageTransactionDAO {

    @Transaction
    @Query("SELECT * from package_transaction")
    LiveData<List<PackageTxWithItems>> getAll();

    @Transaction
    @Query("SELECT * from package_transaction where id=:packageTransactionId LIMIT 1")
    PackageTxWithItems getById(Long packageTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long[] insert(PackageTransaction... packageTransactions);

    @Delete
    void delete(PackageTransaction packageTransaction);

    @Query("DELETE from package_transaction")
    void deleteAll();

    @Update
    void update(PackageTransaction packageTransaction);
}
