package io.agritrack.philosofish.data.service;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import com.google.android.gms.common.util.Strings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.model.EncodingSchemeEntity;

public class EncodingSchemeService {
    // unique instance
    private static EncodingSchemeService mInstance;

    // get an instance of local DB
    private static MobileDB db = MobileDB.getInstance(getAppContext());
    private static Map<String, EncodingSchemeEntity> assetTypesMap;
    private static Set<String> codesSet;
    private static int startIdx = -1;
    private static int codeWidth = 0;

    static {
        assetTypesMap = new LinkedHashMap<>();
        // put the 'All' case on top of the list
        EncodingSchemeEntity allItm = new EncodingSchemeEntity();
        //allItm.code = null; //"All";
        allItm.description = "ALL";
        assetTypesMap.put(null, allItm);

        MobileDB db = MobileDB.getInstance(getAppContext());
        List<EncodingSchemeEntity> entries = db.encodingSchemeDAO().getAll();
        entries.stream().forEach(i -> {
            assetTypesMap.put(i.code, i);
            codeWidth = i.code.length();
            startIdx = i.encoding_index;
        });
        codesSet = assetTypesMap.keySet();

        //codeWidth = assetTypesMap.keySet().stream().mapToInt(String::length).max().getAsInt();
        //startIdx = assetTypesMap.values().stream().map(x-> x.encoding_index).mapToInt(y->y).min().getAsInt();
    }

    public static synchronized EncodingSchemeService getInstance() {
        if (mInstance == null) {
            mInstance = new EncodingSchemeService();
        }
        return mInstance;
    }

    public String nameOf(String code) {
        String result;
        EncodingSchemeEntity itm = assetTypesMap.get(code);
        result = itm != null ? itm.description : "Unknown";
        return result;
    }

    public String codeOf(String description) {
        String result = null;
        if ("All".equalsIgnoreCase(description)) {
            return null;
        }
        Optional<Map.Entry<String, EncodingSchemeEntity>> itm = assetTypesMap.entrySet().stream().filter(x -> description.equalsIgnoreCase(x.getValue().description)).findFirst();
        return itm.isPresent() ? itm.get().getValue().code : result;
    }

    public String[] allCodes() {
        return assetTypesMap.keySet().toArray(new String[assetTypesMap.size()]);
    }

    public String[] allNames() {
        List<String> values = assetTypesMap.values().stream().map(x -> x.description).collect(Collectors.toList());
        values.remove("DATA_LOGGER");
        return values.toArray(new String[values.size()]);
        //return assetTypesMap.values().stream().map(x->x.description).collect(Collectors.toList()).toArray(new String[assetTypesMap.size()]);
    }

    public String[] distinctNamesOnly() {
        List<String> values = assetTypesMap.values().stream().map(x -> x.description).collect(Collectors.toList());
        values.remove("ALL");
        values.remove("DATA_LOGGER");
        return values.toArray(new String[values.size()]);
    }

    public int encodingIndex() {
        return startIdx;
    }

    public int encodingWidth() {
        return codeWidth;
    }

    public String schemeCode(String epc, boolean startFromZero) {
        int idx = startFromZero ? 0 : startIdx;
        if (Strings.isEmptyOrWhitespace(epc)) {
            return null;
        }
        if (epc.length() <= (idx + codeWidth)) {
            return "XXXX";
        }
        String code = epc.substring(idx, idx + codeWidth);
        boolean validCode = codesSet.contains(code);
        if (validCode) {
            return code;
        } else {
            return "XXXX";
        }
    }

    public String schemeCode(String epc) {
        return schemeCode(epc, true);
    }

    public String nativeSchemeCode(CharSequence epc) {
        return schemeCode(String.valueOf(epc), false);
    }

    public String nativeSchemeCode(String epc) {
        return schemeCode(epc, false);
    }

    public EncodingSchemeEntity schemeForFilter(String filter) {
        EncodingSchemeEntity result = assetTypesMap.get(filter);
        return result;
    }
}
