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
import org.twinlife.twinme.skin.Design;

public class ProxyView extends PercentRelativeLayout {
    private static final String LOG_TAG = "ProxyView";
    private static final boolean DEBUG = false;

    private ImageView mQRCodeView;
    private TextView mProxyView;
    private TextView mMessageView;

    public ProxyView(Context context) {
        super(context);
    }

    public ProxyView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view;
            view = inflater.inflate(R.layout.fullscreen_qrcode_proxy_view, (ViewGroup) getParent());
            view.setLayoutParams(new PercentRelativeLayout.LayoutParams(PercentRelativeLayout.LayoutParams.MATCH_PARENT, PercentRelativeLayout.LayoutParams.MATCH_PARENT));
            addView(view);

            initViews();
        }
    }

    public ProxyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setInformation(Bitmap qrcode, String proxy, String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setInformation: " + qrcode + " proxy: " + proxy + " message: " + message);
        }

        mQRCodeView.setImageBitmap(qrcode);
        mProxyView.setText(proxy);
        mMessageView.setText(message);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setBackgroundColor(Color.WHITE);

        View containerView = findViewById(R.id.fullscreen_qrcode_proxy_container_view);
        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable profileViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        profileViewBackground.getPaint().setColor(Design.getMainStyle());
        containerView.setBackground(profileViewBackground);

        View qrcodeContainerView = findViewById(R.id.fullscreen_qrcode_proxy_view_qrcode_container_view);
        ShapeDrawable qrcodeContainerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        qrcodeContainerViewBackground.getPaint().setColor(Color.WHITE);
        qrcodeContainerView.setBackground(qrcodeContainerViewBackground);

        mQRCodeView = findViewById(R.id.fullscreen_qrcode_proxy_view_qrcode_view);

        mProxyView = findViewById(R.id.fullscreen_qrcode_proxy_view_url_view);
        Design.updateTextFont(mProxyView, Design.FONT_MEDIUM34);
        mProxyView.setTextColor(Color.WHITE);

        mMessageView = findViewById(R.id.fullscreen_qrcode_proxy_view_information_view);
        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM28);
        mMessageView.setTextColor(Color.BLACK);
    }
}