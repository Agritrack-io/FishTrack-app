package io.agritrack.philosofish.caen.common;

import com.google.android.gms.common.util.Strings;
import com.uhf.api.cls.Reader;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import io.agritrack.philosofish.data.model.TempSample;
import io.agritrack.philosofish.ui.tools.caen.ILoggerDialog;

public class CAENState implements Serializable {

    private static final String TAG = "CAENState";
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    // keeps the OP result, to decide whether to proceed to next step or not.
    public String path = "";
    public Boolean canProceed = true;
    public ILoggerDialog.StatesEnum state = null;
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
    public List<TempSample> samples = null;
    private String loggerEPC = null;
    private String assetEPC = null;
    private Long pickedAt;
    private String productionLane = null;
    //--------------------------------------
    //-- 1: True, 0: False, null: Failure --
    //--------------------------------------
    private Integer opReset = null;
    private Integer opLogging = null;
    private Integer opHighPower = null;
    private Integer opHighSensitivity = null;
    private Integer opWriteCurrentTS = null;
    private Integer opWriteBinZero = null;
    private Integer opWriteHLimitZERO = null;
    private Integer opWriteBinEnableCounter = null;
    private Integer opWriteBinOne = null;
    private Integer opWriteInterval = null;

    public CAENState() {
    }

    public CAENState(String loggerTag, String assetTag, String productionLane) {
        this.loggerEPC = loggerTag;
        this.assetEPC = assetTag;
        this.productionLane = productionLane;
    }

    public CAENState forHighPower(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = true;
        this.opHighPower = Reader.READER_ERR.MT_OK_ERR.equals(rs) ? 1 : 0;
        return this;
    }

    public CAENState forLowPower(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.opHighPower = Reader.READER_ERR.MT_OK_ERR.equals(rs) ? 0 : 1;
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
        // the opHighSensitivity is inverse of LowSensitivity..
        this.opHighSensitivity = Reader.READER_ERR.MT_OK_ERR.equals(rs) ? 0 : 1;
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
        List<TempSample> rs = (List<TempSample>) val;
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

    public CAENState writeHLimitZERO(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.opWriteHLimitZERO = !Reader.READER_ERR.MT_OK_ERR.equals(rs) ? 0 : 1;
        return this;
    }

    public CAENState writeBinEnableCounter(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.opWriteBinEnableCounter = !Reader.READER_ERR.MT_OK_ERR.equals(rs) ? 0 : 1;
        return this;
    }

    public CAENState writeBinEnaSampleStore(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.opWriteBinEnableCounter = !Reader.READER_ERR.MT_OK_ERR.equals(rs) ? 0 : 1;
        return this;
    }

    public CAENState writeBinEnaTimeStore(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.opWriteBinEnableCounter = !Reader.READER_ERR.MT_OK_ERR.equals(rs) ? 0 : 1;
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
                System.out.println("getInit returned correctly");
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

    public List<TempSample> getSamples() {
        return samples;
    }

    public String getLoggerEPC() {
        return loggerEPC;
    }

    public void setLoggerEPC(String loggerEPC) {
        this.loggerEPC = loggerEPC;
    }

    public String getAssetEPC() {
        return assetEPC;
    }

    public void setAssetEPC(String assetEPC) {
        this.assetEPC = assetEPC;
    }

    public Long getPickedAt() {
        return pickedAt;
    }

    public void setPickedAt(Long pickedAt) {
        this.pickedAt = pickedAt;
    }

    public String getProductionLane() {
        return productionLane;
    }

    public void setProductionLane(String productionLane) {
        this.productionLane = productionLane;
    }

    public Double getLastSample() {
        return lastSample;
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
