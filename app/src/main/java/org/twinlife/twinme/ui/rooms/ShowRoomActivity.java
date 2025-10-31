/*
 *  Copyright (c) 2020-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.rooms;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.TwincodeOutbound;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.services.ShowRoomService;
import org.twinlife.twinme.calls.CallStatus;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.EditContactActivity;
import org.twinlife.twinme.ui.EditIdentityActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.LastCallsActivity;
import org.twinlife.twinme.ui.callActivity.CallActivity;
import org.twinlife.twinme.ui.cleanupActivity.TypeCleanUpActivity;
import org.twinlife.twinme.ui.conversationActivity.ConversationActivity;
import org.twinlife.twinme.ui.conversationFilesActivity.ConversationFilesActivity;
import org.twinlife.twinme.ui.exportActivity.ExportActivity;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.RoundedView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShowRoomActivity extends AbstractTwinmeActivity implements ShowRoomService.Observer {
    private static final String LOG_TAG = "ShowRoomActivity";
    private static final boolean DEBUG = false;

    private static final int DESIGN_ACTION_VIEW_TOP_MARGIN = 80;

    private static int AVATAR_OVER_SIZE;
    private static int AVATAR_MAX_SIZE;

    private UUID mContactId;

    private View mBackClickableView;
    private ImageView mAvatarView;
    private View mContentView;
    private View mAdminView;
    private TextView mNameView;
    private TextView mIdentityTextView;
    private CircularImageView mIdentityAvatarView;
    private View mFallbackView;
    private View mAudioClickableView;
    private View mVideoClickableView;
    private ShowMemberListAdapter mMemberListAdapter;

    private ScrollView mScrollView;

    private boolean mUIInitialized = false;
    @Nullable
    private Contact mRoom;
    private boolean mDeletedContact = false;
    private String mContactName;
    private Bitmap mContactAvatar;
    private String mIdentityName;
    private Bitmap mIdentityAvatar;

    private ShowRoomService mShowRoomService;

    private final List<UIRoomMember> mRoomMembers = new ArrayList<>();

    private float mAvatarLastSize = -1;
    private float mScrollPosition = -1;

    private boolean mInitScrollView = false;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String value = intent.getStringExtra(Intents.INTENT_CONTACT_ID);
        if (value != null) {
            mContactId = UUID.fromString(value);
        }

        initViews();

        mShowRoomService = new ShowRoomService(this, getTwinmeContext(), this, mContactId);
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (mScrollView != null && !mInitScrollView) {
            mInitScrollView = true;

            Rect rectangle = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
            int contentHeight = mContentView.getHeight();
            if (contentHeight < rectangle.height()) {
                contentHeight = rectangle.height();
            }

            ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
            layoutParams.height = contentHeight + AVATAR_MAX_SIZE;

            mScrollView.post(() -> mScrollView.scrollBy(0, AVATAR_OVER_SIZE));
        }

        if (mDeletedContact) {
            finish();
        } else {
            updateContact();
        }
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mShowRoomService.dispose();

        super.onDestroy();
    }

    //
    // Implement ShowContactService.Observer methods
    //

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact contact=" + contact);
        }

        mRoom = contact;
        if (mRoom.hasPeer()) {
            setFullscreen();

            mContactName = contact.getName();
            mContactAvatar = avatar;
            if (mContactAvatar == null) {
                mContactAvatar = getDefaultAvatar();
            }
            mIdentityName = contact.getIdentityName();
            if (mIdentityName == null) {
                mIdentityName = getAnonymousName();
            }

            mShowRoomService.getIdentityImage(mRoom, (Bitmap identityAvatar) -> {
                mIdentityAvatar = identityAvatar;

                updateContact();
            });
        } else {
            mFallbackView.setVisibility(View.VISIBLE);
            mBackClickableView.setVisibility(View.GONE);
            mContentView.setVisibility(View.GONE);

            ViewGroup.LayoutParams avatarLayoutParams = mAvatarView.getLayoutParams();
            avatarLayoutParams.width = Design.DISPLAY_WIDTH;
            //noinspection SuspiciousNameCombination
            avatarLayoutParams.height = Design.DISPLAY_WIDTH;
            mAvatarView.requestLayout();

            setStatusBarColor();
            showToolBar(true);

            mContactName = contact.getName();
            mContactAvatar = getTwinmeApplication().getAnonymousAvatar();
            mIdentityName = getTwinmeApplication().getAnonymousName();
            mIdentityAvatar = getTwinmeApplication().getAnonymousAvatar();

            updateContact();
        }
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }

        mFallbackView.setVisibility(View.VISIBLE);
        mBackClickableView.setVisibility(View.GONE);
        mContentView.setVisibility(View.GONE);

        ViewGroup.LayoutParams avatarLayoutParams = mAvatarView.getLayoutParams();
        avatarLayoutParams.width = Design.DISPLAY_WIDTH;
        //noinspection SuspiciousNameCombination
        avatarLayoutParams.height = Design.DISPLAY_WIDTH;
        mAvatarView.requestLayout();

        setStatusBarColor();
        showToolBar(true);
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact contact=" + contact);
        }

        if (!contact.getId().equals(mContactId)) {

            return;
        }

        mRoom = contact;
        if (contact.hasPeer()) {
            mContactName = contact.getName();
            mContactAvatar = avatar;
            if (mContactAvatar == null) {
                mContactAvatar = getDefaultAvatar();
            }
            mIdentityName = contact.getIdentityName();
            if (mIdentityName == null) {
                mIdentityName = getAnonymousName();
            }
            mShowRoomService.getIdentityImage(mRoom, (Bitmap identityAvatar) -> {
                mIdentityAvatar = identityAvatar;

                updateContact();
            });
        } else {
            mFallbackView.setVisibility(View.VISIBLE);
            mBackClickableView.setVisibility(View.GONE);
            mContentView.setVisibility(View.GONE);

            ViewGroup.LayoutParams avatarLayoutParams = mAvatarView.getLayoutParams();
            avatarLayoutParams.width = Design.DISPLAY_WIDTH;
            //noinspection SuspiciousNameCombination
            avatarLayoutParams.height = Design.DISPLAY_WIDTH;
            mAvatarView.requestLayout();

            setStatusBarColor();
            showToolBar(true);

            mContactName = contact.getName();
            mContactAvatar = getTwinmeApplication().getAnonymousAvatar();
            mIdentityName = getTwinmeApplication().getAnonymousName();
            mIdentityAvatar = getTwinmeApplication().getAnonymousAvatar();

            updateContact();
        }
    }

    @Override
    public void onDeleteContact(@NonNull UUID contactId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteContact contactId=" + contactId);
        }

        if (!contactId.equals(mContactId)) {

            return;
        }

        mDeletedContact = true;

        if (mResumed) {
            finish();
        }
    }

    @Override
    public void onGetRoomMembers(@NonNull List<TwincodeOutbound> members, int memberCount) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetRoomMembers members=" + members + " memberCount=" + memberCount);
        }

        for (TwincodeOutbound twincodeOutbound : members) {
            UIRoomMember uiRoomMember = new UIRoomMember(twincodeOutbound, null);
            mRoomMembers.add(uiRoomMember);
        }

        mMemberListAdapter.setMemberCount(memberCount);
        mMemberListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetRoomMemberAvatar(@NonNull TwincodeOutbound twincodeOutbound, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetRoomMemberAvatar twincodeOutbound=" + twincodeOutbound + " avatar=" + avatar);
        }

        for (UIRoomMember uiRoomMember : mRoomMembers) {
            if (twincodeOutbound.getId().equals(uiRoomMember.getTwincodeOutbound().getId())) {
                uiRoomMember.setAvatar(avatar);
                break;
            }
        }

        mMemberListAdapter.notifyDataSetChanged();
    }

    //
    // Private methods
    //
    @SuppressLint({"ClickableViewAccessibility"})
    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.show_room_activity);

        setToolBar(R.id.show_room_activity_tool_bar);
        setTitle(getString(R.string.application_name));
        showToolBar(false);
        showBackButton(true);
        setBackgroundColor(Design.WHITE_COLOR);

        applyInsets(R.id.show_room_activity_layout, -1, -1, Design.WHITE_COLOR, true);

        mAvatarView = findViewById(R.id.show_room_activity_avatar_view);

        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;
        layoutParams.height = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;

        mBackClickableView = findViewById(R.id.show_room_activity_back_clickable_view);
        mBackClickableView.setOnClickListener(view -> onBackClick());

        layoutParams = mBackClickableView.getLayoutParams();
        layoutParams.height = Design.BACK_CLICKABLE_VIEW_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mBackClickableView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BACK_CLICKABLE_VIEW_LEFT_MARGIN;
        marginLayoutParams.topMargin = Design.BACK_CLICKABLE_VIEW_TOP_MARGIN;

        RoundedView backRoundedView = findViewById(R.id.show_room_activity_back_rounded_view);
        backRoundedView.setColor(Design.BACK_VIEW_COLOR);

        mContentView = findViewById(R.id.show_room_activity_content_view);
        mContentView.setY(AVATAR_MAX_SIZE - Design.ACTION_VIEW_MIN_MARGIN);

        setBackground(mContentView);

        mScrollView = findViewById(R.id.show_room_activity_content_scroll_view);
        ViewTreeObserver viewTreeObserver = mScrollView.getViewTreeObserver();
        viewTreeObserver.addOnScrollChangedListener(() -> {
            if (mScrollPosition == -1) {
                mScrollPosition = AVATAR_OVER_SIZE;
            }

            float delta = mScrollPosition - mScrollView.getScrollY();
            updateAvatarSize(delta);
            mScrollPosition = mScrollView.getScrollY();
        });

        View slideMarkView = findViewById(R.id.show_room_activity_slide_mark_view);
        layoutParams = slideMarkView.getLayoutParams();
        layoutParams.height = Design.SLIDE_MARK_HEIGHT;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.rgb(244, 244, 244));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        slideMarkView.setBackground(gradientDrawable);

        float corner = ((float)Design.SLIDE_MARK_HEIGHT / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) slideMarkView.getLayoutParams();
        marginLayoutParams.topMargin = Design.SLIDE_MARK_TOP_MARGIN;

        mNameView = findViewById(R.id.show_room_activity_name_view);
        Design.updateTextFont(mNameView, Design.FONT_BOLD44);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View headerView = findViewById(R.id.show_room_activity_content_header_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) headerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.HEADER_VIEW_TOP_MARGIN;

        View editClickableView = findViewById(R.id.show_room_activity_edit_clickable_view);
        editClickableView.setOnClickListener(view -> onEditClick());

        layoutParams = editClickableView.getLayoutParams();
        layoutParams.height = Design.EDIT_CLICKABLE_VIEW_HEIGHT;

        ImageView editImageView = findViewById(R.id.show_room_activity_edit_image_view);
        editImageView.setColorFilter(Design.getMainStyle());

        TextView memberTextView = findViewById(R.id.show_room_activity_members_title_view);
        Design.updateTextFont(memberTextView, Design.FONT_BOLD26);
        memberTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) memberTextView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        View memberView = findViewById(R.id.show_room_activity_list_member_layout_view);
        layoutParams = memberView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) memberView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        LinearLayoutManager selectedUIContactLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView memberRecyclerView = findViewById(R.id.show_room_activity_list_member_view);
        memberRecyclerView.setLayoutManager(selectedUIContactLinearLayoutManager);
        memberRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        memberRecyclerView.setItemAnimator(null);
        memberRecyclerView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                onListMembersClick();
            }
            return false;
        });

        mMemberListAdapter = new ShowMemberListAdapter(this, mRoomMembers, 0);
        memberRecyclerView.setAdapter(mMemberListAdapter);

        mAdminView = findViewById(R.id.show_room_activity_admin_view);
        mAdminView.setOnClickListener(view -> onAdminClick());

        layoutParams = mAdminView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        TextView adminTitleView = findViewById(R.id.show_room_activity_admin_text_view);
        Design.updateTextFont(adminTitleView, Design.FONT_REGULAR34);
        adminTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View actionView = findViewById(R.id.show_room_activity_action_view);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) actionView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ACTION_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        View chatClickableView = findViewById(R.id.show_room_activity_chat_clickable_view);
        chatClickableView.setOnClickListener(view -> onChatClick());

        layoutParams = chatClickableView.getLayoutParams();
        layoutParams.height = Design.ACTION_CLICKABLE_VIEW_HEIGHT;

        RoundedView roundedChatView = findViewById(R.id.show_room_activity_chat_rounded_view);
        roundedChatView.setColor(Design.CHAT_COLOR);

        TextView chatTextView = findViewById(R.id.show_room_activity_chat_text_view);
        Design.updateTextFont(chatTextView, Design.FONT_REGULAR28);
        chatTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mVideoClickableView = findViewById(R.id.show_room_activity_video_clickable_view);
        mVideoClickableView.setOnClickListener(view -> onVideoClick());

        layoutParams = mVideoClickableView.getLayoutParams();
        layoutParams.height = Design.ACTION_CLICKABLE_VIEW_HEIGHT;

        RoundedView roundedVideoView = findViewById(R.id.show_room_activity_video_rounded_view);
        roundedVideoView.setColor(Design.VIDEO_CALL_COLOR);

        TextView videoTextView = findViewById(R.id.show_room_activity_video_text_view);
        Design.updateTextFont(videoTextView, Design.FONT_REGULAR28);
        videoTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mAudioClickableView = findViewById(R.id.show_room_activity_audio_clickable_view);
        mAudioClickableView.setOnClickListener(view -> onAudioClick());

        layoutParams = mAudioClickableView.getLayoutParams();
        layoutParams.height = Design.ACTION_CLICKABLE_VIEW_HEIGHT;

        RoundedView roundedAudioView = findViewById(R.id.show_room_activity_audio_rounded_view);
        roundedAudioView.setColor(Design.AUDIO_CALL_COLOR);

        TextView audioTextView = findViewById(R.id.show_room_activity_audio_text_view);
        Design.updateTextFont(audioTextView, Design.FONT_REGULAR28);
        audioTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View identityView = findViewById(R.id.show_room_activity_identity_view);
        identityView.setOnClickListener(view -> onEditIdentityClick());

        layoutParams = identityView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) identityView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        TextView identityTitleView = findViewById(R.id.show_room_activity_identity_title_view);
        Design.updateTextFont(identityTitleView, Design.FONT_BOLD26);
        identityTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) identityTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        mIdentityTextView = findViewById(R.id.show_room_activity_identity_text_view);
        Design.updateTextFont(mIdentityTextView, Design.FONT_REGULAR34);
        mIdentityTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mIdentityAvatarView = findViewById(R.id.show_room_activity_identity_avatar_view);

        TextView lastCallsTitleView = findViewById(R.id.show_room_activity_last_calls_title_view);
        Design.updateTextFont(lastCallsTitleView, Design.FONT_BOLD26);
        lastCallsTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) lastCallsTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        View lastCallsView = findViewById(R.id.show_room_activity_last_calls_view);
        layoutParams = lastCallsView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) lastCallsView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        lastCallsView.setOnClickListener(view -> onLastCallsClick());

        TextView lastCallsTextView = findViewById(R.id.show_room_activity_last_calls_text_view);
        Design.updateTextFont(lastCallsTextView, Design.FONT_REGULAR34);
        lastCallsTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        TextView conversationsTitleView = findViewById(R.id.show_room_activity_conversations_title_view);
        Design.updateTextFont(conversationsTitleView, Design.FONT_BOLD28);
        conversationsTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) conversationsTitleView.getLayoutParams();
        marginLayoutParams.topMargin = Design.TITLE_IDENTITY_TOP_MARGIN;

        View filesView = findViewById(R.id.show_room_activity_files_view);
        layoutParams = filesView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) filesView.getLayoutParams();
        marginLayoutParams.topMargin = Design.IDENTITY_VIEW_TOP_MARGIN;

        filesView.setOnClickListener(view -> onConversationFilesClick());

        TextView filesTextView = findViewById(R.id.show_room_activity_files_text_view);
        Design.updateTextFont(filesTextView, Design.FONT_REGULAR34);
        filesTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ImageView filesImageView = findViewById(R.id.show_room_activity_files_image_view);
        filesImageView.setColorFilter(Design.SHOW_ICON_COLOR);

        View exportView = findViewById(R.id.show_room_activity_export_view);
        layoutParams = exportView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        exportView.setOnClickListener(view -> onExportClick());

        TextView exportTextView = findViewById(R.id.show_room_activity_export_text_view);
        Design.updateTextFont(exportTextView, Design.FONT_REGULAR34);
        exportTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ImageView exportImageView = findViewById(R.id.show_room_activity_export_image_view);
        exportImageView.setColorFilter(Design.SHOW_ICON_COLOR);

        View cleanView = findViewById(R.id.show_room_activity_clean_view);
        layoutParams = cleanView.getLayoutParams();
        layoutParams.height = Design.SECTION_HEIGHT;

        cleanView.setOnClickListener(view -> onCleanClick());

        ViewTreeObserver cleanViewTreeObserver = cleanView.getViewTreeObserver();
        cleanViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = cleanView.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalLayoutListener(this);

                Rect rectangle = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
                int contentHeight = (int) (cleanView.getHeight() + cleanView.getY());
                if (contentHeight < rectangle.height()) {
                    contentHeight = rectangle.height();
                }

                ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
                layoutParams.height = contentHeight + AVATAR_MAX_SIZE;
            }
        });

        TextView cleanTextView = findViewById(R.id.show_room_activity_clean_text_view);
        Design.updateTextFont(cleanTextView, Design.FONT_REGULAR34);
        cleanTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        ImageView cleanImageView = findViewById(R.id.show_room_activity_clean_image_view);
        cleanImageView.setColorFilter(Design.SHOW_ICON_COLOR);

        mFallbackView = findViewById(R.id.show_room_activity_fallback_view);
        mFallbackView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        View removeView = findViewById(R.id.show_room_activity_remove_view);
        removeView.setOnClickListener(v -> onRemoveClick());

        TextView removeTextView = findViewById(R.id.show_room_activity_remove_text_view);
        Design.updateTextFont(removeTextView, Design.FONT_REGULAR44);
        removeTextView.setTextColor(Color.RED);

        mUIInitialized = true;
    }

    private void onEditClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditClick");
        }

        if (mRoom != null) {
            startActivity(EditContactActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
        }
    }

    private void onChatClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onChatClick");
        }

        startActivity(ConversationActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
    }

    private void onVideoClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onVideoClick");
        }

        if (getTwinmeApplication().inCallInfo() == null && mRoom != null && mRoom.getCapabilities().hasVideo()) {
            Intent intent = new Intent(this, CallActivity.class);
            intent.putExtra(Intents.INTENT_CONTACT_ID, mContactId.toString());
            intent.putExtra(Intents.INTENT_CALL_MODE, CallStatus.OUTGOING_VIDEO_CALL);

            startActivity(intent);
        } else if (mRoom != null && !mRoom.getCapabilities().hasVideo()) {
            Toast.makeText(this, R.string.application_not_authorized_operation, Toast.LENGTH_SHORT).show();
        }
    }

    private void onAudioClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAudioClick");
        }

        if (getTwinmeApplication().inCallInfo() == null && mRoom != null && mRoom.getCapabilities().hasAudio()) {
            Intent intent = new Intent(this, CallActivity.class);
            intent.putExtra(Intents.INTENT_CONTACT_ID, mContactId.toString());
            intent.putExtra(Intents.INTENT_CALL_MODE, CallStatus.OUTGOING_CALL);

            startActivity(intent);
        } else if (mRoom != null && !mRoom.getCapabilities().hasAudio()) {
            Toast.makeText(this, R.string.application_not_authorized_operation, Toast.LENGTH_SHORT).show();
        }
    }

    private void onAdminClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAdminClick");
        }

        if (mRoom != null && mRoom.getCapabilities().hasAdmin()) {
            startActivity(AdminRoomActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
        }
    }

    private void onEditIdentityClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditIdentityClick");
        }

        if (mRoom != null && mRoom.hasPrivatePeer()) {
            startActivity(EditIdentityActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
        }
    }

    private void onLastCallsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLastCallsClick");
        }

        startActivity(LastCallsActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
    }

    private void onListMembersClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onListMembersClick");
        }

        startActivity(RoomMembersActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
    }

    private void onRemoveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRemoveClick");
        }

        if (mRoom != null) {
            mShowRoomService.deleteRoom(mRoom);
        }
    }

    private void onExportClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onExportClick");
        }

        if (mRoom != null && mContactId != null) {
            startActivity(ExportActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
        }
    }

    private void onCleanClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCleanClick");
        }

        if (mRoom != null && mContactId != null) {
            startActivity(TypeCleanUpActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
        }
    }

    private void onConversationFilesClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConversationFilesClick");
        }

        if (mRoom != null && mContactId != null) {
            startActivity(ConversationFilesActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
        }
    }

    private void updateContact() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateContact");
        }

        if (mUIInitialized && mResumed && mRoom != null) {
            mAvatarView.setImageBitmap(mContactAvatar);
            mIdentityTextView.setText(mIdentityName);
            mNameView.setText(mContactName);
            mIdentityAvatarView.setImage(this, null, new CircularImageDescriptor(mIdentityAvatar, 0.5f, 0.5f, 0.5f));

            if (getTwinmeApplication().inCallInfo() != null || !mRoom.getCapabilities().hasAudio()) {
                mAudioClickableView.setAlpha(0.5f);
            } else {
                mAudioClickableView.setAlpha(1f);
            }

            if (getTwinmeApplication().inCallInfo() != null || !mRoom.getCapabilities().hasVideo()) {
                mVideoClickableView.setAlpha(0.5f);
            } else {
                mVideoClickableView.setAlpha(1f);
            }

            if (mRoom.getCapabilities().hasAdmin()) {
                mAdminView.setVisibility(View.VISIBLE);
            } else {
                mAdminView.setVisibility(View.GONE);
            }
        }
    }

    private void updateAvatarSize(float deltaY) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateAvatarSize: " + deltaY);
        }

        if (mAvatarLastSize == -1) {
            mAvatarLastSize = AVATAR_MAX_SIZE - AVATAR_OVER_SIZE;
        }

        float avatarViewSize = mAvatarLastSize + deltaY;

        if (avatarViewSize < Design.DISPLAY_WIDTH) {
            avatarViewSize = Design.DISPLAY_WIDTH;
        } else if (avatarViewSize > AVATAR_MAX_SIZE) {
            avatarViewSize = AVATAR_MAX_SIZE;
        }

        if (avatarViewSize != mAvatarLastSize) {
            ViewGroup.LayoutParams avatarLayoutParams = mAvatarView.getLayoutParams();
            avatarLayoutParams.width = (int) avatarViewSize;
            avatarLayoutParams.height = (int) avatarViewSize;
            mAvatarView.requestLayout();

            mAvatarLastSize = avatarViewSize;
        }
    }

    public void updateInCall() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateInCall");
        }

        if (getTwinmeApplication().inCallInfo() != null || (mRoom != null && !mRoom.getCapabilities().hasAudio())) {
            mAudioClickableView.setAlpha(0.5f);
        } else {
            mAudioClickableView.setAlpha(1f);
        }

        if (getTwinmeApplication().inCallInfo() != null || (mRoom != null && !mRoom.getCapabilities().hasVideo())) {
            mVideoClickableView.setAlpha(0.5f);
        } else {
            mVideoClickableView.setAlpha(1f);
        }
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        AVATAR_OVER_SIZE = (int) (Design.AVATAR_OVER_WIDTH * Design.WIDTH_RATIO);
        AVATAR_MAX_SIZE = Design.DISPLAY_WIDTH + (AVATAR_OVER_SIZE * 2);
    }
}
