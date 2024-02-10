package io.agritrack.kefalonia.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.agritrack.kefalonia.data.model.EncodingSchemeEntity;

@Dao
public interface EncodingSchemeDAO {

    @Query("SELECT * from encoding_scheme")
    List<EncodingSchemeEntity> getAll();

    @Query("SELECT * from encoding_scheme where code = :schemeCode LIMIT 1")
    EncodingSchemeEntity getByCode(String schemeCode);

    @Query("SELECT * from encoding_scheme where category = :catType LIMIT 1")
    List<EncodingSchemeEntity> getByCategory(String catType);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EncodingSchemeEntity... schemes);

    @Delete
    void delete(EncodingSchemeEntity schemes);

    @Query("DELETE from encoding_scheme")
    void deleteAll();
}
