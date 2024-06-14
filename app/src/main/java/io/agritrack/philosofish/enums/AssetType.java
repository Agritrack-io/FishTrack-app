package io.agritrack.philosofish.enums;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import java.util.LinkedList;
import java.util.List;

import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.model.EncodingSchemeEntity;

public class AssetType {
    // ALL("ALL"), UNKNOWN("UNKNOWN"), CAGE("CAGE"), NET("NET"), HARVEST_BIN("HARVEST_BIN"), PLATFORM("PLATFORM"), FOOD("FOOD"), VACCINE("VACCINE"), ANTIBIOTIC("ANTIBIOTIC"), POLE("POLE"), LINEN("LINEN"), QUILT_RASO_280_250("Παπλ/θήκη Υπ/πλη Raso 280X250"), DOUBLE_SHEET_RASO_300_300("Σεντόνι Υπ/πλο Raso 300X300"), PILLOW_CASE_RASO_54_95("Μαξ/θήκη Φάκελος Raso 54X95"), POOL_TOWEL_SAND_80_200("Πετσέτα Πισίνας Sand 80Χ200");

    // get an instance of local DB
    private static final MobileDB db = MobileDB.getInstance(getAppContext());
    private static final List<String> assetTypes;
    public static String ALL = "ALL";

    static {
        assetTypes = new LinkedList<>();
        assetTypes.add("ALL");
        List<EncodingSchemeEntity> allSchemes = db.encodingSchemeDAO().getAll();
        allSchemes.stream().forEach(x -> assetTypes.add(x.description));
    }

    private final String name;

    AssetType(String name) {
        this.name = name;
    }

    // Build an immutable map of String name to enum pairs.
    // Any Map impl can be used.

    public static List<String> values() {
        return assetTypes;
    }

    public String getName() {
        return this.name;
    }
}
