/*
 *  Copyright (c) 2016-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Thibaud David (contact@thibauddavid.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Yannis Le Gal (Yannis.LeGal@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.ImageDescriptor;
import org.twinlife.twinlife.ConversationService.NamedFileDescriptor;
import org.twinlife.twinlife.ConversationService.ObjectDescriptor;
import org.twinlife.twinlife.ConversationService.VideoDescriptor;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AcceptInvitationActivity;
import org.twinlife.twinme.ui.baseItemActivity.MessageItemViewHolder.TextViewLinkMovementMethod;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.EphemeralView;
import org.twinlife.twinme.utils.RoundedImageView;
import org.twinlife.twinme.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class PeerMessageItemViewHolder extends PeerItemViewHolder {

    private static final int MAX_EMOJI = 5;

    private static final float DESIGN_EPHEMERAL_HEIGHT = 28f;
    private static final float DESIGN_EPHEMERAL_LEFT_MARGIN = 12f;
    private static final float DESIGN_EPHEMERAL_TOP_MARGIN = 4f;
    private static final float DESIGN_EPHEMERAL_BOTTOM_MARGIN = 14f;

    private final View mReplyView;
    private final TextView mReplyTextView;
    private final View mReplyToImageContentView;
    private final RoundedImageView mReplyImageView;
    private final GradientDrawable mReplyGradientDrawable;
    private final GradientDrawable mReplyToImageContentGradientDrawable;
    private final TextView mTextView;
    private final GradientDrawable mGradientDrawable;
    private final EphemeralView mEphemeralView;

    private CountDownTimer mTimer;

    private boolean mIsLongPressDetected = false;

    PeerMessageItemViewHolder(BaseItemActivity baseItemActivity, View view) {

        super(baseItemActivity, view,
                R.id.base_item_activity_peer_message_item_layout_container,
                R.id.base_item_activity_peer_message_item_avatar,
                R.id.base_item_activity_peer_message_item_overlay_view,
                R.id.base_item_activity_peer_message_item_annotation_view,
                R.id.base_item_activity_peer_message_item_selected_view,
                R.id.base_item_activity_peer_message_item_selected_image_view);

        mTextView = view.findViewById(R.id.base_item_activity_peer_message_item_text);
        mTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);
        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.GREY_ITEM_COLOR);
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mTextView.setBackground(mGradientDrawable);

        mTextView.setTypeface(getMessageFont().typeface);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getMessageFont().size);
        mTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mTextView.setOnClickListener(v -> {
            if (getBaseItemActivity().isSelectItemMode()) {
                onContainerClick();
            }
        });

        mTextView.setOnLongClickListener(v -> {
            baseItemActivity.onItemLongPress(getItem());
            mIsLongPressDetected = true;
            return true;
        });

        mTextView.setMovementMethod(new TextViewLinkMovementMethod() {

            @Override
            public void onLinkClick(String url) {

                if (getBaseItemActivity().isSelectItemMode()) {
                    onContainerClick();
                    return;
                }

                Uri uri = Uri.parse(url);
                String action = uri.getAuthority();
                if (TwincodeURI.INVITE_ACTION.equals(action)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(uri);
                    intent.setClass(baseItemActivity, AcceptInvitationActivity.class);
                    baseItemActivity.startActivity(intent);
                    baseItemActivity.overridePendingTransition(0, 0);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(uri);
                    baseItemActivity.startActivity(intent);
                }
            }

            @Override
            public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_UP && mIsLongPressDetected) {
                    mIsLongPressDetected = false;
                    return false;
                }

                return super.onTouchEvent(textView, spannable, motionEvent);
            }
        });

        mReplyTextView = view.findViewById(R.id.base_item_activity_peer_message_item_reply_text);
        mReplyTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);
        mReplyTextView.setTypeface(getMessageFont().typeface);
        mReplyTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getMessageFont().size);
        mReplyTextView.setTextColor(Design.REPLY_FONT_COLOR);
        mReplyTextView.setMaxLines(3);
        mReplyTextView.setEllipsize(TextUtils.TruncateAt.END);

        mReplyView = view.findViewById(R.id.base_item_activity_peer_message_item_reply_view);

        mReplyView.setOnClickListener(v -> onReplyClick());

        mReplyView.setOnLongClickListener(v -> {
            baseItemActivity.onItemLongPress(getItem());
            mIsLongPressDetected = true;
            return true;
        });

        mReplyGradientDrawable = new GradientDrawable();
        mReplyGradientDrawable.mutate();
        mReplyGradientDrawable.setColor(Design.REPLY_BACKGROUND_COLOR);
        mReplyGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mReplyView.setBackground(mReplyGradientDrawable);

        mReplyImageView = view.findViewById(R.id.base_item_activity_peer_message_item_reply_image_view);
        ViewGroup.LayoutParams layoutParams = mReplyImageView.getLayoutParams();
        layoutParams.width = REPLY_IMAGE_ITEM_MAX_WIDTH;
        layoutParams.height = REPLY_IMAGE_ITEM_MAX_HEIGHT;

        View replyContainerImageView = view.findViewById(R.id.base_item_activity_peer_message_item_reply_container_image_view);
        replyContainerImageView.setPadding(REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN, REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN);

        mReplyToImageContentView = view.findViewById(R.id.base_item_activity_peer_message_item_reply_image_content_view);

        mReplyToImageContentView.setOnClickListener(v -> onReplyClick());

        mReplyToImageContentView.setOnLongClickListener(v -> {
            baseItemActivity.onItemLongPress(getItem());
            mIsLongPressDetected = true;
            return true;
        });

        mReplyToImageContentGradientDrawable = new GradientDrawable();
        mReplyToImageContentGradientDrawable.mutate();
        mReplyToImageContentGradientDrawable.setColor(Design.REPLY_BACKGROUND_COLOR);
        mReplyToImageContentGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mReplyToImageContentView.setBackground(mReplyToImageContentGradientDrawable);

        mEphemeralView = view.findViewById(R.id.base_item_activity_peer_message_item_ephemeral_view);
        mEphemeralView.setColor(Design.BLACK_COLOR);

        layoutParams = mEphemeralView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_EPHEMERAL_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mEphemeralView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_EPHEMERAL_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_EPHEMERAL_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_EPHEMERAL_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_EPHEMERAL_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_EPHEMERAL_LEFT_MARGIN * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_EPHEMERAL_LEFT_MARGIN * Design.WIDTH_RATIO));
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof PeerMessageItem)) {
            return;
        }
        super.onBind(item);

        // Compute the corner radii only once!
        final float[] cornerRadii = getCornerRadii();

        mGradientDrawable.setCornerRadii(cornerRadii);

        final PeerMessageItem peerMessageItem = (PeerMessageItem) item;
        int countEmoji = countEmoji(peerMessageItem.getContent());
        if (countEmoji == 0) {
            mTextView.setTypeface(getMessageFont().typeface);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getMessageFont().size);
            mGradientDrawable.setColor(getBaseItemActivity().getCustomAppearance().getPeerMessageBackgroundColor());
            if (getBaseItemActivity().getCustomAppearance().getPeerMessageBorderColor() != Color.TRANSPARENT) {
                mGradientDrawable.setStroke(Design.BORDER_WIDTH, getBaseItemActivity().getCustomAppearance().getPeerMessageBorderColor());
            }
            mTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);
        } else {
            mTextView.setTypeface(Design.getEmojiFont(countEmoji).typeface);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.getEmojiFont(countEmoji).size);

            if (peerMessageItem.getReplyToDescriptor() == null) {
                mGradientDrawable.setColor(Color.TRANSPARENT);
                mGradientDrawable.setStroke(Design.BORDER_WIDTH, Color.TRANSPARENT);
                mTextView.setPadding(0, 0, 0, 0);
            } else {
                mTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);
                mGradientDrawable.setColor(getBaseItemActivity().getCustomAppearance().getPeerMessageBackgroundColor());
                if (getBaseItemActivity().getCustomAppearance().getPeerMessageBorderColor() != Color.TRANSPARENT) {
                    mGradientDrawable.setStroke(Design.BORDER_WIDTH, getBaseItemActivity().getCustomAppearance().getPeerMessageBorderColor());
                }
            }
        }

        mTextView.setText(Utils.formatText(peerMessageItem.getContent(), 0));

        final Item.ItemMode itemMode = peerMessageItem.getMode();
        ViewTreeObserver viewTreeObserver = mTextView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = mTextView.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalLayoutListener(this);
                if (itemMode == Item.ItemMode.PREVIEW && mTextView.getLineCount() > 5)  {
                    int lineEndIndex = mTextView.getLayout().getLineEnd(4);
                    if (lineEndIndex > 3) {
                        String text = mTextView.getText().subSequence(0, lineEndIndex - 3) + "...";
                        mTextView.setText(Utils.formatText(text, 0));
                    }
                } else if (itemMode == Item.ItemMode.SMALL_PREVIEW && mTextView.getLineCount() > 2)  {
                    int lineEndIndex = mTextView.getLayout().getLineEnd(1);
                    if (lineEndIndex > 3) {
                        String text = mTextView.getText().subSequence(0, lineEndIndex - 3) + "...";
                        mTextView.setText(Utils.formatText(text, 0));
                    }
                }
            }
        });

        if (itemMode == Item.ItemMode.PREVIEW) {
            mTextView.setEllipsize(TextUtils.TruncateAt.END);
            mTextView.setMaxLines(5);
        } else if (itemMode == Item.ItemMode.SMALL_PREVIEW) {
            mTextView.setEllipsize(TextUtils.TruncateAt.END);
            mTextView.setMaxLines(2);
        } else {
            mTextView.setMaxLines(Integer.MAX_VALUE);
            mTextView.setEllipsize(null);
        }

        try {
            Linkify.addLinks(mTextView, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS);
            Linkify.addLinks(mTextView, CommonUtils.IPV6_PATTERN, null, null, null);
        } catch (Exception ex) {
            // Possible exception: android.webkit.WebViewFactory.MissingWebViewPackageException when there is no WebView implementation.
        }
        
        mTextView.setLinkTextColor(Color.BLACK);
        mTextView.setTextColor(getBaseItemActivity().getCustomAppearance().getPeerMessageTextColor());

        mReplyGradientDrawable.setCornerRadii(cornerRadii);
        mReplyToImageContentGradientDrawable.setCornerRadii(cornerRadii);

        mReplyView.setVisibility(View.GONE);
        mReplyTextView.setVisibility(View.GONE);
        mReplyToImageContentView.setVisibility(View.GONE);
        mReplyImageView.setVisibility(View.GONE);

        final Descriptor replyToDescriptor = item.getReplyToDescriptor();
        if (replyToDescriptor != null) {

            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) mTextView.getLayoutParams();

            switch (replyToDescriptor.getType()) {
                case OBJECT_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_message_item_reply_text);

                    ObjectDescriptor objectDescriptor = (ObjectDescriptor) replyToDescriptor;
                    mReplyTextView.setText(objectDescriptor.getMessage());
                    break;

                case IMAGE_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_message_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (ImageDescriptor) replyToDescriptor);
                    break;

                case VIDEO_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_message_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (VideoDescriptor) replyToDescriptor);
                    break;

                case AUDIO_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_message_item_reply_text);

                    mReplyTextView.setText(getString(R.string.conversation_activity_audio_message));
                    break;

                case NAMED_FILE_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_message_item_reply_text);

                    NamedFileDescriptor fileDescriptor = (NamedFileDescriptor) replyToDescriptor;
                    mReplyTextView.setText(fileDescriptor.getName());
                    break;
            }
        }

        if (item.isEphemeralItem()) {
            mEphemeralView.setVisibility(View.VISIBLE);
            startEphemeralAnimation();

            int leftPadding;
            int rightPadding;

            if (CommonUtils.isLayoutDirectionRTL()) {
                leftPadding = MESSAGE_ITEM_TEXT_DEFAULT_PADDING;
                rightPadding = (int) (MESSAGE_ITEM_TEXT_WIDTH_PADDING + (DESIGN_EPHEMERAL_HEIGHT * Design.HEIGHT_RATIO));
            } else {
                leftPadding = (int) (MESSAGE_ITEM_TEXT_WIDTH_PADDING + (DESIGN_EPHEMERAL_HEIGHT * Design.HEIGHT_RATIO));
                rightPadding = MESSAGE_ITEM_TEXT_DEFAULT_PADDING;
            }

            if (countEmoji == 0 || peerMessageItem.getReplyToDescriptor() != null) {
                mTextView.setPadding(leftPadding, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, rightPadding, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);
            } else {
                mTextView.setPadding(leftPadding, 0, 0, 0);
            }
        } else {
            mEphemeralView.setVisibility(View.GONE);
            if (countEmoji == 0 || peerMessageItem.getReplyToDescriptor() != null) {
                mTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);
            } else {
                mTextView.setPadding(0, 0, 0, 0);
            }
        }
    }

    @Override
    void onViewRecycled() {

        super.onViewRecycled();

        mTextView.setText(null);
        mReplyImageView.setImageBitmap(null, null);
    }


    @Override
    List<View> clickableViews() {

        return new ArrayList<View>() {
            {
                add(getContainer());
            }
        };
    }

    //
    // Private methods
    //

    private void startEphemeralAnimation() {

        if (mTimer == null && getItem().getState() == Item.ItemState.READ) {
            mTimer = new CountDownTimer(getItem().getExpireTimeout(), 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Date now = new Date();
                    float timeSinceRead = (now.getTime() - getItem().getReadTimestamp());
                    float percent = (float) (1.0 - (timeSinceRead / getItem().getExpireTimeout()));
                    if (percent < 0) {
                        percent = 0;
                    } else if (percent > 1) {
                        percent = 1;
                    }
                    mEphemeralView.updateWithProgress(percent);
                }

                @Override
                public void onFinish() {

                }
            };
            mTimer.start();
        } else {
            mEphemeralView.updateWithProgress(1);
        }
    }

    private int countEmoji(String content) {

        int countEmoji = 0;
        int length = content.codePointCount(0, content.length());
        if (length > MAX_EMOJI) {
            return 0;
        }

        int index = 0;
        while (index < content.length()) {
            int codePoint = content.codePointAt(index);
            int charCount = Character.charCount(codePoint);

            boolean isEmoji = true;

            for (int i = index; i < index + charCount; i++) {
                char character = content.charAt(i);
                int type = Character.getType(character);

                if (type != Character.SURROGATE && type != Character.OTHER_SYMBOL) {
                    isEmoji = false;
                    break;
                }
            }

            if (isEmoji) {
                countEmoji++;
            } else {
                return 0;
            }

            index += charCount;

            if (countEmoji == MAX_EMOJI) {
                break;
            }
        }

        return countEmoji;
    }
}
