package io.agritrack.fishtrack.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.fishtrack.data.model.Site;

@Dao
public interface SiteDAO {

    @Query("SELECT * from site")
    LiveData<List<Site>> getAll();

//    @Query("SELECT * from plant where distributor_ids LIKE '%,' || :distributorId || ',%'")
//    List<Site> getAllByDistributorId(Long distributorId);

    @Query("SELECT * from site where id=:siteId LIMIT 1")
    Site getById(Long siteId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Site... sites);

    @Delete
    void delete(Site site);

    @Query("DELETE from site")
    void deleteAll();

    @Update
    void update(Site site);
}
