package io.agritrack.kefalonia.enums;

public enum Locales {
    ENGLISH(0), GREEK(1), SPANISH(2);

    private final int lng;

    Locales(int lang) {
        this.lng = lang;
    }

    public int getValue(){
        return this.lng;
    }

    public String getCode() {
        switch (this.lng) {
            case 1:
                return "el";
            case 2:
                return "es";
            default:
                return "en";
        }
    }
}
