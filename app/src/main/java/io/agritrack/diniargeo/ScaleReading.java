package io.agritrack.diniargeo;

import com.google.android.gms.common.util.Strings;

public class ScaleReading {
    private static final String TAG = ScaleReading.class.getSimpleName();

    public enum WeightStatus {Unstable, Stable, Overloaded, Underloaded, NotLevel}
    public enum WeightType {NET, GROSS}

    private String instrumentCode;
    public WeightStatus status;
    public WeightType weightType;
    public Double weight;
    public String unit;

    public ScaleReading(String reading) {
        if (!Strings.isEmptyOrWhitespace(reading) && reading.length() > 18) {
            String[] parts = reading.split(",");
            String cc = parts[0];
            cc = cc.length() > 2 ? cc.substring(2) : cc;
            if (!Strings.isEmptyOrWhitespace(cc)) {
                switch (cc) {
                    case "US":
                        this.status = WeightStatus.Unstable;
                        break;
                    case "ST":
                        this.status = WeightStatus.Stable;
                        break;
                    case "OL":
                        this.status = WeightStatus.Overloaded;
                        break;
                    case "UL":
                        this.status = WeightStatus.Underloaded;
                        break;
                    case "TL":
                        this.status = WeightStatus.NotLevel;
                        break;
                }
            }

            String kk = parts[1];
            if (!Strings.isEmptyOrWhitespace(kk)) {
                switch (kk) {
                    case "NT":
                        this.weightType = WeightType.NET;
                        break;
                    case "GS":
                        this.weightType = WeightType.GROSS;
                        break;
                }
            }

            String pp = parts[2];
            if (!Strings.isEmptyOrWhitespace(pp)) {
                weight = Double.parseDouble(pp);
            }

            String uu = parts[3];
            if (!Strings.isEmptyOrWhitespace(uu)) {
                unit = uu;
            }
        }
    }
}
