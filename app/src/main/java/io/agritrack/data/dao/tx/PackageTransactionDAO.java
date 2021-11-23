package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.PackageTransaction;

@Dao
public interface PackageTransactionDAO {
    @Query("SELECT * from package_transaction")
    LiveData<List<PackageTransaction>> getAll();

    @Query("SELECT * from package_transaction where id=:packageTransactionId LIMIT 1")
    PackageTransaction getById(Long packageTransactionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PackageTransaction... packageTransactions);

    @Delete
    void delete(PackageTransaction packageTransaction);

    @Query("DELETE from package_transaction")
    void deleteAll();

    @Update
    void update(PackageTransaction packageTransaction);
}
