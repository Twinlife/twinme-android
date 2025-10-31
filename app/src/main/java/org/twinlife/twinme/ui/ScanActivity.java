/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BaseService;
import org.twinlife.twinlife.TrustMethod;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.services.ShareProfileService;
import org.twinlife.twinme.skin.Design;

public class ScanActivity extends AbstractScannerActivity implements ShareProfileService.Observer {
    private static final String LOG_TAG = "ScanActivity";
    private static final boolean DEBUG = false;

    private static final float DESIGN_CAPTURE_TOP = 120f;
    private static final float DESIGN_CAPTURE_BOTTOM = 160f;

    private static final float DESIGN_TWINCODE_BOTTOM_MARGIN = 60f;

    private EditText mTwincodeView;
    private ImageView mInviteView;

    private ShareProfileService mProfileService;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        checkCameraPermission();

        mProfileService = new ShareProfileService(this, getTwinmeContext(), this);
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

        mProfileService.dispose();

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        if (resultCode == FullscreenQRCodeActivity.CONTACT_CREATED_RESULT) {
            finish();
        }
    }

    @Override
    protected void onError(String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError: message=" + message);
        }

        showAlertMessageView(R.id.scan_activity_layout, getString(R.string.deleted_account_activity_warning), message, false, this::finish);
    }

    @Override
    public void onGetDefaultProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProfile profile=" + profile);
        }

    }

    @Override
    public void onGetDefaultProfileNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDefaultProfileNotFound");
        }
    }

    @Override
    public void onGetTwincodeURI(@NonNull TwincodeURI uri) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetTwincodeURI uri=" + uri);
        }
    }

    @Override
    public void onCreateContact(@NonNull Contact contact) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateContact contact=" + contact);
        }

        // The peer has scanned our QR-code: we are done and can show the new contact.
        // (if we scan the others' QR-code we will end up in AcceptInvitationActivity.onCreateContact).
        showContactActivity(contact);

        finish();
    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact contact=" + contact);
        }
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact contact=" + contact);
        }
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
        setContentView(R.layout.scan_activity);

        setBackgroundColor(Color.BLACK);
        setStatusBarColor(Color.BLACK);
        showToolBar(false);
        showBackButton(true);

        View closeView = findViewById(R.id.scan_activity_close_view);
        closeView.setOnClickListener(v -> finish());

        ImageView closeImageView = findViewById(R.id.scan_activity_close_icon_view);
        closeImageView.setColorFilter(Color.WHITE);

        View galleryView = findViewById(R.id.scan_activity_gallery_view);
        galleryView.setOnClickListener(v -> onImportFromGalleryClick());

        ImageView galleryImageView = findViewById(R.id.scan_activity_gallery_icon_view);
        galleryImageView.setColorFilter(Color.WHITE);

        mCameraView = findViewById(R.id.scan_activity_camera_view);
        mCameraView.setOnClickListener(view -> {
            if (!mCameraGranted) {
                onSettingsClick();
            }
        });

        ShapeDrawable cameraViewBackground = new ShapeDrawable();
        cameraViewBackground.getPaint().setColor(Color.BLACK);
        mCameraView.setBackground(cameraViewBackground);

        ViewGroup.LayoutParams layoutParams = mCameraView.getLayoutParams();
        layoutParams.width = Design.DISPLAY_WIDTH;
        layoutParams.height = (int) (Design.DISPLAY_HEIGHT - (DESIGN_CAPTURE_TOP * Design.HEIGHT_RATIO) - (DESIGN_CAPTURE_BOTTOM * Design.HEIGHT_RATIO));

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mCameraView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CAPTURE_TOP * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_CAPTURE_BOTTOM * Design.HEIGHT_RATIO);

        mTextureView = findViewById(R.id.scan_activity_texture_view);
        mTextureView.setSurfaceTextureListener(this);

        mViewFinder = findViewById(R.id.scan_activity_view_finder_view);

        mMessageView = findViewById(R.id.scan_activity_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR34);
        mMessageView.setTextColor(Design.WHITE_COLOR);

        if (mCameraGranted) {
            mMessageView.setText(getResources().getString(R.string.capture_activity_message));
            mMessageView.postDelayed(() -> mMessageView.setVisibility(View.GONE), 5000);
        }

        View twincodeContentView = findViewById(R.id.scan_activity_twincode_content_view);
        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable nameViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        nameViewBackground.getPaint().setColor(Design.EDIT_TEXT_CONVERSATION_BACKGROUND_COLOR);
        twincodeContentView.setBackground(nameViewBackground);

        layoutParams = twincodeContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) twincodeContentView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_TWINCODE_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        mTwincodeView = findViewById(R.id.scan_activity_twincode_view);
        Design.updateTextFont(mTwincodeView, Design.FONT_REGULAR30);
        mTwincodeView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mTwincodeView.setHintTextColor(Design.PLACEHOLDER_COLOR);
        mTwincodeView.addTextChangedListener(new TextWatcher() {

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

        mInviteView = findViewById(R.id.scan_activity_invite_view);
        mInviteView.setVisibility(View.GONE);
        mInviteView.setColorFilter(Design.getMainStyle());
        mInviteView.setOnClickListener(v -> onInviteClick());
    }

    private void onInviteClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteClick");
        }

        Uri uri = Uri.parse(mTwincodeView.getText().toString());
        handleDecode(uri);
    }

    private void setUpdated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteClick");
        }

        if (mTwincodeView.getText().toString().isEmpty()) {
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

        mProfileService.parseURI(uri, this::onParseTwincodeURI);
    }

    private void onParseTwincodeURI(@NonNull BaseService.ErrorCode errorCode, @Nullable TwincodeURI twincodeURI) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onParseTwincodeURI: errorCode=" + errorCode + " twincodeURI=" + twincodeURI);
        }

        // @todo Handle errors and report an accurate message:
        // ErrorCode.BAD_REQUEST: link is not well formed or not one of our link
        // ErrorCode.FEATURE_NOT_IMPLEMENTED: link does not target our application or domain.
        // ErrorCode.ITEM_NOT_FOUND: link targets the application but it is not compatible with the version.
        // TwincodeURI.Kind == Kind.AccountMigration => redirect to account migration
        // TwincodeURI.Kind == Kind.Call|Kind.Transfer => forbidden
        if (errorCode == BaseService.ErrorCode.SUCCESS && twincodeURI != null) {
            if (twincodeURI.kind == TwincodeURI.Kind.Invitation) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(twincodeURI.uri));
                intent.putExtra(Intents.INTENT_TRUST_METHOD, TrustMethod.QR_CODE);

                intent.setClass(this, AcceptInvitationActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            } else {
                // Scanning a Call, Transfer or account migration QR-code.
                incorrectQRCode(getLinkError(twincodeURI.kind, R.string.capture_activity_incorrect_qrcode));
            }
        } else {
            incorrectQRCode(getLinkError(errorCode, R.string.capture_activity_incorrect_qrcode));
        }
    }

    @Override
    protected void incorrectQRCode(String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "incorrectQRCode");
        }

        showAlertMessageView(R.id.scan_activity_layout, getString(R.string.deleted_account_activity_warning), message, false, this::finish);
    }
}
