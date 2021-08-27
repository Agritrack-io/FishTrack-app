package io.agritrack.fishtrack.data.dao.common;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.common.Supplier;

@Dao
public interface SupplierDAO {

    @Query("SELECT * from supplier")
    List<Supplier> getAll();

    @Query("SELECT * from supplier where id=:supplierId LIMIT 1")
    Supplier getById(Long supplierId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Supplier... suppliers);

    @Delete
    void delete(Supplier supplier);

    @Query("DELETE from supplier")
    void deleteAll();

    @Update
    void update(Supplier supplier);
}
