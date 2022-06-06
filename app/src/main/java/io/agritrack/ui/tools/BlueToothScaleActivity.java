package io.agritrack.ui.tools;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import io.agritrack.R;

public class BlueToothScaleActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    private TextView mStatusBleTv, mPairedTv;
    ImageView mBlueIV;
    Button mOnBtn, mOffBtn, mDiscoverBtn, mPairedBtn, mDiscoverDevicesBtn;
    ListView mDevicesList;
    BluetoothAdapter bluetoothAdapter;

    private static boolean workerStatus = Boolean.TRUE, newData;
    private InputStream inScaleStream;
    private int readBufferPosition = 0; // Variable for buffer reading

    Runnable scaleReadingsRunnable = new Runnable() {
        public void run()
        {
            final byte delimiter = '\r';
            final byte[] readBuffer = new byte[1024];
            int bytesAvailable;
            String rxdMsgT = "";
            while(workerStatus == true)
            {
                try
                {
                    if(inScaleStream!=null/* && inScaleStream.available() > 0*/)
                    {
                        bytesAvailable = 256;// inScaleStream.available();
                        byte[] packetBytes = new byte[bytesAvailable];
                        inScaleStream.read(packetBytes);
                        for(int i=0;i<bytesAvailable;i++)
                        {
                            byte b = packetBytes[i];
                            if(b == delimiter)
                            {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                final String data = new String(encodedBytes, "US-ASCII");
                                readBufferPosition = 0;
                                rxdMsgT = "Tu: " + data + "\n";
                                newData = true;
                                if (newData == true)
                                {
                                    Intent intent = new Intent("com.boson.BTComms.MainActivity");
                                    intent.putExtra("Mensaje", rxdMsgT);
                                    sendBroadcast(intent);
                                    newData = false;
                                }
                            }
                            else
                            {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                    else
                    {
                        //Log.e("BOSON", "INACTIVE");
                    }
                }
                catch (IOException ex)
                {
                    workerStatus = false;
                }
            }
        }
    };


    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                if(deviceName!=null && deviceName.contains("BTDA_")) {
                    int bondState = device.getBondState();

                    if(BluetoothDevice.BOND_NONE == bondState) {
                        boolean bondCreated = device.createBond();
                        if (!bondCreated) {
                            showToast("Pairing with BT device failed to begin!");
                        }
                    } else if(BluetoothDevice.BOND_BONDED == bondState) {
                        try {
                            BluetoothSocket bs = device.createInsecureRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
                            bs.connect();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            } else if (BluetoothAdapter.ACTION_REQUEST_ENABLE.equals(action)) {
                // Turning on Bluetooth..


            } if (BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE.equals(action)) {
                // Making Your Device Discoverable


            } if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // Device has started discovering closey BT devices
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);


            } if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Device has started discovering closey BT devices
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);


            } if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                // Device has started discovering closey BT devices
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);


            } else if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
//                android.bluetooth.adapter.action.SCAN_MODE_CHANGED
                switch(state) {
                    case BluetoothAdapter.STATE_CONNECTED:

                        break;
                    case BluetoothAdapter.STATE_DISCONNECTED:

                        break;
                    case BluetoothAdapter.STATE_OFF:

                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:

                        break;
                    case BluetoothAdapter.STATE_ON:

                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:

                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:

                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:

                        break;
                }
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch(state) {
                    case BluetoothAdapter.STATE_OFF:

                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:

                        break;
                    case BluetoothAdapter.STATE_ON:

                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:

                        break;
                }

            }
        }
    };

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.e("Activity result","OK");
                    // There are no request codes
                    Intent data = result.getData();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth_scale);
        mStatusBleTv = findViewById(R.id.statusBluetoothTv);
        mPairedTv = findViewById(R.id.pairTv);
        mBlueIV = findViewById(R.id.bluetoothIv);
        mOnBtn = findViewById(R.id.onButn);
        mOffBtn = findViewById(R.id.offButn);
        mDiscoverBtn = findViewById(R.id.discoverableBtn);
        mDiscoverDevicesBtn = findViewById(R.id.discoverBtn);
        mPairedBtn = findViewById(R.id.PairedBtn);
        mDevicesList = findViewById(R.id.devicesList);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_REQUEST_ENABLE); // Turning on Bluetooth..
        filter.addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE); // Making Your Device Discoverable..
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(btReceiver, filter);


        if (bluetoothAdapter == null) {
            mStatusBleTv.setText("Bluetooth is not available");
        } else {
            mStatusBleTv.setText("Bluetooth is  available");

            if (bluetoothAdapter.isEnabled()) {
                mBlueIV.setImageResource(R.drawable.ic_bluetooth_on);
            } else {
                mBlueIV.setImageResource(R.drawable.ic_bluetooth_off);
            }

            mOnBtn.setOnClickListener(v -> {
                if (!bluetoothAdapter.isEnabled()) {
                    showToast("Turning on Bluetooth..");
                    bluetoothAdapter.enable();

//                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                    activityResultLauncher.launch(intent);
                    //startActivityForResult(intent, REQUEST_ENABLE_BT);
                } else {
                    showToast("Bluetooth is already on");

                }
            });

            mDiscoverBtn.setOnClickListener(v -> {
                if (!bluetoothAdapter.isDiscovering()) {
                    showToast("Making Your Device Discoverable");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    activityResultLauncher.launch(intent);
                }
            });

            //
            mDiscoverDevicesBtn.setOnClickListener(v -> {
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                bluetoothAdapter.startDiscovery();
            });

            mOffBtn.setOnClickListener(v -> {
                if (bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.disable();
                    showToast("Turning  Bluetooth off");
                    mBlueIV.setImageResource(R.drawable.ic_bluetooth_off);
                } else {
                    showToast("Bluetooth is already off");

                }
            });

            mPairedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bluetoothAdapter.isEnabled()) {
                        mPairedTv.setText("Paired Devices");
                        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();

                        for (BluetoothDevice device : devices) {
                            mPairedTv.append("\n Device : " + device.getName() + " , " + device);

                            try {
//                                BluetoothServerSocket serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(device.getName(), device.getUuids()[0].getUuid());
//                                BluetoothSocket socket = serverSocket.accept();

                                BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
                                if (!bluetoothAdapter.isDiscovering()) {
//                                    socket.connect();

                                    BluetoothDevice remoteBTDevice = socket.getRemoteDevice();
                                    BluetoothSocket tmp = remoteBTDevice.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
                                    tmp.connect();

                                    inScaleStream = tmp.getInputStream();

                                    Thread scaleThread = new Thread(scaleReadingsRunnable);
                                    scaleThread.start();
//
//                                    InputStream is = tmp.getInputStream();
//                                    int aa = is.read();
//                                    Log.i(null, "read: " + aa);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                    } else {
                        showToast("Turn On bluetooth to get paired devices");
                    }
                }
            });
        }
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}