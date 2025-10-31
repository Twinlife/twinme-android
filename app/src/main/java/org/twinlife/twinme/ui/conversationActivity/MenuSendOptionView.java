/*
 *  Copyright (c) 2019-2025 twinlife SA.
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class MenuSendOptionView extends PercentRelativeLayout {
    private static final String LOG_TAG = "MenuSendOptionView";
    private static final boolean DEBUG = false;

    private static final int DESIGN_LIST_MARGIN = 40;
    private static final int DESIGN_SEND_MARGIN = 80;
    private static final int DESIGN_SEND_VERTICAL_MARGIN = 10;
    private static final int DESIGN_SEND_HORIZONTAL_MARGIN = 20;

    protected View mActionView;

    protected static int ALLOW_COPY_TAG = 0;
    protected static int ALLOW_EPHEMERAL_TAG = 1;

    private boolean mAllowCopy = true;
    private boolean mAllowEphemeral = false;
    private boolean mForceDarkMode = false;
    private int mTimeout = 0;

    private MenuSendOptionAdapter mMenuSendOptionAdapter;

    private int mHeight = Design.DISPLAY_HEIGHT;

    public interface Observer {

        void onCloseMenuAnimationEnd();

        void onAllowEphemeralClick();

        void onSendFromMenuOptionClick(boolean allowCopy, boolean allowEphemeral, int timeout);
    }

    private Observer mObserver;

    private boolean isOpenAnimationEnded = false;
    private boolean isCloseAnimationEnded = false;

    public MenuSendOptionView(Context context) {
        super(context);
    }

    public MenuSendOptionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view = inflater.inflate(R.layout.conversation_activity_menu_send_option_view, (ViewGroup) getParent());
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            addView(view);
        }

        initViews();
    }

    public void setOnMenuSendOptionObserver(AbstractTwinmeActivity activity, Observer observer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setOnMenuSendOptionObserver: " + observer);
        }

        mObserver = observer;

        mMenuSendOptionAdapter = new MenuSendOptionAdapter(activity, this, mAllowCopy, mAllowEphemeral, mTimeout, mForceDarkMode);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, RecyclerView.VERTICAL, false);

        RecyclerView recyclerView = findViewById(R.id.menu_send_option_list_view);
        recyclerView.setBackgroundColor(Color.TRANSPARENT);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(null);
        recyclerView.setAdapter(mMenuSendOptionAdapter);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_LIST_MARGIN * Design.HEIGHT_RATIO);
    }

    public void onOptionChangeValue(int tag, boolean value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onOptionChangeValue: tag=" + tag + " value=" + value);
        }

        boolean needsUpdateHeight = false;
        if (tag == ALLOW_COPY_TAG) {
            mAllowCopy = value;
        } else if (tag == ALLOW_EPHEMERAL_TAG) {
            needsUpdateHeight = true;
            mAllowEphemeral = value;
        }

        mMenuSendOptionAdapter.updateOptions(mAllowCopy, mAllowEphemeral, mTimeout);

        if (needsUpdateHeight) {
            int actionViewHeight = getActionViewHeight();
            ViewGroup.LayoutParams layoutParams = mActionView.getLayoutParams();
            layoutParams.height = actionViewHeight;
            mActionView.setY(mHeight - actionViewHeight);
        }
    }

    public void updateTimeout(int timeout) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateTimeout: timeout=" + timeout);
        }

        mTimeout = timeout;
        mMenuSendOptionAdapter.updateOptions(mAllowCopy, mAllowEphemeral, mTimeout);
    }

    public void setForceDarkMode(boolean forceDarkMode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setForceDarkMode: " + forceDarkMode);
        }

        mForceDarkMode = forceDarkMode;
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
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

                mHeight = getHeight();
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

    public void openMenu(boolean allowCopy, boolean allowEphemeral, long timeout) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu: allowCopy=" + allowCopy);
        }

        isOpenAnimationEnded = false;
        isCloseAnimationEnded = false;

        mAllowCopy = allowCopy;
        mAllowEphemeral = allowEphemeral;
        mTimeout = (int) timeout;

        mMenuSendOptionAdapter.updateOptions(mAllowCopy, mAllowEphemeral, mTimeout);
        
        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};
        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));

        if (mForceDarkMode) {
            scrollIndicatorBackground.getPaint().setColor(Color.rgb(72,72,72));
        } else {
            scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        }

        mActionView.setBackground(scrollIndicatorBackground);

        ViewGroup.LayoutParams layoutParams = mActionView.getLayoutParams();
        layoutParams.height = getActionViewHeight();
        mActionView.setLayoutParams(layoutParams);
        mActionView.setY(Design.DISPLAY_HEIGHT);
        mActionView.invalidate();

        animationOpenMenu();
    }

    public void onAllowEphemeralClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAllowEphemeralClick");
        }

        mObserver.onAllowEphemeralClick();
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

        mActionView = findViewById(R.id.menu_send_option_action_view);
        mActionView.setY(Design.DISPLAY_HEIGHT);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);

        View slideMarkView = findViewById(R.id.menu_send_option_slide_mark_view);
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

        View sendView = findViewById(R.id.menu_send_option_send_view);
        sendView.setOnClickListener(v -> onSendClick());

        radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable sendViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        sendViewBackground.getPaint().setColor(Design.getMainStyle());
        sendView.setBackground(sendViewBackground);

        layoutParams = sendView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;

        sendView.setMinimumHeight(Design.BUTTON_HEIGHT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) sendView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_LIST_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_SEND_MARGIN * Design.HEIGHT_RATIO);

        TextView sendTextView = findViewById(R.id.menu_send_option_send_text_view);
        Design.updateTextFont(sendTextView, Design.FONT_BOLD36);
        sendTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) sendTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SEND_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_SEND_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_SEND_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_SEND_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
    }

    private void onSendClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendClick");
        }

        mObserver.onSendFromMenuOptionClick(mAllowCopy, mAllowEphemeral, mTimeout);
    }

    private int getActionViewHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getActionViewHeight");
        }

        int actionViewHeight = Design.SECTION_HEIGHT * mMenuSendOptionAdapter.getItemCount();

        int bottomInset = 0;
        View rootView = ((Activity) getContext()).getWindow().getDecorView();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowInsets insets = rootView.getRootWindowInsets();
            if (insets != null) {
                bottomInset = insets.getInsets(WindowInsets.Type.systemBars()).bottom;
            }
        }

        return actionViewHeight + (int)(DESIGN_SEND_MARGIN * Design.HEIGHT_RATIO) + Design.BUTTON_HEIGHT + Design.SLIDE_MARK_TOP_MARGIN + Design.SLIDE_MARK_HEIGHT + (int)(DESIGN_LIST_MARGIN * 2 * Design.HEIGHT_RATIO) + bottomInset;
    }
}
