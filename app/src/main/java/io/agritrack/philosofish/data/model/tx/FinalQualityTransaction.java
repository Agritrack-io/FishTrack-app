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

@Entity(tableName = "final_quality_transaction")
public class FinalQualityTransaction {

    public FinalQualityTransaction() {
        this.id = UUID.randomUUID();
    }

    @NonNull
    @ColumnInfo(name = "id")
    public UUID id;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "lot")
    public String lot;

    @ColumnInfo(name = "exfo_rating")
    public Integer exfoRating;

    @ColumnInfo(name = "palette_rating")
    public Integer paletteRating;

    @ColumnInfo(name = "box_rating")
    public Integer boxRating;

    @ColumnInfo(name = "cylindrical")
    public Boolean cylinrical;

    @ColumnInfo(name = "expanded")
    public Boolean expanded;

    @ColumnInfo(name = "soft")
    public Boolean soft;

    @ColumnInfo(name = "head")
    public Boolean head;

    @ColumnInfo(name = "body")
    public Boolean body;

    @ColumnInfo(name = "areas")
    public Boolean areas;

    @ColumnInfo(name = "size_first")
    public String sizeFirst;

    @ColumnInfo(name = "box_type_first")
    public Integer boxTypeFirst;

    @ColumnInfo(name = "label_pieces_first")
    public Integer labelPiecesFirst;

    @ColumnInfo(name = "counted_pieces_first")
    public Integer countedPiecesFirst;

    @ColumnInfo(name = "under_weight_first")
    public Integer underWeightFirst;

    @ColumnInfo(name = "over_weight_first")
    public Integer overWeightFirst;

    @ColumnInfo(name = "net_weight_first")
    public Integer netWeightFirst;

    @ColumnInfo(name = "ice_quantity_first")
    public Integer iceQuantityFirst;

    @ColumnInfo(name = "fish_temp_first")
    public Integer fishTempFirst;

    @ColumnInfo(name = "size_second")
    public String sizeSecond;

    @ColumnInfo(name = "box_type_second")
    public Integer boxTypeSecond;

    @ColumnInfo(name = "label_pieces_second")
    public Integer labelPiecesSecond;

    @ColumnInfo(name = "counted_pieces_second")
    public Integer countedPiecesSecond;

    @ColumnInfo(name = "under_weight_second")
    public Integer underWeightSecond;

    @ColumnInfo(name = "over_weight_second")
    public Integer overWeightSecond;

    @ColumnInfo(name = "net_weight_second")
    public Integer netWeightSecond;

    @ColumnInfo(name = "ice_quantity_second")
    public Integer iceQuantitySecond;

    @ColumnInfo(name = "fish_temp_second")
    public Integer fishTempSecond;

    @ColumnInfo(name = "size_third")
    public String sizeThird;

    @ColumnInfo(name = "box_type_third")
    public Integer boxTypeThird;

    @ColumnInfo(name = "label_pieces_third")
    public Integer labelPiecesThird;

    @ColumnInfo(name = "counted_pieces_third")
    public Integer countedPiecesThird;

    @ColumnInfo(name = "under_weight_third")
    public Integer underWeightThird;

    @ColumnInfo(name = "over_weight_third")
    public Integer overWeightThird;

    @ColumnInfo(name = "net_weight_third")
    public Integer netWeightThird;

    @ColumnInfo(name = "ice_quantity_third")
    public Integer iceQuantityThird;

    @ColumnInfo(name = "fish_temp_third")
    public Integer fishTempThird;

    @ColumnInfo(name = "created_at")
    public Long createdAt;

    @ColumnInfo(name = "signature")
    public String signature;

    @ColumnInfo(name = "is_synced")
    public Boolean isSynced = false;

}
