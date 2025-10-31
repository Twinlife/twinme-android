/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.utils.AbstractMenuSelectActionView;

public class MenuAddContactView  extends AbstractMenuSelectActionView {
    private static final String LOG_TAG = "MenuAddContactView";
    private static final boolean DEBUG = false;

    public interface Observer extends AbstractMenuSelectActionViewObserver {

        void onStartAddContactByScan();

        void onStartAddContactByInvite();
    }

    private Observer mObserver;

    public MenuAddContactView(Context context) {
        super(context);
    }

    public MenuAddContactView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
    public void startAction(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "startAction: " + position);
        }

        if (position == 0) {
            mObserver.onStartAddContactByScan();
        } else {
            mObserver.onStartAddContactByInvite();
        }
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        super.initViews();

        mTitleView.setText(getResources().getString(R.string.main_activity_add_contact));
    }
}
