/*
 *  Copyright (c) 2019-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.conversationActivity.AnnotationInfoViewHolder;
import org.twinlife.twinme.ui.conversationActivity.MenuSendOptionViewHolder;
import org.twinlife.twinme.ui.conversationActivity.UIAnnotation;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.ArrayList;
import java.util.List;

public class InfoItemListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "ItemListAdapter";
    private static final boolean DEBUG = false;

    private static final int BACKGROUND_COLOR_GREY = Color.argb(64, 195, 212, 231);
    private final BaseItemActivity mBaseItemActivity;
    private final List<Item> mItems;
    private List<UIAnnotation> mUIAnnotations = new ArrayList<>();
    private final Item mItem;

    private static final int TITLE = 100;
    private static final int ANNOTATIONS = 101;

    private final boolean mCanUpdateCopy;

    public InfoItemListAdapter(BaseItemActivity baseItemActivity, List<Item> items, Item item, boolean canUpdateCopy) {

        mBaseItemActivity = baseItemActivity;
        mItems = items;
        mItem = item;
        mCanUpdateCopy = canUpdateCopy;
        
        setHasStableIds(true);
    }

    public void setAnnotations(List<UIAnnotation> uiAnnotations) {

        mUIAnnotations = uiAnnotations;

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        if (!mUIAnnotations.isEmpty()) {
            return mItems.size() + mUIAnnotations.size() + 1;
        }
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: position=" + position);
        }

        if (position < mItems.size()) {
            return getItem(position).getType().ordinal();
        } else if (position == mItems.size()) {
            return TITLE;
        } else {
            return ANNOTATIONS;
        }
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemId: position=" + position);
        }

        if (position < mItems.size()) {
            return getItem(position).getItemId();
        } else if (position == mItems.size()) {
            return -1;
        }

        int annotationPosition = position - mItems.size() - 1;
        UIAnnotation uiAnnotation = mUIAnnotations.get(annotationPosition);
        return uiAnnotation.getItemId() + mItems.size();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        if (position < mItems.size()) {
            Item item = getItem(position);

            if (mCanUpdateCopy && item.getType() == Item.ItemType.INFO_COPY
                    && mItem != null
                    && (mItem.getType() == Item.ItemType.MESSAGE
                    || mItem.getType() == Item.ItemType.IMAGE
                    || mItem.getType() == Item.ItemType.VIDEO
                    || mItem.getType() == Item.ItemType.AUDIO
                    || mItem.getType() == Item.ItemType.FILE)) {

                CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (compoundButton, value) -> mBaseItemActivity.updateDescriptor(value);
                MenuSendOptionViewHolder menuSendOptionViewHolder = (MenuSendOptionViewHolder) viewHolder;
                menuSendOptionViewHolder.onBind(mBaseItemActivity.getString(R.string.conversation_activity_send_menu_allow_copy), mItem.getCopyAllowed() ? R.drawable.send_option_copy_allowed_icon : R.drawable.send_option_copy_icon, 0, mItem.getCopyAllowed(), true, false, Design.WHITE_COLOR, true, onCheckedChangeListener);
            } else {
                BaseItemViewHolder baseItemViewHolder = (BaseItemViewHolder) viewHolder;
                baseItemViewHolder.onBind(item);
                switch (item.getType()) {
                    case INFO_DATE:
                    case INFO_COPY:
                    case INFO_FILE:
                        viewHolder.itemView.setBackgroundColor(Design.WHITE_COLOR);
                        break;

                    default:
                        viewHolder.itemView.setBackgroundColor(BACKGROUND_COLOR_GREY);
                        break;
                }
            }

        } else if (position == mItems.size()) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(mBaseItemActivity.getString(R.string.info_item_activity_reactions), true);
        } else {
            int annotationPosition = position - mItems.size() - 1;
            UIAnnotation uiAnnotation = mUIAnnotations.get(annotationPosition);
            AnnotationInfoViewHolder annotationInfoViewHolder = (AnnotationInfoViewHolder) viewHolder;
            boolean hideSeparator = annotationPosition + 1 == mUIAnnotations.size();
            annotationInfoViewHolder.onBind(mBaseItemActivity, uiAnnotation, Design.WHITE_COLOR, hideSeparator);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mBaseItemActivity.getLayoutInflater();
        View convertView;
        if (viewType == Item.ItemType.INFO_DATE.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_info_date_item, parent, false);
            return new InfoDateItemViewHolder(mBaseItemActivity, convertView);
        } else if (viewType == Item.ItemType.INFO_COPY.ordinal()) {
            if (mCanUpdateCopy && mItem != null
                    && (mItem.getType() == Item.ItemType.MESSAGE
                    || mItem.getType() == Item.ItemType.IMAGE
                    || mItem.getType() == Item.ItemType.VIDEO
                    || mItem.getType() == Item.ItemType.AUDIO
                    || mItem.getType() == Item.ItemType.FILE)) {
                convertView = inflater.inflate(R.layout.menu_send_option_item, parent, false);

                return new MenuSendOptionViewHolder(convertView);
            } else {
                convertView = inflater.inflate(R.layout.base_item_activity_info_copy_item, parent, false);
                return new InfoCopyItemViewHolder(mBaseItemActivity, convertView);
            }
        } else if (viewType == Item.ItemType.INFO_FILE.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_info_file_item, parent, false);
            return new InfoFileItemViewHolder(mBaseItemActivity, convertView);
        } else if (viewType == Item.ItemType.MESSAGE.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_message_item, parent, false);
            return new MessageItemViewHolder(mBaseItemActivity, convertView);
        } else if (viewType == Item.ItemType.PEER_MESSAGE.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_peer_message_item, parent, false);
            return new PeerMessageItemViewHolder(mBaseItemActivity, convertView);
        } else if (viewType == Item.ItemType.IMAGE.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_image_item, parent, false);
            return new ImageItemViewHolder(mBaseItemActivity, convertView, false, false);
        } else if (viewType == Item.ItemType.PEER_IMAGE.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_peer_image_item, parent, false);
            return new PeerImageItemViewHolder(mBaseItemActivity, convertView, false, false);
        } else if (viewType == Item.ItemType.TIME.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_time_item, parent, false);
            return new TimeItemViewHolder(mBaseItemActivity, convertView);
        } else if (viewType == Item.ItemType.NAME.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_name_item, parent, false);
            return new NameItemViewHolder(mBaseItemActivity, convertView);
        } else if (viewType == Item.ItemType.AUDIO.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_audio_item, parent, false);
            return new AudioItemViewHolder(mBaseItemActivity, convertView, null);
        } else if (viewType == Item.ItemType.PEER_AUDIO.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_peer_audio_item, parent, false);
            return new PeerAudioItemViewHolder(mBaseItemActivity, convertView, null);
        } else if (viewType == Item.ItemType.VIDEO.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_video_item, parent, false);
            return new VideoItemViewHolder(mBaseItemActivity, convertView, false, false);
        } else if (viewType == Item.ItemType.PEER_VIDEO.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_peer_video_item, parent, false);
            return new PeerVideoItemViewHolder(mBaseItemActivity, convertView, false, false);
        } else if (viewType == Item.ItemType.FILE.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_file_item, parent, false);
            return new FileItemViewHolder(mBaseItemActivity, convertView, false, false);
        } else if (viewType == Item.ItemType.PEER_FILE.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_peer_file_item, parent, false);
            return new PeerFileItemViewHolder(mBaseItemActivity, convertView, false, false);
        } else if (viewType == Item.ItemType.INVITATION.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_invitation_item, parent, false);
            return new InvitationItemViewHolder(mBaseItemActivity, convertView, false, false);
        } else if (viewType == Item.ItemType.PEER_INVITATION.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_peer_invitation_item, parent, false);
            return new PeerInvitationItemViewHolder(mBaseItemActivity, convertView, false, false);
        } else if (viewType == Item.ItemType.CALL.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_call_item, parent, false);
            return new CallItemViewHolder(mBaseItemActivity, convertView, false, false);
        } else if (viewType == Item.ItemType.PEER_CALL.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_peer_call_item, parent, false);
            return new PeerCallItemViewHolder(mBaseItemActivity, convertView, false, false);
        } else if (viewType == Item.ItemType.INVITATION_CONTACT.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_invitation_contact_item, parent, false);
            return new InvitationContactItemViewHolder(mBaseItemActivity, convertView, false, false);
        } else if (viewType == Item.ItemType.PEER_INVITATION_CONTACT.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_peer_invitation_contact_item, parent, false);
            return new PeerInvitationContactItemViewHolder(mBaseItemActivity, convertView, false, false);
        } else if (viewType == Item.ItemType.CLEAR.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_clear_item, parent, false);
            return new ClearItemViewHolder(mBaseItemActivity, convertView, true);
        } else if (viewType == Item.ItemType.PEER_CLEAR.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_peer_clear_item, parent, false);
            return new PeerClearItemViewHolder(mBaseItemActivity, convertView, true);
        } else if (viewType == TITLE) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == ANNOTATIONS) {
            convertView = inflater.inflate(R.layout.annotation_info_item, parent, false);
            return new AnnotationInfoViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.base_item_activity_item_default, parent, false);
            return new DefaultItemViewHolder(mBaseItemActivity, convertView);
        }
    }

    private Item getItem(int position) {

        return mItems.get(position);
    }
}
