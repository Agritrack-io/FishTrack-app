package io.agritrack.fish.state;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoggerDataRecord {

    public Map<String, TemperatureModel> data = new HashMap<>();

    public void addDataSet(String epc, Long retrievedAt, List<String[]> values) {
        this.data.put(epc, new TemperatureModel(epc, retrievedAt, values));
    }

    public class TemperatureModel {
        public final String loggerEPC;
        public final Long retrievedAt;
        public final List<String[]> values;

        public TemperatureModel(String epc, Long retrievedAt, List<String[]> measurements) {
            this.loggerEPC = epc;
            this.retrievedAt = retrievedAt;
            this.values = measurements;
        }

        @Override
        public String toString() {
            return String.format("{EPC:'%s', retrievedAt:%s, values:%s}", loggerEPC, retrievedAt, values);
        }
    }
}