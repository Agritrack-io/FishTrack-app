package io.agritrack.scale.diniargeo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import androidx.recyclerview.widget.ItemTouchHelper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.agritrack.scale.Enums;

public class MCWScale {
    private final int CONNECTION_TIMEOUT;
    private final int WAIT_BLUETOOTH;
    private final int WAIT_TCP;
    private final boolean _alibi;
    private BluetoothDevice _blueDevice;
    private InputStream _blueInput;
    private OutputStream _blueOutput;
    private BluetoothSocket _blueSocket;
    private String _bluetoothAddress;

    private String _communicationCommand;
    private boolean _communicationStop;
    private eCommunicationType _communicationType;

    private boolean _connected;
    private int _decimals;
    private String _description;
    private String _lastCommand;
    private final List<ScaleCommunicationListener> _listeners;
    private final int _portTCP;
    private final List<String> _queueCommands;
    private Socket _sock;
    private eTerminators _terminator;
    private Enums.eWeightUM _um;
    private final String _version;
    private Thread thrCommunication;

    public enum eCommunicationType {
        TCP_IP,
        Bluetooth
    }

    public enum eTerminators {
        CrLf("CrLf"),
        Cr("Cr"),
        Lf("Lf"),
        None("-----");

        private final String friendlyName;

        eTerminators(String str) {
            this.friendlyName = str;
        }

        public String toString() {
            return this.friendlyName;
        }
    }

    public String getDescription() {
        return this._description;
    }

    public void setDescripton(String str) {
        this._description = str;
    }

    public String getVersion() {
        return this._version;
    }

    public int getDecimals() {
        return this._decimals;
    }

    public Enums.eWeightUM getUM() {
        return this._um;
    }

    public void setUM(String str) {
        String lowerCase = str.toLowerCase();
        if (lowerCase.contains("kg")) {
            this._um = Enums.eWeightUM.kg;
        } else if (lowerCase.contains("g")) {
            this._um = Enums.eWeightUM.g;
        } else if (lowerCase.contains("lb")) {
            this._um = Enums.eWeightUM.lb;
        } else if (lowerCase.contains("t")) {
            this._um = Enums.eWeightUM.t;
        }
    }

    public void setUM(Enums.eWeightUM eweightum) {
        this._um = eweightum;
    }

    public boolean getAlibi() {
        return this._alibi;
    }

    public eCommunicationType getCommunicationType() {
        return this._communicationType;
    }

    public int getPortTCP() {
        return this._portTCP;
    }

    public String getBluetoothAddress() {
        return this._bluetoothAddress;
    }

    public boolean isConnected() {
        return this._connected;
    }

    public void setTerminator(eTerminators eterminators) {
        this._terminator = eterminators;
    }

    public eTerminators getTerminator() {
        return this._terminator;
    }

    public byte[] getTerminatorBytes() {
        switch (this._terminator) {
            case Cr:
                return new byte[]{13};
            case Lf:
                return new byte[]{10};
            case CrLf:
                return new byte[]{13, 10};
            default:
                return new byte[0];
        }
    }

    public MCWScale(BluetoothDevice bluetoothDevice) {
        this.CONNECTION_TIMEOUT = 4000;
        this.WAIT_BLUETOOTH = ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION;
        this.WAIT_TCP = ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION;
        this._communicationCommand = Commands.CMD_READ;
        this._blueDevice = null;
        this._blueSocket = null;
        this._blueOutput = null;
        this._blueInput = null;
        this._sock = null;
        this._lastCommand = "";
        this._description = "";
        this._version = "";
        this._decimals = 0;
        this._um = Enums.eWeightUM.kg;
        this._alibi = false;
        this._communicationType = eCommunicationType.TCP_IP;
        this._portTCP = 2001;
        this._bluetoothAddress = "00:00:00:00:00:00";
        this._connected = false;
        this._terminator = eTerminators.CrLf;
        this._queueCommands = new ArrayList();
        this._communicationStop = false;
        this.thrCommunication = new Thread(new Runnable() {
            public void run() {
                if (!MCWScale.this.isConnected()) {
                    MCWScale.this.Connect();
                }
                if (MCWScale.this.isConnected()) {
                    while (!MCWScale.this._communicationStop) {
                        try {
                            String unused = MCWScale.this._lastCommand = MCWScale.this._communicationCommand;
                            if (MCWScale.this._queueCommands.size() > 0) {
                                String unused2 = MCWScale.this._lastCommand = (String) MCWScale.this._queueCommands.get(0);
                                MCWScale.this._queueCommands.remove(0);
                            }
                            MCWScale.this.Send(MCWScale.this._lastCommand);
                            Thread.sleep(200);
                            MCWScale.this.Read();
                        } catch (Exception unused3) {
                        }
                    }
                }
                if (MCWScale.this.isConnected()) {
                    MCWScale.this.Disconnect();
                }
            }
        });
        this._listeners = new ArrayList();
        this._communicationType = eCommunicationType.Bluetooth;
        if (bluetoothDevice != null) {
            this._bluetoothAddress = bluetoothDevice.getAddress();
        }
    }

