package io.agritrack.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.BinInfo;

@Dao
public interface BinInfoDAO {

    @Query("SELECT * from bin_info")
    LiveData<List<BinInfo>> getAll();

    @Query("SELECT * from bin_info where bin_rfid=:rfId LIMIT 1")
    BinInfo getByRFId(String rfId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BinInfo... bins);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BinInfo bin);

    @Delete
    void delete(BinInfo bin);

    @Query("DELETE from bin_info")
    void deleteAll();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(BinInfo bin);
}
