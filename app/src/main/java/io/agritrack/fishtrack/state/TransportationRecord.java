package io.agritrack.fishtrack.state;

import java.util.List;

public class TransportationRecord {
    public List<String> availBins;
    public int sitePos = -1;
    public int companyPos = -1;
    public String driverName;
    public String licensePlate;

    public TransportationRecord() {}
}
