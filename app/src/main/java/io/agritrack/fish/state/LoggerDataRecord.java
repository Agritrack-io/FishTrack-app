package io.agritrack.fish.state;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoggerDataRecord {

    public Map<String, TemperatureModel> data = new HashMap<>();

    public void addDataSet(String epc, Long retrievedAt, String enabledAt, List<String[]> values) {
        this.data.put(epc, new TemperatureModel(epc, retrievedAt, enabledAt, values));
    }

    public class TemperatureModel {
        public final String loggerEPC;
        public final Long retrievedAt;
        public final String enabledAt;
        public final List<String[]> values;

        public TemperatureModel(String epc, Long retrievedAt, String enabledAt, List<String[]> measurements) {
            this.loggerEPC = epc;
            this.retrievedAt = retrievedAt;
            this.enabledAt = enabledAt;
            this.values = measurements;
        }

        @Override
        public String toString() {
            return String.format("{EPC:'%s', retrievedAt:%s, values:%s}", loggerEPC, retrievedAt, values);
        }
    }
}