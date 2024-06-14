package io.agritrack.philosofish.data.dto.wh;

import io.agritrack.philosofish.data.model.wh.FoodSku;

public class FoodSkuDTO {
    public String gtin;
    public String description;
    public String product_code;
    public String species;
    public String diameter;

    public static FoodSku convert(FoodSkuDTO foodSkuDTO) {
        FoodSku foodSku = new FoodSku();
        foodSku.gtin = foodSkuDTO.gtin;
        foodSku.description = foodSkuDTO.description;
        foodSku.productCode = foodSkuDTO.product_code;
        foodSku.species = foodSkuDTO.species;
        foodSku.diameter = foodSkuDTO.diameter;

        return foodSku;
    }
}
