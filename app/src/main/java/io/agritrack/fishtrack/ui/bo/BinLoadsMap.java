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

    public Integer weightOf(String bin) {
        if(Strings.isEmptyOrWhitespace(bin)) {
            return 0;
        }

        Integer total = 0;
        for(String w : loads.get(bin)) {
            total += Integer.valueOf(w);
        }
        return total;
    }

    public Integer totalWeight() {
        Integer total = 0;
        for(String bin : loads.keySet()) {
            total += weightOf(bin);
        }
        return total;
    }
}
