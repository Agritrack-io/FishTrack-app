package io.agritrack.kefalonia.scale.diniargeo;

import android.util.Log;

public class ClassREAD {
    private static final String NO_ALIBI = "---";
    private int Decimals = 0;
    private float Net = 0.0f;
    private Enums.eWeightUM UM = Enums.eWeightUM.kg;
    private Enums.eWeigthState WeightState = Enums.eWeigthState.Tilt;

    public Enums.eWeigthState getWeigthState() {
        return this.WeightState;
    }

    public Enums.eWeightUM getWeigthUM() {
        return this.UM;
    }

    public float getNet() {
        return this.Net;
    }

    public int getDecimals() {
        return this.Decimals;
    }

    public ClassREAD(String str) {
        LoadValues(str);
    }

    private void LoadValues(String str) {
        try {
            String[] split = str.split(",");
            if (split[0].toString().toUpperCase().contains("ST")) {
                this.WeightState = Enums.eWeigthState.Stable;
            } else if (split[0].toString().toUpperCase().contains("US")) {
                this.WeightState = Enums.eWeigthState.Unstable;
            } else if (split[0].toString().toUpperCase().contains("UL")) {
                this.WeightState = Enums.eWeigthState.Underload;
            } else if (split[0].toString().toUpperCase().contains("OL")) {
                this.WeightState = Enums.eWeigthState.Overload;
            } else {
                this.WeightState = Enums.eWeigthState.Tilt;
            }
            if (split[3].toString().toLowerCase().contains("kg")) {
                this.UM = Enums.eWeightUM.kg;
            } else if (split[3].toString().toLowerCase().contains("g")) {
                this.UM = Enums.eWeightUM.g;
            } else if (split[3].toString().toLowerCase().contains("lb")) {
                this.UM = Enums.eWeightUM.lb;
            } else if (split[3].toString().toLowerCase().contains("t")) {
                this.UM = Enums.eWeightUM.t;
            }
            this.Net = Float.valueOf(split[2].toString().replace(this.UM.toString(), "")).floatValue();
            this.Decimals = ScaleUtils.getDecimalsFromString(split[2].toString().replace(this.UM.toString(), ""));
        } catch (Exception e) {
            this.WeightState = Enums.eWeigthState.Tilt;
            this.UM = Enums.eWeightUM.kg;
            this.Net = 0.0f;
            this.Decimals = 0;
            Log.wtf("READ Parser Error", e.getMessage());
        }
    }
}
