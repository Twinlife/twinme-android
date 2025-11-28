/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.AbstractConfirmView;

public class SpaceActionConfirmView extends AbstractConfirmView {
    private static final String LOG_TAG = "SecretSpaceConfirmView";
    private static final boolean DEBUG = false;

    private static final int BULLET_COLOR = Color.rgb(213, 213, 213);
    private static final float CONTAINER_RADIUS = 14f;
    private static final float NAME_RADIUS = 12f;

    private static final int DESIGN_ICON_VIEW_SIZE = 82;
    private static final int DESIGN_ICON_IMAGE_VIEW_HEIGHT = 38;

    private View mNoAvatarView;
    private TextView mSpaceNameView;

    public SpaceActionConfirmView(Context context) {
        super(context);
    }

    public SpaceActionConfirmView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.space_action_confirm_view, this, true);
        initViews();
    }

    public  void setSpaceName(String spaceName, String spaceStyle) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setSpaceName: " + spaceName + " spaceStyle:" + spaceStyle);
        }

        if (spaceName != null && !spaceName.isEmpty()) {
            mSpaceNameView.setText(spaceName.substring(0, 1).toUpperCase());
        }

        int color;
        if (spaceStyle != null) {
            color = Design.getDefaultColor(spaceStyle);
        } else {
            color = Design.getMainStyle();
        }

        float radius = NAME_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable textDrawableBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        textDrawableBackground.getPaint().setColor(color);
        mSpaceNameView.setBackground(textDrawableBackground);
    }

    @Override
    public void setAvatar(Bitmap avatar, boolean isDefaultGroupAvatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setAvatar");
        }

        if (mAvatarView == null) {
            return;
        }

        mDefaultGroupAvatar = isDefaultGroupAvatar;

        if (avatar != null) {
            mAvatarView.setVisibility(VISIBLE);
            mAvatarView.setImageBitmap(avatar);
            mNoAvatarView.setVisibility(INVISIBLE);
        } else {
            mAvatarView.setVisibility(INVISIBLE);
            mNoAvatarView.setVisibility(VISIBLE);
        }
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.space_action_confirm_view_overlay_view);
        mActionView = findViewById(R.id.space_action_confirm_view_action_view);
        mSlideMarkView = findViewById(R.id.space_action_confirm_view_slide_mark_view);
        mNoAvatarView = findViewById(R.id.space_action_confirm_view_no_avatar_view);
        mSpaceNameView = findViewById(R.id.space_action_confirm_view_no_avatar_text_view);
        mAvatarView = findViewById(R.id.space_action_confirm_view_avatar_view);
        mIconView = findViewById(R.id.space_action_confirm_view_icon_view);
        mIconImageView = findViewById(R.id.space_action_confirm_view_icon_image_view);
        mBulletView = findViewById(R.id.space_action_confirm_view_bullet_view);
        mTitleView = findViewById(R.id.space_action_confirm_view_title_view);
        mMessageView = findViewById(R.id.space_action_confirm_view_message_view);
        mConfirmView = findViewById(R.id.space_action_confirm_view_confirm_view);
        mConfirmTextView = findViewById(R.id.space_action_confirm_view_confirm_text_view);
        mCancelView = findViewById(R.id.space_action_confirm_view_cancel_view);
        mCancelTextView = findViewById(R.id.space_action_confirm_view_cancel_text_view);

        super.initViews();

        float radius = CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable drawableBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        drawableBackground.getPaint().setColor(Color.WHITE);
        mNoAvatarView.setBackground(drawableBackground);

        ViewGroup.LayoutParams layoutParams = mNoAvatarView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO);

        MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mNoAvatarView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_AVATAR_MARGIN * Design.HEIGHT_RATIO);

        mSpaceNameView.setTypeface(Design.FONT_BOLD68.typeface);
        mSpaceNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD68.size);
        mSpaceNameView.setTextColor(Color.WHITE);

        layoutParams = mIconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO);

        GradientDrawable iconBackgroundDrawable = new GradientDrawable();
        iconBackgroundDrawable.setColor(BULLET_COLOR);
        iconBackgroundDrawable.setCornerRadius((int) ((DESIGN_ICON_VIEW_SIZE * Design.HEIGHT_RATIO) * 0.5));
        iconBackgroundDrawable.setStroke(8, Color.WHITE);
        mIconView.setBackground(iconBackgroundDrawable);

        layoutParams = mIconImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ICON_IMAGE_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        GradientDrawable bulletBackgroundDrawable = new GradientDrawable();
        bulletBackgroundDrawable.setColor(BULLET_COLOR);
        bulletBackgroundDrawable.setCornerRadius((int) ((DESIGN_BULLET_VIEW_SIZE * Design.HEIGHT_RATIO) * 0.5));
        bulletBackgroundDrawable.setStroke(8, Color.WHITE);
        mBulletView.setBackground(bulletBackgroundDrawable);

        radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.getMainStyle());
        mConfirmView.setBackground(confirmViewBackground);
    }
}
