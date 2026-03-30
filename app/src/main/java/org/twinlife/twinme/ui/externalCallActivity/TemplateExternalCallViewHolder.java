/*
 *  Copyright (c) 2023-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CircularImageView;

public class TemplateExternalCallViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "TemplateExternalCal...";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ROUNDED_VIEW_SIZE = 86f;
    private static final float DESIGN_ROUNDED_VIEW_MARGIN = 20f;
    private static final float DESIGN_ROUNDED_ICON_SIZE = 36f;
    private static final float DESIGN_TEXT_MARGIN = 8f;
    private static final int ROUNDED_VIEW_SIZE;
    private static final int ROUNDED_VIEW_MARGIN;
    private static final int ROUNDED_ICON_SIZE;
    private static final int TEXT_MARGIN;

    static {
        ROUNDED_VIEW_SIZE = (int) (DESIGN_ROUNDED_VIEW_SIZE * Design.HEIGHT_RATIO);
        ROUNDED_VIEW_MARGIN = (int) (DESIGN_ROUNDED_VIEW_MARGIN * Design.HEIGHT_RATIO);
        ROUNDED_ICON_SIZE = (int) (DESIGN_ROUNDED_ICON_SIZE * Design.HEIGHT_RATIO);
        TEXT_MARGIN = (int) (DESIGN_TEXT_MARGIN * Design.HEIGHT_RATIO);
    }

    private final CircularImageView mAvatarView;
    private final TextView mNoAvatarTextView;
    private final TextView mNameView;
    private final View mSeparatorView;
    private final View mNoAvatarView;
    private final ImageView mIconView;

    private final GradientDrawable mNoAvatarGradientDrawable;

    TemplateExternalCallViewHolder(@NonNull View view) {

        super(view);

        View avatarContainerView = view.findViewById(R.id.template_external_call_activity_item_avatar_container_view);

        ViewGroup.LayoutParams layoutParams = avatarContainerView.getLayoutParams();
        layoutParams.width = ROUNDED_VIEW_SIZE;
        layoutParams.height = ROUNDED_VIEW_SIZE;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) avatarContainerView.getLayoutParams();
        marginLayoutParams.topMargin = ROUNDED_VIEW_MARGIN;
        marginLayoutParams.bottomMargin = ROUNDED_VIEW_MARGIN;

        mAvatarView = view.findViewById(R.id.template_external_call_activity_item_avatar_view);

        mNameView = view.findViewById(R.id.template_external_call_activity_item_name_view);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM34);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mNoAvatarTextView = view.findViewById(R.id.template_external_call_activity_item_no_avatar_text_view);
        Design.updateTextFont(mNoAvatarTextView, Design.FONT_BOLD44);
        mNoAvatarTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mNoAvatarView = view.findViewById(R.id.template_external_call_activity_item_no_avatar_view);

        mNoAvatarGradientDrawable = new GradientDrawable();
        mNoAvatarGradientDrawable.mutate();
        mNoAvatarGradientDrawable.setShape(GradientDrawable.OVAL);
        mNoAvatarGradientDrawable.setCornerRadii(new float[]{0, 0, 0, 0, 0, 0, 0, 0});
        mNoAvatarGradientDrawable.setColor(Design.BACKGROUND_COLOR_GREY);
        mNoAvatarView.setBackground(mNoAvatarGradientDrawable);

        mIconView = view.findViewById(R.id.template_external_call_activity_item_icon_view);

        layoutParams = mIconView.getLayoutParams();
        layoutParams.width = ROUNDED_ICON_SIZE;
        layoutParams.height = ROUNDED_ICON_SIZE;

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
        } else if (uiTemplateExternalCall.getAvatar() != null) {
            mAvatarView.setVisibility(View.VISIBLE);
            mNoAvatarView.setVisibility(View.GONE);
            mAvatarView.setImage(itemView.getContext(), null,
                    new CircularImageDescriptor(uiTemplateExternalCall.getAvatar(), 0.5f, 0.5f, 0.5f));
            mNoAvatarTextView.setVisibility(View.GONE);
        } else {
            mAvatarView.setVisibility(View.GONE);
            mNoAvatarView.setVisibility(View.VISIBLE);

            if (uiTemplateExternalCall.getTemplateType() == UITemplateExternalCall.TemplateType.OTHER) {
                mNoAvatarGradientDrawable.setColor(Design.getMainStyle());
                mIconView.setVisibility(View.VISIBLE);
                mNoAvatarTextView.setVisibility(View.GONE);
            } else {
                mNoAvatarGradientDrawable.setColor(Design.BACKGROUND_COLOR_GREY);
                mIconView.setVisibility(View.GONE);
                mNoAvatarTextView.setVisibility(View.VISIBLE);

                String name = uiTemplateExternalCall.getName();
                if (name != null && !name.isEmpty()) {
                    mNoAvatarTextView.setText(name.substring(0, 1).toUpperCase());
                }
                mNoAvatarTextView.setTextColor(Design.getMainStyle());
            }
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(uiTemplateExternalCall.getName());
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (uiTemplateExternalCall.getMessage() != null) {
            spannableStringBuilder.append("\n");
            int startInfo = spannableStringBuilder.length();
            spannableStringBuilder.append(uiTemplateExternalCall.getMessage());
            spannableStringBuilder.setSpan(new RelativeSizeSpan(0.87f), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startInfo, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        mNameView.setText(spannableStringBuilder);

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }
    }

    public void onViewRecycled() {

    }
}
