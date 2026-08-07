/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversations;

import android.app.Activity;
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
import android.view.WindowInsets;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.mainActivity.UIConversation;
import org.twinlife.twinme.utils.AbstractMenuView;

public class MenuConversationShortcutView extends AbstractMenuView {
    private static final String LOG_TAG = "MenuConversationShortcutView";
    private static final boolean DEBUG = false;

    public static final String PROPERTY_CONVERSATION_NOTIFICATION_REACTION = "ConversationNotificationReaction";
    public static final String PROPERTY_CONVERSATION_SILENT_MODE = "ConversationSilentMode";
    public static final String PROPERTY_CONVERSATION_SILENT_MODE_EXPIRATION = "ConversationSilentModeExpiration";

    protected static final int DESIGN_HEADER_HEIGHT = 300;

    public interface Observer extends AbstractMenuViewObserver {

        void onResetConversationClick();

        void onOriginatorClick();

        void onSilentModeDurationClick();

        void onSettingChange(String property, boolean value);
    }

    protected TextView mTitleView;

    private MenuConversationShortcutAdapter mMenuConversationShortcutAdapter;

    private Observer mObserver;
    private UIConversation mConversation;

    private boolean mSilentModeEnabled = false;
    private boolean mNotificationReactionEnabled = true;
    private long mSilentExpirationTimestamp = 0;

    public MenuConversationShortcutView(Context context) {
        super(context);
    }

    public MenuConversationShortcutView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.menu_conversation_shortcut_view, this, true);
        initViews();
    }

    public void openMenu(UIConversation conversation, boolean silentModeEnabled, boolean notificationReactionEnabled, long silentExpirationTimestamp) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu: conversation=" + conversation);
        }

        mConversation = conversation;
        mSilentModeEnabled = silentModeEnabled;
        mNotificationReactionEnabled = notificationReactionEnabled;
        mSilentExpirationTimestamp = silentExpirationTimestamp;

        mMenuConversationShortcutAdapter.loadItems();

        super.openMenu();
    }

    public void setObserver(Observer observer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setObserver: " + observer);
        }

        mObserver = observer;
    }

    @Override
    public AbstractMenuViewObserver getObserver() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getObserver");
        }

        return mObserver;
    }

    public void onHeaderClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onHeaderClick");
        }

        mObserver.onOriginatorClick();
    }

    public void onSettingCheckedChange(MenuConversationShortcutItem item, boolean value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingCheckedChange: item=" + item);
        }

        if (item.getType() == MenuConversationShortcutItem.ConversationShortcutItemType.NOTIFICATIONS_REACTIONS) {
            mNotificationReactionEnabled = value;
            mObserver.onSettingChange(PROPERTY_CONVERSATION_NOTIFICATION_REACTION, value);
        } else if (item.getType() == MenuConversationShortcutItem.ConversationShortcutItemType.SILENT_MODE) {
            mSilentModeEnabled = value;
            mObserver.onSettingChange(PROPERTY_CONVERSATION_SILENT_MODE, value);
            if (mSilentModeEnabled) {
                mObserver.onSilentModeDurationClick();
            }
            mMenuConversationShortcutAdapter.loadItems();
            updateHeight(getActionViewHeight());
        }
    }

    public void onSilentModeDurationClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSilentModeDurationClick");
        }

        mObserver.onSilentModeDurationClick();
    }

    public void onResetConversationClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResetConversationClick");
        }

        mObserver.onResetConversationClick();
    }

    protected boolean isSilentModeEnabled() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isSilentModeEnabled");
        }

        return mSilentModeEnabled;
    }

    protected boolean isNotificationReactionEnabled() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isNotificationReactionEnabled");
        }

        return mNotificationReactionEnabled;
    }

    protected long getSilentModeExpiration() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getSilentModeExpiration");
        }

        return mSilentExpirationTimestamp != -1 ? mSilentExpirationTimestamp * 1000 : mSilentExpirationTimestamp;
    }

    protected UIConversation getConversation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getConversation");
        }

        return mConversation;
    }

    @Override
    public void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.menu_conversation_shortcut_view_overlay_view);
        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setAlpha(0);
        mOverlayView.setOnClickListener(v -> onDismissClick());

        mActionView = findViewById(R.id.menu_conversation_shortcut_view_action_view);
        mActionView.setY(Design.DISPLAY_HEIGHT);

        float radius = Design.ACTION_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mActionView.setBackground(scrollIndicatorBackground);

        View slideMarkView = findViewById(R.id.menu_conversation_shortcut_view_slide_mark_view);
        ViewGroup.LayoutParams layoutParams = slideMarkView.getLayoutParams();
        layoutParams.width = Design.SLIDE_MARK_WIDTH;
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

        mMenuConversationShortcutAdapter = new MenuConversationShortcutAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        RecyclerView menuRecyclerView = findViewById(R.id.menu_conversation_shortcut_view_list_view);
        menuRecyclerView.setLayoutManager(linearLayoutManager);
        menuRecyclerView.setAdapter(mMenuConversationShortcutAdapter);
        menuRecyclerView.setItemAnimator(null);
    }

    @Override
    public int getActionViewHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getActionViewHeight");
        }

        int slideMarkHeight = Design.SLIDE_MARK_HEIGHT + Design.SLIDE_MARK_TOP_MARGIN;
        int headerHeight = (int) (DESIGN_HEADER_HEIGHT * Design.HEIGHT_RATIO);
        int actionViewHeight = Design.SECTION_HEIGHT * (mMenuConversationShortcutAdapter.getItemCount() - 1);
        int bottomInset = 0;
        View rootView = ((Activity) getContext()).getWindow().getDecorView();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowInsets insets = rootView.getRootWindowInsets();
            if (insets != null) {
                bottomInset = insets.getInsets(WindowInsets.Type.systemBars()).bottom;
            }
        }

        mActionView.setPadding(0, 0, 0, bottomInset);
        return (slideMarkHeight + headerHeight + actionViewHeight + bottomInset);
    }
}
