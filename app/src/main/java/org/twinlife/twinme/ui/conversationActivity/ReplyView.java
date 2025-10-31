/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.widget.TextViewCompat;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.baseItemActivity.FileItem;
import org.twinlife.twinme.ui.baseItemActivity.ImageItem;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.ui.baseItemActivity.LinkItem;
import org.twinlife.twinme.ui.baseItemActivity.MessageItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerFileItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerImageItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerLinkItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerMessageItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerVideoItem;
import org.twinlife.twinme.ui.baseItemActivity.VideoItem;
import org.twinlife.twinme.utils.Utils;

import java.io.File;

@SuppressWarnings("deprecation")
public class ReplyView extends PercentRelativeLayout {
    private static final String LOG_TAG = "ReplyView";
    private static final boolean DEBUG = false;

    private static final int ITEM_COLOR = Color.argb(255, 110, 110, 110);

    private static final float DESIGN_TITLE_TOP_MARGIN = 12f;
    private static final float DESIGN_MESSAGE_TOP_MARGIN = 6f;
    private static final float DESIGN_IMAGE_ITEM_MAX_WIDTH = 90f;
    private static final float DESIGN_IMAGE_ITEM_MAX_HEIGHT = 90f;
    static final int TITLE_TOP_MARGIN;
    static final int MESSAGE_TOP_MARGIN;
    static final int IMAGE_ITEM_MAX_HEIGHT;
    static final int IMAGE_ITEM_MAX_WIDTH;

    static {
        TITLE_TOP_MARGIN = (int) (DESIGN_TITLE_TOP_MARGIN * Design.HEIGHT_RATIO);
        MESSAGE_TOP_MARGIN = (int) (DESIGN_MESSAGE_TOP_MARGIN * Design.HEIGHT_RATIO);
        IMAGE_ITEM_MAX_HEIGHT = (int) (DESIGN_IMAGE_ITEM_MAX_HEIGHT * Design.HEIGHT_RATIO);
        IMAGE_ITEM_MAX_WIDTH = (int) (DESIGN_IMAGE_ITEM_MAX_WIDTH * Design.WIDTH_RATIO);
    }

    private ConversationActivity mConversationActivity;

    private TextView mTitleView;
    private TextView mMessageView;
    private ImageView mImageView;
    private View mOverlayView;

    public ReplyView(Context context) {
        super(context);
    }

    public ReplyView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        mConversationActivity = (ConversationActivity) context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view;
            view = inflater.inflate(R.layout.conversation_activity_reply_view, (ViewGroup) getParent());
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            addView(view);

