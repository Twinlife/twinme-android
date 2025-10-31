/*
 *  Copyright (c) 2019-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.ui.TwinmeActivity;

import java.util.UUID;

class TabbarFragment extends Fragment {

    @Nullable
    MainActivity mTwinmeActivity;

    //
    // Override Fragment methods
    //

    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);

        if (context instanceof MainActivity) {
            mTwinmeActivity = (MainActivity) context;
        }
    }

    @Override
    public void startActivity(@NonNull Intent intent) {

        // If we are detached, there is no activity, make sure to use the activity startActivity to handle errors.
        if (mTwinmeActivity != null) {
            mTwinmeActivity.startActivity(intent, null);
        }
    }

    public void startActivity(@NonNull Intent intent, @NonNull Class<?> clazz) {

        // If we are detached, there is no activity, make sure to use the activity startActivity to handle errors.
        if (mTwinmeActivity != null) {
            mTwinmeActivity.startActivity(clazz, intent);
        }
    }

    public void startActivity(@NonNull Class<?> clazz, @NonNull String name, @NonNull UUID param) {

        if (mTwinmeActivity != null) {
            mTwinmeActivity.startActivity(clazz, name, param);
        }
    }

    public void showContactActivity(@NonNull Originator subject) {

        // If we are detached, there is no activity, make sure to use the activity startActivity to handle errors.
        if (mTwinmeActivity != null) {
            mTwinmeActivity.showContactActivity(subject);
        }
    }

    public void onRequestPermissions(@NonNull TwinmeActivity.Permission[] grantedPermissions) {

    }

    public void setEnabled(@Nullable MenuItem menuItem, boolean enabled) {

        if (menuItem != null) {
            View actionView = menuItem.getActionView();
            if (actionView != null) {
                actionView.setAlpha(enabled ? 1.0f : 0.5f);
            }
            menuItem.setEnabled(enabled);
        }
    }
}
