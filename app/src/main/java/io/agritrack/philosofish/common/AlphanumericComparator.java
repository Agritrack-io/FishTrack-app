package io.agritrack.philosofish.common;

import java.util.Comparator;

public class AlphanumericComparator implements Comparator<String> {
    @Override
    public int compare(String s1, String s2) {
        String prefix1 = extractPrefix(s1);
        String prefix2 = extractPrefix(s2);

        int letterCompare = prefix1.compareTo(prefix2);
        if (letterCompare != 0) return letterCompare; // Sort alphabetically first

        int num1 = extractNumber(s1);
        int num2 = extractNumber(s2);

        return Integer.compare(num1, num2); // Then sort numerically
    }

    private String extractPrefix(String str) {
        return str.replaceAll("[0-9]", ""); // Remove all numbers to get only letters
    }

    private int extractNumber(String str) {
        String numPart = str.replaceAll("\\D", ""); // Remove all non-digits to get the number
        return numPart.isEmpty() ? 0 : Integer.parseInt(numPart); // Convert to integer
    }
}

