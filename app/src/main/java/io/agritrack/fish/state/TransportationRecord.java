package io.agritrack.fish.state;

import android.graphics.Bitmap;

import java.util.List;

import io.agritrack.data.model.tx.TransportTransaction;

public class TransportationRecord {
    public String packagingSite;
    public String driverName;
    public String driverPhone;
    public String licensePlate;
    public Boolean refrigeratedTruck = Boolean.TRUE;
    public Boolean parallelTransport = Boolean.FALSE;
    public String clipNumber;
    public int sitePos = -1;
    public int companyPos = -1;
    public List<String> availBins;
    public byte[] signatureBytes;
    public Bitmap signature;

    public Double longitude;
    public Double latitude;
    public long txKey;
    public int hashCode;

    public TransportationRecord() {
    }
}
