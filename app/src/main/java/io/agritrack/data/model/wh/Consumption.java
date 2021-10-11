package io.agritrack.data.model.wh;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "consumption")
public class Consumption {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "erp_user")
    public String erpUser;

    @ColumnInfo(name = "plot")
    public String plot;

    @ColumnInfo(name = "hlot")
    public String hlot;

    @ColumnInfo(name = "cage_rfid")
    public String cageRFID;

    @ColumnInfo(name = "basic_categories")
    public String basicCategories;

    @ColumnInfo(name = "type_packaging")
    public String typePackaging;

    @ColumnInfo(name = "boxes_per_category")
    public Integer boxesPerCategory;

    @ColumnInfo(name = "number_of_boxes_in_category")
    public Integer numberOfBoxesInCategory;

    @ColumnInfo(name = "number_of_fish")
    public Integer numberOfFish;

    @ColumnInfo(name = "total_kg_felizol")
    public Double totalKgFelizol;

    @ColumnInfo(name = "mean_weight")
    public Integer meanWeight;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_load_id", foreignKey = @ForeignKey(name="FK_Consumption_Harvest_Load"))
    public HarvestLoad harvestLoad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flot_id", foreignKey = @ForeignKey(name="FK_Consummption_FLOT"))
    public Flot flot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", foreignKey = @ForeignKey(name="FK_Consumption_Site"))
    public Site site;*/
}
