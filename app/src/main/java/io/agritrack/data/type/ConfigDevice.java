package io.agritrack.data.type;

import java.util.List;

import lombok.Data;

@Data
public class ConfigDevice {

    private String prefix;
    private List<EpcPerDevice> epcs;
}
