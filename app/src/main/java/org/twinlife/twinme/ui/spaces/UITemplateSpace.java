/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.content.Context;

import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.R;

public class UITemplateSpace {

    public enum TemplateType {
        BUSINESS_1,
        BUSINESS_2,
        FAMILY_1,
        FAMILY_2,
        FRIENDS_1,
        FRIENDS_2,
        OTHER
    }

    private final TemplateType mTemplateType;

    private static int sItemId = 0;

    private final long mItemId;
    @Nullable
    private String mSpace;
    private String mProfile;
    private String mProfilePlaceholder;
    private int mAvatarId;
    private String mAvatarUrl;
    private String mColor;

    public UITemplateSpace(Context context, TemplateType templateType) {

        mItemId = sItemId++;

        mTemplateType = templateType;
        initTemplateInformation(context);
    }

    public long getItemId() {

        return mItemId;
    }

    public TemplateType getTemplateType() {

        return mTemplateType;
    }

    public String getSpace() {

        return mSpace;
    }

    public String getProfile() {

        return mProfile;
    }

    public String getProfilePlaceholder() {

        return mProfilePlaceholder;
    }

    public int getAvatarId() {

        return mAvatarId;
    }

    public String getAvatarUrl() {

        return mAvatarUrl;
    }

    public String getColor() {

        return mColor;
    }

    private void initTemplateInformation(Context context) {

        switch (mTemplateType) {
            case BUSINESS_1:
                mSpace = context.getString(R.string.spaces_activity_sample_business);
                mProfile = context.getString(R.string.spaces_activity_sample_business_name);
                mProfilePlaceholder = context.getString(R.string.template_space_activity_template_business_placeholder);
                mAvatarId = R.drawable.space_sample_business_2;
                mAvatarUrl = "https://skred.mobi/download/space_sample_business_2.jpg";
                mColor = "#4B90E2";
                break;

            case BUSINESS_2:
                mSpace = context.getString(R.string.spaces_activity_sample_business);
                mProfile = context.getString(R.string.template_space_activity_template_business_profile);
                mProfilePlaceholder = context.getString(R.string.template_space_activity_template_business_placeholder);
                mAvatarId = R.drawable.space_sample_business_3;
                mAvatarUrl = "https://skred.mobi/download/space_sample_business_3.jpg";
                mColor = "#9DDBED";
                break;

            case FAMILY_1:
                mSpace = context.getString(R.string.spaces_activity_sample_family);
                mProfile = context.getString(R.string.spaces_activity_sample_family_name);
                mProfilePlaceholder = context.getString(R.string.template_space_activity_template_family_placeholder);
                mAvatarId = R.drawable.space_sample_family_2;
                mAvatarUrl = "https://skred.mobi/download/space_sample_family_2.jpg";
                mColor = "#89AC8F";
                break;

            case FAMILY_2:
                mSpace = context.getString(R.string.spaces_activity_sample_family);
                mProfile = context.getString(R.string.template_space_activity_template_family_profile);
                mProfilePlaceholder = context.getString(R.string.template_space_activity_template_family_placeholder);
                mAvatarId = R.drawable.space_sample_family_3;
                mAvatarUrl = "https://skred.mobi/download/space_sample_family_3.jpg";
                mColor = "#E99616";
                break;

            case FRIENDS_1:
                mSpace = context.getString(R.string.spaces_activity_sample_friends);
                mProfile = context.getString(R.string.spaces_activity_sample_friends_name);
                mProfilePlaceholder = context.getString(R.string.template_space_activity_template_friends_placeholder);
                mAvatarId = R.drawable.space_sample_friends_2;
                mAvatarUrl = "https://skred.mobi/download/space_sample_friends_2.jpg";
                mColor = "#F0CB26";
                break;

            case FRIENDS_2:
                mSpace = context.getString(R.string.spaces_activity_sample_friends);
                mProfile = context.getString(R.string.template_space_activity_template_friends_profile);
                mProfilePlaceholder = context.getString(R.string.template_space_activity_template_friends_placeholder);
                mAvatarId = R.drawable.space_sample_friends_3;
                mAvatarUrl = "https://skred.mobi/download/space_sample_friends_3.jpg";
                mColor = "#EBBDBF";
                break;

            case OTHER:
                mSpace = context.getString(R.string.template_space_activity_template_other);
                mProfile = null;
                mProfilePlaceholder = context.getString(R.string.template_space_activity_template_other_placeholder);
                mAvatarId = -1;
                mAvatarUrl = null;
                mColor = null;
                break;

            default:
                break;
        }
    }
}
