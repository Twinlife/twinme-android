/*
 *  Copyright (c) 2021-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.rooms;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.services.InvitationRoomService;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.FullscreenQRCodeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.ScanActivity;
import org.twinlife.twinme.ui.conversationActivity.NamedFileProvider;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.RoundedView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InvitationRoomActivity extends AbstractTwinmeActivity implements InvitationRoomService.Observer {
    private static final String LOG_TAG = "InvitationRoomActivity";
    private static final boolean DEBUG = false;

    private static final float DESIGN_BUTTON_BOTTOM = 16f;
    private static final float DESIGN_SOCIAL_BOTTOM = 62f;
    private static final float DESIGN_SOCIAL_SUBTITLE_BOTTOM = 34f;

    private static final int QRCODE_PIXEL_WIDTH = 295;
    private static final int QRCODE_PIXEL_HEIGHT = 295;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    private CircularImageView mAvatarView;
    private ImageView mQRCodeView;
    private TextView mNameView;
    private TextView mTwincodeView;
    private Bitmap mQRCodeBitmap;

    private String mRoomName;
    private UUID mRoomId;
    @Nullable
    private Contact mRoom;
    @Nullable
    private TwincodeURI mInvitationLink;

    private InvitationRoomService mInvitationRoomService;

    //
    // Override TwinmeActivityImpl methods
    //


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mRoomId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));
        mRoomName = intent.getStringExtra(Intents.INTENT_ROOM_NAME);

        initViews();

        mInvitationRoomService = new InvitationRoomService(this, getTwinmeContext(), this, mRoomId);
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
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu);
        }

        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_contact_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.share_action);
        ImageView imageView = (ImageView) menuItem.getActionView();

        if (imageView != null) {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.action_bar_share, null));
            imageView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);
            imageView.setOnClickListener(view -> onSocialClick());
        }

        return true;
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

        RoundedView avatarRoundedView = findViewById(R.id.invitation_room_activity_avatar_rounded_view);
        avatarRoundedView.setColor(Design.POPUP_BACKGROUND_COLOR);

        mAvatarView = findViewById(R.id.invitation_room_activity_avatar_view);

        View roomView = findViewById(R.id.invitation_room_activity_room_view);
        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable roomViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        roomViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        roomView.setBackground(roomViewBackground);

        mNameView = findViewById(R.id.invitation_room_activity_name_view);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR32);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mQRCodeView = findViewById(R.id.invitation_room_activity_qrcode_view);
        mQRCodeView.setOnClickListener(view -> onQRCodeClick());

        mTwincodeView = findViewById(R.id.invitation_room_activity_twincode_view);
        Design.updateTextFont(mTwincodeView, Design.FONT_REGULAR30);
        mTwincodeView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mTwincodeView.setOnClickListener(v -> onTwincodeClick());

        TextView messageView = findViewById(R.id.invitation_room_activity_message_view);
        Design.updateTextFont(messageView, Design.FONT_REGULAR30);
        messageView.setTextColor(Design.FONT_COLOR_GREY);

        View inviteView = findViewById(R.id.invitation_room_activity_invite_view);
        inviteView.setOnClickListener(v -> onInviteClick());

        ShapeDrawable inviteViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        inviteViewBackground.getPaint().setColor(Design.getMainStyle());
        inviteView.setBackground(inviteViewBackground);

        ViewGroup.LayoutParams layoutParams = inviteView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) inviteView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_BUTTON_BOTTOM * Design.HEIGHT_RATIO);

        TextView inviteTitleView = findViewById(R.id.invitation_room_activity_invite_title_view);
        Design.updateTextFont(inviteTitleView, Design.FONT_BOLD28);
        inviteTitleView.setTextColor(Design.WHITE_COLOR);

        View scanView = findViewById(R.id.invitation_room_activity_scan_view);
        scanView.setOnClickListener(v -> onScanClick());

        ShapeDrawable scanViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scanViewBackground.getPaint().setColor(Design.BLACK_COLOR);
        scanView.setBackground(scanViewBackground);

        layoutParams = scanView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) scanView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_BUTTON_BOTTOM * Design.HEIGHT_RATIO);

        TextView scanTitleView = findViewById(R.id.invitation_room_activity_scan_title_view);
        Design.updateTextFont(scanTitleView, Design.FONT_BOLD28);
        scanTitleView.setTextColor(Design.WHITE_COLOR);

        View socialView = findViewById(R.id.invitation_room_activity_social_view);
        socialView.setOnClickListener(v -> onSocialClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) socialView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_SOCIAL_BOTTOM * Design.HEIGHT_RATIO);

        ShapeDrawable socialViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        socialViewBackground.getPaint().setColor(Design.getMainStyle());
        socialView.setBackground(socialViewBackground);

        layoutParams = socialView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView socialViewTitleView = findViewById(R.id.invitation_room_activity_social_title_view);
        Design.updateTextFont(socialViewTitleView, Design.FONT_BOLD28);
        socialViewTitleView.setTextColor(Color.WHITE);

        TextView socialViewSubTitleView = findViewById(R.id.invitation_room_activity_social_subtitle_view);
        Design.updateTextFont(socialViewSubTitleView, Design.FONT_REGULAR24);
        socialViewSubTitleView.setTextColor(Design.GREY_COLOR);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) socialViewSubTitleView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_SOCIAL_SUBTITLE_BOTTOM * Design.HEIGHT_RATIO);
    }

    private void updateRoom() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateRoom");
        }

        if (mRoom != null && mRoom.getPublicPeerTwincodeOutboundId() != null) {
            mNameView.setText(mRoom.getName());
            mInvitationRoomService.getImage(mRoom, (Bitmap avatar) -> {
                if (avatar != null) {
                    mAvatarView.setImage(this, null, new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
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

    private void onInviteClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteClick");
        }

        Intent intent = new Intent(this, AddParticipantsRoomActivity.class);
        intent.putExtra(Intents.INTENT_CONTACT_ID, mRoomId.toString());
        startActivity(intent);
    }

    private void onTwincodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCopyClick");
        }

        if (mInvitationLink != null) {
            org.twinlife.twinme.utils.Utils.setClipboard(this, mInvitationLink.uri);
            Toast.makeText(this, R.string.conversation_activity_menu_item_view_copy_message, Toast.LENGTH_SHORT).show();
        }
    }

    private void onQRCodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onQRCodeClick");
        }

        if (mRoom == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(getApplication(), FullscreenQRCodeActivity.class);
        intent.putExtra(Intents.INTENT_CONTACT_ID, mRoomId.toString());
        startActivity(intent);
    }

    private void onScanClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onScanClick");
        }

        if (mRoom == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(getApplication(), ScanActivity.class);
        startActivity(intent);
    }
}
