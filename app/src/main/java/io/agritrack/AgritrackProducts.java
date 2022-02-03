package io.agritrack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum AgritrackProducts {
    FISH, TOMATO, HOTEL, MILK;

    private static List<String> values = null;

    public List<String> allValues() {
        if(values==null) {
            values = Arrays.stream(AgritrackProducts.values()).map(x -> x.name()).collect(Collectors.toList());
        }
        return values;
    }
}
