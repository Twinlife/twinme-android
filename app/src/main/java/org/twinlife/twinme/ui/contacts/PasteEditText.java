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

public class PasteEditText extends androidx.appcompat.widget.AppCompatEditText {

    public interface PasteObserver {
        void onPaste();
    }

    private PasteObserver mObserver;

    public PasteEditText(Context context) {
        super(context);
    }

    public PasteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PasteEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPasteObserver(PasteObserver observer){

        mObserver = observer;
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        super.onTextContextMenuItem(id);

        if (id == android.R.id.paste && mObserver != null) {
            mObserver.onPaste();
        }

        return true;
    }
}