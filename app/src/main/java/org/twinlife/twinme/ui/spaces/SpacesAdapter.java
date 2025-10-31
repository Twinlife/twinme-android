/*
 *  Copyright (c) 2019-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.models.SpaceSettings;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpacesAdapter extends RecyclerView.Adapter<SpaceViewHolder> {
    private static final String LOG_TAG = "SpacesAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    protected final AbstractTwinmeActivity mListActivity;
    protected final int mItemHeight;
    protected final List<UISpace> mUISpaces;

    @NonNull
    private final OnSpaceClickListener mOnSpaceClickListener;
    @Nullable
    private final OnSpaceLongClickListener mOnSpaceLongClickListener;

    public interface OnSpaceClickListener {
        void onSpaceClick(int position);
    }

    public interface OnSpaceLongClickListener {
        void onSpaceLongClick(int position);
    }

    public SpacesAdapter(@NonNull AbstractTwinmeActivity listActivity, int itemHeight, @NonNull List<UISpace> uiSpaces,
                         @NonNull OnSpaceClickListener onSpaceClickListener, @Nullable OnSpaceLongClickListener onSpaceLongClickListener) {

        mOnSpaceClickListener = onSpaceClickListener;
        mOnSpaceLongClickListener = onSpaceLongClickListener;
        mListActivity = listActivity;
        mItemHeight = itemHeight;
        mUISpaces = uiSpaces;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public SpaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mListActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.spaces_activity_space_item, parent, false);
        SpaceViewHolder uiSpaceViewHolder = new SpaceViewHolder(convertView);

        convertView.setOnClickListener(v -> {
            int position = uiSpaceViewHolder.getBindingAdapterPosition();
            if (position >= 0) {
                mOnSpaceClickListener.onSpaceClick(position);
            }
        });

        convertView.setOnLongClickListener(v -> {
            int position = uiSpaceViewHolder.getBindingAdapterPosition();
            if (position >= 0 && mOnSpaceLongClickListener != null) {
                mOnSpaceLongClickListener.onSpaceLongClick(position);
            }
            return true;
        });

        return uiSpaceViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SpaceViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        boolean hideSeparator = position + 1 == mUISpaces.size();
        viewHolder.onBind(mUISpaces.get(position), hideSeparator);
    }

    @Override
    public int getItemCount() {

        return mUISpaces.size();
    }

    @Override
    public long getItemId(int position) {

        return mUISpaces.get(position).getItemId();
    }

    @Override
    public void onViewRecycled(@NonNull SpaceViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        viewHolder.onViewRecycled();
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

        boolean added = false;
        String spaceName = uiSpace.getNameSpace();
        if (spaceName !=  null) {

            // TBD Sort using id order when name are equals
            int size = mUISpaces.size();
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
    public boolean removeUISpace(@NonNull Space space) {

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
    @NonNull
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
