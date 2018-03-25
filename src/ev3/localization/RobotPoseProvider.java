/*
 * Copyright (c) [2018] [Jonathan McIntosh, Martin Staadecker, Ryan Zazo]
 */

package ev3.localization;

import common.Config;
import common.Logger;
import common.particles.ParticleAndPoseContainer;
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
 * Singleton pattern
 * Based on odometry pose provider with the extra capability of storing a particle set and using it to refine it's location
 */
public class RobotPoseProvider implements MoveListener, PoseProvider {
    private static final String LOG_TAG = RobotPoseProvider.class.getSimpleName();

    private static final RobotPoseProvider mParticlePoseProvider = new RobotPoseProvider();

    private static final int NUM_PARTICLES = 300;

    private MoveProvider mp;
    private ParticleAndPoseContainer data;

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
     * Doesn't update the data object since we don't want to need to update the particles each time
     *
     * @return the current pose
     */
    @NotNull
    @Override
    public synchronized Pose getPose() {
        Move missingMove = Util.subtractMove(getCurrentCompletedMove(), completedMove);

        return Util.movePose(data.getCurrentPose(), missingMove);
    }

    @Override
    public synchronized void setPose(@NotNull Pose pose) {
        data = new ParticleAndPoseContainer(Util.getNewParticleSet(pose, NUM_PARTICLES), pose);
        completedMove = getCurrentCompletedMove();

        updatePC();
    }

    @Override
    public synchronized void moveStarted(@NotNull Move move, MoveProvider moveProvider) {
        Logger.info(LOG_TAG, "Started move " + move.toString());
    }

    /**
     * Moves the particles and pose over by the amount remaining
     *
     * @param move         the move that was completed
     * @param moveProvider the move provider
     */
    @Override
    public synchronized void moveStopped(@NotNull Move move, MoveProvider moveProvider) {
        Logger.info(LOG_TAG, "Stopped move " + move.toString());

        Move missingMove = Util.subtractMove(Util.deepCopyMove(move), completedMove);

        data.setCurrentPose(Util.movePose(data.getCurrentPose(), missingMove));
        data.setParticles(Util.moveParticleSet(data.getParticles(), missingMove));

        completedMove = null;

        updatePC();
    }

    public synchronized void update(@NotNull Readings readings) {
        Move move = getCurrentCompletedMove();

        Move missingMove = Util.subtractMove(move, completedMove);

        data.setParticles(Util.update(data.getParticles(), missingMove, readings));
        data.setCurrentPose(Util.movePose(data.getCurrentPose(), missingMove));
//        data.setCurrentPose(Util.refineCurrentPose(data.getParticles())); //Updates current pose

        completedMove = move;

        updatePC();
    }

    private void updatePC() {
        DataSender.sendParticleData(data);
    }

    public void sendCurrentPoseToPC() {
        if (Config.currentMode == Config.Mode.DUAL || Config.currentMode == Config.Mode.SIM) {
            DataSender.sendCurrentPose(getPose());
        }
    }

    @NotNull
    private Move getCurrentCompletedMove() {
        return Util.deepCopyMove(mp.getMovement());
    }
}