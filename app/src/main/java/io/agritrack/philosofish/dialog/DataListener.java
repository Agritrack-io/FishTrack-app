package io.agritrack.philosofish.dialog;

public interface DataListener {
    void onDataPassed(Double fishT, Double waterT, String corrAction);
}
