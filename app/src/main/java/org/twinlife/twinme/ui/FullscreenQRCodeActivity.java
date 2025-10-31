/*
 *  Copyright (c) 2018-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.percentlayout.widget.PercentRelativeLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinlife.util.Logger;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.services.ShareProfileService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.contacts.ResetInvitationConfirmView;
import org.twinlife.twinme.ui.conversationActivity.NamedFileProvider;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.SaveTwincodeAsyncTask;
import org.twinlife.twinme.utils.TwincodeView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class FullscreenQRCodeActivity extends AbstractTwinmeActivity implements ShareProfileService.Observer {
    private static final String LOG_TAG = "FullscreenQRCodeActi...";
    private static final boolean DEBUG = false;

    private static final float DESIGN_SHARE_PADDING = 20f;
    private static final float DESIGN_SHARE_ICON_SIZE = 42f;
    private static final float DESIGN_SHARE_ICON_PADDING = 27f;

    private static final int QRCODE_PIXEL_WIDTH = 295;
    private static final int QRCODE_PIXEL_HEIGHT = 295;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    static final int CONTACT_CREATED_RESULT = 1024;

    private boolean mDeferredSaveTwincode = false;
    private ImageView mQRCodeView;
    private TextView mTwincodeView;
    private View mGenerateCodeView;
    private TwincodeView mSaveTwincodeView;
    private Bitmap mQRCodeBitmap;
    private ShareProfileService mProfileService;
    @Nullable
    private Profile mProfile;
    @Nullable
    private TwincodeURI mInvitationLink;
    private Contact mRoom;
    private UUID mRoomId;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {

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
                TwinmeContext.Consumer<Bitmap> avatarConsumer = getAvatarConsumer();

                if (mProfile != null && mInvitationLink != null) {
                    mProfileService.getProfileImage(mProfile, avatarConsumer);
                } else if (mRoom != null && mInvitationLink != null) {
                    mProfileService.getImage(mRoom, avatarConsumer);
                } else {
                    toast(getString(R.string.application_operation_failure));
                }
            }
        }
    }

    @NonNull
    private TwinmeContext.Consumer<Bitmap> getAvatarConsumer() {

        return (Bitmap avatar) -> {
            final String name = mProfile != null ? mProfile.getName() : mRoom.getName();

            mSaveTwincodeView.setTwincodeInformation(this, name, avatar
                    , mQRCodeBitmap, mInvitationLink.label,
                    getString(R.string.fullscreen_qrcode_activity_save_message));
            Bitmap bitmapToSave = getBitmapFromTwincodeView();
            if (bitmapToSave != null) {
                new SaveTwincodeAsyncTask(this, bitmapToSave).execute();
            } else {
                toast(getString(R.string.application_operation_failure));
            }
        };
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        UUID profileId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_PROFILE_ID));
        mRoomId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));
        if (profileId == null && mRoomId == null) {
            finish();
            return;
        }

        initViews();
        mProfileService = new ShareProfileService(this, getTwinmeContext(), this);
        if (profileId != null) {
            mProfileService.getProfile(profileId);
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        if (mProfileService != null) {
            mProfileService.dispose();
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (mRoomId != null) {
            mProfileService.getRoom(mRoomId);
        }
    }

    @Override
    public void onGetDefaultProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProfile profile=" + profile);
        }

        if (mRoomId == null) {
            mProfile = profile;
            updateQRCode();
        }
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
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact contact=" + contact);
        }

        mRoom = contact;
        updateQRCode();
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }

        // Room was not found.
        finish();
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact contact=" + contact);
        }
    }

    @Override
    public void onCreateContact(@NonNull Contact contact) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateContact contact=" + contact);
        }

        showContactActivity(contact);

        // Propagate status to tell AddContactActivity that the contact is created.
        setResult(CONTACT_CREATED_RESULT);
        finish();
    }

    //
    // Private methods
    //

    private void initViews() {

        setContentView(R.layout.fullscreen_qrcode_activity_layout);

        setBackgroundColor(Color.BLACK);
        setStatusBarColor(Color.BLACK);
        showToolBar(false);
        showBackButton(true);

        setTitle(getString(R.string.add_contact_activity_title));

        View closeView = findViewById(R.id.fullscreen_qrcode_activity_close_view);
        closeView.setOnClickListener(v -> finish());

        ImageView closeImageView = findViewById(R.id.fullscreen_qrcode_activity_close_icon_view);
        closeImageView.setColorFilter(Color.WHITE);

        mQRCodeView = findViewById(R.id.fullscreen_qrcode_activity_qrcode_view);
        mQRCodeView.setImageBitmap(mQRCodeBitmap);

        mTwincodeView = findViewById(R.id.fullscreen_qrcode_activity_twincode_view);
        Design.updateTextFont(mTwincodeView, Design.FONT_REGULAR34);
        mTwincodeView.setTextColor(Color.WHITE);
        mTwincodeView.setOnClickListener(v -> onTwincodeClick());

        mGenerateCodeView = findViewById(R.id.fullscreen_qrcode_activity_reset_clickable_view);
        mGenerateCodeView.setOnClickListener(v -> onGenerateCodeClick());

        RoundedView resetRoundedView = findViewById(R.id.fullscreen_qrcode_activity_reset_rounded_view);
        resetRoundedView.setBorder(1, Design.GREY_COLOR);
        resetRoundedView.setColor(Color.BLACK);

        ImageView resetIconView = findViewById(R.id.fullscreen_qrcode_activity_reset_icon_view);
        resetIconView.setColorFilter(Color.WHITE);

        TextView resetTextView = findViewById(R.id.fullscreen_qrcode_activity_reset_text_view);
        Design.updateTextFont(resetTextView, Design.FONT_MEDIUM28);
        resetTextView.setTextColor(Color.WHITE);

        View saveView = findViewById(R.id.fullscreen_qrcode_activity_save_clickable_view);
        saveView.setOnClickListener(v -> onSaveInGalleryClick());

        RoundedView saveRoundedView = findViewById(R.id.fullscreen_qrcode_activity_save_rounded_view);
        saveRoundedView.setBorder(1, Design.GREY_COLOR);
        saveRoundedView.setColor(Color.BLACK);

        ImageView saveIconView = findViewById(R.id.fullscreen_qrcode_activity_save_icon_view);
        saveIconView.setColorFilter(Color.WHITE);

        TextView saveTextView = findViewById(R.id.fullscreen_qrcode_activity_save_text_view);
        Design.updateTextFont(saveTextView, Design.FONT_MEDIUM28);
        saveTextView.setTextColor(Color.WHITE);

        View shareView = findViewById(R.id.fullscreen_qrcode_activity_social_view);
        shareView.setOnClickListener(v -> onSocialClick());

        float radius = (int) (Design.BUTTON_HEIGHT * 0.5) * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable socialViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        socialViewBackground.getPaint().setColor(Design.getMainStyle());
        shareView.setBackground(socialViewBackground);

        ImageView socialIconView = findViewById(R.id.fullscreen_qrcode_activity_social_icon_view);
        socialIconView.setColorFilter(Color.WHITE);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) socialIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_SHARE_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_SHARE_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        ViewGroup.LayoutParams layoutParams = socialIconView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SHARE_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.width = (int) (DESIGN_SHARE_ICON_SIZE * Design.HEIGHT_RATIO);

        layoutParams = shareView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView socialViewTitleView = findViewById(R.id.fullscreen_qrcode_activity_social_title_view);
        Design.updateTextFont(socialViewTitleView, Design.FONT_MEDIUM32);
        socialViewTitleView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) socialViewTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        TextView shareViewSubTitleView = findViewById(R.id.fullscreen_qrcode_activity_social_subtitle_view);
        Design.updateTextFont(shareViewSubTitleView, Design.FONT_REGULAR24);
        shareViewSubTitleView.setTextColor(Design.GREY_COLOR);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) shareViewSubTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (Design.BUTTON_HEIGHT * 0.5);

        mProgressBarView = findViewById(R.id.fullscreen_qrcode_activity_progress_indicator_view);

        mSaveTwincodeView = findViewById(R.id.fullscreen_qrcode_activity_save_twincode_view);
    }

    private void updateQRCode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateQRCode");
        }

        if (mQRCodeView == null || (mProfile == null && mRoom == null) || mInvitationLink == null) {

            return;
        }

        if (mProfile != null) {
            mGenerateCodeView.setVisibility(View.VISIBLE);
        } else {
            mGenerateCodeView.setVisibility(View.GONE);
        }
        mTwincodeView.setText(mInvitationLink.label);

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

    private void onSocialClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSocialClick");
        }

        if ((mProfile == null && mRoom == null) || mInvitationLink == null) {
            return;
        }

        String name;
        if (mProfile != null) {
            name = mProfile.getName();
        } else {
            name = mRoom.getName();
        }

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
        String invitationName = name.replace('.', '\u2024').replace(':', '\u02d0');
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.add_contact_activity_invite_subject));

        if (file != null) {
            Uri uri = NamedFileProvider.getInstance().getUriForFile(this, file, invitationName + "-QR-code.png");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(uri, "image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        }

        intent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.add_contact_activity_invite_message),
                mInvitationLink.uri, invitationName));
        startActivity(Intent.createChooser(intent, null));
    }

    private void onSaveInGalleryClick() {

        Permission[] permissions = new Permission[]{Permission.WRITE_EXTERNAL_STORAGE};
        mDeferredSaveTwincode = true;
        if (checkPermissions(permissions)) {
            mDeferredSaveTwincode = false;

            TwinmeContext.Consumer<Bitmap> avatarConsumer = getAvatarConsumer();

            if (mProfile != null && mInvitationLink != null) {
                mProfileService.getProfileImage(mProfile, avatarConsumer);
            } else if (mRoom != null && mInvitationLink != null) {
                mProfileService.getImage(mRoom, avatarConsumer);
            } else {
                toast(getString(R.string.application_operation_failure));
            }
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

        mProfileService.getProfileImage(mProfile, (Bitmap avatar) -> {
            PercentRelativeLayout percentRelativeLayout = findViewById(R.id.fullscreen_qrcode_activity_layout);

            ResetInvitationConfirmView resetInvitationConfirmView = new ResetInvitationConfirmView(this, null);
            PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            resetInvitationConfirmView.setLayoutParams(layoutParams);
            resetInvitationConfirmView.setAvatar(avatar, false);

            AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
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
                    percentRelativeLayout.removeView(resetInvitationConfirmView);
                }
            };
            resetInvitationConfirmView.setObserver(observer);

            percentRelativeLayout.addView(resetInvitationConfirmView);
            resetInvitationConfirmView.show();
        });

    }
}
