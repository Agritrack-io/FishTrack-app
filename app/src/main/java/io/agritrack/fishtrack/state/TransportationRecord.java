package io.agritrack.fishtrack.state;

public class TransportationRecord {
    public String packagingSite;
    public String destinationCompany;
    public String driverName;
    public String licensePlate;
    public Boolean refrigeratedTruck;
    public Boolean parallelTransport;
    public String clipNumber;
    public int sitePos = -1;
    public int companyPos = -1;

    public TransportationRecord() {}
}
