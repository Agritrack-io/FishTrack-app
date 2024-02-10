package io.agritrack.kefalonia.fish.ui.bo;

import java.util.HashMap;
import java.util.Map;

public class BinTemperatureRecord {

    private final Map<String, BinRecord> data = new HashMap<>();

    public void addRecord(String binEPC, Long retrievedAt, Double value) {
        this.data.put(binEPC, new BinRecord(binEPC, retrievedAt, value));
    }

    public String toJSONText() {
        String SEPARATOR = "";
        StringBuilder sb = new StringBuilder();
        if (this.data.size() > 0) {
            for (String key : this.data.keySet()) {
                sb.append(SEPARATOR);
                sb.append(this.data.get(key).toString());
                SEPARATOR = ",";
            }
        }
        return String.format("[%s]", sb);
    }

    private class BinRecord {
        private final Long retrievedAt;
        private final Double value;
        private final String binEPC;

        public BinRecord(String binEPC, Long retrievedAt, Double value) {
            this.binEPC = binEPC;
            this.retrievedAt = retrievedAt;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("{binEPC:'%s', retrievedAt:%s, value:%.2f}", binEPC, retrievedAt, value);
        }
    }
}
