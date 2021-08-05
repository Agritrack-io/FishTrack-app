package io.agritrack.fishtrack.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "harvest_request")
public class HarvestRequest {

    @PrimaryKey
    private Long id;

    @ColumnInfo(name = "request_id")
    private String requestId;

    @ColumnInfo(name = "request_quantity")
    private String reqQty;

    @ColumnInfo(name = "requestor")
    private String requestor;

   /* @OneToMany(mappedBy = "harvestRequest", fetch = FetchType.LAZY)
    private List<HarvestLoad> harvestLoads;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_HarvestRequest_Site"))
    private Site site;*/
}
