package io.agritrack.fish.ui.bo;

import java.util.HashMap;
import java.util.Map;

public class BinWeightRecord {

    private final Map<String, BinRecord> data = new HashMap<>();

    public void addRecord(String binEPC, Double value) {
        this.data.put(binEPC, new BinRecord(binEPC, value));
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
        return String.format("[%s]", sb.toString());
    }

    private class BinRecord {
        private final Double value;
        private final String binEPC;

        public BinRecord(String binEPC, Double value) {
            this.binEPC = binEPC;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("{binEPC:'%s', value:%.2f}", binEPC, value);
        }
    }
}
