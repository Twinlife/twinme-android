/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.AbstractConfirmView;

public class InvitationCodeShareView extends AbstractConfirmView {
    private static final String LOG_TAG = "InvitationCodeShareView";
    private static final boolean DEBUG = false;

    public InvitationCodeShareView(Context context) {
        super(context);
    }

    public InvitationCodeShareView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.invitation_code_share_view, null);
            view.setLayoutParams(new PercentRelativeLayout.LayoutParams(PercentRelativeLayout.LayoutParams.MATCH_PARENT, PercentRelativeLayout.LayoutParams.MATCH_PARENT));
            addView(view);

            initViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.invitation_code_share_view_overlay_view);
        mActionView = findViewById(R.id.invitation_code_share_view_action_view);
        mSlideMarkView = findViewById(R.id.invitation_code_share_view_slide_mark_view);
        mTitleView = findViewById(R.id.invitation_code_share_view_code_view);
        mMessageView = findViewById(R.id.invitation_code_share_view_message_view);
        mConfirmView = findViewById(R.id.invitation_code_share_view_confirm_view);
        mConfirmTextView = findViewById(R.id.invitation_code_share_view_confirm_text_view);
        mCancelView = findViewById(R.id.invitation_code_share_view_cancel_view);
        mCancelTextView = findViewById(R.id.invitation_code_share_view_cancel_text_view);

        super.initViews();

        Design.updateTextFont(mTitleView, Design.FONT_BOLD88);
        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM36);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.getMainStyle());
        mConfirmView.setBackground(confirmViewBackground);

        mCancelTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}
