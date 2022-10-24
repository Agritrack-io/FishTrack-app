package io.agritrack.caen.api;

import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdDisableLogging;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdEnableLogging;
import static io.agritrack.caen.api.CAEN_CONSTANTS.CmdRESET;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadCTRLReg;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadFWRevision;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadHWRevision;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadInitTimeStamp;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadInterval;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadLastSample;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadSTATUSReg;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadSamples;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadSamplesCnt;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadShippingDate;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadStopDate;
import static io.agritrack.caen.api.CAEN_CONSTANTS.ReadTimeBIN;
import static io.agritrack.caen.api.CAEN_CONSTANTS.WriteInterval;
import static io.agritrack.caen.api.CAEN_CONSTANTS.WriteTimeBINZero;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.uhf.api.cls.Reader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.agritrack.caen.common.CAENState;

public class CAENLoggerService {
    private final ExecutorService cmdPool;
    private final Handler mHandler;
    private final ICAEN_API cmd;

    public CAENLoggerService(ICAEN_API logger, Handler mScanHandler) {
        cmdPool = Executors.newCachedThreadPool();
        mHandler = mScanHandler;
        cmd = logger;
    }

    public void setEPCFilter(String EPC) {
        if (this.cmd != null) {
            this.cmd.setFilterEPC(EPC);
        }
    }

