package io.agritrack.caen.api;

import com.uhf.api.cls.Reader;

import java.util.List;

import cn.pda.serialport.Tools;

public interface ICAEN_API {
    public static final Short DefaultInterval = (short) (30);
    public static final Short SampleBatchSize = 30;
    public final byte[] accessPassword = Tools.HexString2Bytes("00000000");


    public void setFilterEPC(String epc);

    // ########################
    // ###  public methods  ###
    // ########################
    public Double Init() throws Exception;

    public Double Init(short interval) throws Exception;

    /* This function RESETS the logger */
    public Reader.READER_ERR Reset();

    /* This function sets the interval, timeBin */
    public Reader.READER_ERR Setup(short interval);

    /* This function triggers the logger to start logging */
    public Double StartLogging() throws Exception;

    /* This function triggers the tag parsing and execution of a command */
    public byte[] Trigger();

    /* This function checks the reply value, recommended to call it after a trigger command */
    public byte CheckReply();

    /* This function enables TimeBin-One */
    public Reader.READER_ERR WriteTimeBinONE();

    /* This function sets the current epoch timestamp */
    public Reader.READER_ERR WriteCurrentDatetime();

    /* This function sets the sampling interval. */
    public Reader.READER_ERR WriteInterval(Short interval);

    /* This function starts Logging. */
    public Reader.READER_ERR EnableLogging();

    /* This function starts Logging. */
    public Reader.READER_ERR DisableLogging();

    /* This function sets Logger to HIGH sensitivity mode. */
    public Reader.READER_ERR HighSensitivity();

    /* This function sets Logger to LOW sensitivity mode. */
    public Reader.READER_ERR LowSensitivity();

    //##################################################
    //###  Public methods for Read / Write commands  ###
    //##################################################
    /* This function returns in one step, the LastSampleMeasurement and the Samples Count */
    public Object[] ReadSamplesCntAndLastValue();

    /* This function returns the CONTROL register bits value */
    public String ReadControlRegister();

    /* This function returns the STATUS register bits value */
    public String ReadStatusRegister();

    /* This function returns the READ_FW_REVISION value */
    public String ReadFWRevision();

    /* This function returns the READ_HW_REVISION value */
    public String ReadHWRevision();

    /* This function returns the READ_INIT_DATETIME value */
    public String ReadInitDatetime();

    /* This function returns the READ_TIME_BIN value */
    public Short ReadTimeBIN();

    /* This function returns the READ_INTERVAL value */
    public Short ReadInterval();

    /* This function returns the READ_SAMPLES_COUNT value */
    public Double ReadLastSample();

    /* This function returns the READ_SAMPLES_COUNT value */
    public Short ReadSamplesCount();

    /* This function returns first 'samplesCnt' temperature measurements */
    public List<String[]> ReadSamples(int samplesCnt) throws Exception;

    //public List<Double[]> ReadNumericSamples(int samplesCnt) throws Exception;

    public void CloseReader();

    public void Status(boolean open);

    public boolean IsOpen();
}
