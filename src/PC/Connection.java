/*
 * Copyright (c) [2018] [Jonathan McIntosh, Martin Staadecker, Ryan Zazo]
 */

package PC;

import Common.Config;
import Common.Logger;
import lejos.utility.Delay;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Manages the connection to the EV3 and calls a listener when new data arrives
 */
public class Connection {
    private static final String LOG_TAG = Connection.class.getSimpleName();

    private static Socket socket;
    private static DataInputStream dis;

    private volatile static DataChangeListener listener;

    public static void setListener(DataChangeListener listener) {
        Connection.listener = listener;
    }

    /**
     * Connect to EV3
     *
     * @return true if successful
     */
    static boolean connect() {
        for (int attempt = 0; attempt < 6; attempt++) {
            try {
                socket = new Socket(Config.useSimulator ? "localhost" : Config.EV3_IP_ADDRESS, Config.PORT_TO_CONNECT_ON_EV3);
                dis = new DataInputStream(socket.getInputStream());

                Logger.info(LOG_TAG, "Connected to DataSender");

                return true;

            } catch (IOException e) {
                Logger.warning(LOG_TAG, "Failed attempt " + attempt + " to connect to EV3");
                Delay.msDelay(3000);
            }
        }

        return false;
    }

    /**
     * Listen for data
     */
    static void listen() {
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                readNext();
            }
        } catch (IOException e) {
            if (listener != null) {
                listener.connectionLost();
            }
        } finally {
            close();
        }
    }

    /**
     * Close the connection
     */
    public static void close() {
        try {
            socket.close();
            dis.close();
        } catch (IOException e) {
            Logger.error(LOG_TAG, "Failed closing socket or dis" + e);
        }
    }

    private synchronized static void readNext() throws IOException {
        if (listener != null) {
            EventTypes dataType = EventTypes.values()[dis.readByte()];

            listener.dataChanged(dataType, dis);
        }
    }
}