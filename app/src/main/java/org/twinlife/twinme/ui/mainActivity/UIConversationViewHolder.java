/*
 *  Copyright (c) 2017-2026 twinlife SA.
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
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
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
    private static final int DESIGN_DATE_WIDTH = 200;
    private static final int DESIGN_AVATAR_MEMBER_SIZE = 38;
    private static final int DESIGN_UNREAD_SIZE=  20;
    private static final int DESIGN_UNREAD_MARGIN = 10;
    private static final int DESIGN_START_TEXT_MARGIN = 34;
    private static final int DESIGN_END_TEXT_MARGIN = 44;
    private static final int DESIGN_ICON_WIDTH = 24;
    private static final int DESIGN_ICON_HEIGHT = 28;

    private final AbstractTwinmeService mService;
    private final View mAvatarContainerView;
    private final RoundedView mNoAvatarView;
    private final CircularImageView mAvatarView;
    private final CircularImageView mMemberOneAvatarView;
    private final CircularImageView mMemberTwoAvatarView;
    private final CircularImageView mMemberThreeAvatarView;
    private final CircularImageView mMemberFourAvatarView;
    private final ViewGroup mNameContainerView;
    private final TextView mNameView;
    private final TextView mInformationView;
    private final TextView mDateView;
    private final View mMoreView;
    private final TextView mMoreTextView;
    private final RoundedView mUnreadView;
    private final View mTagView;
    private final View mCertifiedView;
    private final ImageView mIconView;
    private final View mSeparatorView;

    UIConversationViewHolder(@NonNull AbstractTwinmeService service, View view, int infoTopMargin) {

        super(view);

        mService = service;

        view.setBackgroundColor(Design.WHITE_COLOR);

        mNoAvatarView = view.findViewById(R.id.conversations_fragment_conversation_item_no_avatar_view);
        mNoAvatarView.setColor(Color.parseColor(Design.DEFAULT_COLOR));

        mAvatarContainerView = view.findViewById(R.id.conversations_fragment_conversation_item_avatar_container_view);

        ViewGroup.LayoutParams layoutParams = mAvatarContainerView.getLayoutParams();
        layoutParams.width = Design.AVATAR_HEIGHT;
        layoutParams.height = Design.AVATAR_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mAvatarContainerView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.AVATAR_MARGIN;

        mAvatarView = view.findViewById(R.id.conversations_fragment_conversation_item_avatar_view);

        mMemberOneAvatarView = view.findViewById(R.id.conversations_fragment_conversation_item_member_one_avatar_view);
        mMemberTwoAvatarView = view.findViewById(R.id.conversations_fragment_conversation_item_member_two_avatar_view);
        mMemberThreeAvatarView = view.findViewById(R.id.conversations_fragment_conversation_item_member_three_avatar_view);
        mMemberFourAvatarView = view.findViewById(R.id.conversations_fragment_conversation_item_member_four_avatar_view);

        layoutParams = mMemberOneAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_MEMBER_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_MEMBER_SIZE * Design.HEIGHT_RATIO);

        layoutParams = mMemberTwoAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_MEMBER_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_MEMBER_SIZE * Design.HEIGHT_RATIO);

        layoutParams = mMemberThreeAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_MEMBER_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_MEMBER_SIZE * Design.HEIGHT_RATIO);

        layoutParams = mMemberFourAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_MEMBER_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_MEMBER_SIZE * Design.HEIGHT_RATIO);

        mMoreView = view.findViewById(R.id.conversations_fragment_conversation_item_more_view);

        layoutParams = mMoreView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_MEMBER_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_MEMBER_SIZE * Design.HEIGHT_RATIO);

        RoundedView moreRoundedView = view.findViewById(R.id.conversations_fragment_conversation_item_more_rounded_view);
        moreRoundedView.setColor(Design.BLUE_NORMAL);

        mMoreTextView = view.findViewById(R.id.conversations_fragment_conversation_item_more_text_view);
        Design.updateTextFont(mMoreTextView, Design.FONT_MEDIUM20);
        mMoreTextView.setTextColor(Color.WHITE);

        View infoView = view.findViewById(R.id.conversations_fragment_conversation_item_text_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) infoView.getLayoutParams();
        marginLayoutParams.topMargin = infoTopMargin;
        marginLayoutParams.leftMargin = (int) (DESIGN_START_TEXT_MARGIN * Design.WIDTH_RATIO);

        View topView = view.findViewById(R.id.conversations_fragment_conversation_item_top_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) topView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_END_TEXT_MARGIN * Design.WIDTH_RATIO);

        mNameContainerView = view.findViewById(R.id.conversations_fragment_conversation_item_name_container_view);

        mNameView = view.findViewById(R.id.conversations_fragment_conversation_item_name_view);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM34);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mInformationView = view.findViewById(R.id.conversations_fragment_conversation_item_information_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mInformationView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_END_TEXT_MARGIN * Design.WIDTH_RATIO);

        Design.updateTextFont(mInformationView, Design.FONT_REGULAR30);
        mInformationView.setTextColor(DESIGN_INFORMATION_COLOR);

        mDateView = view.findViewById(R.id.conversations_fragment_conversation_item_date_view);
        Design.updateTextFont(mDateView, Design.FONT_REGULAR30);
        mDateView.setTextColor(DESIGN_INFORMATION_COLOR);

        mDateView.setMaxWidth((int)(Design.WIDTH_RATIO * DESIGN_DATE_WIDTH));

        mUnreadView = view.findViewById(R.id.conversations_fragment_conversation_unread_view);
        mUnreadView.setColor(Design.getMainStyle());

        layoutParams = mUnreadView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_UNREAD_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_UNREAD_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mUnreadView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_UNREAD_MARGIN * Design.WIDTH_RATIO);

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
        layoutParams.width = Design.CERTIFIED_HEIGHT;
        layoutParams.height = Design.CERTIFIED_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCertifiedView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO));
        marginLayoutParams.rightMargin = (int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginEnd((int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO));

        mIconView = view.findViewById(R.id.conversations_fragment_conversation_item_icon_view);
        mIconView.setColorFilter(DESIGN_INFORMATION_COLOR);
        mIconView.setVisibility(View.GONE);

        layoutParams = mIconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ICON_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height =(int) (DESIGN_ICON_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mIconView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_END_TEXT_MARGIN * Design.WIDTH_RATIO);

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

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mNameContainerView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_END_TEXT_MARGIN * Design.WIDTH_RATIO);
        mNameContainerView.setLayoutParams(marginLayoutParams);

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

                    marginLayoutParams = (ViewGroup.MarginLayoutParams) mMemberOneAvatarView.getLayoutParams();
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

                    marginLayoutParams = (ViewGroup.MarginLayoutParams) mMemberOneAvatarView.getLayoutParams();
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
                    marginLayoutParams = (ViewGroup.MarginLayoutParams) mMemberOneAvatarView.getLayoutParams();
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
                uiConversation.getUIContact().setAvatar(avatar);
            });

            if (uiConversation.isCertified()) {
                mCertifiedView.setVisibility(View.VISIBLE);
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

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mInformationView.getLayoutParams();
        if (uiConversation.isSilentMode()) {
            mIconView.setVisibility(View.VISIBLE);
            marginLayoutParams.rightMargin = (int) (DESIGN_END_TEXT_MARGIN * 2 * Design.WIDTH_RATIO + DESIGN_ICON_WIDTH * Design.WIDTH_RATIO);
        } else {
            mIconView.setVisibility(View.GONE);
            marginLayoutParams.rightMargin = (int) (DESIGN_END_TEXT_MARGIN * Design.WIDTH_RATIO);
        }

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }

        ViewTreeObserver viewTreeObserver = mNameContainerView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = mNameContainerView.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalLayoutListener(this);

                int nameContainerWidth = mNameContainerView.getWidth();
                if (uiConversation.isCertified()) {
                    mNameView.setMaxWidth(nameContainerWidth - Design.CERTIFIED_HEIGHT - (int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO));
                } else {
                    mNameView.setMaxWidth(nameContainerWidth);
                }
            }
        });

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