            initViews();
        }
    }

    public ReplyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void hideOverlay(boolean hide) {

        if (hide) {
            mOverlayView.setVisibility(INVISIBLE);
        } else {
            mOverlayView.setVisibility(VISIBLE);
        }
    }

    @SuppressLint({"StringFormatInvalid", "StringFormatMatches"})
    public void showReply(Item item, String contactName) {

        mTitleView.setText(String.format(mConversationActivity.getString(R.string.conversation_activity_reply_to), contactName));

        String path;
        Bitmap thumbnail;
        File file;
        BitmapDrawable bitmapDrawable;

        switch (item.getType()) {
            case MESSAGE:
                MessageItem messageItem = (MessageItem) item;
                mMessageView.setText(messageItem.getContent());
                mMessageView.setVisibility(VISIBLE);
                mImageView.setVisibility(GONE);
                break;

            case PEER_MESSAGE:
                PeerMessageItem peerMessageItem = (PeerMessageItem) item;
                mMessageView.setText(peerMessageItem.getContent());
                mMessageView.setVisibility(VISIBLE);
                mImageView.setVisibility(GONE);
                break;

            case LINK:
                LinkItem linkItem = (LinkItem) item;
                mMessageView.setText(linkItem.getContent());
                mMessageView.setVisibility(VISIBLE);
                mImageView.setVisibility(GONE);
                break;

            case PEER_LINK:
                PeerLinkItem peerLinkItem = (PeerLinkItem) item;
                mMessageView.setText(peerLinkItem.getContent());
                mMessageView.setVisibility(VISIBLE);
                mImageView.setVisibility(GONE);
                break;

            case IMAGE:
                ImageItem imageItem = (ImageItem) item;
                path = imageItem.getPath();
                thumbnail = mConversationActivity.getThumbnail(imageItem.getImageDescriptor());
                if (thumbnail != null) {
                    mImageView.setImageBitmap(thumbnail);
                } else {
                    file = new File(mConversationActivity.getTwinmeContext().getFilesDir(), path);
                    bitmapDrawable = Utils.getBitmapDrawable(mConversationActivity, file.getPath(), IMAGE_ITEM_MAX_WIDTH, IMAGE_ITEM_MAX_HEIGHT);
                    if (bitmapDrawable != null) {
                        mImageView.setImageBitmap(bitmapDrawable.getBitmap());
                    }
                }
                mMessageView.setVisibility(GONE);
                mImageView.setVisibility(VISIBLE);
                break;

            case PEER_IMAGE:
                PeerImageItem peerImageItem = (PeerImageItem) item;
                path = peerImageItem.getPath();
                thumbnail = mConversationActivity.getThumbnail(peerImageItem.getImageDescriptor());
                if (thumbnail != null) {
                    mImageView.setImageBitmap(thumbnail);
                } else {
                    file = new File(mConversationActivity.getTwinmeContext().getFilesDir(), path);
                    bitmapDrawable = Utils.getBitmapDrawable(mConversationActivity, file.getPath(), IMAGE_ITEM_MAX_WIDTH, IMAGE_ITEM_MAX_HEIGHT);
                    if (bitmapDrawable != null) {
                        mImageView.setImageBitmap(bitmapDrawable.getBitmap());
                    }
                }
                mMessageView.setVisibility(GONE);
                mImageView.setVisibility(VISIBLE);
                break;

            case VIDEO:
                VideoItem videoItem = (VideoItem) item;
                path = videoItem.getPath();
                thumbnail = mConversationActivity.getThumbnail(videoItem.getVideoDescriptor());
                if (thumbnail == null) {
                    thumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
                }

                if (thumbnail != null) {
                    bitmapDrawable = new BitmapDrawable(mConversationActivity.getResources(), thumbnail);
                    mImageView.setImageBitmap(bitmapDrawable.getBitmap());
                }

                mMessageView.setVisibility(GONE);
                mImageView.setVisibility(VISIBLE);
                break;

            case PEER_VIDEO:
                PeerVideoItem peerVideoItem = (PeerVideoItem) item;
                path = peerVideoItem.getPath();
                thumbnail = mConversationActivity.getThumbnail(peerVideoItem.getVideoDescriptor());
                if (thumbnail == null) {
                    thumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
                }

                if (thumbnail != null) {
                    bitmapDrawable = new BitmapDrawable(mConversationActivity.getResources(), thumbnail);
                    mImageView.setImageBitmap(bitmapDrawable.getBitmap());
                }

                mMessageView.setVisibility(GONE);
                mImageView.setVisibility(VISIBLE);
                break;

            case AUDIO:
            case PEER_AUDIO: {
                mMessageView.setText(mConversationActivity.getResources().getString(R.string.conversation_activity_audio_message));
                mMessageView.setVisibility(VISIBLE);
                mImageView.setVisibility(GONE);
                break;
            }

            case LOCATION:
            case PEER_LOCATION: {
                mMessageView.setText(mConversationActivity.getResources().getString(R.string.application_location));
                mMessageView.setVisibility(VISIBLE);
                mImageView.setVisibility(GONE);
                break;
            }

            case FILE:
                FileItem fileItem = (FileItem) item;
                mMessageView.setText(fileItem.getNamedFileDescriptor().getName());
                mMessageView.setVisibility(VISIBLE);
                mImageView.setVisibility(GONE);
                break;

            case PEER_FILE:
                PeerFileItem peerFileItem = (PeerFileItem) item;
                mMessageView.setText(peerFileItem.getNamedFileDescriptor().getName());
                mMessageView.setVisibility(VISIBLE);
                mImageView.setVisibility(GONE);
                break;
        }
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mTitleView = findViewById(R.id.conversation_activity_reply_view_title);
        Design.updateTextFont(mTitleView, Design.FONT_REGULAR24);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        int size24 = (int) (Design.MIN_RATIO * 24);
        int size26 = (int) (Design.MIN_RATIO * 26);

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mTitleView, size24, size26, 1,
                TypedValue.COMPLEX_UNIT_PX);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.topMargin = TITLE_TOP_MARGIN;

        mMessageView = findViewById(R.id.conversation_activity_reply_view_content);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR24);
        mMessageView.setTextColor(ITEM_COLOR);

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mMessageView, size24, size26, 1,
                TypedValue.COMPLEX_UNIT_PX);

        marginLayoutParams = (MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = MESSAGE_TOP_MARGIN;

        mImageView = findViewById(R.id.conversation_activity_reply_view_image);

        marginLayoutParams = (MarginLayoutParams) mImageView.getLayoutParams();
        marginLayoutParams.topMargin = MESSAGE_TOP_MARGIN;

        View closeView = findViewById(R.id.conversation_activity_reply_view_close);
        closeView.setOnClickListener(v -> mConversationActivity.closeReplyView());

        mOverlayView = findViewById(R.id.conversation_activity_reply_view_overlay_view);
        mOverlayView.setBackgroundColor(Design.BACKGROUND_COLOR_WHITE_OPACITY85);
        mOverlayView.setOnClickListener(view -> mConversationActivity.closeMenu());
    }
}
