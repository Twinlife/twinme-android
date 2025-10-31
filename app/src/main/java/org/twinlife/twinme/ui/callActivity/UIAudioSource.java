/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.content.Context;

import androidx.annotation.NonNull;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.audio.AudioDevice;

public class UIAudioSource {

    @NonNull
    private String mName;

    private int mIcon;

    @NonNull
    private final AudioDevice mAudioDevice;

    private boolean mSelected = false;

    public UIAudioSource(Context context, @NonNull AudioDevice audioDevice) {

        mAudioDevice = audioDevice;
        initDefaultAttribute(context);
    }

    public AudioDevice getAudioDevice() {

        return mAudioDevice;
    }

    public String getName() {

        return mName;
    }

    public void setName(String name) {

        mName = name;
    }

    public int getIcon() {

        return mIcon;
    }

    public boolean isSelected() {

        return mSelected;
    }

    public void setIsSelected(boolean selected) {

        mSelected = selected;
    }

    private void initDefaultAttribute(Context context) {

        switch (mAudioDevice) {
            case SPEAKER_PHONE:
                mName = context.getString(R.string.call_activity_audio_source_loudspeaker);
                mIcon = R.drawable.loud_speaker_action_call_on;
                break;

            case WIRED_HEADSET:
                mName = context.getString(R.string.call_activity_audio_source_headset);
                mIcon = R.drawable.audio_headphone_icon;
                break;

            case EARPIECE:
                mName =context.getString(R.string.call_activity_audio_source_device);
                mIcon = R.drawable.audio_phonespeaker_icon;
                break;

            case BLUETOOTH:
                mName = context.getString(R.string.call_activity_audio_source_bluetooth);
                mIcon = R.drawable.audio_bluetooth_icon;
                break;

            case NONE:
                mName = context.getString(R.string.call_activity_audio_source_earphones);
                mIcon = R.drawable.audio_phonespeaker_icon;
                break;
        }
    }
}
