package io.agritrack.fish.ui.bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agritrack.data.model.BinInfo;

public class BinWeightRecord {

    private final Map<String, BinRecord> data = new HashMap<>();

    public void addRecord(String binEPC, Integer weight) {
        this.data.put(binEPC, new BinRecord(binEPC, weight));
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

    public List<BinRecord> getBins(){
        return new ArrayList<>(data.values());
    }

    public class BinRecord {
        private final Integer weight;
        private final String binEPC;

        public BinRecord(String binEPC, Integer weight) {
            this.binEPC = binEPC;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return String.format("{binEPC:'%s', weight:%4d}", binEPC, weight);
        }
    }
}
