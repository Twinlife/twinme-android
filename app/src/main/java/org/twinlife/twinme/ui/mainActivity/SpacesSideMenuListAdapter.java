/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.models.SpaceSettings;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.spaces.SpaceSettingProperty;
import org.twinlife.twinme.ui.spaces.UISpace;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpacesSideMenuListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "SpacesSideMenuListAd...";
    private static final boolean DEBUG = false;

    private static final float DESIGN_SPACE_ITEM_HEIGHT = 160f;

    protected final MainActivity mListActivity;
    protected final int mItemHeight;
    protected List<UISpace> mUISpaces;

    private final OnSpaceClickListener mOnSpaceClickListener;

    public interface OnSpaceClickListener {
        void onSpaceClick(int position);

        void onSecretSpaceClick();

        void onSpaceLongClick(int position);
    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {

        FooterViewHolder(@NonNull View view) {
            super(view);
        }

        public void onBind(int height) {

            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            layoutParams.height = height;
            itemView.setLayoutParams(layoutParams);
        }

        public void onViewRecycled() {

        }
    }

    private static class SearchViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mSearchImageView;
        SearchViewHolder(@NonNull View view) {
            super(view);

            mSearchImageView = view.findViewById(R.id.side_menu_space_search_item_image_view);
        }

        public void onBind() {

            mSearchImageView.setColorFilter(Design.BLACK_COLOR);
        }

        public void onViewRecycled() {

        }
    }

    private static final int TYPE_SPACE = 0;
    private static final int TYPE_SEARCH_SPACE = 1;
    private static final int TYPE_FOOTER = 2;

    public SpacesSideMenuListAdapter(MainActivity listActivity, int itemHeight, List<UISpace> uiSpaces, OnSpaceClickListener onSpaceClickListener) {

        mOnSpaceClickListener = onSpaceClickListener;
        mListActivity = listActivity;
        mItemHeight = itemHeight;
        mUISpaces = uiSpaces;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        if (viewType == TYPE_SEARCH_SPACE) {
            LayoutInflater inflater = mListActivity.getLayoutInflater();
            View convertView = inflater.inflate(R.layout.side_menu_space_search_item, parent, false);
            SearchViewHolder searchViewHolder = new SearchViewHolder(convertView);
            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
            layoutParams.height = (int) (DESIGN_SPACE_ITEM_HEIGHT * Design.HEIGHT_RATIO);
            convertView.setLayoutParams(layoutParams);
            convertView.setOnClickListener(v -> mOnSpaceClickListener.onSecretSpaceClick());
            return searchViewHolder;
        } else if (viewType == TYPE_FOOTER) {
            LayoutInflater inflater = mListActivity.getLayoutInflater();
            View convertView = inflater.inflate(R.layout.side_menu_space_footer_item, parent, false);
            FooterViewHolder footerViewHolder = new FooterViewHolder(convertView);

            int footerHeight = (int) (mListActivity.getSpacesListHeight() - (mUISpaces.size() * (DESIGN_SPACE_ITEM_HEIGHT * Design.HEIGHT_RATIO)));
            if (footerHeight < 0) {
                footerHeight = 0;
            }
            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
            layoutParams.height = footerHeight;
            convertView.setLayoutParams(layoutParams);
            return footerViewHolder;
        } else {
            LayoutInflater inflater = mListActivity.getLayoutInflater();
            View convertView = inflater.inflate(R.layout.side_menu_space_item, parent, false);
            SpaceSideMenuViewHolder uiSpaceViewHolder = new SpaceSideMenuViewHolder(convertView);
            convertView.setOnClickListener(v -> {
                int position = uiSpaceViewHolder.getBindingAdapterPosition();
                if (position > 0 && viewType == TYPE_SPACE) {
                    mOnSpaceClickListener.onSpaceClick(position - 1);
                }
            });
            convertView.setOnLongClickListener(v -> {
                int position = uiSpaceViewHolder.getBindingAdapterPosition();
                if (position > 0 && viewType == TYPE_SPACE) {
                    mOnSpaceClickListener.onSpaceLongClick(position - 1);
                }
                return true;
            });
            return uiSpaceViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == TYPE_FOOTER) {
            int footerHeight = (int) (mListActivity.getSpacesListHeight() - (mUISpaces.size() * (DESIGN_SPACE_ITEM_HEIGHT * Design.HEIGHT_RATIO)));
            if (footerHeight < 0) {
                footerHeight = 0;
            }
            FooterViewHolder footerViewHolder = (FooterViewHolder) viewHolder;
            footerViewHolder.onBind(footerHeight);
        } else if (viewType == TYPE_SPACE) {
            SpaceSideMenuViewHolder spaceSideMenuViewHolder = (SpaceSideMenuViewHolder) viewHolder;
            spaceSideMenuViewHolder.onBind(mUISpaces.get(position - 1));
        } else if (viewType == TYPE_SEARCH_SPACE) {
            SearchViewHolder searchViewHolder = (SearchViewHolder) viewHolder;
            searchViewHolder.onBind();
        }
    }

    @Override
    public int getItemCount() {

        return mUISpaces.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position == 0) {
            return TYPE_SEARCH_SPACE;
        } else if (position == mUISpaces.size() + 1) {
            return TYPE_FOOTER;
        }

        return TYPE_SPACE;
    }

    @Override
    public long getItemId(int position) {

        if (position > 0 && position <= mUISpaces.size()) {
            return mUISpaces.get(position - 1).getItemId();
        }

        return -1;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

    }

    public List<UISpace> getUISpaces() {
        return mUISpaces;
    }

    /**
     * Update the contact in the list.
     *
     * @param space the contact to update or add.
     * @return the UI space that was created.
     */
    public UISpace updateUISpace(Space space, Bitmap avatar, Bitmap profileAvatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateUISpace: space=" + space);
        }

        UISpace uiSpace = null;
        for (UISpace lUISpace : mUISpaces) {
            if (lUISpace.getSpace().getId().equals(space.getId())) {
                uiSpace = lUISpace;

                break;
            }
        }

        SpaceSettings spaceSettings = space.getSpaceSettings();
        if (space.getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_APPEARANCE_SETTINGS, true)) {
            spaceSettings = mListActivity.getTwinmeContext().getDefaultSpaceSettings();
        }

        if (uiSpace != null) {
            mUISpaces.remove(uiSpace);
            uiSpace.update(mListActivity.getTwinmeApplication(), space, avatar, profileAvatar, spaceSettings);
        } else {
            uiSpace = new UISpace(mListActivity.getTwinmeApplication(), space, avatar, profileAvatar, spaceSettings);
        }

        // TBD Sort using id order when name are equals
        boolean added = false;
        int size = mUISpaces.size();
        final String spaceName = uiSpace.getNameSpace();
        if (spaceName != null) {
            for (int i = 0; i < size; i++) {
                String name = mUISpaces.get(i).getNameSpace();
                if (name != null && name.compareToIgnoreCase(spaceName) > 0) {
                    mUISpaces.add(i, uiSpace);
                    added = true;
                    break;
                }
            }
        }

        if (!added) {
            mUISpaces.add(uiSpace);
        }
        return uiSpace;
    }

    /**
     * Remove the contact from the list.
     *
     * @param space the contact to remove.
     * @return True if the contact was removed and false if it was not found.
     */
    public boolean removeUISpace(Space space) {

        return removeUISpace(space.getId());
    }

    public boolean removeUISpace(UUID spaceId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "removeUISpace: spaceId=" + spaceId);
        }

        for (UISpace item : mUISpaces) {
            if (item.getSpace().getId().equals(spaceId)) {
                mUISpaces.remove(item);
                return true;
            }
        }
        return false;
    }

    /**
     * Get the list of spaces.
     *
     * @return the list of spaces.
     */
    public List<Space> getSpaces() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getSpaces");
        }

        final List<Space> result = new ArrayList<>();

        for (UISpace item : mUISpaces) {
            result.add(item.getSpace());
        }
        return result;
    }
}
