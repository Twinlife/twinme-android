/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.TwinmeApplication;

public class DefaultTabViewHolder extends RecyclerView.ViewHolder {

    private static final int TAB_GREY_COLOR = Color.argb(255, 119, 138, 159);

    private final ImageView mSpacesImageView;
    private final ImageView mCallsImageView;
    private final ImageView mContactsImageView;
    private final ImageView mConversationsImageView;
    private final ImageView mNotificationsImageView;

    private final View mSelectedView;

    private final AbstractTwinmeActivity mListActivity;

    private int mDefaultColor;

    public DefaultTabViewHolder(@NonNull View view, AbstractTwinmeActivity listActivity) {

        super(view);

        mListActivity = listActivity;

        mDefaultColor = mDefaultColor;

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Design.WHITE_COLOR);

        View spacesView = view.findViewById(R.id.personalization_activity_default_tab_item_spaces_view);
        spacesView.setOnClickListener(v -> {
            mListActivity.getTwinmeApplication().updateDefaultTab(TwinmeApplication.DefaultTab.SPACES);
            updateTab();
        });
        mSpacesImageView = view.findViewById(R.id.personalization_activity_default_tab_item_spaces_image_view);

        View callsView = view.findViewById(R.id.personalization_activity_default_tab_item_calls_view);
        callsView.setOnClickListener(v -> {
            mListActivity.getTwinmeApplication().updateDefaultTab(TwinmeApplication.DefaultTab.CALLS);
            updateTab();
        });
        mCallsImageView = view.findViewById(R.id.personalization_activity_default_tab_item_calls_image_view);

        View contactsView = view.findViewById(R.id.personalization_activity_default_tab_item_contacts_view);
        contactsView.setOnClickListener(v -> {
            mListActivity.getTwinmeApplication().updateDefaultTab(TwinmeApplication.DefaultTab.CONTACTS);
            updateTab();
        });
        mContactsImageView = view.findViewById(R.id.personalization_activity_default_tab_item_contacts_image_view);

        View conversationsView = view.findViewById(R.id.personalization_activity_default_tab_item_conversations_view);
        conversationsView.setOnClickListener(v -> {
            mListActivity.getTwinmeApplication().updateDefaultTab(TwinmeApplication.DefaultTab.CONVERSATIONS);
            updateTab();
        });
        mConversationsImageView = view.findViewById(R.id.personalization_activity_default_tab_item_conversations_image_view);

        View notificationsView = view.findViewById(R.id.personalization_activity_default_tab_item_notifications_view);
        notificationsView.setOnClickListener(v -> {
            mListActivity.getTwinmeApplication().updateDefaultTab(TwinmeApplication.DefaultTab.NOTIFICATIONS);
            updateTab();
        });
        mNotificationsImageView = view.findViewById(R.id.personalization_activity_default_tab_item_notifications_image_view);

        mSelectedView = view.findViewById(R.id.personalization_activity_default_tab_item_selected_tab_view);
        mSelectedView.setBackgroundColor(mDefaultColor);
    }

    public void onBind(int defaultColor) {

        mDefaultColor = defaultColor;

        updateTab();
        updateColor();
    }

    private void updateTab() {

        mCallsImageView.setColorFilter(TAB_GREY_COLOR);
        mContactsImageView.setColorFilter(TAB_GREY_COLOR);
        mConversationsImageView.setColorFilter(TAB_GREY_COLOR);
        mNotificationsImageView.setColorFilter(TAB_GREY_COLOR);

        float tabWidth = (float) (Design.DISPLAY_WIDTH / 5.0);

        if (mListActivity.getTwinmeApplication().defaultTab() == TwinmeApplication.DefaultTab.SPACES.ordinal()) {
            mSpacesImageView.setColorFilter(mDefaultColor);
            mSelectedView.setX(0);
        } else if (mListActivity.getTwinmeApplication().defaultTab() == TwinmeApplication.DefaultTab.CALLS.ordinal()) {
            mCallsImageView.setColorFilter(mDefaultColor);
            mSelectedView.setX(tabWidth);
        } else if (mListActivity.getTwinmeApplication().defaultTab() == TwinmeApplication.DefaultTab.CONTACTS.ordinal()) {
            mContactsImageView.setColorFilter(mDefaultColor);
            mSelectedView.setX(tabWidth * 2);
        } else if (mListActivity.getTwinmeApplication().defaultTab() == TwinmeApplication.DefaultTab.CONVERSATIONS.ordinal()) {
            mConversationsImageView.setColorFilter(mDefaultColor);
            mSelectedView.setX(tabWidth * 3);
        } else if (mListActivity.getTwinmeApplication().defaultTab() == TwinmeApplication.DefaultTab.NOTIFICATIONS.ordinal()) {
            mNotificationsImageView.setColorFilter(mDefaultColor);
            mSelectedView.setX(tabWidth * 4);
        }
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mSelectedView.setBackgroundColor(mDefaultColor);
    }
}
