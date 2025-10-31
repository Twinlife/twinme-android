/*
 *  Copyright (c) 2019-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Switch;

import org.twinlife.device.android.twinme.R;

public class SwitchView extends Switch {

    public SwitchView(Context context) {

        super(context);
    }

    public SwitchView(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);

        if (checked) {
            setThumbResource(R.drawable.switch_thumb_on);
        } else {
            setThumbResource(R.drawable.switch_thumb_off);
        }
    }
}
