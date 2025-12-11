package io.agritrack.philosofish.data.model.tx;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.agritrack.philosofish.data.converter.DateConverter;
import io.agritrack.philosofish.data.converter.SortingSampleConverter;
import io.agritrack.philosofish.data.converter.TonneSampleConverter;
import io.agritrack.philosofish.data.model.common.SortingSample;
import io.agritrack.philosofish.data.model.common.TonneSample;

@Entity(tableName = "package_quality_transaction")
public class PackageQualityTransaction {

    public PackageQualityTransaction() {
        this.id = UUID.randomUUID();
    }
//
//    @PrimaryKey
//    @NonNull
//    public UUID uid;

    @NonNull
    @ColumnInfo(name = "id")
    public UUID id;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "lot")
    public String lot;

    @ColumnInfo(name = "fish_lot")
    public String fishingLot;

    @ColumnInfo(name = "fresh_grade")
    public Integer freshGrade;

    @ColumnInfo(name = "overall_grade")
    public Integer overallGrade;

    @ColumnInfo(name = "skin_grade")
    public Integer skinGrade;

    @ColumnInfo(name = "eye_grade")
    public Integer eyeGrade;

    @ColumnInfo(name = "gill_grade")
    public Integer gillGrade;

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

    @ColumnInfo(name = "tail_deformity")
    public Integer tailDeformity;

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

    @TypeConverters(SortingSampleConverter.class)
    @ColumnInfo(name = "sorting_samples")
    public List<SortingSample> sortingSamples;

    @TypeConverters(TonneSampleConverter.class)
    @ColumnInfo(name = "tonne_samples")
    public List<TonneSample> tonneSamples;

    @ColumnInfo(name = "start_packing")
    public Boolean startPacking;


    @ColumnInfo(name = "change_packing")
    public Boolean changePacking;

    @ColumnInfo(name = "middle_packing")
    public Boolean middlePacking;

    @ColumnInfo(name = "end_packing")
    public Boolean endPacking;

    @ColumnInfo(name = "label_comments")
    public String labelComments;

    @ColumnInfo(name = "disinfected_bins")
    public Integer disinfectedBins;

    @ColumnInfo(name = "total_kg")
    public Integer totalKg;

    @ColumnInfo(name = "fresh_created_at")
    public Long freshCreatedAt;

    @ColumnInfo(name = "sample_created_at")
    public Long sampleCreatedAt;

    @ColumnInfo(name = "label_created_at")
    public Long labelCreatedAt;

    @ColumnInfo(name = "fresh_sync")
    public Boolean isFreshSynced = false;

    @ColumnInfo(name = "sample_sync")
    public Boolean isSampleSynced = false;

    @ColumnInfo(name = "label_sync")
    public Boolean isLabelSynced = false;


    @TypeConverters(DateConverter.class)
    @ColumnInfo(name = "best_before")
    public Date bestBefore;

}
