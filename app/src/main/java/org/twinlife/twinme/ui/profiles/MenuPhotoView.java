/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.profiles;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.utils.AbstractMenuSelectActionView;

public class MenuPhotoView extends AbstractMenuSelectActionView {
    private static final String LOG_TAG = "MenuPhotoView";
    private static final boolean DEBUG = false;

    public interface Observer extends AbstractMenuSelectActionViewObserver {

        void onCameraClick();

        void onPhotoGalleryClick();
    }

    private Observer mObserver;

    public MenuPhotoView(Context context) {
        super(context);
    }

    public MenuPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void startAction(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "startAction: " + position);
        }

        if (position == 0) {
            mObserver.onCameraClick();
        } else {
            mObserver.onPhotoGalleryClick();
        }
    }

    public void setObserver(Observer observer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setObserver: " + observer);
        }

        mObserver = observer;
    }

    @Override
    public AbstractMenuSelectActionViewObserver getObserver() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getObserver");
        }

        return mObserver;
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        super.initViews();

        mTitleView.setText(getResources().getString(R.string.application_profile_avatar_not_defined));
    }
}
