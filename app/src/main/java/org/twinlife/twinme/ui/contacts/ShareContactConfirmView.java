/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.AbstractBottomSheetView;

public class ShareContactConfirmView extends AbstractBottomSheetView {
    private static final String LOG_TAG = "ShareContactConfirmView";
    private static final boolean DEBUG = false;

    private static final int BULLET_COLOR = Color.rgb(213, 213, 213);
    private static final int DESIGN_AVATAR_MARGIN = 60;
    private static final int DESIGN_AVATAR_SIZE = 148;
    private static final int DESIGN_ICON_VIEW_SIZE = 72;
    private static final int DESIGN_ICON_IMAGE_VIEW_SIZE = 42;
    private static final int DESIGN_BULLET_VIEW_MARGIN = 20;
    private static final int DESIGN_TITLE_MARGIN = 40;
    private static final int DESIGN_NAME_MARGIN = 6;

    private static final int DESIGN_LINE_DASH_LONG_LENGTH = 8;
    private static final int DESIGN_LINE_DASH_SHORT_LENGTH = 4;
    private static final int DESIGN_LINE_DASH_SPACING = 6;
    private static final int DESIGN_LINE_DASH_WIDTH = 3;
    
    private TextView mLeftNameView;
    private TextView mRightNameView;
    private ShapeableImageView mLeftAvatarView;
    private ShapeableImageView mRightAvatarView;
    private View mIconView;

    public ShareContactConfirmView(Context context) {
        super(context);
    }

