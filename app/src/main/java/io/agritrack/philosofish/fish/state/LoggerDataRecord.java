package io.agritrack.philosofish.fish.state;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agritrack.philosofish.data.model.TempSample;

public class LoggerDataRecord {

    public Map<String, TemperatureModel> data = new HashMap<>();
    public Map<String, Long> loggerInitData = new HashMap<>();
    public Double highT, lowT, avgT;

    public void addDataSet(String loggerEPC, String assetEPC, Long retrievedAt, List<TempSample> values) {
        this.data.put(assetEPC, new TemperatureModel(loggerEPC, assetEPC, retrievedAt, values));
    }

    public void addDataSet(String loggerEPC, String assetEPC, String productionLane, Long retrievedAt, List<TempSample> values) {
        this.data.put(assetEPC, new TemperatureModel(loggerEPC, assetEPC, productionLane, retrievedAt, values));
    }

    public void addDataSetForBin(String assetEPC, Double fishT, Double waterT, Double fishT2) {
        this.data.put(assetEPC, new TemperatureModel(getLoggerEPC(assetEPC), assetEPC, "1", getRetrieveAt(assetEPC), getValues(assetEPC), fishT, waterT, fishT2));
    }

    public void addInitData(String assetEPC, Long initedAt) {
        this.loggerInitData.put(assetEPC, initedAt);
    }

    public List<TempSample> getValues(String epc) {
        List<TempSample> result = null;
        if (data != null) {
            TemperatureModel valuesforEPC = data.get(epc);
            if (valuesforEPC != null) {
                result = data.get(epc).values;
            }
        }
        return result;
    }

    public String getLoggerEPC(String epc) {
        String result = null;
        if (data != null) {
            TemperatureModel valuesforEPC = data.get(epc);
            if (valuesforEPC != null) {
                result = data.get(epc).loggerEPC;
            }
        }
        return result;
    }

    public Long getRetrieveAt(String epc) {
        Long result = null;
        if (data != null) {
            TemperatureModel valuesforEPC = data.get(epc);
            if (valuesforEPC != null) {
                result = data.get(epc).retrievedAt;
            }
        }
        return result;
    }

    public void clearData() {
        data = new HashMap<>();
    }

    public class TemperatureModel {
        public String loggerEPC;
        public String assetEPC;
        public String productionLane;
        public Double fishT, waterT, fishT2;
        public Long retrievedAt;
        public List<TempSample> values;

        public TemperatureModel(String loggerEPC, String assetEPC, Long retrievedAt, List<TempSample> measurements) {
            this.loggerEPC = loggerEPC;
            this.assetEPC = assetEPC;
            this.retrievedAt = retrievedAt;
            this.values = measurements;
        }

        public TemperatureModel(String loggerEPC, String assetEPC, String productionLane, Long retrievedAt, List<TempSample> measurements) {
            this.loggerEPC = loggerEPC;
            this.assetEPC = assetEPC;
            this.productionLane = productionLane;
            this.retrievedAt = retrievedAt;
            this.values = measurements;
        }

        public TemperatureModel(String loggerEPC, String assetEPC, String productionLane, Long retrievedAt, List<TempSample> measurements, Double fishT, Double waterT, Double fishT2) {
            this.loggerEPC = loggerEPC;
            this.assetEPC = assetEPC;
            this.productionLane = productionLane;
            this.retrievedAt = retrievedAt;
            this.values = measurements;
            this.fishT = fishT;
            this.waterT = waterT;
            this.fishT2 = fishT2;
        }

//        @Override
//        public String toString() {
//            return String.format("{EPC:'%s', asset:'%s', retrievedAt:%s, values:%s}", assetEPC, retrievedAt, values);
//        }
    }
}