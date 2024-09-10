package io.agritrack.philosofish.data.model.tx;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "receipt_quality_transaction")
public class ReceiptQualityTransaction {

    public ReceiptQualityTransaction() {
        this.id = UUID.randomUUID();
    }

    @PrimaryKey
    @NonNull
    public UUID id;

    @ColumnInfo(name = "lot")
    public String lot;

    @ColumnInfo(name = "cage")
    public String cage;

    @ColumnInfo(name = "fish_date")
    public Long fishingDate;

    @ColumnInfo(name = "species")
    public String species;

    @ColumnInfo(name = "farm")
    public String farm;

    @ColumnInfo(name = "arrival_time")
    public Long arrivalTime;

    @ColumnInfo(name = "start_time")
    public Long startTime;

    @ColumnInfo(name = "sealed")
    public boolean sealed;

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
}
