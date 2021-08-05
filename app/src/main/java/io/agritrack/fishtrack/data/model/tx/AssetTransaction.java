package io.agritrack.fishtrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "asset_transaction")
public class AssetTransaction {

    @PrimaryKey
    private Long id;

    @ColumnInfo(name = "rfid")
    private String rfid;

    @ColumnInfo(name = "state")
    private String state;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_rfid", referencedColumnName = "rfid", nullable = false, foreignKey = @ForeignKey(name="FK_AssetTX_Asset"))
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_site", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name="FK_AssetTX_Source_Site"))
    private Site sourceSite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_site", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name="FK_AssetTX_Target_Site"))
    private Site targetSite;*/
}
