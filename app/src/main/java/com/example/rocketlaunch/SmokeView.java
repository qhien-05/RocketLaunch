package com.example.rocketlaunch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SmokeView extends View {

    private static class Particle {

        float x;
        float y;

        float speedX;
        float speedY;

        float radius;
        float growth;

        float alpha;

        long lifeTime;
        long age;

        int grayColor;
    }


    private final List<Particle> particles =
            new ArrayList<>();

    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Random random =
            new Random();


    private float emitterX = 0f;
    private float emitterY = 0f;

    private boolean emitting = false;

    private long lastFrameTime = 0;
    private long lastSpawnTime = 0;


    public SmokeView(Context context) {
        super(context);
        initialize();
    }


    public SmokeView(
            Context context,
            AttributeSet attrs) {

        super(context, attrs);

        initialize();
    }


    public SmokeView(
            Context context,
            AttributeSet attrs,
            int defStyleAttr) {

        super(
                context,
                attrs,
                defStyleAttr
        );

        initialize();
    }


    private void initialize() {

        /*
         * SmokeView itself has no background.
         * Only particles are drawn.
         */
        setWillNotDraw(false);
    }


    // =====================================================
    // SET POSITION OF ROCKET ENGINE
    // =====================================================

    public void setEmitter(
            float x,
            float y) {

        emitterX = x;
        emitterY = y;
    }


    // =====================================================
    // START SMOKE
    // =====================================================

    public void startSmoke() {

        emitting = true;

        lastFrameTime =
                SystemClock.uptimeMillis();

        lastSpawnTime = 0;

        removeCallbacks(
                animationRunnable
        );

        post(
                animationRunnable
        );
    }


    // =====================================================
    // STOP CREATING NEW SMOKE
    // Existing smoke continues fading away.
    // =====================================================

    public void stopSmoke() {

        emitting = false;

        if (!particles.isEmpty()) {

            removeCallbacks(
                    animationRunnable
            );

            lastFrameTime =
                    SystemClock.uptimeMillis();

            post(
                    animationRunnable
            );
        }
    }


    // =====================================================
    // DELETE ALL SMOKE
    // =====================================================

    public void clearSmoke() {

        emitting = false;

        removeCallbacks(
                animationRunnable
        );

        particles.clear();

        invalidate();
    }


    // =====================================================
    // CREATE PARTICLES
    // =====================================================

    private void createSmokeParticle() {

        /*
         * Generate multiple particles at once
         * so the exhaust looks thick.
         */
        int amount =
                2 + random.nextInt(2);

        for (int i = 0;
             i < amount;
             i++) {

            Particle particle =
                    new Particle();


            /*
             * Start directly underneath
             * the rocket engine.
             */
            particle.x =
                    emitterX
                            + randomRange(
                            -9f,
                            9f
                    );

            particle.y =
                    emitterY
                            + randomRange(
                            -2f,
                            8f
                    );


            /*
             * Smoke spreads sideways.
             */
            particle.speedX =
                    randomRange(
                            -1.5f,
                            1.5f
                    );


            /*
             * Positive Y means smoke
             * slowly falls downward.
             */
            particle.speedY =
                    randomRange(
                            1.0f,
                            2.2f
                    );


            /*
             * Different particle sizes
             * make the smoke look natural.
             */
            particle.radius =
                    randomRange(
                            dpToPx(5),
                            dpToPx(10)
                    );


            particle.growth =
                    randomRange(
                            dpToPx(0.15f),
                            dpToPx(0.40f)
                    );


            particle.alpha =
                    randomRange(
                            0.45f,
                            0.75f
                    );


            particle.lifeTime =
                    900
                            + random.nextInt(
                            650
                    );


            particle.age = 0;


            /*
             * Different shades:
             * white / light grey / grey.
             */
            particle.grayColor =
                    195
                            + random.nextInt(
                            55
                    );


            particles.add(
                    particle
            );
        }
    }


    // =====================================================
    // PARTICLE ANIMATION LOOP
    // =====================================================

    private final Runnable animationRunnable =
            new Runnable() {

                @Override
                public void run() {

                    long currentTime =
                            SystemClock
                                    .uptimeMillis();


                    if (lastFrameTime == 0) {

                        lastFrameTime =
                                currentTime;
                    }


                    /*
                     * Approximately 60 FPS.
                     */
                    float frameMultiplier =
                            Math.min(
                                    (currentTime
                                            - lastFrameTime)
                                            / 16.67f,
                                    3f
                            );


                    lastFrameTime =
                            currentTime;


                    // -------------------------------------
                    // CREATE NEW SMOKE
                    // -------------------------------------

                    if (emitting
                            && currentTime
                            - lastSpawnTime
                            >= 45) {

                        createSmokeParticle();

                        lastSpawnTime =
                                currentTime;
                    }


                    // -------------------------------------
                    // UPDATE ALL PARTICLES
                    // -------------------------------------

                    Iterator<Particle> iterator =
                            particles.iterator();


                    while (iterator.hasNext()) {

                        Particle particle =
                                iterator.next();


                        particle.age +=
                                (long)
                                        (16.67f
                                                * frameMultiplier);


                        /*
                         * Move smoke.
                         */
                        particle.x +=
                                particle.speedX
                                        * frameMultiplier;

                        particle.y +=
                                particle.speedY
                                        * frameMultiplier;


                        /*
                         * Smoke expands.
                         */
                        particle.radius +=
                                particle.growth
                                        * frameMultiplier;


                        /*
                         * Fade out.
                         */
                        float progress =
                                (float)
                                        particle.age
                                        /
                                        particle.lifeTime;


                        if (progress >= 1f) {

                            iterator.remove();

                        } else {

                            particle.alpha =
                                    Math.max(
                                            0f,
                                            0.70f
                                                    * (1f
                                                    - progress)
                                    );
                        }
                    }


                    invalidate();


                    /*
                     * Continue while launching
                     * OR while old smoke remains.
                     */
                    if (emitting
                            || !particles.isEmpty()) {

                        postOnAnimation(
                                this
                        );

                    } else {

                        lastFrameTime = 0;
                    }
                }
            };


    // =====================================================
    // DRAW SMOKE
    // =====================================================

    @Override
    protected void onDraw(
            Canvas canvas) {

        super.onDraw(
                canvas
        );


        for (Particle particle :
                particles) {

            int gray =
                    particle.grayColor;


            paint.setColor(
                    Color.rgb(
                            gray,
                            gray,
                            gray
                    )
            );


            paint.setAlpha(
                    (int)
                            (particle.alpha
                                    * 255)
            );


            canvas.drawCircle(
                    particle.x,
                    particle.y,
                    particle.radius,
                    paint
            );
        }
    }


    // =====================================================
    // RANDOM NUMBER
    // =====================================================

    private float randomRange(
            float min,
            float max) {

        return min
                + random.nextFloat()
                * (max - min);
    }


    // =====================================================
    // DP TO PIXELS
    // =====================================================

    private float dpToPx(
            float dp) {

        return dp
                * getResources()
                .getDisplayMetrics()
                .density;
    }
}