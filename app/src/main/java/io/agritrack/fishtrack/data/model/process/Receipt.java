package io.agritrack.fishtrack.data.model.process;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "receipt")
public class Receipt {

    @PrimaryKey
    private Long id;

    @ColumnInfo(name = "clean_truck")
    private String cleanTruck;

    @ColumnInfo(name = "plot")
    private String plot;

    @ColumnInfo(name = "fish_type")
    private String fishType;

    @ColumnInfo(name = "dispatch_note")
    private String dispatchNote;

    @ColumnInfo(name = "fish_condition")
    private String fishCondition;

    @ColumnInfo(name = "security_clip_number")
    private String securityClipNumber;

/*    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flot_id")
    private Flot flot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_Receipt_Site"))
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_load_id", foreignKey = @ForeignKey(name="FK_Receipt_Harvest_Load"))
    private HarvestLoad harvestLoad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name="FK_Receipt_User"))
    private User user;*/
}
