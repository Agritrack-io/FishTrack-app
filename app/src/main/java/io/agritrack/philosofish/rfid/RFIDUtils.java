package io.agritrack.philosofish.rfid;

public class RFIDUtils {


    public static void WaitFor(long interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
