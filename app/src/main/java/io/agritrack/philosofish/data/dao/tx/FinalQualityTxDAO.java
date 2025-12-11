package io.agritrack.philosofish.data.dao.tx;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.philosofish.data.model.tx.FinalQualityTransaction;

@Dao
public interface FinalQualityTxDAO {

    @Query("SELECT * from final_quality_transaction")
    List<FinalQualityTransaction> getAll();

    @Query("SELECT * from final_quality_transaction where lot=:lot LIMIT 1")
    FinalQualityTransaction getByLot(String lot);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FinalQualityTransaction... qualityTxs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FinalQualityTransaction qualityTx);

    @Query("DELETE FROM final_quality_transaction WHERE fish_lot = :fishLot")
    int deleteByFishingLot(String fishLot);


    @Delete
    void delete(FinalQualityTransaction qualityTx);

    @Query("DELETE from final_quality_transaction")
    int deleteAll();

    @Query("DELETE from final_quality_transaction where created_at<:threeDaysAgo")
    int deleteThreeDaysOld(Long threeDaysAgo);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(FinalQualityTransaction qualityTx);

    @Query("DELETE FROM final_quality_transaction WHERE lot = :lot")
    void deleteByLot(String lot);

}
