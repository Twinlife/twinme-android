/*
 *  Copyright (c) 2022-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.EmojiSize;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

public class ConversationSettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "PersonalizationList...";
    private static final boolean DEBUG = false;

    public interface OnConversationSettingsClickListener {

        void onUpdateEmojiSize(EmojiSize emojiSize);

        void onColorsAndBackgroundClick();
    }

    private final ConversationSettingsActivity mListActivity;

    private final OnConversationSettingsClickListener mOnConversationSettingsClickListener;

    private static final int ITEM_COUNT = 5;

    private static final int POSITION_COLORS_BACKGROUND = 0;
    private static final int SECTION_EMOJI = 1;

    private static final int POSITION_SMALL_EMOJI = 2;
    private static final int POSITION_STANDARD_EMOJI = 3;
    private static final int POSITION_LARGE_EMOJI = 4;

    private static final int TITLE = 0;
    private static final int PERSONNALIZATION = 1;
    private static final int SUBSECTION = 2;

    ConversationSettingsAdapter(ConversationSettingsActivity listActivity, OnConversationSettingsClickListener onConversationSettingsClickListener) {

        mListActivity = listActivity;
        mOnConversationSettingsClickListener = onConversationSettingsClickListener;
        setHasStableIds(true);
    }

    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        notifyItemRangeChanged(0, ITEM_COUNT);
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return ITEM_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position == SECTION_EMOJI) {
            return TITLE;
        } else if (position == POSITION_COLORS_BACKGROUND) {
            return SUBSECTION;
        } else {
            return PERSONNALIZATION;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(getSectionTitle(position), false);
        } else if (viewType == SUBSECTION) {
            SettingSectionViewHolder settingSectionViewHolder = (SettingSectionViewHolder) viewHolder;
            settingSectionViewHolder.itemView.setOnClickListener(view -> mOnConversationSettingsClickListener.onColorsAndBackgroundClick());
            settingSectionViewHolder.onBind(mListActivity.getString(R.string.conversation_settings_activity_background_colors), false);
        } else if (viewType == PERSONNALIZATION) {
            EmojiSizeViewHolder emojiSizeViewHolder = (EmojiSizeViewHolder) viewHolder;

            boolean isSelected = false;
            String title = "";

            int emojiFontSize = mListActivity.getTwinmeApplication().emojiFontSize();
            EmojiSize emojiSize = EmojiSize.STANDARD;
            if (position == POSITION_SMALL_EMOJI) {
                title = mListActivity.getString(R.string.personalization_activity_font_small);
                emojiSizeViewHolder.itemView.setOnClickListener(view -> mOnConversationSettingsClickListener.onUpdateEmojiSize(EmojiSize.SMALL));
                isSelected = emojiFontSize == EmojiSize.SMALL.ordinal();
                emojiSize = EmojiSize.SMALL;
            } else if (position == POSITION_STANDARD_EMOJI) {
                title = mListActivity.getString(R.string.conversation_activity_reduce_menu_lower);
                emojiSizeViewHolder.itemView.setOnClickListener(view -> mOnConversationSettingsClickListener.onUpdateEmojiSize(EmojiSize.STANDARD));
                isSelected = emojiFontSize == EmojiSize.STANDARD.ordinal();
            } else if (position == POSITION_LARGE_EMOJI) {
                title = mListActivity.getString(R.string.personalization_activity_font_large);
                emojiSizeViewHolder.itemView.setOnClickListener(view -> mOnConversationSettingsClickListener.onUpdateEmojiSize(EmojiSize.LARGE));
                isSelected = emojiFontSize == EmojiSize.LARGE.ordinal();
                emojiSize = EmojiSize.LARGE;
            }

            emojiSizeViewHolder.onBind(title, emojiSize, isSelected);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mListActivity.getLayoutInflater();
        View convertView;

        if (viewType == TITLE) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else if (viewType == SUBSECTION) {
            convertView = inflater.inflate(R.layout.settings_activity_item_section, parent, false);
            return new SettingSectionViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.conversation_settings_activity_emoji_size_item, parent, false);
            return new EmojiSizeViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        int position = viewHolder.getBindingAdapterPosition();
        int viewType = getItemViewType(position);
        if (viewType == TITLE && position != -1) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(getSectionTitle(position), false);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewDetachedFromWindow: viewHolder=" + viewHolder);
        }

        super.onViewDetachedFromWindow(viewHolder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewAttachedToWindow: viewHolder=" + viewHolder);
        }

        super.onViewAttachedToWindow(viewHolder);

        int position = viewHolder.getBindingAdapterPosition();
        int viewType = getItemViewType(position);
        if (viewType == TITLE && position != -1) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(getSectionTitle(position), false);
        }
    }

    private String getSectionTitle(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getSectionTitle: " + position);
        }

        String title = "";

        if (position == SECTION_EMOJI) {
            title = mListActivity.getString(R.string.conversation_settings_activity_emoji_size);
        }

        return title;
    }
}
