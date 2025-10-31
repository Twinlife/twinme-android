/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumFeatureConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.CircularImageView;

import java.util.UUID;

public abstract class AbstractPreviewActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "AbstractPreviewActivity";
    private static final boolean DEBUG = false;

    protected static final float DESIGN_CONTENT_SEND_VIEW_HEIGHT = 80f;
    protected static final float DESIGN_EDIT_TEXT_WIDTH_INSET = 32f;
    protected static final float DESIGN_EDIT_TEXT_HEIGHT_INSET = 20f;
    protected static final float DESIGN_SEND_IMAGE_HEIGHT = 30f;
    protected static final float DESIGN_CLOSE_SIZE = 120f;
    protected static final float DESIGN_AVATAR_SIZE = 48f;
    protected static final float DESIGN_CERTIFIED_SIZE = 24f;
    protected static final float DESIGN_NAME_MARGIN = 12f;

    protected static final float DESIGN_THUMBNAIL_HEIGHT = 120f;
    protected static final float DESIGN_THUMBNAIL_MARGIN = 40f;

    private static final int EDIT_TEXT_BORDER_COLOR = Color.rgb(78, 78, 78);


    protected class TwinmeContextObserver extends TwinmeContext.DefaultObserver {
        @Override
        public void onTwinlifeReady() {
            AbstractPreviewActivity.this.onTwinlifeReady();
        }
    }

    protected CircularImageView mAvatarView;
    protected TextView mNameView;
    protected ImageView mCertifiedImageView;

    protected EditText mEditText;
    protected View mSendView;
    protected View mOverlayView;
    protected MenuSendOptionView mMenuSendOptionView;

    protected boolean mIsMenuSendOptionOpen = false;
    protected boolean mAllowCopy = true;

    @Nullable
    protected UUID mOriginatorId;
    @Nullable
    protected Originator mOriginator;
    protected String mContactName;
    protected Bitmap mContactAvatar;
    protected boolean mIsCertified = false;
    protected String mInitMessage;

    @Nullable
    protected Uri mSelectedUri;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();
    }

    public void closeMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeMenu");
        }

        if (mIsMenuSendOptionOpen) {
            mIsMenuSendOptionOpen = false;
            mMenuSendOptionView.setVisibility(View.INVISIBLE);
            mOverlayView.setVisibility(View.INVISIBLE);
            setStatusBarColor(Color.BLACK);
        }
    }

    @Override
    public void finish() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.finish();

        hideKeyboard();
    }

    abstract protected void updateViews();
    abstract protected  void send();

    protected void initViews() {

        View closeView = findViewById(R.id.preview_activity_close_view);
        closeView.setOnClickListener(v -> finish());

        ViewGroup.LayoutParams layoutParams = closeView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CLOSE_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_CLOSE_SIZE * Design.HEIGHT_RATIO);

        ImageView closeImageView = findViewById(R.id.preview_activity_close_icon_view);
        closeImageView.setColorFilter(Color.WHITE);

        View headerView = findViewById(R.id.preview_activity_header_view);

        layoutParams = headerView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CLOSE_SIZE * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) headerView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_CLOSE_SIZE * Design.HEIGHT_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CLOSE_SIZE * Design.HEIGHT_RATIO);

        mAvatarView = findViewById(R.id.preview_activity_avatar_view);

        layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        mCertifiedImageView = findViewById(R.id.preview_activity_certified_view);

        layoutParams = mCertifiedImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CERTIFIED_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_CERTIFIED_SIZE * Design.HEIGHT_RATIO);

        mNameView = findViewById(R.id.preview_activity_name_view);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM34);
        mNameView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_NAME_MARGIN * Design.WIDTH_RATIO);

        View contentSendView = findViewById(R.id.preview_activity_content_send_view);
        contentSendView.setMinimumHeight((int) (DESIGN_CONTENT_SEND_VIEW_HEIGHT * Design.HEIGHT_RATIO));
        contentSendView.setBackgroundColor(Color.TRANSPARENT);

        mEditText = findViewById(R.id.preview_activity_edit_text);
        Design.updateTextFont(mEditText, Design.FONT_REGULAR32);
        mEditText.setTextColor(Color.WHITE);
        mEditText.setHintTextColor(Design.PLACEHOLDER_COLOR);

        float radius =  (DESIGN_CONTENT_SEND_VIEW_HEIGHT * Design.HEIGHT_RATIO) * 0.5f;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};


        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.BLACK);
        gradientDrawable.setCornerRadius(radius);
        gradientDrawable.setStroke(3, EDIT_TEXT_BORDER_COLOR);

        mEditText.setBackground(gradientDrawable);

        mEditText.setPadding((int) (DESIGN_EDIT_TEXT_WIDTH_INSET * Design.WIDTH_RATIO), (int) (DESIGN_EDIT_TEXT_HEIGHT_INSET * Design.HEIGHT_RATIO), (int) (DESIGN_EDIT_TEXT_WIDTH_INSET * Design.WIDTH_RATIO), (int) (DESIGN_EDIT_TEXT_HEIGHT_INSET * Design.HEIGHT_RATIO));

        mSendView = findViewById(R.id.preview_activity_send_clickable_view);
        mSendView.setOnClickListener(v -> onSendClick());

        mSendView.setOnLongClickListener(v -> {
            onSendLongClick();
            return true;
        });

        layoutParams = mSendView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CONTENT_SEND_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        View sendRoundedView = findViewById(R.id.preview_activity_send_rounded_view);

        layoutParams = sendRoundedView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CONTENT_SEND_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_CONTENT_SEND_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        ShapeDrawable sendBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        sendBackground.getPaint().setColor(Design.getMainStyle());
        sendRoundedView.setBackground(sendBackground);

        ImageView sendImageView = findViewById(R.id.preview_activity_send_image_view);
        sendImageView.setColorFilter(Color.WHITE);

        layoutParams = sendImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SEND_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        mOverlayView = findViewById(R.id.preview_activity_overlay_view);
        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setOnClickListener(view -> closeMenu());

        mMenuSendOptionView = findViewById(R.id.preview_activity_menu_send_option_view);
        mMenuSendOptionView.setVisibility(View.INVISIBLE);
        mMenuSendOptionView.setForceDarkMode(true);

        MenuSendOptionView.Observer menuSendOptionObserver = new MenuSendOptionView.Observer() {
            @Override
            public void onCloseMenuAnimationEnd() {

            }

            @Override
            public void onAllowEphemeralClick() {
                onPremiumFeatureClick();
            }

            @Override
            public void onSendFromMenuOptionClick(boolean allowCopy, boolean allowEphemeral, int timeout) {
                mAllowCopy = allowCopy;
                onSendClick();
            }
        };

        mMenuSendOptionView.setOnMenuSendOptionObserver(this, menuSendOptionObserver);

        if (mInitMessage != null && !mInitMessage.isEmpty()) {
            mEditText.setText(mInitMessage);
        }

        updateViews();
    }

    //
    // Private methods
    //

    private void onSendClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendClick");
        }

        send();
    }

    private void onSendLongClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendLongClick");
        }

        if (!mIsMenuSendOptionOpen) {

            hideKeyboard();
            
            mIsMenuSendOptionOpen = true;
            mMenuSendOptionView.setVisibility(View.VISIBLE);
            mOverlayView.setVisibility(View.VISIBLE);
            mMenuSendOptionView.openMenu(mAllowCopy);

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Color.BLACK);
            setStatusBarColor(color, Color.rgb(72,72,72));
        }
    }

    protected void onPremiumFeatureClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPremiumFeatureClick");
        }

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.preview_activity_layout);

        PremiumFeatureConfirmView premiumFeatureConfirmView = new PremiumFeatureConfirmView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        premiumFeatureConfirmView.setLayoutParams(layoutParams);
        premiumFeatureConfirmView.setForceDarkMode(true);
        premiumFeatureConfirmView.initWithPremiumFeature(new UIPremiumFeature(this, UIPremiumFeature.FeatureType.PRIVACY));

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                premiumFeatureConfirmView.redirectStore();
            }

            @Override
            public void onCancelClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                percentRelativeLayout.removeView(premiumFeatureConfirmView);

                if (!mIsMenuSendOptionOpen) {
                    setStatusBarColor();
                }
            }
        };
        premiumFeatureConfirmView.setObserver(observer);

        percentRelativeLayout.addView(premiumFeatureConfirmView);
        premiumFeatureConfirmView.show();
        premiumFeatureConfirmView.hideOverlay();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Color.BLACK);
        setStatusBarColor(color, Color.rgb(72,72,72));
    }

    private void hideKeyboard() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideKeyboard");
        }

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && mEditText != null) {
            inputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
    }

    protected abstract void onTwinlifeReady();
}
