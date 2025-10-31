/*
 *  Copyright (c) 2021-2025 twinlife SA.
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.ui.profiles.UIProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProfilesSideMenuListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "ProfilesSideMenuList...";
    private static final boolean DEBUG = false;

    protected final MainActivity mListActivity;
    protected final int mItemHeight;
    protected final List<UIProfile> mUIProfiles;

    private final OnProfileClickListener mOnProfileClickListener;

    public interface OnProfileClickListener {
        void onProfileClick(int position);

        void onEditProfileClick(int position);
    }

    private static final int TYPE_PROFILE = 0;

    public ProfilesSideMenuListAdapter(MainActivity listActivity, int itemHeight, List<UIProfile> uiProfiles, OnProfileClickListener onProfileClickListener) {

        mOnProfileClickListener = onProfileClickListener;
        mListActivity = listActivity;
        mItemHeight = itemHeight;
        mUIProfiles = uiProfiles;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mListActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.side_menu_profile_item, parent, false);
        ProfileSideMenuViewHolder uiProfileViewHolder = new ProfileSideMenuViewHolder(convertView);
        convertView.setOnClickListener(v -> {
            int position = uiProfileViewHolder.getBindingAdapterPosition();
            if (position >= 0) {
                mOnProfileClickListener.onProfileClick(position);
            }
        });

        convertView.setOnLongClickListener(v -> {
            int position = uiProfileViewHolder.getBindingAdapterPosition();
            if (position >= 0) {
                mOnProfileClickListener.onEditProfileClick(position);
            }
            return true;
        });
        return uiProfileViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == TYPE_PROFILE) {
            ProfileSideMenuViewHolder profileSideMenuViewHolder = (ProfileSideMenuViewHolder) viewHolder;
            UIProfile uiProfile = mUIProfiles.get(position);
            boolean isActiveProfile = mListActivity.getProfile() != null && mListActivity.getProfile().getId() == uiProfile.getProfile().getId();
            profileSideMenuViewHolder.onBind(uiProfile, isActiveProfile);
        }
    }

    @Override
    public int getItemCount() {

        return mUIProfiles.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        return TYPE_PROFILE;
    }

    @Override
    public long getItemId(int position) {

        if (position > 0 && position <= mUIProfiles.size()) {
            return mUIProfiles.get(position - 1).getItemId();
        }

        return -1;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

    }

    /**
     * Update the contact in the list.
     *
     * @param profile the contact to update or add.
     * @return the UI contact that was created.
     */
    public UIProfile updateUIProfile(Profile profile, Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateUIProfile: profile=" + profile);
        }

        UIProfile uiProfile = null;
        for (UIProfile lUIProfile : mUIProfiles) {
            if (lUIProfile.getProfile().getId().equals(profile.getId())) {
                uiProfile = lUIProfile;

                break;
            }
        }

        if (uiProfile != null) {
            mUIProfiles.remove(uiProfile);

            uiProfile.update(profile, avatar);
        } else {
            uiProfile = new UIProfile(profile, avatar);
        }

        // TBD Sort using id order when name are equals
        boolean added = false;
        int size = mUIProfiles.size();
        for (int i = 0; i < size; i++) {
            if (mUIProfiles.get(i).getName().compareToIgnoreCase(uiProfile.getName()) > 0) {
                mUIProfiles.add(i, uiProfile);
                added = true;
                break;
            }
        }

        if (!added) {
            mUIProfiles.add(uiProfile);
        }

        return uiProfile;
    }

    /**
     * Remove the profile from the list.
     *
     * @param profile the contact to remove.
     * @return True if the contact was removed and false if it was not found.
     */
    public boolean removeUIProfile(Profile profile) {

        return removeUIProfile(profile.getId());
    }

    public boolean removeUIProfile(UUID profileId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "removeUIProfile: profileId=" + profileId);
        }

        for (UIProfile item : mUIProfiles) {
            if (item.getProfile().getId().equals(profileId)) {
                mUIProfiles.remove(item);

                return true;
            }
        }

        return false;
    }

    /**
     * Get the list of profiles.
     *
     * @return the list of profiles.
     */
    public List<Profile> getProfiles() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getProfiles");
        }

        final List<Profile> result = new ArrayList<>();

        for (UIProfile item : mUIProfiles) {
            result.add(item.getProfile());
        }

        return result;
    }
}
