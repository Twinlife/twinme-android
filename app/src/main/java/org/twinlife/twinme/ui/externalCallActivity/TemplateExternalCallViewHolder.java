/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CircularImageView;

public class TemplateExternalCallViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "TemplateExternalCal...";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 124;
    private static final int ITEM_VIEW_HEIGHT;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private final CircularImageView mAvatarView;
    private final TextView mNoAvatarTextView;
    private final TextView mNameView;
    private final View mSeparatorView;
    private final View mNoAvatarView;

    TemplateExternalCallViewHolder(@NonNull View view) {

        super(view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ITEM_VIEW_HEIGHT;
        view.setLayoutParams(layoutParams);

        mAvatarView = view.findViewById(R.id.template_external_call_activity_item_avatar_view);

        mNameView = view.findViewById(R.id.template_external_call_activity_item_name_view);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM34);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mNoAvatarTextView = view.findViewById(R.id.template_external_call_activity_item_no_avatar_text_view);
        Design.updateTextFont(mNoAvatarTextView, Design.FONT_BOLD44);
        mNoAvatarTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mNoAvatarView = view.findViewById(R.id.template_external_call_activity_item_no_avatar_view);

        GradientDrawable noAvatarGradientDrawable = new GradientDrawable();
        noAvatarGradientDrawable.mutate();
        noAvatarGradientDrawable.setShape(GradientDrawable.OVAL);
        noAvatarGradientDrawable.setCornerRadii(new float[]{0, 0, 0, 0, 0, 0, 0, 0});
        noAvatarGradientDrawable.setColor(Design.BACKGROUND_COLOR_GREY);
        ViewCompat.setBackground(mNoAvatarView, noAvatarGradientDrawable);

        mSeparatorView = view.findViewById(R.id.template_external_call_activity_item_current_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    public void onBind(UITemplateExternalCall uiTemplateExternalCall, boolean hideSeparator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind: uiTemplateExternalCall=" + uiTemplateExternalCall);
        }

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        if (uiTemplateExternalCall.getAvatarId() != -1) {
            mAvatarView.setVisibility(View.VISIBLE);
            mNoAvatarView.setVisibility(View.GONE);
            Bitmap bitmap = BitmapFactory.decodeResource(itemView.getResources(), uiTemplateExternalCall.getAvatarId());
            mAvatarView.setImage(itemView.getContext(), null,
                    new CircularImageDescriptor(bitmap, 0.5f, 0.5f, 0.5f));
            mNoAvatarTextView.setVisibility(View.GONE);
        } else {
            mAvatarView.setVisibility(View.GONE);
            mNoAvatarView.setVisibility(View.VISIBLE);
            mNoAvatarTextView.setVisibility(View.VISIBLE);

            String name = uiTemplateExternalCall.getName();
            if (name != null && !name.isEmpty()) {
                mNoAvatarTextView.setText(name.substring(0, 1).toUpperCase());
            }
            mNoAvatarTextView.setTextColor(Design.getMainStyle());
        }

        mNameView.setText(uiTemplateExternalCall.getName());

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }
    }

    public void onViewRecycled() {

    }
}
