package io.agritrack.philosofish.fish.state;

import android.graphics.Bitmap;

import io.agritrack.philosofish.data.model.common.FinalSample;

public class FinalQualityRecord {

    public String lot;
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
    public Bitmap signature;
    public byte[] signatureBytes;

    public FinalSample sample1 = new FinalSample();
    public FinalSample sample2 = new FinalSample();
    public FinalSample sample3 = new FinalSample();

}
