package io.agritrack.philosofish.caen.api;

import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.CmdDisableLogging;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.CmdEnableLogging;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.CmdRESET;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.InitSΤΑΤΕ;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ReadCTRLReg;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ReadFWRevision;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ReadHWRevision;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ReadInitTimeStamp;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ReadInterval;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ReadLastSample;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ReadSTATUSReg;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ReadSamples;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ReadSamplesCnt;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ReadSΤΑΤΕ;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ReadTimeBIN;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ResetSΤΑΤΕ;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.ValidSΤΑΤΕ;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.WriteBinEnaSampleStore;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.WriteBinEnaTimeStore;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.WriteBinEnableCounter;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.WriteHLimitBINZero;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.WriteInterval;
import static io.agritrack.philosofish.caen.api.CAEN_CONSTANTS.WriteTimeBINZero;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.uhf.api.cls.Reader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.agritrack.philosofish.caen.common.CAENState;

public class CAENLoggerService {
    private final ExecutorService cmdPool;
    private final Handler mHandler;
    private final ICAEN_API cmd;
    private final Long pickedAt;
    private Boolean sendMessagesToHandler = Boolean.FALSE;

    public CAENLoggerService(ICAEN_API logger, Handler mScanHandler, Boolean sendMessages) {
        this(logger, mScanHandler);
        this.sendMessagesToHandler = sendMessages;
    }

    public CAENLoggerService(ICAEN_API logger, Handler mScanHandler) {
        cmdPool = Executors.newCachedThreadPool();
        mHandler = mScanHandler;
        cmd = logger;
        this.pickedAt = 0L;
    }

    public CAENLoggerService(ICAEN_API logger, Handler mScanHandler, Long pickedAt) {
        cmdPool = Executors.newCachedThreadPool();
        mHandler = mScanHandler;
        cmd = logger;
        this.pickedAt = pickedAt;
    }

    public void setEPCFilter(String EPC) {
        if (this.cmd != null) {
            this.cmd.setFilterEPC(EPC);
        }
    }

