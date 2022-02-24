package io.agritrack.data.service;

import static io.agritrack.FishTrackApplication.getAppContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.EncodingSchemeEntity;

public class EncodingSchemeService {
    // unique instance
    private static EncodingSchemeService mInstance;

    // get an instance of local DB
    private static MobileDB db = MobileDB.getInstance(getAppContext());
    private static Map<String, EncodingSchemeEntity> assetTypesMap;


    static {
        assetTypesMap = new LinkedHashMap<>();
        // put the 'All' case on top of the list
        EncodingSchemeEntity allItm = new EncodingSchemeEntity();
        allItm.code = "All";
        allItm.description = "All";
        assetTypesMap.put(allItm.code, allItm);

        MobileDB db = MobileDB.getInstance(getAppContext());
        List<EncodingSchemeEntity> entries = db.encodingSchemeDAO().getAll();
        entries.stream().forEach(i -> assetTypesMap.put(i.code, i));

    }

    public static synchronized EncodingSchemeService getInstance() {
        if (mInstance == null) {
            mInstance = new EncodingSchemeService();
        }
        return mInstance;
    }

    public String nameOf(String code) {
        String result = code;
        EncodingSchemeEntity itm = assetTypesMap.get(code);
        result = itm != null ? itm.description : code;
        return result;
    }

    public String codeOf(String description) {
        String result = null;
        Optional<Map.Entry<String, EncodingSchemeEntity>> itm = assetTypesMap.entrySet().stream().filter(x -> description.equalsIgnoreCase(x.getValue().description)).findFirst();
        return itm.isPresent() ? itm.get().getValue().code : result;
    }

    public String[] allCodes() {
        return assetTypesMap.keySet().toArray(new String[assetTypesMap.size()]);
    }

    public String[] allNames() {
        return assetTypesMap.values().stream().map(x->x.description).collect(Collectors.toList()).toArray(new String[assetTypesMap.size()]);
    }
}
