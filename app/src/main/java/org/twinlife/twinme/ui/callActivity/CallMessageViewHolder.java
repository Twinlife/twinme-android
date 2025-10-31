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
import android.util.TypedValue;
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
import org.twinlife.twinme.ui.baseItemActivity.MessageItem;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.Utils;

public class CallMessageViewHolder extends AbstractCallMessageViewHolder {

    CallMessageViewHolder(CallActivity callActivity, View view) {

        super(callActivity, view);

        mContainerView = view.findViewById(R.id.call_activity_conversation_message_item_layout_container);

        mTextView = view.findViewById(R.id.call_activity_conversation_message_item_text);
        mTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_HEIGHT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_HEIGHT_PADDING);
        Design.updateTextFont(mTextView, Design.FONT_REGULAR34);
        mTextView.setLinkTextColor(Color.WHITE);
        mTextView.setTextColor(Color.WHITE);
        mTextView.setMaxWidth(MESSAGE_MAX_WIDTH);

        mTextView.setOnLongClickListener(v -> {
            Utils.setClipboard(callActivity, mTextView.getText().toString());
            Toast.makeText(callActivity, R.string.conversation_activity_menu_item_view_copy_message, Toast.LENGTH_SHORT).show();

            return false;
        });

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTextView.getLayoutParams();
        marginLayoutParams.rightMargin = MESSAGE_MARGIN;
        marginLayoutParams.setMarginEnd(MESSAGE_MARGIN);

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.getMainStyle());
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mTextView.setBackground(mGradientDrawable);

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

        mCorners = item.getCorners();

        final MessageItem messageItem = (MessageItem) item;
        mTextView.setText(Utils.formatText(messageItem.getContent(), 0));

        try {
            Linkify.addLinks(mTextView, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS);
            Linkify.addLinks(mTextView, CommonUtils.IPV6_PATTERN, null, null, null);
        } catch (Exception ex) {
            // Possible exception: android.webkit.WebViewFactory.MissingWebViewPackageException when there is no WebView implementation.
        }

        // Compute the corner radii only once!
        final float[] cornerRadii = getCornerRadii();

        mGradientDrawable.setCornerRadii(cornerRadii);

        int countEmoji = countEmoji(messageItem.getContent());
        if (countEmoji == 0) {
            Design.updateTextFont(mTextView, Design.FONT_REGULAR34);
            mTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_HEIGHT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_HEIGHT_PADDING);
            mGradientDrawable.setColor(Design.getMainStyle());
        } else {
            mTextView.setTypeface(Design.getEmojiFont(countEmoji).typeface);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.getEmojiFont(countEmoji).size);
            mGradientDrawable.setColor(Color.TRANSPARENT);
            mTextView.setPadding(0, 0, 0, 0);
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

        if ((mCorners & Item.TOP_RIGHT) == 0) {
            topMargin = ITEM_TOP_MARGIN1;
        } else {
            topMargin = ITEM_TOP_MARGIN2;
        }

        if (((mCorners & Item.BOTTOM_RIGHT) == 0)) {
            bottomMargin = ITEM_BOTTOM_MARGIN1;
        } else {
            bottomMargin = ITEM_BOTTOM_MARGIN2;
        }

        if (layoutParams.topMargin != topMargin || layoutParams.bottomMargin != bottomMargin) {
            layoutParams.topMargin = topMargin;
            layoutParams.bottomMargin = bottomMargin;
            mContainerView.setLayoutParams(layoutParams);
        }
    }
}