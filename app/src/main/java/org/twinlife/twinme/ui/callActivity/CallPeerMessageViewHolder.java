/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.util.Linkify;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AcceptInvitationActivity;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.ui.baseItemActivity.PeerMessageItem;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.Utils;

public class CallPeerMessageViewHolder extends AbstractCallMessageViewHolder {

    private final CallActivity mCallActivity;

    CallPeerMessageViewHolder(CallActivity callActivity, View view) {

        super(callActivity, view);

        mCallActivity = callActivity;

        mContainerView = view.findViewById(R.id.call_activity_conversation_peer_message_item_layout_container);

        mTextView = view.findViewById(R.id.call_activity_conversation_peer_message_item_text);
        Design.updateTextFont(mTextView, Design.FONT_REGULAR34);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mTextView.setLinkTextColor(Design.FONT_COLOR_DEFAULT);
        mTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_HEIGHT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_HEIGHT_PADDING);
        mTextView.setMaxWidth(MESSAGE_MAX_WIDTH);

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.GREY_ITEM_COLOR);
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mTextView.setBackground(mGradientDrawable);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTextView.getLayoutParams();
        marginLayoutParams.leftMargin = MESSAGE_MARGIN;
        marginLayoutParams.setMarginStart(MESSAGE_MARGIN);

        mTextView.setOnLongClickListener(v -> {
            Utils.setClipboard(callActivity, mTextView.getText().toString());
            Toast.makeText(callActivity, R.string.conversation_activity_menu_item_view_copy_message, Toast.LENGTH_SHORT).show();

            return false;
        });

        mTextView.setMovementMethod(new TextViewLinkMovementMethod() {

            @Override
            public void onLinkClick(String url) {

                Uri uri = Uri.parse(url);
                String action = uri.getAuthority();
                if (TwincodeURI.INVITE_ACTION.equals(action)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(uri);
                    intent.setClass(callActivity, AcceptInvitationActivity.class);
                    callActivity.startActivity(intent);
                    callActivity.overridePendingTransition(0, 0);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(uri);
                    callActivity.startActivity(intent);
                }
            }

            @Override
            public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent motionEvent) {

                return super.onTouchEvent(textView, spannable, motionEvent);
            }
        });
    }

    public void onBind(Item item) {

        if (item.getReadTimestamp() == 0) {
            mCallActivity.markDescriptorRead(item.getDescriptorId());
        }

        mCorners = item.getCorners();

        // Compute the corner radii only once!
        final float[] cornerRadii = getCornerRadii();

        mGradientDrawable.setCornerRadii(cornerRadii);

        final PeerMessageItem peerMessageItem = (PeerMessageItem) item;
        int countEmoji = countEmoji(peerMessageItem.getContent());
        if (countEmoji == 0) {
            Design.updateTextFont(mTextView, Design.FONT_REGULAR34);
            mTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_HEIGHT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_HEIGHT_PADDING);
            mGradientDrawable.setColor(Design.GREY_ITEM_COLOR);
        } else {
            Design.updateTextFont(mTextView, Design.getEmojiFont(countEmoji));
            mTextView.setPadding(0, 0, 0, 0);
            mGradientDrawable.setColor(Color.TRANSPARENT);
        }

        mTextView.setText(Utils.formatText(peerMessageItem.getContent(), 0));

        try {
            Linkify.addLinks(mTextView, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS);
            Linkify.addLinks(mTextView, CommonUtils.IPV6_PATTERN, null, null, null);
        } catch (Exception ex) {
            // Possible exception: android.webkit.WebViewFactory.MissingWebViewPackageException when there is no WebView implementation.
        }

        updateMargin();
    }

    public void onViewRecycled() {

        mTextView.setText(null);
    }

    private void updateMargin() {

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mContainerView.getLayoutParams();
        int topMargin;
        int bottomMargin;

        if ((mCorners & Item.TOP_LARGE_MARGIN) == 0) {
            topMargin = ITEM_TOP_MARGIN1;
        } else {
            topMargin = ITEM_TOP_MARGIN2;
        }
        if (((mCorners & Item.BOTTOM_LARGE_MARGIN) == 0)) {
            bottomMargin = ITEM_BOTTOM_MARGIN1;
        } else {
            bottomMargin = ITEM_BOTTOM_MARGIN2;
        }
        if (topMargin != layoutParams.topMargin || bottomMargin != layoutParams.bottomMargin) {
            layoutParams.topMargin = topMargin;
            layoutParams.bottomMargin = bottomMargin;
            mContainerView.setLayoutParams(layoutParams);
        }
    }
}
