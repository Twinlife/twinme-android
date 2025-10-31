/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.baseItemActivity.BaseItemActivity;

import java.util.ArrayList;
import java.util.List;

public class AnnotationsView extends PercentRelativeLayout {
    private static final String LOG_TAG = "AnnotationsView";
    private static final boolean DEBUG = false;

    public interface Observer {

        void onCloseAnnotationsAnimationEnd();
    }

    private static final int DESIGN_LIST_MARGIN = 70;

    private View mActionView;

    private AnnotationsAdapter mAnnotationsAdapter;

    private boolean isOpenAnimationEnded = false;
    private boolean isCloseAnimationEnded = false;

    private Observer mObserver;
    private int mHeight = Design.DISPLAY_HEIGHT;

    public AnnotationsView(Context context) {
        super(context);
    }

    public AnnotationsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.annotations_view, null);
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            addView(view);

            initViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setObserver(Observer observer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setObserver: " + observer);
        }

        mObserver = observer;
    }

    public void open(List<UIAnnotation> annotations) {
        if (DEBUG) {
            Log.d(LOG_TAG, "open");
        }

        mAnnotationsAdapter.setAnnotations(annotations);

        isOpenAnimationEnded = false;
        isCloseAnimationEnded = false;

        ViewGroup.LayoutParams layoutParams = mActionView.getLayoutParams();
        layoutParams.height = getActionViewHeight();
        mActionView.setLayoutParams(layoutParams);
        mActionView.setY(Design.DISPLAY_HEIGHT);
        mActionView.invalidate();

        animationOpenMenu();
    }

    public void animationOpenMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationOpenMenu");
        }

        if (isOpenAnimationEnded) {
            return;
        }

        int startValue = mHeight;
        int endValue = mHeight - getActionViewHeight();

        PropertyValuesHolder propertyValuesHolder = PropertyValuesHolder.ofFloat(View.Y, startValue, endValue);

        List<Animator> animators = new ArrayList<>();
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mActionView, propertyValuesHolder);
        objectAnimator.setDuration(Design.ANIMATION_VIEW_DURATION);
        animators.add(objectAnimator);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animators);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {

                isOpenAnimationEnded = true;

                if (mHeight != getHeight()) {
                    mHeight = getHeight();
                    mActionView.setY(mHeight - getActionViewHeight());
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

    public void animationCloseMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationCloseMenu");
        }

        if (isCloseAnimationEnded) {
            return;
        }

        int startValue = mHeight - getActionViewHeight();
        int endValue = mHeight;

        PropertyValuesHolder propertyValuesHolder = PropertyValuesHolder.ofFloat(View.Y, startValue, endValue);

        List<Animator> animators = new ArrayList<>();
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mActionView, propertyValuesHolder);
        objectAnimator.setDuration(Design.ANIMATION_VIEW_DURATION);
        animators.add(objectAnimator);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animators);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {

                isCloseAnimationEnded = true;
                mObserver.onCloseAnnotationsAnimationEnd();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    public void setActivity(BaseItemActivity activity) {

        mAnnotationsAdapter = new AnnotationsAdapter(activity, new ArrayList<>());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, RecyclerView.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.annotations_list_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mAnnotationsAdapter);
        recyclerView.setItemAnimator(null);
        recyclerView.setBackgroundColor(Color.TRANSPARENT);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) recyclerView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_LIST_MARGIN * Design.HEIGHT_RATIO);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);

                mHeight = getHeight();
            }
        });

        mActionView = findViewById(R.id.annotations_action_view);
        mActionView.setY(Design.DISPLAY_HEIGHT);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);
    }

    private int getActionViewHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getActionViewHeight");
        }

        int margin = (int) (DESIGN_LIST_MARGIN * Design.HEIGHT_RATIO);
        int maxAnnotation = ((Design.DISPLAY_HEIGHT - margin) / Design.SECTION_HEIGHT) - 2;

        if (mAnnotationsAdapter.getItemCount() < maxAnnotation) {
            maxAnnotation = mAnnotationsAdapter.getItemCount();
        }

        int actionViewHeight = Design.SECTION_HEIGHT * maxAnnotation;

        int bottomInset = 0;
        View rootView = ((Activity) getContext()).getWindow().getDecorView();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowInsets insets = rootView.getRootWindowInsets();
            if (insets != null) {
                bottomInset = insets.getInsets(WindowInsets.Type.systemBars()).bottom;
            }
        }

        return actionViewHeight + margin + bottomInset;
    }
}