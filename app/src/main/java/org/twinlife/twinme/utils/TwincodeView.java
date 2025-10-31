/*
 *  Copyright (c) 2021 twinlife SA.
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
public class TwincodeView extends PercentRelativeLayout {
    private static final String LOG_TAG = "TwincodeView";
    private static final boolean DEBUG = false;

    private CircularImageView mAvatarView;
    private TextView mNameView;
    private ImageView mQRCodeView;
    private TextView mTwincodeView;
    private TextView mMessageView;

    public TwincodeView(Context context) {
        super(context);
    }

    public TwincodeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view;
            view = inflater.inflate(R.layout.fullscreen_qrcode_twincode_view, (ViewGroup) getParent());
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            addView(view);

            initViews();
        }
    }

    public TwincodeView(Context context, AttributeSet attrs, int defStyle) {
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

        setBackgroundColor(Color.WHITE);

        RoundedView avatarRoundedView = findViewById(R.id.fullscreen_qrcode_twincode_view_avatar_rounded_view);
        avatarRoundedView.setColor(Design.POPUP_BACKGROUND_COLOR);

        mAvatarView = findViewById(R.id.fullscreen_qrcode_twincode_view_avatar_view);

        View profileView = findViewById(R.id.fullscreen_qrcode_twincode_view_profile_view);
        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable profileViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        profileViewBackground.getPaint().setColor(Design.getMainStyle());
        profileView.setBackground(profileViewBackground);

        mNameView = findViewById(R.id.fullscreen_qrcode_twincode_view_name_view);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM34);
        mNameView.setTextColor(Color.WHITE);

        View qrcodeContainerView = findViewById(R.id.fullscreen_qrcode_twincode_view_qrcode_container_view);
        ShapeDrawable qrcodeContainerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        qrcodeContainerViewBackground.getPaint().setColor(Color.WHITE);
        qrcodeContainerView.setBackground(qrcodeContainerViewBackground);

        mQRCodeView = findViewById(R.id.fullscreen_qrcode_twincode_view_qrcode_view);

        mTwincodeView = findViewById(R.id.fullscreen_qrcode_twincode_view_twincode_view);
        Design.updateTextFont(mTwincodeView, Design.FONT_MEDIUM34);
        mTwincodeView.setTextColor(Color.WHITE);

        mMessageView = findViewById(R.id.fullscreen_qrcode_twincode_view_information_view);
        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM28);
        mMessageView.setTextColor(Color.BLACK);
    }
}