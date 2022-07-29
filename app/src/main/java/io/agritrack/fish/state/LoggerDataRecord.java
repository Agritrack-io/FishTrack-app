package io.agritrack.fish.state;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoggerDataRecord {

    public Map<String, TemperatureModel> data = new HashMap<>();
    public Map<String, Long> loggerInitData = new HashMap<>();
    public Double highT, lowT, avgT;

    public void addDataSet(String loggerEPC, String assetEPC, Long retrievedAt, List<String[]> values) {
        this.data.put(assetEPC, new TemperatureModel(loggerEPC, assetEPC, retrievedAt, values));
    }

    public void addDataSet(String loggerEPC, String assetEPC, String productionLane, Long retrievedAt, List<String[]> values) {
        this.data.put(assetEPC, new TemperatureModel(loggerEPC, assetEPC, productionLane, retrievedAt, values));
    }

    public void addInitData(String assetEPC, Long initedAt) {
        this.loggerInitData.put(assetEPC, initedAt);
    }

    public List<String[]> getValues(String epc) {
        List<String[]> result = null;
        if (data != null) {
            TemperatureModel valuesforEPC = data.get(epc);
            if (valuesforEPC != null) {
                result = data.get(epc).values;
            }
        }
        return result;
    }

    public void clearData() {
        data = new HashMap<>();
    }

    public class TemperatureModel {
        public final String loggerEPC;
        public String assetEPC;
        public String productionLane;
        public final Long retrievedAt;
        public final List<String[]> values;

        public TemperatureModel(String loggerEPC, String assetEPC, Long retrievedAt, List<String[]> measurements) {
            this.loggerEPC = loggerEPC;
            this.assetEPC = assetEPC;
            this.retrievedAt = retrievedAt;
            this.values = measurements;
        }

        public TemperatureModel(String loggerEPC, String assetEPC, String productionLane, Long retrievedAt, List<String[]> measurements) {
            this.loggerEPC = loggerEPC;
            this.assetEPC = assetEPC;
            this.productionLane = productionLane;
            this.retrievedAt = retrievedAt;
            this.values = measurements;
        }

        @Override
        public String toString() {
            return String.format("{EPC:'%s', asset:'%s', retrievedAt:%s, values:%s}", assetEPC, retrievedAt, values);
        }
    }
}