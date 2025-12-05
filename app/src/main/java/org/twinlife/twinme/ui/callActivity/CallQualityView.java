/*
 *  Copyright (c) 2022-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.AbstractConfirmView;

public class CallQualityView extends AbstractConfirmView {
    private static final String LOG_TAG = "CallQualityView";
    private static final boolean DEBUG = false;

    protected static final int DESIGN_STAR_SIZE = 100;
    protected static final int DESIGN_STAR_MARGIN = 80;

    public interface CallQualityObserver {
        void onSendCallQuality(int quality);
    }

    private ImageView mStarOneImageView;
    private ImageView mStarTwoImageView;
    private ImageView mStarThreeImageView;
    private ImageView mStarFourImageView;

    private int mCallQuality = 4;

    private CallQualityObserver mCallQualityObserver;

    public CallQualityView(Context context) {

        super(context);
    }

    public CallQualityView(Context context, AttributeSet attrs) {

        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.call_quality_view, this, true);
        initViews();
    }

    public void setCallQualityObserver(CallQualityObserver observer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCallQualityObserver: " + observer);
        }

        mCallQualityObserver = observer;
    }

    @Override
    protected void onConfirmClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConfirmClick");
        }

        if (mCallQualityObserver != null) {
            mCallQualityObserver.onSendCallQuality(mCallQuality);
        }
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = findViewById(R.id.call_quality_view_overlay_view);
        mActionView = findViewById(R.id.call_quality_view_action_view);
        mSlideMarkView = findViewById(R.id.call_quality_view_slide_mark_view);
        mTitleView = findViewById(R.id.call_quality_view_title_view);
        mMessageView = findViewById(R.id.call_quality_view_message_view);
        mConfirmView = findViewById(R.id.call_quality_view_confirm_view);
        mConfirmTextView = findViewById(R.id.call_quality_view_confirm_text_view);
        mCancelView = findViewById(R.id.call_quality_view_cancel_view);
        mCancelTextView = findViewById(R.id.call_quality_view_cancel_text_view);

        super.initViews();

        View starContainer = findViewById(R.id.call_quality_view_stars_view);

        MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) starContainer.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_STAR_MARGIN * Design.HEIGHT_RATIO);

        int starSize = (int) (DESIGN_STAR_SIZE * Design.HEIGHT_RATIO);

        mStarOneImageView = findViewById(R.id.call_quality_view_star_one_image_view);

        ViewGroup.LayoutParams layoutParams = mStarOneImageView.getLayoutParams();
        layoutParams.width = starSize;
        layoutParams.height = starSize;

        mStarOneImageView.setOnClickListener(v -> {
            mCallQuality = 1;
            updateStars();
        });

        mStarTwoImageView = findViewById(R.id.call_quality_view_star_two_image_view);

        layoutParams = mStarTwoImageView.getLayoutParams();
        layoutParams.width = starSize;
        layoutParams.height = starSize;

        mStarTwoImageView.setOnClickListener(v -> {
            mCallQuality = 2;
            updateStars();
        });

        mStarThreeImageView = findViewById(R.id.call_quality_view_star_three_image_view);

        layoutParams = mStarThreeImageView.getLayoutParams();
        layoutParams.width = starSize;
        layoutParams.height = starSize;

        mStarThreeImageView.setOnClickListener(v -> {
            mCallQuality = 3;
            updateStars();
        });

        mStarFourImageView = findViewById(R.id.call_quality_view_star_four_image_view);

        layoutParams = mStarFourImageView.getLayoutParams();
        layoutParams.width = starSize;
        layoutParams.height = starSize;

        mStarFourImageView.setOnClickListener(v -> {
            mCallQuality = 4;
            updateStars();
        });

        updateStars();

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.getMainStyle());
        mConfirmView.setBackground(confirmViewBackground);
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
}
