/*
 *  Copyright (c) 2020-2026 twinlife SA.
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
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.skin.FontSize;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.ArrayList;
import java.util.List;

public class  PersonalizationListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "PersonalizationList...";
    private static final boolean DEBUG = false;

    public interface OnPersonalizationClickListener {

        void onUpdateDisplayMode(DisplayMode displayMode);

        void onUpdateFontSize(FontSize fontSize);
        
        void onUpdateMainColor();

        void onUpdateConversationColor();
    }

    private final PersonalizationActivity mListActivity;
    private final OnPersonalizationClickListener mOnPersonalizationClickListener;
    private final List<UIPersonalizationItem> mItems = new ArrayList<>();

    private static final int TITLE = 0;
    private static final int COLOR = 1;
    private static final int PERSONALIZATION = 2;
    private static final int DEFAULT_TAB = 3;
    private static final int INFORMATION = 4;
    private static final int DISPLAY_MODE = 5;
    private static final int SUBSECTION = 6;
    private static final int CHECKBOX = 7;

    PersonalizationListAdapter(PersonalizationActivity listActivity, OnPersonalizationClickListener onPersonalizationClickListener) {

        mListActivity = listActivity;
        mOnPersonalizationClickListener = onPersonalizationClickListener;
        setHasStableIds(false);
        initItems();
    }

    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

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
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        UIPersonalizationItem item = mItems.get(position);

        switch (item.getType()) {
            case TAB_SECTION:
            case DISPLAY_SECTION:
            case APPEARANCE_SECTION:
            case FONT_SECTION:
            case SOUND_VIBRATION_SECTION:
                return TITLE;

            case THEME:
                return COLOR;

            case TAB:
                return DEFAULT_TAB;

            case TAB_INFO:
                return INFORMATION;

            case DISPLAY_MODE:
                return DISPLAY_MODE;

            case CONVERSATION_APPEARANCE:
                return SUBSECTION;

            case HAPTIC_FEEDBACK:
            case SOUND_EFFECTS:
                return CHECKBOX;

            case FONT_SYSTEM:
            case FONT_SMALL:
            case FONT_MEDIUM:
            case FONT_LARGE:
                return PERSONALIZATION;

            default:
                return -1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);
        UIPersonalizationItem item = mItems.get(position);
        if (viewType == TITLE) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            boolean hideSeparator = item.getType() == UIPersonalizationItem.PersonalizationType.TAB_SECTION || item.getType() == UIPersonalizationItem.PersonalizationType.SOUND_VIBRATION_SECTION;
            sectionTitleViewHolder.onBind(item.getTitle(), hideSeparator);
        } else if (viewType == SUBSECTION) {
            SettingSectionViewHolder settingSectionViewHolder = (SettingSectionViewHolder) viewHolder;
            settingSectionViewHolder.itemView.setOnClickListener(view -> mOnPersonalizationClickListener.onUpdateConversationColor());
            settingSectionViewHolder.onBind(item.getTitle(), false);
        } else if (viewType == COLOR) {
            AppearanceColorViewHolder appearanceColorViewHolder = (AppearanceColorViewHolder) viewHolder;
            appearanceColorViewHolder.itemView.setOnClickListener(view -> mOnPersonalizationClickListener.onUpdateMainColor());
            appearanceColorViewHolder.onBind(Design.getMainStyle(), mListActivity.getString(R.string.application_theme), null, false);
        } else if (viewType == DISPLAY_MODE) {
            DisplayModeViewHolder displayModeViewHolder = (DisplayModeViewHolder) viewHolder;
            int displayMode = mListActivity.getTwinmeApplication().displayMode();
            displayModeViewHolder.onBind(displayMode);
        } else if (viewType == PERSONALIZATION) {
            PersonalizationViewHolder personalizationViewHolder = (PersonalizationViewHolder) viewHolder;
            boolean isSelected = false;
            String title = item.getTitle();
            int defaultFontSize = mListActivity.getTwinmeApplication().fontSize();
            FontSize fontSize;
            if (item.getType() == UIPersonalizationItem.PersonalizationType.FONT_SMALL) {
                fontSize = FontSize.SMALL;
                personalizationViewHolder.itemView.setOnClickListener(view -> mOnPersonalizationClickListener.onUpdateFontSize(FontSize.SMALL));
            } else if (item.getType() == UIPersonalizationItem.PersonalizationType.FONT_MEDIUM) {
                fontSize = FontSize.LARGE;
            } else if (item.getType() == UIPersonalizationItem.PersonalizationType.FONT_LARGE) {
                fontSize = FontSize.EXTRA_LARGE;
            } else {
                fontSize = FontSize.SYSTEM;
            }

            isSelected = defaultFontSize == fontSize.ordinal();
            personalizationViewHolder.itemView.setOnClickListener(view -> mOnPersonalizationClickListener.onUpdateFontSize(fontSize));
            personalizationViewHolder.onBind(title, isSelected, Design.WHITE_COLOR);
        } else if (viewType == DEFAULT_TAB) {
            DefaultTabViewHolder defaultTabViewHolder = (DefaultTabViewHolder) viewHolder;
            defaultTabViewHolder.onBind();
        } else if (viewType == INFORMATION) {
            InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
            informationViewHolder.onBind(item.getTitle(), true);
        } else if (viewType == CHECKBOX) {
            SettingSwitchViewHolder settingsViewHolder = (SettingSwitchViewHolder) viewHolder;

            String title = item.getTitle();
            String subtitle = item.getSubtitle();
            Settings.BooleanConfig config = Settings.hapticFeedbackEnable;

            if (item.getType() == UIPersonalizationItem.PersonalizationType.SOUND_EFFECTS) {
                config = Settings.soundEffectsEnable;
            }

            if (title != null) {
                UISetting<Boolean> uiSetting = new UISetting<>(UISetting.TypeSetting.CHECKBOX, title, subtitle, config);
                CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (buttonView, isChecked) -> mListActivity.onSettingChangeValue(uiSetting, isChecked);
                settingsViewHolder.onBind(uiSetting, uiSetting.getBoolean(), true, onCheckedChangeListener);
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
        } else if (viewType == COLOR) {
            convertView = inflater.inflate(R.layout.personalization_activity_appearance_color_item, parent, false);
            return new AppearanceColorViewHolder(convertView);
        } else if (viewType == SUBSECTION) {
            convertView = inflater.inflate(R.layout.settings_activity_item_section, parent, false);
            return new SettingSectionViewHolder(convertView);
        } else if (viewType == DEFAULT_TAB) {
            convertView = inflater.inflate(R.layout.personalization_activity_default_tab_item, parent, false);
            return new DefaultTabViewHolder(convertView, mListActivity);
        } else if (viewType == INFORMATION) {
            convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
            return new InformationViewHolder(convertView);
        } else if (viewType == DISPLAY_MODE) {
            convertView = inflater.inflate(R.layout.personalization_activity_mode_item, parent, false);
            DisplayModeViewHolder.Observer observer = mOnPersonalizationClickListener::onUpdateDisplayMode;
            return new DisplayModeViewHolder(convertView, observer);
        } else if (viewType == CHECKBOX) {
            convertView = inflater.inflate(R.layout.settings_activity_item_switch, parent, false);
            return new SettingSwitchViewHolder(convertView);
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
        if (position >= 0 && position < mItems.size()) {
            UIPersonalizationItem item = mItems.get(position);
            int viewType = getItemViewType(position);
            if (viewType == TITLE) {
                SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
                sectionTitleViewHolder.onBind(item.getTitle(), false);
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
        if (position >= 0 && position < mItems.size()) {
            int viewType = getItemViewType(position);
            UIPersonalizationItem item = mItems.get(position);
            if (viewType == TITLE) {
                SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
                boolean hideSeparator = item.getType() == UIPersonalizationItem.PersonalizationType.TAB_SECTION || item.getType() == UIPersonalizationItem.PersonalizationType.SOUND_VIBRATION_SECTION;
                sectionTitleViewHolder.onBind(item.getTitle(), hideSeparator);
            }
        }
    }

    private void initItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initItems");
        }

        mItems.add(new UIPersonalizationItem(UIPersonalizationItem.PersonalizationType.TAB_SECTION, mListActivity.getString(R.string.personalization_view_start_tab_title), null));
        mItems.add(new UIPersonalizationItem(UIPersonalizationItem.PersonalizationType.TAB_INFO, mListActivity.getString(R.string.personalization_view_start_tab_information), null));
        mItems.add(new UIPersonalizationItem(UIPersonalizationItem.PersonalizationType.TAB, null, null));
        mItems.add(new UIPersonalizationItem(UIPersonalizationItem.PersonalizationType.DISPLAY_SECTION, mListActivity.getString(R.string.personalization_view_mode), null));
        mItems.add(new UIPersonalizationItem(UIPersonalizationItem.PersonalizationType.DISPLAY_MODE, null, null));
        mItems.add(new UIPersonalizationItem(UIPersonalizationItem.PersonalizationType.APPEARANCE_SECTION, mListActivity.getString(R.string.application_color), null));
        mItems.add(new UIPersonalizationItem(UIPersonalizationItem.PersonalizationType.THEME, mListActivity.getString(R.string.application_theme), null));
        mItems.add(new UIPersonalizationItem(UIPersonalizationItem.PersonalizationType.CONVERSATION_APPEARANCE, mListActivity.getString(R.string.conversations_view_title), null));
        mItems.add(new UIPersonalizationItem(UIPersonalizationItem.PersonalizationType.FONT_SECTION, mListActivity.getString(R.string.personalization_view_font), null));
        mItems.add(new UIPersonalizationItem(UIPersonalizationItem.PersonalizationType.FONT_SYSTEM, mListActivity.getString(R.string.personalization_view_system), null));
        mItems.add(new UIPersonalizationItem(UIPersonalizationItem.PersonalizationType.FONT_SMALL, mListActivity.getString(R.string.personalization_view_font_small), null));
        mItems.add(new UIPersonalizationItem(UIPersonalizationItem.PersonalizationType.FONT_MEDIUM, mListActivity.getString(R.string.personalization_view_font_large), null));
        mItems.add(new UIPersonalizationItem(UIPersonalizationItem.PersonalizationType.FONT_LARGE, mListActivity.getString(R.string.personalization_view_font_extra_large), null));
        mItems.add(new UIPersonalizationItem(UIPersonalizationItem.PersonalizationType.SOUND_VIBRATION_SECTION, mListActivity.getString(R.string.settings_view_sound_vibrations_title), null));
        mItems.add(new UIPersonalizationItem(UIPersonalizationItem.PersonalizationType.HAPTIC_FEEDBACK, mListActivity.getString(R.string.personalization_view_haptic_feedback), mListActivity.getString(R.string.personalization_view_haptic_feedback_message)));
        mItems.add(new UIPersonalizationItem(UIPersonalizationItem.PersonalizationType.SOUND_EFFECTS, mListActivity.getString(R.string.settings_view_sound_effects_title), mListActivity.getString(R.string.settings_view_sound_effects_info)));
    }
}
