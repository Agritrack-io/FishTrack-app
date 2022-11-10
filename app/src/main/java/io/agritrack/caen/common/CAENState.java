package io.agritrack.caen.common;

import com.google.android.gms.common.util.Strings;
import com.uhf.api.cls.Reader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class CAENState {
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public boolean canProceed = true;

    private boolean highPower = false;
    private boolean highSensitivity = false;
    public boolean reset = false;
    public Integer logging = null;
    public String fwRev = null;
    public String hwRev = null;
    public String statusReg = null;
    public String ctrlReg = null;
    public String initDateTime = null;
    public String shippingDate = null;
    public String stopDate = null;
    public Short timeBin = null;
    public Short interval = null;
    public Short samplesCnt = null;
    public Double lastSample = null;
    public List<String[]> samples = null;
    private boolean writeCurrentTS = false;
    private boolean writeBinZero = false;
    private boolean writeBinOne = false;
    private boolean writeInterval = false;


    public CAENState forHighPower(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.highPower = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        return this;
    }

    public CAENState forLowPower(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.highPower = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        return this;
    }

    public CAENState forHighSensitivity(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.highSensitivity = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        return this;
    }

    public CAENState forLowSensitivity(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.highSensitivity = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        return this;
    }

    public CAENState forReset(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.reset = Reader.READER_ERR.MT_OK_ERR.equals(rs);
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

    public CAENState forCTRLRevisions(Object val) {
        String[] rs = (String[]) val;
        this.canProceed = rs != null && rs.length == 3 && !"N/A".equalsIgnoreCase(rs[0]) && !"N/A".equalsIgnoreCase(rs[1]) && !"N/A".equalsIgnoreCase(rs[2]);
        this.fwRev = rs[0];
        this.hwRev = rs[1];
        this.ctrlReg = rs[2];
        return this;
    }

    public CAENState forSamplesInfo(Object val) {
        String[] rs = (String[]) val;
        this.canProceed = rs != null && rs.length == 3 && !"N/A".equalsIgnoreCase(rs[0]) && !"N/A".equalsIgnoreCase(rs[1]) && !"N/A".equalsIgnoreCase(rs[2]) && !"N/A".equalsIgnoreCase(rs[3]);
        this.lastSample = "N/A".equalsIgnoreCase(rs[0]) ? Double.NaN : Double.valueOf(rs[0]);
        this.samplesCnt = "N/A".equalsIgnoreCase(rs[0]) ? -1 : Short.valueOf(rs[1]);
        this.shippingDate = rs[2];
        this.initDateTime = rs[2];
        this.stopDate = rs[3];
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

    public CAENState forShippingDate(Object val) {
        String rs = (String) val;
        this.canProceed = !Strings.isEmptyOrWhitespace(rs);
        this.shippingDate = rs;
        return this;
    }

    public CAENState forStopDate(Object val) {
        String rs = (String) val;
        this.canProceed = !Strings.isEmptyOrWhitespace(rs);
        this.stopDate = rs;
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
        this.logging = this.canProceed ? 1 : null;
        return this;
    }

    public CAENState forDisableLogging(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.logging = this.canProceed ? 0 : null;
        return this;
    }

    public CAENState writeCurrentDatetime(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.writeCurrentTS = !Reader.READER_ERR.MT_OK_ERR.equals(rs);
        return this;

    }

    public CAENState writeTimeBinZERO(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.writeBinZero = !Reader.READER_ERR.MT_OK_ERR.equals(rs);
        return this;
    }

    public CAENState writeTimeBinONE(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.writeBinOne = !Reader.READER_ERR.MT_OK_ERR.equals(rs);
        return this;
    }

    public CAENState writeInterval(Object val) {
        Reader.READER_ERR rs = (Reader.READER_ERR) val;
        this.canProceed = Reader.READER_ERR.MT_OK_ERR.equals(rs);
        this.writeInterval = !Reader.READER_ERR.MT_OK_ERR.equals(rs);
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

    @Override
    public String toString() {
        return "CAENState{" +
                "canProceed=" + canProceed +
                ", logging=" + logging +
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
