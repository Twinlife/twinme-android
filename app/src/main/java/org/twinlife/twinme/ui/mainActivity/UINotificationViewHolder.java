/*
 *  Copyright (c) 2017-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Thibaud David (contact@thibauddavid.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Auguste Hatton (Auguste.Hatton@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.conversationActivity.UIReaction;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.RoundedView;

import java.util.concurrent.TimeUnit;

public class UINotificationViewHolder extends RecyclerView.ViewHolder {

    private static final int DESIGN_TIME_COLOR = Color.rgb(119, 138, 159);

    public static final float DESIGN_ITEM_TYPE_VIEW_WIDTH = 26f;
    public static final float DESIGN_ITEM_TYPE_VIEW_MARGIN_RIGHT = 14f;
    public static final float DESIGN_ITEM_TOP_VIEW_MARGIN_BOTTOM = 16f;
    private static final int DESIGN_CERTIFIED_MARGIN = 20;
    private static final float DESIGN_MARGIN_PERCENT = 0.2479f;
    private static final float DESIGN_DATE_PERCENT = 0.12f;
    private static final int DESIGN_AVATAR_HEIGHT = 86;

    private final AbstractTwinmeActivity mNotificationActivity;
    private final @NonNull
    AbstractTwinmeService mService;

    private final View mSlidingContainerView;
    private final RoundedView mNoAvatarView;
    private final CircularImageView mAvatarView;
    private final TextView mTitleView;
    private final ImageView mTypeView;
    private final TextView mSubtitleView;
    private final TextView mTimeView;
    private final RoundedView mAcknowledgedView;
    private final View mCertifiedView;
    private final View mSeparatorView;
    private final boolean mIsRTL;

    public UINotificationViewHolder(AbstractTwinmeActivity notificationActivity, @NonNull AbstractTwinmeService service, View view) {

        super(view);

        mNotificationActivity = notificationActivity;
        mService = service;

        mIsRTL = CommonUtils.isLayoutDirectionRTL();

        mSlidingContainerView = view.findViewById(R.id.notifications_fragment_notification_item_sliding_container_view);

        mAvatarView = view.findViewById(R.id.notifications_fragment_notification_item_avatar_view);

        mNoAvatarView = view.findViewById(R.id.notifications_fragment_notification_item_no_avatar_view);
        mNoAvatarView.setColor(Design.GREY_ITEM_COLOR);

        mTitleView = view.findViewById(R.id.notifications_fragment_notification_item_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM34);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.rightMargin = Design.NAME_TRAILING;
        marginLayoutParams.setMarginEnd(Design.NAME_TRAILING);

        View informationTopView = view.findViewById(R.id.notifications_fragment_notification_item_information_top_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) informationTopView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_ITEM_TOP_VIEW_MARGIN_BOTTOM * Design.WIDTH_RATIO);

        mTypeView = view.findViewById(R.id.notifications_fragment_notification_item_type_view);

        ViewGroup.LayoutParams layoutParams = mTypeView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ITEM_TYPE_VIEW_WIDTH * Design.WIDTH_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mTypeView.getLayoutParams();
        if (mIsRTL) {
            marginLayoutParams.leftMargin = (int) (DESIGN_ITEM_TYPE_VIEW_MARGIN_RIGHT * Design.WIDTH_RATIO);
        } else {
            marginLayoutParams.rightMargin = (int) (DESIGN_ITEM_TYPE_VIEW_MARGIN_RIGHT * Design.WIDTH_RATIO);
        }

        mSubtitleView = view.findViewById(R.id.notifications_fragment_notification_item_subtitle_view);
        Design.updateTextFont(mSubtitleView, Design.FONT_MEDIUM28);
        mSubtitleView.setTextColor(DESIGN_TIME_COLOR);

        mTimeView = view.findViewById(R.id.notifications_fragment_notification_item_time_view);
        Design.updateTextFont(mTimeView, Design.FONT_MEDIUM26);
        mTimeView.setTextColor(DESIGN_TIME_COLOR);

        mTimeView.setMaxWidth((int) (Design.DISPLAY_WIDTH * DESIGN_DATE_PERCENT));

        mAcknowledgedView = view.findViewById(R.id.notifications_fragment_notification_item_acknowledged_view);
        mAcknowledgedView.setColor(Design.getMainStyle());

        mCertifiedView = view.findViewById(R.id.notifications_fragment_notification_item_certified_image_view);

        layoutParams = mCertifiedView.getLayoutParams();
        layoutParams.height = Design.CERTIFIED_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCertifiedView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginEnd((int) (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO));

        mSeparatorView = view.findViewById(R.id.notifications_fragment_notification_item_separator_view);
        mSeparatorView.setBackgroundColor(Design.SEPARATOR_COLOR);
    }

    @SuppressLint("DefaultLocale")
    public void onBind(Context context, UINotification uiNotification, boolean hideSeparator) {

        mSlidingContainerView.setTranslationX(0);

        float maxWidth = Design.DISPLAY_WIDTH - (Design.DISPLAY_WIDTH * DESIGN_MARGIN_PERCENT) - (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO) - Design.NAME_TRAILING;
        mTitleView.setMaxWidth((int) maxWidth);

        uiNotification.getAvatar(mService, (Bitmap avatar) -> {
            if (avatar == null) {
                avatar = mNotificationActivity.getTwinmeApplication().getAnonymousAvatar();
            }

            if (avatar.equals(mNotificationActivity.getTwinmeApplication().getDefaultGroupAvatar())) {
                mNoAvatarView.setVisibility(View.VISIBLE);
            } else {
                mNoAvatarView.setVisibility(View.GONE);
            }

            mAvatarView.setImage(context, null, new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
        });

        mTitleView.setText(uiNotification.getName(mIsRTL));

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
        if (uiNotification.isCertified()) {
            mCertifiedView.setVisibility(View.VISIBLE);

            float maxNameWidth = Design.DISPLAY_WIDTH - (Design.DISPLAY_WIDTH * DESIGN_MARGIN_PERCENT) - (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO) - (DESIGN_CERTIFIED_MARGIN * Design.WIDTH_RATIO) - Design.CERTIFIED_HEIGHT;
            mTitleView.setMaxWidth((int) maxNameWidth);
            marginLayoutParams.rightMargin = 0;
            marginLayoutParams.setMarginEnd(0);
        } else {
            mCertifiedView.setVisibility(View.GONE);
            marginLayoutParams.rightMargin = Design.NAME_TRAILING;
            marginLayoutParams.setMarginEnd(Design.NAME_TRAILING);
        }

        String subTitle = "";

        mTypeView.clearColorFilter();
        Drawable drawable = null;
        switch (uiNotification.getNotificationType()) {
            case NEW_CONTACT:
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.notification_new_contact, null);
                subTitle = context.getString(R.string.notifications_fragment_item_new_contact);
                break;

            case UPDATED_CONTACT:
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.notification_update_contact, null);
                subTitle = context.getString(R.string.notifications_fragment_item_updated_contact_name);
                break;

            case UPDATED_AVATAR_CONTACT:
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.notification_update_contact, null);
                subTitle = context.getString(R.string.notifications_fragment_item_updated_contact_avatar);
                break;

            case DELETED_CONTACT:
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.notification_remove_contact, null);
                mTypeView.setColorFilter(Design.DELETE_COLOR_RED);
                subTitle = context.getString(R.string.notifications_fragment_item_deleted_contact);
                break;

            case MISSED_AUDIO_CALL:
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.notification_audio_call, null);
                subTitle = context.getString(R.string.notifications_fragment_item_audio_call);
                break;

            case MISSED_VIDEO_CALL:
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.notification_video_call, null);
                subTitle = context.getString(R.string.notifications_fragment_item_video_call);
                break;

            case RESET_CONVERSATION:
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.toolbar_trash_grey, null);
                subTitle = context.getString(R.string.notifications_fragment_item_cleanup_message);
                mTypeView.setColorFilter(Design.DELETE_COLOR_RED);
                break;

            case DELETED_GROUP:
                break;

            case NEW_TEXT_MESSAGE:
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.notification_text_message, null);
                subTitle = context.getString(R.string.notifications_fragment_item_text_message);
                break;

            case NEW_IMAGE_MESSAGE:
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.notification_image_message, null);
                subTitle = context.getString(R.string.notifications_fragment_item_image_message);
                break;

            case NEW_AUDIO_MESSAGE:
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.notification_audio_message, null);
                subTitle = context.getString(R.string.notifications_fragment_item_audio_message);
                mTypeView.setColorFilter(Design.BLACK_COLOR);
                break;

            case NEW_VIDEO_MESSAGE:
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.notification_video_message, null);
                subTitle = context.getString(R.string.notifications_fragment_item_video_message);
                break;

            case NEW_FILE_MESSAGE:
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.notification_file_message, null);
                subTitle = context.getString(R.string.notifications_fragment_item_file_message);
                mTypeView.setColorFilter(Design.BLACK_COLOR);
                break;

            case NEW_CONTACT_INVITATION:
            case NEW_GROUP_INVITATION:
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.notification_invitation_group, null);
                subTitle = context.getString(R.string.notifications_fragment_item_group_invitation);
                break;

            case NEW_GROUP_JOINED:
                drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.notification_join_group, null);
                subTitle = context.getString(R.string.notifications_fragment_item_join_group);
                break;

            case UPDATED_ANNOTATION:
                mTypeView.setImageResource(UIReaction.getNotificationImageReactionWithReactionType(uiNotification.getLastNotification().getAnnotationValue()));
                mTypeView.setColorFilter(UIReaction.getColorFilterReactionWithReactionType(uiNotification.getLastNotification().getAnnotationValue()));
                subTitle = context.getString(R.string.notification_center_reaction_message);
                break;
        }
        if (drawable != null) {
            mTypeView.setImageDrawable(drawable);
        }

        if (uiNotification.getCount() > 1) {
            mSubtitleView.setText(String.format("%s (%d)", subTitle, uiNotification.getCount()));
        } else {
            mSubtitleView.setText(subTitle);
        }

        mTimeView.setText(getTimeAgo(context, uiNotification.getTimestamp()));

        if (!uiNotification.isAcknowledged()) {
            mAcknowledgedView.setVisibility(View.VISIBLE);
        } else {
            mAcknowledgedView.setVisibility(View.GONE);
        }

        if (hideSeparator) {
            mSeparatorView.setVisibility(View.GONE);
        } else {
            mSeparatorView.setVisibility(View.VISIBLE);
        }
    }

    public void onViewRecycled() {

        mSlidingContainerView.setTranslationX(0);

        mAvatarView.dispose();
    }

    //
    // Private Methods
    //

    private static String getTimeAgo(Context context, long time) {

        long now = System.currentTimeMillis();
        final long diff = now - time;

        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (days > 0) {
            return String.format(context.getString(R.string.notifications_fragment_item_shortest_day), days);
        }

        long hours = TimeUnit.MILLISECONDS.toHours(diff);

        if (hours > 0) {
            return String.format(context.getString(R.string.notifications_fragment_item_shortest_hour), hours);
        }

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);

        if (minutes > 0) {
            return String.format(context.getString(R.string.notifications_fragment_item_shortest_minute), minutes);
        }

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);

        if (seconds >= 0) {
            return String.format(context.getString(R.string.notifications_fragment_item_shortest_second), seconds);
        }

        return "";
    }
}
