/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.inAppSubscriptionActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractScannerActivity;
import org.twinlife.twinme.utils.camera.CameraManager;

public class InvitationSubscriptionActivity extends AbstractScannerActivity {
    private static final String LOG_TAG = "InvitationSubscr...";
    private static final boolean DEBUG = false;

    private static final float DESIGN_GALLERY_PADDING = 20f;
    private static final float DESIGN_GALLERY_ICON_SIZE = 42f;
    private static final float DESIGN_GALLERY_ICON_PADDING = 27f;

    private static final int DESIGN_PLACEHOLDER_COLOR = Color.rgb(162, 162, 162);
    private View mTwincodeContentView;
    private ImageView mInviteView;
    private EditText mTwincodeEditText;
    private View mImportFromGalleryView;
    protected View mInfoScanView;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //
    // Private methods
    //

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.invitation_subscription_activity);

        setStatusBarColor();
        setToolBar(R.id.invitation_subscription_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setBackgroundColor(Design.GREY_BACKGROUND_COLOR);

        setTitle(getString(R.string.add_contact_activity_title));

        applyInsets(R.id.invitation_subscription_activity_layout, R.id.invitation_subscription_activity_tool_bar, R.id.invitation_subscription_activity_background, Design.TOOLBAR_COLOR, false);

        View backgroundView = findViewById(R.id.invitation_subscription_activity_background);
        backgroundView.setBackgroundColor(Design.GREY_BACKGROUND_COLOR);

        TextView messageInviteView = findViewById(R.id.invitation_subscription_activity_message_view);
        messageInviteView.setTypeface(Design.FONT_REGULAR30.typeface);
        messageInviteView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR30.size);
        messageInviteView.setTextColor(Design.FONT_COLOR_GREY);

        float radius = (int) (Design.BUTTON_HEIGHT * 0.5) * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        mCameraView = findViewById(R.id.invitation_subscription_activity_camera_view);
        mCameraView.setOnClickListener(view -> {
            if (!mCameraGranted) {
                onSettingsClick();
            }
        });

        ShapeDrawable cameraViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        if (mCameraGranted) {
            cameraViewBackground.getPaint().setColor(Design.GREY_BACKGROUND_COLOR);
        } else {
            cameraViewBackground.getPaint().setColor(Design.BLACK_COLOR);
        }
        mCameraView.setBackground(cameraViewBackground);

        mMessageView = findViewById(R.id.invitation_subscription_activity_camera_message_view);
        mMessageView.setTypeface(Design.FONT_REGULAR34.typeface);
        mMessageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR34.size);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_GALLERY_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_GALLERY_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_GALLERY_PADDING * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_GALLERY_PADDING * Design.WIDTH_RATIO));

        mTextureView = findViewById(R.id.invitation_subscription_activity_texture_view);
        mTextureView.setSurfaceTextureListener(this);

        mViewFinder = findViewById(R.id.invitation_subscription_activity_view_finder_view);
        mViewFinder.setDrawCorner(false);

        mImportFromGalleryView = findViewById(R.id.invitation_subscription_activity_import_gallery_view);
        mImportFromGalleryView.setOnClickListener(v -> onImportFromGalleryClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mImportFromGalleryView.getLayoutParams();
        marginLayoutParams.bottomMargin = - (int) (Design.BUTTON_HEIGHT * 0.5);

        radius = (int) (Design.BUTTON_HEIGHT * 0.5) * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable importFromGalleryViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        importFromGalleryViewBackground.getPaint().setColor(Design.getMainStyle());
        mImportFromGalleryView.setBackground(importFromGalleryViewBackground);

        ImageView importFromGalleryIconView = findViewById(R.id.invitation_subscription_activity_import_gallery_icon_view);
        importFromGalleryIconView.setColorFilter(Color.WHITE);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) importFromGalleryIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_GALLERY_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_GALLERY_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_GALLERY_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_GALLERY_PADDING * Design.WIDTH_RATIO));

        ViewGroup.LayoutParams layoutParams = importFromGalleryIconView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_GALLERY_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.width = (int) (DESIGN_GALLERY_ICON_SIZE * Design.HEIGHT_RATIO);

        layoutParams = mImportFromGalleryView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView importFromGalleryTitleView = findViewById(R.id.invitation_subscription_activity_import_gallery_title_view);
        importFromGalleryTitleView.setTypeface(Design.FONT_MEDIUM32.typeface);
        importFromGalleryTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM32.size);
        importFromGalleryTitleView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) importFromGalleryTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_GALLERY_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_GALLERY_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_GALLERY_PADDING * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_GALLERY_PADDING * Design.WIDTH_RATIO));

        mTwincodeContentView = findViewById(R.id.invitation_subscription_activity_twincode_content_view);
        radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable twincodeViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        twincodeViewBackground.getPaint().setColor(Color.WHITE);
        mTwincodeContentView.setBackground(twincodeViewBackground);

        layoutParams = mTwincodeContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        ImageView copyTwincodeImageView = findViewById(R.id.invitation_subscription_activity_twincode_copy_view);
        copyTwincodeImageView.setColorFilter(DESIGN_PLACEHOLDER_COLOR);

        mTwincodeEditText = findViewById(R.id.invitation_subscription_activity_twincode_edit_text);
        mTwincodeEditText.setTypeface(Design.FONT_REGULAR30.typeface);
        mTwincodeEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR30.size);
        mTwincodeEditText.setTextColor(Color.BLACK);
        mTwincodeEditText.setHintTextColor(DESIGN_PLACEHOLDER_COLOR);
        mTwincodeEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                setUpdated();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mInviteView = findViewById(R.id.invitation_subscription_activity_twincode_invite_view);
        mInviteView.setVisibility(View.GONE);
        mInviteView.setColorFilter(Design.getMainStyle());
        mInviteView.setOnClickListener(v -> onInviteClick());

        mInfoScanView = findViewById(R.id.invitation_subscription_activity_scan_info_view);

        ImageView scanIconView = findViewById(R.id.invitation_subscription_activity_scan_icon_view);
        scanIconView.setColorFilter(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) scanIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_GALLERY_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_GALLERY_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_GALLERY_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_GALLERY_PADDING * Design.WIDTH_RATIO));

        layoutParams = scanIconView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_GALLERY_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.width = (int) (DESIGN_GALLERY_ICON_SIZE * Design.HEIGHT_RATIO);

        TextView scanTitleView = findViewById(R.id.invitation_subscription_activity_scan_message_view);
        scanTitleView.setTypeface(Design.FONT_MEDIUM32.typeface);
        scanTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM32.size);
        scanTitleView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) scanTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_GALLERY_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_GALLERY_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_GALLERY_PADDING * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_GALLERY_PADDING * Design.WIDTH_RATIO));

        updateViews();
    }

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (mScanSelect) {

            if (mCameraManager == null) {
                mCameraManager = createCameraManager(mTextureView, this, CameraManager.Mode.QRCODE);
            }

            if (mAmbientLightManager == null) {
                mAmbientLightManager = new AmbientLightManager();
            }

            if (!mCameraGranted && !mDeferedOnCreateInternal) {
                mDeferedOnCreateInternal = true;
            }
            checkCameraPermission();

            mCameraView.setVisibility(View.VISIBLE);
            mImportFromGalleryView.setVisibility(View.VISIBLE);
            mTwincodeContentView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void permissionCameraResult() {
        super.permissionCameraResult();

        if (mCameraGranted) {
            mMessageView.setVisibility(View.GONE);
            mInfoScanView.setVisibility(View.VISIBLE);
            mInfoScanView.postDelayed(() -> mInfoScanView.setVisibility(View.GONE), 5000);
        } else {
            mInfoScanView.setVisibility(View.GONE);
            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(getResources().getString(R.string.capture_activity_no_camera));
        }
    }

    private void onInviteClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteClick");
        }

        Uri uri = Uri.parse(mTwincodeEditText.getText().toString());
        handleDecode(uri);
    }

    private void setUpdated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteClick");
        }

        if (mTwincodeEditText.getText().toString().isEmpty()) {
            mInviteView.setVisibility(View.GONE);
        } else {
            mInviteView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void handleDecode(@NonNull Uri uri) {
        if (DEBUG) {
            Log.d(LOG_TAG, "handleDecode: uri=" + uri);
        }

        String action = uri.getAuthority();
        if (TwincodeURI.INVITE_ACTION.equals(action)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setClass(this, AcceptInvitationSubscriptionActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else {
            incorrectQRCode(getString(R.string.capture_activity_incorrect_qrcode));
        }
    }

    @Override
    protected void incorrectQRCode(String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "incorrectQRCode");
        }

        showAlertMessageView(R.id.invitation_subscription_activity_layout, getString(R.string.deleted_account_activity_warning), message, false, this::finish);
    }

    @Override
    protected void onError(String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError: message=" + message);
        }

        showAlertMessageView(R.id.invitation_subscription_activity_layout, getString(R.string.deleted_account_activity_warning), message, false, this::finish);
    }
}
