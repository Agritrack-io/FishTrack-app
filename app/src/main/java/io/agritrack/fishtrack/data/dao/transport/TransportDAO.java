package io.agritrack.fishtrack.data.dao.transport;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.transport.Transport;

@Dao
public interface TransportDAO {

    @Query("SELECT * from transport")
    LiveData<List<Transport>> getAll();

    @Query("SELECT * from transport where id=:transportId LIMIT 1")
    Transport getById(Long transportId);

    @Insert
    void insert(Transport... transports);

    @Delete
    void delete(Transport transport);

    @Query("DELETE from transport")
    void deleteAll();

    @Update
    void update(Transport transport);
}
