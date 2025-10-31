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
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.privacyActivity.UITimeout;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class MenuSelectValueView extends PercentRelativeLayout {
    private static final String LOG_TAG = "MenuSelectValueView";
    private static final boolean DEBUG = false;

    public enum MenuType {
        DISPLAY_CALLS,
        EDIT_SPACE,
        IMAGE,
        VIDEO,
        EPHEMERAL_MESSAGE,
        LOCKSCREEN,
        PROFILE_UPDATE_MODE,
        CAMERA_CONTROL
    }

    public interface Observer {

        void onCloseMenuAnimationEnd();

        void onSelectValue(int value);

        void onSelectTimeout(UITimeout timeout);
    }

    private static final int DESIGN_TITLE_MARGIN = 40;

    private View mActionView;
    private TextView mTitleView;
    private ListView mListView;

    private MenuSelectValueAdapter mMenuSelectValueAdapter;

    private AbstractTwinmeActivity mActivity;

    private boolean isOpenAnimationEnded = false;
    private boolean isCloseAnimationEnded = false;
    private boolean mForceDarkMode = false;

    private Observer mObserver;
    private int mHeight = Design.DISPLAY_HEIGHT;

    private MenuType mMenuType;

    public MenuSelectValueView(Context context) {
        super(context);
    }

    public MenuSelectValueView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.messages_settings_activity_menu_select_value, null);
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
        mListView.invalidateViews();

        setupTitle();

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

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};
        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));

        if (mForceDarkMode) {
            scrollIndicatorBackground.getPaint().setColor(Color.rgb(72,72,72));
            mTitleView.setTextColor(Color.WHITE);
        } else {
            scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
            mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        }

        mActionView.setBackground(scrollIndicatorBackground);

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
        if (DEBUG) {
            Log.d(LOG_TAG, "setActivity: " + activity);
        }

        mActivity = activity;

        MenuSelectValueAdapter.OnValueClickListener valueClickListener = new MenuSelectValueAdapter.OnValueClickListener() {
            @Override
            public void onValueClick(int value) {
                mObserver.onSelectValue(value);
            }

            @Override
            public void onTimeoutClick(UITimeout timeout) {
                mObserver.onSelectTimeout(timeout);
            }
        };

        mMenuSelectValueAdapter = new MenuSelectValueAdapter(activity, valueClickListener);
        mMenuSelectValueAdapter.setForceDarkMode(mForceDarkMode);

        mListView = findViewById(R.id.menu_list_view);
        mListView.setBackgroundColor(Color.TRANSPARENT);
        mListView.setAdapter(mMenuSelectValueAdapter);
    }

    public void setSelectedValue(int value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setSelectedValue: " + value);
        }

        mMenuSelectValueAdapter.setSelectedValue(value);
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

        mActionView = findViewById(R.id.menu_action_view);
        mActionView.setY(Design.DISPLAY_HEIGHT);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);

        mTitleView = findViewById(R.id.menu_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM36);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);
    }

    private void setupTitle() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        switch (mMenuType) {
            case IMAGE:
                mTitleView.setText(mActivity.getString(R.string.settings_activity_image_title));
                break;

            case VIDEO:
                mTitleView.setText(mActivity.getString(R.string.show_contact_activity_video));
                break;

            case EDIT_SPACE:
                mTitleView.setText(mActivity.getString(R.string.application_edit));
                break;

            case DISPLAY_CALLS:
                mTitleView.setText(mActivity.getString(R.string.settings_activity_display_call_title));
                break;

            case EPHEMERAL_MESSAGE:
                mTitleView.setText(mActivity.getString(R.string.application_timeout));
                break;

            case LOCKSCREEN:
                mTitleView.setText(mActivity.getString(R.string.privacy_activity_lock_screen_timeout));
                break;

            case PROFILE_UPDATE_MODE:
                mTitleView.setText(mActivity.getString(R.string.edit_profile_activity_propagating_profile));
                break;

            case CAMERA_CONTROL:
                mTitleView.setText(mActivity.getString(R.string.contact_capabilities_activity_camera_control_information));
                break;

            default:
                break;
        }
    }

    private int getActionViewHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getActionViewHeight");
        }

        int actionViewHeight = (int) (MenuSelectValueAdapter.DESIGN_VALUE_HEIGHT * Design.HEIGHT_RATIO) * mMenuSelectValueAdapter.getCount();

        int bottomInset = 0;
        View rootView = ((Activity) getContext()).getWindow().getDecorView();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowInsets insets = rootView.getRootWindowInsets();
            if (insets != null) {
                bottomInset = insets.getInsets(WindowInsets.Type.systemBars()).bottom;
            }
        }

        return (int) (actionViewHeight + (DESIGN_TITLE_MARGIN * 2 * Design.HEIGHT_RATIO)  + mTitleView.getHeight()) + bottomInset;
    }
}
