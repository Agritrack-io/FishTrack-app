package io.agritrack.philosofish.enums;

public enum StandardType {
    GGAP,
    FIG,
    ASC,
    OTHER;

    public static String[] asArray() {
        StandardType[] values = StandardType.values();
        String[] arr = new String[values.length];
        for (int i = 0; i < values.length; i++) arr[i] = values[i].name();
        return arr;
    }
}
