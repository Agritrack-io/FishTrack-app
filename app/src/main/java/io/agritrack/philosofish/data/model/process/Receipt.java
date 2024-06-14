package io.agritrack.philosofish.data.model.process;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "receipt")
public class Receipt {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "clean_truck")
    public String cleanTruck;

    @ColumnInfo(name = "plot")
    public String plot;

    @ColumnInfo(name = "fish_type")
    public String fishType;

    @ColumnInfo(name = "dispatch_note")
    public String dispatchNote;

    @ColumnInfo(name = "fish_condition")
    public String fishCondition;

    @ColumnInfo(name = "security_clip_number")
    public String securityClipNumber;

/*    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flot_id")
    public Flot flot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_Receipt_Site"))
    public Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_load_id", foreignKey = @ForeignKey(name="FK_Receipt_Harvest_Load"))
    public HarvestLoad harvestLoad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name="FK_Receipt_User"))
    public User user;*/
}
