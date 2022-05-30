package io.agritrack.data.model.wh;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_sku")
public class FoodSku {

    @NonNull
    @PrimaryKey
    public String gtin;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "product_code")
    public String productCode;

    @ColumnInfo(name = "species")
    public String species;

    @ColumnInfo(name = "diameter")
    public String diameter;
}
