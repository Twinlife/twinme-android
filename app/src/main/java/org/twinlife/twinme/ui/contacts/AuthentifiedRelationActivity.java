/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BaseService;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.models.CertificationLevel;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.services.ShowContactService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.AbstractScannerActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.MnemonicCodeUtils;
import org.twinlife.twinme.utils.OnboardingConfirmView;
import org.twinlife.twinme.utils.camera.CameraManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AuthentifiedRelationActivity extends AbstractScannerActivity implements ShowContactService.Observer {
    private static final String LOG_TAG = "AuthentifiedRelati...";
    private static final boolean DEBUG = false;

    private static final int SHOW_ONBOARDING = 3;

    private static final float DESIGN_FINGERPRINT_MARGIN = 40f;
    private static final float DESIGN_SHARE_PADDING = 20f;
    private static final float DESIGN_SHARE_ICON_SIZE = 42f;
    private static final float DESIGN_SHARE_ICON_PADDING = 27f;

    private View mCertifiedContainerView;
    private View mFingerPrintView;
    private TextView mFingerPrintTextView;
    protected View mInfoScanView;
    protected TextView mInfoTextView;
    private boolean mShowOnboarding;
    private boolean mShowWords = true;
    private boolean mStartScan;
    private List<String> mWords;
    private Bitmap mContactAvatar;

    @Nullable
    private TwincodeURI mCertificationLink;

    @Nullable
    private ShowContactService mShowContactService;

    private Contact mContact;
    private CertificationLevel mCertificationLevel;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        UUID contactId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));
        mShowOnboarding = intent.getBooleanExtra(Intents.INTENT_SHOW_ONBOARDING, false);
        mStartScan = intent.getBooleanExtra(Intents.INTENT_START_SCAN, false);
        if (contactId == null) {

            finish();
            return;
        }

        updateViews();
        mShowContactService = new ShowContactService(this, getTwinmeContext(), this, contactId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHOW_ONBOARDING && resultCode == RESULT_CANCELED) {
            finish();
        }
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

        showOnboarding();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        if (mShowContactService != null) {
            mShowContactService.dispose();
        }

        super.onDestroy();
    }

    @Override
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        super.onRequestPermissions(grantedPermissions);
    }

    @Override
    protected void onError(String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError: message=" + message);
        }

        showAlertMessageView(R.id.authentified_relation_activity_layout, getString(R.string.deleted_account_activity_warning), message, false, this::finish);
    }

    //
    // Implement ShowContactService.Observer methods
    //

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact contact=" + contact);
        }

        mContact = contact;
        mContactAvatar = avatar;

        mCertificationLevel = mContact.getCertificationLevel();

        updateQRCode();
        updateContact();
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }

        finish();
    }

    @Override
    public void onUpdateImage(@NonNull Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateImage avatar=" + avatar);
        }

        mContactAvatar = avatar;
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact contact=" + contact);
        }

        if (!contact.getId().equals(mContact.getId())) {
            return;
        }

        final CertificationLevel newLevel = contact.getCertificationLevel();
        if (mCertificationLevel != newLevel && newLevel == CertificationLevel.LEVEL_4) {
            showSuccessAuthentification();
        }

        mContact = contact;
        mCertificationLevel = newLevel;

        updateContact();
    }

    @Override
    public void onDeleteContact(@NonNull UUID contactId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteContact contactId=" + contactId);
        }

        if (!contactId.equals(mContact.getId())) {

            return;
        }

        if (mResumed) {
            finish();
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
        setContentView(R.layout.authentified_relation_activity);

        setStatusBarColor();
        setToolBar(R.id.authentified_relation_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.authentified_relation_activity_title));

        applyInsets(R.id.authentified_relation_activity_layout, R.id.authentified_relation_activity_tool_bar, R.id.authentified_relation_activity_content_view, Design.TOOLBAR_COLOR, false);

        View contentView = findViewById(R.id.authentified_relation_activity_content_view);
        contentView.setBackgroundColor(Design.GREY_BACKGROUND_COLOR);

        mCertifiedContainerView = findViewById(R.id.authentified_relation_activity_qrcode_container_view);
        float radius = Design.POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable containerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        containerViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mCertifiedContainerView.setBackground(containerViewBackground);

        mQRCodeView = findViewById(R.id.authentified_relation_activity_qrcode_view);
        mQRCodeView.setOnClickListener(view -> onQRCodeClick());

        mCameraView = findViewById(R.id.authentified_relation_activity_camera_view);
        mCameraView.setOnClickListener(view -> {
            if (!mCameraGranted) {
                onSettingsClick();
            }
        });

        radius = Design.POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable cameraViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        if (mCameraGranted) {
            cameraViewBackground.getPaint().setColor(Design.GREY_BACKGROUND_COLOR);
        } else {
            cameraViewBackground.getPaint().setColor(Design.BLACK_COLOR);
        }
        mCameraView.setBackground(cameraViewBackground);

        mInfoScanView = findViewById(R.id.authentified_relation_activity_scan_info_view);

        ImageView scanIconView = findViewById(R.id.authentified_relation_activity_scan_icon_view);
        scanIconView.setColorFilter(Color.WHITE);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) scanIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_SHARE_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_SHARE_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        ViewGroup.LayoutParams layoutParams = scanIconView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SHARE_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.width = (int) (DESIGN_SHARE_ICON_SIZE * Design.HEIGHT_RATIO);

        TextView scanTitleView = findViewById(R.id.authentified_relation_activity_scan_message_view);
        Design.updateTextFont(scanTitleView, Design.FONT_MEDIUM32);
        scanTitleView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) scanTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        mInfoTextView = findViewById(R.id.authentified_relation_activity_message_view);
        Design.updateTextFont(mInfoTextView, Design.FONT_REGULAR34);
        mInfoTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mMessageView = findViewById(R.id.authentified_relation_activity_camera_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR34);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mFingerPrintView = findViewById(R.id.authentified_relation_activity_finger_print_container_view);

        ShapeDrawable fingerPrintViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        fingerPrintViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mFingerPrintView.setBackground(fingerPrintViewBackground);

        TextView fingerPrintTitleView = findViewById(R.id.authentified_relation_activity_finger_print_title_view);
        Design.updateTextFont(fingerPrintTitleView, Design.FONT_MEDIUM32);
        fingerPrintTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View fingerPrintSubView = findViewById(R.id.authentified_relation_activity_finger_print_view);
        fingerPrintSubView.setOnClickListener(v -> onFingerPrintClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) fingerPrintSubView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_FINGERPRINT_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_FINGERPRINT_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_FINGERPRINT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_FINGERPRINT_MARGIN * Design.WIDTH_RATIO);

        ShapeDrawable fingerPrintContainerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        fingerPrintContainerViewBackground.getPaint().setColor(Design.GREY_BACKGROUND_COLOR);
        fingerPrintSubView.setBackground(fingerPrintContainerViewBackground);

        mFingerPrintTextView = findViewById(R.id.authentified_relation_activity_finger_print_text_view);
        Design.updateTextFont(mFingerPrintTextView, Design.FONT_BOLD44);
        mFingerPrintTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mTextureView = findViewById(R.id.authentified_relation_activity_texture_view);
        mTextureView.setSurfaceTextureListener(this);

        mViewFinder = findViewById(R.id.authentified_relation_activity_view_finder_view);
        mViewFinder.setDrawCorner(false);
    }

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (mStartScan) {
            mScanSelect = true;
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
        }
    }

    private void updateQRCode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateQRCode");
        }

        if (mShowContactService != null && mCertificationLink == null) {
            mShowContactService.createAuthenticateURI((BaseService.ErrorCode errorCode, TwincodeURI twincodeURI) -> {
                if (errorCode == BaseService.ErrorCode.SUCCESS && twincodeURI != null) {
                    mCertificationLink = twincodeURI;
                    updateQRCode(mCertificationLink.uri);
                    updateContact();
                }
            });
        } else if (mCertificationLink != null) {
            updateContact();
        }
    }

    @Override
    protected void handleDecode(@NonNull Uri uri) {
        if (DEBUG) {
            Log.d(LOG_TAG, "handleDecode: uri=" + uri);
        }

        if (mShowContactService == null) {
            return;
        }

        mShowContactService.verifyAuthenticateURI(uri, ((BaseService.ErrorCode errorCode, Contact contact) -> {
            if (errorCode == BaseService.ErrorCode.SUCCESS && contact != null) {
                mContact = contact;
                mCertificationLevel = mContact.getCertificationLevel();
                stopScan();
                updateContact();
                // Only display success if we reached level 4 (we could change from level 1 to level 3).
                if (mCertificationLevel == CertificationLevel.LEVEL_4) {
                    showSuccessAuthentification();
                }
            } else {
                incorrectQRCode(getLinkError(errorCode, R.string.capture_activity_incorrect_qrcode));
            }
        }));
    }

    @Override
    protected void incorrectQRCode(String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "incorrectQRCode");
        }

        showAlertMessageView(R.id.authentified_relation_activity_layout, getString(R.string.deleted_account_activity_warning), message, false, this::finish);
    }

    @Override
    protected void permissionCameraResult() {
        if (DEBUG) {
            Log.d(LOG_TAG, "permissionCameraResult");
        }

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

    private void onQRCodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onQRCodeClick");
        }
    }

    private void onFingerPrintClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onFingerPrintClick");
        }

        mShowWords = !mShowWords;

        updateFingerPrint();
    }

    private void stopScan() {
        if (DEBUG) {
            Log.d(LOG_TAG, "stopScan");
        }

        mScanSelect = false;

        // Release the camera and any resource while we are not active.
        if (mCameraManager != null) {
            mCameraManager.close();
            mCameraManager = null;
        }

        // Turn off the ambient light manager if there is one.
        if (mAmbientLightManager != null) {
            mAmbientLightManager.stop();
            mAmbientLightManager = null;
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void updateContact() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateContact");
        }

        if (mContact == null) {
            return;
        }

        if (mCertificationLevel == CertificationLevel.LEVEL_2
                || (mCertificationLevel == CertificationLevel.LEVEL_1 && mContact.getPublicPeerTwincodeOutboundId() == null)) {
            setTitle(getString(R.string.authentified_relation_activity_to_be_certified_title));

            mScanSelect = true;
            if (mCameraManager == null) {
                mCameraManager = createCameraManager(mTextureView, this, CameraManager.Mode.QRCODE);
            }
            if (!mCameraGranted && !mDeferedOnCreateInternal) {
                mDeferedOnCreateInternal = true;
            }
            checkCameraPermission();

            if (mAmbientLightManager == null) {
                mAmbientLightManager = new AmbientLightManager();
            }

            mCertifiedContainerView.setVisibility(View.GONE);
            mFingerPrintView.setVisibility(View.GONE);
            mQRCodeView.setVisibility(View.INVISIBLE);
            mCameraView.setVisibility(View.VISIBLE);
            mInfoTextView.setText(String.format(getString(R.string.authentified_relation_activity_level_2), mContact.getName()));
        } else {
            mCameraView.setVisibility(View.GONE);

            if (mCertificationLevel != CertificationLevel.LEVEL_4) {
                setTitle(getString(R.string.authentified_relation_activity_to_be_certified_title));

                mInfoTextView.setText(String.format(getString(R.string.authentified_relation_activity_level_3), mContact.getName()));
                mQRCodeView.setVisibility(View.VISIBLE);
                mCertifiedContainerView.setVisibility(View.VISIBLE);
                mFingerPrintView.setVisibility(View.GONE);
            } else {
                setTitle(getString(R.string.authentified_relation_activity_title));

                String message = String.format(getString(R.string.authentified_relation_activity_level_4), mContact.getName()) + "\n\n" + getString(R.string.authentified_relation_activity_relation_print_message);
                mInfoTextView.setText(message);
                mQRCodeView.setVisibility(View.GONE);
                mCertifiedContainerView.setVisibility(View.GONE);
                mFingerPrintView.setVisibility(View.VISIBLE);

                updateFingerPrint();
            }
        }
    }

    private void updateFingerPrint() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFingerPrint");
        }

        if (mCertificationLink == null) {
            return;
        }

        if (mWords == null) {
            try {
                MnemonicCodeUtils mnemonicCodeUtils = new MnemonicCodeUtils(this);
                byte[] hash = MessageDigest.getInstance("SHA-256").digest(mCertificationLink.label.getBytes(StandardCharsets.UTF_8));
                mWords = mnemonicCodeUtils.xorAndMnemonic(hash, Locale.getDefault());
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        if (mShowWords) {
            StringBuilder stringBuilder = new StringBuilder();

            int count = mWords.size();
            for (int i = 0; i < mWords.size(); i++) {
                stringBuilder.append(mWords.get(i).toUpperCase());
                if (i+1 < count) {
                    stringBuilder.append("\n");
                }
            }

            mFingerPrintTextView.setText(stringBuilder.toString());
        } else {
            mFingerPrintTextView.setText(mCertificationLink.label);
        }
    }

    private void showOnboarding() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showOnboarding");
        }

        if (!mShowOnboarding && getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.CERTIFIED_RELATION)) {
            mShowOnboarding = true;

            ViewGroup viewGroup = findViewById(R.id.authentified_relation_activity_layout);

            OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(getString(R.string.authentified_relation_activity_to_be_certified_title));
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.append("\n\n");
            int startSubTitle = spannableStringBuilder.length();
            spannableStringBuilder.append(getString(R.string.authentified_relation_activity_onboarding_subtitle));
            spannableStringBuilder.setSpan(new RelativeSizeSpan(0.94f), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            onboardingConfirmView.setSpannableTitle(spannableStringBuilder);

            boolean darkMode = false;
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            int displayMode = getTwinmeApplication().displayMode();
            if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
                darkMode = true;
            }

            onboardingConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), darkMode ? R.drawable.onboarding_authentified_relation_dark : R.drawable.onboarding_authentified_relation, null));

            onboardingConfirmView.setMessage(getString(R.string.authentified_relation_activity_onboarding_message));
            onboardingConfirmView.setConfirmTitle(getString(R.string.authentified_relation_activity_start));
            onboardingConfirmView.setCancelTitle(getString(R.string.application_do_not_display));

            AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
                @Override
                public void onConfirmClick() {
                    onboardingConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCancelClick() {
                    getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.CERTIFIED_RELATION, false);
                    onboardingConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onDismissClick() {
                    onboardingConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    viewGroup.removeView(onboardingConfirmView);
                    setStatusBarColor();
                }
            };
            onboardingConfirmView.setObserver(observer);
            viewGroup.addView(onboardingConfirmView);
            onboardingConfirmView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    private void showSuccessAuthentification() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showSuccessAuthentification");
        }

        ViewGroup viewGroup = findViewById(R.id.authentified_relation_activity_layout);

        SuccessAuthentifiedRelationView successAuthentifiedRelationView = new SuccessAuthentifiedRelationView(this, null);
        successAuthentifiedRelationView.setAvatar(mContactAvatar, false);
        successAuthentifiedRelationView.setTitle(mContact.getName());

        String message = String.format(getString(R.string.authentified_relation_activity_certified_message), mContact.getName());
        successAuthentifiedRelationView.setMessage(message);
        successAuthentifiedRelationView.setConfirmTitle(getString(R.string.application_ok));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                successAuthentifiedRelationView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                successAuthentifiedRelationView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                successAuthentifiedRelationView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(successAuthentifiedRelationView);
                setStatusBarColor();
            }
        };
        successAuthentifiedRelationView.setObserver(observer);
        viewGroup.addView(successAuthentifiedRelationView);
        successAuthentifiedRelationView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }
}
