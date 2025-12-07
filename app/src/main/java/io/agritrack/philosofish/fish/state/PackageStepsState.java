package io.agritrack.philosofish.fish.state;

public class PackageStepsState {
    public static boolean[] completed = new boolean[3];

    public static void updateParentQualityStep() {
        boolean allDone = completed[0] && completed[1] && completed[2];
        QualityStepsState.setPackagingStep(allDone);
    }
}
