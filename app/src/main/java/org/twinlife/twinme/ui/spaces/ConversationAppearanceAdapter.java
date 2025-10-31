/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;

public class ConversationAppearanceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "ConversationAppearan...";
    private static final boolean DEBUG = false;

    protected static final float DESIGN_MODE_TITLE_HEIGHT = 80f;
    protected static final float DESIGN_SUBSECTION_VIEW_HEIGHT = 80f;
    protected static final float DESIGN_CONVERSATION_VIEW_HEIGHT = 140f;
    protected static final float DESIGN_ITEM_VIEW_HEIGHT = 120f;
    protected static final int MODE_TITLE_HEIGHT;
    protected static final int SUBSECTION_VIEW_HEIGHT;
    protected static final int CONVERSATION_VIEW_HEIGHT;
    protected static final int ITEM_VIEW_HEIGHT;

    static {
        MODE_TITLE_HEIGHT = (int) (DESIGN_MODE_TITLE_HEIGHT * Design.HEIGHT_RATIO);
        SUBSECTION_VIEW_HEIGHT = (int) (DESIGN_SUBSECTION_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        CONVERSATION_VIEW_HEIGHT = (int) (DESIGN_CONVERSATION_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private static final int ITEM_COUNT = 13;

    private static final int MODE_TITLE = 0;
    private static final int SUBSECTION_TITLE = 1;
    private static final int PREVIEW_APPEARANCE = 2;
    private static final int COLOR = 3;
    private static final int RESET_APPEARANCE = 4;
    private static final int INFORMATION = 5;

    public static final int PREVIEW_APPEARANCE_TITLE_POSITION = 0;
    public static final int PREVIEW_APPEARANCE_POSITION = 1;
    public static final int BACKGROUND_APPEARANCE_TITLE_POSITION = 2;
    public static final int BACKGROUND_APPEARANCE_INFORMATION_POSITION = 3;
    public static final int BACKGROUND_COLOR_POSITION = 4;
    public static final int BACKGROUND_TEXT_POSITION = 5;
    public static final int ITEM_APPEARANCE_TITLE_POSITION = 6;
    public static final int ITEM_BACKGROUND_COLOR_POSITION = 7;
    public static final int PEER_ITEM_BACKGROUND_COLOR_POSITION = 8;
    public static final int ITEM_BORDER_COLOR_POSITION = 9;
    public static final int PEER_ITEM_BORDER_COLOR_POSITION = 10;
    public static final int ITEM_TEXT_COLOR_POSITION = 11;
    public static final int PEER_ITEM_TEXT_COLOR_POSITION = 12;
    public static final int RESET_APPEARANCE_POSITION = 13;

    public interface OnAppearanceClickListener {

        void onColorClick(int position, String color);

        void onResetAppearanceClick();
    }

    private final OnAppearanceClickListener mOnAppearanceClickListener;
    protected final ConversationAppearanceActivity mConversationAppearanceActivity;

    public ConversationAppearanceAdapter(ConversationAppearanceActivity conversationAppearanceActivity, OnAppearanceClickListener onAppearanceClickListener) {

        mConversationAppearanceActivity = conversationAppearanceActivity;
        mOnAppearanceClickListener = onAppearanceClickListener;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mConversationAppearanceActivity.getLayoutInflater();
        View convertView;

        switch (viewType) {
            case MODE_TITLE: {
                convertView = inflater.inflate(R.layout.space_appearance_activity_subsection_item, parent, false);
                ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
                layoutParams.height = MODE_TITLE_HEIGHT;
                convertView.setLayoutParams(layoutParams);
                return new SubSectionViewHolder(convertView, MODE_TITLE_HEIGHT);
            }
            case SUBSECTION_TITLE: {
                convertView = inflater.inflate(R.layout.space_appearance_activity_subsection_item, parent, false);
                ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
                layoutParams.height = SUBSECTION_VIEW_HEIGHT;
                convertView.setLayoutParams(layoutParams);
                return new SubSectionViewHolder(convertView);
            }
            case PREVIEW_APPEARANCE: {
                convertView = inflater.inflate(R.layout.space_appearance_activity_preview_appearance_item, parent, false);
                return new PreviewAppearanceViewHolder(convertView);
            }
            case RESET_APPEARANCE: {
                convertView = inflater.inflate(R.layout.space_appearance_activity_reset_appearance_item, parent, false);
                ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
                layoutParams.height = ITEM_VIEW_HEIGHT;
                convertView.setLayoutParams(layoutParams);
                return new ResetSettingsViewHolder(convertView);
            }
            case INFORMATION: {
                convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
                return new InformationViewHolder(convertView);
            }
            case COLOR:
            default: {
                convertView = inflater.inflate(R.layout.space_appearance_activity_appearance_color_item, parent, false);
                ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
                layoutParams.height = ITEM_VIEW_HEIGHT;
                convertView.setLayoutParams(layoutParams);
                return new AppearanceColorViewHolder(convertView);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == MODE_TITLE || viewType == SUBSECTION_TITLE) {
            SubSectionViewHolder subSectionViewHolder = (SubSectionViewHolder) viewHolder;
            boolean hideSeparator = position == BACKGROUND_APPEARANCE_TITLE_POSITION;
            subSectionViewHolder.onBind(getTitle(position), hideSeparator);
        } else if (viewType == PREVIEW_APPEARANCE) {
            PreviewAppearanceViewHolder previewAppearanceViewHolder = (PreviewAppearanceViewHolder) viewHolder;
            previewAppearanceViewHolder.onBind();
        } else if (viewType == INFORMATION) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
            informationViewHolder.onBind(mConversationAppearanceActivity.getString(R.string.space_appearance_activity_background_message), true);
        } else if (viewType == COLOR) {
            int color = getColor(position);
            String colorName = getTitle(position);
            AppearanceColorViewHolder appearanceColorViewHolder = (AppearanceColorViewHolder) viewHolder;
            appearanceColorViewHolder.itemView.setOnClickListener(view -> mOnAppearanceClickListener.onColorClick(position, colorName));
            appearanceColorViewHolder.onBind(color, colorName, null, false);
        } else if (viewType == RESET_APPEARANCE) {
            ResetSettingsViewHolder resetSettingsViewHolder = (ResetSettingsViewHolder) viewHolder;
            resetSettingsViewHolder.itemView.setOnClickListener(view -> mOnAppearanceClickListener.onResetAppearanceClick());
        }
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

        if (position == PREVIEW_APPEARANCE_POSITION) {
            return PREVIEW_APPEARANCE;
        } else if (position == PREVIEW_APPEARANCE_TITLE_POSITION || position == BACKGROUND_APPEARANCE_TITLE_POSITION || position == ITEM_APPEARANCE_TITLE_POSITION) {
            return SUBSECTION_TITLE;
        } else if (position == RESET_APPEARANCE_POSITION) {
            return RESET_APPEARANCE;
        } else if (position == BACKGROUND_APPEARANCE_INFORMATION_POSITION) {
            return INFORMATION;
        }

        return COLOR;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        int position = viewHolder.getBindingAdapterPosition();
        int viewType = getItemViewType(position);

        if (viewType == COLOR && position != -1) {
            int color = getColor(position);
            String colorName = getTitle(position);
            AppearanceColorViewHolder appearanceColorViewHolder = (AppearanceColorViewHolder) viewHolder;
            appearanceColorViewHolder.itemView.setOnClickListener(view -> mOnAppearanceClickListener.onColorClick(position, colorName));
            appearanceColorViewHolder.onBind(color, colorName, null, false);
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

        if (viewType == COLOR && position != -1) {
            int color = getColor(position);
            String colorName = getTitle(position);
            AppearanceColorViewHolder appearanceColorViewHolder = (AppearanceColorViewHolder) viewHolder;
            appearanceColorViewHolder.itemView.setOnClickListener(view -> mOnAppearanceClickListener.onColorClick(position, colorName));
            appearanceColorViewHolder.onBind(color, colorName, null, false);
        }
    }

    private String getTitle(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getTitle: position=" + position);
        }

        String title = "";

        switch (position) {
            case PREVIEW_APPEARANCE_TITLE_POSITION:
                title = mConversationAppearanceActivity.getResources().getString(R.string.space_appearance_activity_preview_title);
                break;

            case BACKGROUND_APPEARANCE_TITLE_POSITION:
                title = mConversationAppearanceActivity.getResources().getString(R.string.space_appearance_activity_background_title);
                break;

            case BACKGROUND_COLOR_POSITION:
                title = mConversationAppearanceActivity.getResources().getString(R.string.application_color);
                break;

            case BACKGROUND_TEXT_POSITION:
                title = mConversationAppearanceActivity.getResources().getString(R.string.space_appearance_activity_background_text_title);
                break;

            case ITEM_APPEARANCE_TITLE_POSITION:
                title = mConversationAppearanceActivity.getResources().getString(R.string.space_appearance_activity_container_title);
                break;

            case ITEM_BACKGROUND_COLOR_POSITION:
                title = mConversationAppearanceActivity.getResources().getString(R.string.space_appearance_activity_container_background_message);
                break;

            case PEER_ITEM_BACKGROUND_COLOR_POSITION:
                title = mConversationAppearanceActivity.getResources().getString(R.string.space_appearance_activity_container_background_peer_message);
                break;

            case ITEM_BORDER_COLOR_POSITION:
                title = mConversationAppearanceActivity.getResources().getString(R.string.space_appearance_activity_container_border_message);
                break;

            case PEER_ITEM_BORDER_COLOR_POSITION:
                title = mConversationAppearanceActivity.getResources().getString(R.string.space_appearance_activity_container_border_peer_message);
                break;

            case ITEM_TEXT_COLOR_POSITION:
                title = mConversationAppearanceActivity.getResources().getString(R.string.space_appearance_activity_container_text_message);
                break;

            case PEER_ITEM_TEXT_COLOR_POSITION:
                title = mConversationAppearanceActivity.getResources().getString(R.string.space_appearance_activity_container_text_peer_message);
                break;

            default:
                break;
        }

        return title;
    }

    private int getColor(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getColor: position=" + position);
        }

        int color = Color.TRANSPARENT;

        switch (position) {
            case BACKGROUND_COLOR_POSITION:
                color = Design.CONVERSATION_BACKGROUND_COLOR;
                break;

            case BACKGROUND_TEXT_POSITION:
                color = Design.TIME_COLOR;
                break;

            case ITEM_BACKGROUND_COLOR_POSITION:
                color = Design.getMainStyle();
                break;

            case PEER_ITEM_BACKGROUND_COLOR_POSITION:
                color = Design.GREY_ITEM_COLOR;
                break;

            case ITEM_TEXT_COLOR_POSITION:
                color = Color.WHITE;
                break;

            case PEER_ITEM_TEXT_COLOR_POSITION:
                color = Design.FONT_COLOR_DEFAULT;
                break;

            default:
                break;
        }

        return color;
    }
}