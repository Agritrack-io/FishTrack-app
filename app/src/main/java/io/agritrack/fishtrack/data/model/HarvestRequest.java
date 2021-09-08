package io.agritrack.fishtrack.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "harvest_request")
public class HarvestRequest {

    @PrimaryKey
    @ColumnInfo
    public Long id;

    @ColumnInfo(name = "request_id")
    public String requestId;

    @ColumnInfo(name = "request_quantity")
    public String reqQty;

    @ColumnInfo(name = "requester")
    public String requester;

    @ColumnInfo(name = "species")
    public String fishName;

    @ColumnInfo(name = "user_name")
    public String user;

    @ColumnInfo(name = "site_name")
    public String site;

   /* @OneToMany(mappedBy = "harvestRequest", fetch = FetchType.LAZY)
    public List<HarvestLoad> harvestLoads;*/
}
