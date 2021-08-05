package io.agritrack.fishtrack.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "harvest_load")
public class HarvestLoad {

    @PrimaryKey
    private Long id;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "fish_type")
    private String fishType;

    @ColumnInfo(name = "fish_size")
    private String fishSize;

  /*  @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_request_id", foreignKey = @ForeignKey(name="FK_HarvestLoad_Harvest_Request"))
    private HarvestRequest harvestRequest;*/
}
