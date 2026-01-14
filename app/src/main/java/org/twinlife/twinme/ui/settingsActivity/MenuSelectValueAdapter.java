/*
 *  Copyright (c) 2022-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.database.DataSetObserver;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.DisplayCallsMode;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Zoomable;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.privacyActivity.UITimeout;

import java.util.ArrayList;
import java.util.List;

public class MenuSelectValueAdapter implements ListAdapter {

    protected static final float DESIGN_VALUE_HEIGHT = 120;
    protected static final float DESIGN_CHECKMARK_HEIGHT = 44;
    protected static final float DESIGN_MARGIN = 32;

    private final AbstractTwinmeActivity mActivity;
    private final OnValueClickListener mOnValueClickListener;

    private MenuSelectValueView.MenuType mMenuType = MenuSelectValueView.MenuType.QUALITY_MEDIA;

    private int mSelectedValue = -1;
    private boolean mForceDarkMode = false;

    private List<UITimeout> mTimeouts = new ArrayList<>();

    public interface OnValueClickListener {

        void onValueClick(int value);

        void onTimeoutClick(UITimeout UITimeout);
    }

    MenuSelectValueAdapter(AbstractTwinmeActivity activity, OnValueClickListener onValueClickListener) {

        mActivity = activity;
        mOnValueClickListener = onValueClickListener;
    }

    public void setForceDarkMode(boolean forceDarkMode) {

        mForceDarkMode = forceDarkMode;
    }

    public void setSelectedValue(int selectedValue) {

        mSelectedValue = selectedValue;
    }

    public void setMenuType(MenuSelectValueView.MenuType menuType) {

        mMenuType = menuType;
        setupSelectedValue();

        if (mMenuType == MenuSelectValueView.MenuType.LOCKSCREEN) {
            mTimeouts.clear();
            mTimeouts.add(new UITimeout(mActivity.getString(R.string.privacy_activity_lock_screen_timeout_instant), 0));
            mTimeouts.add(new UITimeout(mActivity.getString(R.string.application_timeout_minute), 60));
            mTimeouts.add(new UITimeout(String.format(mActivity.getString(R.string.application_timeout_minutes), 5), 5 * 60));
            mTimeouts.add(new UITimeout(String.format(mActivity.getString(R.string.application_timeout_minutes), 15), 15 * 60));
            mTimeouts.add(new UITimeout(String.format(mActivity.getString(R.string.application_timeout_minutes), 30), 30 * 60));
            mTimeouts.add(new UITimeout(mActivity.getString(R.string.application_timeout_hour), 60 * 60));
            mTimeouts.add(new UITimeout(String.format(mActivity.getString(R.string.application_timeout_hours), 4), 4 * 60 * 60));
        } else if (mMenuType == MenuSelectValueView.MenuType.EPHEMERAL_MESSAGE) {
            mTimeouts.clear();
            mTimeouts.add(new UITimeout(String.format(mActivity.getString(R.string.application_timeout_seconds), 5), 5));
            mTimeouts.add(new UITimeout(String.format(mActivity.getString(R.string.application_timeout_seconds), 10), 10));
            mTimeouts.add(new UITimeout(String.format(mActivity.getString(R.string.application_timeout_seconds), 30), 30));
            mTimeouts.add(new UITimeout(mActivity.getString(R.string.application_timeout_minute), 60));
            mTimeouts.add(new UITimeout(String.format(mActivity.getString(R.string.application_timeout_minutes), 5), 5 * 60));
            mTimeouts.add(new UITimeout(String.format(mActivity.getString(R.string.application_timeout_minutes), 30), 30 * 60));
            mTimeouts.add(new UITimeout(mActivity.getString(R.string.application_timeout_hour), 60 * 60));
            mTimeouts.add(new UITimeout(mActivity.getString(R.string.application_timeout_day), 24 * 60 * 60));
            mTimeouts.add(new UITimeout(mActivity.getString(R.string.application_timeout_week), 7 * 24 * 60 * 60));
            mTimeouts.add(new UITimeout(mActivity.getString(R.string.application_timeout_month), 30 * 24 * 60 * 60));
        }
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public int getCount() {

        if (mMenuType == MenuSelectValueView.MenuType.QUALITY_MEDIA || mMenuType == MenuSelectValueView.MenuType.EDIT_SPACE) {
            return 2;
        } else if (mMenuType == MenuSelectValueView.MenuType.EPHEMERAL_MESSAGE || mMenuType == MenuSelectValueView.MenuType.LOCKSCREEN) {
            return mTimeouts.size();
        }
        return 3;
    }

    @Override
    public Object getItem(int i) {

        return null;
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public boolean hasStableIds() {

        return true;
    }

    private void setupSelectedValue() {

        if (mActivity == null) {
            return;
        }

        switch (mMenuType) {
            case QUALITY_MEDIA:
                mSelectedValue = mActivity.getTwinmeApplication().qualityMedia();
                break;

            case EDIT_SPACE:
                mSelectedValue = -1;
                break;

            case DISPLAY_CALLS:
                mSelectedValue = mActivity.getTwinmeApplication().displayCallsMode().ordinal();
                break;

            case LOCKSCREEN:
                mSelectedValue = mActivity.getTwinmeApplication().screenLockTimeout();
                break;

            case PROFILE_UPDATE_MODE:
                mSelectedValue = mActivity.getTwinmeApplication().updateProfileMode();
                break;

            default:
                break;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.menu_select_value_child, parent, false);
        }

        String title = "";
        String subTitle = "";
        boolean isChecked = mSelectedValue == position;

        if (mMenuType == MenuSelectValueView.MenuType.QUALITY_MEDIA) {
            if (position == TwinmeApplication.QualityMedia.STANDARD.ordinal()) {
                title = mActivity.getString(R.string.conversation_activity_media_quality_standard);
                subTitle = mActivity.getString(R.string.conversation_activity_media_quality_standard_subtitle);
            } else {
                title = mActivity.getString(R.string.conversation_activity_media_quality_original);
                subTitle = mActivity.getString(R.string.conversation_activity_media_quality_original_subtitle);
            }
        } else if (mMenuType == MenuSelectValueView.MenuType.EDIT_SPACE) {
            if (position == 0) {
                title = mActivity.getString(R.string.settings_space_activity_space_category_title);
            } else {
                title = mActivity.getString(R.string.application_profile);
            }
        } else if (mMenuType == MenuSelectValueView.MenuType.DISPLAY_CALLS) {
            if (position == DisplayCallsMode.NONE.ordinal()) {
                title = mActivity.getString(R.string.settings_activity_display_call_menu_none);
            } else if (position == DisplayCallsMode.MISSED.ordinal()) {
                title = mActivity.getString(R.string.settings_activity_display_call_menu_missed);
            } else {
                title = mActivity.getString(R.string.settings_activity_call_item_menu_all);
            }
        } else if (mMenuType == MenuSelectValueView.MenuType.EPHEMERAL_MESSAGE || mMenuType == MenuSelectValueView.MenuType.LOCKSCREEN) {
            UITimeout uiTimeout = mTimeouts.get(position);
            title = uiTimeout.getText();
            isChecked = uiTimeout.getDelay() == mSelectedValue;
        } else if (mMenuType == MenuSelectValueView.MenuType.CAMERA_CONTROL) {
            if (position == Zoomable.NEVER.ordinal()) {
                title = mActivity.getString(R.string.contact_capabilities_activity_camera_control_never);
            } else if (position == Zoomable.ASK.ordinal()) {
                title = mActivity.getString(R.string.contact_capabilities_activity_camera_control_ask);
            } else {
                title = mActivity.getString(R.string.contact_capabilities_activity_camera_control_allow);
            }
        } else {
            if (position == Profile.UpdateMode.NONE.ordinal()) {
                title = mActivity.getString(R.string.edit_profile_activity_propagating_no_contact);
            } else if (position == Profile.UpdateMode.DEFAULT.ordinal()) {
                title = mActivity.getString(R.string.edit_profile_activity_propagating_except_contacts);
            } else {
                title = mActivity.getString(R.string.edit_profile_activity_propagating_all_contacts);
            }
        }

        ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
        convertView.setBackgroundColor(Color.TRANSPARENT);
        layoutParams.height = (int) (DESIGN_VALUE_HEIGHT * Design.HEIGHT_RATIO);

        TextView nameView = convertView.findViewById(R.id.menu_select_value_child_title);
        Design.updateTextFont(nameView, Design.FONT_REGULAR34);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) nameView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_MARGIN * 2 * Design.WIDTH_RATIO) + (int) (DESIGN_CHECKMARK_HEIGHT * Design.HEIGHT_RATIO);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(title);

        int textColor = Design.FONT_COLOR_DEFAULT;
        if (mForceDarkMode) {
            textColor = Color.WHITE;
        }

        spannableStringBuilder.setSpan(new ForegroundColorSpan(textColor), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (!subTitle.isEmpty()) {
            spannableStringBuilder.append("\n");
            int startSubTitle = spannableStringBuilder.length();
            spannableStringBuilder.append(subTitle);
            spannableStringBuilder.setSpan(new RelativeSizeSpan(0.9f), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        nameView.setText(spannableStringBuilder);

        View checkMarkView = convertView.findViewById(R.id.menu_select_value_child_title_checkmark_view);

        layoutParams = checkMarkView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CHECKMARK_HEIGHT * Design.HEIGHT_RATIO);
        layoutParams.width = (int) (DESIGN_CHECKMARK_HEIGHT * Design.HEIGHT_RATIO);

        ImageView checkMarkImageView = convertView.findViewById(R.id.menu_select_value_child_title_checkmark_image);
        checkMarkImageView.setColorFilter(Design.getMainStyle());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) checkMarkView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_MARGIN * Design.WIDTH_RATIO);

        if (isChecked) {
            checkMarkView.setVisibility(View.VISIBLE);
        } else {
            checkMarkView.setVisibility(View.GONE);
        }

        View separatorView = convertView.findViewById(R.id.menu_select_value_child_separator);
        if (position + 1 == getCount()) {
            separatorView.setVisibility(View.GONE);
        } else {
            separatorView.setVisibility(View.VISIBLE);
        }

        if (mMenuType == MenuSelectValueView.MenuType.EPHEMERAL_MESSAGE || mMenuType == MenuSelectValueView.MenuType.LOCKSCREEN) {
            UITimeout uiTimeout = mTimeouts.get(position);
            convertView.setOnClickListener(view -> mOnValueClickListener.onTimeoutClick(uiTimeout));
        } else {
            convertView.setOnClickListener(view -> mOnValueClickListener.onValueClick(position));
        }


        return convertView;
    }

    @Override
    public int getItemViewType(int i) {

        return 0;
    }

    @Override
    public int getViewTypeCount() {

        return 1;
    }

    @Override
    public boolean isEmpty() {

        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {

        return true;
    }

    @Override
    public boolean isEnabled(int position) {

        return true;
    }
}

