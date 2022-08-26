package io.agritrack.common.compare;

import com.google.android.gms.common.util.Strings;

import java.util.Comparator;

import io.agritrack.data.model.FishingRequest;

public class FishingRequestComparator implements Comparator<FishingRequest> {
    private final int LEFT = -1;
    private final int RIGHT = 1;
    @Override
    public int compare(FishingRequest l, FishingRequest r) {
        Short lItin = l.itinSNo!=null ? l.itinSNo : 1;
        Short rItin = r.itinSNo!=null ? r.itinSNo : 1;
        if (l!=null && r!=null){
            if (!Strings.isEmptyOrWhitespace(l.farmArrival)){
                if (!Strings.isEmptyOrWhitespace(r.farmArrival)){
                    int c = l.farmArrival.compareTo(r.farmArrival);
                    return c==0 ? lItin.compareTo(rItin) : c;
                } else if (!Strings.isEmptyOrWhitespace(r.harvestDate)){
                    return l.farmArrival.compareTo(r.harvestDate);
                }
            } else if (!Strings.isEmptyOrWhitespace(l.harvestDate)){
                if (!Strings.isEmptyOrWhitespace(r.farmArrival)){
                    return l.harvestDate.compareTo(r.farmArrival);
                } else if (!Strings.isEmptyOrWhitespace(r.harvestDate)){
                    return l.harvestDate.compareTo(r.harvestDate);
                }
            }
        }

        return 0;
    }
}
