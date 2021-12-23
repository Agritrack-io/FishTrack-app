package io.agritrack.caen.pojo;

import java.util.Objects;

public class RFIDTag {

    private String epc;
    private int rssi;

    public RFIDTag(String epc, int rssi) {
        this.epc = epc;
        this.rssi = rssi;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RFIDTag rfidTag = (RFIDTag) o;
        return Objects.equals(epc, rfidTag.epc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(epc);
    }
}
