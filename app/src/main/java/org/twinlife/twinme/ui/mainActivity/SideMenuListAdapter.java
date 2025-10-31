/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity;

import android.database.DataSetObserver;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.profiles.UIProfile;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SideMenuListAdapter implements ListAdapter {

    private static final float DESIGN_LAYER0_HEIGHT = 260;
    private static final float DESIGN_LAYER1_HEIGHT = 154;
    private static final float DESIGN_LAYER2_HEIGHT = 110;
    private static final float DESIGN_NOTIFICATION_HEIGHT = 16;
    private static final float DESIGN_TOOLBAR_AVATAR_HEIGHT = 32;
    public static final int NUMBER_TAP_HIDDEN_MODE = 8;

    final MainActivity mActivity;
    final List<MenuItem> mMenuItems = new ArrayList<>();
    private final OnMenuClickListener mOnMenuClickListener;

    private boolean mHiddenMode = true;

    private UIProfile mUIProfile;

    private static final MenuItem[] sMenuItems = {
            new MenuItem(MenuItem.MenuItemLevel.LEVEL0, R.string.application_profile, MenuItem.MenuItemAction.PROFILE),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL1, R.string.navigation_activity_application_settings, MenuItem.MenuItemAction.NO_ACTION),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL2, R.string.application_appearance, MenuItem.MenuItemAction.PERSONALIZATION),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL2, R.string.settings_activity_chat_category_title, MenuItem.MenuItemAction.MESSAGE_SETTINGS),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL2, R.string.notifications_fragment_title, MenuItem.MenuItemAction.SOUND_SETTINGS),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL2, R.string.privacy_activity_title, MenuItem.MenuItemAction.PRIVACY),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL2, R.string.premium_services_activity_transfert_title, MenuItem.MenuItemAction.TRANSFER_CALL),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL2, R.string.settings_advanced_activity_title, MenuItem.MenuItemAction.SETTINGS_ADVANCED),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL1, R.string.navigation_activity_support, MenuItem.MenuItemAction.NO_ACTION),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL2, R.string.navigation_activity_help, MenuItem.MenuItemAction.HELP),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL2, R.string.navigation_activity_about_twinme, MenuItem.MenuItemAction.ABOUT_TWINME),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL2, R.string.account_activity_title, MenuItem.MenuItemAction.ACCOUNT),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL2, R.string.migration_twinme_plus_activity_premium_title, MenuItem.MenuItemAction.UPGRADE),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL1, R.string.navigation_activity_sign_out, MenuItem.MenuItemAction.NO_ACTION),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL2, R.string.navigation_activity_sign_out, MenuItem.MenuItemAction.SIGN_OUT)
    };

    public interface OnMenuClickListener {

        void onMenuClick(MenuItem menuItem);
    }

    SideMenuListAdapter(MainActivity activity, OnMenuClickListener onMenuClickListener) {

        mActivity = activity;
        mOnMenuClickListener = onMenuClickListener;


        if (!BuildConfig.DISPLAY_PREMIUM_FEATURE) {
            for (MenuItem menuItem : sMenuItems) {
                if (menuItem.getAction() != MenuItem.MenuItemAction.PRIVACY && menuItem.getAction() != MenuItem.MenuItemAction.UPGRADE) {
                    mMenuItems.add(menuItem);
                }
            }
        } else {
            mMenuItems.addAll(Arrays.asList(sMenuItems));
        }
    }

    public void setHiddenMode(boolean hiddenMode) {

        mHiddenMode = hiddenMode;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public int getCount() {

        if (mHiddenMode) {

            return mMenuItems.size() - 2;
        }

        return mMenuItems.size();
    }

    @Override
    public Object getItem(int position) {

        return mMenuItems.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public boolean hasStableIds() {

        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MenuItem menuItem = mMenuItems.get(position);

        if (convertView == null) {
            if (menuItem.getLevel() == MenuItem.MenuItemLevel.LEVEL0) {
                convertView = mActivity.getLayoutInflater().inflate(R.layout.side_menu_header_view, parent, false);
            } else {
                convertView = mActivity.getLayoutInflater().inflate(R.layout.navigation_activity_child, parent, false);
            }
        }

        ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();

        if (menuItem.getLevel() == MenuItem.MenuItemLevel.LEVEL0) {
            convertView.setBackgroundColor(Design.WHITE_COLOR);
            layoutParams.height = (int) (DESIGN_LAYER0_HEIGHT * Design.HEIGHT_RATIO);

            View addContactView = convertView.findViewById(R.id.side_menu_header_add_contact_view);
            addContactView.setOnClickListener(v -> mActivity.inviteContact());

            RoundedView addContactRoundedView = convertView.findViewById(R.id.side_menu_header_add_contact_rounded_view);
            addContactRoundedView.setColor(Design.getMainStyle());

            CircularImageView avatarView = convertView.findViewById(R.id.side_menu_header_avatar_view);
            avatarView.setOnClickListener(v -> mActivity.showProfile());

            float toolbarAvatarHeight = DESIGN_TOOLBAR_AVATAR_HEIGHT * mActivity.getResources().getDisplayMetrics().density;
            float avatarMarginTop = (getActionBarHeight() - toolbarAvatarHeight) * 0.5f;
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) avatarView.getLayoutParams();
            marginLayoutParams.topMargin = (int) avatarMarginTop;

            TextView nameView = convertView.findViewById(R.id.side_menu_header_name_view);
            Design.updateTextFont(nameView, Design.FONT_REGULAR34);
            nameView.setTextColor(Design.FONT_COLOR_DEFAULT);
            nameView.setOnClickListener(v -> mActivity.showProfile());

            if (mUIProfile != null) {
                avatarView.setImage(convertView.getContext(), null, new CircularImageDescriptor(mUIProfile.getAvatar(), 0.5f, 0.5f, 0.5f));
                nameView.setText(mUIProfile.getName());
            } else {
                avatarView.setImage(convertView.getContext(), null, new CircularImageDescriptor(mActivity.getTwinmeApplication().getAnonymousAvatar(), 0.5f, 0.5f, 0.5f));
                nameView.setText(mActivity.getString(R.string.profile_fragment_add_profile));
            }
        } else {
            TextView textView = null;
            ImageView imageView;
            RoundedView roundedView;
            switch (menuItem.getLevel()) {
                case LEVEL1:
                    convertView.setBackgroundColor(Design.WHITE_COLOR);
                    layoutParams.height = (int) (DESIGN_LAYER1_HEIGHT * Design.HEIGHT_RATIO);
                    textView = convertView.findViewById(R.id.navigation_activity_child_level1);
                    Design.updateTextFont(textView, Design.FONT_BOLD26);
                    textView.setTextColor(Design.FONT_COLOR_DEFAULT);

                    imageView = convertView.findViewById(R.id.navigation_activity_child_accessory_level2);
                    imageView.setVisibility(View.GONE);
                    break;

                case LEVEL2:
                    convertView.setBackgroundColor(Design.WHITE_COLOR);
                    layoutParams.height = (int) (DESIGN_LAYER2_HEIGHT * Design.HEIGHT_RATIO);

                    textView = convertView.findViewById(R.id.navigation_activity_child_level2);
                    Design.updateTextFont(textView, Design.FONT_REGULAR32);
                    textView.setTextColor(Design.FONT_COLOR_DEFAULT);

                    imageView = convertView.findViewById(R.id.navigation_activity_child_accessory_level2);
                    imageView.setVisibility(View.VISIBLE);

                    roundedView = convertView.findViewById(R.id.navigation_activity_child_notification_rounded_view);
                    roundedView.setColor(Design.DELETE_COLOR_RED);

                    ViewGroup.LayoutParams roundedViewLayoutParams = roundedView.getLayoutParams();
                    roundedViewLayoutParams.height = (int) (Design.HEIGHT_RATIO * DESIGN_NOTIFICATION_HEIGHT);

                    if (menuItem.getAction() == MenuItem.MenuItemAction.ABOUT_TWINME && mActivity.getTwinmeApplication().hasNewVersion()) {
                        roundedView.setVisibility(View.VISIBLE);
                    } else {
                        roundedView.setVisibility(View.GONE);
                    }

                    break;
            }
            convertView.setLayoutParams(layoutParams);
            convertView.setOnClickListener(view -> mOnMenuClickListener.onMenuClick(menuItem));
            if (textView != null) {
                textView.setText(Utils.capitalizeString(convertView.getResources().getString(menuItem.getText())));
                textView.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {

        MenuItem menuItem = mMenuItems.get(position);
        switch (menuItem.getLevel()) {
            case LEVEL0:
                return MenuItem.TYPE_LEVEL0;

            case LEVEL1:
                return MenuItem.TYPE_LEVEL1;

            case LEVEL2:
            default:
                return MenuItem.TYPE_LEVEL2;
        }
    }

    @Override
    public int getViewTypeCount() {

        return 3;
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

    public void setUIProfile(UIProfile uiProfile) {

        mUIProfile = uiProfile;
    }

    private int getActionBarHeight() {

        int actionBarHeight = 0;
        TypedValue typedValue = new TypedValue();
        if (mActivity.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, mActivity.getResources().getDisplayMetrics());
        }

        return actionBarHeight;
    }
}
