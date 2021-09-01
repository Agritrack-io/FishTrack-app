package io.agritrack.fishtrack.state;

import android.graphics.Bitmap;

import java.util.List;

public class TransportationRecord {
    public String packagingSite;
    public String destinationCompany;
    public String driverName;
    public String licensePlate;
    public Boolean refrigeratedTruck = Boolean.TRUE;
    public Boolean parallelTransport = Boolean.FALSE;
    public String clipNumber;
    public int sitePos = -1;
    public int companyPos = -1;
    public List<String> availBins;
    public byte[] signatureBytes;
    public Bitmap signature;

    public TransportationRecord() {}
}
