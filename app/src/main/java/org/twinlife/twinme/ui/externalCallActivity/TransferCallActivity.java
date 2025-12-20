/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.EditIdentityActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.contacts.DeleteConfirmView;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.RoundedView;

public class TransferCallActivity extends AbstractInvitationCallReceiverActivity {
    private static final String LOG_TAG = "TransferCallActivity";
    private static final boolean DEBUG = false;

    private class RemoveListener implements View.OnClickListener {

        private boolean disabled = true;

        @Override
        public void onClick(View view) {
            if (DEBUG) {
                Log.d(LOG_TAG, "RemoveListener.onClick: view=" + view);
            }

            if (disabled) {

                return;
            }
            disabled = true;

            onRemoveClick();
        }

        void enable() {

            disabled = false;
        }
    }

    private RemoveListener mRemoveListener;

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
    // Private methods
    //

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.transfer_call_activity);

        setStatusBarColor(DESIGN_NAVIGATION_BAR_COLOR);
        setToolBar(R.id.transfer_call_activity_tool_bar, DESIGN_NAVIGATION_BAR_COLOR);
        showToolBar(true);
        showBackButton(true);

        setBackgroundColor(Color.BLACK);

        setTitle(getString(R.string.premium_services_activity_transfert_title));

        applyInsets(R.id.invitation_external_call_activity_layout, R.id.transfer_call_activity_tool_bar, R.id.transfer_call_activity_background, DESIGN_NAVIGATION_BAR_COLOR, false);

        View backgroundView = findViewById(R.id.transfer_call_activity_background);
        backgroundView.setBackgroundColor(Color.BLACK);

        View invitationView = findViewById(R.id.transfer_call_activity_invitation_view);
        float radius = DESIGN_CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable invitationViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        invitationViewBackground.getPaint().setColor(DESIGN_INVITATION_VIEW_COLOR);

        ShapeDrawable invitationViewBorder = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        invitationViewBorder.getPaint().setColor(DESIGN_INVITATION_VIEW_BORDER_COLOR);
        invitationViewBorder.getPaint().setStyle(Paint.Style.STROKE);
        invitationViewBorder.getPaint().setStrokeWidth(DESIGN_CONTAINER_BORDER);

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{invitationViewBackground, invitationViewBorder});
        ViewCompat.setBackground(invitationView, layerDrawable);

        View headerView = findViewById(R.id.transfer_call_activity_header_view);
        outerRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};

        ShapeDrawable headerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        headerViewBackground.getPaint().setColor(DESIGN_HEADER_COLOR);

        ShapeDrawable headerViewBorder = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        headerViewBorder.getPaint().setColor(DESIGN_INVITATION_VIEW_BORDER_COLOR);
        headerViewBorder.getPaint().setStyle(Paint.Style.STROKE);
        headerViewBorder.getPaint().setStrokeWidth(DESIGN_CONTAINER_BORDER);

        LayerDrawable headerLayerDrawable = new LayerDrawable(new Drawable[]{headerViewBackground, headerViewBorder});
        ViewCompat.setBackground(headerView, headerLayerDrawable);

        ViewGroup.LayoutParams layoutParams = headerView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_HEADER_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        RoundedView redRoundedView = findViewById(R.id.transfer_call_activity_red_rounded_view);
        redRoundedView.setColor(DESIGN_RED_VIEW_COLOR);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) redRoundedView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ROUNDED_VIEW_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_ROUNDED_VIEW_MARGIN * Design.WIDTH_RATIO));

        RoundedView yellowRoundedView = findViewById(R.id.transfer_call_activity_yellow_rounded_view);
        yellowRoundedView.setColor(DESIGN_YELLOW_VIEW_COLOR);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) yellowRoundedView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ROUNDED_VIEW_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_ROUNDED_VIEW_MARGIN * Design.WIDTH_RATIO));

        RoundedView greenRoundedView = findViewById(R.id.transfer_call_activity_green_rounded_view);
        greenRoundedView.setColor(DESIGN_GREEN_VIEW_COLOR);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) greenRoundedView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ROUNDED_VIEW_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_ROUNDED_VIEW_MARGIN * Design.WIDTH_RATIO));

        View nameContainerView = findViewById(R.id.transfer_call_activity_name_container_view);
        radius = (DESIGN_AVATAR_VIEW_HEIGHT * 0.5f) * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable nameViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        nameViewBackground.getPaint().setColor(DESIGN_NAME_VIEW_COLOR);
        ViewCompat.setBackground(nameContainerView, nameViewBackground);

        int rightMargin = (int) ((DESIGN_AVATAR_VIEW_HEIGHT * Design.HEIGHT_RATIO) + ((DESIGN_ROUNDED_VIEW_MARGIN + DESIGN_AVATAR_VIEW_MARGIN) * Design.WIDTH_RATIO));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) nameContainerView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_ROUNDED_VIEW_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = rightMargin;
        marginLayoutParams.setMarginStart((int) (DESIGN_ROUNDED_VIEW_MARGIN * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd(rightMargin);

        mNameView = findViewById(R.id.transfer_call_activity_name_view);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM32);
        mNameView.setTextColor(Color.WHITE);

        mAvatarView = findViewById(R.id.transfer_call_activity_avatar_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mAvatarView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_AVATAR_VIEW_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginEnd((int) (DESIGN_AVATAR_VIEW_MARGIN * Design.WIDTH_RATIO));

        TextView messageInviteView = findViewById(R.id.transfer_call_activity_message_view);
        Design.updateTextFont(messageInviteView, Design.FONT_REGULAR30);
        messageInviteView.setTextColor(Color.WHITE);

        View qrCodeContainerView = findViewById(R.id.transfer_call_activity_qrcode_container_view);
        radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable qrCodeViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        qrCodeViewBackground.getPaint().setColor(Color.WHITE);
        ViewCompat.setBackground(qrCodeContainerView, qrCodeViewBackground);

        qrCodeContainerView.setOnClickListener(v -> onTwincodeClick());

        mQRCodeView = findViewById(R.id.transfer_call_activity_qrcode_view);

        mTwincodeView = findViewById(R.id.transfer_call_activity_twincode_view);
        Design.updateTextFont(mTwincodeView, Design.FONT_MEDIUM28);
        mTwincodeView.setTextColor(Color.WHITE);

        mTwincodeView.setOnClickListener(v -> onTwincodeClick());

        View resetView = findViewById(R.id.transfer_call_activity_reset_clickable_view);
        resetView.setOnClickListener(v -> onGenerateCodeClick());

        RoundedView resetRoundedView = findViewById(R.id.transfer_call_activity_reset_rounded_view);
        resetRoundedView.setBorder(1, DESIGN_ACTION_BORDER_COLOR);
        resetRoundedView.setColor(Color.BLACK);

        ImageView resetIconView = findViewById(R.id.transfer_call_activity_reset_icon_view);
        resetIconView.setColorFilter(Color.WHITE);

        TextView resetTextView = findViewById(R.id.transfer_call_activity_reset_text_view);
        Design.updateTextFont(resetTextView, Design.FONT_MEDIUM28);
        resetTextView.setTextColor(Color.WHITE);

        View editView = findViewById(R.id.transfer_call_activity_edit_clickable_view);
        editView.setOnClickListener(v -> onEditTransferCallClick());

        RoundedView editRoundedView = findViewById(R.id.transfer_call_activity_edit_rounded_view);
        editRoundedView.setBorder(1, DESIGN_ACTION_BORDER_COLOR);
        editRoundedView.setColor(Color.BLACK);

        ImageView editIconView = findViewById(R.id.transfer_call_activity_edit_icon_view);
        editIconView.setColorFilter(Color.WHITE);

        TextView editTextView = findViewById(R.id.transfer_call_activity_edit_text_view);
        Design.updateTextFont(editTextView, Design.FONT_MEDIUM28);
        editTextView.setTextColor(Color.WHITE);

        View saveView = findViewById(R.id.transfer_call_activity_save_clickable_view);
        saveView.setOnClickListener(v -> onSaveInGalleryClick());

        RoundedView saveRoundedView = findViewById(R.id.transfer_call_activity_save_rounded_view);
        saveRoundedView.setBorder(1, DESIGN_ACTION_BORDER_COLOR);
        saveRoundedView.setColor(Color.BLACK);

        ImageView saveIconView = findViewById(R.id.transfer_call_activity_save_icon_view);
        saveIconView.setColorFilter(Color.WHITE);

        TextView saveTextView = findViewById(R.id.transfer_call_activity_save_text_view);
        Design.updateTextFont(saveTextView, Design.FONT_MEDIUM28);
        saveTextView.setTextColor(Color.WHITE);

        View shareView = findViewById(R.id.transfer_call_activity_social_view);
        shareView.setOnClickListener(v -> onSocialClick());

        layoutParams = shareView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) shareView.getLayoutParams();
        marginLayoutParams.bottomMargin = - (int) (Design.BUTTON_HEIGHT * 0.5);

        radius = (int) (Design.BUTTON_HEIGHT * 0.5) * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable shareViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        shareViewBackground.getPaint().setColor(Design.getMainStyle());
        ViewCompat.setBackground(shareView, shareViewBackground);

        ImageView socialIconView = findViewById(R.id.transfer_call_activity_social_icon_view);
        socialIconView.setColorFilter(Color.WHITE);

        layoutParams = socialIconView.getLayoutParams();
        int iconSize = (int) (DESIGN_SHARE_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.width = iconSize;
        layoutParams.height = iconSize;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) socialIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        TextView socialViewTitleView = findViewById(R.id.transfer_call_activity_social_title_view);
        Design.updateTextFont(socialViewTitleView, Design.FONT_MEDIUM32);
        socialViewTitleView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) socialViewTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        TextView shareSubTitleView = findViewById(R.id.transfer_call_activity_social_subtitle_view);
        Design.updateTextFont(shareSubTitleView, Design.FONT_REGULAR24);
        shareSubTitleView.setTextColor(Design.GREY_COLOR);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) shareSubTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (Design.BUTTON_HEIGHT * 0.5);

        View removeView = findViewById(R.id.transfer_call_activity_remove_view);
        mRemoveListener = new RemoveListener();
        mRemoveListener.enable();
        removeView.setOnClickListener(mRemoveListener);

        layoutParams = removeView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView removeTextView = findViewById(R.id.transfer_call_activity_remove_label_view);
        Design.updateTextFont(removeTextView, Design.FONT_REGULAR34);
        removeTextView.setTextColor(Design.FONT_COLOR_RED);

        mSaveClickToCallView = findViewById(R.id.transfer_call_activity_save_click_to_call_view);
    }

    private void onEditTransferCallClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditTransferCallClick");
        }

        startActivity(EditIdentityActivity.class, Intents.INTENT_CALL_RECEIVER_ID, mCallReceiver.getId());
    }

    private void onRemoveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveClick");
        }

        if (mCallReceiverService == null || mCallReceiver == null) {
            return;
        }

        ViewGroup viewGroup = findViewById(R.id.invitation_external_call_activity_layout);

        DeleteConfirmView deleteConfirmView = new DeleteConfirmView(this, null);
        deleteConfirmView.setAvatar(mAvatar, false);

        String message = getString(R.string.transfert_call_activity_delete_message) + "\n\n"  + getString(R.string.transfert_call_activity_delete_confirm_message);
        deleteConfirmView.setMessage(message);

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                mCallReceiverService.deleteCallReceiver(mCallReceiver);
                deleteConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                mRemoveListener.enable();
                deleteConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                mRemoveListener.enable();
                deleteConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(deleteConfirmView);
            }
        };
        deleteConfirmView.setObserver(observer);
        viewGroup.addView(deleteConfirmView);
        deleteConfirmView.show();
    }
}
