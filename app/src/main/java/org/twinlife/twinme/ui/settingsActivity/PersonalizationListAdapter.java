/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.skin.FontSize;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.ui.spaces.AppearanceColorViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

public class PersonalizationListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "PersonalizationList...";
    private static final boolean DEBUG = false;

    public interface OnPersonalizationClickListener {

        void onUpdateDisplayMode(DisplayMode displayMode);

        void onUpdateFontSize(FontSize fontSize);

        void onUpdateHapticFeedback(TwinmeApplication.HapticFeedbackMode hapticFeedbackMode);

        void onUpdateMainColor();

        void onUpdateConversationColor();
    }

    private final PersonalizationActivity mListActivity;

    private final OnPersonalizationClickListener mOnPersonalizationClickListener;

    private final static int ITEM_COUNT = 19;

    private static final int SECTION_INFO = 0;
    private static final int SECTION_DEFAULT_TAB = 1;
    private static final int SECTION_MODE = 4;
    private static final int SECTION_APPEARANCE = 6;
    private static final int SECTION_FONT = 9;
    private static final int SECTION_HAPTIC_FEEDBACK = 14;
    private static final int POSITION_DEFAULT_TAB_INFORMATION = 2;
    private static final int POSITION_SELECT_DEFAULT_TAB = 3;

    private static final int POSITION_DISPLAY_MODE = 5;

    private static final int POSITION_MAIN_COLOR = 7;
    private static final int POSITION_CONVERSATION_APPEARANCE = 8;
    private static final int POSITION_FONT_SYSTEM = 10;
    private static final int POSITION_FONT_SMALL = 11;
    private static final int POSITION_FONT_LARGE = 12;
    private static final int POSITION_FONT_EXTRA_LARGE = 13;
    private static final int POSITION_HAPTIC_FEEDBACK_INFORMATION = 15;

    private static final int POSITION_HAPTIC_FEEDBACK_SYSTEM = 16;
    private static final int POSITION_HAPTIC_FEEDBACK_ON = 17;
    private static final int POSITION_HAPTIC_FEEDBACK_OFF = 18;

    private static final int TITLE = 0;
    private static final int PERSONALIZATION = 1;
    private static final int DEFAULT_TAB = 2;
    private static final int INFORMATION = 3;
    private static final int DISPLAY_MODE = 4;
    private static final int COLOR = 5;
    private static final int SUBSECTION = 6;

    PersonalizationListAdapter(PersonalizationActivity listActivity, OnPersonalizationClickListener onPersonalizationClickListener) {

        mListActivity = listActivity;
        mOnPersonalizationClickListener = onPersonalizationClickListener;
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

        if (position == SECTION_MODE || position == SECTION_APPEARANCE || position == SECTION_DEFAULT_TAB || position == SECTION_HAPTIC_FEEDBACK || position == SECTION_FONT) {
            return TITLE;
        } else if (position == POSITION_SELECT_DEFAULT_TAB) {
            return DEFAULT_TAB;
        } else if (position == POSITION_DEFAULT_TAB_INFORMATION || position == SECTION_INFO || position == POSITION_HAPTIC_FEEDBACK_INFORMATION) {
            return INFORMATION;
        } else if (position == POSITION_DISPLAY_MODE) {
            return DISPLAY_MODE;
        } else if (position == POSITION_MAIN_COLOR) {
            return COLOR;
        } else if (position == POSITION_CONVERSATION_APPEARANCE) {
            return SUBSECTION;
        } else {
            return PERSONALIZATION;
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
            boolean hideSeparator = position == SECTION_DEFAULT_TAB || position == SECTION_HAPTIC_FEEDBACK;
            sectionTitleViewHolder.onBind(getSectionTitle(position), hideSeparator);
        } else if (viewType == DISPLAY_MODE) {
            DisplayModeViewHolder displayModeViewHolder = (DisplayModeViewHolder) viewHolder;
            displayModeViewHolder.onBind(mListActivity.getDisplayMode().ordinal(), mListActivity.getMainColor());
        } else if (viewType == SUBSECTION) {
            SettingSectionViewHolder settingSectionViewHolder = (SettingSectionViewHolder) viewHolder;
            settingSectionViewHolder.itemView.setOnClickListener(view -> mOnPersonalizationClickListener.onUpdateConversationColor());
            settingSectionViewHolder.onBind(mListActivity.getString(R.string.conversations_fragment_title), true);
        } else if (viewType == COLOR) {
            AppearanceColorViewHolder appearanceColorViewHolder = (AppearanceColorViewHolder) viewHolder;
            appearanceColorViewHolder.itemView.setOnClickListener(view -> mOnPersonalizationClickListener.onUpdateMainColor());
            appearanceColorViewHolder.onBind(mListActivity.getMainColor(), mListActivity.getString(R.string.application_theme), null, false);
        } else if (viewType == PERSONALIZATION) {
            PersonalizationViewHolder personalizationViewHolder = (PersonalizationViewHolder) viewHolder;

            boolean isSelected = false;
            String title = "";

            int fontSize = mListActivity.getTwinmeApplication().fontSize();
            int hapticFeedbackMode = mListActivity.getTwinmeApplication().hapticFeedbackMode();

            switch (position) {
                case POSITION_HAPTIC_FEEDBACK_SYSTEM:
                    title = mListActivity.getString(R.string.personalization_activity_system);
                    personalizationViewHolder.itemView.setOnClickListener(view -> mOnPersonalizationClickListener.onUpdateHapticFeedback(TwinmeApplication.HapticFeedbackMode.SYSTEM));
                    isSelected = hapticFeedbackMode == TwinmeApplication.HapticFeedbackMode.SYSTEM.ordinal();
                    break;

                case POSITION_HAPTIC_FEEDBACK_ON:
                    title = mListActivity.getString(R.string.application_on);
                    personalizationViewHolder.itemView.setOnClickListener(view -> mOnPersonalizationClickListener.onUpdateHapticFeedback(TwinmeApplication.HapticFeedbackMode.ON));
                    isSelected = hapticFeedbackMode == TwinmeApplication.HapticFeedbackMode.ON.ordinal();
                    break;

                case POSITION_HAPTIC_FEEDBACK_OFF:
                    title = mListActivity.getString(R.string.application_off);
                    personalizationViewHolder.itemView.setOnClickListener(view -> mOnPersonalizationClickListener.onUpdateHapticFeedback(TwinmeApplication.HapticFeedbackMode.OFF));
                    isSelected = hapticFeedbackMode == TwinmeApplication.HapticFeedbackMode.OFF.ordinal();
                    break;

                case POSITION_FONT_SYSTEM:
                    title = mListActivity.getString(R.string.personalization_activity_system);
                    personalizationViewHolder.itemView.setOnClickListener(view -> mOnPersonalizationClickListener.onUpdateFontSize(FontSize.SYSTEM));
                    isSelected = fontSize == FontSize.SYSTEM.ordinal();
                    break;

                case POSITION_FONT_SMALL:
                    title = mListActivity.getString(R.string.personalization_activity_font_small);
                    personalizationViewHolder.itemView.setOnClickListener(view -> mOnPersonalizationClickListener.onUpdateFontSize(FontSize.SMALL));
                    isSelected = fontSize == FontSize.SMALL.ordinal();
                    break;

                case POSITION_FONT_LARGE:
                    title = mListActivity.getString(R.string.personalization_activity_font_large);
                    personalizationViewHolder.itemView.setOnClickListener(view -> mOnPersonalizationClickListener.onUpdateFontSize(FontSize.LARGE));
                    isSelected = fontSize == FontSize.LARGE.ordinal();
                    break;

                case POSITION_FONT_EXTRA_LARGE:
                    title = mListActivity.getString(R.string.personalization_activity_font_extra_large);
                    personalizationViewHolder.itemView.setOnClickListener(view -> mOnPersonalizationClickListener.onUpdateFontSize(FontSize.EXTRA_LARGE));
                    isSelected = fontSize == FontSize.EXTRA_LARGE.ordinal();
                    break;
            }

            personalizationViewHolder.onBind(title, isSelected, mListActivity.getMainColor());
        } else if (viewType == DEFAULT_TAB) {
            DefaultTabViewHolder defaultTabViewHolder = (DefaultTabViewHolder) viewHolder;
            defaultTabViewHolder.onBind(mListActivity.getMainColor());
        } else if (viewType == INFORMATION) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
            if (position == SECTION_INFO) {
                informationViewHolder.onBind(mListActivity.getString(R.string.settings_activity_default_value_message), false);
            } else if (position == POSITION_DEFAULT_TAB_INFORMATION) {
                informationViewHolder.onBind(mListActivity.getString(R.string.personalization_activity_start_tab_information), true);
            } else {
                informationViewHolder.onBind(mListActivity.getString(R.string.personalization_activity_haptic_feedback_message), true);
            }
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
        } else if (viewType == DEFAULT_TAB) {
            convertView = inflater.inflate(R.layout.personalization_activity_default_tab_item, parent, false);
            return new DefaultTabViewHolder(convertView, mListActivity);
        } else if (viewType == INFORMATION) {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        } else if (viewType == COLOR) {
            convertView = inflater.inflate(R.layout.space_appearance_activity_appearance_color_item, parent, false);
            return new AppearanceColorViewHolder(convertView);
        } else if (viewType == SUBSECTION) {
            convertView = inflater.inflate(R.layout.settings_activity_item_section, parent, false);
            return new SettingSectionViewHolder(convertView);
        } else if (viewType == DISPLAY_MODE) {
            convertView = inflater.inflate(R.layout.personalization_activity_mode_item, parent, false);
            DisplayModeViewHolder.Observer observer = displayMode -> {
                mOnPersonalizationClickListener.onUpdateDisplayMode(displayMode);
            };
            return new DisplayModeViewHolder(convertView, observer);
        } else {
            convertView = inflater.inflate(R.layout.personalization_activity_item, parent, false);
            return new PersonalizationViewHolder(convertView);
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
            boolean hideSeparator = position == SECTION_DEFAULT_TAB || position == SECTION_HAPTIC_FEEDBACK;
            sectionTitleViewHolder.onBind(getSectionTitle(position), hideSeparator);
        }
    }

    private String getSectionTitle(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getSectionTitle: " + position);
        }

        String title = "";
        switch (position) {

            case SECTION_APPEARANCE:
                title = mListActivity.getString(R.string.application_appearance);
                break;

            case SECTION_DEFAULT_TAB:
                title = mListActivity.getString(R.string.personalization_activity_start_tab_title);
                break;

            case SECTION_MODE:
                title = mListActivity.getString(R.string.personalization_activity_mode);
                break;

            case SECTION_HAPTIC_FEEDBACK:
                title = mListActivity.getString(R.string.personalization_activity_haptic_feedback);
                break;

            case SECTION_FONT:
                title = mListActivity.getString(R.string.personalization_activity_font);
                break;

            default:
                break;
        }

        return title;
    }
}
