/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.conversations;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.services.ChatService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.mainActivity.ConversationsFragment;
import org.twinlife.twinme.ui.mainActivity.UIConversation;
import org.twinlife.twinme.ui.mainActivity.UIGroupConversation;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.ui.users.UIContactViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ConversationsSearchAdapter extends ListAdapter<SearchResultItem, RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "ConversationsSear...";
    private static final boolean DEBUG = false;

    public interface OnSearchClickListener {

        void onConversationClick(UIConversation uiConversation);

        void onShowAllContactClick();

        void onShowAllGroupClick();

        void onCurrentListChanged();
    }

    private static final DiffUtil.ItemCallback<SearchResultItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SearchResultItem>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull SearchResultItem oldItem, @NonNull SearchResultItem newItem) {
                    return oldItem.isSameItem(newItem);
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull SearchResultItem oldItem, @NonNull SearchResultItem newItem) {
                    return oldItem.hasSameContent(newItem);
                }
            };

    @NonNull
    private final ConversationsFragment mConversationsFragment;
    @NonNull
    private final ChatService mChatService;
    @NonNull
    private final OnSearchClickListener mOnSearchClickListener;

    @NonNull
    private String mSearchContent = "";

    public ConversationsSearchAdapter(@NonNull ConversationsFragment conversationsFragment, @NonNull ChatService chatService, @NonNull OnSearchClickListener onSearchClickListener) {
        super(DIFF_CALLBACK);
        mConversationsFragment = conversationsFragment;
        mChatService = chatService;
        mOnSearchClickListener = onSearchClickListener;
    }

    public void updateSearchParam(@NonNull ConversationsFragment.SearchFilter searchFilter, @NonNull String searchContent, @NonNull ArrayList<UIConversation> searchContacts, @NonNull ArrayList<UIGroupConversation> searchGroups, @NonNull ArrayList<UIConversation> searchConversations) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSearchParam: " + searchFilter);
        }

        mSearchContent = searchContent;

        ArrayList<SearchResultItem> items = new ArrayList<>();

        if (searchFilter == ConversationsFragment.SearchFilter.CONTACTS) {
            for (UIConversation uiConversation : searchContacts) {
                items.add(new SearchResultItem.Conversation(SearchResultItem.Type.CONTACT, uiConversation, searchContent));
            }
        } else if (searchFilter == ConversationsFragment.SearchFilter.GROUPS) {
            for (UIConversation uiConversation : searchGroups) {
                items.add(new SearchResultItem.Conversation(SearchResultItem.Type.GROUP, uiConversation, searchContent));
            }
        } else if (searchFilter == ConversationsFragment.SearchFilter.MESSAGES) {
            for (UIConversation uiConversation : searchConversations) {
                items.add(new SearchResultItem.Conversation(SearchResultItem.Type.MESSAGE, uiConversation, searchContent));
            }
        } else { // ALL
            if (!searchContacts.isEmpty()) {
                items.add(new SearchResultItem.Header(ConversationsFragment.SearchFilter.CONTACTS, mConversationsFragment.getString(R.string.contacts_fragment_title)));
                int nbContacts = mConversationsFragment.isShowAllContacts() ? searchContacts.size() : Math.min(searchContacts.size(), ConversationsFragment.MIN_RESULTS_VISIBLE);
                for (int i = 0; i < nbContacts; i++) {
                    items.add(new SearchResultItem.Conversation(SearchResultItem.Type.CONTACT, searchContacts.get(i), searchContent));
                }
                items.add(new SearchResultItem.Footer(ConversationsFragment.SearchFilter.CONTACTS));
            }

            if (!searchGroups.isEmpty()) {
                items.add(new SearchResultItem.Header(ConversationsFragment.SearchFilter.GROUPS, mConversationsFragment.getString(R.string.share_activity_group_list)));
                int nbGroups = mConversationsFragment.isShowAllGroups() ? searchGroups.size() : Math.min(searchGroups.size(), ConversationsFragment.MIN_RESULTS_VISIBLE);
                for (int i = 0; i < nbGroups; i++) {
                    items.add(new SearchResultItem.Conversation(SearchResultItem.Type.GROUP, searchGroups.get(i), searchContent));
                }
                items.add(new SearchResultItem.Footer(ConversationsFragment.SearchFilter.GROUPS));
            }

            if (!searchConversations.isEmpty()) {
                items.add(new SearchResultItem.Header(ConversationsFragment.SearchFilter.MESSAGES, mConversationsFragment.getString(R.string.settings_activity_chat_category_title)));
                for (UIConversation uiConversation : searchConversations) {
                    items.add(new SearchResultItem.Conversation(SearchResultItem.Type.MESSAGE, uiConversation, searchContent));
                }
                items.add(new SearchResultItem.Footer(ConversationsFragment.SearchFilter.MESSAGES));
            }
        }

        submitList(items);
    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    @Override
    public void onCurrentListChanged(@NonNull List<SearchResultItem> previousList, @NonNull List<SearchResultItem> currentList) {
        mOnSearchClickListener.onCurrentListChanged();
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return getCurrentList().size();
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        SearchResultItem item = getItem(position);

        return item.type.index;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        SearchResultItem item = getItem(position);

        if (item instanceof SearchResultItem.Header) {
            SearchResultItem.Header header = (SearchResultItem.Header) item;
            SearchSectionViewHolder searchSectionViewHolder = (SearchSectionViewHolder) viewHolder;
            boolean showAction = false;
            Runnable runnable = null;

            if (header.section == ConversationsFragment.SearchFilter.CONTACTS && !mConversationsFragment.isShowAllContacts()) {
                showAction = true;
                runnable = mOnSearchClickListener::onShowAllContactClick;
            } else if (header.section == ConversationsFragment.SearchFilter.GROUPS && !mConversationsFragment.isShowAllGroups()) {
                showAction = true;
                runnable = mOnSearchClickListener::onShowAllGroupClick;
            }

            searchSectionViewHolder.onBind(header.title, showAction, runnable);
        } else if (item instanceof SearchResultItem.Footer) {
            SearchSectionFooterViewHolder searchSectionFooterViewHolder = (SearchSectionFooterViewHolder) viewHolder;
            searchSectionFooterViewHolder.onBind();
        } else if (item instanceof SearchResultItem.Conversation) {
            SearchResultItem.Conversation convItem = (SearchResultItem.Conversation) item;
            if (item.type == SearchResultItem.Type.GROUP || item.type == SearchResultItem.Type.CONTACT) {
                //noinspection unchecked
                UIContactViewHolder<UIContact> contactViewHolder = (UIContactViewHolder<UIContact>) viewHolder;
                contactViewHolder.itemView.setOnClickListener(view -> mOnSearchClickListener.onConversationClick(convItem.uiConversation));
                contactViewHolder.onBind(mConversationsFragment.getContext(), convItem.uiConversation.getUIContact(), mSearchContent, true);
            } else {
                SearchContentMessageViewHolder searchContentMessageViewHolder = (SearchContentMessageViewHolder) viewHolder;
                searchContentMessageViewHolder.itemView.setOnClickListener(view -> mOnSearchClickListener.onConversationClick(convItem.uiConversation));
                searchContentMessageViewHolder.onBind(mConversationsFragment.getContext(), convItem.uiConversation, mSearchContent);
            }
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mConversationsFragment.getLayoutInflater();
        View convertView;

        if (viewType == SearchResultItem.Type.HEADER.index) {
            convertView = inflater.inflate(R.layout.search_section_item, parent, false);
            return new SearchSectionViewHolder(convertView);
        } else if (viewType == SearchResultItem.Type.FOOTER.index) {
            convertView = inflater.inflate(R.layout.search_section_footer_item, parent, false);
            return new SearchSectionFooterViewHolder(convertView);
        } else if (viewType == SearchResultItem.Type.CONTACT.index || viewType == SearchResultItem.Type.GROUP.index) {
            convertView = inflater.inflate(R.layout.contacts_fragment_contact_item, parent, false);

            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
            layoutParams.height = Design.ITEM_VIEW_HEIGHT;
            convertView.setLayoutParams(layoutParams);

            return new UIContactViewHolder<UIContact>(mChatService, convertView, R.id.contacts_fragment_contact_item_name_view, R.id.contacts_fragment_contact_item_avatar_view, R.id.contacts_fragment_contact_item_tag_view, R.id.contacts_fragment_contact_item_tag_title_view, 0, R.id.contacts_fragment_contact_item_certified_image_view, R.id.contacts_fragment_contact_item_separator_view, Design.FONT_REGULAR34);
        } else {
            convertView = inflater.inflate(R.layout.search_content_message_item, parent, false);
            return new SearchContentMessageViewHolder(mChatService, convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        if (viewHolder instanceof UIContactViewHolder) {
            ((UIContactViewHolder<?>) viewHolder).onViewRecycled();
        } else if (viewHolder instanceof SearchContentMessageViewHolder) {
            ((SearchContentMessageViewHolder) viewHolder).onViewRecycled();
        } else if (viewHolder instanceof SearchSectionViewHolder) {
            ((SearchSectionViewHolder) viewHolder).onViewRecycled();
        } else if (viewHolder instanceof SearchSectionFooterViewHolder) {
            ((SearchSectionFooterViewHolder) viewHolder).onViewRecycled();
        }
    }
}