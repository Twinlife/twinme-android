/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.utils.AbstractConfirmView;

public class DeleteConfirmView extends AbstractConfirmView {
    private static final String LOG_TAG = "DeleteConfirmView";
    private static final boolean DEBUG = false;

    public DeleteConfirmView(Context context) {
        super(context);
    }

    public DeleteConfirmView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.delete_confirm_view, null);
            addView(view);
            initViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideAvatar() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideAvatar");
        }

        if (mAvatarView != null) {
            mAvatarView.setVisibility(GONE);
        }

        if (mIconImageView != null) {
            mIconImageView.setVisibility(GONE);
        }

        if (mBulletView != null) {
            mBulletView.setVisibility(GONE);
        }

        if (mIconView != null) {
            mIconView.setVisibility(GONE);
        }
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.delete_confirm_view_overlay_view);
        mActionView = findViewById(R.id.delete_confirm_view_action_view);
        mSlideMarkView = findViewById(R.id.delete_confirm_view_slide_mark_view);
        mAvatarView = findViewById(R.id.delete_confirm_view_avatar_view);
        mIconView = findViewById(R.id.delete_confirm_view_icon_view);
        mIconImageView = findViewById(R.id.delete_confirm_view_icon_image_view);
        mBulletView = findViewById(R.id.delete_confirm_view_bullet_view);
        mTitleView = findViewById(R.id.delete_confirm_view_title_view);
        mMessageView = findViewById(R.id.delete_confirm_view_message_view);
        mConfirmView = findViewById(R.id.delete_confirm_view_confirm_view);
        mConfirmTextView = findViewById(R.id.delete_confirm_view_confirm_text_view);
        mCancelView = findViewById(R.id.delete_confirm_view_cancel_view);
        mCancelTextView = findViewById(R.id.delete_confirm_view_cancel_text_view);

        super.initViews();
    }
}
