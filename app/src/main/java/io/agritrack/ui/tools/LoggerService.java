package io.agritrack.ui.tools;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.agritrack.caen.api.ICAEN_API;

public class LoggerService {
    public static enum Operation {DisableLogging, EnableLogging, CheckReply, HighPower, LowPower, HighSensitivity, Init, ReadFWRevision, ReadHWRevision, ReadTimeBIN, ReadInitDatetime, ReadCTRLReg, ReadInterval, ReadLastSample};

    private final ExecutorService executorService;
    private final ICAEN_API cmd;

    public LoggerService(ICAEN_API logger) {
        executorService = Executors.newSingleThreadExecutor();
        cmd = logger;
    }


    public void executeLoggerCommand(Operation op, int timeoutInSeconds) {

        switch(op) {
            case DisableLogging:
                executeInTimeout(executorService.submit(() -> cmd.DisableLogging()), timeoutInSeconds);
                break;
            case EnableLogging:
                executeInTimeout(executorService.submit(() -> cmd.EnableLogging()), timeoutInSeconds);
                break;
            case CheckReply:
                executeInTimeout(executorService.submit(() -> cmd.CheckReply()), timeoutInSeconds);
                break;
            case HighPower:
                executeInTimeout(executorService.submit(() -> cmd.HighPowerLevel()), timeoutInSeconds);
                break;
            case LowPower:
                executeInTimeout(executorService.submit(() -> cmd.LowPowerLevel()), timeoutInSeconds);
                break;
            case HighSensitivity:
                executeInTimeout(executorService.submit(() -> cmd.HighSensitivity()), timeoutInSeconds);
                break;
            case Init:
                executeInTimeout(executorService.submit(() -> cmd.Init()), timeoutInSeconds);
                break;
            case ReadFWRevision:
                executeInTimeout(executorService.submit(() -> cmd.ReadFWRevision()), timeoutInSeconds);
                break;
            case ReadHWRevision:
                executeInTimeout(executorService.submit(() -> cmd.ReadHWRevision()), timeoutInSeconds);
                break;
            case ReadTimeBIN:
                executeInTimeout(executorService.submit(() -> cmd.ReadTimeBIN()), timeoutInSeconds);
                break;
            case ReadInitDatetime:
                executeInTimeout(executorService.submit(() -> cmd.ReadInitDatetime()), timeoutInSeconds);
                break;
            case ReadCTRLReg:
                executeInTimeout(executorService.submit(() -> cmd.ReadControlRegister()), timeoutInSeconds);
                break;
            case ReadInterval:
                executeInTimeout(executorService.submit(() -> cmd.ReadInterval()), timeoutInSeconds);
                break;
            case ReadLastSample:
                executeInTimeout(executorService.submit(() -> cmd.ReadLastSample()), timeoutInSeconds);
                break;
        }
        System.out.println(String.format("Executed op:%s, continue main thread!", op.name()));
        shutdownExecutorService();
    }

    private Object executeInTimeout(Future<?> futureTask, int timeoutInSeconds) {
        try {
            return futureTask.get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        } catch (TimeoutException timeoutException) {
            futureTask.cancel(true);
        }
        return null;
    }

    private void shutdownExecutorService() {
        executorService.shutdown();
        if(!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

}
