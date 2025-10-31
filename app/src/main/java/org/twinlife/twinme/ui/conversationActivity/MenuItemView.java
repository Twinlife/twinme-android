/*
 *  Copyright (c) 2019-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.baseItemActivity.Item;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class MenuItemView extends PercentRelativeLayout {
    private static final String LOG_TAG = "MenuItemView";
    private static final boolean DEBUG = false;

    private static final long ANIMATION_DURATION = 100;

    private ConversationActivity mConversationActivity;

    private final List<View> animationList = new ArrayList<>();
    private final List<UIMenuAction> mActions = new ArrayList<>();
    private View mMenuView;
    private MenuItemAdapter mMenuItemAdapter;
    private boolean mCanEditMessage = true;

    private boolean isAnimationEnded = false;

    enum MenuType {
        DEFAULT,
        TEXT,
        IMAGE,
        VIDEO,
        AUDIO,
        FILE,
        INVITATION,
        CALL,
        CLEAR
    }

    public MenuItemView(Context context) {
        super(context);
    }

    public MenuItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        mConversationActivity = (ConversationActivity) context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view;
            view = inflater.inflate(R.layout.conversation_activity_menu_item_view, (ViewGroup) getParent());
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            addView(view);

            initViews();
        }
    }

    public MenuItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setCanEditMessage(boolean canEditMessage) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCanEditMessage: "  + canEditMessage);
        }

        mCanEditMessage = canEditMessage;
    }

    public void openMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu");
        }

        mActions.clear();

        boolean enableAction = true;
        Item selectedItem = mConversationActivity.getSelectedItem();
        if (selectedItem != null && (selectedItem.getState() == Item.ItemState.DELETED || selectedItem.isClearLocalItem() || (selectedItem.isPeerItem() && ((!selectedItem.getCopyAllowed()) || !selectedItem.isAvailableItem() || selectedItem.isEphemeralItem())))) {
            enableAction = false;
        }

        boolean enableReply = true;
        if (selectedItem != null && selectedItem.getState() == Item.ItemState.DELETED) {
            enableReply = false;
        }

        MenuType menuType;
        if (mConversationActivity.getSelectedItem() != null) {

            switch (mConversationActivity.getSelectedItem().getType()) {
                case MESSAGE:
                case PEER_MESSAGE:
                case LINK:
                case PEER_LINK:
                    menuType = MenuType.TEXT;
                    break;
                case IMAGE:
                case PEER_IMAGE:
                    menuType = MenuType.IMAGE;
                    break;
                case VIDEO:
                case PEER_VIDEO:
                    menuType = MenuType.VIDEO;
                    break;
                case AUDIO:
                case PEER_AUDIO:
                    menuType = MenuType.AUDIO;
                    break;
                case FILE:
                case PEER_FILE:
                    menuType = MenuType.FILE;
                    break;
                case INVITATION:
                case PEER_INVITATION:
                case INVITATION_CONTACT:
                case PEER_INVITATION_CONTACT:
                case CALL:
                case PEER_CALL:
                    menuType = MenuType.INVITATION;
                    break;

                case CLEAR:
                case PEER_CLEAR:
                    menuType = MenuType.CLEAR;
                    break;

                default:
                    menuType = MenuType.DEFAULT;
                    break;
            }
        } else {
            menuType = MenuType.DEFAULT;
        }

        switch (menuType) {
            case TEXT:
                mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.conversation_activity_menu_item_view_info_title), R.drawable.info_item, UIMenuAction.ActionType.INFO, true));
                if (selectedItem != null && !selectedItem.isPeerItem() && mCanEditMessage) {
                    mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.application_edit), R.drawable.edit_message_icon, UIMenuAction.ActionType.EDIT, true));
                }
                mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.conversation_activity_menu_item_view_reply_title), R.drawable.reply_item, UIMenuAction.ActionType.REPLY, enableReply));
                mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.conversation_activity_menu_item_view_forward_title), R.drawable.forward_item, UIMenuAction.ActionType.FORWARD, enableAction));
                mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.conversation_activity_menu_item_view_share_title), R.drawable.share_item, UIMenuAction.ActionType.SHARE, enableAction));
                mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.conversation_activity_menu_item_view_copy_title), R.drawable.copy_item, UIMenuAction.ActionType.COPY, enableAction));
                mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.conversation_activity_menu_item_view_delete_title), R.drawable.toolbar_trash_grey, UIMenuAction.ActionType.DELETE, enableReply));
                mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.application_select_more), R.drawable.select_more_item, UIMenuAction.ActionType.SELECT_MORE, true));
                break;

            case IMAGE:
            case VIDEO:
            case AUDIO:
            case FILE:
                mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.conversation_activity_menu_item_view_info_title), R.drawable.info_item, UIMenuAction.ActionType.INFO, true));
                mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.conversation_activity_menu_item_view_reply_title), R.drawable.reply_item, UIMenuAction.ActionType.REPLY, enableReply));
                mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.conversation_activity_menu_item_view_forward_title), R.drawable.forward_item, UIMenuAction.ActionType.FORWARD, enableAction));
                mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.conversation_activity_menu_item_view_share_title), R.drawable.share_item, UIMenuAction.ActionType.SHARE, enableAction));
                mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.conversation_activity_menu_item_view_save_title), R.drawable.save_item, UIMenuAction.ActionType.SAVE, enableAction));
                mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.conversation_activity_menu_item_view_delete_title), R.drawable.toolbar_trash_grey, UIMenuAction.ActionType.DELETE, enableReply));
                mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.application_select_more), R.drawable.select_more_item, UIMenuAction.ActionType.SELECT_MORE, true));
                break;

            case INVITATION:
            case CLEAR:
                mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.conversation_activity_menu_item_view_info_title), R.drawable.info_item, UIMenuAction.ActionType.INFO, true));
                mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.conversation_activity_menu_item_view_delete_title), R.drawable.toolbar_trash_grey, UIMenuAction.ActionType.DELETE, enableReply));
                mActions.add(new UIMenuAction(mConversationActivity.getString(R.string.application_select_more), R.drawable.select_more_item, UIMenuAction.ActionType.SELECT_MORE, true));
                break;

            default:
                break;
        }

        isAnimationEnded = false;

        animationList.clear();
        animationList.add(mMenuView);

        mMenuItemAdapter.setActions(mActions);
    }

    public void animationMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationMenu");
        }

        if (isAnimationEnded) {
            return;
        }

        PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.0f, 1.0f);

        List<Animator> animators = new ArrayList<>();

        for (View view : animationList) {
            ObjectAnimator alphaViewAnimator = ObjectAnimator.ofPropertyValuesHolder(view, propertyValuesHolderAlpha);
            alphaViewAnimator.setDuration(ANIMATION_DURATION);
            animators.add(alphaViewAnimator);
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animators);
        animatorSet.start();
        animatorSet.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                isAnimationEnded = true;
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mMenuView = findViewById(R.id.menu_item_content_view);

        MenuItemAdapter.OnActionClickListener actionClickListener = menuAction -> {
            if (menuAction.getActionType() == UIMenuAction.ActionType.COPY) {
                mConversationActivity.onCopyItemClick();
            } else if (menuAction.getActionType() == UIMenuAction.ActionType.EDIT) {
                mConversationActivity.onEditItemClick();
            } else if (menuAction.getActionType() == UIMenuAction.ActionType.DELETE) {
                mConversationActivity.onDeleteItemClick();
            } else if (menuAction.getActionType() == UIMenuAction.ActionType.FORWARD) {
                mConversationActivity.onForwardItemClick();
            } else if (menuAction.getActionType() == UIMenuAction.ActionType.INFO) {
                mConversationActivity.onInfoItemClick();
            } else if (menuAction.getActionType() == UIMenuAction.ActionType.REPLY) {
                mConversationActivity.onReplyItemClick();
            } else if (menuAction.getActionType() == UIMenuAction.ActionType.SAVE) {
                mConversationActivity.onSaveItemClick();
            } else if (menuAction.getActionType() == UIMenuAction.ActionType.SHARE) {
                mConversationActivity.onShareItemClick();
            } else if (menuAction.getActionType() == UIMenuAction.ActionType.SELECT_MORE) {
                mConversationActivity.onSelectMoreItemClick();
            }
        };
        mMenuItemAdapter = new MenuItemAdapter(mConversationActivity, actionClickListener, mActions);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mConversationActivity, RecyclerView.VERTICAL, false);
        RecyclerView listView = findViewById(R.id.menu_item_list_view);
        listView.setBackgroundColor(Color.TRANSPARENT);
        listView.setLayoutManager(linearLayoutManager);
        listView.setItemAnimator(null);
        listView.setAdapter(mMenuItemAdapter);
    }
}
