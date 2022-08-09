package io.agritrack.ui.tools;

import static android.view.View.VISIBLE;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;

import io.agritrack.R;
import io.agritrack.scale.diniargeo.BluetoothUtils;
import io.agritrack.scale.diniargeo.ClassREAD;
import io.agritrack.scale.diniargeo.MCWScale;
import io.agritrack.ui.login.LoginActivity;

public class DiniArgeoScaleActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter = null;
    private TextView tvName, tvMac, tvReading;
    private Button btnGetReading = null;
    private Button btnBluetooth = null;
    private ProgressBar pbBluetooth;
    private String currState = null;
    private BluetoothDevice bluetoothDevice = null;
    private MCWScale scale = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dini_argeo_scale);

        // assign controls to global variables
        assignCtrlVars();

        // get instance of BT Adapter. Will be used to search dor BT devices.
        this.bluetoothAdapter = BluetoothUtils.getBluetoothAdapter();

        // register handlers for BT events.
        bluetooth_RegisterHandlers();

        btnGetReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Checks if Bluetooth Adapter is present
                if (bluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(), "Bluetooth Not Supported", Toast.LENGTH_SHORT).show();
                } else if(scale==null) {
                    Toast.makeText(getApplicationContext(), "No Scale was found!", Toast.LENGTH_SHORT).show();
                } else {
                    boolean connected = scale.Connect();
                    if(connected) {
                        boolean sentReadCmd = scale.Send("READ");
                        String read = scale.ReadString();
                        if(!Strings.isEmptyOrWhitespace(read)) {
                            ClassREAD reading = new ClassREAD(read);
                            if(reading != null) {
                                tvReading.append(String.format("%.0f %s %s\n", reading.getNet(), reading.getWeigthUM(), reading.getWeigthState()));
                            }
                        }
                    }
                }
            }
        });

        // BT status button listener. On each click it switches current BT device to different state.
        btnBluetooth.setOnClickListener(view -> {
            if (currState == null) {
                changeState("ENABLE_BT");
            } else if ("READY".equalsIgnoreCase(currState)) {
                changeState("READ");
            }
        });
        configFooter();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Bluetooth_DisposeHandlers();
    }


    private void changeState(String state) {
        runOnUiThread(() -> btnBluetooth.setText(state));

        switch (state) {
            case "ENABLE_BT":
                runOnUiThread(() -> btnBluetooth.setText("Enabling BT..."));
                // enabling Bluetooth device
                BluetoothUtils.Switch(true);
                // start discovering
                if (this.bluetoothAdapter != null && !this.bluetoothAdapter.isDiscovering()) {
                    ((Runnable) () -> DiniArgeoScaleActivity.this.bluetoothAdapter.startDiscovery()).run();
                }
                break;
            case "DISCOVERY_STARTED":
                runOnUiThread(() -> btnBluetooth.setText("Discovering BT devices..."));
                break;
            case "DISCOVERY_FINISHED":
                runOnUiThread(() -> btnBluetooth.setText("Finished discovering BT devices..."));
                currState = "READY";
                break;
            case "STATE_CHANGED":
                break;
            case "BOND_STATE_CHANGED":
                runOnUiThread(() -> btnBluetooth.setText("BT pairing changed..."));
                break;
            case "FOUND":
                currState = "FOUND";
                runOnUiThread(() -> btnBluetooth.setText("BT devices found..."));
                break;
            case "READ":
                currState = "READ";
                runOnUiThread(() -> btnBluetooth.setText("Ready for Readings..."));
                break;
            default:
        }
    }


    //#############################
    //####   Private methods   ####
    //#############################
    private void bluetooth_RegisterHandlers() {
        registerReceiver(this.bluetoothReceiver, new IntentFilter("android.bluetooth.adapter.action.DISCOVERY_STARTED"));
        registerReceiver(this.bluetoothReceiver, new IntentFilter("android.bluetooth.adapter.action.DISCOVERY_FINISHED"));
        registerReceiver(this.bluetoothReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
        registerReceiver(this.bluetoothReceiver, new IntentFilter("android.bluetooth.device.action.FOUND"));
        registerReceiver(this.bluetoothReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
    }

    private void Bluetooth_DisposeHandlers() {
        unregisterReceiver(this.bluetoothReceiver);
    }

    private void assignCtrlVars() {
        this.tvName = findViewById(R.id.nameTv);
        this.tvMac = findViewById(R.id.macAddressTv);
        this.tvReading = findViewById(R.id.tvReading);
        this.tvReading.setMovementMethod(new ScrollingMovementMethod());
        this.btnGetReading = findViewById(R.id.btnGetReading);
        this.btnBluetooth = findViewById(R.id.btnBlueTooth);
        this.pbBluetooth =  findViewById(R.id.pbBluetooth);
    }

    private void addDevice(BluetoothDevice bluetoothDevice) {
        this.scale = new MCWScale(bluetoothDevice);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.wtf("DEVICE FOUND", String.format("%s [%s]", bluetoothDevice.getName(), bluetoothDevice.getAddress()));
        // append in the two separate views
        tvName.append(bluetoothDevice.getName() + "\n");
        tvMac.append(bluetoothDevice.getAddress() + "\n");
    }

    // Broadcast receiver that handles BlueTooth events.
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.bluetooth.adapter.action.DISCOVERY_STARTED".equals(action)) {
                changeState("DISCOVERY_STARTED");
                pbBluetooth.setVisibility(VISIBLE);
            } else if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
                changeState("DISCOVERY_FINISHED");
                pbBluetooth.setVisibility(View.INVISIBLE);
                btnGetReading.setVisibility(VISIBLE);
            } else if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
                if (DiniArgeoScaleActivity.this.bluetoothAdapter.isEnabled()) {
                    changeState("STATE_CHANGED");
                    currState = null;
                }
            } else if ("android.bluetooth.device.action.BOND_STATE_CHANGED".equals(action)) {
                changeState("BOND_STATE_CHANGED");
            } else if ("android.bluetooth.device.action.FOUND".equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                String trim = ((bluetoothDevice == null || bluetoothDevice.getName() == null) ? "" : bluetoothDevice.getName()).trim();
                if (trim.length() > 0 && trim.startsWith("BTDA")) {
                    addDevice(bluetoothDevice);
                    changeState("FOUND");
                }
            }
        }
    };

    private void configFooter() {
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        });
    }
}