package io.agritrack.scale.diniargeo;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import io.agritrack.R;


public class Fragment_DeviceItem extends Fragment {

    private final Handler _handler = new Handler();
    private String _model = "";
    private Context context = null;
    private ImageView imgIcon = null;
    private View item = null;
    private TextView lblSubTitle = null;
    private TextView lblTitle = null;
    private final List<ItemListener> listeners = new ArrayList();
    private ProgressBar prgLoading = null;
    private MCWScale scale = null;
    private Thread thr = null;


    public Fragment_DeviceItem() {
        // Required empty public constructor
    }


    public static Fragment_DeviceItem newInstance(Context ctx, MCWScale scale) {
        Fragment_DeviceItem fragment_DeviceItem = new Fragment_DeviceItem();
        fragment_DeviceItem.context = ctx;
        fragment_DeviceItem.scale = scale;
        return fragment_DeviceItem;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.Start();
    }

    public void Start() {
        this.thr = new Thread(this.Task_ScaleModel);
        this.thr.start();
    }

    public void Dispose() {
        if (this.scale.isConnected()) {
            this.scale.Disconnect();
        }
        Thread thread = this.thr;
        if (thread != null && thread.isAlive()) {
            this.thr.stop();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.listviewitem_device, container, false);
        InitDesigner(inflate);
        return inflate;
    }



    //##############################################
    private String getScaleModel() {
        String str = "";
        if (this.scale.getCommunicationType() == MCWScale.eCommunicationType.Bluetooth) {
            String bluetoothAddress = this.scale.getBluetoothAddress();
//            if (scalesCacheManager.CheckExists(bluetoothAddress)) {
//                str = scalesCacheManager.getModelFromMAC(bluetoothAddress).trim();
//            }
        }
        if (str.length() == 0) {
            if (this.scale.Connect()) {
                for (int i = 0; i < 5; i++) {
                    try {
                        this.scale.Send(Commands.CMD_READ_VERSION);
                        str = this.scale.ReadString().split(Pattern.quote(","))[2].trim();
                    } catch (Exception unused) {
                    }
                    if (str.length() > 0) {
                        break;
                    }
                    ScaleUtils.Wait(100);
                }
                this.scale.Disconnect();
            } else {
                Log.wtf("getScaleModel()", "SCALE CONNECTION ERROR [" + this.scale.getBluetoothAddress() + "]");
            }
        }
        return str;
    }

