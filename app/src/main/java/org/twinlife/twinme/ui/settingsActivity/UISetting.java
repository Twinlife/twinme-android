/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import androidx.annotation.NonNull;

import org.twinlife.twinme.ui.Settings;

public class UISetting<T> {

    public enum TypeSetting {
        CHECKBOX,
        DIRECTORY,
        RINGTONE,
        VALUE,
        RESET,
        SYSTEM,
        SYSTEM_MESSAGE
    }

    private final TypeSetting mTypeSetting;
    private final String mTitle;
    private final Settings.Config<T> mSetting;

    public UISetting(@NonNull TypeSetting typeSetting, @NonNull String title, @NonNull Settings.Config<T> setting) {

        mTypeSetting = typeSetting;
        mTitle = title;
        mSetting = setting;
    }

    UISetting(@NonNull TypeSetting typeSetting, @NonNull String title) {

        mTypeSetting = typeSetting;
        mTitle = title;
        mSetting = null;
    }

    public boolean isSetting(@NonNull Settings.StringConfig setting) {

        return mSetting == setting;
    }

    public boolean isSetting(@NonNull Settings.IntConfig setting) {

        return mSetting == setting;
    }

    public String getTitle() {

        return mTitle;
    }

    public TypeSetting getTypeSetting() {

        return mTypeSetting;
    }

    public void setString(String value) {

        if (mSetting instanceof Settings.StringConfig) {
            ((Settings.StringConfig) mSetting).setString(value).save();
        }
    }

    public void setBoolean(boolean value) {

        if (mSetting instanceof Settings.BooleanConfig) {
            ((Settings.BooleanConfig) mSetting).setBoolean(value).save();
        }
    }

    public String getString() {

        if (mSetting instanceof Settings.StringConfig) {
            return ((Settings.StringConfig) mSetting).getString();
        } else {
            return "";
        }
    }

    public boolean getBoolean() {

        if (mSetting instanceof Settings.BooleanConfig) {
            return ((Settings.BooleanConfig) mSetting).getBoolean();
        } else {
            return false;
        }
    }

    public Integer getInteger() {

        if (mSetting instanceof Settings.IntConfig) {
            return ((Settings.IntConfig) mSetting).getInt();
        } else {
            return 0;
        }
    }

    @Override
    @NonNull
    public String toString() {

        return "UISetting\n" +
                " title: " +
                mTitle +
                "\n" +
                " key: " +
                mSetting +
                "\n" +
                " type: " +
                mTypeSetting +
                "\n";
    }
}
