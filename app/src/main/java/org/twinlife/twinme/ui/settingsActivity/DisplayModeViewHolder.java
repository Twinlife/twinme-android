/*
 *  Copyright (c) 2022-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.utils.SwitchView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DisplayModeViewHolder extends RecyclerView.ViewHolder {

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 660f;

    private static final int DESIGN_DARK_PEER_ITEM_COLOR = Color.rgb(38, 38, 41);
    private static final int DESIGN_PEER_ITEM_COLOR = Color.rgb(243, 243, 243);
    private static final int DESIGN_DEVICE_BORDER_COLOR = Color.rgb(114, 140, 161);

    private static final int DESIGN_DEVICE_RADIUS = 10;
    private static final int DESIGN_BORDER_WIDTH = 2;
    private static final int DESIGN_ITEM_RADIUS = 3;

    private final SwitchView mSystemSwitchView;

    private final View mLightView;
    private final TextView mLightTimeView;
    private final ImageView mLightSelectedView;
    private final TextView mLightTitleView;
    private final View mLightItemView;

    private final View mDarkView;
    private final TextView mDarkTimeView;
    private final ImageView mDarkSelectedView;
    private final TextView mDarkTitleView;
    private final View mDarkItemView;

    private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener;

    public interface Observer {

        void onDisplayModeSelected(DisplayMode displayMode);
    }

    private final Observer mObserver;

    private int mDisplayMode;

    private int mDefaultColor;

    public DisplayModeViewHolder(@NonNull View view, Observer observer) {

        super(view);

        mObserver = observer;

        mDefaultColor = mDefaultColor;

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        mSystemSwitchView = view.findViewById(R.id.personalization_activity_mode_item_system_view);
        Design.updateTextFont(mSystemSwitchView, Design.FONT_REGULAR32);
        mSystemSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mOnCheckedChangeListener = (buttonView, isChecked) -> {
            if (isChecked) {
                mObserver.onDisplayModeSelected(DisplayMode.SYSTEM);
            } else {
                int currentNightMode = view.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                    mObserver.onDisplayModeSelected(DisplayMode.DARK);
                } else {
                    mObserver.onDisplayModeSelected(DisplayMode.LIGHT);
                }
            }

        };
        mSystemSwitchView.setOnCheckedChangeListener(mOnCheckedChangeListener);

        mLightView = view.findViewById(R.id.personalization_activity_mode_item_light_view);
        mLightView.setOnClickListener(v -> {
            if (mDisplayMode != DisplayMode.SYSTEM.ordinal()) {
                mObserver.onDisplayModeSelected(DisplayMode.LIGHT);
            }
        });

        View deviceLightView = view.findViewById(R.id.personalization_activity_mode_item_light_device_view);

        float radius = DESIGN_DEVICE_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        GradientDrawable lightGradientDrawable = new GradientDrawable();
        lightGradientDrawable.setColor(Color.WHITE);
        lightGradientDrawable.setCornerRadius(radius);
        lightGradientDrawable.setStroke(DESIGN_BORDER_WIDTH, DESIGN_DEVICE_BORDER_COLOR);
        deviceLightView.setBackground(lightGradientDrawable);

        mLightTimeView = view.findViewById(R.id.personalization_activity_mode_item_light_time_view);
        Design.updateTextFont(mLightTimeView, Design.FONT_MEDIUM30);
        mLightTimeView.setTextColor(Color.BLACK);

        View peerItemLightView = view.findViewById(R.id.personalization_activity_mode_item_light_peer_item_view);

        radius = DESIGN_ITEM_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable peerItemLightViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        peerItemLightViewBackground.getPaint().setColor(DESIGN_PEER_ITEM_COLOR);
        peerItemLightView.setBackground(peerItemLightViewBackground);

        mLightItemView = view.findViewById(R.id.personalization_activity_mode_item_light_item_view);

        ShapeDrawable itemLightViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        itemLightViewBackground.getPaint().setColor(mDefaultColor);
        mLightItemView.setBackground(itemLightViewBackground);

        mLightTitleView = view.findViewById(R.id.personalization_activity_mode_item_light_title_view);
        Design.updateTextFont(mLightTitleView, Design.FONT_REGULAR34);
        mLightTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mLightSelectedView = view.findViewById(R.id.personalization_activity_mode_item_light_check_image_view);
        mLightSelectedView.setColorFilter(mDefaultColor);

        mDarkView = view.findViewById(R.id.personalization_activity_mode_item_dark_view);
        mDarkView.setOnClickListener(v -> {
            if (mDisplayMode != DisplayMode.SYSTEM.ordinal()) {
                mObserver.onDisplayModeSelected(DisplayMode.DARK);
            }
        });
        View deviceDarkView = view.findViewById(R.id.personalization_activity_mode_item_dark_device_view);

        radius = DESIGN_DEVICE_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        GradientDrawable darkGradientDrawable = new GradientDrawable();
        darkGradientDrawable.setColor(Color.BLACK);
        darkGradientDrawable.setCornerRadius(radius);
        darkGradientDrawable.setStroke(DESIGN_BORDER_WIDTH, DESIGN_DEVICE_BORDER_COLOR);
        deviceDarkView.setBackground(darkGradientDrawable);

        mDarkTimeView = view.findViewById(R.id.personalization_activity_mode_item_dark_time_view);
        Design.updateTextFont(mDarkTimeView, Design.FONT_MEDIUM30);
        mDarkTimeView.setTextColor(Color.WHITE);

        View peerItemDarkView = view.findViewById(R.id.personalization_activity_mode_item_dark_peer_item_view);

        radius = DESIGN_ITEM_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable peerItemDarkViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        peerItemDarkViewBackground.getPaint().setColor(DESIGN_DARK_PEER_ITEM_COLOR);
        peerItemDarkView.setBackground(peerItemDarkViewBackground);

        mDarkItemView = view.findViewById(R.id.personalization_activity_mode_item_dark_item_view);

        ShapeDrawable itemDarkViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        itemDarkViewBackground.getPaint().setColor(mDefaultColor);
        mDarkItemView.setBackground(itemDarkViewBackground);

        mDarkTitleView = view.findViewById(R.id.personalization_activity_mode_item_dark_title_view);
        Design.updateTextFont(mDarkTitleView, Design.FONT_REGULAR34);
        mDarkTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mDarkSelectedView = view.findViewById(R.id.personalization_activity_mode_item_dark_check_image_view);
        mDarkSelectedView.setColorFilter(mDefaultColor);
    }

    public void onBind(int displayMode, int defaultColor) {

        mDefaultColor = defaultColor;
        mDisplayMode = displayMode;

        mSystemSwitchView.setOnCheckedChangeListener(null);

        if (displayMode == DisplayMode.SYSTEM.ordinal()) {
            mSystemSwitchView.setChecked(true);
            mLightView.setAlpha(0.5f);
            mDarkView.setAlpha(0.5f);

            int currentNightMode = mLightView.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                mLightSelectedView.setVisibility(View.GONE);
                mDarkSelectedView.setVisibility(View.VISIBLE);
            } else {
                mLightSelectedView.setVisibility(View.GONE);
                mDarkSelectedView.setVisibility(View.VISIBLE);
            }
        } else {
            mSystemSwitchView.setChecked(false);
            mLightView.setAlpha(1.0f);
            mDarkView.setAlpha(1.0f);

            if (displayMode == DisplayMode.LIGHT.ordinal()) {
                mLightSelectedView.setVisibility(View.VISIBLE);
                mDarkSelectedView.setVisibility(View.GONE);
            } else {
                mLightSelectedView.setVisibility(View.GONE);
                mDarkSelectedView.setVisibility(View.VISIBLE);
            }
        }
        mSystemSwitchView.setOnCheckedChangeListener(mOnCheckedChangeListener);

        Date now = new Date();
        String format = "hh:mm";

        if (DateFormat.is24HourFormat(mLightTimeView.getContext())) {
            format = "kk:mm";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());
        mLightTimeView.setText(simpleDateFormat.format(now));
        mDarkTimeView.setText(simpleDateFormat.format(now));

        updateFont();
        updateColor();
    }

    private void updateFont() {

        Design.updateTextFont(mLightTitleView, Design.FONT_REGULAR34);
        Design.updateTextFont(mDarkTitleView, Design.FONT_REGULAR34);
        Design.updateTextFont(mSystemSwitchView, Design.FONT_REGULAR32);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mLightTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mDarkTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mSystemSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mLightSelectedView.setColorFilter(mDefaultColor);
        mDarkSelectedView.setColorFilter(mDefaultColor);

        float radius = DESIGN_ITEM_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable itemLightViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        itemLightViewBackground.getPaint().setColor(mDefaultColor);
        mLightItemView.setBackground(itemLightViewBackground);

        ShapeDrawable itemDarkViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        itemDarkViewBackground.getPaint().setColor(mDefaultColor);
        mDarkItemView.setBackground(itemDarkViewBackground);
    }
}