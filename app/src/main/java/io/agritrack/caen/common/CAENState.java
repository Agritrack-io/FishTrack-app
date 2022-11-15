package io.agritrack.caen.common;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import com.google.android.gms.common.util.Strings;
import com.uhf.api.cls.Reader;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class CAENState implements Serializable {

    private static final String TAG = "CAENState";
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    // keeps the OP result, to decide whether to proceed to next step or not.
    public String path = "";
    public Boolean canProceed = true;

    //--------------------------------------
    //-- 1: True, 0: False, null: Failure --
    //--------------------------------------
    private Integer opReset = null;
    private Integer opLogging = null;
    private Integer opHighPower = null;
    private Integer opHighSensitivity = null;
    private Integer opWriteCurrentTS = null;
    private Integer opWriteBinZero = null;
    private Integer opWriteBinOne = null;
    private Integer opWriteInterval = null;

    //------------------------------
    public String fwRev = null;
    public String hwRev = null;
    public String statusReg = null;
    public String ctrlReg = null;
    public String initDateTime = null;
    public String shippingDate = null;
    public String stopDate = null;

    //------------------------------
    public Short timeBin = null;
    public Short interval = null;
    public Short samplesCnt = null;

    //------------------------------
    public Double lastSample = null;

    //------------------------------
    public List<String[]> samples = null;

    public CAENState() {}

    public CAENState forHighPower(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.opHighPower = Reader.READER_ERR.MT_OK_ERR.equals(rs) ? 1 : 0;
        return this;
    }

    public CAENState forLowPower(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.opHighPower = Reader.READER_ERR.MT_OK_ERR.equals(rs) ? 1 : 0;
        return this;
    }

    public CAENState forHighSensitivity(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.opHighSensitivity = Reader.READER_ERR.MT_OK_ERR.equals(rs) ? 1 : 0;
        return this;
    }

    public CAENState forLowSensitivity(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.opHighSensitivity = Reader.READER_ERR.MT_OK_ERR.equals(rs) ? 1 : 0;
        return this;
    }

    public CAENState forReset(Object val) {
        this.path += ":Reset";
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.opReset = Reader.READER_ERR.MT_OK_ERR.equals(rs) ? 1 : 0;
        return this;
    }

    public CAENState forFWRevision(Object val) {
        String rs = (String) val;
        this.canProceed = !Strings.isEmptyOrWhitespace(rs);
        this.fwRev = rs;
        return this;
    }

    public CAENState forHWRevision(Object val) {
        String rs = (String) val;
        this.canProceed = !Strings.isEmptyOrWhitespace(rs);
        this.hwRev = rs;
        return this;
    }

    public CAENState forSTATUS(Object val) {
        String rs = (String) val;
        this.canProceed = !Strings.isEmptyOrWhitespace(rs);
        this.statusReg = rs;
        return this;
    }

    public CAENState forCTRL(Object val) {
        String rs = (String) val;
        this.canProceed = !Strings.isEmptyOrWhitespace(rs) && !"N/A".equalsIgnoreCase(rs);
        this.ctrlReg = rs;
        return this;
    }

    public CAENState forInitDateTime(Object val) {
        String rs = (String) val;
        this.canProceed = !Strings.isEmptyOrWhitespace(rs);
        this.initDateTime = rs;
        return this;
    }

    public CAENState forSamples(Object val) {
        List<String[]> rs = (List<String[]>) val;
        this.canProceed = rs != null;
        this.samples = rs;
        return this;
    }

    public CAENState forLastSample(Object val) {
        Double rs = (Double) val;
        this.canProceed = rs != null;
        this.lastSample = rs;
        return this;
    }

    public CAENState forTimeBin(Object val) {
        Short rs = (Short) val;
        this.canProceed = rs != null;
        this.timeBin = rs;
        return this;
    }

    public CAENState forInterval(Object val) {
        Short rs = (Short) val;
        this.canProceed = rs != null;
        this.interval = rs;
        return this;
    }

    public CAENState forSamplesCount(Object val) {
        Short rs = (Short) val;
        this.canProceed = rs != null;
        this.samplesCnt = rs;
        return this;
    }

    public CAENState forEnableLogging(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.opLogging = this.canProceed ? 1 : null;
        return this;
    }

    public CAENState forDisableLogging(Object val) {
        this.path += ":DisableLogging";
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.opLogging = this.canProceed ? 0 : null;
        return this;
    }

    public CAENState writeCurrentDatetime(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.opWriteCurrentTS = !Reader.READER_ERR.MT_OK_ERR.equals(rs) ? 0 : 1;
        return this;
    }

    public CAENState writeTimeBinZERO(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.opWriteBinZero = !Reader.READER_ERR.MT_OK_ERR.equals(rs) ? 0 : 1;
        return this;
    }

    public CAENState writeTimeBinONE(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.opWriteBinOne = !Reader.READER_ERR.MT_OK_ERR.equals(rs) ? 0 : 1;
        return this;
    }

    public CAENState writeInterval(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.opWriteInterval = !Reader.READER_ERR.MT_OK_ERR.equals(rs) ? 0 : 1;
        return this;
    }

    public int getInterval() {
        return this.interval != null ? this.interval.intValue() : 30 * 60;
    }

    public Long getInitTS() {
        long epoch = System.currentTimeMillis();
        try {
            if (!Strings.isEmptyOrWhitespace(this.initDateTime) && this.initDateTime.indexOf("1970") < 0) {
                epoch = df.parse(this.initDateTime).getTime();
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return epoch;
    }

    public Integer getOpReset() {
        return opReset;
    }

    public Integer getOpLogging() {
        return opLogging;
    }

    public Short getSamplesCnt() {
        return samplesCnt;
    }

    public List<String[]> getSamples() {
        return samples;
    }

    @Override
    public String toString() {
        return "CAENState{" +
                "canProceed=" + canProceed +
                ", logging=" + opLogging +
                ", fwRev='" + fwRev + '\'' +
                ", hwRev='" + hwRev + '\'' +
                ", statusReg='" + statusReg + '\'' +
                ", ctrlReg='" + ctrlReg + '\'' +
                ", initDateTime='" + initDateTime + '\'' +
                ", shippingDate='" + shippingDate + '\'' +
                ", stopDate='" + stopDate + '\'' +
                ", timeBin=" + timeBin +
                ", interval=" + interval +
                ", samplesCnt=" + samplesCnt +
                ", lastSample=" + lastSample +
                '}';
    }
}
