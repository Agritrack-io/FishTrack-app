package io.agritrack.common.async;

import android.os.Handler;
import android.os.Looper;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Generic class that acts like an AsyncTask but wraps the ExecutorService logic.
 * Used to overcome the deprecation of AsyncTask class...
 * @param <Params>: Type of the input params,
 * @param <Progress>: Type of the progress indicator value,
 * @param <Result>: : Type of the Outcome params
 */
public abstract class AsyncTaskExecutorService<Params, Progress, Result> {

    private final ExecutorService executor;
    private Handler handler;

    protected AsyncTaskExecutorService() {
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public Handler getHandler() {
        if (handler == null) {
            synchronized (AsyncTaskExecutorService.class) {
                handler = new Handler(Looper.getMainLooper());
            }
        }
        return handler;
    }

    protected void onPreExecute() {
        // Override this method whereever you want to perform task before background execution get started
    }

    protected abstract Result doInBackground(Params params);

    protected void onPostExecute(Result result) {
        //throw new RuntimeException("onPostExecute Stub!");
    }

    protected void onProgressUpdate(@NotNull Progress value) {
        // Override this method whereever you want update a progress result
    }

    // used for push progress resport to UI
    public void publishProgress(@NotNull Progress value) {
        getHandler().post(() -> onProgressUpdate(value));
    }

    public void execute() {
        execute(null);
    }

    public void execute(Params params) {
        getHandler().post(() -> {
            onPreExecute();
            executor.execute(() -> {
                Result result = doInBackground(params);
                getHandler().post(() -> onPostExecute(result));
            });
        });
    }

    public void shutDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public boolean isCancelled() {
        return executor == null || executor.isTerminated() || executor.isShutdown();
    }
}
