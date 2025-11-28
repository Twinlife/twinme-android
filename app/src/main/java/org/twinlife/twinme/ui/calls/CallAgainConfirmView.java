/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.calls;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.AbstractConfirmView;

public class CallAgainConfirmView  extends AbstractConfirmView {
    private static final String LOG_TAG = "CallAgainConfirmView";
    private static final boolean DEBUG = false;

    private static final int BULLET_COLOR = Color.rgb(213, 213, 213);
    private static final int DESIGN_TITLE_MARGIN = 40;

    public CallAgainConfirmView(Context context) {
        super(context);
    }

    public CallAgainConfirmView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.call_again_confirm_view, this, true);
        initViews();
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.call_again_confirm_view_overlay_view);
        mActionView = findViewById(R.id.call_again_confirm_view_action_view);
        mSlideMarkView = findViewById(R.id.call_again_confirm_view_slide_mark_view);
        mAvatarView = findViewById(R.id.call_again_confirm_view_avatar_view);
        mIconView = findViewById(R.id.call_again_confirm_view_icon_view);
        mIconImageView = findViewById(R.id.call_again_confirm_view_icon_image_view);
        mBulletView = findViewById(R.id.call_again_confirm_view_bullet_view);
        mTitleView = findViewById(R.id.call_again_confirm_view_title_view);
        mMessageView = findViewById(R.id.call_again_confirm_view_message_view);
        mConfirmView = findViewById(R.id.call_again_confirm_view_confirm_view);
        mConfirmTextView = findViewById(R.id.call_again_confirm_view_confirm_text_view);
        mCancelView = findViewById(R.id.call_again_confirm_view_cancel_view);
        mCancelTextView = findViewById(R.id.call_again_confirm_view_cancel_text_view);

        super.initViews();

        MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);

        GradientDrawable iconBackgroundDrawable = new GradientDrawable();
        iconBackgroundDrawable.setColor(BULLET_COLOR);
        iconBackgroundDrawable.setCornerRadius((int) ((DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO) * 0.5));
        iconBackgroundDrawable.setStroke(8, Color.WHITE);
        mIconView.setBackground(iconBackgroundDrawable);

        mIconImageView.setColorFilter(Color.WHITE);

        GradientDrawable bulletBackgroundDrawable = new GradientDrawable();
        bulletBackgroundDrawable.setColor(BULLET_COLOR);
        bulletBackgroundDrawable.setCornerRadius((int) ((DESIGN_BULLET_VIEW_SIZE * Design.HEIGHT_RATIO) * 0.5));
        bulletBackgroundDrawable.setStroke(8, Color.WHITE);
        mBulletView.setBackground(bulletBackgroundDrawable);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.getMainStyle());
        mConfirmView.setBackground(confirmViewBackground);
    }
}
