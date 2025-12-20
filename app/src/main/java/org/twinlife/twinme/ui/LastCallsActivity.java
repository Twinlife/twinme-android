/*
 *  Copyright (c) 2020-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.CallDescriptor;
import org.twinlife.twinlife.ConversationService.ClearMode;
import org.twinlife.twinlife.ConversationService.Conversation;
import org.twinlife.twinlife.ConversationService.DescriptorId;
import org.twinlife.twinme.calls.CallStatus;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.CallsService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.callActivity.CallActivity;
import org.twinlife.twinme.ui.calls.CallAgainConfirmView;
import org.twinlife.twinme.ui.calls.LastCallsAdapter;
import org.twinlife.twinme.ui.calls.UICall;
import org.twinlife.twinme.ui.contacts.DeleteConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumFeatureConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.SwipeItemTouchHelper;
import org.twinlife.twinme.utils.SwipeItemTouchHelper.OnSwipeItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LastCallsActivity extends AbstractTwinmeActivity implements CallsService.Observer, ViewTreeObserver.OnGlobalLayoutListener {
    private static final String LOG_TAG = "LastCallsActivity";
    private static final boolean DEBUG = false;

    private LastCallsAdapter mCallsListAdapter;
    private RadioButton mAudioRadioButton;
    private RadioButton mVideoRadioButton;
    private RecyclerView mCallsRecyclerView;
    private ImageView mNoCallImageView;
    private TextView mNoCallTitleView;
    private TextView mNoCallTextView;
    private final ArrayList<CallDescriptor> mAllCalls = new ArrayList<>();
    private final ArrayList<UICall> mFilteredCalls = new ArrayList<>();
    private UIContact mUIContact;
    private boolean mVideoCalls = false;
    private boolean mResetAllCalls = false;

    private CallsService mCallsService;

    @Nullable
    private Menu mMenu;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        initViews();
    }

    //
    // Override Activity methods
    //


    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();

        mCallsService.dispose();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onGlobalLayout() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGlobalLayout");
        }

        int maxWidth = mAudioRadioButton.getWidth();

        if (mVideoRadioButton.getWidth() > maxWidth) {
            maxWidth = mVideoRadioButton.getWidth();
        }

        mAudioRadioButton.setWidth(maxWidth);
        mVideoRadioButton.setWidth(maxWidth);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu);
        }

        super.onCreateOptionsMenu(menu);

        mMenu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.calls_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.reset_calls_action);

        ImageView imageView = (ImageView) menuItem.getActionView();

        if (imageView != null) {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.action_bar_delete, null));
            imageView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);
            imageView.setOnClickListener(view -> onResetClick());
        }

        // Update to take into account the menu.
        updateCalls();

        return true;
    }

    //
    // Implement CallsService.Observer methods
    //

    @Override
    public void onGetContacts(@NonNull List<Contact> contacts) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContacts: contacts=" + contacts);
        }

    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: contact=" + contact);
        }

        mUIContact = new UIContact(getTwinmeApplication(), contact, avatar);
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }
    }

    @Override
    public void onGetGroup(@NonNull Group contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup: group=" + contact);
        }

        mUIContact = new UIContact(getTwinmeApplication(), contact, avatar);
    }

    @Override
    public void onGetGroupNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroupNotFound");
        }
    }

    @Override
    public void onCreateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateContact: contact=" + contact);
        }

    }

    @Override
    public void onDeleteContact(@NonNull UUID contactId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteContact: contactId=" + contactId);
        }

        if (mUIContact.getContact().getId() == contactId) {
            finish();
        }

        updateCalls();
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: contact=" + contact);
        }

        if (mUIContact != null) {
            mUIContact.setAvatar(avatar);
        }
    }

    @Override
    public void onSetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpace: space=" + space);
        }

    }

    @Override
    public void onGetDescriptors(@NonNull List<CallDescriptor> descriptors) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDescriptors: descriptors=" + descriptors);
        }

        for (CallDescriptor descriptor : descriptors) {
            if (!mAllCalls.contains(descriptor)) {
                mAllCalls.add(descriptor);
            }
        }

        if (!mCallsService.isGetDescriptorDone()) {
            mCallsService.getPreviousDescriptors();
        }

        if (mResetAllCalls) {
            int count = mAllCalls.size();
            if (count == 0) {
                mResetAllCalls = false;
                hideProgressIndicator();

                if (mMenu != null) {
                    MenuItem resetMenuItem = mMenu.findItem(R.id.reset_calls_action);
                    resetMenuItem.setEnabled(false);
                    if (resetMenuItem.getActionView() != null) {
                        resetMenuItem.getActionView().setAlpha(0.5f);
                    }
                }
                updateCalls();
            } else {
                for (int i = 0; i < mAllCalls.size(); i++) {
                    CallDescriptor callDescriptor = mAllCalls.get(i);
                    mCallsService.deleteCallDescriptor(callDescriptor);
                }
            }
        } else {
            updateCalls();
        }
    }

    @Override
    public void onAddDescriptor(@NonNull CallDescriptor descriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddDescriptor: descriptor=" + descriptor);
        }

        if (!mAllCalls.contains(descriptor)) {
            mAllCalls.add(0, descriptor);
        }

        updateCalls();
    }

    @Override
    public void onUpdateDescriptor(@NonNull CallDescriptor descriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateDescriptor: descriptor=" + descriptor);
        }

        boolean isUpdated = false;

        for (int i = 0; i < mAllCalls.size(); i++) {
            CallDescriptor callDescriptor = mAllCalls.get(i);
            if (callDescriptor.getTwincodeOutboundId().equals(descriptor.getTwincodeOutboundId()) && callDescriptor.getSequenceId() == descriptor.getSequenceId()) {
                mAllCalls.set(i, callDescriptor);
                isUpdated = true;
                break;
            }
        }

        if (!isUpdated) {
            mAllCalls.add(0, descriptor);
        }

        updateCalls();
    }

    @Override
    public void onDeleteDescriptors(@NonNull Set<DescriptorId> descriptorIdSet) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteDescriptors: descriptorIdSet=" + descriptorIdSet.size());
        }

        for (CallDescriptor callDescriptor : mAllCalls) {
            if (descriptorIdSet.remove(callDescriptor.getDescriptorId())) {
                mAllCalls.remove(callDescriptor);
                if (descriptorIdSet.isEmpty()) {
                    break;
                }
            }
        }

        if (mResetAllCalls && mAllCalls.isEmpty()) {
            if (mCallsService.isGetDescriptorDone()) {
                mResetAllCalls = false;
                hideProgressIndicator();

                if (mMenu != null) {
                    MenuItem resetMenuItem = mMenu.findItem(R.id.reset_calls_action);
                    resetMenuItem.setEnabled(false);
                    if (resetMenuItem.getActionView() != null) {
                        resetMenuItem.getActionView().setAlpha(0.5f);
                    }
                }
                updateCalls();
            } else {
                mCallsService.getPreviousDescriptors();
            }
        } else {
            updateCalls();
        }
    }

    @Override
    public void onResetConversation(@NonNull Conversation conversation, @NonNull ClearMode clearMode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResetConversation: conversation=" + conversation + " clearMode=" + clearMode);
        }

        if (clearMode == ClearMode.CLEAR_MEDIA) {
            return;
        }

        mAllCalls.clear();
        updateCalls();
    }

    @Override
    public void onGetCallReceivers(@NonNull List<CallReceiver> callReceivers) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceivers: callReceivers=" + callReceivers);
        }

    }

    @Override
    public void onCreateCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateCallReceiver: callReceiver=" + callReceiver);
        }
    }

    @Override
    public void onUpdateCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateCallReceiver: callReceiver=" + callReceiver);
        }
    }

    @Override
    public void onGetCallReceiver(@NonNull CallReceiver callReceiver, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceiver: callReceiver=" + callReceiver);
        }

        mUIContact = new UIContact(getTwinmeApplication(), callReceiver, avatar);
    }

    @Override
    public void onDeleteCallReceiver(@NonNull UUID callReceiverId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteCallReceiver: callReceiverId=" + callReceiverId);
        }
    }

    @Override
    public void onGetGroupMembers(@NonNull List<ConversationService.GroupMemberConversation> groupMembers) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroupMembers: groupMembers=" + groupMembers);
        }
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.last_calls_activity);

        setStatusBarColor();
        setTitle("");
        setToolBar(R.id.last_calls_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        applyInsets(R.id.last_calls_activity_layout, R.id.last_calls_activity_tool_bar, R.id.last_calls_activity_list_view, Design.TOOLBAR_COLOR, false);

        RadioGroup callRadioGroup = findViewById(R.id.last_calls_tool_bar_radio_group);

        mAudioRadioButton = findViewById(R.id.last_calls_tool_bar_audio_radio);
        Design.updateTextFont(mAudioRadioButton, Design.FONT_REGULAR32);

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        Design.getMainStyle(),
                        Color.WHITE
                }
        );

        mAudioRadioButton.setTextColor(colorStateList);

        mVideoRadioButton = findViewById(R.id.last_calls_tool_bar_video_radio);
        mVideoRadioButton.setTextColor(colorStateList);
        Design.updateTextFont(mVideoRadioButton, Design.FONT_REGULAR32);

        if (CommonUtils.isLayoutDirectionRTL()) {
            mAudioRadioButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.segmented_control_right, null));
            mVideoRadioButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.segmented_control_left, null));
        }

        ViewTreeObserver viewTreeObserver = mVideoRadioButton.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(this);

        callRadioGroup.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (checkedId == R.id.last_calls_tool_bar_audio_radio) {
                mVideoCalls = false;
            } else if (checkedId == R.id.last_calls_tool_bar_video_radio) {
                mVideoCalls = true;
            }

            updateCalls();
        });

        LastCallsAdapter.OnLastCallClickListener onCallClickListener = position -> {
            if (getTwinmeApplication().inCallInfo() == null) {
                UICall uiCall = mFilteredCalls.get(position);
                onUICallClick(uiCall);
            }
        };

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mCallsRecyclerView = findViewById(R.id.last_calls_activity_list_view);
        mCallsRecyclerView.setLayoutManager(linearLayoutManager);
        mCallsRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mCallsRecyclerView.setItemAnimator(null);
        mCallsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        OnSwipeItemClickListener onSwipeItemClickListener = new OnSwipeItemClickListener() {
            @Override
            public void onLeftActionClick(int adapterPosition) {

            }

            @Override
            public void onRightActionClick(int adapterPosition) {

                onUICallDeleteClick(mFilteredCalls.get(adapterPosition));
            }

            @Override
            public void onOtherActionClick(int adapterPosition) {

            }
        };
        SwipeItemTouchHelper swipeItemTouchHelper = new SwipeItemTouchHelper(mCallsRecyclerView, null, SwipeItemTouchHelper.ButtonType.DELETE, onSwipeItemClickListener);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeItemTouchHelper);
        itemTouchHelper.attachToRecyclerView(mCallsRecyclerView);

        mNoCallImageView = findViewById(R.id.last_calls_activity_no_call_image_view);

        mNoCallTitleView = findViewById(R.id.last_calls_activity_no_call_title_view);
        mNoCallTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        Design.updateTextFont(mNoCallTitleView, Design.FONT_MEDIUM28);

        mNoCallTextView = findViewById(R.id.last_calls_activity_no_call_text_view);
        mNoCallTextView.setTextColor(Design.FONT_COLOR_DESCRIPTION);
        Design.updateTextFont(mNoCallTextView, Design.FONT_MEDIUM28);

        Intent intent = getIntent();
        String contactId = intent.getStringExtra(Intents.INTENT_CONTACT_ID);
        String groupId = intent.getStringExtra(Intents.INTENT_GROUP_ID);
        UUID originatorId = null;
        Originator.Type originatorType = null;
        if (contactId != null) {
            originatorId = UUID.fromString(contactId);
            originatorType = Originator.Type.CONTACT;
        } else if (groupId != null) {
            originatorId = UUID.fromString(groupId);
            originatorType = Originator.Type.GROUP;
        } else {
            String callReceiverdId = intent.getStringExtra(Intents.INTENT_CALL_RECEIVER_ID);
            if (callReceiverdId != null) {
                originatorId = UUID.fromString(callReceiverdId);
                originatorType = Originator.Type.CALL_RECEIVER;
            }
        }

        // Setup the service after the view is initialized but before the adapter.
        mCallsService = new CallsService(this, getTwinmeContext(), this, originatorId, originatorType);

        mCallsListAdapter = new LastCallsAdapter(this, mCallsService, mFilteredCalls, onCallClickListener);
        mCallsRecyclerView.setAdapter(mCallsListAdapter);
    }

    private void updateCalls() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateCalls");
        }

        mFilteredCalls.clear();

        for (CallDescriptor callDescriptor : mAllCalls) {
            if (callDescriptor.isVideo() == mVideoCalls) {
                UICall uiCall = new UICall(mUIContact, callDescriptor);
                mFilteredCalls.add(uiCall);
            }
        }

        mCallsListAdapter.notifyDataSetChanged();

        if (mFilteredCalls.isEmpty() && mCallsService.isGetDescriptorDone()) {
            mNoCallImageView.setVisibility(View.VISIBLE);
            mNoCallTitleView.setVisibility(View.VISIBLE);
            mNoCallTextView.setVisibility(View.VISIBLE);
        } else {
            mNoCallImageView.setVisibility(View.GONE);
            mNoCallTitleView.setVisibility(View.GONE);
            mNoCallTextView.setVisibility(View.GONE);
        }

        if (mMenu != null) {
            MenuItem resetMenuItem = mMenu.findItem(R.id.reset_calls_action);
            if (mAllCalls.isEmpty()) {
                resetMenuItem.setEnabled(false);
                resetMenuItem.getActionView().setAlpha(0.5f);
            } else {
                resetMenuItem.setEnabled(true);
                resetMenuItem.getActionView().setAlpha(1f);
            }
        }
    }

    private void onResetClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResetClick");
        }

        if (mAllCalls.isEmpty()) {
            return;
        }

        ViewGroup viewGroup = findViewById(R.id.last_calls_activity_layout);
        DeleteConfirmView deleteConfirmView = new DeleteConfirmView(this, null);
        deleteConfirmView.setAvatar(mUIContact.getAvatar(), mUIContact.getAvatar() == null || mUIContact.getAvatar().equals(getTwinmeApplication().getDefaultGroupAvatar()));

        String message = getString(R.string.application_operation_irreversible) + "\n\n"  + getString(R.string.calls_fragment_reset);
        deleteConfirmView.setMessage(message);
        deleteConfirmView.setConfirmTitle(getString(R.string.calls_fragment_reset_title));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                resetCalls();
                deleteConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                deleteConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                deleteConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(deleteConfirmView);
                setStatusBarColor();
            }
        };
        deleteConfirmView.setObserver(observer);

        viewGroup.addView(deleteConfirmView);
        deleteConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void resetCalls() {
        if (DEBUG) {
            Log.d(LOG_TAG, "resetCalls");
        }

        showProgressIndicator();

        mResetAllCalls = true;

        if (mMenu != null) {
            MenuItem resetMenuItem = mMenu.findItem(R.id.reset_calls_action);
            resetMenuItem.setEnabled(false);
            resetMenuItem.getActionView().setAlpha(0.5f);
        }

        for (int i = 0; i < mAllCalls.size(); i++) {
            CallDescriptor callDescriptor = mAllCalls.get(i);
            mCallsService.deleteCallDescriptor(callDescriptor);
        }
    }

    private void onUICallClick(UICall uiCall) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUICallClick: uiCall=" + uiCall);
        }

        if (uiCall != null && (mUIContact.getContact().getType() == Originator.Type.CONTACT || mUIContact.getContact().getType() == Originator.Type.GROUP)) {
            CallDescriptor callDescriptor = uiCall.getLastCallDescriptor();

            if (mUIContact.getContact().getType() == Originator.Type.CONTACT) {
                Contact contact = (Contact) mUIContact.getContact();
                if ((callDescriptor.isVideo() && contact.getCapabilities().hasVideo()) || (!callDescriptor.isVideo() && contact.getCapabilities().hasAudio())) {
                    showCallAgainConfirmView(mUIContact, false, callDescriptor.isVideo());
                }
            } else if (mUIContact.getContact().getType() == Originator.Type.GROUP) {
                ViewGroup viewGroup = findViewById(R.id.last_calls_activity_layout);

                PremiumFeatureConfirmView premiumFeatureConfirmView = new PremiumFeatureConfirmView(this, null);
                premiumFeatureConfirmView.initWithPremiumFeature(new UIPremiumFeature(this, UIPremiumFeature.FeatureType.GROUP_CALL));

                AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
                    @Override
                    public void onConfirmClick() {
                        premiumFeatureConfirmView.redirectStore();
                    }

                    @Override
                    public void onCancelClick() {
                        premiumFeatureConfirmView.animationCloseConfirmView();
                    }

                    @Override
                    public void onDismissClick() {
                        premiumFeatureConfirmView.animationCloseConfirmView();
                    }

                    @Override
                    public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                        viewGroup.removeView(premiumFeatureConfirmView);
                        setStatusBarColor();
                    }
                };
                premiumFeatureConfirmView.setObserver(observer);

                viewGroup.addView(premiumFeatureConfirmView);
                premiumFeatureConfirmView.show();

                int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
                setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
            }
        }
    }

    private void showCallAgainConfirmView(UIContact uiContact, boolean isGroup, boolean isVideoCall) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showCallAgainConfirmView: uiContact=" + uiContact + " isGroup=" + isGroup + " isVideoCall=" + isVideoCall);
        }

        ViewGroup viewGroup = findViewById(R.id.last_calls_activity_layout);

        CallAgainConfirmView callAgainConfirmView = new CallAgainConfirmView(this, null);
        callAgainConfirmView.setTitle(uiContact.getName());
        callAgainConfirmView.setAvatar(uiContact.getAvatar(), uiContact.getAvatar() == null || uiContact.getAvatar().equals(getTwinmeApplication().getDefaultGroupAvatar()));

        if (isVideoCall) {
            callAgainConfirmView.setMessage(getString(R.string.conversation_activity_video_call));
            callAgainConfirmView.setIcon(R.drawable.video_call);
        } else {
            callAgainConfirmView.setMessage(getString(R.string.conversation_activity_audio_call));
            callAgainConfirmView.setIcon(R.drawable.audio_call);
        }

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {

                Intent intent = new Intent(getApplicationContext(), CallActivity.class);

                if (isGroup) {
                    intent.putExtra(Intents.INTENT_GROUP_ID, uiContact.getContact().getId().toString());
                } else {
                    intent.putExtra(Intents.INTENT_CONTACT_ID, uiContact.getContact().getId().toString());
                }

                if (isVideoCall) {
                    intent.putExtra(Intents.INTENT_CALL_MODE, CallStatus.OUTGOING_VIDEO_CALL);
                } else {
                    intent.putExtra(Intents.INTENT_CALL_MODE, CallStatus.OUTGOING_CALL);
                }

                startActivity(intent);
            }

            @Override
            public void onCancelClick() {
                callAgainConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                callAgainConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(callAgainConfirmView);
                setStatusBarColor();
            }
        };
        callAgainConfirmView.setObserver(observer);
        viewGroup.addView(callAgainConfirmView);
        callAgainConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void onUICallDeleteClick(UICall uiCall) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUICallDeleteClick: uiCall=" + uiCall);
        }

        if (uiCall != null && uiCall.getContact() != null && uiCall.getContact().getTwincodeOutboundId() != null) {
            CallDescriptor callDescriptor = uiCall.getLastCallDescriptor();
            mCallsService.deleteCallDescriptor(callDescriptor);
        }
    }

    @Override
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        super.updateColor();

        setToolBar(R.id.last_calls_activity_tool_bar);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setStatusBarColor();
        applyInsets(R.id.last_calls_activity_layout, R.id.last_calls_activity_tool_bar, R.id.last_calls_activity_list_view, Design.TOOLBAR_COLOR, false);

        mCallsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        mNoCallTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mNoCallTextView.setTextColor(Design.FONT_COLOR_DESCRIPTION);
    }

    @Override
    public void updateFont() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFont");
        }

        super.updateFont();

        Design.updateTextFont(mAudioRadioButton, Design.FONT_REGULAR32);
        Design.updateTextFont(mVideoRadioButton, Design.FONT_REGULAR32);
        Design.updateTextFont(mNoCallTitleView, Design.FONT_MEDIUM34);
        Design.updateTextFont(mNoCallTextView, Design.FONT_MEDIUM28);
    }
}
