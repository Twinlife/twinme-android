/*
 *  Copyright (c) 2018-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;

import java.util.List;

public class ItemListAdapter extends RecyclerView.Adapter<BaseItemViewHolder> {
    private static final String LOG_TAG = "ItemListAdapter";
    private static final boolean DEBUG = false;

    private final BaseItemActivity mBaseItemActivity;
    private final BaseItemActivity.AudioItemObserver mAudioItemObserver;
    private final List<Item> mItems;
    private final HeaderItem mHeaderItem = new HeaderItem();
    private final FooterItem mFooterItem = new FooterItem();
    private final TypingItem mTypingItem = new TypingItem();


    public ItemListAdapter(BaseItemActivity baseItemActivity, BaseItemActivity.AudioItemObserver audioItemObserver, List<Item> items) {

        mBaseItemActivity = baseItemActivity;
        mAudioItemObserver = audioItemObserver;
        mItems = items;
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        if (mBaseItemActivity.isPeerTyping()) {
            // conversation + header + typing indicator + footer
            return mItems.size() + 3;
        }

        // conversation + header + footer
        return mItems.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: position=" + position);
        }

        return getItem(position).getType().ordinal();
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemId: position=" + position);
        }

        return getItem(position).getItemId();
    }

    @Override
    public void onBindViewHolder(@NonNull BaseItemViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        Item item = getItem(position);
        if (item.isPeerItem()) {
            if (item.needsUpdateReadTimestamp() && item.getType() != Item.ItemType.PEER_CALL && item.getType() != Item.ItemType.PEER_FILE && item.getType() != Item.ItemType.PEER_AUDIO && item.getType() != Item.ItemType.PEER_VIDEO) {
                mBaseItemActivity.markDescriptorRead(item.getDescriptorId());
            }
        }

        viewHolder.onBind(item);
    }

    @Override
    @NonNull
    public BaseItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mBaseItemActivity.getLayoutInflater();
        View convertView;
        switch (Item.ItemType.values()[viewType]) {
            case HEADER:
                convertView = inflater.inflate(R.layout.base_item_activity_item_header, parent, false);

                return new HeaderItemViewHolder(mBaseItemActivity, convertView);

            case FOOTER:
                convertView = inflater.inflate(R.layout.base_item_activity_item_footer, parent, false);

                return new FooterItemViewHolder(mBaseItemActivity, convertView);

            case TYPING:
                convertView = inflater.inflate(R.layout.base_item_activity_typing_item, parent, false);

                return new TypingItemViewHolder(mBaseItemActivity, convertView);

            case SECURITY_INFO:
                convertView = inflater.inflate(R.layout.base_item_activity_security_info_item, parent, false);

                return new SecurityInfoItemViewHolder(mBaseItemActivity, convertView);

            case MESSAGE:
                convertView = inflater.inflate(R.layout.base_item_activity_message_item, parent, false);

                return new MessageItemViewHolder(mBaseItemActivity, convertView);

            case PEER_MESSAGE:
                convertView = inflater.inflate(R.layout.base_item_activity_peer_message_item, parent, false);

                return new PeerMessageItemViewHolder(mBaseItemActivity, convertView);

            case LINK:
                convertView = inflater.inflate(R.layout.base_item_activity_link_item, parent, false);

                return new LinkItemViewHolder(mBaseItemActivity, convertView);

            case PEER_LINK:
                convertView = inflater.inflate(R.layout.base_item_activity_peer_link_item, parent, false);

                return new PeerLinkItemViewHolder(mBaseItemActivity, convertView);

            case IMAGE:
                convertView = inflater.inflate(R.layout.base_item_activity_image_item, parent, false);

                return new ImageItemViewHolder(mBaseItemActivity, convertView, true, true);

            case PEER_IMAGE:
                convertView = inflater.inflate(R.layout.base_item_activity_peer_image_item, parent, false);

                return new PeerImageItemViewHolder(mBaseItemActivity, convertView, true, true);

            case TIME:
                convertView = inflater.inflate(R.layout.base_item_activity_time_item, parent, false);

                return new TimeItemViewHolder(mBaseItemActivity, convertView);

            case NAME:
                convertView = inflater.inflate(R.layout.base_item_activity_name_item, parent, false);

                return new NameItemViewHolder(mBaseItemActivity, convertView);

            case AUDIO:
                convertView = inflater.inflate(R.layout.base_item_activity_audio_item, parent, false);

                return new AudioItemViewHolder(mBaseItemActivity, convertView, mAudioItemObserver);

            case PEER_AUDIO:
                convertView = inflater.inflate(R.layout.base_item_activity_peer_audio_item, parent, false);

                return new PeerAudioItemViewHolder(mBaseItemActivity, convertView, mAudioItemObserver);

            case VIDEO:
                convertView = inflater.inflate(R.layout.base_item_activity_video_item, parent, false);

                return new VideoItemViewHolder(mBaseItemActivity, convertView, true, true);

            case PEER_VIDEO:
                convertView = inflater.inflate(R.layout.base_item_activity_peer_video_item, parent, false);

                return new PeerVideoItemViewHolder(mBaseItemActivity, convertView, true, true);

            case FILE:
                convertView = inflater.inflate(R.layout.base_item_activity_file_item, parent, false);
                return new FileItemViewHolder(mBaseItemActivity, convertView, true, true);

            case PEER_FILE:
                convertView = inflater.inflate(R.layout.base_item_activity_peer_file_item, parent, false);
                return new PeerFileItemViewHolder(mBaseItemActivity, convertView, true, true);

            case INVITATION:
                convertView = inflater.inflate(R.layout.base_item_activity_invitation_item, parent, false);

                return new InvitationItemViewHolder(mBaseItemActivity, convertView, true, true);

            case PEER_INVITATION:
                convertView = inflater.inflate(R.layout.base_item_activity_peer_invitation_item, parent, false);

                return new PeerInvitationItemViewHolder(mBaseItemActivity, convertView, true, true);

            case LOCATION:
                convertView = inflater.inflate(R.layout.base_item_activity_location_item, parent, false);

                return new LocationItemViewHolder(mBaseItemActivity, convertView, true, true);

            case PEER_LOCATION:
                convertView = inflater.inflate(R.layout.base_item_activity_peer_location_item, parent, false);

                return new PeerLocationItemViewHolder(mBaseItemActivity, convertView, true, true);

            case CALL:
                convertView = inflater.inflate(R.layout.base_item_activity_call_item, parent, false);

                return new CallItemViewHolder(mBaseItemActivity, convertView, true, true);

            case PEER_CALL:
                convertView = inflater.inflate(R.layout.base_item_activity_peer_call_item, parent, false);

                return new PeerCallItemViewHolder(mBaseItemActivity, convertView, true, true);

            case INVITATION_CONTACT:
                convertView = inflater.inflate(R.layout.base_item_activity_invitation_contact_item, parent, false);

                return new InvitationContactItemViewHolder(mBaseItemActivity, convertView, true, true);

            case PEER_INVITATION_CONTACT:
                convertView = inflater.inflate(R.layout.base_item_activity_peer_invitation_contact_item, parent, false);

                return new PeerInvitationContactItemViewHolder(mBaseItemActivity, convertView, true, true);

            case CLEAR:
                convertView = inflater.inflate(R.layout.base_item_activity_clear_item, parent, false);

                return new ClearItemViewHolder(mBaseItemActivity, convertView, true);

            case PEER_CLEAR:
                convertView = inflater.inflate(R.layout.base_item_activity_peer_clear_item, parent, false);

                return new PeerClearItemViewHolder(mBaseItemActivity, convertView, true);

            default:
                convertView = inflater.inflate(R.layout.base_item_activity_item_default, parent, false);

                return new DefaultItemViewHolder(mBaseItemActivity, convertView);
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull BaseItemViewHolder viewHolder) {

        super.onViewAttachedToWindow(viewHolder);

        viewHolder.onViewAttachedToWindow();
    }

    @Override
    public void onViewRecycled(@NonNull BaseItemViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        viewHolder.onViewRecycled();
    }

    private Item getItem(int position) {

        if (position == 0) {

            return mHeaderItem;
        }

        // From now on +1 on all mItems indexes to account for header item
        if (position <= mItems.size()) {

            return mItems.get(position - 1);
        }

        if (mBaseItemActivity.isPeerTyping()) {
            // account for header item
            if (position == mItems.size() + 1) {

                return mTypingItem;
            }
        }

        return mFooterItem;
    }

    public int indexToPosition(int index) {

        return index + 1;
    }
}
