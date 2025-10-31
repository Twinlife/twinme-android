/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.content.Context;
import android.content.res.Resources;
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

public class CallQualityView extends PercentRelativeLayout {
    private static final String LOG_TAG = "CallQualityView";
    private static final boolean DEBUG = false;

    public interface CallQualityListener {

        void onSendCallQuality(int quality);

        void onCancelCallQuality();
    }

    private static final int DESIGN_CONTENT_VIEW_WIDTH = 580;
    private static final int DESIGN_CONTENT_VIEW_HEIGHT = 740;

    private ImageView mStarOneImageView;
    private ImageView mStarTwoImageView;
    private ImageView mStarThreeImageView;
    private ImageView mStarFourImageView;

    private int mCallQuality = 4;

    private CallQualityListener mCallQualityListener;

    public CallQualityView(Context context) {

        super(context);
    }

    public CallQualityView(Context context, AttributeSet attrs) {

        super(context, attrs);

        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.call_quality_view, (ViewGroup) getParent());
            //noinspection deprecation
            view.setLayoutParams(new PercentRelativeLayout.LayoutParams(PercentRelativeLayout.LayoutParams.MATCH_PARENT, PercentRelativeLayout.LayoutParams.MATCH_PARENT));
            addView(view);
            initViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CallQualityView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    public void setCallQualityListener(CallQualityListener callQualityListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCallQualityListener: " + callQualityListener);
        }

        mCallQualityListener = callQualityListener;
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setBackgroundColor(Design.OVERLAY_VIEW_COLOR);

        View contentView = findViewById(R.id.call_quality_activity_content_view);

        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CONTENT_VIEW_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_CONTENT_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        float radius = Design.POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable popupViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        popupViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        contentView.setBackground(popupViewBackground);

        mStarOneImageView = findViewById(R.id.call_quality_star_one_image_view);
        mStarOneImageView.setOnClickListener(v -> {
            mCallQuality = 1;
            updateStars();
        });

        mStarTwoImageView = findViewById(R.id.call_quality_star_two_image_view);
        mStarTwoImageView.setOnClickListener(v -> {
            mCallQuality = 2;
            updateStars();
        });

        mStarThreeImageView = findViewById(R.id.call_quality_star_three_image_view);
        mStarThreeImageView.setOnClickListener(v -> {
            mCallQuality = 3;
            updateStars();
        });

        mStarFourImageView = findViewById(R.id.call_quality_star_four_image_view);
        mStarFourImageView.setOnClickListener(v -> {
            mCallQuality = 4;
            updateStars();
        });

        TextView titleTextView = findViewById(R.id.call_quality_activity_title_view);
        Design.updateTextFont(titleTextView, Design.FONT_MEDIUM34);
        titleTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        TextView messageTextView = findViewById(R.id.call_quality_activity_message_view);
        Design.updateTextFont(messageTextView, Design.FONT_MEDIUM32);
        messageTextView.setTextColor(Design.FONT_COLOR_DESCRIPTION);

        View sendClickableView = findViewById(R.id.call_quality_activity_send_view);
        sendClickableView.setOnClickListener(v -> onSendClick());

        ShapeDrawable sendViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        sendViewBackground.getPaint().setColor(Color.parseColor(Design.DEFAULT_COLOR));
        sendClickableView.setBackground(sendViewBackground);

        layoutParams = sendClickableView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView sendTextView = findViewById(R.id.call_quality_activity_send_text_view);
        Design.updateTextFont(sendTextView, Design.FONT_BOLD28);
        sendTextView.setTextColor(Color.WHITE);

        View closeView = findViewById(R.id.call_quality_activity_close_view);
        closeView.setOnClickListener(view -> onCloseClick());

        updateStars();
    }

    private void updateStars() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateStars");
        }

        if (mCallQuality > 0) {
            mStarOneImageView.setImageResource(R.drawable.star_red);
        } else {
            mStarOneImageView.setImageResource(R.drawable.star_grey);
        }

        if (mCallQuality > 1) {
            mStarTwoImageView.setImageResource(R.drawable.star_red);
        } else {
            mStarTwoImageView.setImageResource(R.drawable.star_grey);
        }

        if (mCallQuality > 2) {
            mStarThreeImageView.setImageResource(R.drawable.star_red);
        } else {
            mStarThreeImageView.setImageResource(R.drawable.star_grey);
        }

        if (mCallQuality > 3) {
            mStarFourImageView.setImageResource(R.drawable.star_red);
        } else {
            mStarFourImageView.setImageResource(R.drawable.star_grey);
        }
    }

    private void onCloseClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseClick");
        }

        mCallQualityListener.onCancelCallQuality();
    }

    private void onSendClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendClick");
        }

        mCallQualityListener.onSendCallQuality(mCallQuality);
    }
}
