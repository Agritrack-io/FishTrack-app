package io.agritrack.philosofish.data.type;

import java.util.List;


public class ConfigDevice {

    private String prefix;
    private List<EpcPerDevice> epcs;

    public ConfigDevice() {
    }

    public List<EpcPerDevice> getEpcs() {
        return epcs;
    }

    public void setEpcs(List<EpcPerDevice> epcs) {
        this.epcs = epcs;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
