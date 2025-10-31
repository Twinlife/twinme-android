/*
 *  Copyright (c) 2017-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Auguste Hatton (Auguste.Hatton@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.Utils;

class UIConversationViewHolder extends RecyclerView.ViewHolder {

    private static final int DESIGN_INFORMATION_COLOR = Color.rgb(115, 138, 161);
    private static final int DESIGN_TAG_BACKGROUND_COLOR = Color.argb(30, 255, 147, 0);
    private static final int DESIGN_TAG_FOREGROUND_COLOR = Color.argb(255, 255, 147, 0);
    private static final int DESIGN_TAG_HEIGHT = 46;
    private static final int DESIGN_TAG_TITLE_MARGIN = 12;
    private static final int DESIGN_TAG_MARGIN = 38;
    private static final int DESIGN_TAG_BORDER_WIDTH = 2;
    private static final int DESIGN_CERTIFIED_MARGIN = 20;
    private static final float DESIGN_MARGIN_PERCENT = 0.4169f;
    private static final float DESIGN_DATE_PERCENT = 0.3114f;
    private static final int DESIGN_AVATAR_HEIGHT = 86;

    private final AbstractTwinmeService mService;
    private final View mAvatarContainerView;
    private final RoundedView mNoAvatarView;
    private final CircularImageView mAvatarView;
    private final CircularImageView mMemberOneAvatarView;
    private final CircularImageView mMemberTwoAvatarView;
    private final CircularImageView mMemberThreeAvatarView;
    private final CircularImageView mMemberFourAvatarView;
    private final TextView mNameView;
    private final TextView mInformationView;
    private final TextView mDateView;
    private final View mMoreView;
    private final TextView mMoreTextView;
    private final RoundedView mUnreadView;
    private final View mTagView;
    private final View mCertifiedView;
    private final View mSeparatorView;

    UIConversationViewHolder(@NonNull AbstractTwinmeService service, View view, int infoTopMargin) {

        super(view);

        mService = service;

        view.setBackgroundColor(Design.WHITE_COLOR);

        mNoAvatarView = view.findViewById(R.id.conversations_fragment_conversation_item_no_avatar_view);
        mNoAvatarView.setColor(Color.parseColor(Design.DEFAULT_COLOR));

        mAvatarContainerView = view.findViewById(R.id.conversations_fragment_conversation_item_avatar_container_view);

        ViewGroup.LayoutParams layoutParams = mAvatarContainerView.getLayoutParams();
        layoutParams.height = Design.AVATAR_HEIGHT;

        mAvatarView = view.findViewById(R.id.conversations_fragment_conversation_item_avatar_view);

        mMemberOneAvatarView = view.findViewById(R.id.conversations_fragment_conversation_item_member_one_avatar_view);
        mMemberTwoAvatarView = view.findViewById(R.id.conversations_fragment_conversation_item_member_two_avatar_view);
        mMemberThreeAvatarView = view.findViewById(R.id.conversations_fragment_conversation_item_member_three_avatar_view);
        mMemberFourAvatarView = view.findViewById(R.id.conversations_fragment_conversation_item_member_four_avatar_view);

        mMoreView = view.findViewById(R.id.conversations_fragment_conversation_item_more_view);

        RoundedView moreRoundedView = view.findViewById(R.id.conversations_fragment_conversation_item_more_rounded_view);
        moreRoundedView.setColor(Design.BLUE_NORMAL);

        mMoreTextView = view.findViewById(R.id.conversations_fragment_conversation_item_more_text_view);
        Design.updateTextFont(mMoreTextView, Design.FONT_MEDIUM20);
        mMoreTextView.setTextColor(Color.WHITE);

        View infoView = view.findViewById(R.id.conversations_fragment_conversation_item_text_view);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) infoView.getLayoutParams();
        marginLayoutParams.topMargin = infoTopMargin;

        mNameView = view.findViewById(R.id.conversations_fragment_conversation_item_name_view);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM34);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mInformationView = view.findViewById(R.id.conversations_fragment_conversation_item_information_view);
        Design.updateTextFont(mInformationView, Design.FONT_REGULAR30);
        mInformationView.setTextColor(DESIGN_INFORMATION_COLOR);

        mDateView = view.findViewById(R.id.conversations_fragment_conversation_item_date_view);
        Design.updateTextFont(mDateView, Design.FONT_REGULAR30);
        mDateView.setTextColor(DESIGN_INFORMATION_COLOR);

        mDateView.setMaxWidth((int) (Design.DISPLAY_WIDTH * DESIGN_DATE_PERCENT));

        mUnreadView = view.findViewById(R.id.conversations_fragment_conversation_unread_view);
        mUnreadView.setColor(Design.getMainStyle());

        mTagView = view.findViewById(R.id.conversations_fragment_conversation_item_tag_view);
        mTagView.setVisibility(View.GONE);

        layoutParams = mTagView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_TAG_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTagView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_TAG_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginEnd((int) (DESIGN_TAG_MARGIN * Design.WIDTH_RATIO));

        final TextView tagTitleView = view.findViewById(R.id.conversations_fragment_conversation_item_tag_title_view);
        Design.updateTextFont(tagTitleView, Design.FONT_REGULAR28);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) tagTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_TAG_TITLE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_TAG_TITLE_MARGIN * Design.WIDTH_RATIO);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(DESIGN_TAG_BACKGROUND_COLOR);
        gradientDrawable.setCornerRadius(Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density);
        gradientDrawable.setStroke(DESIGN_TAG_BORDER_WIDTH, DESIGN_TAG_FOREGROUND_COLOR);
        mTagView.setBackground(gradientDrawable);

        tagTitleView.setTextColor(DESIGN_TAG_FOREGROUND_COLOR);

        mCertifiedView = view.findViewById(R.id.conversations_fragment_conversation_item_certified_image_view);

        layoutParams = mCertifiedView.getLayoutParams();
        layoutParams.height = Design.CERTIFIED_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCertifiedView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginEnd((int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO));

        mSeparatorView = view.findViewById(R.id.conversations_fragment_conversation_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    void onBind(@NonNull Context context, @NonNull UIConversation uiConversation, boolean hideSeparator) {

        mNoAvatarView.setVisibility(View.GONE);
        mAvatarView.setColorFilter(Color.TRANSPARENT);
        mTagView.setVisibility(View.GONE);
        mInformationView.setVisibility(View.VISIBLE);
        mDateView.setVisibility(View.VISIBLE);
        mCertifiedView.setVisibility(View.GONE);

        float maxWidth = Design.DISPLAY_WIDTH - (Design.DISPLAY_WIDTH * DESIGN_MARGIN_PERCENT) - (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO);
        mNameView.setMaxWidth((int) maxWidth);

        final Originator subject = uiConversation.getContact();
        if (subject.isGroup()) {
            final UIGroupConversation groupConversation = (UIGroupConversation) uiConversation;

            if (subject.getAvatarId() != null || groupConversation.getGroupAvatars().size() < 2) {
                mAvatarView.setImage(context, null,
                        new CircularImageDescriptor(uiConversation.getAvatar(), 0.5f, 0.5f, 0.5f));
                mAvatarView.setVisibility(View.VISIBLE);

                if (subject.getAvatarId() == null) {
                    mAvatarView.setColorFilter(Color.WHITE);
                    mNoAvatarView.setVisibility(View.VISIBLE);
                }
                mMemberOneAvatarView.setVisibility(View.GONE);
                mMemberTwoAvatarView.setVisibility(View.GONE);
                mMemberThreeAvatarView.setVisibility(View.GONE);
                mMemberFourAvatarView.setVisibility(View.GONE);

                if (groupConversation.getGroupMemberCount() > 0) {
                    mMoreView.setVisibility(View.VISIBLE);
                    int more = groupConversation.getGroupMemberCount();
                    String text = "+" + more;
                    mMoreTextView.setText(text);
                } else {
                    mMoreView.setVisibility(View.GONE);
                }
            } else {
                mAvatarView.setVisibility(View.GONE);
                if (groupConversation.getGroupAvatars().size() == 2) {
                    Bitmap memberOne = groupConversation.getGroupAvatars().get(0);
                    mMemberOneAvatarView.setImage(context, null, new CircularImageDescriptor(memberOne, 0.5f, 0.5f, 0.5f));
                    mMemberOneAvatarView.setVisibility(View.VISIBLE);

                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mMemberOneAvatarView.getLayoutParams();
                    marginLayoutParams.topMargin = (int) (mAvatarContainerView.getHeight() * 0.5 - mMemberOneAvatarView.getHeight() * 0.5);

                    Bitmap memberTwo = groupConversation.getGroupAvatars().get(1);
                    mMemberTwoAvatarView.setImage(context, null, new CircularImageDescriptor(memberTwo, 0.5f, 0.5f, 0.5f));
                    mMemberTwoAvatarView.setVisibility(View.VISIBLE);

                    mMemberThreeAvatarView.setVisibility(View.GONE);
                    mMemberFourAvatarView.setVisibility(View.GONE);
                    mMoreView.setVisibility(View.GONE);
                } else if (groupConversation.getGroupAvatars().size() == 3) {
                    Bitmap memberOne = groupConversation.getGroupAvatars().get(0);
                    mMemberOneAvatarView.setImage(context, null, new CircularImageDescriptor(memberOne, 0.5f, 0.5f, 0.5f));
                    mMemberOneAvatarView.setVisibility(View.VISIBLE);

                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mMemberOneAvatarView.getLayoutParams();
                    marginLayoutParams.topMargin = 0;

                    Bitmap memberTwo = groupConversation.getGroupAvatars().get(1);
                    mMemberTwoAvatarView.setImage(context, null, new CircularImageDescriptor(memberTwo, 0.5f, 0.5f, 0.5f));
                    mMemberTwoAvatarView.setVisibility(View.VISIBLE);

                    Bitmap memberThree = groupConversation.getGroupAvatars().get(2);
                    mMemberThreeAvatarView.setImage(context, null, new CircularImageDescriptor(memberThree, 0.5f, 0.5f, 0.5f));
                    mMemberThreeAvatarView.setVisibility(View.VISIBLE);
                    marginLayoutParams = (ViewGroup.MarginLayoutParams) mMemberThreeAvatarView.getLayoutParams();
                    marginLayoutParams.setMarginStart((int) (mAvatarContainerView.getHeight() * 0.5 - mMemberOneAvatarView.getHeight() * 0.5));

                    mMemberFourAvatarView.setVisibility(View.GONE);
                    mMoreView.setVisibility(View.GONE);
                } else if (groupConversation.getGroupAvatars().size() > 3) {
                    Bitmap memberOne = groupConversation.getGroupAvatars().get(0);
                    mMemberOneAvatarView.setImage(context, null, new CircularImageDescriptor(memberOne, 0.5f, 0.5f, 0.5f));
                    mMemberOneAvatarView.setVisibility(View.VISIBLE);
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mMemberOneAvatarView.getLayoutParams();
                    marginLayoutParams.topMargin = 0;

                    Bitmap memberTwo = groupConversation.getGroupAvatars().get(1);
                    mMemberTwoAvatarView.setImage(context, null, new CircularImageDescriptor(memberTwo, 0.5f, 0.5f, 0.5f));
                    mMemberTwoAvatarView.setVisibility(View.VISIBLE);

                    Bitmap memberThree = groupConversation.getGroupAvatars().get(2);
                    mMemberThreeAvatarView.setImage(context, null, new CircularImageDescriptor(memberThree, 0.5f, 0.5f, 0.5f));
                    mMemberThreeAvatarView.setVisibility(View.VISIBLE);
                    marginLayoutParams = (ViewGroup.MarginLayoutParams) mMemberThreeAvatarView.getLayoutParams();
                    marginLayoutParams.setMarginStart(0);

                    mMemberFourAvatarView.setVisibility(View.GONE);
                    mMoreView.setVisibility(View.GONE);

                    if (groupConversation.getGroupAvatars().size() == 4) {
                        Bitmap memberFour = groupConversation.getGroupAvatars().get(3);
                        mMemberFourAvatarView.setImage(context, null, new CircularImageDescriptor(memberFour, 0.5f, 0.5f, 0.5f));
                        mMemberFourAvatarView.setVisibility(View.VISIBLE);
                    } else {
                        mMoreView.setVisibility(View.VISIBLE);

                        int more = groupConversation.getGroupMemberCount() - 3;
                        String text = "+" + more;
                        mMoreTextView.setText(text);
                    }
                }
            }

            if (groupConversation.getGroupConversationState() == ConversationService.GroupConversation.State.CREATED) {
                mTagView.setVisibility(View.VISIBLE);
                mDateView.setVisibility(View.GONE);
            }
        } else {
            mMemberOneAvatarView.setVisibility(View.GONE);
            mMemberTwoAvatarView.setVisibility(View.GONE);
            mMemberThreeAvatarView.setVisibility(View.GONE);
            mMemberFourAvatarView.setVisibility(View.GONE);
            mMoreView.setVisibility(View.GONE);

            mService.getImage(subject, (Bitmap avatar) -> {
                mAvatarView.setImage(context, null,
                        new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
                mAvatarView.setVisibility(View.VISIBLE);
            });

            if (uiConversation.isCertified()) {
                mCertifiedView.setVisibility(View.VISIBLE);
                maxWidth = Design.DISPLAY_WIDTH - (Design.DISPLAY_WIDTH * DESIGN_MARGIN_PERCENT) - (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO) - (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO) - Design.CERTIFIED_HEIGHT;
                mNameView.setMaxWidth((int) maxWidth);
            }
        }

        mNameView.setText(uiConversation.getName());

        mInformationView.setText(Utils.formatText(uiConversation.getLastMessage(context), 0));
        mDateView.setText(uiConversation.getMessageDate());

        if (uiConversation.isLastDescriptorUnread()) {
            mUnreadView.setVisibility(View.VISIBLE);
        } else {
            mUnreadView.setVisibility(View.GONE);
        }

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }

        updateColor();
        updateFont();
    }

    void onViewRecycled() {

        mAvatarView.dispose();

        mMemberOneAvatarView.setVisibility(View.GONE);
        mMemberTwoAvatarView.setVisibility(View.GONE);
        mMemberThreeAvatarView.setVisibility(View.GONE);
        mMemberFourAvatarView.setVisibility(View.GONE);
        mMoreView.setVisibility(View.GONE);
    }

    private void updateColor() {

        itemView.setBackgroundColor(Design.WHITE_COLOR);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mUnreadView.setColor(Design.getMainStyle());
    }

    private void updateFont() {

        Design.updateTextFont(mNameView, Design.FONT_MEDIUM34);
        Design.updateTextFont(mInformationView, Design.FONT_REGULAR30);
        Design.updateTextFont(mMoreTextView, Design.FONT_MEDIUM20);
        Design.updateTextFont(mDateView, Design.FONT_REGULAR30);
    }
}
