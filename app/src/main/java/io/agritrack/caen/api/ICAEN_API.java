package io.agritrack.caen.api;

import com.uhf.api.cls.Reader;

import java.util.List;

import cn.pda.serialport.Tools;
import io.agritrack.caen.pojo.RFIDTag;

public interface ICAEN_API {

    Short DefaultInterval = (short) 60; //(900); //(1800); //(3600);
    Short SampleBatchSize = 50;
    byte[] accessPassword = Tools.HexString2Bytes("00000000");


    void setFilterEPC(String epc);

    boolean clearEPCFilter();

    // ########################
    // ###  public methods  ###
    // ########################
    /* This function RESETS the logger */
    Reader.READER_ERR Reset();

    /* This function sets the interval, timeBin */
    Reader.READER_ERR Setup(short interval);

    /* This function triggers the logger to start logging */
    Double StartLogging() throws Exception;

    /* This function triggers the tag parsing and execution of a command */
    byte[] Trigger();

    /* This function checks the reply value, recommended to call it after a trigger command */
    byte CheckReply();

    /* This function enables TimeBin-One */
    Reader.READER_ERR WriteTimeBinONE();

    /* This function disables timestamp recording */
    Reader.READER_ERR WriteTimeBinZERO();

    /* This function sets the current epoch timestamp */
    Reader.READER_ERR WriteCurrentDatetime();

    /* This function sets the sampling interval. */
    Reader.READER_ERR WriteInterval(Short interval);

    /* This function starts Logging. */
    Reader.READER_ERR EnableLogging();

    /* This function starts Logging. */
    Reader.READER_ERR DisableLogging();

    /* This function sets Logger to HIGH sensitivity mode. */
    Reader.READER_ERR HighSensitivity();

    /* This function sets Logger to LOW sensitivity mode. */
    Reader.READER_ERR LowSensitivity();

    /* This function sets Logger to HIGH Read Power mode. */
    Reader.READER_ERR HighPowerLevel();

    /* This function sets Logger to LOW Read Power mode. */
    Reader.READER_ERR LowPowerLevel();

    int[] getPowerLevel();

    void Wait(long ms);

    //##################################################
    //###  Public methods for Read / Write commands  ###
    //##################################################
    /* This function returns in one step, the LastSampleMeasurement and the Samples Count */
    Object[] ReadSamplesCntAndLastValue();

    /* This function returns the CONTROL register bits value */
    String ReadControlRegister();

    /* This function returns the STATUS register bits value */
    String ReadStatusRegister();

    /* This function returns the READ_FW_REVISION value */
    String ReadFWRevision();

    /* This function returns the READ_HW_REVISION value */
    String ReadHWRevision();

    /* This function returns the READ_FW_REVISION together with READ_HW_REVISION */
    String[] ReadCTRLRevisions();

    /* This function returns the READ_INIT_DATETIME value */
    String ReadInitDatetime();

    /* This function returns the READ_TIME_BIN value */
    Short ReadTimeBIN();

    /* This function returns the READ_INTERVAL value */
    Short ReadInterval();

    /* This function returns the ADDR_LAST_SAMPLE value */
    Double ReadLastSample();

    /* This function returns the READ_SAMPLES_COUNT value */
    Short ReadSamplesCount();

    /* This function returns the SHIPPING_DATE register value */
    String ReadShippingDatetime();

    /* This function returns the STOP_DATE register value */
    String ReadStopDatetime();

    /* This function returns: LAST_SAMPLE_VALUE, SAMPLES_NUM, SHIPPING_DATE, STOP_DATE */
    String[] ReadSamplesInfo();

    /* This function returns first 'samplesCnt' temperature measurements having an interval of 'DefaultInterval' seconds, starting at 'initedAt' epoch time */
    List<String[]> ReadSamplesWithInitTime(int samplesCnt, long initedAt) throws Exception;

    /* This function returns first 'samplesCnt' temperature measurements */
    List<String[]> ReadSamples(int samplesCnt) throws Exception;

    /* This function returns first 'samplesCnt' temperature measurements having an interval of 'intervalSeconds' seconds */
    List<String[]> ReadSamples(int samplesCnt, int intervalSeconds) throws Exception;

    /* This function returns first 'samplesCnt' temperature measurements having an interval of 'intervalSeconds' seconds, starting at 'startTSmSecQ' epoch time */
    List<String[]> ReadSamples(int samplesCnt, int intervalSeconds, long startTSmSec) throws Exception;


    void CloseReader();

    void StopReading();

    void Status(boolean open);

    boolean IsOpen();

    List<RFIDTag> inventoryRealTime();

    List<RFIDTag> inventoryByTimer();

    List<RFIDTag> searchInventory();

    boolean startReading();

    boolean startSearching();

    List<RFIDTag> search();

    boolean stopSearching();
}
