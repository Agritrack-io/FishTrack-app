package io.agritrack.philosofish.data.dao.tx;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.philosofish.data.model.tx.PostPackageQualityTransaction;

@Dao
public interface PostPackageQualityTransactionDAO {

    @Query("SELECT * from post_package_quality_transaction")
    List<PostPackageQualityTransaction> getAll();

    @Query("SELECT * from post_package_quality_transaction where id=:qualityTxId LIMIT 1")
    PostPackageQualityTransaction getById(Long qualityTxId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PostPackageQualityTransaction... qualityTxs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PostPackageQualityTransaction qualityTxs);

    @Delete
    void delete(PostPackageQualityTransaction qualityTx);

    @Query("DELETE from post_package_quality_transaction")
    int deleteAll();

    @Update
    void update(PostPackageQualityTransaction qualityTx);
}
