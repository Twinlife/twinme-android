/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.profiles;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.skin.Design;

import java.util.ArrayList;
import java.util.List;

public class MenuPropagatingProfileView extends PercentRelativeLayout {
    private static final String LOG_TAG = "MenuPropagating...";
    private static final boolean DEBUG = false;

    public interface Observer {

        void onCloseMenuAnimationEnd();

        void onSelectValue(int value);
    }

    private static final int DESIGN_TITLE_MARGIN = 40;
    private static final int DESIGN_CONFIRM_MARGIN = 40;
    private static final int DESIGN_CONFIRM_VERTICAL_MARGIN = 10;
    private static final int DESIGN_CONFIRM_HORIZONTAL_MARGIN = 20;
    private static final int DESIGN_CANCEL_HEIGHT = 140;
    private static final int DESIGN_CANCEL_MARGIN = 40;

    private View mOverlayView;
    private View mActionView;

    private boolean isOpenAnimationEnded = false;
    private boolean isCloseAnimationEnded = false;

    private Observer mObserver;
    private int mRootHeight = Design.DISPLAY_HEIGHT;
    private int mActionHeight = 0;

    private Profile.UpdateMode mUpdateMode = Profile.UpdateMode.DEFAULT;

    public MenuPropagatingProfileView(Context context) {
        super(context);
    }

    public MenuPropagatingProfileView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.menu_propagating_profile_view, this, true);
        initViews();
    }

    public Profile.UpdateMode getUpdateMode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getUpdateMode");
        }

        return mUpdateMode;
    }

    public void setObserver(Observer observer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setObserver: " + observer);
        }

        mObserver = observer;
    }

    public void openMenu(int updateMode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu");
        }

        if (updateMode == 0) {
            mUpdateMode = Profile.UpdateMode.NONE;
        } else if (updateMode == 1) {
            mUpdateMode = Profile.UpdateMode.DEFAULT;
        } else {
            mUpdateMode = Profile.UpdateMode.ALL;
        }

        isOpenAnimationEnded = false;
        isCloseAnimationEnded = false;

        ViewTreeObserver viewTreeObserver = mActionView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = mActionView.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalLayoutListener(this);

                mRootHeight = mOverlayView.getHeight();
                mActionHeight = mActionView.getHeight();

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
                mObserver.onCloseMenuAnimationEnd();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.menu_propagating_profile_view_overlay_view);
        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setAlpha(0);
        mOverlayView.setOnClickListener(v -> onCloseMenuClick());

        mActionView = findViewById(R.id.menu_propagating_profile_view_action_view);
        mActionView.setY(Design.DISPLAY_HEIGHT);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);

        View slideMarkView = findViewById(R.id.menu_propagating_profile_view_slide_mark_view);
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

        TextView titleView = findViewById(R.id.menu_propagating_profile_view_title_view);
        titleView.setTypeface(Design.FONT_BOLD36.typeface);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD36.size);
        titleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (MarginLayoutParams) titleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);

        TextView subTitleView = findViewById(R.id.menu_propagating_profile_view_subtitle_view);
        subTitleView.setTypeface(Design.FONT_MEDIUM34.typeface);
        subTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
        subTitleView.setTextColor(Design.FONT_COLOR_GREY);

        marginLayoutParams = (MarginLayoutParams) subTitleView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);

        MenuPropagatingAdapter.OnMenuPropagatingClickListener onMenuPropagatingClickListener = value -> mUpdateMode = value;
        MenuPropagatingAdapter menuPropagatingAdapter = new MenuPropagatingAdapter(this, onMenuPropagatingClickListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.menu_propagating_profile_view_list_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(menuPropagatingAdapter);
        recyclerView.setItemAnimator(null);

        View confirmView = findViewById(R.id.menu_propagating_profile_view_confirm_view);
        confirmView.setOnClickListener(view -> onConfirmClick());

        radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.getMainStyle());
        confirmView.setBackground(confirmViewBackground);

        layoutParams = confirmView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;

        confirmView.setMinimumHeight(Design.BUTTON_HEIGHT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) confirmView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONFIRM_MARGIN * Design.HEIGHT_RATIO);

        TextView confirmTextView = findViewById(R.id.menu_propagating_profile_view_confirm_text_view);
        confirmTextView.setTypeface(Design.FONT_BOLD36.typeface);
        confirmTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD36.size);
        confirmTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) confirmTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONFIRM_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_CONFIRM_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CONFIRM_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        View cancelView = findViewById(R.id.menu_propagating_profile_view_cancel_view);
        cancelView.setOnClickListener(v -> onCloseMenuClick());

        layoutParams = cancelView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CANCEL_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) cancelView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_CANCEL_MARGIN * Design.HEIGHT_RATIO);

        TextView cancelTextView = findViewById(R.id.menu_propagating_profile_view_cancel_text_view);
        cancelTextView.setTypeface(Design.FONT_BOLD36.typeface);
        cancelTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD36.size);
        cancelTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    private void onCloseMenuClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseMenuClick");
        }

        mObserver.onCloseMenuAnimationEnd();
    }

    private void onConfirmClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConfirmClick");
        }

        mObserver.onSelectValue(mUpdateMode.ordinal());
    }
}
