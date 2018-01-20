package navigation;

import PC.Displayable;
import com.sun.istack.internal.NotNull;
import geometry.SurfaceMap;
import lejos.robotics.Transmittable;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.Pose;
import utils.Logger;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Inspired by Lawrie Griffiths' MCLParticleSet class in LEJOS EV3 Source code
 */
public class ParticleSet implements Transmittable, Iterable<Particle>, Displayable {

    private static final int numParticles = 200;

    // Static variables
    private static final int maxIterationsForResample = 1000;
    private static final String LOG_TAG = ParticleSet.class.getSimpleName();
    //For moves
    private static final float distanceNoiseFactor = 0.2f;
    private static final float angleNoiseFactor = 0.4f;
    private static final float startingRadiusNoise = 1;
    private static final float startingHeadingNoise = 1;
    private ArrayList<Particle> particles;

    ParticleSet() {
        generateParticleSet();
    }


    public void setInitialPose(@NotNull Pose initialPose) {
        particles = new ArrayList<>(numParticles);

        Random random = new Random();

        for (int i = 0; i < numParticles; i++) {
            float rad = startingRadiusNoise * (float) random.nextGaussian();

            float theta = (float) (2 * Math.PI * Math.random());  //Random angle between 0 and 2pi

            float x = initialPose.getX() + rad * (float) Math.cos(theta);
            float y = initialPose.getY() + rad * (float) Math.sin(theta);

            float heading = initialPose.getHeading() + startingHeadingNoise * (float) random.nextGaussian();
            particles.add(new Particle((new Pose(x, y, heading))));
        }
    }


    @NotNull
    private Particle generateParticle() {
        Point point = SurfaceMap.get().getRandomPoint();

        // Pick a random angle
        float angle = (float) (Math.random() * 360);

        return new Particle(new Pose(point.x, point.y, angle));
    }


    public void resample() {
        ArrayList<Particle> oldParticles = particles;
        particles = new ArrayList<>(numParticles);

        int particlesGenerated = 0;

        for (int iterations = 0; iterations < maxIterationsForResample; iterations++) {
            float rand = (float) Math.random();

            //Copy particles with weight higher than random
            for (Particle particle : oldParticles) {
                if (particle.getWeight() >= rand) {
                    particles.add(particle);
                    particlesGenerated++;
                }

                if (particlesGenerated >= numParticles) {
                    return;
                }
            }
        }


        //If passed max iterations
        if (particlesGenerated == 0) {
            generateParticleSet();
            Logger.warning(LOG_TAG, "Bad resample; regenerated all particles");
            return;
        }

        for (int i = particlesGenerated; i < numParticles; i++) {
            particles.add(new Particle(particles.get(i % particlesGenerated).getPose()));
            particles.get(i).setWeight(particles.get(i % particlesGenerated).getWeight());
        }
        Logger.warning(LOG_TAG, "Bad resample; had to duplicate existing particles");
    }


    /**
     * Calculate the weight for each particle
     */
    public void calculateWeights(@NotNull Readings readings) {
        for (Particle particle : particles) {
            particle.calculateWeight(readings);
        }
    }

    public void applyMove(@NotNull Move move) {
        if (move == null) {
            Logger.warning(LOG_TAG, "Could not apply move because move is null");
            return;
        }

        for (Particle particle : particles) {
            particle.applyMove(move, distanceNoiseFactor, angleNoiseFactor);
        }
    }

    public void displayOnGUI(@NotNull Graphics g) {
        for (Particle particle : particles) {

            if (particle == null) {
                Logger.warning(LOG_TAG, "Could not display particle because is null");
                continue;
            }

            particle.displayOnGUI(g);
        }
    }

    private void generateParticleSet() {
        particles = new ArrayList<>(numParticles);
        for (int i = 0; i < numParticles; i++) {
            particles.add(generateParticle());
        }
    }

    @Override
    @NotNull
    public Iterator<Particle> iterator() {
        return particles.iterator();
    }

    public void dumpObject(@NotNull DataOutputStream dos) throws IOException {
        for (Particle particle : particles) {
            particle.dumpObject(dos);
        }
    }

    public void loadObject(@NotNull DataInputStream dis) throws IOException {
        ArrayList<Particle> newParticles = new ArrayList<>(numParticles);

        for (int i = 0; i < numParticles; ++i) {
            newParticles.add(new Particle(dis));
        }

        this.particles = newParticles;
    }
}