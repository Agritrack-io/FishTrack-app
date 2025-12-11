package io.agritrack.philosofish.data.model.tx;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.sql.Time;
import java.util.Date;
import java.util.UUID;

import io.agritrack.philosofish.data.converter.DateConverter;
import io.agritrack.philosofish.data.converter.TimeConverter;

@Entity(tableName = "receipt_quality_transaction")
public class ReceiptQualityTransaction {

    public ReceiptQualityTransaction() {
        this.id = UUID.randomUUID();
    }
//
//    @PrimaryKey
//    @NonNull
//    public UUID uid;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "lot")
    public String lot;

    @NonNull
    @ColumnInfo(name = "id")
    public UUID id;

    @ColumnInfo(name = "cage")
    public String cage;

    @TypeConverters(DateConverter.class)
    @ColumnInfo(name = "fish_date")
    public Date fishingDate;

    @ColumnInfo(name = "species")
    public String species;

    @ColumnInfo(name = "farm")
    public String farm;

    @TypeConverters(TimeConverter.class)
    @ColumnInfo(name = "arrival_time")
    public Time arrivalTime;

    @TypeConverters(TimeConverter.class)
    @ColumnInfo(name = "start_time")
    public Time startTime;

    @ColumnInfo(name = "sealed")
    public Boolean sealed;

    @ColumnInfo(name = "eye_rating")
    public Integer eyeRating;

    @ColumnInfo(name = "gill_rating")
    public Integer gillRating;

    @ColumnInfo(name = "flesh_rating")
    public Integer fleshRating;

    @ColumnInfo(name = "skin_rating")
    public Integer skinRating;

    @ColumnInfo(name = "dis_eyes")
    public Integer disEyes;

    @ColumnInfo(name = "dis_tail")
    public Integer disTail;

    @ColumnInfo(name = "dis_skeletal")
    public Integer disSkeletal;

    @ColumnInfo(name = "dis_blood")
    public Integer disBlood;

    @ColumnInfo(name = "dis_mouth")
    public Integer disMouth;

    @ColumnInfo(name = "dis_oper")
    public Integer disOper;

    @ColumnInfo(name = "comments")
    public String comments;

    @ColumnInfo(name = "plant")
    public String plant;

    @ColumnInfo(name = "user")
    public String user;

    @ColumnInfo(name = "created_at")
    public Long createdAt;

    @ColumnInfo(name = "is_synced")
    public Boolean isSynced = false;


}
