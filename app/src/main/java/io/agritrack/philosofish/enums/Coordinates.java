package io.agritrack.philosofish.enums;

public enum Coordinates {
    LONGITUDE(0), LATITUDE(1);

    private final int tp;

    Coordinates(int coordType) {
        this.tp = coordType;
    }

    public int getLiteral(){
        return tp;
    }
}
