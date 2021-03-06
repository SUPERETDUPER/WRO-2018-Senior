/*
 * Copyright (c) [2018] [Jonathan McIntosh, Martin Staadecker, Ryan Zazo]
 */

package pc;

import common.TransmittableType;
import common.logger.LogMessage;
import common.logger.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Takes data from an input stream, parses it and then notifies the listener
 */
public class DataReceiver {
    private static final String LOG_TAG = DataReceiver.class.getSimpleName();

    private final DataInputStream dis;
    private final DataReceivedListener listenerToNotify;

    private boolean shouldRun = true;

    public DataReceiver(InputStream inputStream, DataReceivedListener listenerToNotify) {
        this.dis = new DataInputStream(inputStream);
        this.listenerToNotify = listenerToNotify;
    }

    /**
     * Listen for data
     */
    public synchronized void read() {
        try {
            //noinspection InfiniteLoopStatement
            while (shouldRun) {
                TransmittableType dataType = TransmittableType.values()[dis.readByte()];

                if (dataType == TransmittableType.LOG) {
                    LogMessage logMessage = new LogMessage();
                    logMessage.loadObject(dis);
                    logMessage.printToSysOut("From EV3 : ");
                    continue;
                }

                listenerToNotify.dataReceived(dataType, dis);
            }
        } catch (IOException e) {
            Logger.warning(LOG_TAG, "Connection lost. Could not read input stream");
        } finally {
            close();
        }
    }

    public void stop() {
        shouldRun = false;
    }

    /**
     * Close the connection
     */
    private void close() {
        try {
            dis.close();
        } catch (IOException e) {
            Logger.error(LOG_TAG, "Failed closing socket or dis " + e);
        }
    }

    public interface DataReceivedListener {
        /**
         * Called when the data has changed
         *
         * @param event the type of new data
         * @param dis   the data input stream to read from
         * @throws IOException thrown when reading from dataInputStream
         */
        void dataReceived(TransmittableType event, DataInputStream dis) throws IOException;
    }
}