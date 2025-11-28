/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.audio.AudioDevice;
import org.twinlife.twinme.skin.Design;

import java.util.ArrayList;
import java.util.List;

public class SelectAudioSourceView extends RelativeLayout {
    private static final String LOG_TAG = "SelectAudioSourceView";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 124;
    private static final float DESIGN_CONTAINER_MARGIN = 34;
    private static final float DESIGN_CONTAINER_BOTTOM = 186;
    private static final long ANIMATION_DURATION = 50;

    public interface SelectAudioSourceListener {

        void onSelectAudioSource(AudioDevice audioDevice);

        void onDismissSelectAudioSourceView();

        void onCloseSelectAudioSourceViewFinish();
    }

    private CallActivity mCallActivity;
    private SelectAudioSourceListener mSelectAudioSourceListener;

    private final List<View> animationList = new ArrayList<>();
    private final List<UIAudioSource> mAudioSources = new ArrayList<>();
    private View mContainerView;
    private SelectAudioSourceAdapter mSelectAudioSourceAdapter;

    private boolean isAnimationEnded = false;

    public SelectAudioSourceView(Context context) {
        super(context);
    }

    public SelectAudioSourceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        mCallActivity = (CallActivity) context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.call_activity_select_audio_source_view, this, true);
        initViews();
    }

    public SelectAudioSourceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setSelectAudioSourceListener(SelectAudioSourceListener selectAudioSourceListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setSelectAudioSourceListener: " + selectAudioSourceListener);
        }

        mSelectAudioSourceListener = selectAudioSourceListener;
    }

    public void openMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu");
        }

        isAnimationEnded = false;

        mContainerView.setAlpha(0);

        animationList.clear();
        animationList.add(mContainerView);

        animationMenu(false);
    }

    public void closeMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeMenu");
        }

        isAnimationEnded = false;

        animationList.clear();
        animationList.add(mContainerView);

        animationMenu(true);
    }

    public void animationMenu(boolean close) {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationMenu");
        }

        if (isAnimationEnded) {
            return;
        }

        PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.0f, 1.0f);

        if (close) {
            propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0f);
        }

        List<Animator> animators = new ArrayList<>();

        for (View view : animationList) {
            ObjectAnimator alphaViewAnimator = ObjectAnimator.ofPropertyValuesHolder(view, propertyValuesHolderAlpha);
            alphaViewAnimator.setDuration(ANIMATION_DURATION);
            animators.add(alphaViewAnimator);
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animators);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                isAnimationEnded = true;

                if (close) {
                    mSelectAudioSourceListener.onCloseSelectAudioSourceViewFinish();
                }
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    public void setAudioSources(List<UIAudioSource> audioSources) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setAudioSources: " + audioSources);
        }

        mSelectAudioSourceAdapter.setAudioSources(audioSources);

        if (mContainerView != null) {
            ViewGroup.LayoutParams layoutParams = mContainerView.getLayoutParams();
            layoutParams.height = (int) (audioSources.size() * DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        }
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        View overlayView = findViewById(R.id.call_activity_select_audio_source_overlay_view);
        overlayView.setOnClickListener(v -> mSelectAudioSourceListener.onDismissSelectAudioSourceView());

        mContainerView = findViewById(R.id.call_activity_select_audio_source_container_view);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mContainerView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (Design.WIDTH_RATIO * DESIGN_CONTAINER_MARGIN);
        marginLayoutParams.rightMargin = (int) (Design.WIDTH_RATIO * DESIGN_CONTAINER_MARGIN);
        marginLayoutParams.bottomMargin = (int) (Design.HEIGHT_RATIO * DESIGN_CONTAINER_BOTTOM);

        SelectAudioSourceAdapter.OnAudioSourceClickListener audioSourceClickListener = audioSource -> {
            mSelectAudioSourceListener.onSelectAudioSource(audioSource.getAudioDevice());
        };

        mSelectAudioSourceAdapter = new SelectAudioSourceAdapter(mCallActivity, audioSourceClickListener, mAudioSources);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mCallActivity, RecyclerView.VERTICAL, false);
        RecyclerView listView = findViewById(R.id.call_activity_select_audio_source_list_view);
        listView.setBackgroundColor(Color.TRANSPARENT);
        listView.setLayoutManager(linearLayoutManager);
        listView.setItemAnimator(null);
        listView.setAdapter(mSelectAudioSourceAdapter);
    }


}