    public ShareContactConfirmView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.share_contact_confirm_view, this, true);
        initViews();
    }

    public void setup(String leftName, String rightName, String contactName, Bitmap leftAvatar, Bitmap rightAvatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setup");
        }

        mLeftNameView.setText(leftName);
        mRightNameView.setText(rightName);
        mLeftAvatarView.setImageBitmap(leftAvatar);
        mRightAvatarView.setImageBitmap(rightAvatar);
        mMessageView.setText(String.format(getResources().getString(R.string.share_contact_accept_view_message), contactName, rightName));
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.share_contact_confirm_view_overlay_view);
        mActionView = findViewById(R.id.share_contact_confirm_view_action_view);
        mSlideMarkView = findViewById(R.id.share_contact_confirm_view_slide_mark_view);
        mIconView = findViewById(R.id.share_contact_confirm_view_icon_view);
        mIconImageView = findViewById(R.id.share_contact_confirm_view_icon_image_view);
        mMessageView = findViewById(R.id.share_contact_confirm_view_message_view);
        mConfirmView = findViewById(R.id.share_contact_confirm_view_confirm_view);
        mConfirmTextView = findViewById(R.id.share_contact_confirm_view_confirm_text_view);
        mCancelView = findViewById(R.id.share_contact_confirm_view_cancel_view);
        mCancelTextView = findViewById(R.id.share_contact_confirm_view_cancel_text_view);

        super.initViews();

        View identityView = findViewById(R.id.share_contact_confirm_view_identity_view);
        View leftIdentityView = findViewById(R.id.share_contact_confirm_view_identity_left_view);
        View rightIdentityView = findViewById(R.id.share_contact_confirm_view_identity_right_view);
        View middleIdentityView = findViewById(R.id.share_contact_confirm_view_identity_middle_view);
        mLeftAvatarView = findViewById(R.id.share_contact_confirm_view_left_avatar_view);

        View leftLineContainerView = findViewById(R.id.share_contact_confirm_view_left_line_container_view);
        View leftLineView = findViewById(R.id.share_contact_confirm_view_left_line_view);
        mLeftNameView = findViewById(R.id.share_contact_confirm_view_left_name_view);

        View rightLineContainerView = findViewById(R.id.share_contact_confirm_view_right_line_container_view);
        View rightLineView = findViewById(R.id.share_contact_confirm_view_right_line_view);
        mRightAvatarView = findViewById(R.id.share_contact_confirm_view_right_avatar_view);
        mRightNameView = findViewById(R.id.share_contact_confirm_view_right_name_view);

        MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) identityView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_AVATAR_MARGIN * Design.HEIGHT_RATIO);

        ViewGroup.LayoutParams layoutParams = leftIdentityView.getLayoutParams();
        layoutParams.width = (int) (Design.DISPLAY_WIDTH * 0.5f);

        layoutParams = middleIdentityView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        layoutParams = leftLineContainerView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        layoutParams = rightLineContainerView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        layoutParams = mLeftAvatarView.getLayoutParams();
        mLeftAvatarView.setBackgroundColor(Design.WHITE_COLOR);
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) leftIdentityView.getLayoutParams();
        marginLayoutParams.leftMargin = - (int) (DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO * 0.5);

        Design.updateTextFont(mLeftNameView, Design.FONT_BOLD36);
        mLeftNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        int nameMargin = (int) (DESIGN_NAME_MARGIN * Design.WIDTH_RATIO) + (int) (DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO * 0.5);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) mLeftNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = nameMargin;
        marginLayoutParams.rightMargin = nameMargin;

        float dp = Resources.getSystem().getDisplayMetrics().density;

        Paint dashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dashPaint.setColor(BULLET_COLOR);
        dashPaint.setStyle(Paint.Style.STROKE);
        dashPaint.setStrokeWidth(dp * DESIGN_LINE_DASH_WIDTH);
        dashPaint.setStrokeCap(Paint.Cap.ROUND);
        dashPaint.setPathEffect(new DashPathEffect(new float[]{dp * DESIGN_LINE_DASH_LONG_LENGTH, dp * DESIGN_LINE_DASH_SPACING, dp * DESIGN_LINE_DASH_SHORT_LENGTH, dp * DESIGN_LINE_DASH_SPACING}, 0));
        ShapeDrawable lineLeftDrawable = new ShapeDrawable() {
            @Override
            public void draw(Canvas canvas) {
                float startY = getBounds().height() * 0.5f;
                canvas.drawLine(0, startY, getBounds().width(), startY, dashPaint);
            }
        };
        leftLineView.setBackground(lineLeftDrawable);

        ShapeDrawable lineRightDrawable = new ShapeDrawable() {
            @Override
            public void draw(Canvas canvas) {
                float startY = getBounds().height() * 0.5f;
                canvas.drawLine(getBounds().width(), startY, 0, startY, dashPaint);
            }
        };
        rightLineView.setBackground(lineRightDrawable);

        layoutParams = rightIdentityView.getLayoutParams();
        layoutParams.width = (int) (Design.DISPLAY_WIDTH * 0.5f);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) rightIdentityView.getLayoutParams();
        marginLayoutParams.rightMargin = - (int) (DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO * 0.5);

        layoutParams = mRightAvatarView.getLayoutParams();
        mRightAvatarView.setBackgroundColor(Design.WHITE_COLOR);
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        layoutParams = mIconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (-DESIGN_BULLET_VIEW_MARGIN * Design.WIDTH_RATIO);

        GradientDrawable iconBackgroundDrawable = new GradientDrawable();
        iconBackgroundDrawable.setColor(BULLET_COLOR);
        iconBackgroundDrawable.setCornerRadius((int) ((DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO) * 0.5));
        iconBackgroundDrawable.setStroke(8, Color.WHITE);
        mIconView.setBackground(iconBackgroundDrawable);

        mIconImageView.setColorFilter(Color.WHITE);

        layoutParams = mIconImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_IMAGE_VIEW_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_IMAGE_VIEW_SIZE * Design.HEIGHT_RATIO);

        layoutParams = mRightNameView.getLayoutParams();
        layoutParams.width = (int) (Design.DISPLAY_WIDTH * 0.5f);

        Design.updateTextFont(mRightNameView, Design.FONT_BOLD36);
        mRightNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mRightNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = nameMargin;
        marginLayoutParams.rightMargin = nameMargin;

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.getMainStyle());
        mConfirmView.setBackground(confirmViewBackground);
    }
}

