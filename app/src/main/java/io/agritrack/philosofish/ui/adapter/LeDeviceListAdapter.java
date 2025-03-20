package io.agritrack.philosofish.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvPacketSensor;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvType;
import com.kkmcn.kbeaconlib2.KBeacon;

import io.agritrack.philosofish.R;


public class LeDeviceListAdapter extends BaseAdapter {

	// Adapter for holding devices found through scanning.
	public interface ListDataSource {
		KBeacon getBeaconDevice(int nIndex);

		int getCount();
	}

	private ListDataSource mDataSource;
	private Context mContext;

	public LeDeviceListAdapter(ListDataSource c, Context ctx) {
		super();
		mDataSource = c;
		mContext = ctx;
	}

	@Override
	public int getCount() {
		return mDataSource.getCount();
	}

	@Override
	public Object getItem(int i) {
		return mDataSource.getBeaconDevice(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}


	@Override
	public View getView(int i, View view, ViewGroup viewGroup)
	{
        try {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null)
            {
                view = LayoutInflater.from(mContext).inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceName = view
                        .findViewById(R.id.beacon_name);

                viewHolder.deviceMacAddr =  view
                        .findViewById(R.id.beacon_mac_address);

                viewHolder.rssiState = view
                        .findViewById(R.id.beacon_rssi);

                viewHolder.deviceBatteryPercent = view
                        .findViewById(R.id.beacon_battery_percent);


                //humidity
                viewHolder.llHTSensor= view
                        .findViewById(R.id.ll_ht_sensor);
                viewHolder.deviceTemp = view
                        .findViewById(R.id.tv_temp);



                view.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) view.getTag();
            }

            KBeacon device = mDataSource.getBeaconDevice(i);
            if (device == null) {
                return null;
            }

            if (device.getName() != null && device.getName().length() > 0) {
                viewHolder.deviceName.setText(device.getName());
            }

            //common field
            String strMacAddress = mContext.getString(R.string.BEACON_MAC_ADDRESS) + device.getMac();
            viewHolder.deviceMacAddr.setText(strMacAddress);

            String strRssiValue = mContext.getString(R.string.BEACON_RSSI_VALUE) + device.getRssi();
            viewHolder.rssiState.setText(strRssiValue);

            String strBattPercent = mContext.getString(R.string.BEACON_BATTERY) + device.getBatteryPercent() + "%";
            viewHolder.deviceBatteryPercent.setText(strBattPercent);

            if (device.getBatteryPercent() < 20) {
                viewHolder.deviceBatteryPercent.setTextColor(Color.RED);
            }

            String strNA = "N/A";


            //KBSensor info
            KBAdvPacketSensor kSensor = (KBAdvPacketSensor) device.getAdvPacketByType(KBAdvType.Sensor);
            if (kSensor != null)
            {
                //humidity and temp info
                StringBuffer strHTInfo = new StringBuffer(50);
                if (kSensor.getTemperature() != null)
                {
                    strHTInfo.append(kSensor.getTemperature())
                            .append("℃ ");
                }
                viewHolder.deviceTemp.setText(strHTInfo);
                if (kSensor.getTemperature() > 4.0) {
                    viewHolder.deviceTemp.setTextColor(Color.RED);
                } else {
                    viewHolder.deviceTemp.setTextColor(Color.GREEN);
                }
            }
            else
            {
                viewHolder.llHTSensor.setVisibility(View.GONE);
            }

            return view;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	class ViewHolder {
		TextView deviceName;      //名
		TextView rssiState;     //状态
		TextView deviceBatteryPercent;
		TextView deviceMacAddr;
		LinearLayout llHTSensor;
		TextView deviceTemp;
	}
}
