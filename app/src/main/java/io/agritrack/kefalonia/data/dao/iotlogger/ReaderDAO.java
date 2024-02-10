package io.agritrack.kefalonia.data.dao.iotlogger;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.kefalonia.data.model.common.Reader;

@Dao
public interface ReaderDAO {

    @Query("SELECT * from reader")
    LiveData<List<Reader>> getAll();

    @Query("SELECT * from reader where id=:readerId LIMIT 1")
    Reader getById(Long readerId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Reader... readers);

    @Delete
    void delete(Reader reader);

    @Query("DELETE from reader")
    void deleteAll();

    @Update
    void update(Reader reader);
}
