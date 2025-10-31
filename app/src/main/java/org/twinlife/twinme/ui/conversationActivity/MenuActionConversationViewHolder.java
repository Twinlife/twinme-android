/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.RoundedView;

public class MenuActionConversationViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "MenuActionConversat...";
    private static final boolean DEBUG = false;

    private static final long ANIMATION_DURATION = 200;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 124f;
    private static final float DESIGN_ROUNDED_VIEW_SIZE = 80f;
    private static final float DESIGN_ICON_SIZE = 40f;
    private static final float DESIGN_ICON_MARGIN = 30f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final TextView mTitleView;
    private final View mContainerIconView;
    private final RoundedView mIconRoundedView;
    private final ImageView mIconView;

    MenuActionConversationViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mTitleView = view.findViewById(R.id.menu_action_conversation_item_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR34);
        mTitleView.setTextColor(Design.BLACK_COLOR);
        mTitleView.setAlpha(0.f);

        mContainerIconView = view.findViewById(R.id.menu_action_conversation_item_icon_container_view);
        mContainerIconView.setAlpha(0.f);

        layoutParams = mContainerIconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ROUNDED_VIEW_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ROUNDED_VIEW_SIZE * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mContainerIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_ICON_MARGIN * Design.WIDTH_RATIO);

        mIconRoundedView = view.findViewById(R.id.menu_action_conversation_item_rounded_view);
        mIconRoundedView.setColor(Design.getMainStyle());

        mIconView = view.findViewById(R.id.menu_action_conversation_item_icon_view);

        layoutParams = mIconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_SIZE * Design.HEIGHT_RATIO);
    }

    public void onBind(Context context, UIActionConversation uiActionConversation, int visibilityDelay) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: uiActionConversation=" + uiActionConversation + " visibilityDelay" + visibilityDelay);
        }

        mTitleView.setText(uiActionConversation.getTitle());
        mIconView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),uiActionConversation.getIcon(), context.getTheme()));
        mIconView.setColorFilter(uiActionConversation.getIconColor());
        mIconRoundedView.setColor(Design.WHITE_COLOR);

        mContainerIconView.animate().alpha(1.0f).setDuration(ANIMATION_DURATION).setStartDelay(visibilityDelay);
        mTitleView.animate().alpha(1.0f).setDuration(ANIMATION_DURATION).setStartDelay(visibilityDelay);
    }

    public void onViewRecycled() {
    }
}
