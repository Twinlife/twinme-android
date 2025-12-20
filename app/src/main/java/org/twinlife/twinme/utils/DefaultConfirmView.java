/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class DefaultConfirmView extends AbstractBottomSheetView {

    private static final String LOG_TAG = "DefaultConfirmView";
    private static final boolean DEBUG = false;

    private static final int DESIGN_IMAGE_WIDTH = 400;
    private static final int DESIGN_IMAGE_HEIGHT = 240;
    private static final int DESIGN_LARGE_IMAGE_HEIGHT = 400;

    private ImageView mImageView;

    public DefaultConfirmView(Context context) {
        super(context);
    }

    public DefaultConfirmView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.default_confirm_view, this, true);
        initViews();
    }

    @Override
    public void setAvatar(Bitmap avatar, boolean isDefaultGroupAvatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setAvatar: " + avatar);
        }

        super.setAvatar(avatar, isDefaultGroupAvatar);

        mImageView.setVisibility(INVISIBLE);

        ViewGroup.LayoutParams layoutParams = mImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO);
    }

    public void setImage(Drawable drawable) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setImage: " + drawable);
        }

        if (mImageView != null) {
            if (drawable != null) {
                mImageView.setImageDrawable(drawable);
            } else {
                mImageView.setVisibility(GONE);
            }
        }
    }

    public void useLargeImage() {
        if (DEBUG) {
            Log.d(LOG_TAG, "useLargeImage");
        }

        if (mImageView != null) {
            ViewGroup.LayoutParams layoutParams = mImageView.getLayoutParams();
            layoutParams.height = (int) (DESIGN_LARGE_IMAGE_HEIGHT * Design.HEIGHT_RATIO);
        }
    }

    public void setConfirmColor(int color) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setConfirmColor: " + color);
        }

        if (mConfirmView != null) {
            float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
            float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

            ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
            confirmViewBackground.getPaint().setColor(color);
            mConfirmView.setBackground(confirmViewBackground);
        }
    }

    public void hideTitleView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideTitleView");
        }

        if (mTitleView != null) {
            mTitleView.setVisibility(GONE);
        }
    }

    public void hideCancelView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideCancelView");
        }

        if (mCancelView != null) {
            mCancelView.setVisibility(GONE);

            MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mConfirmView.getLayoutParams();
            marginLayoutParams.bottomMargin = (int) (DESIGN_CONFIRM_MARGIN * Design.HEIGHT_RATIO);
        }
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.default_confirm_view_overlay_view);
        mActionView = findViewById(R.id.default_confirm_view_action_view);
        mSlideMarkView = findViewById(R.id.default_confirm_view_slide_mark_view);
        mAvatarView = findViewById(R.id.default_confirm_view_avatar_view);
        mImageView = findViewById(R.id.default_confirm_view_feature_image_view);
        mTitleView = findViewById(R.id.default_confirm_view_title_view);
        mMessageView = findViewById(R.id.default_confirm_view_message_view);
        mConfirmView = findViewById(R.id.default_confirm_view_confirm_view);
        mConfirmTextView = findViewById(R.id.default_confirm_view_confirm_text_view);
        mCancelView = findViewById(R.id.default_confirm_view_cancel_view);
        mCancelTextView = findViewById(R.id.default_confirm_view_cancel_text_view);

        super.initViews();

        mImageView.setMaxWidth((int) (DESIGN_IMAGE_WIDTH * Design.WIDTH_RATIO));
        mImageView.setMaxHeight((int) (DESIGN_IMAGE_HEIGHT * Design.HEIGHT_RATIO));

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mImageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.getMainStyle());
        mConfirmView.setBackground(confirmViewBackground);
    }

    @Override
    public void showConfirmView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        super.showConfirmView();

        updateMessageViewMaxHeight();
    }

    private void updateMessageViewMaxHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateMessageViewMaxHeight");
        }

        float minHeight = Design.SLIDE_MARK_HEIGHT + Design.SLIDE_MARK_TOP_MARGIN + Design.BUTTON_HEIGHT + ((DESIGN_TITLE_MARGIN + DESIGN_MESSAGE_MARGIN + DESIGN_CONFIRM_MARGIN) * Design.HEIGHT_RATIO);

        if (mImageView != null && mImageView.getVisibility() != GONE) {
            minHeight += mImageView.getLayoutParams().height;
        }

        if (mCancelView != null && mCancelView.getVisibility() != GONE) {
            minHeight += (DESIGN_CANCEL_HEIGHT + DESIGN_CANCEL_MARGIN) * Design.HEIGHT_RATIO;
        } else {
            minHeight += (DESIGN_CONFIRM_MARGIN * Design.HEIGHT_RATIO);
        }

        if (mTitleView != null && mTitleView.getVisibility() != GONE) {
            minHeight += (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO) + mTitleView.getHeight();
        }

        float messageMaxHeight = mOverlayView.getHeight() - minHeight;
        mMessageView.setMaxHeight((int) messageMaxHeight);

        if (mOverlayView.getHeight() < mMessageView.getHeight() + minHeight) {
            mMessageView.setVerticalScrollBarEnabled(true);
            mMessageView.setMovementMethod(new ScrollingMovementMethod());
        }
    }
}
