/*
 *  Copyright (c) 2015-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui;

import static org.twinlife.twinme.ui.Intents.INTENT_PROFILE_ID;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BaseService;
import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinlife.ConnectivityService;
import org.twinlife.twinlife.ProxyDescriptor;
import org.twinlife.twinlife.SNIProxyDescriptor;
import org.twinlife.twinlife.TrustMethod;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinlife.util.Logger;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.services.ShareProfileService;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.contacts.EnterInvitationCodeActivity;
import org.twinlife.twinme.ui.contacts.InvitationCodeActivity;
import org.twinlife.twinme.ui.contacts.ResetInvitationConfirmView;
import org.twinlife.twinme.ui.contacts.SuccessAuthentifiedRelationView;
import org.twinlife.twinme.ui.conversationActivity.NamedFileProvider;
import org.twinlife.twinme.ui.conversationFilesActivity.CustomTabView;
import org.twinlife.twinme.ui.conversationFilesActivity.UICustomTab;
import org.twinlife.twinme.ui.inAppSubscriptionActivity.AcceptInvitationSubscriptionActivity;
import org.twinlife.twinme.ui.settingsActivity.SettingsAdvancedActivity;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.DefaultConfirmView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.SaveTwincodeAsyncTask;
import org.twinlife.twinme.utils.TwincodeView;
import org.twinlife.twinme.utils.camera.CameraManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AddContactActivity extends AbstractScannerActivity implements ShareProfileService.Observer, CustomTabView.Observer {
    private static final String LOG_TAG = "AddContactActivity";
    private static final boolean DEBUG = false;

    public enum InvitationMode {
        INVITE,
        SCAN,
        INVITE_ONLY
    }

    private static final int QRCODE_PIXEL_WIDTH = 295;
    private static final int QRCODE_PIXEL_HEIGHT = 295;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    private static final int ANIMATION_DURATION = 100;

    private static final float DESIGN_PROFILE_VIEW_HEIGHT = 92f;
    private static final float DESIGN_PROFILE_VIEW_MARGIN = 40f;
    private static final float DESIGN_NAME_VIEW_MARGIN = 20f;
    private static final float DESIGN_MESSAGE_VIEW_MARGIN = 24f;
    private static final float DESIGN_SHARE_SUBTITLE_VIEW_MARGIN = 10f;
    private static final float DESIGN_ZOOM_MARGIN = 21f;
    private static final float DESIGN_SHARE_PADDING = 20f;
    private static final float DESIGN_SHARE_ICON_SIZE = 42f;
    private static final float DESIGN_SHARE_ICON_PADDING = 27f;
    private static final float DESIGN_CUSTOM_TAB_VIEW_HEIGHT = 148f;
    private static final float DESIGN_ZOOM_HEIGHT = 70;
    private static final float DESIGN_QR_CODE_TOP_MARGIN = 60;
    private static final float DESIGN_TWINCODE_TOP_MARGIN = 18;
    private static final float DESIGN_TWINCODE_MARGIN = 30;
    private static final float DESIGN_ACTION_TOP_MARGIN = 24;
    private static final float DESIGN_ACTION_HEIGHT = 128;
    private static final float DESIGN_INVITATION_CODE_HEIGHT = 100;
    private static final float DESIGN_PASTE_TWINCODE_SMALL_MARGIN = 24;
    private static final float DESIGN_PASTE_TWINCODE_LARGE_MARGIN = 120;
    private static final float DESIGN_INVITATION_CODE_BOTTOM_MARGIN = 30;

    private boolean mDeferredSaveTwincode = false;

    private CustomTabView mCustomTabView;
    private View mTwincodeContainerView;
    private View mProfileView;
    private CircularImageView mAvatarView;
    private TextView mNameView;
    private TextView mTwincodeView;
    private View mZoomView;
    private View mResetView;
    private View mCopyView;
    private View mSaveView;
    private View mTwincodePasteView;
    private View mInvitationCodeView;
    private View mInviteView;
    private EditText mTwincodeEditText;
    private View mShareView;
    private TextView mShareSubTitleView;
    private TextView mMessageInviteView;
    private View mImportFromGalleryView;
    private ImageView mQRCodeView;
    protected View mInfoScanView;
    private TwincodeView mSaveTwincodeView;
    private Bitmap mQRCodeBitmap;

    private ShareProfileService mProfileService;
    @Nullable
    private Profile mProfile;
    private Bitmap mAvatar;
    @Nullable
    private TwincodeURI mInvitationLink;

    private InvitationMode mInvitationMode = InvitationMode.INVITE;

    @Nullable
    private TrustMethod mTrustMethod = null;

    private float mQrCodeInitialTop = 0;
    private float mQrCodeInitialHeight = 0;
    private float mQrCodeMaxHeight = 0;
    private boolean mZoomQRCode = false;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        mInvitationMode = (InvitationMode) getIntent().getSerializableExtra(Intents.INTENT_INVITATION_MODE);

        super.onCreate(savedInstanceState);

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

        // Update again the QR-code because the twincode could change.
        updateProfile();
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
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        super.onRequestPermissions(grantedPermissions);

        boolean storageWriteAccessGranted = false;
        for (Permission grantedPermission : grantedPermissions) {
            if (grantedPermission == Permission.WRITE_EXTERNAL_STORAGE) {
                storageWriteAccessGranted = true;
                break;
            }
        }

        if (mDeferredSaveTwincode) {
            mDeferredSaveTwincode = false;
            if (storageWriteAccessGranted) {
                mProfileService.getProfileImage(mProfile, (Bitmap avatar) -> {
                    if (mProfile != null && mInvitationLink != null) {
                        mSaveTwincodeView.setTwincodeInformation(this, mProfile.getName(), avatar,
                                mQRCodeBitmap, mInvitationLink.label,
                                getString(R.string.fullscreen_qrcode_activity_save_message));
                    }

                    Bitmap bitmapToSave = getBitmapFromTwincodeView();
                    if (bitmapToSave != null) {
                        new SaveTwincodeAsyncTask(this, bitmapToSave).execute();
                    } else {
                        toast(getString(R.string.application_operation_failure));
                    }

                });
            }
        }
    }

    @Override
    protected void onError(String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError: message=" + message);
        }

        showAlertMessageView(R.id.add_contact_activity_layout, getString(R.string.deleted_account_activity_warning), message, false, this::finish);
    }

    @Override
    public void onGetDefaultProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProfile profile=" + profile);
        }

        mProfile = profile;
        updateProfile();
    }

    @Override
    public void onGetTwincodeURI(@NonNull TwincodeURI uri) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetTwincodeURI uri=" + uri);
        }

        mInvitationLink = uri;
        updateQRCode();
    }

    @Override
    public void onGetDefaultProfileNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDefaultProfileNotFound");
        }
    }

    @Override
    public void onCreateContact(@NonNull Contact contact) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateContact contact=" + contact);
        }

        // The peer has scanned our QR-code: we are done and can show the new contact.
        // (if we scan the others' QR-code we will end up in AcceptInvitationActivity.onCreateContact).
        if (mResumed) {
            showContactActivity(contact);
        }

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
    // CustomTabView.Observer implements methods
    //

    @Override
    public void onSelectCustomTab(UICustomTab customTab) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectCustomTab: " + customTab);
        }

        if (customTab.getCustomTabType() == UICustomTab.CustomTabType.INVITE) {
            mInvitationMode = InvitationMode.INVITE;
        } else if (customTab.getCustomTabType() == UICustomTab.CustomTabType.SCAN) {
            mInvitationMode = InvitationMode.SCAN;
        }

        updateViews();
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
        setContentView(R.layout.add_contact_activity);

        setStatusBarColor();
        setToolBar(R.id.add_contact_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.GREY_BACKGROUND_COLOR);

        applyInsets(R.id.add_contact_activity_layout, R.id.add_contact_activity_tool_bar, R.id.add_contact_activity_background, Design.TOOLBAR_COLOR, false);

        if (mInvitationMode == InvitationMode.INVITE_ONLY) {
            setTitle(getString(R.string.add_contact_activity_title));
        } else {
            setTitle(getString(R.string.main_activity_add_contact));
        }

        View backgroundView = findViewById(R.id.add_contact_activity_background);
        backgroundView.setBackgroundColor(Design.GREY_BACKGROUND_COLOR);

        mProfileView = findViewById(R.id.add_contact_activity_profile_view);
        mProfileView.setOnClickListener(v -> onProfileViewClick());

        ViewGroup.LayoutParams layoutParams = mProfileView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_PROFILE_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mProfileView.getLayoutParams();

        if (mInvitationMode == InvitationMode.INVITE_ONLY) {
            marginLayoutParams.topMargin = (int) (DESIGN_PROFILE_VIEW_MARGIN * Design.HEIGHT_RATIO);
        }

        marginLayoutParams.bottomMargin = (int) (DESIGN_PROFILE_VIEW_MARGIN * Design.HEIGHT_RATIO);

        mAvatarView = findViewById(R.id.add_contact_activity_avatar_view);

        mTwincodeContainerView = findViewById(R.id.add_contact_activity_twincode_container_view);

        layoutParams = mTwincodeContainerView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;

        float radius = Design.POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable containerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        containerViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mTwincodeContainerView.setBackground(containerViewBackground);

        mNameView = findViewById(R.id.add_contact_activity_name_view);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM32);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_NAME_VIEW_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_NAME_VIEW_MARGIN * Design.WIDTH_RATIO));

        mMessageInviteView = findViewById(R.id.add_contact_activity_message_view);
        Design.updateTextFont(mMessageInviteView, Design.FONT_REGULAR28);
        mMessageInviteView.setTextColor(Design.FONT_COLOR_DEFAULT);

        if (mInvitationMode == InvitationMode.INVITE_ONLY) {
            mMessageInviteView.setText(getResources().getString(R.string.twincode_activity_message));
        }

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageInviteView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_VIEW_MARGIN * Design.WIDTH_RATIO);

        mQRCodeView = findViewById(R.id.add_contact_activity_qrcode_view);
        mQRCodeView.setOnClickListener(view -> onQRCodeClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mQRCodeView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_QR_CODE_TOP_MARGIN * Design.HEIGHT_RATIO);

        mZoomView = findViewById(R.id.add_contact_activity_zoom_view);
        mZoomView.setOnClickListener(v -> onQRCodeClick());

        layoutParams = mZoomView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ZOOM_HEIGHT * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ZOOM_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mZoomView.getLayoutParams();
        marginLayoutParams.topMargin = - (int) (DESIGN_ZOOM_MARGIN * Design.HEIGHT_RATIO);

        RoundedView zoomRoundedView = findViewById(R.id.add_contact_activity_zoom_rounded_view);
        zoomRoundedView.setBorder(1, Design.GREY_COLOR);
        zoomRoundedView.setColor(Design.WHITE_COLOR);

        ImageView zoomIconView = findViewById(R.id.add_contact_activity_zoom_icon_view);
        zoomIconView.setColorFilter(Design.BLACK_COLOR);

        mTwincodeView = findViewById(R.id.add_contact_activity_twincode_view);
        Design.updateTextFont(mTwincodeView, Design.FONT_BOLD28);
        mTwincodeView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mTwincodeView.setOnClickListener(v -> onTwincodeClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTwincodeView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TWINCODE_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_TWINCODE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_TWINCODE_MARGIN * Design.WIDTH_RATIO);

        mResetView = findViewById(R.id.add_contact_activity_reset_clickable_view);
        mResetView.setOnClickListener(v -> onGenerateCodeClick());

        layoutParams = mResetView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ACTION_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mResetView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ACTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        RoundedView resetRoundedView = findViewById(R.id.add_contact_activity_reset_rounded_view);
        resetRoundedView.setBorder(1, Design.GREY_COLOR);
        resetRoundedView.setColor(Design.WHITE_COLOR);

        ImageView resetIconView = findViewById(R.id.add_contact_activity_reset_icon_view);
        resetIconView.setColorFilter(Design.BLACK_COLOR);

        TextView resetTextView = findViewById(R.id.add_contact_activity_reset_text_view);
        Design.updateTextFont(resetTextView, Design.FONT_MEDIUM28);
        resetTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mCopyView = findViewById(R.id.add_contact_activity_copy_clickable_view);
        mCopyView.setOnClickListener(v -> onTwincodeClick());

        layoutParams = mCopyView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ACTION_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCopyView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ACTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        RoundedView copyRoundedView = findViewById(R.id.add_contact_activity_copy_rounded_view);
        copyRoundedView.setBorder(1, Design.GREY_COLOR);
        copyRoundedView.setColor(Design.WHITE_COLOR);

        ImageView copyIconView = findViewById(R.id.add_contact_activity_copy_icon_view);
        copyIconView.setColorFilter(Design.BLACK_COLOR);

        TextView copyTextView = findViewById(R.id.add_contact_activity_copy_text_view);
        Design.updateTextFont(copyTextView, Design.FONT_MEDIUM28);
        copyTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mSaveView = findViewById(R.id.add_contact_activity_save_clickable_view);
        mSaveView.setOnClickListener(v -> onSaveInGalleryClick());

        layoutParams = mSaveView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ACTION_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSaveView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ACTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        RoundedView saveRoundedView = findViewById(R.id.add_contact_activity_save_rounded_view);
        saveRoundedView.setBorder(1, Design.GREY_COLOR);
        saveRoundedView.setColor(Design.WHITE_COLOR);

        ImageView saveIconView = findViewById(R.id.add_contact_activity_save_icon_view);
        saveIconView.setColorFilter(Design.BLACK_COLOR);

        TextView saveTextView = findViewById(R.id.add_contact_activity_save_text_view);
        Design.updateTextFont(saveTextView, Design.FONT_MEDIUM28);
        saveTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mShareView = findViewById(R.id.add_contact_activity_social_view);
        mShareView.setOnClickListener(v -> onSocialClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mShareView.getLayoutParams();
        marginLayoutParams.bottomMargin = - (int) (Design.BUTTON_HEIGHT * 0.5);

        radius = (int) (Design.BUTTON_HEIGHT * 0.5) * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable socialViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        socialViewBackground.getPaint().setColor(Design.getMainStyle());
        mShareView.setBackground(socialViewBackground);

        ImageView socialIconView = findViewById(R.id.add_contact_activity_social_icon_view);
        socialIconView.setColorFilter(Color.WHITE);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) socialIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_SHARE_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_SHARE_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        layoutParams = socialIconView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SHARE_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.width = (int) (DESIGN_SHARE_ICON_SIZE * Design.HEIGHT_RATIO);

        layoutParams = mShareView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView socialViewTitleView = findViewById(R.id.add_contact_activity_social_title_view);
        Design.updateTextFont(socialViewTitleView, Design.FONT_MEDIUM32);
        socialViewTitleView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) socialViewTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        mShareSubTitleView = findViewById(R.id.add_contact_activity_social_subtitle_view);
        Design.updateTextFont(mShareSubTitleView, Design.FONT_REGULAR24);
        mShareSubTitleView.setTextColor(Design.GREY_COLOR);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mShareSubTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SHARE_SUBTITLE_VIEW_MARGIN * Design.HEIGHT_RATIO + (Design.BUTTON_HEIGHT * 0.5f)) ;

        mSaveTwincodeView = findViewById(R.id.add_contact_activity_save_twincode_view);

        mCameraView = findViewById(R.id.add_contact_activity_camera_view);
        mCameraView.setOnClickListener(view -> {
            if (!mCameraGranted) {
                onSettingsClick();
            }
        });

        layoutParams = mCameraView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;

        radius = Design.POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable cameraViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        if (mCameraGranted) {
            cameraViewBackground.getPaint().setColor(Design.GREY_BACKGROUND_COLOR);
        } else {
            cameraViewBackground.getPaint().setColor(Design.BLACK_COLOR);
        }
        mCameraView.setBackground(cameraViewBackground);

        mMessageView = findViewById(R.id.add_contact_activity_camera_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR34);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        mTextureView = findViewById(R.id.add_contact_activity_texture_view);
        mTextureView.setSurfaceTextureListener(this);

        mViewFinder = findViewById(R.id.add_contact_activity_view_finder_view);
        mViewFinder.setDrawCorner(false);

        mImportFromGalleryView = findViewById(R.id.add_contact_activity_import_gallery_view);
        mImportFromGalleryView.setOnClickListener(v -> onImportFromGalleryClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mImportFromGalleryView.getLayoutParams();
        marginLayoutParams.bottomMargin = - (int) (Design.BUTTON_HEIGHT * 0.5);

        radius = (int) (Design.BUTTON_HEIGHT * 0.5) * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable importFromGalleryViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        importFromGalleryViewBackground.getPaint().setColor(Design.getMainStyle());
        mImportFromGalleryView.setBackground(importFromGalleryViewBackground);

        ImageView importFromGalleryIconView = findViewById(R.id.add_contact_activity_import_gallery_icon_view);
        importFromGalleryIconView.setColorFilter(Color.WHITE);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) importFromGalleryIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_SHARE_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_SHARE_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        layoutParams = importFromGalleryIconView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SHARE_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.width = (int) (DESIGN_SHARE_ICON_SIZE * Design.HEIGHT_RATIO);

        layoutParams = mImportFromGalleryView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView importFromGalleryTitleView = findViewById(R.id.add_contact_activity_import_gallery_title_view);
        Design.updateTextFont(importFromGalleryTitleView, Design.FONT_MEDIUM32);
        importFromGalleryTitleView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) importFromGalleryTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        mTwincodePasteView = findViewById(R.id.add_contact_activity_twincode_paste_view);
        radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable twincodeViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        twincodeViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mTwincodePasteView.setBackground(twincodeViewBackground);

        layoutParams = mTwincodePasteView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = (int) (DESIGN_INVITATION_CODE_HEIGHT * Design.HEIGHT_RATIO);

        ImageView copyTwincodeImageView = findViewById(R.id.add_contact_activity_twincode_copy_view);
        copyTwincodeImageView.setColorFilter(Design.PLACEHOLDER_COLOR);

        mTwincodeEditText = findViewById(R.id.add_contact_activity_twincode_edit_text);
        Design.updateTextFont(mTwincodeEditText, Design.FONT_REGULAR30);
        mTwincodeEditText.setTextColor(Design.FONT_COLOR_DEFAULT);
        mTwincodeEditText.setHintTextColor(Design.PLACEHOLDER_COLOR);
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

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTwincodeEditText.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_PASTE_TWINCODE_SMALL_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_PASTE_TWINCODE_SMALL_MARGIN * Design.HEIGHT_RATIO);

        mInviteView = findViewById(R.id.add_contact_activity_twincode_invite_view);
        mInviteView.setVisibility(View.GONE);
        mInviteView.setOnClickListener(v -> onInviteClick());

        RoundedView inviteRoundedView = findViewById(R.id.add_contact_activity_twincode_invite_add_rounded_view);
        inviteRoundedView.setColor(Design.getMainStyle());

        mInvitationCodeView = findViewById(R.id.add_contact_activity_invitation_code_view);

        mInvitationCodeView.setOnClickListener(view -> onInvitationCodeClick());

        radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable invitationCodeViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        invitationCodeViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mInvitationCodeView.setBackground(invitationCodeViewBackground);

        layoutParams = mInvitationCodeView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = (int) (DESIGN_INVITATION_CODE_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mInvitationCodeView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_INVITATION_CODE_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        TextView invitationCodeTitleView = findViewById(R.id.add_contact_activity_invitation_code_text_view);
        Design.updateTextFont(invitationCodeTitleView, Design.FONT_MEDIUM34);
        invitationCodeTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) invitationCodeTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.NEW_FEATURE_MARGIN;

        mInfoScanView = findViewById(R.id.add_contact_activity_scan_info_view);

        ImageView scanIconView = findViewById(R.id.add_contact_activity_scan_icon_view);
        scanIconView.setColorFilter(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) scanIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_SHARE_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_SHARE_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        layoutParams = scanIconView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SHARE_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.width = (int) (DESIGN_SHARE_ICON_SIZE * Design.HEIGHT_RATIO);

        TextView scanTitleView = findViewById(R.id.add_contact_activity_scan_message_view);
        Design.updateTextFont(scanTitleView, Design.FONT_MEDIUM32);
        scanTitleView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) scanTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        // Setup scan select according to current camera visibility.
        mScanSelect = mCameraView.getVisibility() == View.VISIBLE;

        initTabs();

        ViewTreeObserver viewTreeObserver = mMessageInviteView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = mMessageInviteView.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalLayoutListener(this);

                updateTwincodeHeight();
            }
        });

        updateViews();
    }

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (mInvitationMode == InvitationMode.SCAN) {

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
            mTwincodePasteView.setVisibility(View.VISIBLE);
            mTwincodeContainerView.setVisibility(View.GONE);
            mMessageInviteView.setVisibility(View.INVISIBLE);
            mShareView.setVisibility(View.GONE);
            mShareSubTitleView.setVisibility(View.INVISIBLE);
        } else {

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

            mCameraView.setVisibility(View.GONE);
            mImportFromGalleryView.setVisibility(View.GONE);
            mTwincodePasteView.setVisibility(View.GONE);
            mTwincodeContainerView.setVisibility(View.VISIBLE);
            mMessageInviteView.setVisibility(View.VISIBLE);
            mShareView.setVisibility(View.VISIBLE);
            mShareSubTitleView.setVisibility(View.VISIBLE);
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

        InputMethodManager inputMethodManager = (InputMethodManager) mTwincodeEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mTwincodeEditText.getWindowToken(), 0);
        }

        mTrustMethod = TrustMethod.LINK;
        Uri uri = Uri.parse(mTwincodeEditText.getText().toString());
        handleDecode(uri);
    }

    private void setUpdated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setUpdated");
        }

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTwincodeEditText.getLayoutParams();

        if (mTwincodeEditText.getText().toString().isEmpty()) {
            mInviteView.setVisibility(View.GONE);
            marginLayoutParams.rightMargin = (int) (DESIGN_PASTE_TWINCODE_SMALL_MARGIN * Design.HEIGHT_RATIO);
            marginLayoutParams.setMarginEnd((int) (DESIGN_PASTE_TWINCODE_SMALL_MARGIN * Design.HEIGHT_RATIO));
        } else {
            mInviteView.setVisibility(View.VISIBLE);
            marginLayoutParams.rightMargin = (int) (DESIGN_PASTE_TWINCODE_LARGE_MARGIN * Design.HEIGHT_RATIO);
            marginLayoutParams.setMarginEnd((int) (DESIGN_PASTE_TWINCODE_LARGE_MARGIN * Design.HEIGHT_RATIO));
        }
        mTwincodeEditText.setLayoutParams(marginLayoutParams);
    }

    private void onSocialClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSocialClick");
        }

        if (mProfile == null || mInvitationLink == null) {
            return;
        }
        String profileName = mProfile.getName();
        File file = new File(getExternalCacheDir() + "/qrcode.png");
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            mQRCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            // Exception could happen if there is no space left on the device.
            file = null;
            Log.e(LOG_TAG, "Cannot save QR-code: " + e.getMessage());
        }

        //noinspection UnnecessaryUnicodeEscape
        String name = profileName.replace('.', '\u2024').replace(':', '\u02d0');
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.add_contact_activity_invite_subject));

        if (file != null) {
            Uri uri = NamedFileProvider.getInstance().getUriForFile(this, file, name + "-QR-code.png");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(uri, "image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        }

        intent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.add_contact_activity_invite_message),
                mInvitationLink.uri, name));
        startActivity(Intent.createChooser(intent, null));
    }

    private void updateProfile() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateProfile");
        }

        if (mProfile == null) {

            return;
        }

        if (mProfile.getTwincodeOutboundId() != null) {
            mNameView.setText(mProfile.getName());
            mProfileService.getProfileImage(mProfile, (Bitmap avatar) -> {
                if (avatar != null) {
                    mAvatar = avatar;
                    mAvatarView.setImage(this, null, new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
                }
                updateQRCode();
            });
        }
    }

    private void updateQRCode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateQRCode");
        }

        if (mQRCodeView == null || mProfile == null || mInvitationLink == null) {

            return;
        }

        BitMatrix result;
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 0);
            result = new QRCodeWriter().encode(mInvitationLink.uri, BarcodeFormat.QR_CODE, QRCODE_PIXEL_WIDTH, QRCODE_PIXEL_HEIGHT, hints);
        } catch (Exception exception) {
            Log.e(LOG_TAG, "updateQrcode: exception=" + exception);

            return;
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        mQRCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mQRCodeBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        mQRCodeView.setImageBitmap(mQRCodeBitmap);
        mTwincodeView.setText(mInvitationLink.label);
    }

    private void onQRCodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onQRCodeClick");
        }

        if (mProfile == null) {
            return;
        }

        mZoomQRCode = !mZoomQRCode;
        float alpha = mZoomQRCode ? 0.0f : 1.0f;

        float qrCodeHeight = mZoomQRCode ? mQrCodeMaxHeight : mQrCodeInitialHeight;
        float qrCodeTop = mZoomQRCode ? (mTwincodeContainerView.getHeight() - mQrCodeMaxHeight) * 0.5f : mQrCodeInitialTop;
        long animateActionDelay = mZoomQRCode ? 0 : 100;
        long animateQRCodeDelay = mZoomQRCode ? 100 : 0;

        animateQRCodeAction(alpha, animateActionDelay);
        animateQRCodeSize(qrCodeTop, qrCodeHeight, animateQRCodeDelay);
    }

    private void onTwincodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTwincodeClick");
        }

        if (mInvitationLink != null) {
            org.twinlife.twinme.utils.Utils.setClipboard(this, mInvitationLink.uri);
            Toast.makeText(this, R.string.conversation_activity_menu_item_view_copy_message, Toast.LENGTH_SHORT).show();
        }
    }

    private void onSaveInGalleryClick() {

        Permission[] permissions = new Permission[]{Permission.WRITE_EXTERNAL_STORAGE};
        mDeferredSaveTwincode = true;
        if (checkPermissions(permissions)) {
            mDeferredSaveTwincode = false;
            mProfileService.getProfileImage(mProfile, (Bitmap avatar) -> {
                if (mProfile != null && mInvitationLink != null) {
                    mSaveTwincodeView.setTwincodeInformation(this, mProfile.getName(), avatar,
                            mQRCodeBitmap, mInvitationLink.label,
                            getString(R.string.fullscreen_qrcode_activity_save_message));
                }

                Bitmap bitmapToSave = getBitmapFromTwincodeView();
                if (bitmapToSave != null) {
                    new SaveTwincodeAsyncTask(this, bitmapToSave).execute();
                } else {
                    toast(getString(R.string.application_operation_failure));
                }
            });
        }
    }

    private void onPasteClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPasteClick");
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription() != null && (clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) || clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML))) {
            if (clipboard.getPrimaryClip() != null) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                String code = item.getText().toString();
                mTwincodeEditText.setText(code);
            }
        }
    }

    private void initTabs() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initTabs");
        }

        List<UICustomTab> customTabs = new ArrayList<>();
        customTabs.add(new UICustomTab(getString(R.string.add_contact_activity_invite), UICustomTab.CustomTabType.INVITE, mInvitationMode == InvitationMode.INVITE));
        customTabs.add(new UICustomTab(getString(R.string.add_contact_activity_scan_title), UICustomTab.CustomTabType.SCAN, mInvitationMode == InvitationMode.SCAN));

        mCustomTabView = findViewById(R.id.add_contact_activity_tab_view);

        ViewGroup.LayoutParams layoutParams = mCustomTabView.getLayoutParams();

        if (mInvitationMode == InvitationMode.INVITE_ONLY) {
            mCustomTabView.setVisibility(View.INVISIBLE);
            layoutParams.height = 1;
        } else {
            layoutParams.height = (int) (DESIGN_CUSTOM_TAB_VIEW_HEIGHT * Design.HEIGHT_RATIO);
            mCustomTabView.initTabs(customTabs, this);
            mCustomTabView.updateColor(Design.GREY_BACKGROUND_COLOR, Design.CUSTOM_TAB_GREY_COLOR, Design.BLACK_COLOR, -1);
        }
    }

    @Nullable
    private Bitmap getBitmapFromTwincodeView() {

        try {
            Bitmap bitmap = Bitmap.createBitmap(Design.DISPLAY_WIDTH, Design.DISPLAY_HEIGHT, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            mSaveTwincodeView.layout(0, 0, Design.DISPLAY_WIDTH, Design.DISPLAY_HEIGHT);
            mSaveTwincodeView.draw(canvas);
            return bitmap;

        } catch (Throwable exception) {

            if (Logger.ERROR) {
                Log.e(LOG_TAG, "Exception when creating bitmap", exception);
            }
            return null;
        }
    }

    private void onGenerateCodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGenerateCodeClick");
        }

        ViewGroup viewGroup = findViewById(R.id.add_contact_activity_layout);

        ResetInvitationConfirmView resetInvitationConfirmView = new ResetInvitationConfirmView(this, null);
        resetInvitationConfirmView.setAvatar(mAvatar, false);

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                mProfileService.changeProfileTwincode(() -> updateQRCode());
                resetInvitationConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                resetInvitationConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                resetInvitationConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(resetInvitationConfirmView);
                setStatusBarColor();
            }
        };
        resetInvitationConfirmView.setObserver(observer);

        viewGroup.addView(resetInvitationConfirmView);
        resetInvitationConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void onProfileViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onProfileViewClick");
        }

        if (mProfile == null) {
            return;
        }

        Intent intent = new Intent(this, ShowProfileActivity.class);
        intent.putExtra(INTENT_PROFILE_ID, mProfile.getId().toString());
        startActivity(intent);
    }

    private void onInvitationCodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInvitationCodeClick");
        }

        if (mProfile == null) {
            return;
        }

        if (mInvitationMode == InvitationMode.SCAN) {
            Intent intent = new Intent(this, EnterInvitationCodeActivity.class);
            intent.putExtra(INTENT_PROFILE_ID, mProfile.getId().toString());
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, InvitationCodeActivity.class);
            intent.putExtra(INTENT_PROFILE_ID, mProfile.getId().toString());
            startActivity(intent);
        }
    }

    @Override
    protected void handleDecode(@NonNull Uri uri) {
        if (DEBUG) {
            Log.d(LOG_TAG, "handleDecode: uri=" + uri);
        }

        mProfileService.parseURI(uri, this::onParseTwincodeURI);
    }

    private void onParseTwincodeURI(@NonNull ErrorCode errorCode, @Nullable TwincodeURI twincodeURI) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onParseTwincodeURI: errorCode=" + errorCode + " twincodeURI=" + twincodeURI);
        }

        // @todo Handle errors and report an accurate message:
        // ErrorCode.BAD_REQUEST: link is not well formed or not one of our link
        // ErrorCode.FEATURE_NOT_IMPLEMENTED: link does not target our application or domain.
        // ErrorCode.ITEM_NOT_FOUND: link targets the application but it is not compatible with the version.
        // TwincodeURI.Kind == Kind.AccountMigration => redirect to account migration
        // TwincodeURI.Kind == Kind.Call|Kind.Transfer => forbidden
        if (errorCode == ErrorCode.SUCCESS && twincodeURI != null) {
            if (twincodeURI.kind == TwincodeURI.Kind.Invitation) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(twincodeURI.uri));
                intent.putExtra(Intents.INTENT_TRUST_METHOD, mTrustMethod != null ? mTrustMethod : TrustMethod.QR_CODE);

                if (twincodeURI.twincodeOptions == null) {
                    intent.setClass(this, AcceptInvitationActivity.class);
                } else {
                    intent.setClass(this, AcceptInvitationSubscriptionActivity.class);
                }
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();

            } else if (twincodeURI.kind == TwincodeURI.Kind.Authenticate) {
                mProfileService.verifyAuthenticateURI(Uri.parse(twincodeURI.uri), ((BaseService.ErrorCode error, Contact contact) -> {
                    if (error == BaseService.ErrorCode.SUCCESS && contact != null) {
                        mProfileService.getImage(contact, (Bitmap avatar) -> showSuccessAuthentification(contact.getName(), avatar));
                    } else {
                        incorrectQRCode(getLinkError(errorCode, R.string.add_contact_activity_scan_error_incorect_link));
                    }
                }));
            } else if (twincodeURI.kind == TwincodeURI.Kind.Proxy) {
                addProxy(twincodeURI.twincodeOptions);
            } else {
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

        showAlertMessageView(R.id.add_contact_activity_layout, getString(R.string.deleted_account_activity_warning), message, false, this::finish);
    }

    private void showSuccessAuthentification(String name, Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showSuccessAuthentification");
        }

        ViewGroup viewGroup = findViewById(R.id.add_contact_activity_layout);

        SuccessAuthentifiedRelationView successAuthentifiedRelationView = new SuccessAuthentifiedRelationView(this, null);
        successAuthentifiedRelationView.setAvatar(avatar, false);
        successAuthentifiedRelationView.setTitle(name);

        String message = String.format(getString(R.string.authentified_relation_activity_certified_message), name);
        successAuthentifiedRelationView.setMessage(message);
        successAuthentifiedRelationView.setConfirmTitle(getString(R.string.application_ok));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                successAuthentifiedRelationView.animationCloseConfirmView();
                finish();
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

    private void updateTwincodeHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateTwincodeHeight");
        }

        int customTabViewHeight = (int) (DESIGN_CUSTOM_TAB_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        float viewHeight = (mInvitationCodeView.getY() + mInvitationCodeView.getHeight()) - mCustomTabView.getY();

        if (mInvitationMode == InvitationMode.INVITE_ONLY) {
            customTabViewHeight = 0;
            viewHeight = (mInvitationCodeView.getY() + mInvitationCodeView.getHeight()) - mProfileView.getY() - (DESIGN_PROFILE_VIEW_MARGIN * Design.HEIGHT_RATIO);
        }

        float spaceAboveInvitationCodeView;

        if (mMessageInviteView.getHeight() > (DESIGN_INVITATION_CODE_HEIGHT * Design.HEIGHT_RATIO)) {
            spaceAboveInvitationCodeView = mShareSubTitleView.getHeight() + Design.BUTTON_HEIGHT * 0.5f + ((DESIGN_MESSAGE_VIEW_MARGIN + DESIGN_SHARE_SUBTITLE_VIEW_MARGIN) * Design.HEIGHT_RATIO) + mMessageInviteView.getHeight();
        } else {
            spaceAboveInvitationCodeView = mShareSubTitleView.getHeight() + Design.BUTTON_HEIGHT * 0.5f + ((DESIGN_MESSAGE_VIEW_MARGIN + DESIGN_SHARE_SUBTITLE_VIEW_MARGIN + DESIGN_INVITATION_CODE_HEIGHT) * Design.HEIGHT_RATIO);
        }

        float maxHeight = viewHeight - customTabViewHeight - spaceAboveInvitationCodeView - (DESIGN_PROFILE_VIEW_HEIGHT + DESIGN_PROFILE_VIEW_MARGIN + DESIGN_INVITATION_CODE_HEIGHT + DESIGN_INVITATION_CODE_BOTTOM_MARGIN) * Design.HEIGHT_RATIO;

        ViewGroup.LayoutParams layoutParams = mTwincodeContainerView.getLayoutParams();
        layoutParams.height = (int) maxHeight;

        layoutParams = mCameraView.getLayoutParams();
        layoutParams.height = (int) maxHeight;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTwincodePasteView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (spaceAboveInvitationCodeView - (DESIGN_INVITATION_CODE_HEIGHT * Design.HEIGHT_RATIO));

        float qrCodeHeight = maxHeight - ((DESIGN_QR_CODE_TOP_MARGIN + DESIGN_TWINCODE_TOP_MARGIN + DESIGN_ACTION_TOP_MARGIN + DESIGN_ACTION_HEIGHT) * Design.HEIGHT_RATIO) - mTwincodeView.getHeight() - Design.BUTTON_HEIGHT;

        if (qrCodeHeight + (DESIGN_ZOOM_HEIGHT * Design.HEIGHT_RATIO * 2) > maxHeight) {
            qrCodeHeight = qrCodeHeight - (DESIGN_ZOOM_HEIGHT * Design.HEIGHT_RATIO * 2);
        }

        mQrCodeInitialHeight = qrCodeHeight;

        layoutParams = mQRCodeView.getLayoutParams();
        layoutParams.height = (int) qrCodeHeight;
        layoutParams.width = (int) qrCodeHeight;

        float twincodeViewContentHeight = qrCodeHeight + ((DESIGN_TWINCODE_TOP_MARGIN + DESIGN_ACTION_TOP_MARGIN + DESIGN_ACTION_HEIGHT) * Design.HEIGHT_RATIO) + mTwincodeView.getHeight() + Design.BUTTON_HEIGHT;

        mQrCodeInitialTop = Math.max(DESIGN_QR_CODE_TOP_MARGIN * Design.HEIGHT_RATIO, (maxHeight - twincodeViewContentHeight) * 0.5f);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mQRCodeView.getLayoutParams();
        marginLayoutParams.topMargin = (int) mQrCodeInitialTop;

        float qrCodeMaxHeight = Design.BUTTON_WIDTH - (DESIGN_TWINCODE_MARGIN * Design.WIDTH_RATIO * 2);

        if (qrCodeMaxHeight > (maxHeight - Design.BUTTON_HEIGHT)) {
            qrCodeMaxHeight = maxHeight -Design.BUTTON_HEIGHT;
        }

        mQrCodeMaxHeight = qrCodeMaxHeight;
    }

    private void animateQRCodeAction(float alpha, long delay) {
        if (DEBUG) {
            Log.d(LOG_TAG, "animateQRCodeAction");
        }

        PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, mSaveView.getAlpha(), alpha);

        List<Animator> animators = new ArrayList<>();
        animators.add(ObjectAnimator.ofPropertyValuesHolder(mCopyView, propertyValuesHolderAlpha));
        animators.add(ObjectAnimator.ofPropertyValuesHolder(mResetView, propertyValuesHolderAlpha));
        animators.add(ObjectAnimator.ofPropertyValuesHolder(mSaveView, propertyValuesHolderAlpha));
        animators.add(ObjectAnimator.ofPropertyValuesHolder(mZoomView, propertyValuesHolderAlpha));
        animators.add(ObjectAnimator.ofPropertyValuesHolder(mTwincodeView, propertyValuesHolderAlpha));

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.setStartDelay(delay);
        animatorSet.playTogether(animators);
        animatorSet.start();
    }

    private void animateQRCodeSize(float top, float height, long delay) {
        if (DEBUG) {
            Log.d(LOG_TAG, "animateQRCodeSize");
        }

        LayoutTransition layoutTransition = ((ViewGroup) mTwincodeContainerView).getLayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        layoutTransition.setStartDelay(LayoutTransition.CHANGING, delay);
        layoutTransition.setDuration(ANIMATION_DURATION);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mQRCodeView.getLayoutParams();
        marginLayoutParams.topMargin = (int) top;
        mQRCodeView.setLayoutParams(marginLayoutParams);

        ViewGroup.LayoutParams layoutParams = mQRCodeView.getLayoutParams();
        layoutParams.height = (int) height;
        layoutParams.width = (int) height;
        mQRCodeView.setLayoutParams(layoutParams);
    }

    private void addProxy(String proxy) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addProxy: proxy= " + proxy);
        }

        final List<ProxyDescriptor> proxies = getTwinmeContext().getConnectivityService().getUserProxies();
        if (proxies.size() >= ConnectivityService.MAX_PROXIES) {
            showAlertMessageView(R.id.add_contact_activity_layout, getString(R.string.deleted_account_activity_warning), String.format(getString(R.string.proxy_activity_limit), ConnectivityService.MAX_PROXIES), false, this::finish);
            return;
        }

        for (ProxyDescriptor proxyDescriptor : proxies) {
            if (proxyDescriptor.getDescriptor().equalsIgnoreCase(proxy)) {
                showAlertMessageView(R.id.add_contact_activity_layout, getString(R.string.deleted_account_activity_warning), getString(R.string.proxy_activity_already_use), false, null);
                return;
            }
        }

        ViewGroup viewGroup = findViewById(R.id.add_contact_activity_layout);

        DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(getString(R.string.proxy_activity_title));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("\n\n");
        int startSubTitle = spannableStringBuilder.length();
        spannableStringBuilder.append(proxy);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        defaultConfirmView.setSpannableTitle(spannableStringBuilder);
        defaultConfirmView.setMessage(getString(R.string.proxy_activity_url));
        defaultConfirmView.setImage(ResourcesCompat.getDrawable(getResources(),  R.drawable.onboarding_proxy, null));
        defaultConfirmView.setConfirmTitle(getString(R.string.proxy_activity_enable));
        defaultConfirmView.setCancelTitle(getString(R.string.application_cancel));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                defaultConfirmView.animationCloseConfirmView();
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

                if (fromConfirmAction) {
                    SNIProxyDescriptor proxyDescriptor = SNIProxyDescriptor.create(proxy);
                    if (proxyDescriptor == null) {
                        showAlertMessageView(R.id.add_contact_activity_layout, getString(R.string.deleted_account_activity_warning), getString(R.string.proxy_activity_invalid_format), false, null);
                        return;
                    }
                    proxies.add(proxyDescriptor);
                    getTwinmeContext().getConnectivityService().setUserProxies(proxies);

                    startActivity(SettingsAdvancedActivity.class);
                    finish();
                }
            }
        };

        defaultConfirmView.setObserver(observer);
        viewGroup.addView(defaultConfirmView);
        defaultConfirmView.show();

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }
}
