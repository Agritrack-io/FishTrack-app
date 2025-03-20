package io.agritrack.philosofish.data.type;


public class EpcPerDevice {
    private String type;
    private String epc;

    public String getEpc() {
        return this.epc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }
}
