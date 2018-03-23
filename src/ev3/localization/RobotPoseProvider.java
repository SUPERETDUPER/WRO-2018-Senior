/*
 * Copyright (c) [2018] [Jonathan McIntosh, Martin Staadecker, Ryan Zazo]
 */

package ev3.localization;

import common.Config;
import ev3.DataSender;
import ev3.navigation.Readings;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.MoveProvider;
import lejos.robotics.navigation.Pose;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Based on odometry pose provider with the extra capability of storing a particle set and using it to refine it's location
 */
public class RobotPoseProvider implements MoveListener, PoseProvider {
    private static final String LOG_TAG = RobotPoseProvider.class.getSimpleName();

    private static final RobotPoseProvider mParticlePoseProvider = new RobotPoseProvider();

    private MoveProvider mp;
    private MCLData data;

    /**
     * The amount the data has been shifted since the start of this move.
     * completedMove is null when the move starts and each time the data is updated (with update()) the completedMove is updated
     */
    @Nullable
    private Move completedMove;

    private RobotPoseProvider() {
    }

    @NotNull
    public static RobotPoseProvider get() {
        return mParticlePoseProvider;
    }

    public void addMoveProvider(@NotNull MoveProvider moveProvider) {
        this.mp = moveProvider;
        moveProvider.addMoveListener(this);
    }

    /**
     * Doesn't update the data object since we don't want the particles to update every time
     *
     * @return the current pose
     */
    @NotNull
    @Override
    public synchronized Pose getPose() {
        Move missingMove = Util.subtractMove(mp.getMovement(), completedMove);

        return Util.movePose(data.getParticlePose(), missingMove);
    }

    @Override
    public synchronized void setPose(@NotNull Pose pose) {
        data = new MCLData(pose);
        completedMove = deepCopyMove(mp.getMovement());

        updatePC();
    }

    @Override
    public void moveStarted(@NotNull Move move, MoveProvider moveProvider) {
    }

    /**
     * Moves the particles and pose over by the amount remaining
     *
     * @param move         the move that was completed
     * @param moveProvider the move provider
     */
    @Override
    public synchronized void moveStopped(@NotNull Move move, MoveProvider moveProvider) {
        data.moveParticlesAndPose(Util.subtractMove(move, completedMove));

        completedMove = null;

        updatePC();
    }

    public synchronized void update(@NotNull Readings readings) {
        Move move = mp.getMovement();

        data.moveParticlesAndPose(Util.subtractMove(move, completedMove));

        completedMove = deepCopyMove(move); //Deep copy because the movement is modified afterwards

        data.weightParticles(readings); //Recalculate all the particle weights
        data.resample();//Re samples for highest weights
//        data.refineCurrentPose(); //Updates current pose

        updatePC(); //SendToPc
    }

    public void updatePC() {
        if (Config.currentMode == Config.Mode.DUAL || Config.currentMode == Config.Mode.SIM) {
            data.setCurrentPose(getPose());
            DataSender.sendParticleData(data);
        }
    }

    @NotNull
    private static Move deepCopyMove(@NotNull Move move) {
        return new Move(move.getMoveType(), move.getDistanceTraveled(), move.getAngleTurned(), move.getTravelSpeed(), move.getRotateSpeed(), move.isMoving());
    }
}