package io.agritrack.fishtrack.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.agritrack.fishtrack.data.model.AppUser;

@Dao
public interface AppUserDAO {
    @Query("SELECT * from appUser")
    List<AppUser> getAll();

    @Query("SELECT * from appUser where id = :userId LIMIT 1")
    AppUser getById(Long userId);

    @Query("SELECT * from appUser where username = :username LIMIT 1")
    AppUser getByUsername(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AppUser... appUsers);

    @Delete
    void delete(AppUser appUser);

    @Query("DELETE from appUser")
    void deleteAll();

    @Query("SELECT * from appUser where roles LIKE :roleName")
    List<AppUser> getByRole(String roleName);
}
