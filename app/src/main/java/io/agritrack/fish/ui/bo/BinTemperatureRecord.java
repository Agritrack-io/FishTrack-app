package io.agritrack.fish.ui.bo;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BinTemperatureRecord {

    private Map<String, BinRecord> data = new HashMap<>();

    public void addRecord(String binEPC, Long retrievedAt, Double value){
        this.data.put(binEPC, new BinRecord(binEPC, retrievedAt, value));
    }

    public String toJSONText(){
        String SEPARATOR = "";
        StringBuilder sb = new StringBuilder();
        if (this.data.size()>0){
            for (String key: this.data.keySet()){
                sb.append(SEPARATOR);
                sb.append(this.data.get(key).toString());
                SEPARATOR = ",";
            }
        }
        return String.format("[%s]",sb.toString());
    }

    private class BinRecord {
        private Long retrievedAt;
        private Double value;
        private String binEPC;

        public BinRecord(String binEPC, Long retrievedAt, Double value){
            this.binEPC = binEPC;
            this.retrievedAt = retrievedAt;
            this. value = value;
        }

        @Override
        public String toString() {
            return String.format("{binEPC:'%s', retrievedAt:%s, value:%.2f}", binEPC, retrievedAt, value);
        }
    }
}
