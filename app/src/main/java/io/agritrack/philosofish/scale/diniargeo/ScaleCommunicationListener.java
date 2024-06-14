package io.agritrack.philosofish.scale.diniargeo;

public interface ScaleCommunicationListener {
    void ConnectectionEnd(boolean z);

    void ConnectionStart();

    void DisconnectionEnd(boolean z);

    void DisconnectionStart();

    void Received(byte[] bArr, String str, String str2);

    void ReceivedError();

    void Sent(byte[] bArr, String str);
}