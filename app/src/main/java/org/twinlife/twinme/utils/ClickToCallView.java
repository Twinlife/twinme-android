/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;

@SuppressWarnings("deprecation")
public class ClickToCallView extends PercentRelativeLayout {
    private static final String LOG_TAG = "ClickToCallView";
    private static final boolean DEBUG = false;

    private static final int DESIGN_INVITATION_VIEW_COLOR = Color.rgb(69, 69, 69);
    private static final int DESIGN_INVITATION_VIEW_BORDER_COLOR = Color.argb(120, 151, 151, 151);
    private static final int DESIGN_HEADER_COLOR = Color.rgb(102, 102, 102);
    private static final int DESIGN_NAME_VIEW_COLOR = Color.rgb(81, 79, 79);
    private static final int DESIGN_RED_VIEW_COLOR = Color.rgb(191, 60, 52);
    private static final int DESIGN_YELLOW_VIEW_COLOR = Color.rgb(255, 207, 8);
    private static final int DESIGN_GREEN_VIEW_COLOR = Color.rgb(23, 196, 164);

    private static final float DESIGN_HEADER_VIEW_HEIGHT = 90f;
    private static final float DESIGN_AVATAR_VIEW_HEIGHT = 52f;

    private static final float DESIGN_ROUNDED_VIEW_MARGIN = 20f;
    private static final float DESIGN_AVATAR_VIEW_MARGIN = 24f;

    public static final float DESIGN_CONTAINER_RADIUS = 6f;
    private static final float DESIGN_CONTAINER_BORDER = 3f;
    
    private CircularImageView mAvatarView;
    private TextView mNameView;
    private ImageView mQRCodeView;
    private TextView mTwincodeView;
    private TextView mMessageView;

    public ClickToCallView(Context context) {
        super(context);
    }

    public ClickToCallView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view;
            view = inflater.inflate(R.layout.click_to_call_view, (ViewGroup) getParent());
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            addView(view);

            initViews();
        }
    }

    public ClickToCallView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setTwincodeInformation(Context context, String name, Bitmap avatar, Bitmap qrcode, String twincode, String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setTwincodeInformation: " + name + " avatar: " + avatar + " qrcode: " + qrcode + " twincode: " + twincode + " message: " + message);
        }

        mAvatarView.setImage(context, null, new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
        mNameView.setText(name);
        mQRCodeView.setImageBitmap(qrcode);
        mTwincodeView.setText(twincode);
        mMessageView.setText(message);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setBackgroundColor(Color.BLACK);

        View invitationView = findViewById(R.id.click_to_call_view_invitation_view);
        float radius = DESIGN_CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable invitationViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        invitationViewBackground.getPaint().setColor(DESIGN_INVITATION_VIEW_COLOR);

        ShapeDrawable invitationViewBorder = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        invitationViewBorder.getPaint().setColor(DESIGN_INVITATION_VIEW_BORDER_COLOR);
        invitationViewBorder.getPaint().setStyle(Paint.Style.STROKE);
        invitationViewBorder.getPaint().setStrokeWidth(DESIGN_CONTAINER_BORDER);

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{invitationViewBackground, invitationViewBorder});
        invitationView.setBackground(layerDrawable);

        View headerView = findViewById(R.id.click_to_call_view_header_view);
        outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable headerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        headerViewBackground.getPaint().setColor(DESIGN_HEADER_COLOR);

        ShapeDrawable headerViewBorder = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        headerViewBorder.getPaint().setColor(DESIGN_INVITATION_VIEW_BORDER_COLOR);
        headerViewBorder.getPaint().setStyle(Paint.Style.STROKE);
        headerViewBorder.getPaint().setStrokeWidth(DESIGN_CONTAINER_BORDER);

        LayerDrawable headerLayerDrawable = new LayerDrawable(new Drawable[]{headerViewBackground, headerViewBorder});
        headerView.setBackground(headerLayerDrawable);

        ViewGroup.LayoutParams layoutParams = headerView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_HEADER_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        RoundedView redRoundedView = findViewById(R.id.click_to_call_view_red_rounded_view);
        redRoundedView.setColor(DESIGN_RED_VIEW_COLOR);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) redRoundedView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ROUNDED_VIEW_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_ROUNDED_VIEW_MARGIN * Design.WIDTH_RATIO));

        RoundedView yellowRoundedView = findViewById(R.id.click_to_call_view_yellow_rounded_view);
        yellowRoundedView.setColor(DESIGN_YELLOW_VIEW_COLOR);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) yellowRoundedView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ROUNDED_VIEW_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_ROUNDED_VIEW_MARGIN * Design.WIDTH_RATIO));

        RoundedView greenRoundedView = findViewById(R.id.click_to_call_view_green_rounded_view);
        greenRoundedView.setColor(DESIGN_GREEN_VIEW_COLOR);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) greenRoundedView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ROUNDED_VIEW_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_ROUNDED_VIEW_MARGIN * Design.WIDTH_RATIO));

        View nameContainerView = findViewById(R.id.click_to_call_view_name_container_view);
        radius = (DESIGN_AVATAR_VIEW_HEIGHT * 0.5f) * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable nameViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        nameViewBackground.getPaint().setColor(DESIGN_NAME_VIEW_COLOR);
        nameContainerView.setBackground(nameViewBackground);

        int rightMargin = (int) ((DESIGN_AVATAR_VIEW_HEIGHT * Design.HEIGHT_RATIO) + ((DESIGN_ROUNDED_VIEW_MARGIN + DESIGN_AVATAR_VIEW_MARGIN) * Design.WIDTH_RATIO));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) nameContainerView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ROUNDED_VIEW_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = rightMargin;
        marginLayoutParams.setMarginStart((int) (DESIGN_ROUNDED_VIEW_MARGIN * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd(rightMargin);

        mNameView = findViewById(R.id.click_to_call_view_name_view);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM32);
        mNameView.setTextColor(Color.WHITE);

        mAvatarView = findViewById(R.id.click_to_call_view_avatar_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mAvatarView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_AVATAR_VIEW_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginEnd((int) (DESIGN_AVATAR_VIEW_MARGIN * Design.WIDTH_RATIO));

        View qrCodeContainerView = findViewById(R.id.click_to_call_view_qrcode_container_view);
        radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable qrCodeViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        qrCodeViewBackground.getPaint().setColor(Color.WHITE);
        qrCodeContainerView.setBackground(qrCodeViewBackground);

        mQRCodeView = findViewById(R.id.click_to_call_view_qrcode_view);

        mTwincodeView = findViewById(R.id.click_to_call_view_twincode_view);
        Design.updateTextFont(mTwincodeView, Design.FONT_MEDIUM28);
        mTwincodeView.setTextColor(Color.WHITE);

        mMessageView = findViewById(R.id.click_to_call_view_information_view);
        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM28);
        mMessageView.setTextColor(Color.WHITE);
    }
}