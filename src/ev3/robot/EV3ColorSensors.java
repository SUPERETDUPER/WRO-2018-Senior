/*
 * Copyright (c) [2018] [Jonathan McIntosh, Martin Staadecker, Ryan Zazo]
 */

package ev3.robot;

import org.jetbrains.annotations.Contract;

/**
 * class allowing access to ev3's color sensors
 */
public final class EV3ColorSensors {
    private final CustomEV3ColorSensor surfaceLeft = new CustomEV3ColorSensor(Ports.SENSOR_COLOR_SURFACE_LEFT);
    private final CustomEV3ColorSensor surfaceRight = new CustomEV3ColorSensor(Ports.SENSOR_COLOR_SURFACE_RIGHT);
    private final CustomEV3ColorSensor boat = new CustomEV3ColorSensor(Ports.SENSOR_COLOR_BOAT);

    void setup() {
        surfaceLeft.setup();
        surfaceRight.setup();
        boat.setup();
    }

    @Contract(pure = true)
    boolean isSetup() {
        return surfaceLeft.isSetup() && surfaceRight.isSetup() && boat.isSetup();
    }

    public float getColorSurfaceLeft() {
        return surfaceLeft.getRed();
    }

    public float getColorSurfaceRight() {
        return surfaceRight.getRed();
    }

    public int getColorBoat() {
        return boat.getColor();
    }
}