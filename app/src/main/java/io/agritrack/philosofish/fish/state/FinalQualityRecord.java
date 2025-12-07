package io.agritrack.philosofish.fish.state;

import android.graphics.Bitmap;

import io.agritrack.philosofish.data.model.common.FinalSample;

public class FinalQualityRecord {

    public String lot;
    public String fishLot;
    public Integer exfoRating;
    public Integer paletteRating;
    public Integer boxRating;
    public Boolean cylinrical;
    public Boolean expanded;
    public Boolean soft;
    public Boolean head;
    public Boolean body;
    public Boolean areas;
    public String bestBefore;
    public String corrAction;
    public Boolean lotAccepted, foreignBody;
    public Double discardedQty;
    public Bitmap signature;
    public byte[] signatureBytes;

    public String standardType;   // GGAP / FIG / ASC / OTHER
    public String standardOther;  // Optional text when OTHER

    public FinalSample sample1 = new FinalSample();
    public FinalSample sample2 = new FinalSample();
    public FinalSample sample3 = new FinalSample();

}
