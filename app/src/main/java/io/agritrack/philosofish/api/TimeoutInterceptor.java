package io.agritrack.philosofish.api;

import static java.lang.Integer.parseInt;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TimeoutInterceptor implements Interceptor {

    private static final String CONNECT_TIMEOUT = "CONNECT_TIMEOUT";
    private static final String READ_TIMEOUT = "READ_TIMEOUT";
    private static final String WRITE_TIMEOUT = "WRITE_TIMEOUT";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        int connectTimeout = request.header(CONNECT_TIMEOUT) != null ? parseInt(Objects.requireNonNull(request.header(CONNECT_TIMEOUT))) : chain.connectTimeoutMillis();
        int readTimeout = request.header(READ_TIMEOUT) != null ? parseInt(Objects.requireNonNull(request.header(READ_TIMEOUT))) : chain.readTimeoutMillis();
        int writeTimeout = request.header(WRITE_TIMEOUT) != null ? parseInt(Objects.requireNonNull(request.header(WRITE_TIMEOUT))) : chain.writeTimeoutMillis();

        return chain
                .withConnectTimeout(connectTimeout, MILLISECONDS)
                .withReadTimeout(readTimeout, MILLISECONDS)
                .withWriteTimeout(writeTimeout, MILLISECONDS)
                .proceed(request);
    }
}