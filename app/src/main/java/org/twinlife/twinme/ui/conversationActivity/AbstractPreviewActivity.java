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
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.privacyActivity.UITimeout;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.settingsActivity.MenuSelectValueView;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.DefaultConfirmView;
import org.twinlife.twinme.utils.RoundedView;

import java.util.UUID;

public abstract class AbstractPreviewActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "AbstractPreviewActivity";
    private static final boolean DEBUG = false;

    private static final long WARNING_ORIGINAL_SIZE = 1024 * 1024 * 10;

    protected static final float DESIGN_CONTENT_SEND_VIEW_HEIGHT = 80f;
    protected static final float DESIGN_EDIT_TEXT_WIDTH_INSET = 32f;
    protected static final float DESIGN_EDIT_TEXT_HEIGHT_INSET = 20f;
    protected static final float DESIGN_SEND_IMAGE_HEIGHT = 30f;
    protected static final float DESIGN_HEADER_SIZE = 120f;
    protected static final float DESIGN_HEADER_MARGIN = 12f;
    protected static final float DESIGN_HEADER_ACTION_SIZE = 80f;
    protected static final float DESIGN_CLOSE_SIZE = 32f;
    protected static final float DESIGN_QUALITY_WIDTH = 46f;
    protected static final float DESIGN_QUALITY_HEIGHT = 40f;
    protected static final float DESIGN_AVATAR_SIZE = 48f;
    protected static final float DESIGN_CERTIFIED_SIZE = 24f;
    protected static final float DESIGN_SEND_VIEW_MARGIN = 60f;

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
    protected View mQualityView;
    private  ImageView mQualityImageView;

    protected EditText mEditText;
    protected View mSendView;
    protected View mOverlayView;
    protected MenuSendOptionView mMenuSendOptionView;
    protected boolean mIsMenuSendOptionOpen = false;
    protected boolean mAllowCopy = true;
    protected boolean mAllowEphemeralMessage = false;
    protected long mExpireTimeout = 0;
    protected boolean mPreviewStartWithMedia = false;
    protected boolean mShareLocation = false;
    protected boolean mIsQualityMediaOriginal = false;

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

    protected long totalFilesSize() {

        return 0;
    }

    protected void initViews() {

        View headerView = findViewById(R.id.preview_activity_header_view);

        ViewGroup.LayoutParams layoutParams = headerView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_HEADER_SIZE * Design.HEIGHT_RATIO);

        View closeView = findViewById(R.id.preview_activity_close_view);
        closeView.setOnClickListener(v -> finish());

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) closeView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HEADER_MARGIN * Design.WIDTH_RATIO);

        RoundedView closeRoundedView = findViewById(R.id.preview_activity_close_rounded_view);
        closeRoundedView.setColor(Design.BACK_VIEW_COLOR);

        layoutParams = closeRoundedView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_HEADER_ACTION_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_HEADER_ACTION_SIZE * Design.HEIGHT_RATIO);

        ImageView closeImageView = findViewById(R.id.preview_activity_close_icon_view);
        closeImageView.setColorFilter(Color.WHITE);

        layoutParams = closeImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CLOSE_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_CLOSE_SIZE * Design.HEIGHT_RATIO);

        View contactView = findViewById(R.id.preview_activity_contact_view);

        layoutParams = contactView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_HEADER_ACTION_SIZE * Design.HEIGHT_RATIO);

        float radius =  (DESIGN_HEADER_ACTION_SIZE * Design.HEIGHT_RATIO) * 0.5f;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable infoBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        infoBackground.getPaint().setColor(Design.BACK_VIEW_COLOR);
        contactView.setBackground(infoBackground);

        mAvatarView = findViewById(R.id.preview_activity_avatar_view);

        layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mAvatarView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HEADER_MARGIN * Design.WIDTH_RATIO);

        mCertifiedImageView = findViewById(R.id.preview_activity_certified_view);

        layoutParams = mCertifiedImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CERTIFIED_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_CERTIFIED_SIZE * Design.HEIGHT_RATIO);

        mNameView = findViewById(R.id.preview_activity_name_view);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM34);
        mNameView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HEADER_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HEADER_MARGIN * Design.WIDTH_RATIO);

        mQualityView = findViewById(R.id.preview_activity_quality_view);
        mQualityView.setOnClickListener(v -> onQualityClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mQualityView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_HEADER_MARGIN * Design.WIDTH_RATIO);

        RoundedView qualityRoundedView = findViewById(R.id.preview_activity_quality_rounded_view);
        qualityRoundedView.setColor(Design.BACK_VIEW_COLOR);

        layoutParams = qualityRoundedView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_HEADER_ACTION_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_HEADER_ACTION_SIZE * Design.HEIGHT_RATIO);

        mQualityImageView = findViewById(R.id.preview_activity_quality_image_view);
        mQualityImageView.setColorFilter(Color.WHITE);

        layoutParams = mQualityImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_QUALITY_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_QUALITY_HEIGHT * Design.HEIGHT_RATIO);

        if (!mIsQualityMediaOriginal) {
            mQualityImageView.setImageResource(R.drawable.media_sd_icon);
        } else {
            mQualityImageView.setImageResource(R.drawable.media_hd_icon);
        }

        View contentSendView = findViewById(R.id.preview_activity_content_send_view);
        contentSendView.setMinimumHeight((int) (DESIGN_CONTENT_SEND_VIEW_HEIGHT * Design.HEIGHT_RATIO));
        contentSendView.setBackgroundColor(Color.TRANSPARENT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) contentSendView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_SEND_VIEW_MARGIN * Design.HEIGHT_RATIO);

        mEditText = findViewById(R.id.preview_activity_edit_text);
        Design.updateTextFont(mEditText, Design.FONT_REGULAR32);
        mEditText.setTextColor(Color.WHITE);
        mEditText.setHintTextColor(Design.PLACEHOLDER_COLOR);

        radius =  (DESIGN_CONTENT_SEND_VIEW_HEIGHT * Design.HEIGHT_RATIO) * 0.5f;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

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

                onTimeoutClick();
            }

            @Override
            public void onSendFromMenuOptionClick(boolean allowCopy, boolean allowEphemeral, int timeout) {
                mAllowCopy = allowCopy;
                mAllowEphemeralMessage = allowEphemeral;
                mExpireTimeout = timeout;
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

        if ((!mAllowCopy || mAllowEphemeralMessage) && !mPreviewStartWithMedia && !mShareLocation) {

            ViewGroup viewGroup = findViewById(R.id.preview_activity_layout);

            DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
            defaultConfirmView.setForceDarkMode(true);
            defaultConfirmView.setImage(null);
            defaultConfirmView.setTitle(getString(R.string.account_migration_activity_state_send_files));
            defaultConfirmView.setMessage(getString(R.string.conversation_activity_send_file_warning));
            defaultConfirmView.setConfirmTitle(getString(R.string.application_confirm));
            defaultConfirmView.setCancelTitle(getString(R.string.application_cancel));

            AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
                @Override
                public void onConfirmClick() {
                    defaultConfirmView.animationCloseConfirmView();
                    send();
                }

                @Override
                public void onCancelClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onDismissClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    viewGroup.removeView(defaultConfirmView);
                    setStatusBarColor();
                }
            };
            defaultConfirmView.setObserver(observer);
            viewGroup.addView(defaultConfirmView);
            defaultConfirmView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Color.BLACK);
            setStatusBarColor(color, Color.rgb(72,72,72));
        } else if (mPreviewStartWithMedia && mIsQualityMediaOriginal) {

            long totalSize = totalFilesSize();
            if (totalSize > WARNING_ORIGINAL_SIZE) {
                ViewGroup viewGroup = findViewById(R.id.preview_activity_layout);

                DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
                defaultConfirmView.setForceDarkMode(true);
                defaultConfirmView.setImage(null);
                defaultConfirmView.setTitle(getString(R.string.deleted_account_activity_warning));

                String message = String.format(getString(R.string.conversation_activity_send_quality_size), Formatter.formatFileSize(this, totalSize)) + "\n\n"  + getString(R.string.conversation_activity_send_quality_warning);
                defaultConfirmView.setMessage(message);
                defaultConfirmView.setConfirmTitle(getString(R.string.conversation_activity_send_quality_standard));
                defaultConfirmView.setCancelTitle(getString(R.string.conversation_activity_media_quality_original));

                AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
                    @Override
                    public void onConfirmClick() {
                        defaultConfirmView.animationCloseConfirmView();
                        mIsQualityMediaOriginal = false;
                        send();
                    }

                    @Override
                    public void onCancelClick() {
                        defaultConfirmView.animationCloseConfirmView();
                        send();
                    }

                    @Override
                    public void onDismissClick() {
                        defaultConfirmView.animationCloseConfirmView();
                    }

                    @Override
                    public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                        viewGroup.removeView(defaultConfirmView);
                        setStatusBarColor();
                    }
                };
                defaultConfirmView.setObserver(observer);
                viewGroup.addView(defaultConfirmView);
                defaultConfirmView.show();

                int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Color.BLACK);
                setStatusBarColor(color, Color.rgb(72,72,72));
            } else {
                send();
            }

        } else {
            send();
        }
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
            mMenuSendOptionView.openMenu(mAllowCopy, mAllowEphemeralMessage, mExpireTimeout);

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Color.BLACK);
            setStatusBarColor(color, Color.rgb(72,72,72));
        }
    }

    private void onQualityClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onQualityClick");
        }

        ViewGroup viewGroup = findViewById(R.id.preview_activity_layout);

        MenuSelectValueView menuSelectValueView = new MenuSelectValueView(this, null);
        menuSelectValueView.setActivity(this);
        menuSelectValueView.setForceDarkMode(true);
        menuSelectValueView.setObserver(new MenuSelectValueView.Observer() {
            @Override
            public void onCloseMenuAnimationEnd() {
                viewGroup.removeView(menuSelectValueView);
                setStatusBarColor();
            }

            @Override
            public void onSelectValue(int value) {

                menuSelectValueView.animationCloseMenu();

                if (value == TwinmeApplication.QualityMedia.STANDARD.ordinal()) {
                    mIsQualityMediaOriginal = false;
                    mQualityImageView.setImageResource(R.drawable.media_sd_icon);
                } else {
                    mIsQualityMediaOriginal = true;
                    mQualityImageView.setImageResource(R.drawable.media_hd_icon);
                }
            }

            @Override
            public void onSelectTimeout(UITimeout timeout) {

            }
        });

        viewGroup.addView(menuSelectValueView);
        menuSelectValueView.openMenu(MenuSelectValueView.MenuType.QUALITY_MEDIA);

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Color.BLACK);
        setStatusBarColor(color, Color.rgb(72,72,72));
    }

    private void onTimeoutClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTimeoutClick");
        }

        ViewGroup viewGroup = findViewById(R.id.preview_activity_layout);

        MenuSelectValueView menuTimeoutView = new MenuSelectValueView(this, null);
        menuTimeoutView.setActivity(this);
        menuTimeoutView.setForceDarkMode(true);
        menuTimeoutView.setSelectedValue((int) mExpireTimeout);
        menuTimeoutView.setObserver(new MenuSelectValueView.Observer() {
            @Override
            public void onCloseMenuAnimationEnd() {

                viewGroup.removeView(menuTimeoutView);
                mOverlayView.setVisibility(View.VISIBLE);
                setStatusBarColor();
            }

            @Override
            public void onSelectValue(int value) {

            }

            @Override
            public void onSelectTimeout(UITimeout timeout) {

                menuTimeoutView.animationCloseMenu();
                mExpireTimeout = timeout.getDelay();
                mMenuSendOptionView.updateTimeout((int) mExpireTimeout);
            }
        });

        mOverlayView.setVisibility(View.GONE);
        viewGroup.addView(menuTimeoutView);
        menuTimeoutView.openMenu(MenuSelectValueView.MenuType.EPHEMERAL_MESSAGE);
        menuTimeoutView.bringToFront();
        mOverlayView.setZ(3);
        mMenuSendOptionView.setZ(4);
        menuTimeoutView.setZ(5);

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
