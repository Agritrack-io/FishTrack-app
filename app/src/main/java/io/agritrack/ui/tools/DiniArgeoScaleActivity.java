package io.agritrack.ui.tools;

import android.Manifest;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.agritrack.R;
import io.agritrack.scale.diniargeo.BluetoothUtils;
import io.agritrack.scale.diniargeo.Fragment_DeviceItem;
import io.agritrack.scale.diniargeo.MCWScale;
import io.agritrack.scale.diniargeo.ScaleUtils;

public class DiniArgeoScaleActivity extends AppCompatActivity {
    private BluetoothAdapter _bluetooth = null;
    private final List<String> _devices = new ArrayList();
    private final ArrayList<BluetoothDevice> _foundedDevices = new ArrayList<>();
    private final ArrayList<Fragment_DeviceItem> fragments = new ArrayList<>();
    private Fragment_DeviceItem _pairingFragment = null;
    private LinearLayout lstDevices = null;
    private final MenuItem menuSearch = null;

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.bluetooth.adapter.action.DISCOVERY_STARTED".equals(action)) {
                DiniArgeoScaleActivity.this.ShowSearch();
                DiniArgeoScaleActivity.this.RemoveDevices();
            } else if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
                DiniArgeoScaleActivity.this.ShowSearch();
            } else if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
                if (DiniArgeoScaleActivity.this._bluetooth.isEnabled()) {
                    DiniArgeoScaleActivity.this.Bluetooth_StartSearch();
                }
            } else if ("android.bluetooth.device.action.BOND_STATE_CHANGED".equals(action)) {
                if (DiniArgeoScaleActivity.this._pairingFragment != null && DiniArgeoScaleActivity.this._pairingFragment.isPaired()) {
                    DiniArgeoScaleActivity.this._pairingFragment.Start();
                }
            } else if ("android.bluetooth.device.action.FOUND".equals(action)) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                String trim = ((bluetoothDevice == null || bluetoothDevice.getName() == null) ? "" : bluetoothDevice.getName()).trim();
                if (trim.length() > 0 && !DiniArgeoScaleActivity.this._devices.contains(trim)) {
                    DiniArgeoScaleActivity.this.AddDevice(bluetoothDevice);
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(5);
        setContentView(R.layout.activity_dini_argeo_scale);

        InitDesigner();
        getWindow().addFlags(128);
        this._bluetooth = BluetoothUtils.getBluetoothAdapter();
        Bluetooth_RegisterHandlers();
    }

    @Override
    protected void onStart() {
        super.onStart();
        BluetoothUtils.Switch(true);
        if (this._bluetooth.isEnabled()) {
            Bluetooth_StartSearch();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < this.fragments.size(); i++) {
            this.fragments.get(i).Dispose();
        }
        this.fragments.clear();
        Bluetooth_DisposeHandlers();
    }

    //#############################
    //####   Private methods   ####
    //#############################
    private void Bluetooth_RegisterHandlers() {
        registerReceiver(this.bluetoothReceiver, new IntentFilter("android.bluetooth.adapter.action.DISCOVERY_STARTED"));
        registerReceiver(this.bluetoothReceiver, new IntentFilter("android.bluetooth.adapter.action.DISCOVERY_FINISHED"));
        registerReceiver(this.bluetoothReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
        registerReceiver(this.bluetoothReceiver, new IntentFilter("android.bluetooth.device.action.FOUND"));
        registerReceiver(this.bluetoothReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
    }

    private void Bluetooth_DisposeHandlers() {
        unregisterReceiver(this.bluetoothReceiver);
    }

    public void Bluetooth_StartSearch() {
        BluetoothAdapter bluetoothAdapter = this._bluetooth;
        if (bluetoothAdapter != null && !bluetoothAdapter.isDiscovering()) {
            Iterator<Fragment_DeviceItem> it = this.fragments.iterator();
            while (it.hasNext()) {
                if (it.next().isCommunicating()) {
                    return;
                }
            }
            new Runnable() {
                public void run() {
                    DiniArgeoScaleActivity.this._bluetooth.startDiscovery();
                }
            }.run();
        }
    }

    private void ShowSearch() {
        BluetoothAdapter bluetoothAdapter = this._bluetooth;
        boolean z = bluetoothAdapter != null && bluetoothAdapter.isDiscovering();
        //setProgressBarIndeterminateVisibility(z);
        MenuItem menuItem = this.menuSearch;
        if (menuItem != null) {
            menuItem.setVisible(!z);
        }
    }

    private void InitDesigner() {
        this.lstDevices = (LinearLayout) findViewById(R.id.lstDevices_act_devices_bluetooth);
    }

    public void AddDevice(BluetoothDevice bluetoothDevice) {
        MCWScale scale = new MCWScale(bluetoothDevice);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.wtf("TROVATO DISPOSITIVO", String.format("%s [%s]", bluetoothDevice.getName(), bluetoothDevice.getAddress()));
        Fragment_DeviceItem newInstance = Fragment_DeviceItem.newInstance(getApplicationContext(), scale);
        newInstance.addListener(this.item_listener);
        this.fragments.add(newInstance);
        this._devices.add(bluetoothDevice.getName());
        FragmentTransaction beginTransaction = getFragmentManager().beginTransaction();
        //beginTransaction.add(this.lstDevices.getId(), newInstance);
        beginTransaction.commit();
        newInstance.Start();
    }

    private void RemoveDevices() {
        FragmentTransaction beginTransaction = getFragmentManager().beginTransaction();
        for (int i = 0; i < this.fragments.size(); i++) {
            //beginTransaction.remove(this.fragments.get(i));
            this.fragments.get(i).Dispose();
        }
        beginTransaction.commit();
        this.fragments.clear();
        this._devices.clear();
    }


    //#########################################
    private final Fragment_DeviceItem.ItemListener item_listener = new Fragment_DeviceItem.ItemListener() {
        public void ItemClick(Fragment_DeviceItem fragment_DeviceItem) {
            //App.SaveScale(DiniArgeoScaleActivity.this.getApplicationContext(), fragment_DeviceItem.getScale());
            new Thread(new Runnable() {
                public void run() {
                    ScaleUtils.Wait(2000);
//                    Intent intent = new Intent(DiniArgeoScaleActivity.this.getApplicationContext(), Activity_ScaleMain.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    DiniArgeoScaleActivity.this.startActivity(intent);
//                    DiniArgeoScaleActivity.this.finish();
                }
            }).start();
        }

        public void PairStart(Fragment_DeviceItem fragment_DeviceItem) {
            Fragment_DeviceItem unused = DiniArgeoScaleActivity.this._pairingFragment = fragment_DeviceItem;
        }
    };
}