/*
 *  Copyright (c) 2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

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
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.utils.RoundedView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class MenuSelectColorView extends PercentRelativeLayout implements OnColorSpaceTouchListener.OnColorObserver {
    private static final String LOG_TAG = "MenuSelectColorView";
    private static final boolean DEBUG = false;

    private static final long ANIMATION_DURATION = 100;

    private static final int DESIGN_TITLE_MARGIN = 40;
    private static final int DESIGN_CONTAINER_MARGIN = 20;
    private static final int DESIGN_COLOR_MARGIN = 10;
    private static final int DESIGN_PREFIX_LEFT_MARGIN = 24;
    private static final int DESIGN_PREFIX_RIGHT_MARGIN = 16;
    private static final int DESIGN_PREVIEW_SIZE = 60;
    private static final int DESIGN_PREVIEW_MARGIN = 20;
    private static final int DESIGN_CONFIRM_MARGIN = 40;
    private static final int DESIGN_CONFIRM_VERTICAL_MARGIN = 10;
    private static final int DESIGN_CONFIRM_HORIZONTAL_MARGIN = 20;
    private static final int DESIGN_CANCEL_HEIGHT = 140;
    private static final int DESIGN_CANCEL_MARGIN = 40;

    private View mOverlayView;
    private View mActionView;
    private TextView mTitleView;
    private TextView mEnterColorTextView;
    private View mEnterColorView;
    private EditText mEnterColorEditText;
    private RoundedView mPreviewView;
    private View mConfirmView;
    private RecyclerView mUIColorRecyclerView;
    private ColorSpaceAdapter mUIColorSpaceListAdapter;
    private List<UIColorSpace> mUIColors;

    public interface OnMenuColorListener {

        void onSelectedColor(String color);

        void onResetColor();

        void onCloseMenu();
    }

    private OnMenuColorListener mOnMenuColorListener;
    private AbstractTwinmeActivity mAppearanceActivity;

    private boolean isOpenAnimationEnded = false;
    private boolean isCloseAnimationEnded = false;

    private boolean mEnterColorEnable = false;

    private int mRootHeight = 0;
    private int mActionHeight = 0;

    private String mDefaultColor;
    private String mHexColor;

    public MenuSelectColorView(Context context) {
        super(context);
    }

    public MenuSelectColorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.space_appearance_activity_menu_select_color_view, this, true);
        initViews();
    }

    public void setOnMenuColorListener(OnMenuColorListener menuColorListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setOnMenuColorListener");
        }

        mOnMenuColorListener = menuColorListener;
    }

    public void openMenu(String title, String color, String defaultColor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu");
        }

        mDefaultColor = defaultColor;
        mUIColorSpaceListAdapter.setDefaultColor(defaultColor);

        mTitleView.setText(title);

        boolean findColor = false;

        for (UIColorSpace customColor : mUIColors) {
            if (customColor.getStringColor() != null && customColor.getStringColor().equals(color)) {
                customColor.setSelected(true);
                findColor = true;
            } else {
                customColor.setSelected(false);
            }
        }

        if (!findColor) {
            if (!mUIColors.isEmpty() && defaultColor.equals(color)) {
                UIColorSpace customColor = mUIColors.get(0);
                customColor.setSelected(true);
                mUIColorSpaceListAdapter.setSelectedColor(color);
            } else if (color != null) {
                mEnterColorEnable = true;
                mUIColorSpaceListAdapter.setEnterColorEnable(true);
                Pattern colorPattern = Pattern.compile("^#([A-Fa-f0-9]{6})$");
                Matcher matcher = colorPattern.matcher(color);
                boolean isColor = matcher.matches();
                if (isColor) {
                    mPreviewView.setColor(Color.parseColor(color));
                    mUIColorSpaceListAdapter.setSelectedColor(color);
                    mEnterColorEditText.setText(color.substring(1));
                } else {
                    mPreviewView.setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
                }

                mPreviewView.invalidate();
            }
        } else {
            mUIColorSpaceListAdapter.setSelectedColor(color);
        }

        InputFilter alphaNumericFilter = (charSequence, i, i1, spanned, i2, i3) -> {
            try {
                char c = charSequence.charAt(0);
                if (Character.isLetter(c) || Character.isDigit(c)) {
                    return "" + Character.toUpperCase(c);
                } else {
                    return "";
                }
            } catch (Exception e) {
            }

            return null;
        };
        mEnterColorEditText.setFilters(new InputFilter[]{alphaNumericFilter});

        enterColor();

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
        objectAnimator.setDuration(ANIMATION_DURATION);
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

        hideKeyboard();

        int startValue = mRootHeight - mActionHeight;
        int endValue = mRootHeight;

        PropertyValuesHolder propertyValuesHolder = PropertyValuesHolder.ofFloat(View.Y, startValue, endValue);

        List<Animator> animators = new ArrayList<>();
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mActionView, propertyValuesHolder);
        objectAnimator.setDuration(ANIMATION_DURATION);
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
                mOnMenuColorListener.onCloseMenu();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    public void setAppearanceActivity(AbstractTwinmeActivity activity) {

        mAppearanceActivity = activity;

        if (mUIColorSpaceListAdapter == null) {

            ColorSpaceAdapter.OnColorClickListener onColorClickListener = new ColorSpaceAdapter.OnColorClickListener() {
                @Override
                public void onUpdateColor(UIColorSpace color) {

                    onSelectColorClick(color);
                }

                @Override
                public void onEnterCustomColor() {

                    onCustomColorClick();
                }
            };

            int colorWidth = (int) (Design.BUTTON_WIDTH - (DESIGN_COLOR_MARGIN * 2 * Design.HEIGHT_RATIO)) / (mUIColors.size() + 1);

            mUIColorSpaceListAdapter = new ColorSpaceAdapter(activity, mUIColors, onColorClickListener, colorWidth);
            LinearLayoutManager uiColorsLinearLayoutManager = new LinearLayoutManager(mAppearanceActivity, RecyclerView.HORIZONTAL, false);
            mUIColorRecyclerView = findViewById(R.id.menu_select_color_view_color_list_view);
            mUIColorRecyclerView.setLayoutManager(uiColorsLinearLayoutManager);
            mUIColorRecyclerView.setAdapter(mUIColorSpaceListAdapter);
            mUIColorRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
            mUIColorRecyclerView.setItemAnimator(null);
            OnColorSpaceTouchListener onTouchListener = new OnColorSpaceTouchListener(mAppearanceActivity, mUIColorRecyclerView, this);
            mUIColorRecyclerView.addOnItemTouchListener(onTouchListener);

            MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mUIColorRecyclerView.getLayoutParams();
            marginLayoutParams.leftMargin = (int) (DESIGN_COLOR_MARGIN * Design.WIDTH_RATIO);
            marginLayoutParams.rightMargin = (int) (DESIGN_COLOR_MARGIN * Design.WIDTH_RATIO);
        }
    }

    //
    // Implement OnColorSpaceTouchListener.OnColorObserver methods
    //

    @Override
    public boolean onUIColorSpaceClick(RecyclerView recyclerView, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUIColorSpaceClick position=" + position);
        }

        if (recyclerView == mUIColorRecyclerView && position >= 0 && position < mUIColors.size()) {
            onSelectColorClick(mUIColors.get(position));
            return true;
        }

        return false;
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.menu_select_color_view_overlay_view);
        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setAlpha(0);
        mOverlayView.setOnClickListener(v -> onCloseMenuClick());

        mActionView = findViewById(R.id.menu_select_color_view_action_view);
        mActionView.setY(Design.DISPLAY_HEIGHT);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);

        View slideMarkView = findViewById(R.id.menu_select_color_view_view_slide_mark_view);
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

        mTitleView = findViewById(R.id.menu_select_color_view_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_BOLD34);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);

        TextView defaultColorTextView = findViewById(R.id.menu_select_color_view_default_color_text_view);
        Design.updateTextFont(defaultColorTextView, Design.FONT_BOLD28);
        defaultColorTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) defaultColorTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);

        View defaultColorView = findViewById(R.id.menu_select_color_view_default_color_content_view);

        radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable defaultColorViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        defaultColorViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        ViewCompat.setBackground(defaultColorView, defaultColorViewBackground);

        layoutParams = defaultColorView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) defaultColorView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.HEIGHT_RATIO);

        mEnterColorTextView = findViewById(R.id.menu_select_color_view_enter_color_text_view);
        Design.updateTextFont(mEnterColorTextView, Design.FONT_BOLD28);
        mEnterColorTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mEnterColorTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);

        mEnterColorView = findViewById(R.id.menu_select_color_view_enter_color_view);

        ShapeDrawable enterColorViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        enterColorViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        ViewCompat.setBackground(mEnterColorView, enterColorViewBackground);

        layoutParams = mEnterColorView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mEnterColorView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.HEIGHT_RATIO);

        TextView colorPrefixTextView = findViewById(R.id.menu_select_color_view_enter_color_prefix_view);
        Design.updateTextFont(colorPrefixTextView, Design.FONT_REGULAR44);
        colorPrefixTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) colorPrefixTextView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_PREFIX_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_PREFIX_RIGHT_MARGIN * Design.WIDTH_RATIO);

        mEnterColorEditText = findViewById(R.id.menu_select_color_view_enter_color_edit_text);
        Design.updateTextFont(mEnterColorEditText, Design.FONT_REGULAR32);
        mEnterColorEditText.setTextColor(Design.FONT_COLOR_DEFAULT);
        mEnterColorEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        mEnterColorEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                previewColor();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mPreviewView = findViewById(R.id.menu_select_color_view_enter_color_preview_view);
        mPreviewView.setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);

        layoutParams = mPreviewView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_PREVIEW_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_PREVIEW_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (MarginLayoutParams) mPreviewView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_PREVIEW_MARGIN * Design.HEIGHT_RATIO);

        mConfirmView = findViewById(R.id.menu_select_color_view_confirm_view);
        mConfirmView.setOnClickListener(view -> onConfirmClick());
        mConfirmView.setAlpha(0.5f);

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.getMainStyle());
        mConfirmView.setBackground(confirmViewBackground);

        layoutParams = mConfirmView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;

        mConfirmView.setMinimumHeight(Design.BUTTON_HEIGHT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mConfirmView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONFIRM_MARGIN * Design.HEIGHT_RATIO);

        TextView confirmTextView = findViewById(R.id.menu_select_color_view_confirm_text_view);
        Design.updateTextFont(confirmTextView, Design.FONT_BOLD36);
        confirmTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) confirmTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONFIRM_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_VERTICAL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_CONFIRM_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CONFIRM_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        View cancelView = findViewById(R.id.menu_select_color_view_cancel_view);
        cancelView.setOnClickListener(v -> onCloseMenuClick());

        layoutParams = cancelView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CANCEL_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) cancelView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_CANCEL_MARGIN * Design.HEIGHT_RATIO);

        TextView cancelTextView = findViewById(R.id.menu_select_color_view_cancel_text_view);
        Design.updateTextFont(cancelTextView, Design.FONT_BOLD36);
        cancelTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mUIColors = Design.spaceColors();

        UIColorSpace colorSpace = mUIColors.get(0);
        colorSpace.setSelected(true);

        if (mAppearanceActivity != null) {

            ColorSpaceAdapter.OnColorClickListener onColorClickListener = new ColorSpaceAdapter.OnColorClickListener() {
                @Override
                public void onUpdateColor(UIColorSpace color) {

                    onSelectColorClick(color);
                }

                @Override
                public void onEnterCustomColor() {
                    onCustomColorClick();
                }
            };

            int colorWidth = (int) (Design.BUTTON_WIDTH - (DESIGN_COLOR_MARGIN * 2 * Design.HEIGHT_RATIO)) / (mUIColors.size() + 1);

            mUIColorSpaceListAdapter = new ColorSpaceAdapter(mAppearanceActivity, mUIColors, onColorClickListener, colorWidth);
            LinearLayoutManager uiColorsLinearLayoutManager = new LinearLayoutManager(mAppearanceActivity, RecyclerView.HORIZONTAL, false);
            mUIColorRecyclerView = findViewById(R.id.menu_select_color_view_color_list_view);
            mUIColorRecyclerView.setLayoutManager(uiColorsLinearLayoutManager);
            mUIColorRecyclerView.setAdapter(mUIColorSpaceListAdapter);
            mUIColorRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
            mUIColorRecyclerView.setItemAnimator(null);

            OnColorSpaceTouchListener onTouchListener = new OnColorSpaceTouchListener(mAppearanceActivity, mUIColorRecyclerView, this);
            mUIColorRecyclerView.addOnItemTouchListener(onTouchListener);

            marginLayoutParams = (MarginLayoutParams) mUIColorRecyclerView.getLayoutParams();
            marginLayoutParams.leftMargin = (int) (DESIGN_COLOR_MARGIN * Design.WIDTH_RATIO);
            marginLayoutParams.rightMargin = (int) (DESIGN_COLOR_MARGIN * Design.WIDTH_RATIO);
        }
    }

    private void onCustomColorClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCustomColorClick");
        }

        mEnterColorEnable = !mEnterColorEnable;
        enterColor();
    }

    private void onSelectColorClick(UIColorSpace color) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectColorClick");
        }

        mEnterColorEnable = false;
        enterColor();

        if (color.getStringColor() == null) {
            mHexColor = mDefaultColor;
        } else {
            mHexColor = color.getStringColor();
        }

        mConfirmView.setAlpha(1.0f);
        mUIColorSpaceListAdapter.setSelectedColor(mHexColor);
    }

    private void onCloseMenuClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseMenuClick");
        }

        mOnMenuColorListener.onCloseMenu();
    }

    private void onConfirmClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConfirmClick");
        }

        if (mHexColor != null) {
            if (mHexColor.equals(mDefaultColor)) {
                mOnMenuColorListener.onResetColor();
            } else {
                mOnMenuColorListener.onSelectedColor(mHexColor);
            }
        }
    }

    private void enterColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "enterColor");
        }

        mUIColorSpaceListAdapter.setEnterColorEnable(mEnterColorEnable);

        if (mEnterColorEnable) {
            mEnterColorTextView.setVisibility(VISIBLE);
            mEnterColorView.setVisibility(VISIBLE);
        } else {
            mEnterColorTextView.setVisibility(GONE);
            mEnterColorView.setVisibility(GONE);
        }
    }

    private void previewColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "previewColor");
        }

        String color = "#" + mEnterColorEditText.getText().toString();
        Pattern colorPattern = Pattern.compile("^#([A-Fa-f0-9]{6})$");
        Matcher matcher = colorPattern.matcher(color);
        boolean isColor = matcher.matches();

        if (isColor) {
            mHexColor = color;
            mConfirmView.setAlpha(1.0f);
            mPreviewView.setColor(Color.parseColor(color));
        } else {
            mHexColor = null;
            mConfirmView.setAlpha(0.5f);
            mPreviewView.setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        }

        mPreviewView.invalidate();
        mUIColorSpaceListAdapter.setSelectedColor(mHexColor);
    }

    private void hideKeyboard() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideKeyboard");
        }

        InputMethodManager inputMethodManager = (InputMethodManager) mAppearanceActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mEnterColorEditText.getWindowToken(), 0);
        }
    }
}
