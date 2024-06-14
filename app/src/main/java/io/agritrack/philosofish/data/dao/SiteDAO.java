package io.agritrack.philosofish.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.UUID;

import io.agritrack.philosofish.data.model.Site;

@Dao
public interface SiteDAO {

    @Query("SELECT * from site")
    List<Site> getAll();

    @Query("SELECT * from site where site_type=:siteType")
    List<Site> getAllBySiteType(String siteType);

    @Query("SELECT * from site where site_type='PLANT'")
    List<Site> getAllProcessingPlants();

    @Query("SELECT * from site where site_type='SUPPLIER'")
    List<Site> getAllSuppliers();

    @Query("SELECT * from site where site_lvl=4 and lvl3=:parentId and site_type!='SUPPLIER' and site_type!='LAUNDRY'")
    List<Site> getCurrentSiteSubSites(String parentId);

    @Query("SELECT * from site where id=:siteId LIMIT 1")
    Site getById(UUID siteId);

    @Query("SELECT * from site where name=:site_name LIMIT 1")
    Site getBySiteName(String site_name);

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
