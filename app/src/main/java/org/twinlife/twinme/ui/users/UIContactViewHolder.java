/*
 *  Copyright (c) 2017-2020 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.users;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.TextStyle;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.Utils;

public class UIContactViewHolder<E extends UIOriginator> extends RecyclerView.ViewHolder {

    private static final int DESIGN_TAG_HEIGHT = 46;
    private static final int DESIGN_TAG_MARGIN = 12;
    protected static final int DESIGN_CERTIFIED_MARGIN = 20;
    protected static final float DESIGN_MARGIN_PERCENT = 0.0986f;
    protected static final int DESIGN_AVATAR_HEIGHT = 86;

    private static final int DESIGN_TAG_BORDER_WIDTH = 2;

    @NonNull
    private final AbstractTwinmeService mService;
    private final CircularImageView mAvatarView;
    protected final TextView mNameView;

    private final View mTagView;
    private final TextView mTagTitleView;
    private final ImageView mTagImageView;
    protected final ImageView mCertifiedView;

    private final View mSeparatorView;
    private final TextStyle mFont;

    public UIContactViewHolder(@NonNull AbstractTwinmeService service, View view, @IdRes int nameId, @IdRes int avatarId, @IdRes int tagId, @IdRes int tagTitleId, @IdRes int tagImageId, @IdRes int certifiedId, @IdRes int separatorId, TextStyle font) {
        super(view);

        mService = service;

        mFont = font;

        mAvatarView = view.findViewById(avatarId);

        if (nameId != 0) {
            view.setBackgroundColor(Design.WHITE_COLOR);
            mNameView = view.findViewById(nameId);
            mNameView.setTypeface(mFont.typeface);
            mNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mFont.size);
            mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameView.getLayoutParams();
            marginLayoutParams.rightMargin = Design.NAME_TRAILING;
            marginLayoutParams.setMarginEnd(Design.NAME_TRAILING);
        } else {
            mNameView = null;
            view.setBackgroundColor(Color.TRANSPARENT);
        }

        if (tagId != 0) {
            mTagView = view.findViewById(tagId);

            ViewGroup.LayoutParams layoutParams = mTagView.getLayoutParams();
            layoutParams.height = (int) (DESIGN_TAG_HEIGHT * Design.HEIGHT_RATIO);

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTagView.getLayoutParams();
            marginLayoutParams.rightMargin = Design.NAME_TRAILING;
            marginLayoutParams.setMarginEnd(Design.NAME_TRAILING);
        } else {
            mTagView = null;
        }

        if (tagTitleId != 0) {
            mTagTitleView = view.findViewById(tagTitleId);
            Design.updateTextFont(mTagTitleView, Design.FONT_REGULAR28);

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTagTitleView.getLayoutParams();
            marginLayoutParams.leftMargin = (int) (DESIGN_TAG_MARGIN * Design.WIDTH_RATIO);
            marginLayoutParams.rightMargin = (int) (DESIGN_TAG_MARGIN * Design.WIDTH_RATIO);
        } else {
            mTagTitleView = null;
        }

        if (tagImageId != 0) {
            mTagImageView = view.findViewById(tagImageId);

            ViewGroup.LayoutParams layoutParams = mTagImageView.getLayoutParams();
            layoutParams.height = (int) (DESIGN_TAG_HEIGHT * Design.HEIGHT_RATIO);

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTagImageView.getLayoutParams();
            marginLayoutParams.rightMargin = Design.NAME_TRAILING;
            marginLayoutParams.setMarginEnd(Design.NAME_TRAILING);
        } else {
            mTagImageView = null;
        }

        if (certifiedId != 0) {
            mCertifiedView = view.findViewById(certifiedId);

            ViewGroup.LayoutParams layoutParams = mCertifiedView.getLayoutParams();
            layoutParams.height = Design.CERTIFIED_HEIGHT;

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mCertifiedView.getLayoutParams();
            marginLayoutParams.rightMargin = Design.NAME_TRAILING;
            marginLayoutParams.setMarginEnd(Design.NAME_TRAILING);
        } else {
            mCertifiedView = null;
        }

        if (separatorId != 0) {
            mSeparatorView = view.findViewById(separatorId);
            mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
        } else {
            mSeparatorView = null;
        }
    }

    public  void onBind(Context context, Bitmap avatar) {

        mAvatarView.setImage(context, null,
                new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
    }

    public void onBind(@Nullable Context context, @Nullable E uiContact, boolean hideSeparator) {
        onBind(context, uiContact, null, hideSeparator);
    }

    public void onBind(@Nullable Context context, @Nullable E uiContact, @Nullable String searchContent, boolean hideSeparator) {

        if (uiContact == null || context == null) {
            return;
        }

        mService.getImageOrDefaultAvatar(uiContact.getContact(), (Bitmap avatar) -> {
            mAvatarView.setImage(context, null,
                    new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
        });

        if (mNameView != null) {

            if (searchContent == null) {
                mNameView.setText(uiContact.getName());
            } else {
                String name = uiContact.getName().replaceAll("(?i)" + searchContent, "~" + searchContent + "~");
                mNameView.setText(Utils.formatText(name, (int) Design.FONT_REGULAR34.size));
            }

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameView.getLayoutParams();
            marginLayoutParams.rightMargin = Design.NAME_TRAILING;
            marginLayoutParams.setMarginEnd(Design.NAME_TRAILING);

            float maxWidth = Design.DISPLAY_WIDTH - mNameView.getX() - (Design.NAME_TRAILING);
            mNameView.setMaxWidth((int) maxWidth);
        }

        if (mTagView != null) {
            if (uiContact.getUIContactTag() != null) {

                mTagView.setVisibility(View.VISIBLE);

                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setColor(uiContact.getUIContactTag().getBackgroundColor());
                gradientDrawable.setCornerRadius(Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density);
                gradientDrawable.setStroke(DESIGN_TAG_BORDER_WIDTH, uiContact.getUIContactTag().getForegroundColor());
                mTagView.setBackground(gradientDrawable);

                mTagTitleView.setTextColor(uiContact.getUIContactTag().getForegroundColor());
                mTagTitleView.setText(uiContact.getUIContactTag().getTitle());

                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameView.getLayoutParams();
                marginLayoutParams.rightMargin = (int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO);
                marginLayoutParams.setMarginEnd((int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO));

                Paint paint = new Paint();
                paint.setTextSize(Design.FONT_REGULAR28.size);
                paint.setTypeface(Design.FONT_REGULAR28.typeface);
                paint.setStyle(Paint.Style.STROKE);

                float maxWidth = Design.DISPLAY_WIDTH - (Design.DISPLAY_WIDTH * DESIGN_MARGIN_PERCENT) - (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO) - Design.NAME_TRAILING - ((DESIGN_CERTIFIED_MARGIN + (DESIGN_TAG_MARGIN * 2)) * Design.WIDTH_RATIO) - paint.measureText(uiContact.getUIContactTag().getTitle());
                mNameView.setMaxWidth((int) maxWidth);
            } else {
                mTagView.setVisibility(View.GONE);
            }
        }

        if (mTagImageView != null) {
            if (uiContact.isScheduleEnable()) {
                mTagImageView.setVisibility(View.VISIBLE);
            } else {
                mTagImageView.setVisibility(View.GONE);
            }
        }

        if (mCertifiedView != null) {
            if (uiContact.isCertified()) {
                mCertifiedView.setVisibility(View.VISIBLE);

                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameView.getLayoutParams();
                marginLayoutParams.rightMargin = (int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO);
                marginLayoutParams.setMarginEnd((int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO));

                float maxWidth = Design.DISPLAY_WIDTH - (Design.DISPLAY_WIDTH * DESIGN_MARGIN_PERCENT) - (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO) - Design.NAME_TRAILING -  (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO) - Design.CERTIFIED_HEIGHT;
                mNameView.setMaxWidth((int) maxWidth);
            } else {
                mCertifiedView.setVisibility(View.GONE);
            }
        }

        if (mSeparatorView != null) {
            if (hideSeparator) {
                mSeparatorView.setVisibility(View.GONE);
            } else {
                mSeparatorView.setVisibility(View.VISIBLE);
            }
        }

        updateFont();
        updateColor();
    }

    public void onViewRecycled() {

        mAvatarView.dispose();
    }

    CircularImageView getAvatarView() {

        return mAvatarView;
    }

    TextView getNameView() {
        return mNameView;
    }

    private void updateFont() {

        if (mNameView != null) {
            mNameView.setTypeface(mFont.typeface);
            mNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mFont.size);
        }
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);

        if (mSeparatorView != null) {
            mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
        }

        if (mNameView != null) {
            itemView.setBackgroundColor(Design.WHITE_COLOR);
            mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
            mNameView.setTypeface(mFont.typeface);
            mNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mFont.size);
        }
    }
}
