package io.agritrack.kefalonia.scale.diniargeo;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.Socket;

public class TimedSocket {
    private static final int POLL_DELAY = 100;

    public static Socket getSocket(InetAddress inetAddress, int i, int i2) throws IOException {
        SocketThread socketThread = new SocketThread(inetAddress, i);
        socketThread.start();
        int i3 = 0;
        while (!socketThread.isConnected()) {
            if (!socketThread.isError()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException unused) {
                }
                i3 += 100;
                if (i3 > i2) {
                    throw new InterruptedIOException("Could not connect for " + i2 + " milliseconds");
                }
            } else {
                throw socketThread.getException();
            }
        }
        return socketThread.getSocket();
    }

    public static Socket getSocket(String str, int i, int i2) throws IOException {
        return getSocket(InetAddress.getByName(str), i, i2);
    }

    public static void main(String[] strArr) throws Exception {
        try {
            getSocket("192.168.0.3", 80, 5000).close();
            System.out.println("connected");
        } catch (IOException unused) {
            System.out.println("time out");
        }
    }

    static class SocketThread extends Thread {
        private volatile Socket m_connection = null;
        private IOException m_exception = null;
        private String m_host = null;
        private InetAddress m_inet = null;
        private int m_port = 0;

        public SocketThread(String str, int i) {
            this.m_host = str;
            this.m_port = i;
        }

        public SocketThread(InetAddress inetAddress, int i) {
            this.m_inet = inetAddress;
            this.m_port = i;
        }

        public void run() {
            Socket socket;
            try {
                if (this.m_host != null) {
                    socket = new Socket(this.m_host, this.m_port);
                } else {
                    socket = new Socket(this.m_inet, this.m_port);
                }
                this.m_connection = socket;
            } catch (IOException e) {
                this.m_exception = e;
            }
        }

        public boolean isConnected() {
            return this.m_connection != null;
        }

        public boolean isError() {
            return this.m_exception != null;
        }

        public Socket getSocket() {
            return this.m_connection;
        }

        public IOException getException() {
            return this.m_exception;
        }
    }
}
