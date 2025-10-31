/*
 *  Copyright (c) 2022-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.utils.update;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.ui.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class LastVersionImpl implements Serializable, LastVersion {

    private static final String SAVE_NAME = "lastVersion.dat";

    private static final long CHECK_DELAY = 24 * 3600 * 1000L; // Check for new version at most each 24 hour.

    @Nullable
    private String mVersionNumber;
    @Nullable
    private int[] mVersion;
    private int mMinSupportedSDK;
    @Nullable
    private List<String> mMinorChanges;
    @Nullable
    private List<String> mMajorChanges;
    @Nullable
    private List<String> mImages;
    @Nullable
    private List<String> mImagesDark;
    private Date mLastCheckDate;

    private static int[] mLastInformedVersion;

    /**
     * Check if the version is known.
     *
     * @return true if the version is known, false if we failed or don't know it yet.
     */
    public boolean isValid() {

        return mVersion != null;
    }

    /**
     * Check if the information concerns the current version.
     *
     * @return true if the information is for the current version.
     */
    public boolean isCurrentVersion() {

        int[] currentVersion = parseVersion(BuildConfig.VERSION_NAME);
        return Arrays.equals(currentVersion, mVersion);
    }

    /**
     * Returns true if a major version is available.
     *
     * @return true if a major version is available.
     */
    public boolean isMajorVersion() {

        // This version is not for us: ignore it.
        if (mMinSupportedSDK > Build.VERSION.SDK_INT) {

            return false;
        }

        if (mVersion == null) {

            return false;
        }

        int[] currentVersion = parseVersion(BuildConfig.VERSION_NAME);
        for (int i = 0; i < currentVersion.length - 1; i++) {
            if (mVersion.length <= i) {
                return false;
            }
            if (mVersion[i] > currentVersion[i]) {
                return true;
            }
            if (mVersion[i] < currentVersion[i]) {
                return false;
            }
        }

        return false;
    }

    /**
     * Returns true if a minor version is available.
     *
     * @return true if a minor version is available.
     */
    public boolean isMinorVersion() {

        // This version is not for us: ignore it.
        if (mMinSupportedSDK > Build.VERSION.SDK_INT) {

            return false;
        }

        if (mVersion == null) {

            return false;
        }

        int[] currentVersion = parseVersion(BuildConfig.VERSION_NAME);
        for (int i = 0; i < currentVersion.length - 1; i++) {
            if (mVersion.length <= i) {
                return false;
            }
            if (mVersion[i] > currentVersion[i]) {
                return false;
            }
            if (mVersion[i] < currentVersion[i]) {
                return false;
            }
        }

        return currentVersion.length == mVersion.length && currentVersion[currentVersion.length - 1] < mVersion[currentVersion.length - 1];
    }

    public boolean hasNewVersion() {

        return isMajorVersion() || isMinorVersion();
    }

    /**
     * Returns true if we must check again for a new version.
     *
     * @return true if we must check if a new version is now available.
     */
    public boolean needUpdate() {

        long now = System.currentTimeMillis();
        return mLastCheckDate == null || mLastCheckDate.getTime() + CHECK_DELAY < now;
    }

    public boolean isMajorVersionWithUpdate(boolean update) {

        if (mVersion == null) {
            return false;
        }

        int[] version;
        if (!update) {
            version = mLastInformedVersion;
            Settings.lastInformedVersion.setString(mVersionNumber).save();
            mLastInformedVersion = mVersion;
        } else {
            version = parseVersion(BuildConfig.VERSION_NAME);
        }

        // A 13.0.0 and a 12.6.0 are considered a major version compared to a 12.5.3.
        if (version.length > 1 && mVersion.length > 1) {
            if (version[0] < mVersion[0]) {
                return true;
            } else if (version[0] > mVersion[0]) {
                return false;
            }

            if (version[1] < mVersion[1]) {
                return true;
            } else if (version[1] > mVersion[1]) {
                return false;
            }
        }

        // Both majorVersion and minorVersion are equal: this is a minor version update.
        return false;
    }

    public boolean isVersionUpdated() {

        if (mVersion == null) {
            return false;
        }

        int[] currentVersion = parseVersion(BuildConfig.VERSION_NAME);

        for (int i = 0; i < currentVersion.length - 1; i++) {
            if (mVersion.length <= i) {
                return false;
            }
            if (mVersion[i] != currentVersion[i]) {
                return false;
            }
        }

        for (int i = 0; i < mLastInformedVersion.length - 1; i++) {
            if (mVersion.length <= i) {
                return true;
            }
            if (mVersion[i] != mLastInformedVersion[i]) {
                return true;
            }
        }

        return false;
    }

    public String getVersionNumber() {

        return mVersionNumber;
    }

    public void setVersionNumber(String versionNumber) {

        mVersionNumber = versionNumber;
        mVersion = parseVersion(versionNumber);
    }

    public void setMinSupportedSDK(String minSupportedSDK) {

        try {
            mMinSupportedSDK = Integer.parseInt(minSupportedSDK);

        } catch (NumberFormatException ex) {

            mMinSupportedSDK = 0;
        }
    }

    public List<String> getImages() {

        return mImages;
    }

    public void setImages(List<String> images) {

        mImages = images;
    }

    public List<String> getImagesDark() {

        return mImagesDark;
    }

    public void setImagesDark(List<String> images) {

        mImagesDark = images;
    }

    public void setMinorChanges(List<String> minorChanges) {

        mMinorChanges = minorChanges;
    }

    public String getMinorChanges() {

        if (mMinorChanges == null) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String change : mMinorChanges) {
            if (!stringBuilder.toString().isEmpty()) {
                stringBuilder.append("\n");
            }
            stringBuilder.append(change);
        }
        return stringBuilder.toString();
    }

    @Override
    public List<String> getListMinorChanges() {

        return mMinorChanges;
    }

    public void setMajorChanges(List<String> majorChanges) {

        mMajorChanges = majorChanges;
    }

    public String getMajorChanges() {

        if (mMajorChanges == null) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String change : mMajorChanges) {
            if (!stringBuilder.toString().isEmpty()) {
                stringBuilder.append("\n");
            }
            stringBuilder.append(change);
        }
        return stringBuilder.toString();
    }

    @Override
    public List<String> getListMajorChanges() {

        return mMajorChanges;
    }

    @NonNull
    public static LastVersion load(@NonNull Context context) {

        String lastInformedVersion = Settings.lastInformedVersion.getString();
        if (lastInformedVersion != null) {
            mLastInformedVersion = parseVersion(lastInformedVersion);
        } else {
            mLastInformedVersion = new int[]{0,0,0};
        }

        LastVersion lastVersion;
        File file = new File(context.getCacheDir(), SAVE_NAME);
        try (FileInputStream fis = new FileInputStream(file)) {
            try (ObjectInputStream is = new ObjectInputStream(fis)) {
                lastVersion = (LastVersion) is.readObject();
            }
        } catch (Exception exception) {
            lastVersion = new LastVersionImpl();
            lastVersion.setVersionNumber("1.0.0");
            Utils.deleteFile("", file);
        }

        return lastVersion;
    }

    public void save(@NonNull Context context) {

        File file = new File(context.getCacheDir(), SAVE_NAME);

        mLastCheckDate = new Date();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(this);

        } catch (Exception ignored) {

        }
    }

    @NonNull
    private static int[] parseVersion(@NonNull String version) {

        String[] numbers = version.split("\\.");
        int[] result = new int[numbers.length];
        for (int i = 0; i < result.length; i++) {
            try {
                result[i] = Integer.parseInt(numbers[i]);
            } catch (NumberFormatException exception) {
                result[i] = 0;
            }
        }

        return result;
    }
}
