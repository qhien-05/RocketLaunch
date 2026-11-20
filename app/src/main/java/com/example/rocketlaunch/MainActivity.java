package com.example.rocketlaunch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private FrameLayout launchArea;

    private ImageView imgRocket;

    private SmokeView smokeView;

    private TextView txtStatus;

    private Button btnStart;
    private Button btnReset;

    private AnimationDrawable flameAnimation;

    private ObjectAnimator launchAnimator;

    private boolean isLaunching = false;
    private boolean launchCompleted = false;


    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_main
        );


        // =================================================
        // CONNECT VIEWS
        // =================================================

        launchArea =
                findViewById(
                        R.id.launch_area
                );

        imgRocket =
                findViewById(
                        R.id.img_rocket
                );

        smokeView =
                findViewById(
                        R.id.smoke_view
                );

        txtStatus =
                findViewById(
                        R.id.txt_status
                );

        btnStart =
                findViewById(
                        R.id.btn_start
                );

        btnReset =
                findViewById(
                        R.id.btn_reset
                );


        // START
        btnStart.setOnClickListener(
                v -> startRocketLaunch()
        );


        // RESET
        btnReset.setOnClickListener(
                v -> resetRocket()
        );
    }


    // =====================================================
    // START ROCKET
    // =====================================================

    private void startRocketLaunch() {

        if (isLaunching) {
            return;
        }


        /*
         * If a previous launch was completed,
         * return rocket to launch pad first.
         */
        if (launchCompleted) {

            imgRocket.setTranslationY(
                    0f
            );

            smokeView.clearSmoke();

            launchCompleted = false;
        }


        isLaunching = true;


        // Disable START
        btnStart.setEnabled(
                false
        );


        // Status
        txtStatus.setText(
                "Launching..."
        );


        Toast.makeText(
                this,
                "Rocket launch started",
                Toast.LENGTH_SHORT
        ).show();


        // =================================================
        // FLAME ANIMATION / ROCKET IMAGE
        // =================================================

        imgRocket.setImageResource(
                R.drawable.rocket_1
        );


        if (imgRocket.getDrawable() instanceof AnimationDrawable) {

            flameAnimation =
                    (AnimationDrawable)
                            imgRocket.getDrawable();

            flameAnimation.start();

        } else {

            flameAnimation = null;
        }


        // =================================================
        // WAIT FOR SCREEN LAYOUT
        // =================================================

        imgRocket.post(() -> {

            /*
             * Start smoke at rocket engine.
             */
            updateSmokePosition();

            smokeView.startSmoke();


            /*
             * Rocket currently starts near bottom.
             *
             * We want it to travel all the way
             * ABOVE the top edge of the screen.
             *
             * Final rocket bottom:
             * approximately 20dp above screen top.
             */
            float distanceToTop =
                    imgRocket.getTop()
                            + imgRocket.getHeight()
                            + dpToPx(25);


            float targetTranslationY =
                    -distanceToTop;


            // =================================================
            // CREATE ROCKET MOVEMENT
            // =================================================

            launchAnimator =
                    ObjectAnimator.ofFloat(
                            imgRocket,
                            View.TRANSLATION_Y,
                            0f,
                            targetTranslationY
                    );


            /*
             * Slightly longer duration so
             * smoke trail can be seen clearly.
             */
            launchAnimator.setDuration(
                    5000
            );


            /*
             * Rocket accelerates upward.
             */
            launchAnimator.setInterpolator(
                    new AccelerateInterpolator(
                            1.15f
                    )
            );


            // =================================================
            // KEEP SMOKE ATTACHED TO ROCKET ENGINE
            // =================================================

            launchAnimator.addUpdateListener(
                    animation -> {

                        updateSmokePosition();
                    }
            );


            // =================================================
            // END OF LAUNCH
            // =================================================

            launchAnimator.addListener(
                    new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationEnd(
                                Animator animation) {

                            /*
                             * If RESET cancelled animation,
                             * do not display Launch Complete.
                             */
                            if (!isLaunching) {

                                return;
                            }


                            isLaunching = false;

                            launchCompleted = true;


                            // Stop flame
                            if (flameAnimation != null) {

                                flameAnimation.stop();
                            }


                            /*
                             * Stop generating new smoke.
                             *
                             * Existing smoke slowly
                             * disappears naturally.
                             */
                            smokeView.stopSmoke();


                            txtStatus.setText(
                                    "Launch Complete!"
                            );


                            btnStart.setEnabled(
                                    true
                            );


                            Toast.makeText(
                                    MainActivity.this,
                                    "Rocket launch completed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );


            launchAnimator.start();
        });
    }


    // =====================================================
    // UPDATE SMOKE POSITION
    // =====================================================

    private void updateSmokePosition() {

        /*
         * Rocket and SmokeView now have
         * the SAME full-screen parent.
         *
         * Therefore these coordinates
         * match directly.
         */

        float rocketCenterX =
                imgRocket.getX()
                        + imgRocket.getWidth()
                        / 2f;


        /*
         * Generate smoke directly below
         * the rocket engine / flame.
         */
        float rocketEngineY =
                imgRocket.getY()
                        + imgRocket.getHeight()
                        - dpToPx(5);


        smokeView.setEmitter(
                rocketCenterX,
                rocketEngineY
        );
    }


    // =====================================================
    // RESET
    // =====================================================

    private void resetRocket() {

        /*
         * Set false before cancel()
         * because cancel may trigger
         * onAnimationEnd().
         */
        isLaunching = false;

        launchCompleted = false;


        // Stop rocket movement
        if (launchAnimator != null) {

            launchAnimator.cancel();

            launchAnimator = null;
        }


        // Stop flame
        if (flameAnimation != null) {

            flameAnimation.stop();

            flameAnimation = null;
        }


        // Delete all smoke
        smokeView.clearSmoke();


        // Return rocket to launch pad
        imgRocket.setTranslationY(
                0f
        );


        // Initial rocket image
        imgRocket.setImageResource(
                R.drawable.rocket_1
        );


        // Reset status
        txtStatus.setText(
                "Rocket ready to launch!"
        );


        // START available again
        btnStart.setEnabled(
                true
        );


        Toast.makeText(
                this,
                "Rocket reset",
                Toast.LENGTH_SHORT
        ).show();
    }


    // =====================================================
    // DP → PIXELS
    // =====================================================

    private float dpToPx(
            float dp) {

        return dp
                * getResources()
                .getDisplayMetrics()
                .density;
    }


    // =====================================================
    // CLEAN UP
    // =====================================================

    @Override
    protected void onDestroy() {

        if (launchAnimator != null) {

            launchAnimator.cancel();
        }


        if (flameAnimation != null) {

            flameAnimation.stop();
        }


        if (smokeView != null) {

            smokeView.clearSmoke();
        }


        super.onDestroy();
    }
}