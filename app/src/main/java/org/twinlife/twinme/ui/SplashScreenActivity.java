/*
 *  Copyright (c) 2019-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.splashscreen.SplashScreenViewProvider;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BaseService;
import org.twinlife.twinme.services.SplashService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.accountMigrationActivity.AccountMigrationActivity;
import org.twinlife.twinme.ui.mainActivity.MainActivity;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumServicesActivity;
import org.twinlife.twinme.ui.welcomeActivity.WelcomeActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

public class SplashScreenActivity extends AbstractTwinmeActivity implements SplashService.Observer {
    private static final String LOG_TAG = "SplashScreenActivity";
    private static final boolean DEBUG = false;

    private static final long MIN_ANIMATION_DURATION = 500;
    private static final long ANIMATION_DURATION = 1000;

    private final List<View> animationList = new ArrayList<>();
    private SplashService mSplashService;
    private AnimatorSet mAnimatorSet;
    private TextView mUpgradeMessage;
    private long mStartTime;
    private boolean mReady = false;
    private boolean mStarted = false;
    private boolean mHasConversations = false;
    private TwinmeApplication.State mState;
    private ScheduledFuture<?> mSplashTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        // Be careful that the Splashscreen library works only with SDK >= 21.
        // Only use it starting from SDK 32 to limit the risks in using it.
        // It also has some weird behavior on SDK < 32
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
            SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

            splashScreen.setOnExitAnimationListener((SplashScreenViewProvider splashScreenView) -> {
                if (DEBUG) {
                    Log.d(LOG_TAG, "Android splashScreen terminated, starting alpha animation");
                }

                // Still a double splash screen effect due to the Android imposed and fixed Splashscreen
                // and our own splash screen which displays application name and bottom logo for Skred.
                // Use some fading to switch between the two.
                View view = splashScreenView.getView();
                PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.0f);
                ObjectAnimator alphaViewAnimator = ObjectAnimator.ofPropertyValuesHolder(view, propertyValuesHolderAlpha);
                alphaViewAnimator.setDuration(500L);

                // Call SplashScreenView.remove at the end of your custom animation.
                alphaViewAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        splashScreenView.remove();
                    }
                });

                alphaViewAnimator.start();
            });
        }

        super.onCreate(savedInstanceState);

        TwinmeApplicationImpl twinmeApplication = TwinmeApplicationImpl.getInstance(this);

        // If there is no TwinmeApplication instance, redirect to the fatal error activity.
        // We avoid a crash later on.
        if (twinmeApplication == null) {
            onFatalError(BaseService.ErrorCode.LIBRARY_ERROR);
            return;
        }

        mState = twinmeApplication.getState();
        if (mState == TwinmeApplication.State.READY) {
            startMain();
            return;
        }

        initViews();
        mStartTime = System.currentTimeMillis();
        mSplashService = new SplashService(this, getTwinmeContext(), this);
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        if (mSplashTimer != null) {
            mSplashTimer.cancel(false);
            mSplashTimer = null;
        }
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        animate();
    }

    @Override
    public void onState(TwinmeApplication.State state) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onState state=" + state);
        }

        mState = state;
        mUpgradeMessage.setVisibility(state == TwinmeApplication.State.UPGRADING ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onReady(boolean hasProfiles, boolean hasContacts, boolean hasConversations) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onReady: hasConversations=" + hasConversations);
        }

        mHasConversations = hasConversations;
        long delay = System.currentTimeMillis() - mStartTime;
        mReady = true;
        if (delay >= MIN_ANIMATION_DURATION) {
            startMain();
        }
    }

    @Override
    public void onFatalError(BaseService.ErrorCode errorCode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onFatalError: errorCode=" + errorCode);
        }

        Intent intent = new Intent(this, FatalErrorActivity.class);
        intent.putExtra(Intents.INTENT_DATABASE_UPGRADED, getTwinmeContext().isDatabaseUpgraded());
        intent.putExtra(Intents.INTENT_ERROR_ID, errorCode.toString());
        startActivity(intent);
        finish();
    }

    @Override
    public void finish() {
        if (DEBUG) {
            Log.d(LOG_TAG, "finish");
        }

        if (mSplashTimer != null) {
            mSplashTimer.cancel(false);
            mSplashTimer = null;
        }
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }
        if (mSplashService != null) {
            mSplashService.dispose();
        }
        super.finish();
    }

    private void startMain() {
        if (DEBUG) {
            Log.d(LOG_TAG, "startMain");
        }

        if (mStarted) {
            return;
        }

        mStarted = true;
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(Intents.INTENT_HAS_CONVERSATIONS, mHasConversations);

        UUID accountMigrationId = getTwinmeContext().getAccountMigrationService().getActiveDeviceMigrationId();
        if (mState == TwinmeApplication.State.MIGRATION) {
            intent.putExtra(Intents.INTENT_ACCOUNT_MIGRATION_ID, accountMigrationId);
            intent.setClass(this, AccountMigrationActivity.class);
        } else if (getTwinmeApplication().showWelcomeScreen()) {
            intent.setClass(this, WelcomeActivity.class);
        } else if (getTwinmeApplication().showUpgradeScreen()) {
            intent.putExtra(Intents.INTENT_UPGRADE_FROM_SPLASHSCREEN, true);
            intent.setClass(this, PremiumServicesActivity.class);
        } else {
            intent.setClass(this, MainActivity.class);

            // Forward the android.intent.action.VIEW to the main activity.
            Intent callingIntent = getIntent();
            if (callingIntent != null) {
                String action = callingIntent.getAction();
                if (action != null) {
                    Uri uri = callingIntent.getData();
                    if (uri != null) {
                        intent.putExtras(callingIntent);
                        intent.setAction(action);
                        intent.setData(uri);
                    }
                }
            }
        }
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());

        setContentView(R.layout.splashscreen_activity);
        setBackgroundColor(Design.WHITE_COLOR);

        ImageView logoView = findViewById(R.id.splashscreen_activity_logo_view);
        ImageView twinmeView = findViewById(R.id.splashscreen_activity_twinme_view);

        mUpgradeMessage = findViewById(R.id.splashscreen_activity_twinme_upgrading);
        Design.updateTextFont(mUpgradeMessage, Design.FONT_REGULAR34);
        mUpgradeMessage.setTextColor(Design.FONT_COLOR_DEFAULT);

        logoView.setAlpha((float) 0.0);
        twinmeView.setAlpha((float) 0.0);
        twinmeView.setColorFilter(Design.BLACK_COLOR);

        animationList.clear();

        animationList.add(logoView);
        animationList.add(twinmeView);

        setStatusBarColor(Design.WHITE_COLOR);
    }

    private void animate() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animate");
        }

        PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.0f, 2.0f);

        List<Animator> animators = new ArrayList<>();

        for (View view : animationList) {
            ObjectAnimator alphaViewAnimator = ObjectAnimator.ofPropertyValuesHolder(view, propertyValuesHolderAlpha);
            alphaViewAnimator.setDuration(ANIMATION_DURATION);
            animators.add(alphaViewAnimator);
        }

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {

                mAnimatorSet = null;
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
        mAnimatorSet.playTogether(animators);
        mAnimatorSet.start();

        // We cannot rely on the animation to terminate and move to the main activity because animations can
        // be disabled by the user on Android 12.  Use a specific timer to check and start the main activity.
        mSplashTimer = getTwinmeContext().getJobService().schedule(this::checkStart, ANIMATION_DURATION);
    }

    private void checkStart() {
        if (DEBUG) {
            Log.d(LOG_TAG, "checkStart");
        }

        if (mReady) {
            runOnUiThread(this::startMain);
        } else {
            mSplashTimer = getTwinmeContext().getJobService().schedule(this::checkStart, ANIMATION_DURATION);
        }
    }
}
