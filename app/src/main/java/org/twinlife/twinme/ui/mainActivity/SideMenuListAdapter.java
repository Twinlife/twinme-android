/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity;

import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.spaces.UISpace;
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

    public static final int DESIGN_SUBSCRIBE_BACKGROUND_COLOR = Color.rgb(186, 241, 215);
    public static final int DESIGN_SUBSCRIBE_COLOR = Color.rgb(10, 169, 141);

    final MainActivity mActivity;
    final List<MenuItem> mMenuItems = new ArrayList<>();
    private final OnMenuClickListener mOnMenuClickListener;

    private boolean mHiddenMode = true;
    private boolean mIsFeatureSubscribed = false;

    private UISpace mUISpace;

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
            new MenuItem(MenuItem.MenuItemLevel.LEVEL2, R.string.navigation_activity_subscribe, MenuItem.MenuItemAction.SUBSCRIBE),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL2, R.string.navigation_activity_help, MenuItem.MenuItemAction.HELP),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL2, R.string.navigation_activity_about_twinme, MenuItem.MenuItemAction.ABOUT_TWINME),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL2, R.string.account_activity_title, MenuItem.MenuItemAction.ACCOUNT),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL1, R.string.navigation_activity_sign_out, MenuItem.MenuItemAction.NO_ACTION),
            new MenuItem(MenuItem.MenuItemLevel.LEVEL2, R.string.navigation_activity_sign_out, MenuItem.MenuItemAction.SIGN_OUT)
    };

    public interface OnMenuClickListener {

        void onMenuClick(MenuItem menuItem);
    }

    SideMenuListAdapter(MainActivity activity, OnMenuClickListener onMenuClickListener) {

        mActivity = activity;
        mOnMenuClickListener = onMenuClickListener;
        mMenuItems.addAll(Arrays.asList(sMenuItems));
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

            if (mUISpace != null && mUISpace.getSpace().getProfile() != null) {
                Bitmap avatar = mUISpace.getAvatar();
                avatarView.setImage(mActivity, null, new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
                nameView.setText(mUISpace.getNameProfile());
            } else {
                Bitmap avatar = mActivity.getTwinmeApplication().getAnonymousAvatar();
                avatarView.setImage(mActivity, null, new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
                nameView.setText(mActivity.getResources().getString(R.string.profile_fragment_add_profile));
            }
        } else {
            TextView textView = null;
            ImageView imageView;
            RoundedView roundedView;
            TextView subscribeView;
            mIsFeatureSubscribed = mActivity.isFeatureSubscribed(TwinmeApplication.Feature.GROUP_CALL);
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

                    subscribeView = convertView.findViewById(R.id.navigation_activity_child_subscribe_view);
                    subscribeView.setTypeface(Design.FONT_MEDIUM32.typeface);
                    subscribeView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM32.size);
                    subscribeView.setTextColor(DESIGN_SUBSCRIBE_COLOR);
                    subscribeView.setText(mActivity.getString(R.string.navigation_activity_subscribe_enable));

                    float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
                    float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

                    ShapeDrawable subscribeViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
                    subscribeViewBackground.getPaint().setColor(DESIGN_SUBSCRIBE_BACKGROUND_COLOR);
                    ViewCompat.setBackground(subscribeView, subscribeViewBackground);

                    ViewGroup.LayoutParams roundedViewLayoutParams = roundedView.getLayoutParams();
                    roundedViewLayoutParams.height = (int) (Design.HEIGHT_RATIO * DESIGN_NOTIFICATION_HEIGHT);

                    if (menuItem.getAction() == MenuItem.MenuItemAction.ABOUT_TWINME && mActivity.getTwinmeApplication().hasNewVersion()) {
                        roundedView.setVisibility(View.VISIBLE);
                    } else {
                        roundedView.setVisibility(View.GONE);
                    }

                    if (menuItem.getAction() == MenuItem.MenuItemAction.SUBSCRIBE && mIsFeatureSubscribed) {
                        subscribeView.setVisibility(View.VISIBLE);
                    } else {
                        subscribeView.setVisibility(View.GONE);
                    }

                    break;
            }
            convertView.setLayoutParams(layoutParams);
            convertView.setOnClickListener(view -> mOnMenuClickListener.onMenuClick(menuItem));
            if (textView != null) {

                if (menuItem.getAction() == MenuItem.MenuItemAction.SUBSCRIBE) {
                    if (mIsFeatureSubscribed) {
                        textView.setText(convertView.getResources().getString(R.string.in_app_subscription_activity_title));
                    } else {
                        textView.setText(convertView.getResources().getString(R.string.navigation_activity_subscribe));
                    }
                } else {
                    textView.setText(Utils.capitalizeString(convertView.getResources().getString(menuItem.getText())));
                }

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

    public void setUISpace(UISpace uiSpace) {

        mUISpace = uiSpace;
    }

    public boolean isFeatureSubscribed() {

        return mIsFeatureSubscribed;
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