    public void addListener(ItemListener itemListener) {
        this.listeners.add(itemListener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onItemClick() {
        for (ItemListener itemListener : this.listeners) {
            itemListener.ItemClick(this);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPairStart() {
        for (ItemListener itemListener : this.listeners) {
            itemListener.PairStart(this);
        }
    }

    public MCWScale getScale() {
        return this.scale;
    }

    public String getModel() {
        return this._model;
    }

    public BluetoothDevice getBluetoothDevice() {
        return BluetoothUtils.getBluetoothAdapter().getRemoteDevice(this.scale.getBluetoothAddress());
    }

    public boolean isPaired() {
        if (this.scale.getCommunicationType() == MCWScale.eCommunicationType.Bluetooth) {
            return BluetoothUtils.isPaired(this.context, getBluetoothDevice());
        }
        return true;
    }

    public boolean isCommunicating() {
        Thread thread = this.thr;
        if (thread == null) {
            return false;
        }
        return thread.isAlive();
    }

    @SuppressLint("MissingPermission")
    private void InitDesigner(View view) {
        this.item = view;
        this.lblTitle = (TextView) view.findViewById(R.id.lblTitle_item_device);
        this.lblSubTitle = (TextView) view.findViewById(R.id.lblSubTitle_item_device);
        this.imgIcon = (ImageView) view.findViewById(R.id.imgIcon_item_device);
        this.prgLoading = (ProgressBar) view.findViewById(R.id.prgLoading_item_device);
        switch (this.scale.getCommunicationType()) {
            case Bluetooth:
                BluetoothDevice bluetoothDevice = getBluetoothDevice();
                this.imgIcon.setImageResource(R.drawable.ic_action_bluetooth);
                this.lblTitle.setText(bluetoothDevice.getName());
                this.lblSubTitle.setText(bluetoothDevice.getAddress());
                break;
        }
        this.lblTitle.setOnClickListener(this.item_click);
        this.lblSubTitle.setOnClickListener(this.item_click);
        this.imgIcon.setOnClickListener(this.item_click);
    }

    //##############################################

    private final Runnable Task_ScaleModel = new Runnable() {
        /* class com.scaleapp.Fragment_DeviceItem.AnonymousClass2 */

        public void run() {
            Fragment_DeviceItem.this._model = "";
            Fragment_DeviceItem.this._handler.post(new Runnable() {
                /* class com.scaleapp.Fragment_DeviceItem.AnonymousClass2.AnonymousClass1 */

                public void run() {
                    Fragment_DeviceItem.this.prgLoading.setVisibility(View.VISIBLE);
                }
            });
            if (Fragment_DeviceItem.this.isPaired()) {
                Fragment_DeviceItem fragment_DeviceItem = Fragment_DeviceItem.this;
                fragment_DeviceItem._model = fragment_DeviceItem.getScaleModel();
                if (Fragment_DeviceItem.this._model.length() > 0 && Fragment_DeviceItem.this.scale.getCommunicationType() == MCWScale.eCommunicationType.Bluetooth) {
                    //new ScalesCacheManager().AddScaleToCache(Fragment_DeviceItem.this.scale.getBluetoothAddress(), Fragment_DeviceItem.this._model);
                }
            }
            if (!Fragment_DeviceItem.this.isPaired()) {
                Fragment_DeviceItem.this._handler.post(new Runnable() {
                    /* class com.scaleapp.Fragment_DeviceItem.AnonymousClass2.AnonymousClass4 */

                    public void run() {
                        Fragment_DeviceItem.this.lblSubTitle.setText("Pair device");
                        Fragment_DeviceItem.this.lblSubTitle.setTextColor(Fragment_DeviceItem.this.getResources().getColor(R.color.orange));
                        Fragment_DeviceItem.this.prgLoading.setVisibility(View.GONE);
                    }
                });
            } else if (Fragment_DeviceItem.this._model.length() > 0) {
                Fragment_DeviceItem.this._handler.post(new Runnable() {
                    /* class com.scaleapp.Fragment_DeviceItem.AnonymousClass2.AnonymousClass2 */

                    public void run() {
                        Fragment_DeviceItem.this.lblSubTitle.setText(Fragment_DeviceItem.this.lblTitle.getText());
                        Fragment_DeviceItem.this.lblSubTitle.setTextColor(Fragment_DeviceItem.this.getResources().getColor(R.color.white));
                        Fragment_DeviceItem.this.lblTitle.setTextColor(Fragment_DeviceItem.this.getResources().getColor(R.color.lime));
                        Fragment_DeviceItem.this.lblTitle.setText(Fragment_DeviceItem.this._model);
                        Fragment_DeviceItem.this.prgLoading.setVisibility(View.GONE);
                    }
                });
            } else {
                Fragment_DeviceItem.this._handler.post(new Runnable() {
                    /* class com.scaleapp.Fragment_DeviceItem.AnonymousClass2.AnonymousClass3 */

                    public void run() {
                        Fragment_DeviceItem.this.lblSubTitle.setText("Connection error");
                        Fragment_DeviceItem.this.lblSubTitle.setTextColor(Fragment_DeviceItem.this.getResources().getColor(R.color.gray));
                        Fragment_DeviceItem.this.lblTitle.setTextColor(Fragment_DeviceItem.this.getResources().getColor(R.color.gray));
                        Fragment_DeviceItem.this.prgLoading.setVisibility(View.GONE);
                    }
                });
            }
        }
    };

    public interface ItemListener {
        void ItemClick(Fragment_DeviceItem fragment_DeviceItem);

        void PairStart(Fragment_DeviceItem fragment_DeviceItem);
    }

    private final View.OnClickListener item_click = new View.OnClickListener() {
        /* class com.scaleapp.Fragment_DeviceItem.AnonymousClass1 */

        public void onClick(View view) {
            if (Fragment_DeviceItem.this.thr != null && Fragment_DeviceItem.this.thr.isAlive()) {
                return;
            }
            if (!Fragment_DeviceItem.this.isPaired()) {
                BluetoothUtils.Pair(Fragment_DeviceItem.this.context, Fragment_DeviceItem.this.getBluetoothDevice());
                Fragment_DeviceItem.this.onPairStart();
                Fragment_DeviceItem.this.lblSubTitle.setText("Pairing");
            } else if (Fragment_DeviceItem.this._model.length() > 0) {
                Fragment_DeviceItem.this.item.setBackgroundColor(Fragment_DeviceItem.this.getResources().getColor(R.color.azure));
                Fragment_DeviceItem.this.item.startAnimation(AnimationUtils.loadAnimation(Fragment_DeviceItem.this.context, R.anim.blink));
                Fragment_DeviceItem.this.onItemClick();
            }
        }
    };
}