    public void doResetLogger() {
        try {
            // instantiate the thread pool required by CompletableFuture instances following...
            final ExecutorService actnPool = Executors.newSingleThreadExecutor();

            // begin by stop Logging.
            CompletableFuture<CAENState> future = execDisableLogging(new CAENState(), actnPool);

            // reset logger to clear memory.
            future.thenCompose(x -> execReset(x, actnPool));

            // reads the current state of CTRL register.
            future.thenCompose(x -> execReadControlRegister(x, actnPool));

            // temporary...
            CAENState result = future.join();
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doEnableLogger(Short samplingInterval) {
        try {
            // instantiate the thread pool required by CompletableFuture instances following...
            final ExecutorService actnPool = Executors.newSingleThreadExecutor();

            // set time Bin to 0, (disable timestamps)
            CompletableFuture<CAENState> future = this.execWriteTimeBINZero(new CAENState(), actnPool);

            // set time interval to given value
            future.thenCompose(x -> execWriteInterval(samplingInterval, x, actnPool));

            // set Init time stamp
            future.thenCompose(x -> execWriteCurrentTimeStamp(x, actnPool));

            // enable logger
            future.thenCompose(x -> execEnableLogging(x, actnPool));

            // read CTRL register to config RESET is completed.
            future.thenCompose(x -> execReadControlRegister(x, actnPool));

            // read Last Sample value
            future.thenCompose(x -> execReadLastSample(x, actnPool));

            // temporary...
            CAENState result = future.join();
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doReadFullLoggerState() {
        try {
            // instantiate the thread pool required by CompletableFuture instances following...
            final ExecutorService actnPool = Executors.newFixedThreadPool(1);

            // read the FW revision value,
            CompletableFuture<CAENState> future = this.execReadFWRevision(new CAENState(), actnPool);

            // read the HW revision value,
            future.thenCompose(x -> this.execReadHWRevision(x, actnPool));

            // read the CTRL register value,
            future.thenCompose(x -> this.execReadControlRegister(x, actnPool));

            // read the active Time BIN,
            future.thenCompose(x -> this.execReadTimeBIN(x, actnPool));

            // read current Initialization DateTime,
            future.thenCompose(x -> this.execReadInitDatetime(x, actnPool));

            // read the current state of STATUS register,
            future.thenCompose(x -> this.execReadStatusRegister(x, actnPool));

            // read current Sampling Interval (in seconds),
            future.thenCompose(x -> this.execReadInterval(x, actnPool));

            // read the cnt of temperatures logged,
            future.thenCompose(x -> this.execReadSamplesCount(x, actnPool));

            // read the last temperature reading logged,
            future.thenCompose(x -> this.execReadLastSample(x, actnPool));

            // temporary...
            CAENState result = future.join();
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doStopLogging() {
        try {
            // instantiate the thread pool required by CompletableFuture instances following...
            final ExecutorService actnPool = Executors.newScheduledThreadPool(1);

            // read the FWRevision flag,
            CompletableFuture<CAENState> future = this.execDisableLogging(new CAENState(), actnPool);

            // read the: FWRevision flag, HWRevision flag, the current state of CTRL register.
            future.thenCompose(x -> execReadControlRegister(x, actnPool));

            // temporary...
            CAENState result = future.join();
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doReadSamples(int samplesCount) {
        try {
            // instantiate the thread pool required by CompletableFuture instances following...
            final ExecutorService actnPool = Executors.newFixedThreadPool(1);

            // read the active Time BIN,
            //CompletableFuture<CAENState> future = this.execReadTimeBIN(new CAENState(), actnPool);


            // read current Initialization DateTime,
            //future.thenCompose(x -> this.execReadInitDatetime(x, actnPool));

            // read the stored Temperature measurements,
            CompletableFuture<CAENState> future = this.execReadSamples(samplesCount, new CAENState(), actnPool);

            // temporary...
            CAENState result = future.join();
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //########################################################
    public void shutdownExecutorService() {
        cmdPool.shutdown();
        if (!cmdPool.isShutdown()) {
            cmdPool.shutdownNow();
        }
    }

    //--- private methods ----------------------------------------
    private CompletableFuture<CAENState> enableHighPower(CAENState previousState, ExecutorService threadPool) {
        if (!canProceed(previousState)) {
            return CompletableFuture.completedFuture(previousState);
        }
        return CompletableFuture.supplyAsync(() -> previousState.forHighPower(cmd.HighPowerLevel()), threadPool);
    }

    private CompletableFuture<CAENState> enableLowPower(CAENState previousState, ExecutorService threadPool) {
        if (!canProceed(previousState)) {
            return CompletableFuture.completedFuture(previousState);
        }
        return CompletableFuture.supplyAsync(() -> previousState.forLowPower(cmd.HighPowerLevel()), threadPool);
    }

    private CompletableFuture<CAENState> enableHighSensitivity(CAENState previousState, ExecutorService threadPool) {
        if (!canProceed(previousState)) {
            return CompletableFuture.completedFuture(previousState);
        }
        return CompletableFuture.supplyAsync(() -> previousState.forHighPower(cmd.HighSensitivity()), threadPool);
    }

    private CompletableFuture<CAENState> enableLowSensitivity(CAENState previousState, ExecutorService threadPool) {
        if (!canProceed(previousState)) {
            return CompletableFuture.completedFuture(previousState);
        }
        return CompletableFuture.supplyAsync(() -> previousState.forHighSensitivity(cmd.HighSensitivity()), threadPool);
    }

    private CompletableFuture<CAENState> execDisableLogging(CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.forDisableLogging(cmd.DisableLogging()), threadPool);
            CAENState _state = _future.exceptionally(x -> null).get();
            mHandler.sendMessage(createMessage(CmdDisableLogging, _state.logging));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execEnableLogging(CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.forEnableLogging(cmd.EnableLogging()), threadPool);
            CAENState _state = _future.exceptionally(x -> null).get();
            mHandler.sendMessage(createMessage(CmdEnableLogging, _state.logging));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execReset(CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.forReset(cmd.Reset()), threadPool);
            CAENState _state = _future.exceptionally(x -> null).get();
            mHandler.sendMessage(createMessage(CmdRESET, _state.reset));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execWriteCurrentTimeStamp(CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.writeCurrentDatetime(cmd.WriteCurrentDatetime()), threadPool);
            _future.exceptionally(x -> null).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execWriteTimeBINZero(CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.writeTimeBinZERO(cmd.WriteTimeBinZERO()), threadPool);
            CAENState _state = _future.exceptionally(x -> null).get();
            mHandler.sendMessage(createMessage(WriteTimeBINZero, _state.timeBin));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execWriteTimeBINOne(CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.writeTimeBinONE(cmd.WriteTimeBinONE()), threadPool);
            _future.exceptionally(x -> null).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execWriteInterval(Short samplingInterval, CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.writeInterval(cmd.WriteInterval(samplingInterval)), threadPool);
            CAENState _state = _future.exceptionally(x -> null).get();
            mHandler.sendMessage(createMessage(WriteInterval, _state.interval));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    //-----------------------------------
    //Read Methods
    //-----------------------------------
    private CompletableFuture<CAENState> execReadStatusRegister(CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.forSTATUS(cmd.ReadStatusRegister()), threadPool);
            CAENState _state = _future.exceptionally(x -> null).get();
            mHandler.sendMessage(createMessage(ReadSTATUSReg, _state.statusReg));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execReadControlRegister(CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.forCTRL(cmd.ReadControlRegister()), threadPool);
            CAENState _state = _future.exceptionally(x -> null).get();
            mHandler.sendMessage(createMessage(ReadCTRLReg, _state.ctrlReg));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execReadFWRevision(CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.forFWRevision(cmd.ReadFWRevision()), threadPool);
            CAENState _state = _future.exceptionally(x -> null).get();
            mHandler.sendMessage(createMessage(ReadFWRevision, _state.fwRev));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execReadHWRevision(CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.forHWRevision(cmd.ReadHWRevision()), threadPool);
            CAENState _state = _future.exceptionally(x -> null).get();
            mHandler.sendMessage(createMessage(ReadHWRevision, _state.hwRev));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execReadLastSample(CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.forLastSample(cmd.ReadLastSample()), threadPool);
            CAENState _state = _future.exceptionally(x -> null).get();
            mHandler.sendMessage(createMessage(ReadLastSample, _state.lastSample));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execReadTimeBIN(CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.forTimeBin(cmd.ReadTimeBIN()), threadPool);
            CAENState _state = _future.exceptionally(x -> null).get();
            mHandler.sendMessage(createMessage(ReadTimeBIN, _state.timeBin));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execReadInitDatetime(CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.forInitDateTime(cmd.ReadInitDatetime()), threadPool);
            CAENState _state = _future.exceptionally(x -> null).get();
            mHandler.sendMessage(createMessage(ReadInitTimeStamp, _state.initDateTime));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execReadShippingDate(CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.forShippingDate(cmd.ReadShippingDatetime()), threadPool);
            CAENState _state = _future.exceptionally(x -> null).get();
            mHandler.sendMessage(createMessage(ReadShippingDate, _state.shippingDate));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execReadStopDate(CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.forStopDate(cmd.ReadStopDatetime()), threadPool);
            CAENState _state = _future.exceptionally(x -> null).get();
            mHandler.sendMessage(createMessage(ReadStopDate, _state.stopDate));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execReadInterval(CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.forInterval(cmd.ReadInterval()), threadPool);
            CAENState _state = _future.exceptionally(x -> null).get();
            mHandler.sendMessage(createMessage(ReadInterval, _state.interval));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execReadSamplesCount(CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.forSamplesCount(cmd.ReadSamplesCount()), threadPool);
            CAENState _state = _future.exceptionally(x -> null).get();
            mHandler.sendMessage(createMessage(ReadSamplesCnt, _state.samplesCnt));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    // the following args may also be used:: int samplesCnt, int intervalSeconds, long startTSmSec
    private CompletableFuture<CAENState> execReadSamples(int samplesCnt, CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> {
                try { previousState.forSamples(cmd.ReadSamples(samplesCnt));} catch (Exception e) { e.printStackTrace();}
                return previousState;
            }, threadPool);
            CAENState _state = _future.exceptionally(x -> null).get();
            mHandler.sendMessage(createMessage(ReadSamples, _state.samples));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
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
            else if (value instanceof Integer)
                b.putInt("body", (int) value);
            else if (value instanceof Boolean)
                b.putBoolean("body", (boolean) value);
            else if (value instanceof LinkedList)
                b.putParcelableArrayList("body", new ArrayList((LinkedList) value));
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

    private boolean canProceed(CAENState state) {
        return state != null && state.canProceed;
    }
}
