package io.agritrack.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "harvest_load")
public class HarvestLoad {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "species")
    public String species;

    @ColumnInfo(name = "fish_size")
    public String fishSize;

  /*  @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_request_id", foreignKey = @ForeignKey(name="FK_HarvestLoad_Harvest_Request"))
    public FishingRequest harvestRequest;*/
}
