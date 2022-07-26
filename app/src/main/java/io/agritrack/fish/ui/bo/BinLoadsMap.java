package io.agritrack.fish.ui.bo;

import android.widget.Toast;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.agritrack.common.LargeString.render;
import static io.agritrack.fish.state.GlobalState.recFishing;
import static io.agritrack.ui.custom.CustomToast.CToast;

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
        if (curLoads != null) {
            while (curLoads.contains("0")) {
                curLoads.remove("0");
            }
            if (curLoads == null) {
                curLoads = new ArrayList<>();
                loads.put(bin, curLoads);
            }
        }

        return curLoads;
    }

    public boolean hasLoads() {
        return this.loads==null || !this.loads.isEmpty();
    }

    public String loadsCnt() {
        int cnt = 0;
        if (loads != null) {
            for (String key : loads.keySet()) {
                List<String> loadsPerEPC = loads.get(key);
                if (!CollectionUtils.isEmpty(loadsPerEPC)) {
                    List<String> ll = loadsPerEPC.stream().filter(l -> !"0".equals(l)).collect(Collectors.toList());
                    if (ll.size() > 0) {
                        cnt++;
                    }
                }
            }
        }
        return String.valueOf(cnt);
    }

    public Integer weightOf(String bin) {
        if(Strings.isEmptyOrWhitespace(bin) || !loads.containsKey(bin)) {  //|| recFishing.binWeightRecord.getRecordForEPC(bin).weight == null
            return 0;
        }

        Integer total = 0;
        for(String w : loads.get(bin)) {
            total += !Strings.isEmptyOrWhitespace(w) ? Integer.valueOf(w) : 0; //TextUtils.isDigitsOnly(
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
