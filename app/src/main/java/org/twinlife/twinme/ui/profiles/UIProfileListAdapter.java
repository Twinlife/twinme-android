/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Auguste Hatton (Auguste.Hatton@twin.life)
 */

package org.twinlife.twinme.ui.profiles;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.ui.mainActivity.MainActivity;
import org.twinlife.twinme.ui.mainActivity.ProfileFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UIProfileListAdapter extends RecyclerView.Adapter<UIProfileViewHolder> {
    private static final String LOG_TAG = "UIProfileListAdapter";
    private static final boolean DEBUG = false;

    private final ProfileFragment mProfileFragment;
    private final List<UIProfile> mUIProfiles;
    private final OnProfileClickListener mOnProfileClickListener;
    private final OnProfileLongClickListener mOnProfileLongClickListener;

    public interface OnProfileClickListener {
        void onProfileClick(int position);
    }

    public interface OnProfileLongClickListener {
        void onProfileLongClick(int position);
    }

    public UIProfileListAdapter(ProfileFragment profileFragment, List<UIProfile> uiProfiles, OnProfileClickListener onProfileClickListener, OnProfileLongClickListener onProfileLongClickListener) {

        mOnProfileClickListener = onProfileClickListener;
        mOnProfileLongClickListener = onProfileLongClickListener;
        mProfileFragment = profileFragment;
        mUIProfiles = uiProfiles;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public UIProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mProfileFragment.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.profile_fragment_profile_item, parent, false);
        UIProfileViewHolder uiProfileViewHolder = new UIProfileViewHolder(convertView);
        convertView.setOnClickListener(v -> {
            int position = uiProfileViewHolder.getBindingAdapterPosition();
            if (position >= 0) {
                mOnProfileClickListener.onProfileClick(position);
            }
        });
        convertView.setOnLongClickListener(v -> {
            int position = uiProfileViewHolder.getBindingAdapterPosition();
            if (position >= 0) {
                mOnProfileLongClickListener.onProfileLongClick(position);
            }
            return false;
        });

        return uiProfileViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UIProfileViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        MainActivity mainActivity = (MainActivity) mProfileFragment.getActivity();
        UIProfile uiProfile = mUIProfiles.get(position);

        boolean isActiveProfile = false;

        if (mainActivity != null && mainActivity.getProfile() != null && mainActivity.getProfile().getId() == uiProfile.getProfile().getId()) {
            isActiveProfile = true;
        }

        boolean hideSeparator = position + 1 == mUIProfiles.size();
        viewHolder.onBind(mProfileFragment.getContext(), uiProfile, isActiveProfile, hideSeparator);
    }

    @Override
    public int getItemCount() {

        return mUIProfiles.size();
    }

    @Override
    public long getItemId(int position) {

        return mUIProfiles.get(position).getItemId();
    }

    @Override
    public void onViewRecycled(@NonNull UIProfileViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        viewHolder.onViewRecycled();
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