    /*method that has a sequence of commands to safely and successfully reset the logger
     *each 'exec*' method is used to read from or write to registers of the caen data logger
     *
     */
    public void doResetLogger() {
        try {
            // instantiate the thread pool required by CompletableFuture instances following...
            final ExecutorService actnPool = Executors.newSingleThreadExecutor();


            ///BX6100Commander reader = new BX6100Commander();
            //.LowPowerLevel();

            // begin by Resetting logger (it simultaneously stops Logging too).
            CompletableFuture<CAENState> future = execReset(new CAENState(), actnPool);

            // wait for reset to complete
            this.park2Second();

            CAENState _state = future.get();

            //send reset command again if first time didnt work and tag returned 'NACK'
            if (!canProceed(_state)) {
                future = execReset(new CAENState(), actnPool);
                this.park2Second();
            }
            _state = future.get();
            //future.thenCompose(x -> park3Second(x, actnPool));

            // reads the current state of CTRL register.
            future = this.execReadControlRegister(_state, actnPool);
            _state = future.get();

            int i = 0;

            //check if ctrlReg is '00000', if not, wait for 2 seconds and read again
            while(_state.ctrlReg != null && _state.ctrlReg.endsWith("1") && i < 2) {
                this.park2Second();
                future = this.execReadControlRegister(_state, actnPool);
                _state = future.get();
                i++;
            }

//            if (_state.ctrlReg != null && _state.ctrlReg.endsWith("1")) {
//                future = this.park4Second(_state, actnPool);
//                _state = future.get();
//                future = this.execReadControlRegister(_state, actnPool);
//                _state = future.get();
//            }

            // temporary...
            //CAENState _state = future.join();
            System.out.println("doResetLogger()-->" + _state);

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(ResetSΤΑΤΕ, _state.getOpReset()));
                //reader.HighPowerLevel();
            } else {
                mHandler.sendMessage(createMessage(ResetSΤΑΤΕ, _state));
                // reader.HighPowerLevel();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*method that has a sequence of commands to make sure loggger was initialised properly
     *each 'exec*' method is used to read from or write to registers of the caen data logger
     *
     */
    public void doValidation() {
        try {
            // instantiate the thread pool required by CompletableFuture instances following...
            final ExecutorService actnPool = Executors.newSingleThreadExecutor();

            // read time Bin
            CompletableFuture<CAENState> future = this.execReadTimeBIN(new CAENState(), actnPool);

            // read Init time stamp
            future.thenCompose(x -> execReadInitDatetime(x, actnPool));

            // read CTRL register
            future.thenCompose(x -> execReadControlRegister(x, actnPool));

            // temporary...
            CAENState _state = future.join();
            System.out.println("doValidation()-->" + _state);

            mHandler.sendMessage(createMessage(ValidSΤΑΤΕ, _state));
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


            //future.thenCompose(x -> enableHighSensitivity(x,actnPool));
            future.thenCompose(x -> execWriteHLimitZERO(x, actnPool));

            future.thenCompose(x -> execWriteBinEnableCounter(x, actnPool));

            future.thenCompose(x -> execWriteBinEnaSampleStore(x, actnPool));

            future.thenCompose(x -> execWriteBinEnaTimeStore(x, actnPool));

            // set time interval to given value
            future.thenCompose(x -> execWriteInterval(samplingInterval, x, actnPool));

            // set Init time stamp
            future.thenCompose(x -> execWriteCurrentTimeStamp(x, actnPool));

            // enable logger
            future.thenCompose(x -> execEnableLogging(x, actnPool));

            // temporary...
            CAENState _state = future.join();
            System.out.println("doEnableLogger()-->" + _state);

            // reads the current state of CTRL register.
            future = this.execReadControlRegister(_state, actnPool);
            _state = future.get();

            if (_state.ctrlReg != null && !_state.ctrlReg.endsWith("100")) {
                this.park2Second();
                _state = future.get();
                future = this.execReadControlRegister(_state, actnPool);
                _state = future.get();
            }

            // read Last Sample value
            CompletableFuture<CAENState> futureTemp = this.execReadLastSample(_state, actnPool);

            // read Init time stamp
            futureTemp.thenCompose(x -> execReadInitDatetime(x, actnPool));

            _state = futureTemp.join();
            //--------------------------------------------------

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(ReadLastSample, _state.lastSample));
            } else {
                mHandler.sendMessage(createMessage(InitSΤΑΤΕ, _state));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CompletableFuture<CAENState> execWriteHLimitZERO(CAENState previousState, ExecutorService actnPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.writeHLimitZERO(cmd.writeHLimitZERO()), actnPool);
            CAENState _state = _future.exceptionally(x -> null).get();

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(WriteHLimitBINZero, _state.interval));
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execWriteBinEnableCounter(CAENState previousState, ExecutorService actnPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.writeBinEnableCounter(cmd.writeBinEnableCounter()), actnPool);
            CAENState _state = _future.exceptionally(x -> null).get();

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(WriteBinEnableCounter, _state.interval));
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execWriteBinEnaSampleStore(CAENState previousState, ExecutorService actnPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.writeBinEnaSampleStore(cmd.writeBinEnaSampleStore()), actnPool);
            CAENState _state = _future.exceptionally(x -> null).get();

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(WriteBinEnaSampleStore, _state.interval));
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    private CompletableFuture<CAENState> execWriteBinEnaTimeStore(CAENState previousState, ExecutorService actnPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            _future = CompletableFuture.supplyAsync(() -> previousState.writeBinEnaTimeStore(cmd.writeBinEnaTimeStore()), actnPool);
            CAENState _state = _future.exceptionally(x -> null).get();

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(WriteBinEnaTimeStore, _state.interval));
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
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
            System.out.println("doReadFullLoggerState()-->" + result);
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

            // read the current state of CTRL register.
            future.thenCompose(x -> execReadControlRegister(x, actnPool));

            // temporary...
            CAENState result = future.join();
            System.out.println("doStopLogging()-->" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doReadSamples(int samplesCount) {
        try {
            // instantiate the thread pool required by CompletableFuture instances following...
            final ExecutorService actnPool = Executors.newFixedThreadPool(1);

            // read current Interval between measurements,
            CompletableFuture<CAENState> futureInterval = this.execReadInterval(new CAENState(), actnPool);
            CAENState stateInterval = futureInterval.get();

            // read current Initialization DateTime,
            CompletableFuture<CAENState> futureInitTS = this.execReadInitDatetime(new CAENState(), actnPool);
            CAENState stateTS = futureInitTS.get();

            // read the stored Temperature measurements,
            CompletableFuture<CAENState> future = null;
            if (stateInterval != null && stateTS != null) {
                future = this.execReadSamples(samplesCount, stateInterval.getPickedAt(), stateInterval.getInterval(), stateTS.getInitTS(), new CAENState(), actnPool);
            } else if (stateInterval != null && stateTS == null) {
                future = this.execReadSamples(samplesCount, stateInterval.getInterval(), new CAENState(), actnPool);
            } else if (stateInterval == null && stateTS == null) {
                future = this.execReadSamples(samplesCount, new CAENState(), actnPool);
            }

            if (future != null) {
                // temporary...
                CAENState result = future.join();
                System.out.println("doReadSamples()-->" + result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doReadMeasurements(Boolean forceRead) {
        try {
            // instantiate the thread pool required by CompletableFuture instances following...
            final ExecutorService actnPool = Executors.newFixedThreadPool(1);

//           B X6100Commander reader = new BX6100Commander();
//            reader.LowPowerLevel();

            CAENState status = execReadControlRegister(new CAENState(), actnPool).get();
            status.setPickedAt(this.pickedAt);

            if (Boolean.FALSE.equals(forceRead) && (status.ctrlReg == null || status.ctrlReg.equalsIgnoreCase("N/A"))) {
                System.out.println("doReadMeasurements()-->" + status);
                status = execReadSamplesCount(status, actnPool).get();
                mHandler.sendMessage(createMessage(ReadSΤΑΤΕ, status));
                return;
            }

            // read current Interval between measurements,
            CompletableFuture<CAENState> _future = this.execReadInterval(new CAENState(), actnPool);
            _future.thenCompose(x -> enableHighSensitivity(x, actnPool));
            _future.thenCompose(x -> execDisableLogging(x, actnPool));
            _future.thenCompose(x -> execReadInitDatetime(x, actnPool));
            _future.thenCompose(x -> execReadSamplesCount(x, actnPool));
            CAENState _state = _future.join();

            Integer stateInterval = _state.interval != null ? Integer.valueOf(_state.interval.intValue()) : null;
            Integer samplesCount = _state.samplesCnt != null ? Integer.valueOf(_state.samplesCnt.intValue()) : null;
            Long initTS = _state.getInitTS();

            // read the stored Temperature measurements,
            CompletableFuture<CAENState> future = null;
            if (stateInterval != null && initTS != null) {
                future = this.execReadSamples(samplesCount, pickedAt, stateInterval, initTS, _state, actnPool);
            } else if (stateInterval != null && initTS == null) {
                future = this.execReadSamples(samplesCount, stateInterval, _state, actnPool);
            } else if (stateInterval == null && initTS == null) {
                future = this.execReadSamples(samplesCount, _state, actnPool);
            }

            if (future != null) {
                // temporary...
                CAENState result = future.join();
//                reader.HighPowerLevel();
                System.out.println("doReadMeasurements()-->" + result);
                mHandler.sendMessage(createMessage(ReadSΤΑΤΕ, _state));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CAENState doReadLastTemperature() {
        try {
            // instantiate the thread pool required by CompletableFuture instances following...
            final ExecutorService actnPool = Executors.newScheduledThreadPool(1);

            // read the FWRevision flag,
            CompletableFuture<CAENState> future = this.execReadLastSample(new CAENState(), actnPool);

            // temporary...
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //########################################################
    public void shutdownExecutorService() {
        cmdPool.shutdown();
        if (!cmdPool.isShutdown()) {
            cmdPool.shutdownNow();
        }
    }

    //--- private methods ----------------------------------------
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

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(CmdDisableLogging, _state.getOpLogging()));
            }
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

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(CmdEnableLogging, _state.getOpLogging()));
            }
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

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(CmdRESET, _state.getOpReset()));
            }
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

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(WriteTimeBINZero, _state.timeBin));
            }
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

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(WriteInterval, _state.interval));
            }
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

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(ReadSTATUSReg, _state.statusReg));
            }
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

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(ReadCTRLReg, _state.ctrlReg));
            }
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

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(ReadFWRevision, _state.fwRev));
            }
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

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(ReadHWRevision, _state.hwRev));
            }
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
            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(ReadLastSample, _state.lastSample));
            }
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

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(ReadTimeBIN, _state.timeBin));
            }
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

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(ReadInitTimeStamp, _state.initDateTime));
            }
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

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(ReadInterval, _state.interval));
            }
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

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(ReadSamplesCnt, _state.samplesCnt));
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }

    // the following args may also be used:: int samplesCnt, int intervalSeconds, long startTSmSec
    private CompletableFuture<CAENState> execReadSamples(Integer samplesCnt, CAENState previousState, ExecutorService threadPool) {
        return execReadSamples(samplesCnt, null, null, null, previousState, threadPool);
    }

    private CompletableFuture<CAENState> execReadSamples(Integer samplesCnt, Integer intervalSeconds, CAENState previousState, ExecutorService threadPool) {
        return execReadSamples(samplesCnt, null, intervalSeconds, null, previousState, threadPool);
    }

    private CompletableFuture<CAENState> execReadSamples(Integer samplesCnt, Long pickedAt, Integer intervalSeconds, CAENState previousState, ExecutorService threadPool) {
        return execReadSamples(samplesCnt, pickedAt, intervalSeconds, null, previousState, threadPool);
    }

    private CompletableFuture<CAENState> execReadSamples(Integer samplesCnt, Long pickedAt, Integer intervalSeconds, Long startTSmSec, CAENState previousState, ExecutorService threadPool) {
        CompletableFuture<CAENState> _future = CompletableFuture.completedFuture(previousState);
        try {
            if (!canProceed(previousState)) {
                return _future;
            }

            if (intervalSeconds == null && startTSmSec == null) {
                _future = CompletableFuture.supplyAsync(() -> {
                    try {
                        previousState.forSamples(cmd.ReadSamples(samplesCnt));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return previousState;
                }, threadPool);
            } else if (startTSmSec == null) {
                _future = CompletableFuture.supplyAsync(() -> {
                    try {
                        previousState.forSamples(cmd.ReadSamples(samplesCnt, intervalSeconds));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return previousState;
                }, threadPool);
            } else {
                _future = CompletableFuture.supplyAsync(() -> {
                    try {
                        previousState.forSamples(cmd.ReadSamples(samplesCnt, intervalSeconds, startTSmSec, pickedAt));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return previousState;
                }, threadPool);
            }

            CAENState _state = _future.exceptionally(x -> null).get();

            if (sendMessagesToHandler) {
                mHandler.sendMessage(createMessage(ReadSamples, _state.samples));
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return _future;
    }


    // sleep for 4.0 second before resume flow.
    private void park2Second() {
        try {
//            CompletableFuture.supplyAsync(() -> {
//                try {
            Thread.sleep(2*1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }, threadPool).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //return CompletableFuture.completedFuture(previousState);
    }

    //
    private Message createMessage(int what, Object value) {
        Message msg = new Message();
        msg.what = what;
        Bundle b = new Bundle();

        if (value != null) {
            if (value instanceof CAENState)
                b.putSerializable("body", (CAENState) value);
            else if (value instanceof String)
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