    public void Dispose() {
        Disconnect();
        if (this.thrCommunication != null) {
            this.thrCommunication = null;
        }
    }

    public void StartCommunication(String str) {
        StopCommunication();
        try {
            Thread.sleep(250);
        } catch (Exception unused) {
        }
        this._communicationCommand = str;
        this._communicationStop = false;
        this.thrCommunication.start();
    }

    public void StopCommunication() {
        this._communicationStop = true;
    }

    public void SendCommunicationCommand(String str) {
        this._queueCommands.add(str);
    }

    public boolean Connect() {
        return Connect(4000);
    }

    public boolean Connect(int i) {
        onConnectionStart();
        if (!this._connected) {
            if (this._blueDevice == null) {
                this._blueDevice = BluetoothUtils.getBluetoothAdapter().getRemoteDevice(this._bluetoothAddress);
            }
            this._blueSocket = BluetoothUtils.CreateSocket(this._blueDevice);
            try {
                this._blueSocket.connect();
                this._blueOutput = this._blueSocket.getOutputStream();
                this._blueInput = this._blueSocket.getInputStream();
                this._connected = this._blueSocket.isConnected();
            } catch (Exception unused) {
                this._connected = false;
            }
        }
        onConnectionEnd(this._connected);
        return this._connected;
    }

    public boolean Disconnect() {
        onDisconnectionStart();
        if (this._connected) {
            try {
                this._blueSocket.close();
                this._connected = this._blueSocket.isConnected();
            } catch (Exception unused) {
                this._connected = false;
            }
        }
        if (!this._connected) {
            this._blueDevice = null;
            this._blueSocket = null;
            this._blueOutput = null;
            this._blueInput = null;
        }
        if (!this._connected) {
            this._communicationStop = true;
        }
        onDisconnectionEnd(this._connected);
        return !this._connected;
    }

    public boolean TestConnection() {
        boolean Connect = Connect(1000);
        if (Connect) {
            Disconnect();
        }
        return Connect;
    }

    public boolean Send(String str) {
        return Send(str.getBytes());
    }

    public boolean Send(byte[] bArr) {
        if (!this._connected) {
            return false;
        }
        byte[] terminatorBytes = getTerminatorBytes();
        if (terminatorBytes.length > 0 && !ScaleUtils.endsWith(bArr, terminatorBytes)) {
            bArr = ScaleUtils.append(bArr, terminatorBytes);
        }
        if (!BluetoothUtils.Send(this._blueOutput, bArr)) {
            return false;
        }
        onSent(bArr);
        return true;
    }

    public String ReadString() {
        return new String(Read());
    }

    public byte[] Read() {
        if (!this._connected) {
            return new byte[0];
        }
        byte[] bArr = new byte[0];
        byte[] terminatorBytes = getTerminatorBytes();
        while (true) {
            byte[] Read = BluetoothUtils.Read(this._blueInput);
            Log.wtf("RECEIVED", String.format("LEN %d", Integer.valueOf(Read.length)));
            if (Read.length > 0) {
                int indexOf = ScaleUtils.indexOf(Read, terminatorBytes);
                if (indexOf >= 0) {
                    bArr = ScaleUtils.append(bArr, Arrays.copyOf(Read, indexOf));
                    break;
                }
                bArr = ScaleUtils.append(bArr, Read);
            }
            if (Read.length <= 0) {
                break;
            }
        }
        Log.wtf("READ ANSWER", String.format("LEN %d", Integer.valueOf(bArr.length)));
        if (bArr.length > 0) {
            onReceived(bArr);
        } else {
            onReceivedError();
        }
        return bArr;
    }

    public synchronized void addListener(ScaleCommunicationListener scaleCommunicationListener) {
        this._listeners.add(scaleCommunicationListener);
    }

    public synchronized void removeListener(ScaleCommunicationListener scaleCommunicationListener) {
        this._listeners.remove(scaleCommunicationListener);
    }

    private void onConnectionStart() {
        for (ScaleCommunicationListener ConnectionStart : this._listeners) {
            ConnectionStart.ConnectionStart();
        }
    }

    private void onConnectionEnd(boolean z) {
        for (ScaleCommunicationListener ConnectectionEnd : this._listeners) {
            ConnectectionEnd.ConnectectionEnd(z);
        }
    }

    private void onDisconnectionStart() {
        for (ScaleCommunicationListener DisconnectionStart : this._listeners) {
            DisconnectionStart.DisconnectionStart();
        }
    }

    private void onDisconnectionEnd(boolean z) {
        for (ScaleCommunicationListener DisconnectionEnd : this._listeners) {
            DisconnectionEnd.DisconnectionEnd(z);
        }
    }

    private void onSent(byte[] bArr) {
        for (ScaleCommunicationListener Sent : this._listeners) {
            Sent.Sent(bArr, new String(bArr));
        }
    }

    private void onReceived(byte[] bArr) {
        for (ScaleCommunicationListener Received : this._listeners) {
            Received.Received(bArr, new String(bArr), this._lastCommand);
        }
    }

    private void onReceivedError() {
        for (ScaleCommunicationListener ReceivedError : this._listeners) {
            ReceivedError.ReceivedError();
        }
    }
}