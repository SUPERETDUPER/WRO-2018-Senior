/*
 * Copyright (c) [2018] [Jonathan McIntosh, Martin Staadecker, Ryan Zazo]
 */

package ev3;

import common.Config;
import ev3.hardware.Brick;
import ev3.hardware.ChassisBuilder;
import lejos.robotics.chassis.Chassis;

final class EV3Main {
    private static final String LOG_TAG = EV3Main.class.getSimpleName();

    public static void main(String[] args) {
        if (Config.currentMode == Config.Mode.DUAL || Config.currentMode == Config.Mode.SIM) {
            DataSender.connect(); //Try to connect to pc
        }

        Brain.start();
        Brick.waitForUserConfirmation(); //And wait for complete
    }
}