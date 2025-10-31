/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.recyclerview.widget.RecyclerView;

public class ItemRecyclerView extends RecyclerView {

    private boolean mIsScrollEnable = true;

    public ItemRecyclerView(Context context) {
        super(context);
    }

    public ItemRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ItemRecyclerView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
    }

    public void setScrollEnable(boolean scrollEnable) {

        mIsScrollEnable = scrollEnable;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        //Ignore scroll events.
        if (ev.getAction() == MotionEvent.ACTION_MOVE && !mIsScrollEnable)
            return true;

        //Dispatch event for non-scroll actions, namely clicks!
        return super.dispatchTouchEvent(ev);
    }
}