package io.agritrack.fishtrack.enums;

public enum Coordinates {
    LONGITUDE(0), LATITUDE(1);

    public final int tp;

    Coordinates(int coordType) {
        this.tp = coordType;
    }

    public int getLiteral(){
        return tp;
    }
}
