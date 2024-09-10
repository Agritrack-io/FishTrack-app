package io.agritrack.philosofish.data.model.tx;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;
import java.util.UUID;

import io.agritrack.philosofish.data.converter.BinRecordConverter;
import io.agritrack.philosofish.data.converter.LaundrySampleConverter;
import io.agritrack.philosofish.data.converter.TonneSampleConverter;
import io.agritrack.philosofish.data.model.common.LaundrySample;
import io.agritrack.philosofish.data.model.common.TonneSample;

@Entity(tableName = "package_quality_transaction")
public class PackageQualityTransaction {

    public PackageQualityTransaction() {
        this.id = UUID.randomUUID();
    }

    @PrimaryKey
    @NonNull
    public UUID id;

    @ColumnInfo(name = "lot")
    public String lot;

    @ColumnInfo(name = "fresh_grade")
    public Integer freshGrade;

    @ColumnInfo(name = "skin_grade")
    public Integer skingGrade;

    @ColumnInfo(name = "eye_grade")
    public Integer eye_grade;

    @ColumnInfo(name = "gill_grade")
    public Integer gill_grade;

    @ColumnInfo(name = "crooked_mouth")
    public Integer crookedMouth;

    @ColumnInfo(name = "lower_law")
    public Integer lowerJaw;

    @ColumnInfo(name = "jaw_over")
    public Integer jawOver;

    @ColumnInfo(name = "operculum")
    public Integer operculum;

    @ColumnInfo(name = "lordosis")
    public Integer lordosis;

    @ColumnInfo(name = "shortening")
    public Integer shortening;

    @ColumnInfo(name = "skeletical")
    public Integer skeletical;

    @ColumnInfo(name = "tail")
    public Integer tail;

    @ColumnInfo(name = "tail_deform")
    public Integer tailDeform;

    @ColumnInfo(name = "fin_deform")
    public Integer finDeform;

    @ColumnInfo(name = "wound_deform")
    public Integer woundsDeform;

    @ColumnInfo(name = "hem_slight")
    public Integer hemSlight;

    @ColumnInfo(name = "hem_spots")
    public Integer hemSpots;

    @ColumnInfo(name = "hem_diffuse")
    public Integer hemDiffuse;

    @ColumnInfo(name = "hem_wounds")
    public Integer hemWounds;

    @ColumnInfo(name = "eye_blurred")
    public Integer eyeBlurred;

    @ColumnInfo(name = "eye_cured")
    public Integer eyeCured;

    @ColumnInfo(name = "eye_blind")
    public Integer eyeBlind;

    @ColumnInfo(name = "eye_bleed")
    public Integer eyeBleed;

    @ColumnInfo(name = "gill_mucus")
    public Integer gillMucus;

    @ColumnInfo(name = "gill_bloody")
    public Integer gillBloody;

    @ColumnInfo(name = "gill_brown")
    public Integer gillBrown;

    @ColumnInfo(name = "gill_discolor")
    public Integer gillDiscolor;

    @ColumnInfo(name = "head_deform")
    public Integer headDeform;

    @TypeConverters(LaundrySampleConverter.class)
    @ColumnInfo(name = "laundry_samples")
    public List<LaundrySample> laundrySamples;

    @TypeConverters(TonneSampleConverter.class)
    @ColumnInfo(name = "tonne_samples")
    public List<TonneSample> tonneSamples;


}
