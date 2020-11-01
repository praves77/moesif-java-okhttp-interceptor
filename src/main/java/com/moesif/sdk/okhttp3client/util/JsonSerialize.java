package com.moesif.sdk.okhttp3client.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * Serializing and deserializing json using Jackson
 */
public class JsonSerialize {
    /**
     * Converts ByteArrayOutputStream to Object.class
     * Uses Jackson
     *
     * @param os ByteArrayOutputSteam
     * @return os -> bytearray -> Object.class
     * @throws IOException
     */
    public static Object jsonBAOutStreamToObj(ByteArrayOutputStream os)
            throws IOException {
        return jsonBAOutStreamToObj(os.toByteArray());
    }

    /**
     * Converts byte array to Object.class using Jackson
     *
     * @param ba byte array
     * @return Object.class
     * @throws IOException
     */
    public static Object jsonBAOutStreamToObj(byte[] ba) throws IOException {
        if (ba.length == 0)
            return null;
        return new ObjectMapper().readValue(ba, Object.class);
    }

}
