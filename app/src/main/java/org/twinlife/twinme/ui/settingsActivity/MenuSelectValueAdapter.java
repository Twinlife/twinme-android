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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;

import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinlife.DisplayCallsMode;
import org.twinlife.twinme.ui.TwinmeApplication;

public class MenuSelectValueAdapter implements ListAdapter {

    protected static final float DESIGN_VALUE_HEIGHT = 120;

    private final AbstractTwinmeActivity mActivity;
    private final OnValueClickListener mOnValueClickListener;

    private MenuSelectValueView.MenuType mMenuType = MenuSelectValueView.MenuType.IMAGE;

    private int mSelectedValue = -1;

    public interface OnValueClickListener {

        void onValueClick(int value);
    }

    MenuSelectValueAdapter(AbstractTwinmeActivity activity, OnValueClickListener onValueClickListener) {

        mActivity = activity;
        mOnValueClickListener = onValueClickListener;
    }

    public void setMenuType(MenuSelectValueView.MenuType menuType) {

        mMenuType = menuType;
        setupSelectedValue();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public int getCount() {

        if (mMenuType == MenuSelectValueView.MenuType.VIDEO) {
            return 2;
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
            case IMAGE:
                mSelectedValue = mActivity.getTwinmeApplication().sendImageSize();
                break;

            case VIDEO:
                mSelectedValue = mActivity.getTwinmeApplication().sendVideoSize();
                break;

            case DISPLAY_CALLS:
                mSelectedValue = mActivity.getTwinmeApplication().displayCallsMode().ordinal();
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

        if (mMenuType == MenuSelectValueView.MenuType.IMAGE) {
            if (position == TwinmeApplication.SendImageSize.SMALL.ordinal()) {
                title = mActivity.getString(R.string.conversation_activity_reduce_menu_minimal);
                subTitle = mActivity.getString(R.string.conversation_activity_reduce_menu_minimal_size);
            } else if (position == TwinmeApplication.SendImageSize.MEDIUM.ordinal()) {
                title = mActivity.getString(R.string.conversation_activity_reduce_menu_lower);
                subTitle = mActivity.getString(R.string.conversation_activity_reduce_menu_lower_size);
            } else {
                title = mActivity.getString(R.string.conversation_activity_reduce_menu_original);
                subTitle = mActivity.getString(R.string.conversation_activity_reduce_menu_original_subtitle);
            }
        } else if (mMenuType == MenuSelectValueView.MenuType.VIDEO) {
            if (position == TwinmeApplication.SendVideoSize.LOWER.ordinal()) {
                title = mActivity.getString(R.string.conversation_activity_reduce_menu_lower);
            } else {
                title = mActivity.getString(R.string.conversation_activity_reduce_menu_original);
            }
        } else if (mMenuType == MenuSelectValueView.MenuType.DISPLAY_CALLS) {
            if (position == DisplayCallsMode.NONE.ordinal()) {
                title = mActivity.getString(R.string.settings_activity_display_call_menu_none);
            } else if (position == DisplayCallsMode.MISSED.ordinal()) {
                title = mActivity.getString(R.string.settings_activity_display_call_menu_missed);
            } else {
                title = mActivity.getString(R.string.settings_activity_call_item_menu_all);
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

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(title);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (!subTitle.isEmpty()) {
            spannableStringBuilder.append("\n");
            int startSubTitle = spannableStringBuilder.length();
            spannableStringBuilder.append(subTitle);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        nameView.setText(spannableStringBuilder);

        View checkMarkView = convertView.findViewById(R.id.menu_select_value_child_title_checkmark_view);
        ImageView checkMarkImageView = convertView.findViewById(R.id.menu_select_value_child_title_checkmark_image);
        checkMarkImageView.setColorFilter(Design.getMainStyle());

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

        convertView.setOnClickListener(view -> mOnValueClickListener.onValueClick(position));

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

