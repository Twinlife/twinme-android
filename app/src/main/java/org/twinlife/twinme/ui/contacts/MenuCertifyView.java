/*
 *  Copyright (c) 2024-2025 twinlife SA.
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

public class MenuCertifyView extends AbstractMenuSelectActionView {
    private static final String LOG_TAG = "MenuCertifyView";
    private static final boolean DEBUG = false;

    public interface Observer extends AbstractMenuSelectActionViewObserver {

        void onStartCertifyByScan();

        void onStartCertifyByVideoCall();
    }

    private Observer mObserver;

    public MenuCertifyView(Context context) {
        super(context);
    }

    public MenuCertifyView(Context context, AttributeSet attrs) {
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
            mObserver.onStartCertifyByScan();
        } else {
            mObserver.onStartCertifyByVideoCall();
        }
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        super.initViews();

        mTitleView.setText(getResources().getString(R.string.authentified_relation_activity_to_be_certified_title));
    }
}
