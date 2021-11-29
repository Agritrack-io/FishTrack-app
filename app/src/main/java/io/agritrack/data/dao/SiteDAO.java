package io.agritrack.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agritrack.data.model.Site;

@Dao
public interface SiteDAO {

    @Query("SELECT * from site")
    List<Site> getAll();

    @Query("SELECT * from site where site_type='PLANT'")
    List<Site> getAllProcessingPlants();

    @Query("SELECT * from site where site_type='FISHFARM'")
    List<Site> getAllFishFarms();

    @Query("SELECT * from site where site_type='GREENHOUSE'")
    List<Site> getAllGreenhouses();

    @Query("SELECT * from site where site_lvl=4 and lvl3=:parentId")
    List<Site> getCurrentSiteSubSites(String parentId);

    @Query("SELECT * from site where id=:siteId LIMIT 1")
    Site getById(Long siteId);

    @Query("SELECT * from site where lvl3=:site_name and lvl4=:site_code LIMIT 1")
    Site getBySiteNameAndCode(String site_name, String site_code);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Site... sites);

    @Delete
    void delete(Site site);

    @Query("DELETE from site")
    void deleteAll();

    @Update
    void update(Site site);
}
