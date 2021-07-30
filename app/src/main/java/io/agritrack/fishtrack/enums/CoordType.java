package io.agritrack.fishtrack.enums;

public enum CoordType {
    LONGITUDE(0), LATITUDE(1);

    public final int tp;

    CoordType(int coordType) {
        this.tp = coordType;
    }

    public int getLiteral(){
        return tp;
    }
}
