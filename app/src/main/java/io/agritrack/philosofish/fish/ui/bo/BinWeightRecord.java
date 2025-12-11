package io.agritrack.philosofish.fish.ui.bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinWeightRecord {

    private final Map<String, BinRecord> data = new HashMap<>();

    public void addRecord(String binEPC, Integer weight, Long epochInit, Long epochFrom, Long epochTo) {
        this.data.put(binEPC, new BinRecord(binEPC, weight, epochInit, epochFrom, epochTo));
    }

    public void addRecord(String binEPC, Integer weight, Double temp, Long epochInit, Long epochFrom, Long epochTo) {
        this.data.put(binEPC, new BinRecord(binEPC, weight, temp, epochInit, epochFrom, epochTo));
    }

    public BinRecord getRecordForEPC(String epc) {
        return this.data.get(epc);
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

    public List<BinRecord> getBinsData() {
        return new ArrayList<>(data.values());
    }

    public Map<String, BinRecord> getBins() {
        return this.data;
    }

    public boolean isEmpty() {
        return this.data == null || this.data.isEmpty();
    }

    public class BinRecord {
        public final Integer weight;
        public final String binEPC;
        //public Double temp;
        public final Long from;
        public final Long to;
        public Long init;


        public BinRecord(String binEPC, Integer weight, Long epochInit, Long epochFrom, Long epochTo) {
            this.binEPC = binEPC;
            this.weight = weight;
            this.init = epochInit;
            this.from = epochFrom;
            this.to = epochTo;
        }

        public BinRecord(String binEPC, Integer weight, Double temp, Long epochInit, Long epochFrom, Long epochTo) {
            this.binEPC = binEPC;
            this.weight = weight;
            //this.temp = temp;
            this.init = epochInit;
            this.from = epochFrom;
            this.to = epochTo;
        }

        @Override
        public String toString() {
            return String.format("{binEPC:'%s', weight:%4d, init:%s, from:%s, to:%s}", binEPC, weight, init, from, to);
        }
    }
}