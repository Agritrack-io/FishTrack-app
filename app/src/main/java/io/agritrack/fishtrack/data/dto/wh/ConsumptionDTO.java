package io.agritrack.fishtrack.data.dto.wh;

import io.agritrack.fishtrack.data.model.wh.Consumption;

public class ConsumptionDTO {

    public Long id;
    public String erpUser;
    public String plot;
    public String hlot;
    public String cageRFID;
    public String basicCategories;
    public String typePackaging;
    public Integer boxesPerCategory;
    public Integer numberOfBoxesInCategory;
    public Integer numberOfFish;
    public Double totalKgFelizol;
    public Integer meanWeight;

    public static Consumption convert(ConsumptionDTO consumptionDTO) {
        Consumption consumption = new Consumption();
        consumption.id = consumptionDTO.id;
        consumption.erpUser = consumptionDTO.erpUser;
        consumption.plot = consumptionDTO.plot;
        consumption.hlot = consumptionDTO.hlot;
        consumption.cageRFID = consumptionDTO.cageRFID;
        consumption.basicCategories = consumptionDTO.basicCategories;
        consumption.typePackaging = consumptionDTO.typePackaging;
        consumption.boxesPerCategory = consumptionDTO.boxesPerCategory;
        consumption.numberOfBoxesInCategory = consumptionDTO.numberOfBoxesInCategory;
        consumption.numberOfFish = consumptionDTO.numberOfFish;
        consumption.totalKgFelizol = consumptionDTO.totalKgFelizol;
        consumption.meanWeight = consumptionDTO.meanWeight;
        return consumption;
    }
}
