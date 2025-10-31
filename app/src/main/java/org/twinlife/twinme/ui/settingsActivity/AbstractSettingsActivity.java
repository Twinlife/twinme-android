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

import androidx.annotation.NonNull;

import org.twinlife.twinme.ui.AbstractTwinmeActivity;

public abstract class AbstractSettingsActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "AbstractSettingsActi..";
    private static final boolean DEBUG = false;

    public abstract void onSettingClick(UISetting<?> setting);

    public abstract void onRingToneClick(UISetting<String> setting);

    public void onSettingChangeValue(@NonNull UISetting<Boolean> setting, boolean value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingChangeValue: setting=" + setting + " value=" + value);
        }

        if (setting.getTypeSetting() == UISetting.TypeSetting.CHECKBOX) {
            setting.setBoolean(value);
        }
    }

    public void onEditSetting(@NonNull UISetting<String> setting, String value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditSetting: setting=" + setting + " value=" + value);
        }

        if (setting.getTypeSetting() == UISetting.TypeSetting.DIRECTORY) {
            setting.setString(value);
        }
    }
}
