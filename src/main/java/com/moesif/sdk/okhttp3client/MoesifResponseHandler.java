package com.moesif.sdk.okhttp3client;

import com.moesif.api.models.EventModel;
import com.moesif.api.models.EventRequestModel;
import com.moesif.api.models.EventResponseModel;
import com.moesif.helpers.EncodeUtils;
import com.moesif.sdk.okhttp3client.models.OkHttp3EventMapper;
import com.moesif.sdk.okhttp3client.util.JsonSerialize;
import com.moesif.external.facebook.stetho.inspector.network.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class MoesifResponseHandler implements ResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(
                                            MoesifResponseHandler.class);
    private EventRequestModel loggedRequest;
    private EventResponseModel loggedResponse;
    private ByteArrayOutputStream outputStream;
    private Boolean jsonHeader;
    private String moesifApplicationId;
    private static Integer maxAllowedBodySize;

    public MoesifResponseHandler(EventRequestModel loggedRequest,
                                 EventResponseModel loggedResponse,
                                 ByteArrayOutputStream outputStream,
                                 Boolean jsonHeader,
                                 String moesifApplicationId,
                                 Integer maxAllowedBodySize) {
        this.loggedRequest = loggedRequest;
        this.loggedResponse = loggedResponse;
        this.outputStream = outputStream;
        this.jsonHeader = jsonHeader;
        this.moesifApplicationId = moesifApplicationId;
        this.maxAllowedBodySize = maxAllowedBodySize;
    }

    @Override
    public void onRead(int numBytes) {

    }

    @Override
    public void onReadDecoded(int numBytes) {

    }

    @Override
    public void onEOF() {
        sendEvent(loggedRequest,
                loggedResponse,
                outputStream,
                jsonHeader
        );

    }

    @Override
    public void onError(IOException e) {
        logger.warn("Error Decompressing stream: " + e.getMessage());
    }

    private void sendEvent(EventRequestModel loggedRequest,
                           EventResponseModel loggedResponse,
                           ByteArrayOutputStream bodyStream,
                           boolean isJsonHeader) {
        try {
            setBodyAndTransferEncoding(isJsonHeader,
                    loggedResponse,
                    bodyStream,
                    maxAllowedBodySize);
            final EventModel loggedEvent =
                    OkHttp3EventMapper.createOkHttp3Event(
                            loggedRequest,
                            loggedResponse
                    );
            MoesifApiLogEvent.sendEventAsync(moesifApplicationId,
                    loggedEvent);
        } catch (IllegalArgumentException e) {
            logger.warn("Is Moesif Application ID configured?", e.getMessage());
        } catch (Exception e) {
            logger.warn("Error creating or submitting event", e.getMessage());
        } catch (Throwable throwable) {
            logger.warn("Error with throwable", throwable);
        }
    }

    private static void setBodyAndTransferEncoding(
            boolean isJsonHeader,
            EventResponseModel loggedResponse,
            ByteArrayOutputStream bodyStream,
            Integer maxAllowedBodySize)
            throws IOException {
        if (isJsonHeader) {
            loggedResponse.setBody(
                    JsonSerialize.jsonBAOutStreamToObj(bodyStream));
        } else {
            if (bodyStream.size() <= maxAllowedBodySize) {
                loggedResponse.setBody(EncodeUtils.BaosToB64Str(bodyStream));
                loggedResponse.setTransferEncoding("base64");
            }
        }
    }
}
