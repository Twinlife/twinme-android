/*
 *  Copyright (c) 2022-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.ImageDescriptor;
import org.twinlife.twinlife.ConversationService.NamedFileDescriptor;
import org.twinlife.twinlife.ConversationService.ObjectDescriptor;
import org.twinlife.twinlife.ConversationService.VideoDescriptor;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AcceptInvitationActivity;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.RoundedImageView;
import org.twinlife.twinme.utils.Utils;
import org.twinlife.twinme.utils.async.LinkLoader;

import java.util.ArrayList;
import java.util.List;

class LinkItemViewHolder extends ItemViewHolder {

    private final View mPreviewLinkView;
    private final TextView mTitleLinkView;
    private final RoundedImageView mImageLinkView;
    private final View mReplyView;
    private final TextView mReplyTextView;
    private final View mReplyContainerImageView;
    private final View mReplyToImageContentView;
    private final RoundedImageView mReplyImageView;
    private final TextView mTextView;
    private final GradientDrawable mPreviewGradientDrawable;
    private final GradientDrawable mGradientDrawable;
    private final GradientDrawable mReplyGradientDrawable;
    private final GradientDrawable mReplyToImageContentGradientDrawable;
    private final DeleteProgressView mDeleteView;

    private boolean mIsLongPressDetected = false;

    @Nullable
    private LinkLoader<Item> mLinkLoader;

    public abstract static class TextViewLinkMovementMethod extends LinkMovementMethod {

        public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent motionEvent) {

            if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
                return super.onTouchEvent(textView, spannable, motionEvent);
            }

            int x = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();

            x -= textView.getTotalPaddingLeft();
            y -= textView.getTotalPaddingTop();

            x += textView.getScrollX();
            y += textView.getScrollY();

            Layout layout = textView.getLayout();
            int line = layout.getLineForVertical(y);
            int offset = layout.getOffsetForHorizontal(line, x);

            URLSpan[] link = spannable.getSpans(offset, offset, URLSpan.class);
            if (link.length != 0) {
                onLinkClick(link[0].getURL());
            }
            return true;
        }

        abstract public void onLinkClick(String url);
    }

    LinkItemViewHolder(BaseItemActivity baseItemActivity, View view) {

        super(baseItemActivity, view,
                R.id.base_item_activity_link_item_layout_container,
                R.id.base_item_activity_link_item_state_view,
                R.id.base_item_activity_link_item_state_avatar_view,
                R.id.base_item_activity_link_item_overlay_view,
                R.id.base_item_activity_link_item_annotation_view,
                R.id.base_item_activity_link_item_selected_view,
                R.id.base_item_activity_link_item_selected_image_view);

        mPreviewLinkView = view.findViewById(R.id.base_item_activity_link_item_preview_view);
        mPreviewLinkView.setVisibility(View.GONE);

        mPreviewLinkView.setOnClickListener(view1 -> {
            if (getBaseItemActivity().isSelectItemMode()) {
                onContainerClick();
            }

            baseItemActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getLinkItem().getUrl().toString())));
        });


        mPreviewLinkView.setOnLongClickListener(v -> {
            baseItemActivity.onItemLongPress(getItem());
            return true;
        });

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mPreviewLinkView.getLayoutParams();
        marginLayoutParams.bottomMargin = LINK_PREVIEW_BOTTOM_MARGIN;

        mPreviewGradientDrawable = new GradientDrawable();
        mPreviewGradientDrawable.mutate();
        mPreviewGradientDrawable.setColor(Design.GREY_ITEM_COLOR);
        mPreviewGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mPreviewLinkView.setBackground(mPreviewGradientDrawable);

        mTitleLinkView = view.findViewById(R.id.base_item_activity_link_item_link_title_textview);
        mTitleLinkView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);
        Design.updateTextFont(mTitleLinkView, Design.FONT_MEDIUM32);
        mTitleLinkView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mTitleLinkView.setMaxLines(2);

        mImageLinkView = view.findViewById(R.id.base_item_activity_link_item_link_image);

        mTextView = view.findViewById(R.id.base_item_activity_link_item_text);
        mTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);
        mTextView.setTypeface(getMessageFont().typeface);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getMessageFont().size);

        mTextView.setOnClickListener(v -> {
            if (getBaseItemActivity().isSelectItemMode()) {
                onContainerClick();
            }
        });

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.getMainStyle());
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mTextView.setBackground(mGradientDrawable);

        mReplyTextView = view.findViewById(R.id.base_item_activity_link_item_reply_text);
        mReplyTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);
        mReplyTextView.setTypeface(getMessageFont().typeface);
        mReplyTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getMessageFont().size);
        mReplyTextView.setTextColor(Design.REPLY_FONT_COLOR);
        mReplyTextView.setMaxLines(3);
        mReplyTextView.setEllipsize(TextUtils.TruncateAt.END);

        mReplyView = view.findViewById(R.id.base_item_activity_link_item_reply_view);

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

        mReplyImageView = view.findViewById(R.id.base_item_activity_link_item_reply_image_view);
        ViewGroup.LayoutParams layoutParams = mReplyImageView.getLayoutParams();
        layoutParams.width = REPLY_IMAGE_ITEM_MAX_WIDTH;
        layoutParams.height = REPLY_IMAGE_ITEM_MAX_HEIGHT;

        mReplyContainerImageView = view.findViewById(R.id.base_item_activity_link_item_reply_container_image_view);
        mReplyContainerImageView.setPadding(REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN, REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN);

        mReplyToImageContentView = view.findViewById(R.id.base_item_activity_link_item_reply_image_content_view);

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

        mDeleteView = view.findViewById(R.id.base_item_activity_link_item_delete_view);

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
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof LinkItem)) {
            return;
        }
        super.onBind(item);

        final LinkItem linkItem = (LinkItem) item;
        final ObjectDescriptor objectDescriptor = linkItem.getObjectDescriptor();

        // Use an async loader to get url metatda.
        if (mLinkLoader == null) {
            mLinkLoader = new LinkLoader<>(item, objectDescriptor);
            addLoader(mLinkLoader);
        }

        String linkTitle = mLinkLoader.getTitle();
        if (linkTitle != null && !linkTitle.isEmpty()) {
            mTitleLinkView.setText(linkTitle);
        }

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        mPreviewGradientDrawable.setCornerRadii(outerRadii);

        if (mLinkLoader.getImage() != null) {
            mImageLinkView.setVisibility(View.VISIBLE);
            Bitmap bitmap = mLinkLoader.getImage();
            int imageViewWidth;
            int imageViewHeight;

            if (bitmap.getWidth() >= bitmap.getHeight()) {
                imageViewWidth = LINK_IMAGE_MAX_WIDTH;
                imageViewHeight = (imageViewWidth * bitmap.getHeight()) / bitmap.getWidth();
            } else {
                imageViewHeight = LINK_IMAGE_MAX_HEIGHT;
                imageViewWidth = (imageViewHeight * bitmap.getWidth()) / bitmap.getHeight();
            }

            ViewGroup.LayoutParams layoutParams = mPreviewLinkView.getLayoutParams();
            layoutParams.width = imageViewWidth;
            mPreviewLinkView.setLayoutParams(layoutParams);

            layoutParams = mImageLinkView.getLayoutParams();
            layoutParams.width = imageViewWidth;
            layoutParams.height = imageViewHeight;
            mImageLinkView.setLayoutParams(layoutParams);

            float[] imageOuterRadii = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};
            mImageLinkView.setImageBitmap(mLinkLoader.getImage(), imageOuterRadii);

        } else {
            mImageLinkView.setVisibility(View.GONE);

            ViewGroup.LayoutParams layoutParams = mPreviewLinkView.getLayoutParams();
            layoutParams.width = LINK_IMAGE_MAX_WIDTH;
            mPreviewLinkView.setLayoutParams(layoutParams);
        }

        if ((linkTitle != null && !linkTitle.isEmpty()) || mLinkLoader.getImage() != null) {
            mPreviewLinkView.setVisibility(View.VISIBLE);
        } else {
            mPreviewLinkView.setVisibility(View.GONE);
        }

        mTextView.setTypeface(getMessageFont().typeface);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getMessageFont().size);

        mTextView.setText(Utils.formatText(linkItem.getContent(), 0));

        try {
            Linkify.addLinks(mTextView, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS);
            Linkify.addLinks(mTextView, CommonUtils.IPV6_PATTERN, null, null, null);
        } catch (Exception ex) {
            // Possible exception: android.webkit.WebViewFactory.MissingWebViewPackageException when there is no WebView implementation.
        }
        mTextView.setLinkTextColor(Color.WHITE);

        // Compute the corner radii only once!
        final float[] cornerRadii = getCornerRadii();
        mGradientDrawable.setCornerRadii(cornerRadii);
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
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_link_item_reply_text);

                    ObjectDescriptor replyObjectDescriptor = (ObjectDescriptor) replyToDescriptor;
                    mReplyTextView.setText(replyObjectDescriptor.getMessage());
                    break;

                case IMAGE_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_link_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (ImageDescriptor) replyToDescriptor);
                    break;

                case VIDEO_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_link_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (VideoDescriptor) replyToDescriptor);
                    break;

                case AUDIO_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_link_item_reply_text);

                    mReplyTextView.setText(getString(R.string.conversation_activity_audio_message));
                    break;

                case NAMED_FILE_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_link_item_reply_text);

                    NamedFileDescriptor fileDescriptor = (NamedFileDescriptor) replyToDescriptor;
                    mReplyTextView.setText(fileDescriptor.getName());
                    break;
            }

            if (mReplyImageView.getVisibility() == View.VISIBLE) {
                relativeLayoutParams = (RelativeLayout.LayoutParams) mReplyContainerImageView.getLayoutParams();
            } else {
                relativeLayoutParams = (RelativeLayout.LayoutParams) mReplyTextView.getLayoutParams();
            }
            relativeLayoutParams.removeRule(RelativeLayout.BELOW);
            relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_link_item_preview_view);
        }
    }

    @Override
    void onViewRecycled() {

        super.onViewRecycled();

        if (mLinkLoader != null) {
            mLinkLoader.cancel();
            mLinkLoader = null;
        }

        mTextView.setText(null);
        mReplyImageView.setImageBitmap(null, null);
        mDeleteView.setVisibility(View.GONE);
        mDeleteView.setOnDeleteProgressListener(null);
        setDeleteAnimationStarted(false);
    }

    @Override
    void startDeletedAnimation() {

        if (isDeleteAnimationStarted()) {
            return;
        }

        setDeleteAnimationStarted(true);
        mDeleteView.setVisibility(View.VISIBLE);

        ViewGroup.MarginLayoutParams deleteLayoutParams = (ViewGroup.MarginLayoutParams) mDeleteView.getLayoutParams();
        deleteLayoutParams.width = mTextView.getWidth();
        deleteLayoutParams.height = mTextView.getHeight();
        mDeleteView.setCornerRadii(getCornerRadii());
        mDeleteView.setOnDeleteProgressListener(() -> deleteItem(getItem()));

        float progress = 0;
        int animationDuration = DESIGN_DELETE_ANIMATION_DURATION;
        if (getItem().getDeleteProgress() > 0) {
            progress = getItem().getDeleteProgress() / 100.0f;
            animationDuration = (int) (BaseItemViewHolder.DESIGN_DELETE_ANIMATION_DURATION - ((getItem().getDeleteProgress() * BaseItemViewHolder.DESIGN_DELETE_ANIMATION_DURATION) / 100.0));
        }

        mDeleteView.startAnimation(animationDuration, progress);
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

    private LinkItem getLinkItem() {

        return (LinkItem) getItem();
    }
}



