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
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

import java.util.ArrayList;
import java.util.List;

public class MenuActionConversationView extends PercentRelativeLayout {
    private static final String LOG_TAG = "MenuActionConver...";
    private static final boolean DEBUG = false;

    private static final float DESIGN_MENU_BOTTOM_MARGIN = 100f;

    public interface Observer {

        void onCloseMenu();

        void onSelectAction(UIActionConversation actionConversation);
    }

    private Observer mObserver;

    public MenuActionConversationView(Context context) {
        super(context);
    }

    public MenuActionConversationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.menu_action_conversation_view, null);
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            addView(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initViews(ConversationActivity activity, Observer observer, boolean sendAllowed) {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setBackgroundColor(Color.TRANSPARENT);

        View backgroundView = findViewById(R.id.menu_action_conversation_view_background);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColors(new int[]{Design.WHITE_COLOR, Design.CONVERSATION_OVERLAY_COLOR});
        gradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        backgroundView.setBackground(gradientDrawable);

        mObserver = observer;

        setOnClickListener(v -> mObserver.onCloseMenu());

        List<UIActionConversation> actions = new ArrayList<>();
        actions.add(new UIActionConversation(getContext(), UIActionConversation.ConversationActionType.RESET, true));
        actions.add(new UIActionConversation(getContext(), UIActionConversation.ConversationActionType.MANAGE_CONVERSATION, true));
        actions.add(new UIActionConversation(getContext(), UIActionConversation.ConversationActionType.MEDIAS_AND_FILES, true));
        actions.add(new UIActionConversation(getContext(), UIActionConversation.ConversationActionType.LOCATION, sendAllowed));
        actions.add(new UIActionConversation(getContext(), UIActionConversation.ConversationActionType.FILE, sendAllowed));
        actions.add(new UIActionConversation(getContext(), UIActionConversation.ConversationActionType.GALLERY, sendAllowed));
        actions.add(new UIActionConversation(getContext(), UIActionConversation.ConversationActionType.VIDEO, sendAllowed));
        actions.add(new UIActionConversation(getContext(), UIActionConversation.ConversationActionType.PHOTO, sendAllowed));

        MenuActionConversationAdapter.OnActionClickListener onActionClickListener = actionConversation -> mObserver.onSelectAction(actionConversation);
        MenuActionConversationAdapter menuActionConversationAdapter = new MenuActionConversationAdapter(activity, onActionClickListener, actions);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, RecyclerView.VERTICAL, false);

        RecyclerView recyclerView = findViewById(R.id.menu_action_conversation_view_list_view);
        recyclerView.setBackgroundColor(Color.TRANSPARENT);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(null);
        recyclerView.setAdapter(menuActionConversationAdapter);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) recyclerView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_MENU_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
    }
}