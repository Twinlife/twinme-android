/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.TwinmeApplication;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SoundEffect {
    private static final String LOG_TAG = "SoundEffect";
    private static final boolean DEBUG = false;
    private static final float SOUND_VOLUME = 1.0f;
    private static final int SOUND_PRIORITY = 1;
    private static final int SOUND_NO_LOOP = 0;
    private static final float SOUND_RATE = 1.0f;

    public enum SoundEffectType {
        SEND_MESSAGE,
        NEW_MESSAGE,
        DELETE_MESSAGE,
        EMOJI,
        JOIN_CALL
    }

    private static SoundPool soundPool;
    private static final Map<SoundEffectType, Integer> soundIds = new HashMap<>();
    private static final Set<SoundEffectType> loadedSounds = new HashSet<>();
    private static final Set<SoundEffectType> pendingSounds = new HashSet<>();

    private SoundEffect() {
    }

    public static synchronized void initialize() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initialize");
        }

        if (soundPool != null) {
            return;
        }

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build();

        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status != 0) {
                return;
            }

            synchronized (SoundEffect.class) {
                for (Map.Entry<SoundEffectType, Integer> entry : soundIds.entrySet()) {
                    if (entry.getValue() == sampleId) {
                        SoundEffectType soundType = entry.getKey();
                        loadedSounds.add(soundType);

                        if (pendingSounds.remove(soundType) && soundPool != null) {
                            soundPool.play(sampleId,
                                    SOUND_VOLUME,
                                    SOUND_VOLUME,
                                    SOUND_PRIORITY,
                                    SOUND_NO_LOOP,
                                    SOUND_RATE
                            );
                        }
                        break;
                    }
                }
            }
        });
    }

    public static void playSoundWithType(SoundEffectType type, Context context, TwinmeApplication twinmeApplication) {
        if (DEBUG) {
            Log.d(LOG_TAG, "playSoundWithType: type=" + type + " twinmeApplication=" + twinmeApplication);
        }

        if (context == null || twinmeApplication == null) {
            return;
        }

        if (!twinmeApplication.soundEffectsEnable()) {
            return;
        }

        int soundId = soundIdForType(type, context);
        if (soundId == 0) {
            return;
        }

        synchronized (SoundEffect.class) {
            if (!loadedSounds.contains(type)) {
                pendingSounds.add(type);
                return;
            }
        }

        soundPool.play(soundId,
                SOUND_VOLUME,
                SOUND_VOLUME,
                SOUND_PRIORITY,
                SOUND_NO_LOOP,
                SOUND_RATE
        );
    }

    public static synchronized void disposeSounds() {
        if (DEBUG) {
            Log.d(LOG_TAG, "disposeSounds");
        }

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }

        soundIds.clear();
        loadedSounds.clear();
        pendingSounds.clear();
    }

    private static synchronized int soundIdForType(SoundEffectType type, Context context) {
        if (DEBUG) {
            Log.d(LOG_TAG, "soundIdForType: type=" + type);
        }

        if (soundPool == null || context == null) {
            return 0;
        }

        Integer existingSoundId = soundIds.get(type);
        if (existingSoundId != null) {
            return existingSoundId;
        }

        int resId = resourceIdForType(type);
        if (resId == 0) {
            return 0;
        }

        int soundId = soundPool.load(context.getApplicationContext(), resId, 1);
        soundIds.put(type, soundId);

        return soundId;
    }

    private static int resourceIdForType(SoundEffectType type) {
        if (DEBUG) {
            Log.d(LOG_TAG, "resourceIdForType: type=" + type);
        }

        switch (type) {
            case SEND_MESSAGE:
                return R.raw.send_message;

            case NEW_MESSAGE:
                return R.raw.new_message;

            case DELETE_MESSAGE:
                return R.raw.delete_message;

            case EMOJI:
                return R.raw.emoji;

            case JOIN_CALL:
                return R.raw.join_call;
        }

        return 0;
    }
}
