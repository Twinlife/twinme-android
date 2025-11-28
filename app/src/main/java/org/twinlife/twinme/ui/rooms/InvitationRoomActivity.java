/*
 *  Copyright (c) 2021-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.rooms;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinlife.util.Logger;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.services.InvitationRoomService;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.conversationActivity.NamedFileProvider;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.SaveTwincodeAsyncTask;
import org.twinlife.twinme.utils.TwincodeView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InvitationRoomActivity extends AbstractTwinmeActivity implements InvitationRoomService.Observer {
    private static final String LOG_TAG = "InvitationRoomActivity";
    private static final boolean DEBUG = false;

    private static final int ANIMATION_DURATION = 100;

    private static final float DESIGN_ROOM_VIEW_HEIGHT = 92f;
    private static final float DESIGN_NAME_VIEW_MARGIN = 20f;
    private static final float DESIGN_CONTAINER_HEIGHT = 720;
    private static final float DESIGN_QRCODE_SIZE = 316;
    private static final float DESIGN_MESSAGE_VIEW_MARGIN = 24f;
    private static final float DESIGN_SHARE_SUBTITLE_VIEW_MARGIN = 10f;
    private static final float DESIGN_ZOOM_MARGIN = 21f;
    private static final float DESIGN_SHARE_PADDING = 20f;
    private static final float DESIGN_SHARE_ICON_SIZE = 42f;
    private static final float DESIGN_SHARE_ICON_PADDING = 27f;
    private static final float DESIGN_ZOOM_HEIGHT = 70;
    private static final float DESIGN_QR_CODE_TOP_MARGIN = 60;
    private static final float DESIGN_ROOM_VIEW_TOP_MARGIN = 80f;
    private static final float DESIGN_ROOM_VIEW_BOTTOM_MARGIN = 40f;
    private static final float DESIGN_CONTAINER_MARGIN = 30;
    private static final float DESIGN_ACTION_TOP_MARGIN = 64;
    private static final float DESIGN_ACTION_HEIGHT = 128;

    private static final int QRCODE_PIXEL_WIDTH = 295;
    private static final int QRCODE_PIXEL_HEIGHT = 295;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    private CircularImageView mAvatarView;

    private View mContainerView;
    private ImageView mQRCodeView;
    private TextView mNameView;
    private TextView mRoomView;
    private View mZoomView;
    private View mCopyView;
    private View mSaveView;
    private Bitmap mQRCodeBitmap;

    private TwincodeView mSaveTwincodeView;

    private String mRoomName;
    @Nullable
    private Contact mRoom;
    @Nullable
    private TwincodeURI mInvitationLink;

    @Nullable
    private Bitmap mAvatar;

    private float mQrCodeInitialTop = 0;
    private float mQrCodeInitialHeight = 0;
    private float mQrCodeMaxHeight = 0;
    private boolean mZoomQRCode = false;

    private boolean mDeferredSaveTwincode = false;

    private InvitationRoomService mInvitationRoomService;

    //
    // Override TwinmeActivityImpl methods
    //


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        UUID roomId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));

        if (roomId == null) {
            finish();
            return;
        }

        mRoomName = intent.getStringExtra(Intents.INTENT_ROOM_NAME);

        initViews();

        mInvitationRoomService = new InvitationRoomService(this, getTwinmeContext(), this, roomId);
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        updateQRCode();
    }

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
                saveTwincodeInGallery();
            }
        }
    }

    //
    // Implement InvitationRoomService.Observer methods
    //

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact contact=" + contact + " avatar=" + avatar);
        }

        mRoom = contact;
        updateRoom();
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
            Log.d(LOG_TAG, "onUpdateContact contact=" + contact + " avatar=" + avatar);
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
    public void onUpdateImage(@NonNull Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateImage avatar=" + avatar);
        }

    }

    @Override
    public void onDeleteContact(@NonNull UUID contactId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteContact contactId=" + contactId);
        }

    }

    @Override
    public void onGetContacts(@NonNull List<Contact> contacts) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContacts contacts=" + contacts);
        }
    }

    @Override
    public void onSendTwincodeToContacts() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendTwincodeToContacts");
        }
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.invitation_room_activity);

        setStatusBarColor();
        setToolBar(R.id.invitation_room_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.show_room_activity_invite_participants));
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        applyInsets(R.id.invitation_room_activity_layout, R.id.invitation_room_activity_tool_bar, R.id.settings_room_activity_list_view, Design.TOOLBAR_COLOR, false);

        View backgroundView = findViewById(R.id.invitation_room_activity_background);
        backgroundView.setBackgroundColor(Design.GREY_BACKGROUND_COLOR);

        View roomView = findViewById(R.id.invitation_room_activity_room_view);

        ViewGroup.LayoutParams layoutParams = roomView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ROOM_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) roomView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ROOM_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_ROOM_VIEW_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        mAvatarView = findViewById(R.id.invitation_room_activity_avatar_view);

        mNameView = findViewById(R.id.invitation_room_activity_name_view);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM32);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_NAME_VIEW_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_NAME_VIEW_MARGIN * Design.WIDTH_RATIO));

        mContainerView = findViewById(R.id.invitation_room_activity_container_view);

        layoutParams = mContainerView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = (int) (DESIGN_CONTAINER_HEIGHT * Design.HEIGHT_RATIO);

        float radius = Design.POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable containerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        containerViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mContainerView.setBackground(containerViewBackground);

        mQRCodeView = findViewById(R.id.invitation_room_activity_qrcode_view);
        mQRCodeView.setOnClickListener(view -> onQRCodeClick());

        layoutParams = mQRCodeView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_QRCODE_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_QRCODE_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mQRCodeView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_QR_CODE_TOP_MARGIN * Design.HEIGHT_RATIO);

        mZoomView = findViewById(R.id.invitation_room_activity_zoom_view);
        mZoomView.setOnClickListener(v -> onQRCodeClick());

        layoutParams = mZoomView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ZOOM_HEIGHT * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ZOOM_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mZoomView.getLayoutParams();
        marginLayoutParams.topMargin = - (int) (DESIGN_ZOOM_MARGIN * Design.HEIGHT_RATIO);

        RoundedView zoomRoundedView = findViewById(R.id.invitation_room_activity_zoom_rounded_view);
        zoomRoundedView.setBorder(1, Design.GREY_COLOR);
        zoomRoundedView.setColor(Design.WHITE_COLOR);

        ImageView zoomIconView = findViewById(R.id.invitation_room_activity_zoom_icon_view);
        zoomIconView.setColorFilter(Design.BLACK_COLOR);

        mRoomView = findViewById(R.id.invitation_room_activity_room_id_view);
        Design.updateTextFont(mRoomView, Design.FONT_BOLD28);
        mRoomView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mRoomView.setOnClickListener(v -> onCopyClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mRoomView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.WIDTH_RATIO);

        mCopyView = findViewById(R.id.invitation_room_activity_copy_clickable_view);
        mCopyView.setOnClickListener(v -> onCopyClick());

        layoutParams = mCopyView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ACTION_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCopyView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ACTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        RoundedView copyRoundedView = findViewById(R.id.invitation_room_activity_copy_rounded_view);
        copyRoundedView.setBorder(1, Design.GREY_COLOR);
        copyRoundedView.setColor(Design.WHITE_COLOR);

        ImageView copyIconView = findViewById(R.id.invitation_room_activity_copy_icon_view);
        copyIconView.setColorFilter(Design.BLACK_COLOR);

        TextView copyTextView = findViewById(R.id.invitation_room_activity_copy_text_view);
        Design.updateTextFont(copyTextView, Design.FONT_MEDIUM28);
        copyTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mSaveView = findViewById(R.id.invitation_room_activity_save_clickable_view);
        mSaveView.setOnClickListener(v -> onSaveInGalleryClick());

        layoutParams = mSaveView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ACTION_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSaveView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ACTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        RoundedView saveRoundedView = findViewById(R.id.invitation_room_activity_save_rounded_view);
        saveRoundedView.setBorder(1, Design.GREY_COLOR);
        saveRoundedView.setColor(Design.WHITE_COLOR);

        ImageView saveIconView = findViewById(R.id.invitation_room_activity_save_icon_view);
        saveIconView.setColorFilter(Design.BLACK_COLOR);

        TextView saveTextView = findViewById(R.id.invitation_room_activity_save_text_view);
        Design.updateTextFont(saveTextView, Design.FONT_MEDIUM28);
        saveTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View shareView = findViewById(R.id.invitation_room_activity_social_view);
        shareView.setOnClickListener(v -> onSocialClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) shareView.getLayoutParams();
        marginLayoutParams.bottomMargin = - (int) (Design.BUTTON_HEIGHT * 0.5);

        radius = (int) (Design.BUTTON_HEIGHT * 0.5) * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable socialViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        socialViewBackground.getPaint().setColor(Design.getMainStyle());
        shareView.setBackground(socialViewBackground);

        ImageView socialIconView = findViewById(R.id.invitation_room_activity_social_icon_view);
        socialIconView.setColorFilter(Color.WHITE);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) socialIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_SHARE_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_SHARE_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        layoutParams = socialIconView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SHARE_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.width = (int) (DESIGN_SHARE_ICON_SIZE * Design.HEIGHT_RATIO);

        layoutParams = shareView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView socialViewTitleView = findViewById(R.id.invitation_room_activity_social_title_view);
        Design.updateTextFont(socialViewTitleView, Design.FONT_MEDIUM32);
        socialViewTitleView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) socialViewTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        TextView shareSubTitleView = findViewById(R.id.invitation_room_activity_social_subtitle_view);
        Design.updateTextFont(shareSubTitleView, Design.FONT_REGULAR24);
        shareSubTitleView.setTextColor(Design.GREY_COLOR);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) shareSubTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SHARE_SUBTITLE_VIEW_MARGIN * Design.HEIGHT_RATIO + (Design.BUTTON_HEIGHT * 0.5f)) ;

        TextView messageTextView = findViewById(R.id.invitation_room_activity_message_view);
        Design.updateTextFont(messageTextView, Design.FONT_MEDIUM28);
        messageTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) messageTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_VIEW_MARGIN * Design.WIDTH_RATIO);

        mSaveTwincodeView = findViewById(R.id.invitation_room_activity_save_twincode_view);

        mQrCodeInitialHeight = DESIGN_QRCODE_SIZE * Design.HEIGHT_RATIO;
        mQrCodeInitialTop = DESIGN_QR_CODE_TOP_MARGIN * Design.HEIGHT_RATIO;
        mQrCodeMaxHeight = Design.BUTTON_WIDTH - (DESIGN_CONTAINER_MARGIN * 2 * Design.WIDTH_RATIO);
    }

    private void updateRoom() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateRoom");
        }

        if (mRoom != null && mRoom.getPublicPeerTwincodeOutboundId() != null) {
            mNameView.setText(mRoom.getName());
            mInvitationRoomService.getImage(mRoom, (Bitmap avatar) -> {
                if (avatar != null) {
                    mAvatar = avatar;
                    mAvatarView.setImage(this, null, new CircularImageDescriptor(mAvatar, 0.5f, 0.5f, 0.5f));
                }
                updateQRCode();
            });
        } else {
            updateQRCode();
        }
    }

    private void updateQRCode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateQRCode");
        }

        if (mQRCodeView == null || mInvitationLink == null) {

            return;
        }

        mRoomView.setText(mInvitationLink.label);

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

    private void onSocialClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSocialClick");
        }

        if (mInvitationLink == null || mRoomName == null) {
            return;
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
        String name = mRoomName.replace('.', '\u2024').replace(':', '\u02d0');
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

    private void onQRCodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onQRCodeClick");
        }

        if (mRoom == null) {
            return;
        }

        mZoomQRCode = !mZoomQRCode;
        float alpha = mZoomQRCode ? 0.0f : 1.0f;

        float qrCodeHeight = mZoomQRCode ? mQrCodeMaxHeight : mQrCodeInitialHeight;
        float qrCodeTop = mZoomQRCode ? (mContainerView.getHeight() - mQrCodeMaxHeight) * 0.5f : mQrCodeInitialTop;
        long animateActionDelay = mZoomQRCode ? 0 : 100;
        long animateQRCodeDelay = mZoomQRCode ? 100 : 0;

        animateQRCodeAction(alpha, animateActionDelay);
        animateQRCodeSize(qrCodeTop, qrCodeHeight, animateQRCodeDelay);
    }

    private void onCopyClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCopyClick");
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
            saveTwincodeInGallery();
        }
    }

    private void saveTwincodeInGallery() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveTwincodeInGallery");
        }

        if (mRoom != null && mInvitationLink != null && mQRCodeBitmap != null) {
            mSaveTwincodeView.setTwincodeInformation(this, mRoom.getName(), mAvatar
                    , mQRCodeBitmap, mInvitationLink.label,
                    getString(R.string.fullscreen_qrcode_activity_save_message));

            Bitmap bitmapToSave = getBitmapFromTwincodeView();
            if (bitmapToSave != null) {
                new SaveTwincodeAsyncTask(this, bitmapToSave).execute();
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

    private void animateQRCodeAction(float alpha, long delay) {
        if (DEBUG) {
            Log.d(LOG_TAG, "animateQRCodeAction");
        }

        PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, mSaveView.getAlpha(), alpha);

        List<Animator> animators = new ArrayList<>();
        animators.add(ObjectAnimator.ofPropertyValuesHolder(mCopyView, propertyValuesHolderAlpha));
        animators.add(ObjectAnimator.ofPropertyValuesHolder(mSaveView, propertyValuesHolderAlpha));
        animators.add(ObjectAnimator.ofPropertyValuesHolder(mZoomView, propertyValuesHolderAlpha));
        animators.add(ObjectAnimator.ofPropertyValuesHolder(mRoomView, propertyValuesHolderAlpha));

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

        LayoutTransition layoutTransition = ((ViewGroup) mContainerView).getLayoutTransition();
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
}
