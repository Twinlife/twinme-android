/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMenuSelectActionView extends RelativeLayout {
    private static final String LOG_TAG = "MenuAddContactView";
    private static final boolean DEBUG = false;

    public interface AbstractMenuSelectActionViewObserver  {

        void onCloseMenuSelectActionAnimationEnd();
    }

    private static final int DESIGN_TITLE_MARGIN = 40;

    private View mOverlayView;
    private View mActionView;
    protected TextView mTitleView;

    private int mRootHeight = 0;
    private int mActionHeight = 0;

    private MenuSelectActionAdapter mMenuSelectActionAdapter;

    private boolean isOpenAnimationEnded = false;
    private boolean isCloseAnimationEnded = false;

    public AbstractMenuSelectActionView(Context context) {
        super(context);
    }

    public AbstractMenuSelectActionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.menu_icon_view, this, true);
        initViews();
    }

    public void updateHeight(int height) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateHeight");
        }

        mActionHeight = height;
    }

    public void openMenu(boolean hideTitle) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu");
        }

        isOpenAnimationEnded = false;
        isCloseAnimationEnded = false;

        if (hideTitle) {
            ViewGroup.LayoutParams layoutParams = mTitleView.getLayoutParams();
            layoutParams.height = 1;
            mTitleView.setVisibility(INVISIBLE);
        } else {
            mTitleView.setVisibility(VISIBLE);
        }

        ViewTreeObserver viewTreeObserver = mActionView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = mActionView.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalLayoutListener(this);

                mRootHeight = mOverlayView.getHeight();
                mActionHeight = getActionViewHeight();

                mActionView.setY(Design.DISPLAY_HEIGHT);
                mActionView.invalidate();
                animationOpenMenu();
            }
        });
    }

    public void animationOpenMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationOpenMenu");
        }

        if (isOpenAnimationEnded) {
            return;
        }

        mOverlayView.setAlpha(1.0f);

        int startValue = mRootHeight;
        int endValue = mRootHeight - mActionHeight;

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

        int startValue = mRootHeight - mActionHeight;
        int endValue = mRootHeight;

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

                mOverlayView.setAlpha(0f);

                isCloseAnimationEnded = true;
                getObserver().onCloseMenuSelectActionAnimationEnd();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    public abstract void startAction(int position);

    public abstract AbstractMenuSelectActionViewObserver getObserver();

    public void setActions(List<UIMenuSelectAction> actions, AbstractTwinmeActivity activity) {

        MenuSelectActionAdapter.OnMenuSelectActionClickListener onMenuSelectActionClickListener = this::startAction;

        mMenuSelectActionAdapter = new MenuSelectActionAdapter(activity, actions, onMenuSelectActionClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, RecyclerView.VERTICAL, false);
        RecyclerView menuRecyclerView = findViewById(R.id.menu_icon_view_list_view);
        menuRecyclerView.setLayoutManager(linearLayoutManager);
        menuRecyclerView.setAdapter(mMenuSelectActionAdapter);
        menuRecyclerView.setItemAnimator(null);
    }

    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.menu_icon_view_overlay_view);
        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setAlpha(0);
        mOverlayView.setOnClickListener(v -> onDismissClick());

        mActionView = findViewById(R.id.menu_icon_view_action_view);
        mActionView.setY(Design.DISPLAY_HEIGHT);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);

        View slideMarkView = findViewById(R.id.menu_icon_view_view_slide_mark_view);
        ViewGroup.LayoutParams layoutParams = slideMarkView.getLayoutParams();
        layoutParams.height = Design.SLIDE_MARK_HEIGHT;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.rgb(244, 244, 244));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        slideMarkView.setBackground(gradientDrawable);

        float corner = ((float)Design.SLIDE_MARK_HEIGHT / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) slideMarkView.getLayoutParams();
        marginLayoutParams.topMargin = Design.SLIDE_MARK_TOP_MARGIN;

        mTitleView = findViewById(R.id.menu_icon_view_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM36);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);
    }

    private int getActionViewHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getActionViewHeight");
        }

        int slideMarkHeight = Design.SLIDE_MARK_HEIGHT + Design.SLIDE_MARK_TOP_MARGIN;
        int actionViewHeight = Design.SECTION_HEIGHT * mMenuSelectActionAdapter.getItemCount();

        int titleHeight = mTitleView.getHeight();
        int titleMargin = (int) (DESIGN_TITLE_MARGIN * 2 * Design.HEIGHT_RATIO);
        if (mTitleView.getVisibility() == INVISIBLE) {
            titleHeight = 0;
        }

        int bottomInset = 0;
        View rootView = ((Activity) getContext()).getWindow().getDecorView();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowInsets insets = rootView.getRootWindowInsets();
            if (insets != null) {
                bottomInset = insets.getInsets(WindowInsets.Type.systemBars()).bottom;
            }
        }

        mActionView.setPadding(0, 0, 0, bottomInset);
        return (slideMarkHeight + actionViewHeight + titleMargin + titleHeight + bottomInset);
    }

    private void onDismissClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDismissClick");
        }

        animationCloseMenu();
    }
}
