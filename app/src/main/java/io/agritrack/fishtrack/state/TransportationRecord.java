package io.agritrack.fishtrack.state;

import android.graphics.Bitmap;

import java.util.Set;

public class TransportationRecord {
    public String packagingSite;
    public String destinationCompany;
    public String driverName;
    public String licensePlate;
    public Boolean refrigeratedTruck = Boolean.TRUE;
    public Boolean parallelTransport = Boolean.FALSE;
    public Set<String> loadedBins;
    public String clipNumber;
    public int sitePos = -1;
    public int companyPos = -1;
    public Set<String> availBins;
    public byte[] signatureBytes;
    public Bitmap signature;

    public TransportationRecord() {}
}
