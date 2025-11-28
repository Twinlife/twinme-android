/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.cleanupActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.app.DatePickerDialog;
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
import org.twinlife.twinme.ui.conversationFilesActivity.CustomTabView;
import org.twinlife.twinme.ui.conversationFilesActivity.UICustomTab;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MenuCleanUpExpirationView extends PercentRelativeLayout implements CustomTabView.Observer {
    private static final String LOG_TAG = "MenuCleanUpExpi...";
    private static final boolean DEBUG = false;

    public interface OnCleanUpExpirationListener {

        void onCloseMenuExpiration();

        void onSelectExpiration(UICleanUpExpiration cleanUpExpiration);
    }

    private static final int DESIGN_TITLE_MARGIN = 40;
    private static final int DESIGN_CUSTOM_TAB_VIEW_HEIGHT = 148;
    private static final int DESIGN_CONFIRM_MARGIN = 40;
    private static final int DESIGN_CONFIRM_VERTICAL_MARGIN = 10;
    private static final int DESIGN_CONFIRM_HORIZONTAL_MARGIN = 20;
    private static final int DESIGN_CANCEL_HEIGHT = 140;
    private static final int DESIGN_CANCEL_MARGIN = 40;
    private static final float DESIGN_ITEM_VIEW_HEIGHT = 100f;
    protected static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private View mOverlayView;
    private View mActionView;
    private TextView mTitleView;

    private int mRootHeight = 0;
    private int mActionHeight = 0;

    private boolean mIsOpenAnimationEnded = false;
    private boolean mIsCloseAnimationEnded = false;

    private MenuCleanUpExpirationAdapter mMenuCleanUpExpirationAdapter;

    private final List<UICleanUpExpiration> mExpirationsPeriod = new ArrayList<>();

    private UICleanUpExpiration mUICleanUpExpiration = new UICleanUpExpiration(UICleanUpExpiration.ExpirationType.VALUE, UICleanUpExpiration.ExpirationPeriod.THREE_MONTHS);

    private CleanUpActivity mCleanUpActivity;

    private OnCleanUpExpirationListener mOnCleanUpExpirationListener;

    public MenuCleanUpExpirationView(Context context) {
        super(context);
    }

    public MenuCleanUpExpirationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.cleanup_activity_menu_expiration_view, this, true);
        initViews();
    }

    public CleanUpActivity getLocalCleanUpActivity() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getLocalCleanUpActivity");
        }

        return mCleanUpActivity;
    }

    public UICleanUpExpiration getCleanupExpiration() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getCleanupExpiration");
        }

        return mUICleanUpExpiration;
    }

    public void openMenu(UICleanUpExpiration cleanUpExpiration) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu");
        }

        mUICleanUpExpiration = cleanUpExpiration;
        mIsOpenAnimationEnded = false;
        mIsCloseAnimationEnded = false;

        ViewTreeObserver viewTreeObserver = mActionView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = mActionView.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalLayoutListener(this);

                mRootHeight = mOverlayView.getHeight();
                mActionHeight = getActionViewHeight();

                ViewGroup.LayoutParams layoutParams = mActionView.getLayoutParams();
                layoutParams.height = mActionHeight;
                mActionView.setLayoutParams(layoutParams);

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

        if (mIsOpenAnimationEnded) {
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

                if (mRootHeight != mOverlayView.getHeight()) {
                    mRootHeight = mOverlayView.getHeight();
                    mActionView.setY(mRootHeight - mActionHeight);
                }

                mIsOpenAnimationEnded = true;
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

        if (mIsCloseAnimationEnded) {
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

                mIsCloseAnimationEnded = true;
                mOnCleanUpExpirationListener.onCloseMenuExpiration();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    public void setOnCleanUpExpirationListener(OnCleanUpExpirationListener onCleanUpExpirationListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setOnCleanUpExpirationListener");
        }

        mOnCleanUpExpirationListener = onCleanUpExpirationListener;
    }

    public void setLocalCleanUpActivity(CleanUpActivity cleanUpActivity) {

        mCleanUpActivity = cleanUpActivity;

        MenuCleanUpExpirationAdapter.OnMenuExpirationClickListener onMenuExpirationClickListener = new MenuCleanUpExpirationAdapter.OnMenuExpirationClickListener() {

            @Override
            public void onSelectDate() {

                selectExpirationDate();
            }

            @Override
            public void onSelectPeriod(UICleanUpExpiration.ExpirationPeriod expirationPeriod) {

                mUICleanUpExpiration.setExpirationPeriod(expirationPeriod);
                mMenuCleanUpExpirationAdapter.notifyDataSetChanged();
            }
        };

        mMenuCleanUpExpirationAdapter = new MenuCleanUpExpirationAdapter(this, mExpirationsPeriod, onMenuExpirationClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(cleanUpActivity, RecyclerView.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.menu_expiration_view_list_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mMenuCleanUpExpirationAdapter);
        recyclerView.setItemAnimator(null);
    }

    //
    // CustomTabView.Observer implements methods
    //

    @Override
    public void onSelectCustomTab(UICustomTab customTab) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectCustomTab: " + customTab);
        }

        if (customTab.getCustomTabType() == UICustomTab.CustomTabType.PERIOD) {
            mUICleanUpExpiration.setExpirationType(UICleanUpExpiration.ExpirationType.VALUE);
            mMenuCleanUpExpirationAdapter.setPeriodSelect(true);
        } else if (customTab.getCustomTabType() == UICustomTab.CustomTabType.DATE) {
            mUICleanUpExpiration.setExpirationType(UICleanUpExpiration.ExpirationType.DATE);
            mMenuCleanUpExpirationAdapter.setPeriodSelect(false);
        }

        mRootHeight = mOverlayView.getHeight();
        mActionHeight = getActionViewHeight();
        mActionView.setY(mRootHeight - mActionHeight);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        initExpirations();

        mOverlayView = findViewById(R.id.menu_expiration_view_overlay_view);
        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setAlpha(0);
        mOverlayView.setOnClickListener(v -> onDismissClick());

        mActionView = findViewById(R.id.menu_expiration_view_action_view);
        mActionView.setY(Design.DISPLAY_HEIGHT);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);

        View slideMarkView = findViewById(R.id.menu_expiration_view_slide_mark_view);
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

        mTitleView = findViewById(R.id.menu_expiration_view_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM36);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);

        List<UICustomTab> customTabs = new ArrayList<>();
        customTabs.add(new UICustomTab(getContext().getString(R.string.cleanup_activity_older_than), UICustomTab.CustomTabType.PERIOD, true));
        customTabs.add(new UICustomTab(getContext().getString(R.string.cleanup_activity_prior_to), UICustomTab.CustomTabType.DATE, false));

        CustomTabView customTabView = findViewById(R.id.menu_expiration_view_tab_view);

        layoutParams = customTabView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CUSTOM_TAB_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        customTabView.initTabs(customTabs, this);
        customTabView.updateColor(Design.WHITE_COLOR, Design.CUSTOM_TAB_GREY_COLOR, Design.BLACK_COLOR,  Design.CUSTOM_TAB_GREY_COLOR);

        View confirmView = findViewById(R.id.menu_expiration_view_confirm_view);
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

        TextView confirmTextView = findViewById(R.id.menu_expiration_view_confirm_text_view);
        Design.updateTextFont(confirmTextView, Design.FONT_BOLD36);
        confirmTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) confirmTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONFIRM_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_CONFIRM_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CONFIRM_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        View cancelView = findViewById(R.id.menu_expiration_view_cancel_view);
        cancelView.setOnClickListener(v -> onDismissClick());

        layoutParams = cancelView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CANCEL_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) cancelView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_CANCEL_MARGIN * Design.HEIGHT_RATIO);

        TextView cancelTextView = findViewById(R.id.menu_expiration_view_cancel_text_view);
        Design.updateTextFont(cancelTextView, Design.FONT_BOLD36);
        cancelTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }

    private void initExpirations() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initExpirations");
        }

        mExpirationsPeriod.clear();
        mExpirationsPeriod.add(new UICleanUpExpiration(UICleanUpExpiration.ExpirationType.VALUE, UICleanUpExpiration.ExpirationPeriod.ONE_DAY));
        mExpirationsPeriod.add(new UICleanUpExpiration(UICleanUpExpiration.ExpirationType.VALUE, UICleanUpExpiration.ExpirationPeriod.ONE_WEEK));
        mExpirationsPeriod.add(new UICleanUpExpiration(UICleanUpExpiration.ExpirationType.VALUE, UICleanUpExpiration.ExpirationPeriod.ONE_MONTH));
        mExpirationsPeriod.add(new UICleanUpExpiration(UICleanUpExpiration.ExpirationType.VALUE, UICleanUpExpiration.ExpirationPeriod.THREE_MONTHS));
        mExpirationsPeriod.add(new UICleanUpExpiration(UICleanUpExpiration.ExpirationType.VALUE, UICleanUpExpiration.ExpirationPeriod.SIX_MONTHS));
        mExpirationsPeriod.add(new UICleanUpExpiration(UICleanUpExpiration.ExpirationType.VALUE, UICleanUpExpiration.ExpirationPeriod.ONE_YEAR));
    }

    private void selectExpirationDate() {
        if (DEBUG) {
            Log.d(LOG_TAG, "selectExpirationDate");
        }

        final Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        DatePickerDialog.OnDateSetListener onDateSetListener = (datePicker, y, m, d) -> {
            calendar.set(y, m, d);
            mUICleanUpExpiration.setExpirationDate(calendar.getTime());
            mMenuCleanUpExpirationAdapter.notifyItemChanged(0);
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(mCleanUpActivity, onDateSetListener, year, month, day);
        datePickerDialog.show();
    }

    private int getActionViewHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getActionViewHeight");
        }

        int slideMarkHeight = Design.SLIDE_MARK_HEIGHT + Design.SLIDE_MARK_TOP_MARGIN;
        int recyclerViewHeight = 0;
        if (mUICleanUpExpiration.getExpirationType() == UICleanUpExpiration.ExpirationType.VALUE) {
            recyclerViewHeight = mExpirationsPeriod.size() * ITEM_VIEW_HEIGHT;
        } else if (mUICleanUpExpiration.getExpirationType() == UICleanUpExpiration.ExpirationType.DATE) {
            recyclerViewHeight = ITEM_VIEW_HEIGHT;
        }

        int customTabHeight = (int) (DESIGN_CUSTOM_TAB_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        int bottomHeight = Design.BUTTON_HEIGHT + (int) ((DESIGN_CANCEL_HEIGHT + DESIGN_CANCEL_MARGIN + (DESIGN_CONFIRM_HORIZONTAL_MARGIN * 2)) * Design.HEIGHT_RATIO);

        int titleHeight = mTitleView.getLineHeight();
        int titleMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);

        int bottomInset = 0;
        View rootView = ((Activity) getContext()).getWindow().getDecorView();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowInsets insets = rootView.getRootWindowInsets();
            if (insets != null) {
                bottomInset = insets.getInsets(WindowInsets.Type.systemBars()).bottom;
            }
        }

        return slideMarkHeight + recyclerViewHeight + titleMargin + titleHeight + customTabHeight + bottomHeight + bottomInset;
    }

    private void onConfirmClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConfirmClick");
        }

        mOnCleanUpExpirationListener.onSelectExpiration(mUICleanUpExpiration);
        animationCloseMenu();
    }

    private void onDismissClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDismissClick");
        }

        animationCloseMenu();
    }
}
