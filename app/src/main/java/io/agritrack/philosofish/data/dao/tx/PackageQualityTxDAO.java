package io.agritrack.philosofish.data.dao.tx;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.philosofish.data.model.tx.PackageQualityTransaction;

@Dao
public interface PackageQualityTxDAO {

    @Query("SELECT * from package_quality_transaction")
    List<PackageQualityTransaction> getAll();

    @Query("SELECT * from package_quality_transaction where lot=:lot LIMIT 1")
    PackageQualityTransaction getByLot(String lot);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PackageQualityTransaction... qualityTxs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PackageQualityTransaction qualityTx);

    @Delete
    void delete(PackageQualityTransaction qualityTx);

    @Query("DELETE from package_quality_transaction")
    int deleteAll();

    @Query("DELETE from package_quality_transaction where fresh_created_at<:threeDaysAgo")
    int deleteThreeDaysOld(Long threeDaysAgo);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(PackageQualityTransaction qualityTx);
}
