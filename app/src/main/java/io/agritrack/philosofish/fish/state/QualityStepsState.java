package io.agritrack.philosofish.fish.state;


public class QualityStepsState {
    public static boolean[] completed = new boolean[4];

    public static void setPackagingStep(boolean done) {
        completed[2] = done;
    }
}
