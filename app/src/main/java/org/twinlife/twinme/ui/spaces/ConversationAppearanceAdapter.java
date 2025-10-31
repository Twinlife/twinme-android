/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.graphics.Bitmap;
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
import org.twinlife.twinme.utils.SectionTitleViewHolder;

public class ConversationAppearanceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "ConversationAppearan...";
    private static final boolean DEBUG = false;

    protected static final float DESIGN_MODE_TITLE_HEIGHT = 40f;
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

    private static final int ITEM_COUNT = 17;
    private static final int ONLY_CONVERSATION_ITEM_COUNT = 15;
    private static final int ONLY_CONVERSATION_OFFSET = 2;

    private static final int GENERAL_TITLE = 0;
    private static final int SUBSECTION_TITLE = 3;
    private static final int PREVIEW_APPEARANCE = 5;
    private static final int COLOR = 6;
    private static final int RESET_APPEARANCE = 7;
    private static final int INFO = 8;

    public static final int INFO_POSITION = 0;
    public static final int GENERAL_TITLE_POSITION = 1;
    public static final int SPACE_COLOR_POSITION = 2;
    public static final int PREVIEW_APPEARANCE_TITLE_POSITION = 3;
    public static final int PREVIEW_APPEARANCE_POSITION = 4;
    public static final int BACKGROUND_APPEARANCE_TITLE_POSITION = 5;
    public static final int BACKGROUND_APPEARANCE_INFORMATION_POSITION = 6;
    public static final int BACKGROUND_COLOR_POSITION = 7;
    public static final int BACKGROUND_TEXT_POSITION = 8;
    public static final int ITEM_APPEARANCE_TITLE_POSITION = 9;
    public static final int ITEM_BACKGROUND_COLOR_POSITION = 10;
    public static final int PEER_ITEM_BACKGROUND_COLOR_POSITION = 11;
    public static final int ITEM_BORDER_COLOR_POSITION = 12;
    public static final int PEER_ITEM_BORDER_COLOR_POSITION = 13;
    public static final int ITEM_TEXT_COLOR_POSITION = 14;
    public static final int PEER_ITEM_TEXT_COLOR_POSITION = 15;
    public static final int RESET_APPEARANCE_POSITION = 16;

    public interface OnAppearanceClickListener {

        void onColorClick(int position, String title, int color, int defaultColor);

        void onResetAppearanceClick();
    }

    private final OnAppearanceClickListener mOnAppearanceClickListener;
    protected final ConversationAppearanceActivity mConversationAppearenceActivity;

    private final boolean mOnlyConversationAppearance;

    private CustomAppearance mCustomAppearance;

    private Bitmap mConversationBackground;

    public ConversationAppearanceAdapter(ConversationAppearanceActivity conversationAppearanceActivity, OnAppearanceClickListener onAppearanceClickListener, CustomAppearance customAppearance, Bitmap conversationBackground, boolean onlyConversationAppearance) {

        mConversationAppearenceActivity = conversationAppearanceActivity;
        mOnAppearanceClickListener = onAppearanceClickListener;
        mOnlyConversationAppearance = onlyConversationAppearance;
        mCustomAppearance = customAppearance;
        mConversationBackground = conversationBackground;
        setHasStableIds(true);
    }

    public void setConversationBackground(Bitmap bitmap) {

        mConversationBackground = bitmap;
    }

    public void setCustomAppearance(CustomAppearance customAppearance) {

        mCustomAppearance = customAppearance;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mConversationAppearenceActivity.getLayoutInflater();
        View convertView;

        switch (viewType) {
            case INFO: {
                convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
                return new InformationViewHolder(convertView);
            }

            case GENERAL_TITLE: {
                convertView = inflater.inflate(R.layout.section_title_item, parent, false);
                ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
                layoutParams.height = ITEM_VIEW_HEIGHT;
                convertView.setLayoutParams(layoutParams);
                return new SectionTitleViewHolder(convertView);
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

        if (viewType == INFO) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
            if (position + ONLY_CONVERSATION_OFFSET == BACKGROUND_APPEARANCE_INFORMATION_POSITION) {
                informationViewHolder.onBind(mConversationAppearenceActivity.getString(R.string.space_appearance_activity_background_message), true);
            } else {
                if (mConversationAppearenceActivity.isUpdateDefaultSettings()) {
                    informationViewHolder.onBind(mConversationAppearenceActivity.getString(R.string.settings_activity_default_value_message), false);
                } else {
                    informationViewHolder.onBind(mConversationAppearenceActivity.getString(R.string.settings_space_activity_default_value_message), false);
                }
            }

        } else if (viewType == GENERAL_TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            boolean hideSeparator = false;
            sectionTitleViewHolder.onBind(getTitle(position), hideSeparator);
        } else if (viewType == SUBSECTION_TITLE) {
            SubSectionViewHolder subSectionViewHolder = (SubSectionViewHolder) viewHolder;
            boolean hideSeparator = position + ONLY_CONVERSATION_OFFSET == BACKGROUND_APPEARANCE_TITLE_POSITION;
            subSectionViewHolder.onBind(getTitle(position), hideSeparator);
        } else if (viewType == PREVIEW_APPEARANCE) {
            PreviewAppearanceViewHolder previewAppearanceViewHolder = (PreviewAppearanceViewHolder) viewHolder;
            previewAppearanceViewHolder.onBind(mCustomAppearance, mConversationBackground);
        } else if (viewType == COLOR) {
            int color = getColor(position);
            int defaultColor = getDefaultColor(position);
            String colorName = getTitle(position);
            AppearanceColorViewHolder appearanceColorViewHolder = (AppearanceColorViewHolder) viewHolder;
            appearanceColorViewHolder.itemView.setOnClickListener(view -> mOnAppearanceClickListener.onColorClick(position + ONLY_CONVERSATION_OFFSET, colorName, color, defaultColor));
            if (position == BACKGROUND_COLOR_POSITION) {
                appearanceColorViewHolder.onBind(color, colorName, mConversationBackground, false);
            } else {
                appearanceColorViewHolder.onBind(color, colorName, null, false);
            }
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

        if (mOnlyConversationAppearance) {
            return ONLY_CONVERSATION_ITEM_COUNT;
        }

        return ITEM_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position == INFO_POSITION) {
            return INFO;
        }

        if (mOnlyConversationAppearance) {
            position = position + ONLY_CONVERSATION_OFFSET;
        }

        if (position == GENERAL_TITLE_POSITION) {
            return GENERAL_TITLE;
        } else if (position == PREVIEW_APPEARANCE_POSITION) {
            return PREVIEW_APPEARANCE;
        } else if (position == PREVIEW_APPEARANCE_TITLE_POSITION || position == BACKGROUND_APPEARANCE_TITLE_POSITION || position == ITEM_APPEARANCE_TITLE_POSITION) {
            return SUBSECTION_TITLE;
        } else if (position == RESET_APPEARANCE_POSITION) {
            return RESET_APPEARANCE;
        } else if (position == BACKGROUND_APPEARANCE_INFORMATION_POSITION) {
            return INFO;
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
            int defaultColor = getDefaultColor(position);
            String colorName = getTitle(position);
            AppearanceColorViewHolder appearanceColorViewHolder = (AppearanceColorViewHolder) viewHolder;
            appearanceColorViewHolder.itemView.setOnClickListener(view -> mOnAppearanceClickListener.onColorClick(position + ONLY_CONVERSATION_OFFSET, colorName, color, defaultColor));
            if (position == BACKGROUND_COLOR_POSITION) {
                appearanceColorViewHolder.onBind(color, colorName, mConversationBackground, false);
            } else {
                appearanceColorViewHolder.onBind(color, colorName, null, false);
            }
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
            int defaultColor = getDefaultColor(position);
            String colorName = getTitle(position);
            AppearanceColorViewHolder appearanceColorViewHolder = (AppearanceColorViewHolder) viewHolder;
            appearanceColorViewHolder.itemView.setOnClickListener(view -> mOnAppearanceClickListener.onColorClick(position + ONLY_CONVERSATION_OFFSET, colorName, color, defaultColor));

            if (position == BACKGROUND_COLOR_POSITION) {
                appearanceColorViewHolder.onBind(color, colorName, mConversationBackground, false);
            } else {
                appearanceColorViewHolder.onBind(color, colorName, null, false);
            }
        }
    }

    private String getTitle(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getTitle: position=" + position);
        }

        if (mOnlyConversationAppearance) {
            position = position + ONLY_CONVERSATION_OFFSET;
        }

        String title = "";

        switch (position) {
            case GENERAL_TITLE_POSITION:
                title = mConversationAppearenceActivity.getResources().getString(R.string.space_appearance_activity_general_title);
                break;

            case SPACE_COLOR_POSITION:
                title = mConversationAppearenceActivity.getResources().getString(R.string.space_appearance_activity_theme);
                break;

            case PREVIEW_APPEARANCE_TITLE_POSITION:
                title = mConversationAppearenceActivity.getResources().getString(R.string.space_appearance_activity_preview_title);
                break;

            case BACKGROUND_APPEARANCE_TITLE_POSITION:
                title = mConversationAppearenceActivity.getResources().getString(R.string.space_appearance_activity_background_title);
                break;

            case BACKGROUND_COLOR_POSITION:
                title = mConversationAppearenceActivity.getResources().getString(R.string.application_color);
                break;

            case BACKGROUND_TEXT_POSITION:
                title = mConversationAppearenceActivity.getResources().getString(R.string.space_appearance_activity_background_text_title);
                break;

            case ITEM_APPEARANCE_TITLE_POSITION:
                title = mConversationAppearenceActivity.getResources().getString(R.string.space_appearance_activity_container_title);
                break;

            case ITEM_BACKGROUND_COLOR_POSITION:
                title = mConversationAppearenceActivity.getResources().getString(R.string.space_appearance_activity_container_background_message);
                break;

            case PEER_ITEM_BACKGROUND_COLOR_POSITION:
                title = mConversationAppearenceActivity.getResources().getString(R.string.space_appearance_activity_container_background_peer_message);
                break;

            case ITEM_BORDER_COLOR_POSITION:
                title = mConversationAppearenceActivity.getResources().getString(R.string.space_appearance_activity_container_border_message);
                break;

            case PEER_ITEM_BORDER_COLOR_POSITION:
                title = mConversationAppearenceActivity.getResources().getString(R.string.space_appearance_activity_container_border_peer_message);
                break;

            case ITEM_TEXT_COLOR_POSITION:
                title = mConversationAppearenceActivity.getResources().getString(R.string.space_appearance_activity_container_text_message);
                break;

            case PEER_ITEM_TEXT_COLOR_POSITION:
                title = mConversationAppearenceActivity.getResources().getString(R.string.space_appearance_activity_container_text_peer_message);
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

        if (mOnlyConversationAppearance) {
            position = position + ONLY_CONVERSATION_OFFSET;
        }

        int color = Color.TRANSPARENT;

        switch (position) {
            case SPACE_COLOR_POSITION:
                color = mCustomAppearance.getMainColor();
                break;

            case BACKGROUND_COLOR_POSITION:
                color = mCustomAppearance.getConversationBackgroundColor();
                break;

            case BACKGROUND_TEXT_POSITION:
                color = mCustomAppearance.getConversationBackgroundText();
                break;

            case ITEM_BACKGROUND_COLOR_POSITION:
                color = mCustomAppearance.getMessageBackgroundColor();
                break;

            case PEER_ITEM_BACKGROUND_COLOR_POSITION:
                color = mCustomAppearance.getPeerMessageBackgroundColor();
                break;

            case ITEM_BORDER_COLOR_POSITION:
                color = mCustomAppearance.getMessageBorderColor();
                break;

            case PEER_ITEM_BORDER_COLOR_POSITION:
                color = mCustomAppearance.getPeerMessageBorderColor();
                break;

            case ITEM_TEXT_COLOR_POSITION:
                color = mCustomAppearance.getMessageTextColor();
                break;

            case PEER_ITEM_TEXT_COLOR_POSITION:
                color = mCustomAppearance.getPeerMessageTextColor();
                break;

            default:
                break;
        }

        return color;
    }

    private int getDefaultColor(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getDefaultColor: position=" + position);
        }

        if (mOnlyConversationAppearance) {
            position = position + ONLY_CONVERSATION_OFFSET;
        }

        int color = Color.TRANSPARENT;

        switch (position) {
            case SPACE_COLOR_POSITION:
                color = mCustomAppearance.getMainColor();
                break;

            case BACKGROUND_COLOR_POSITION:
                color = mCustomAppearance.getConversationBackgroundDefaultColor();
                break;

            case BACKGROUND_TEXT_POSITION:
                color = mCustomAppearance.getConversationBackgroundTextDefaultColor();
                break;

            case ITEM_BACKGROUND_COLOR_POSITION:
                color = mCustomAppearance.getMessageBackgroundDefaultColor();
                break;

            case PEER_ITEM_BACKGROUND_COLOR_POSITION:
                color = mCustomAppearance.getPeerMessageBackgroundDefaultColor();
                break;

            case ITEM_BORDER_COLOR_POSITION:
                color = mCustomAppearance.getMessageBorderDefaultColor();
                break;

            case PEER_ITEM_BORDER_COLOR_POSITION:
                color = mCustomAppearance.getPeerMessageBorderDefaultColor();
                break;

            case ITEM_TEXT_COLOR_POSITION:
                color = mCustomAppearance.getMessageTextDefaultColor();
                break;

            case PEER_ITEM_TEXT_COLOR_POSITION:
                color = mCustomAppearance.getPeerMessageTextDefaultColor();
                break;

            default:
                break;
        }

        return color;
    }
}

