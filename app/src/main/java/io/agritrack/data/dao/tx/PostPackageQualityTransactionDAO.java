package io.agritrack.data.dao.tx;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.tx.PostPackageQualityTransaction;
import io.agritrack.data.model.tx.QualityTransaction;

@Dao
public interface PostPackageQualityTransactionDAO {

    @Query("SELECT * from post_package_quality_transaction")
    LiveData<List<PostPackageQualityTransaction>> getAll();

    @Query("SELECT * from post_package_quality_transaction where id=:qualityTxId LIMIT 1")
    PostPackageQualityTransaction getById(Long qualityTxId);

    @Insert
    void insert(PostPackageQualityTransaction... qualityTxs);

    @Insert
    long insert(PostPackageQualityTransaction qualityTxs);

    @Delete
    void delete(PostPackageQualityTransaction qualityTx);

    @Query("DELETE from post_package_quality_transaction")
    void deleteAll();

    @Update
    void update(PostPackageQualityTransaction qualityTx);
}
