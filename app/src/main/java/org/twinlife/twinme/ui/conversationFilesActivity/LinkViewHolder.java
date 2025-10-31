/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.ui.baseItemActivity.LinkItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerLinkItem;
import org.twinlife.twinme.utils.async.LinkLoader;

public class LinkViewHolder extends RecyclerView.ViewHolder {

    private static final int DESIGN_PLACEHOLDER_COLOR = Color.rgb(229, 229, 229);

    private static final int DESIGN_IMAGE_VIEW_RADIUS = 6;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 252f;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final TextView mTitleView;
    private final ImageView mImageView;

    private final ImageView mPlaceholderView;

    private final View mSelectedView;
    private final ImageView mSelectedImageView;

    @Nullable
    private LinkLoader<Item> mLinkLoader;

    LinkViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.TRANSPARENT);

        mTitleView = view.findViewById(R.id.conversation_files_activity_link_item_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM34);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View containerView = view.findViewById(R.id.conversation_files_activity_link_item_container_view);

        float radius = DESIGN_IMAGE_VIEW_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable containerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        containerViewBackground.getPaint().setColor(DESIGN_PLACEHOLDER_COLOR);
        containerView.setBackground(containerViewBackground);

        mImageView = view.findViewById(R.id.conversation_files_activity_link_item_image_view);
        mImageView.setClipToOutline(true);

        mPlaceholderView = view.findViewById(R.id.conversation_files_activity_link_item_placeholder_view);

        mSelectedView = view.findViewById(R.id.conversation_files_activity_link_item_selected_view);

        mSelectedImageView = view.findViewById(R.id.conversation_files_activity_link_item_selected_image_view);
        mSelectedImageView.setColorFilter(Design.getMainStyle());
    }

    public void onBind(Item item, ConversationFilesActivity conversationFilesActivity) {

        ConversationService.ObjectDescriptor objectDescriptor;
        String url;
        if (item.isPeerItem()) {
            final PeerLinkItem peerLinkItem = (PeerLinkItem) item;
            objectDescriptor = peerLinkItem.getObjectDescriptor();
            url = peerLinkItem.getUrl().toString();
        } else {
            final LinkItem linkItem = (LinkItem) item;
            objectDescriptor = linkItem.getObjectDescriptor();
            url = linkItem.getUrl().toString();
        }

        String linkTitle = null;
        Bitmap linkBitmap = null;
        if (conversationFilesActivity.getTwinmeApplication().visualizationLink()) {
            if (mLinkLoader != null && mLinkLoader.getObjectDescriptor() != null && !mLinkLoader.getObjectDescriptor().getDescriptorId().equals(objectDescriptor.getDescriptorId())) {
                mLinkLoader.cancel();
                mLinkLoader = null;
            }

            // Use an async loader to get url metatda.
            if (mLinkLoader == null) {
                mLinkLoader = new LinkLoader<>(item, objectDescriptor);
                conversationFilesActivity.addLoadableItem(mLinkLoader);
            }

            linkTitle = mLinkLoader.getTitle();
            linkBitmap = mLinkLoader.getImage();
        }

        if (linkTitle != null) {
            mTitleView.setText(linkTitle);
        } else {
            mTitleView.setText(url);
        }

        if (linkBitmap != null) {
            mImageView.setVisibility(View.VISIBLE);
            mPlaceholderView.setVisibility(View.GONE);
            mImageView.setImageBitmap(linkBitmap);
        } else {
            mImageView.setVisibility(View.GONE);
            mPlaceholderView.setVisibility(View.VISIBLE);
        }

        if (conversationFilesActivity.isSelectMode()) {
            mSelectedView.setVisibility(View.VISIBLE);

            if (item.isSelected()) {
                mSelectedImageView.setVisibility(View.VISIBLE);
            } else {
                mSelectedImageView.setVisibility(View.INVISIBLE);
            }
        } else {
            mSelectedView.setVisibility(View.INVISIBLE);
        }
    }

    public void onViewRecycled() {

        mImageView.setImageBitmap(null);

        if (mLinkLoader != null) {
            mLinkLoader.cancel();
            mLinkLoader = null;
        }
    }
}
