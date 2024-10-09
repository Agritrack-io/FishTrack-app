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

    @ColumnInfo(name = "fish_lot")
    public String fishingLot;

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

    @ColumnInfo(name = "under_weight_first_1")
    public Integer underWeightFirst1;

    @ColumnInfo(name = "under_weight_first_2")
    public Integer underWeightFirst2;

    @ColumnInfo(name = "under_weight_first_3")
    public Integer underWeightFirst3;

    @ColumnInfo(name = "over_weight_first_1")
    public Integer overWeightFirst1;

    @ColumnInfo(name = "over_weight_first_2")
    public Integer overWeightFirst2;

    @ColumnInfo(name = "over_weight_first_3")
    public Integer overWeightFirst3;

    @ColumnInfo(name = "net_weight_first")
    public Integer netWeightFirst;

    @ColumnInfo(name = "ice_quantity_first")
    public Integer iceQuantityFirst;

    @ColumnInfo(name = "fish_temp_first")
    public Double fishTempFirst;

    @ColumnInfo(name = "size_second")
    public String sizeSecond;

    @ColumnInfo(name = "box_type_second")
    public Integer boxTypeSecond;

    @ColumnInfo(name = "label_pieces_second")
    public Integer labelPiecesSecond;

    @ColumnInfo(name = "counted_pieces_second")
    public Integer countedPiecesSecond;

    @ColumnInfo(name = "under_weight_second_1")
    public Integer underWeightSecond1;

    @ColumnInfo(name = "under_weight_second_2")
    public Integer underWeightSecond2;

    @ColumnInfo(name = "under_weight_second_3")
    public Integer underWeightSecond3;

    @ColumnInfo(name = "over_weight_second_1")
    public Integer overWeightSecond1;

    @ColumnInfo(name = "over_weight_second_2")
    public Integer overWeightSecond2;

    @ColumnInfo(name = "over_weight_second_3")
    public Integer overWeightSecond3;

    @ColumnInfo(name = "net_weight_second")
    public Integer netWeightSecond;

    @ColumnInfo(name = "ice_quantity_second")
    public Integer iceQuantitySecond;

    @ColumnInfo(name = "fish_temp_second")
    public Double fishTempSecond;

    @ColumnInfo(name = "size_third")
    public String sizeThird;

    @ColumnInfo(name = "box_type_third")
    public Integer boxTypeThird;

    @ColumnInfo(name = "label_pieces_third")
    public Integer labelPiecesThird;

    @ColumnInfo(name = "counted_pieces_third")
    public Integer countedPiecesThird;

    @ColumnInfo(name = "under_weight_third_1")
    public Integer underWeightThird1;

    @ColumnInfo(name = "under_weight_third_2")
    public Integer underWeightThird2;

    @ColumnInfo(name = "under_weight_third_3")
    public Integer underWeightThird3;

    @ColumnInfo(name = "over_weight_third_1")
    public Integer overWeightThird1;

    @ColumnInfo(name = "over_weight_third_2")
    public Integer overWeightThird2;

    @ColumnInfo(name = "over_weight_third_3")
    public Integer overWeightThird3;

    @ColumnInfo(name = "net_weight_third")
    public Integer netWeightThird;

    @ColumnInfo(name = "ice_quantity_third")
    public Integer iceQuantityThird;

    @ColumnInfo(name = "fish_temp_third")
    public Double fishTempThird;

    @ColumnInfo(name = "created_at")
    public Long createdAt;

    @ColumnInfo(name = "signature")
    public String signature;

    @ColumnInfo(name = "is_synced")
    public Boolean isSynced = false;

    @ColumnInfo(name = "corrective_action")
    public String corrAction;

    @ColumnInfo(name = "lot_accepted")
    public Boolean lotAccepted;

    @ColumnInfo(name = "foreign_body")
    public Boolean foreignBody;

    @ColumnInfo(name = "discarded_quantity")
    public Double discardedQty;

}
