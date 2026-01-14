/*
 *  Copyright (c) 2022-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

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
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.ArrayList;
import java.util.List;

public class MenuSelectValueView extends RelativeLayout {
    private static final String LOG_TAG = "MenuSelectValueView";
    private static final boolean DEBUG = false;

    public enum MenuType {
        DISPLAY_CALLS,
        QUALITY_MEDIA,
        PROFILE_UPDATE_MODE
    }

    public interface Observer {

        void onCloseMenuAnimationEnd();

        void onSelectValue(int value);
    }

    private static final int DESIGN_TITLE_MARGIN = 40;

    private View mOverlayView;
    private View mActionView;
    private TextView mTitleView;
    private ListView mListView;

    private MenuSelectValueAdapter mMenuSelectValueAdapter;

    private AbstractTwinmeActivity mActivity;

    private boolean isOpenAnimationEnded = false;
    private boolean isCloseAnimationEnded = false;
    private boolean mForceDarkMode = false;

    private Observer mObserver;
    private int mRootHeight = 0;
    private int mActionHeight = 0;

    private MenuType mMenuType;

    public MenuSelectValueView(Context context) {
        super(context);
    }

    public MenuSelectValueView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.messages_settings_activity_menu_select_value, this, true);
        initViews();
    }

    public void setObserver(Observer observer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setObserver: " + observer);
        }

        mObserver = observer;
    }

    public void setForceDarkMode(boolean forceDarkMode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setForceDarkMode: " + forceDarkMode);
        }

        mForceDarkMode = forceDarkMode;
    }

    public void openMenu(MenuType menuType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu");
        }

        mMenuType = menuType;

        isOpenAnimationEnded = false;
        isCloseAnimationEnded = false;

        mMenuSelectValueAdapter.setMenuType(menuType);
        mMenuSelectValueAdapter.setForceDarkMode(mForceDarkMode);
        mListView.invalidateViews();

        setupTitle();

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

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};
        ShapeDrawable actionBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));

        if (mForceDarkMode) {
            actionBackground.getPaint().setColor(Color.rgb(72,72,72));
            mTitleView.setTextColor(Color.WHITE);
        } else {
            actionBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
            mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        }

        mActionView.setBackground(actionBackground);
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

    public void setActivity(AbstractTwinmeActivity activity) {

        mActivity = activity;

        MenuSelectValueAdapter.OnValueClickListener valueClickListener = value -> mObserver.onSelectValue(value);

        mMenuSelectValueAdapter = new MenuSelectValueAdapter(activity, valueClickListener);

        mListView = findViewById(R.id.menu_select_value_view_list_view);
        mListView.setBackgroundColor(Color.TRANSPARENT);
        mListView.setAdapter(mMenuSelectValueAdapter);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.menu_select_value_view_overlay_view);
        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setAlpha(0);
        mOverlayView.setOnClickListener(v -> onDismissClick());

        mActionView = findViewById(R.id.menu_select_value_view_action_view);
        mActionView.setY(Design.DISPLAY_HEIGHT);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);

        View slideMarkView = findViewById(R.id.menu_select_value_view_slide_mark_view);
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

        mTitleView = findViewById(R.id.menu_select_value_view_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM36);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);
    }

    private void setupTitle() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        switch (mMenuType) {
            case QUALITY_MEDIA: {
                mTitleView.setText(mActivity.getString(R.string.conversation_activity_media_quality_title));
                break;
            }

            case DISPLAY_CALLS:
                mTitleView.setText(mActivity.getString(R.string.settings_activity_display_call_title));
                break;

            case PROFILE_UPDATE_MODE:
                mTitleView.setText(mActivity.getString(R.string.edit_profile_activity_propagating_profile));
                break;

            default:
                break;
        }
    }

    private int getActionViewHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getActionViewHeight");
        }

        int slideMarkHeight = Design.SLIDE_MARK_HEIGHT + Design.SLIDE_MARK_TOP_MARGIN;
        int actionViewHeight = Design.SECTION_HEIGHT * mMenuSelectValueAdapter.getCount();

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
