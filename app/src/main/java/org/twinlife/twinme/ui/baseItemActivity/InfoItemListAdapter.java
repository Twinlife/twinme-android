/*
 *  Copyright (c) 2019-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Romain Kolb (romain.kolb@skyrock.com)
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
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.conversationActivity.AnnotationInfoViewHolder;
import org.twinlife.twinme.ui.conversationActivity.MenuSwitchViewHolder;
import org.twinlife.twinme.ui.conversationActivity.UIAnnotation;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.PlatformSpecificUtils;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class InfoItemListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "ItemListAdapter";
    private static final boolean DEBUG = false;

    private static final Set<Item.ItemType> COPYABLE_ITEM_TYPES = Set.of(
            Item.ItemType.MESSAGE,
            Item.ItemType.IMAGE,
            Item.ItemType.VIDEO,
            Item.ItemType.AUDIO,
            Item.ItemType.FILE,
            Item.ItemType.LOCATION);

    private final BaseItemActivity mBaseItemActivity;
    private final List<Item> mItems;
    private final Item mItem;

    private final boolean mCanUpdateCopy;

    public InfoItemListAdapter(BaseItemActivity baseItemActivity, List<Item> items, Item item, boolean canUpdateCopy) {

        mBaseItemActivity = baseItemActivity;
        mItems = items;
        mItem = item;
        mCanUpdateCopy = canUpdateCopy;
        
        setHasStableIds(true);
    }

    public void setAnnotations(List<UIAnnotation> uiAnnotations, int startIndex) {

        Collections.sort(uiAnnotations, Comparator.comparingInt(UIAnnotation::getOrderPriority));

        List<Item> itemsToAdd = new ArrayList<>();

        for (int i = 0 ; i < uiAnnotations.size(); i++) {
            UIAnnotation uiAnnotation = uiAnnotations.get(i);
            if (i == 0 || (!uiAnnotation.getAnnotationType().equals(uiAnnotations.get(i-1).getAnnotationType()))) {
                String title = "";
                ConversationService.AnnotationType annotationType = uiAnnotation.getAnnotationType();
                if (annotationType == ConversationService.AnnotationType.LIKE) {
                    title = mBaseItemActivity.getString(R.string.info_item_view_reactions);
                } else if (annotationType== ConversationService.AnnotationType.RECEIVED) {
                    title = mBaseItemActivity.getString(R.string.info_item_view_received);
                } else if (annotationType == ConversationService.AnnotationType.READ) {
                    title = mBaseItemActivity.getString(R.string.info_item_view_seen);
                } else if (annotationType == ConversationService.AnnotationType.ERROR) {
                    title = mBaseItemActivity.getString(R.string.info_item_view_not_delivered);
                }
                itemsToAdd.add(new InfoSectionItem(mItem, title));
            }

            itemsToAdd.add(new InfoAnnotationItem(mItem, uiAnnotation));
        }

        mItems.addAll(startIndex, itemsToAdd);
        notifyItemRangeChanged(0, mItems.size());
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mItems.size();
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        Item item = getItem(position);

        if (mCanUpdateCopy && item.getType() == Item.ItemType.INFO_COPY
                && mItem != null &&
                COPYABLE_ITEM_TYPES.contains(mItem.getType())) {

            CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (compoundButton, value) -> mBaseItemActivity.updateDescriptor(value);
            MenuSwitchViewHolder menuSwitchViewHolder = (MenuSwitchViewHolder) viewHolder;
            menuSwitchViewHolder.onBind(mBaseItemActivity.getString(R.string.conversation_view_send_menu_allow_copy), mItem.getCopyAllowed() ? R.drawable.send_option_copy_allowed_icon : R.drawable.send_option_copy_icon, 0, mItem.getCopyAllowed(), true, false, Design.WHITE_COLOR, false, onCheckedChangeListener);
        } else if (item.getType() == Item.ItemType.INFO_SECTION) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            InfoSectionItem infoSectionItem = (InfoSectionItem) item;
            sectionTitleViewHolder.onBind(infoSectionItem.getTitle(), Design.LIGHT_GREY_BACKGROUND_COLOR, true);
        } else if (item.getType() == Item.ItemType.INFO_ANNOTATION) {
            InfoAnnotationItem infoAnnotationItem = (InfoAnnotationItem) item;

            boolean hideSeparator = false;
            if (position + 1 < mItems.size() && mItems.get(position + 1).getType() != Item.ItemType.INFO_ANNOTATION) {
                hideSeparator = true;
            }

            UIAnnotation uiAnnotation = infoAnnotationItem.getAnnotation();
            AnnotationInfoViewHolder annotationInfoViewHolder = (AnnotationInfoViewHolder) viewHolder;
            annotationInfoViewHolder.onBind(mBaseItemActivity, uiAnnotation, Design.WHITE_COLOR, hideSeparator);
        } else {
            BaseItemViewHolder baseItemViewHolder = (BaseItemViewHolder) viewHolder;
            baseItemViewHolder.onBind(item);
            switch (item.getType()) {
                case INFO_DATE:
                case INFO_COPY:
                case INFO_FILE:
                case INFO_DELETED:
                case INFO_EPHEMERAL:
                    viewHolder.itemView.setBackgroundColor(Design.WHITE_COLOR);
                    break;

                default:
                    viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
                    break;
            }
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
                    && COPYABLE_ITEM_TYPES.contains(mItem.getType())) {
                convertView = inflater.inflate(R.layout.menu_send_option_item, parent, false);

                return new MenuSwitchViewHolder(convertView);
            } else {
                convertView = inflater.inflate(R.layout.base_item_activity_info_copy_item, parent, false);
                return new InfoCopyItemViewHolder(mBaseItemActivity, convertView);
            }
        } else if (viewType == Item.ItemType.INFO_FILE.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_info_file_item, parent, false);
            return new InfoFileItemViewHolder(mBaseItemActivity, convertView);
        } else if (viewType == Item.ItemType.INFO_SECTION.ordinal()) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
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
        } else if (viewType == Item.ItemType.POLL.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_poll_item, parent, false);
            return new PollItemViewHolder(mBaseItemActivity, convertView);
        } else if (viewType == Item.ItemType.PEER_POLL.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_peer_poll_item, parent, false);
            return new PeerPollItemViewHolder(mBaseItemActivity, convertView);
        } else if (viewType == Item.ItemType.INVITATION.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_invitation_item, parent, false);
            return new InvitationItemViewHolder(mBaseItemActivity, convertView, false, false);
        } else if (viewType == Item.ItemType.PEER_INVITATION.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_peer_invitation_item, parent, false);
            return new PeerInvitationItemViewHolder(mBaseItemActivity, convertView, false, false);
        } else if (viewType == Item.ItemType.SHARE_CONTACT.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_share_contact_item, parent, false);
            return new ShareContactItemViewHolder(mBaseItemActivity, convertView);
        } else if (viewType == Item.ItemType.PEER_SHARE_CONTACT.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_peer_share_contact_item, parent, false);
            return new PeerShareContactItemViewHolder(mBaseItemActivity, convertView);
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
        } else if (viewType == Item.ItemType.LOCATION.ordinal()) {
            if (mBaseItemActivity.getTwinmeApplication().visualizationMap()) {
                convertView = inflater.inflate(R.layout.base_item_activity_location_item, parent, false);
                return new LocationItemViewHolder(mBaseItemActivity, convertView, true, true);
            } else {
                convertView = inflater.inflate(R.layout.base_item_activity_location_coordinate_item, parent, false);
                return new LocationCoordinateItemViewHolder(mBaseItemActivity, convertView, true, true);
            }
        } else if (viewType == Item.ItemType.PEER_LOCATION.ordinal()) {
            if (PlatformSpecificUtils.isGooglePlayServicesAvailable(mBaseItemActivity)) {
                convertView = inflater.inflate(R.layout.base_item_activity_peer_location_item, parent, false);
                return new PeerLocationItemViewHolder(mBaseItemActivity, convertView, false, false);
            } else {
                convertView = inflater.inflate(R.layout.base_item_activity_peer_location_coordinate_item, parent, false);
                return new PeerLocationCoordinateItemViewHolder(mBaseItemActivity, convertView, true, true);
            }
        } else if (viewType == Item.ItemType.INFO_EPHEMERAL.ordinal() || viewType == Item.ItemType.INFO_DELETED.ordinal()) {
            convertView = inflater.inflate(R.layout.base_item_activity_info_icon_item, parent, false);
            return new InfoIconItemViewHolder(mBaseItemActivity, convertView);
        } else if (viewType == Item.ItemType.INFO_ANNOTATION.ordinal()) {
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
