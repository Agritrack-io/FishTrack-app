package io.agritrack.caen.api;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.caen.api.CAEN_CONSTANTS.EPCBANK;

import android.widget.Toast;

import com.handheld.uhfr.UHFRManager;
import com.uhf.api.cls.Reader;

import cn.pda.serialport.Tools;

public class BX6100Programmer  extends AbstractX9Programmer {
    private final short timeout = 500;
    private final String accessPwd = "00000000";
    private final int filterStartAddress = 2;
    private UHFRManager mUhfRManager;
    private byte[] epcBytes;
    private String tagToSearch;

    public BX6100Programmer() {
        mUhfRManager = UHFRManager.getInstance();// Init Uhf module
        if (mUhfRManager != null) {
            Reader.READER_ERR err = mUhfRManager.setPower(33, 33);//set uhf module power

            if (err == Reader.READER_ERR.MT_OK_ERR) {
                mUhfRManager.setRegion(Reader.Region_Conf.RG_EU3);
                //Toast.makeText(getAppContext(), "FreRegion:" + Reader.Region_Conf.RG_EU3 + "\n" + "Read Power:" + 33 + "\n" + "Write Power:" + 33, Toast.LENGTH_LONG).show();
            } else {
                Reader.READER_ERR err1 = mUhfRManager.setPower(30, 30);//set uhf module power
                if (err1 == Reader.READER_ERR.MT_OK_ERR) {
                    mUhfRManager.setRegion(Reader.Region_Conf.RG_EU3);
                    //Toast.makeText(getAppContext(), "FreRegion:" + Reader.Region_Conf.RG_EU3 + "\n" + "Read Power:" + 30 + "\n" + "Write Power:" + 30, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getAppContext(), "Failed to initialize UHFR manager", Toast.LENGTH_LONG);
                }
            }
        } else {
            Toast.makeText(getAppContext(), "Failed to initialize UHFR manager", Toast.LENGTH_LONG);
        }
    }

    public void HighPowerLevel() {
        if (mUhfRManager != null) {
            Reader.READER_ERR err = mUhfRManager.setPower(33, 33);//set uhf module power
            if (err != Reader.READER_ERR.MT_OK_ERR) {
                Reader.READER_ERR err1 = mUhfRManager.setPower(30, 30);//set uhf module power
                if (err1 != Reader.READER_ERR.MT_OK_ERR) {
                    Toast.makeText(getAppContext(), "Failed to switch to HIGH Energy mode!!", Toast.LENGTH_LONG);
                }
            }
        } else {
            Toast.makeText(getAppContext(), "No UHFR manager found!!", Toast.LENGTH_LONG);
        }
    }

    public void LowPowerLevel() {
        if (mUhfRManager != null) {
            Reader.READER_ERR err = mUhfRManager.setPower(16, 16);//set uhf module power
            if (err != Reader.READER_ERR.MT_OK_ERR) {
                Reader.READER_ERR err1 = mUhfRManager.setPower(15, 15);//set uhf module power
                if (err1 != Reader.READER_ERR.MT_OK_ERR) {
                    Toast.makeText(getAppContext(), "Failed to switch to HIGH Energy mode!!", Toast.LENGTH_LONG);
                }
            }
        } else {
            Toast.makeText(getAppContext(), "No UHFR manager found!!", Toast.LENGTH_LONG);
        }
    }

    @Override
    public int[] getPowerLevel() {
        return mUhfRManager.getPower();
    }

    @Override
    public Reader.READER_ERR writeTagEPC(String epc) {
        if(this.mUhfRManager != null) {
            byte[] epcBytes = Tools.HexString2Bytes(epc) ;
            byte[] accessBytes = Tools.HexString2Bytes(accessPwd) ;

            this.mUhfRManager.writeTagEPC(epcBytes, accessBytes, this.timeout);
//            this.mUhfRManager.writeTagEPC(byte[] data, byte[] accesspwd, short timeout);
        }
        return null;
    }

    @Override
    public Reader.READER_ERR writeTagEPCByFilter(String epc, String fdata) {
        if(this.mUhfRManager != null) {
            byte[] epcBytes = Tools.HexString2Bytes(epc) ;
            byte[] accessBytes = Tools.HexString2Bytes(accessPwd) ;
            byte[] fdataBytes = Tools.HexString2Bytes(fdata) ;

            return this.mUhfRManager.writeTagEPCByFilter(epcBytes, accessBytes, this.timeout, fdataBytes, 1, 2, true);
//            this.mUhfRManager.writeTagEPCByFilter(byte[] data, byte[] accesspwd, short timeout, byte[] fdata, int fbank, int fstartaddr, boolean matching);
        }
        return null;
    }

    @Override
    public void setFilterEPC(String epc) {
        this.tagToSearch = epc;
        this.epcBytes = Tools.HexString2Bytes(epc);
        this.mUhfRManager.setInventoryFilter(this.epcBytes, EPCBANK, filterStartAddress, true);
    }

    @Override
    public boolean clearEPCFilter() {
        this.tagToSearch = null;
        this.epcBytes = null;
        this.mUhfRManager.setCancleInventoryFilter();

        return true;
    }

    @Override
    public void close() {
        this.mUhfRManager.close();
    }
}
