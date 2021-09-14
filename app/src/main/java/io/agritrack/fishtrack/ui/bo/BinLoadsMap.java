package io.agritrack.fishtrack.ui.bo;

import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinLoadsMap {
    private Map<String, List<String>> loads = new HashMap<>();

    public BinLoadsMap() {}

    public void addLoad(String bin, String load) {
        List<String> loadsforBin = loads.get(bin);

        if(loadsforBin==null) {
            loadsforBin = new ArrayList<>();
        }

        loadsforBin.add(load);
        loads.put(bin, loadsforBin);
    }

    public List<String> getLoads(String bin) {
        List<String> curLoads = loads.get(bin);
        if(curLoads==null) {
            curLoads=new ArrayList<>();
            loads.put(bin, curLoads);
        }

        return curLoads;
    }

    public boolean hasLoads() {
        return this.loads==null || this.loads.isEmpty();
    }

    public String loadsCnt() {
        return String.valueOf(loads.size());
    }

    public Double weightOf(String bin) {
        if(Strings.isEmptyOrWhitespace(bin)) {
            return 0.0d;
        }

        Double total = 0.0d;
        for(String w : loads.get(bin)) {
            total += Double.valueOf(w);
        }
        return total;
    }

    public Double totalWeight() {
        Double total = 0.0d;
        for(String bin : loads.keySet()) {
            total += weightOf(bin);
        }
        return total;
    }
}
