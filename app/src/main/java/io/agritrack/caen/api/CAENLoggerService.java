package io.agritrack.caen.api;

import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdDisableLogging;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdINIT;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdRESET;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadCTRLReg;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadFWRevision;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadHWRevision;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadInitTimeStamp;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadInterval;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadLastSample;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadSTATUSReg;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadSamplesCnt;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadTimeBIN;
import static io.agritrack.caen.api.CAEN_CONSTANTS.WriteInterval;
import static io.agritrack.caen.api.CAEN_CONSTANTS.WriteTimeBINOne;
import static io.agritrack.caen.api.CAEN_CONSTANTS.WriteTimeBINZero;
import static io.agritrack.caen.api.CAEN_CONSTANTS.WriteTimeStamp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.uhf.api.cls.Reader;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CAENLoggerService {
    private ExecutorService cmdPool;
    private final Handler mHandler;
    private final ICAEN_API cmd;

    public CAENLoggerService(ICAEN_API logger, Handler mScanHandler) {
        cmdPool = Executors.newCachedThreadPool();
        mHandler = mScanHandler;
        cmd = logger;
    }

    public void doResetLogger() {

        //this.HighSensitivity();

        this.Reset();

        this.ReadControlRegister();

        this.ReadSamplesCount();

        this.ReadControlRegister();

    }

    public void doEnableLogger(Short samplingInterval) {

        // set time Bin to 0, (disable timestamps)
        this.WriteTimeBINZero();

        // set time interval to given value
        this.WriteInterval(samplingInterval);

        // set Init time stamp
        this.WriteCurrentTimeStamp();

        // enable logger
        this.InitLogger();

        // read Last Sample value
        this.ReadLastSample();
    }

    public void WriteCurrentTimeStamp() {
        this.mHandler.sendMessage(createMessage(WriteTimeStamp, executeTask(new FutureTask<>(() -> cmd.WriteCurrentDatetime()))));
    }

    public void WriteInterval(Short samplingInterval) {
        if (samplingInterval == null) {
            return;
        }
        this.mHandler.sendMessage(createMessage(WriteInterval, executeTask(new FutureTask<>(() -> cmd.WriteInterval(samplingInterval)))));
    }

    public void WriteTimeBINZero() {
        this.mHandler.sendMessage(createMessage(WriteTimeBINZero, executeTask(new FutureTask<>(() -> cmd.WriteTimeBinZERO()))));
    }

    public void WriteTimeBINOne() {
        this.mHandler.sendMessage(createMessage(WriteTimeBINOne, executeTask(new FutureTask<>(() -> cmd.WriteTimeBinONE()))));
    }

    public void Reset() {
        this.mHandler.sendMessage(createMessage(CmdRESET, executeTask(new FutureTask<>(() -> cmd.Reset()), 500l)));
    }

    public void StopLogging() {
        this.mHandler.sendMessage(createMessage(CmdDisableLogging, executeTask(new FutureTask<>(() -> cmd.DisableLogging()))));
    }

//    public void CheckReply() {
//        this.mHandler.sendMessage(createMessage(CheckReply, executeTask(new FutureTask<>(() -> cmd.CheckReply()))));
//    }
//
//    public void HighPowerLevel() {
//        this.mHandler.sendMessage(createMessage(HighPowerLevel, executeTask(new FutureTask<>(() -> cmd.HighPowerLevel()))));
//    }
//
//    public void LowPowerLevel() {
//        this.mHandler.sendMessage(createMessage(LowPowerLevel, executeTask(new FutureTask<>(() -> cmd.LowPowerLevel()))));
//    }
//
//    public void HighSensitivity() {
//        this.mHandler.sendMessage(createMessage(HighSensitivity, executeTask(new FutureTask<>(() -> cmd.HighSensitivity()))));
//    }
//
//    public void LowPowerLevel() {
//        this.mHandler.sendMessage(createMessage(LowPowerLevel, executeTask(new FutureTask<>(() -> cmd.LowPowerLevel()))));
//    }

    public void InitLogger() {
        this.mHandler.sendMessage(createMessage(CmdINIT, executeTask(new FutureTask<>(() -> cmd.Init()))));
    }

    public void ReadFWRevision() {
        this.mHandler.sendMessage(createMessage(ReadFWRevision, executeTask(new FutureTask<>(() -> cmd.ReadFWRevision()))));
    }

    public void ReadHWRevision() {
        this.mHandler.sendMessage(createMessage(ReadHWRevision, executeTask(new FutureTask<>(() -> cmd.ReadHWRevision()))));
    }

    public void ReadTimeBIN() {
        this.mHandler.sendMessage(createMessage(ReadTimeBIN, executeTask(new FutureTask<>(() -> cmd.ReadTimeBIN()))));
    }

    public void ReadInitDatetime() {
        this.mHandler.sendMessage(createMessage(ReadInitTimeStamp, executeTask(new FutureTask<>(() -> cmd.ReadInitDatetime()))));
    }

    public void ReadControlRegister() {
        this.mHandler.sendMessage(createMessage(ReadCTRLReg, executeTask(new FutureTask<>(() -> cmd.ReadControlRegister()))));
    }

    public void ReadStatusRegister() {
        this.mHandler.sendMessage(createMessage(ReadSTATUSReg, executeTask(new FutureTask<>(() -> cmd.ReadStatusRegister()))));
    }

    public void ReadInterval() {
        this.mHandler.sendMessage(createMessage(ReadInterval, executeTask(new FutureTask<>(() -> cmd.ReadInterval()))));
    }

    public void ReadSamplesCount() {
        this.mHandler.sendMessage(createMessage(ReadSamplesCnt, executeTask(new FutureTask<>(() -> cmd.ReadSamplesCount()), 300l)));
    }

    public void ReadLastSample() {
        this.mHandler.sendMessage(createMessage(ReadLastSample, executeTask(new FutureTask<>(() -> cmd.ReadLastSample()), 300l)));
    }

    private Object executeTask(Future<Object> futureTask) {
        return executeTask(futureTask, 100l);
    }

    private Object executeTask(Future<Object> futureTask, long timeout) {
        try {
            cmdPool.execute((Runnable) futureTask);
            while (true) {
                if (futureTask.isDone()) {
                    //cmdPool.shutdownNow();
                    return futureTask.get(timeout, TimeUnit.MILLISECONDS);
                }
            }
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        } catch (TimeoutException timeoutException) {
            futureTask.cancel(true);
        }
        return null;
    }

    private Message createMessage(int what, Object value) {
        Message msg = new Message();
        msg.what = what;
        Bundle b = new Bundle();

        if (value != null) {
            if (value instanceof String)
                b.putString("body", (String) value);
            else if (value instanceof Short)
                b.putShort("body", (short) value);
            else if (value instanceof Double)
                b.putString("body", String.format("%.2f", value));
            else if (value instanceof Reader.READER_ERR)
                b.putString("body", ((Reader.READER_ERR) value).name());
        } else {
            b.putString("body", "N/A");
        }

        msg.setData(b);

        return msg;
    }
}
