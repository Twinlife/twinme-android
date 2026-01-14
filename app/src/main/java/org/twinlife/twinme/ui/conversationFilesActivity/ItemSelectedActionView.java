/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

public class ItemSelectedActionView extends PercentRelativeLayout {
    private static final String LOG_TAG = "ItemSelectedActionView";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ACTION_WIDTH = 120f;
    private static final float DESIGN_ACTION_VIEW_HEIGHT = 128f;

    private TextView mSelectedCountView;
    private View mShareView;
    private View mDeleteView;

    private int mSelectedItemsCount = 0;

    public interface Observer {

        void onDeleteActionClick();

        void onShareActionClick();
    }

    private AbstractTwinmeActivity mActivity;

    private Observer mObserver;

    public ItemSelectedActionView(Context context) {
        super(context);
    }

    public ItemSelectedActionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        mActivity = (AbstractTwinmeActivity) context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view;
            view = inflater.inflate(R.layout.conversation_files_activity_item_selected_action_view, (ViewGroup) getParent());
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            addView(view);

            initViews();
        }
    }

    public ItemSelectedActionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setObserver(Observer observer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setObserver: " + observer);
        }

        mObserver = observer;
    }

    public void updateSelectedItems(int count) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSelectedItems: " + count);
        }

        mSelectedItemsCount = count;

        String title;
        if (mSelectedItemsCount == 0) {
            title = "";
        } else if (mSelectedItemsCount == 1) {
            title = mActivity.getString(R.string.application_one_item_selected);
        } else {
            title = String.format(mActivity.getString(R.string.application_items_selected), mSelectedItemsCount);
        }
        mSelectedCountView.setText(title);

        if (mSelectedItemsCount == 0) {
            mShareView.setAlpha(0.5f);
            mDeleteView.setAlpha(0.5f);
        } else {
            mShareView.setAlpha(1.0f);
            mDeleteView.setAlpha(1.0f);
        }
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setBackgroundColor(Design.TOOLBAR_COLOR);

        View containerView = findViewById(R.id.conversation_files_activity_item_selected_action_container);
        ViewGroup.LayoutParams layoutParams = containerView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ACTION_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        mSelectedCountView = findViewById(R.id.conversation_files_activity_item_selected_action_text_view);
        Design.updateTextFont(mSelectedCountView, Design.FONT_MEDIUM34);
        mSelectedCountView.setTextColor(Color.WHITE);

        mShareView = findViewById(R.id.conversation_files_activity_item_selected_action_share_view);
        mShareView.setOnClickListener(view -> onShareClick());

        layoutParams = mShareView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ACTION_WIDTH * Design.WIDTH_RATIO);

        mDeleteView = findViewById(R.id.conversation_files_activity_item_selected_action_delete_view);
        mDeleteView.setOnClickListener(view -> onDeleteClick());

        layoutParams = mDeleteView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ACTION_WIDTH * Design.WIDTH_RATIO);
    }

    private void onShareClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onShareClick");
        }

        if (mSelectedItemsCount > 0) {
            mObserver.onShareActionClick();
        }
    }

    private void onDeleteClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteClick");
        }

        if (mSelectedItemsCount > 0) {
            mObserver.onDeleteActionClick();
        }
    }
}
