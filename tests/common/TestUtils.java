/*
 * Copyright (c) [2018] [Jonathan McIntosh, Martin Staadecker, Ryan Zazo]
 */

package common;

import common.particles.Particle;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestUtils {
    @Test
    public static void assertPoseEqual(Pose pose1, Pose pose2) {
        Assertions.assertEquals(pose1.getX(), pose2.getX());
        Assertions.assertEquals(pose1.getY(), pose2.getY());
        Assertions.assertEquals(pose1.getHeading(), pose2.getHeading());
    }

    @Test
    public static void assertPointEquals(Point point1, Point point2, float tolerance) {
        Assertions.assertTrue(point2.x - tolerance < point1.x && point1.x < point2.x + tolerance);
        Assertions.assertTrue(point2.y - tolerance < point1.y && point1.y < point2.y + tolerance);
    }


    @Contract(pure = true)
    static boolean sumOfWeightsIsOne(@NotNull Particle[] particles) {
        float totalWeight = 0;

        for (Particle particle : particles) {
            totalWeight += particle.weight;
        }

        return totalWeight > 0.99 && totalWeight < 1.01;
    }
}
