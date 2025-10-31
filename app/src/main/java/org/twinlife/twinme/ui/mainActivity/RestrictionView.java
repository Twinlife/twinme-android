/*
 *  Copyright (c) 2022-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity;

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
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.util.Logger;
import org.twinlife.twinme.skin.Design;

public class RestrictionView extends PercentRelativeLayout {
    private static final String LOG_TAG = "RestrictionView";
    private static final boolean DEBUG = false;

    private static final int DESIGN_BACKGROUND_COLOR = Color.argb(255, 56, 56, 56);
    private static final int DESIGN_ACCESSORY_COLOR = Color.argb(255, 97, 97, 97);
    private static final int DESIGN_MESSAGE_COLOR = Color.argb(255, 195, 195, 195);

    private static final int DESIGN_TITLE_MARGIN = 24;
    private static final int DESIGN_MESSAGE_MARGIN = 16;
    private static final int DESIGN_NOTIFICATION_HEIGHT = 40;
    private static final int DESIGN_ACCESSORY_SIZE = 60;
    private static final int DESIGN_ACCESSORY_IMAGE_SIZE = 22;
    private static final int DESIGN_ACCESSORY_RADIUS = 8;

    private View mRestrictionContentView;
    private TextView mLowUsageView;
    private TextView mTitleTextView;
    private TextView mMessageTextView;

    private boolean mLowUsage = false;

    public RestrictionView(Context context) {

        super(context);
    }

    public RestrictionView(Context context, AttributeSet attrs) {

        super(context, attrs);

        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.restriction_view, (ViewGroup) getParent());
            //noinspection deprecation
            view.setLayoutParams(new PercentRelativeLayout.LayoutParams(PercentRelativeLayout.LayoutParams.MATCH_PARENT, PercentRelativeLayout.LayoutParams.MATCH_PARENT));
            addView(view);
            initViews();
        } catch (Exception e) {
            if (Logger.ERROR) {
                Logger.error(LOG_TAG, "exception", e);
            }
        }
    }

    public RestrictionView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    public void updateView(boolean backgroundRestricted, boolean networkRestricted, boolean lowUsage, boolean notificationDisabled) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateView: " + backgroundRestricted + " lowUsage: " + lowUsage + " notificationDisabled: " + notificationDisabled);
        }

        mLowUsage = lowUsage;

        // Order of checks must be the same as in QualityOfServiceActivity.onPermissionsClick().
        if (notificationDisabled) {
            mMessageTextView.setText(getResources().getString(R.string.quality_of_service_activity_warning_receive_notification));
        } else if (networkRestricted) {
            mMessageTextView.setText(getResources().getString(R.string.quality_of_service_activity_warning_background_data));
        } else {
            mMessageTextView.setText(getResources().getString(R.string.quality_of_service_activity_warning_run_background));
        }

        if (lowUsage) {
            mLowUsageView.setVisibility(VISIBLE);
            mRestrictionContentView.setVisibility(GONE);
        } else {
            mLowUsageView.setVisibility(GONE);
            mRestrictionContentView.setVisibility(VISIBLE);
        }

        ViewTreeObserver viewTreeObserver = mMessageTextView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = mMessageTextView.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalLayoutListener(this);

                updateView();
            }
        });
    }

    private void updateView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateView");
        }

        float titleHeight = mTitleTextView.getMeasuredHeight();
        float messageHeight = mMessageTextView.getMeasuredHeight();
        float lowUsageHeight = mLowUsageView.getMeasuredHeight();

        float height;

        if (mLowUsage) {
            height = lowUsageHeight + (DESIGN_TITLE_MARGIN * 2 * Design.HEIGHT_RATIO);
        } else {
            height = titleHeight + messageHeight + ((DESIGN_TITLE_MARGIN + (DESIGN_MESSAGE_MARGIN * 2)) * Design.HEIGHT_RATIO);
        }

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = (int) height;
        setLayoutParams(layoutParams);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setBackgroundColor(Color.TRANSPARENT);

        View contentView = findViewById(R.id.restriction_content_view);

        float radius = Design.POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable popupViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        popupViewBackground.getPaint().setColor(DESIGN_BACKGROUND_COLOR);
        contentView.setBackground(popupViewBackground);

        mRestrictionContentView = findViewById(R.id.restriction_content_restriction_view);

        View headerView = findViewById(R.id.restriction_header_view);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) headerView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);

        ImageView notificationView = findViewById(R.id.restriction_content_image_view);
        ViewGroup.LayoutParams layoutParams = notificationView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_NOTIFICATION_HEIGHT * Design.HEIGHT_RATIO);

        mTitleTextView = findViewById(R.id.restriction_content_title_view);
        Design.updateTextFont(mTitleTextView, Design.FONT_MEDIUM30);
        mTitleTextView.setTextColor(Color.WHITE);

        mMessageTextView = findViewById(R.id.restriction_content_message_view);
        Design.updateTextFont(mMessageTextView, Design.FONT_MEDIUM26);
        mMessageTextView.setTextColor(DESIGN_MESSAGE_COLOR);

        mLowUsageView = findViewById(R.id.restriction_low_usage_view);
        Design.updateTextFont(mLowUsageView, Design.FONT_MEDIUM30);
        mLowUsageView.setTextColor(Color.WHITE);

        marginLayoutParams = (MarginLayoutParams) mLowUsageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_TITLE_MARGIN * Design.HEIGHT_RATIO);

        View footerView = findViewById(R.id.restriction_footer_view);

        marginLayoutParams = (MarginLayoutParams) footerView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_MESSAGE_MARGIN * Design.HEIGHT_RATIO);

        View accessoryView = findViewById(R.id.restriction_accessory_view);

        radius = DESIGN_ACCESSORY_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable accessoryViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        accessoryViewBackground.getPaint().setColor(DESIGN_ACCESSORY_COLOR);
        accessoryView.setBackground(accessoryViewBackground);

        layoutParams = accessoryView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ACCESSORY_SIZE * Design.HEIGHT_RATIO);

        ImageView accessoryImageView = findViewById(R.id.restriction_accessory_image_view);
        accessoryImageView.setColorFilter(Color.WHITE);

        layoutParams = accessoryImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ACCESSORY_IMAGE_SIZE * Design.HEIGHT_RATIO);
    }
}