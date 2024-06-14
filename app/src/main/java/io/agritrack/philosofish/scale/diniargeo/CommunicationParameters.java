package io.agritrack.philosofish.scale.diniargeo;

public class CommunicationParameters {
    private static int _readTimeout = 3000;
    private static int _sendTimeout = 3000;

    public static int getSendTimeout() {
        return _sendTimeout;
    }

    public static void setSendTimeout(int i) {
        _sendTimeout = i;
    }

    public static int getReadTimeout() {
        return _readTimeout;
    }

    public static void setReadTimeout(int i) {
        _readTimeout = i;
    }
}