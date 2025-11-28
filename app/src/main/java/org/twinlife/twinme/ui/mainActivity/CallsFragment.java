/*
 *  Copyright (c) 2019-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.MenuProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.CallDescriptor;
import org.twinlife.twinlife.ConversationService.ClearMode;
import org.twinlife.twinlife.ConversationService.Conversation;
import org.twinlife.twinlife.ConversationService.DescriptorId;
import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.twinme.calls.CallStatus;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.CallsService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.callActivity.CallActivity;
import org.twinlife.twinme.ui.calls.CallAgainConfirmView;
import org.twinlife.twinme.ui.calls.CallsAdapter;
import org.twinlife.twinme.ui.calls.CallsAdapter.OnCallClickListener;
import org.twinlife.twinme.ui.calls.UICall;
import org.twinlife.twinme.ui.contacts.DeleteConfirmView;
import org.twinlife.twinme.ui.externalCallActivity.OnboardingExternalCallActivity;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumFeatureConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.ui.users.UIOriginator;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.SwipeItemTouchHelper;
import org.twinlife.twinme.utils.SwipeItemTouchHelper.OnSwipeItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CallsFragment extends TabbarFragment implements CallsService.Observer, ViewTreeObserver.OnGlobalLayoutListener {
    private static final String LOG_TAG = "CallsFragment";
    private static final boolean DEBUG = false;

    private static final float DESIGN_PLACEHOLDER_MARGIN = 286f;

    protected static final int REQUEST_EXTERNAL_CALL_ONBOARDING = 1001;

    private RecyclerView mCallsRecyclerView;
    private ImageView mNoCallImageView;
    private TextView mNoCallTitleView;
    private TextView mNoCallTextView;
    private CallsAdapter mCallsListAdapter;

    private RadioGroup mCallRadioGroup;
    private RadioButton mAllRadioButton;
    private RadioButton mMissedRadioButton;
    private ImageView mResetCallsImageView;

    private final ArrayList<CallDescriptor> mAllCalls = new ArrayList<>();
    private final ArrayList<UICall> mFilteredCalls = new ArrayList<>();
    private final Map<UUID, UIContact> mUIContacts = new HashMap<>();
    private UICall mUICall;
    private boolean mOnlyMissedCalls;
    private boolean mResetAllCalls = false;
    @Nullable
    private Menu mMenu;

    private CallsService mCallsService;

    private boolean mUIInitialized = false;

    // Default constructor is required by Android for proper activity restoration.
    public CallsFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateView: inflater=" + inflater + " container=" + container + " savedInstanceState=" + savedInstanceState);
        }

        View view = inflater.inflate(R.layout.calls_fragment, container, false);
        initViews(view);
        addMenu();

        return view;
    }

    //
    // Override Fragment methods
    //

    @Override
    public void onAttach(@NonNull Context context) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAttach");
        }

        super.onAttach(context);

        if (mTwinmeActivity != null) {
            mOnlyMissedCalls = mTwinmeActivity.getOnlyMissedCalls();
        }
    }

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        updateCalls();
        updateColor();
        updateFont();

        if (mCallRadioGroup != null) {
            mCallRadioGroup.invalidate();
            mAllRadioButton.invalidate();
            mMissedRadioButton.invalidate();
        }
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();

        if (mTwinmeActivity != null) {
            mTwinmeActivity.setOnlyMissedCalls(mOnlyMissedCalls);
        }

        if (mCallRadioGroup != null) {
            mCallRadioGroup.setOnCheckedChangeListener(null);
        }

        if (mResetCallsImageView != null) {
            mResetCallsImageView.setOnClickListener(null);
        }

        final ViewTreeObserver viewTreeObserver = mMissedRadioButton.getViewTreeObserver();
        if (viewTreeObserver != null && viewTreeObserver.isAlive()) {
            viewTreeObserver.removeOnGlobalLayoutListener(this);
        }

        mCallsService.dispose();
    }

    @Override
    public void onGlobalLayout() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGlobalLayout");
        }

        int maxWidth = mAllRadioButton.getWidth();

        if (mMissedRadioButton.getWidth() > maxWidth) {
            maxWidth = mMissedRadioButton.getWidth();
        }

        Paint paint = new Paint();
        paint.setTypeface(Design.FONT_REGULAR32.typeface);
        paint.setTextSize(Design.FONT_REGULAR32.size);
        float textHeight = Math.abs(paint.getFontMetrics().ascent - paint.getFontMetrics().descent);

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, displayMetrics);

        float defaultHeight = textHeight + padding;

        mAllRadioButton.setWidth(maxWidth);
        mMissedRadioButton.setWidth(maxWidth);
        mAllRadioButton.setHeight((int)defaultHeight);
        mMissedRadioButton.setHeight((int)defaultHeight);

        final ViewTreeObserver viewTreeObserver = mMissedRadioButton.getViewTreeObserver();
        if (viewTreeObserver != null && viewTreeObserver.isAlive()) {
            viewTreeObserver.removeOnGlobalLayoutListener(this);
        }
    }

    //
    // Implement CallsService.Observer methods
    //

    @Override
    public void showProgressIndicator() {

    }

    @Override
    public void hideProgressIndicator() {

    }

    @Override
    public void onGetContacts(@NonNull List<Contact> contacts) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContacts: contacts=" + contacts);
        }

        if (mTwinmeActivity == null) {

            return;
        }

        final TwinmeApplication twinmeApplication = mTwinmeActivity.getTwinmeApplication();
        for (Contact contact : contacts) {
            UIContact uiContact = new UIContact(twinmeApplication, contact, null);
            if (contact.getTwincodeOutboundId() != null) {
                mUIContacts.put(contact.getTwincodeOutboundId(), uiContact);
            }
        }
    }

    @Override
    public void onGetContact(@NonNull Contact contact, Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: contact=" + contact);
        }
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
    }

    @Override
    public void onGetGroupNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroupNotFound");
        }
    }

    @Override
    public void onGetGroups(@NonNull List<Group> groups) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroups: groups.size=" + groups.size());
        }

        if (mTwinmeActivity == null) {

            return;
        }

        final TwinmeApplication twinmeApplication = mTwinmeActivity.getTwinmeApplication();
        for (Group group : groups) {
            if (group.getTwincodeOutboundId() != null) {
                UIContact uiContact = new UIContact(twinmeApplication, group, null);
                mUIContacts.put(group.getTwincodeOutboundId(), uiContact);
            }
        }

    }



    @Override
    public void onCreateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateContact: contact=" + contact);
        }

        if (mTwinmeActivity == null) {

            return;
        }

        UIContact uiContact = new UIContact(mTwinmeActivity.getTwinmeApplication(), contact, avatar);
        if (contact.getTwincodeOutboundId() != null) {
            mUIContacts.put(contact.getTwincodeOutboundId(), uiContact);
        }
    }

    @Override
    public void onDeleteContact(@NonNull UUID contactId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteContact: contactId=" + contactId);
        }

        for (Map.Entry<UUID, UIContact> entry : mUIContacts.entrySet()) {
            Originator contact = entry.getValue().getContact();
            if (contact != null && contactId.equals(contact.getId())) {
                mUIContacts.remove(entry.getKey());
                break;
            }
        }

        updateCalls();
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: contact=" + contact);
        }

        UIContact c = mUIContacts.get(contact.getId());
        if (c == null) {

            return;
        }
        // A new avatar could be available (async image fetch).
        c.setAvatar(avatar);

        // A name could change.
        updateCalls();
    }

    @Override
    public void onSetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpace: space=" + space);
        }

        mUIContacts.clear();
        mAllCalls.clear();
        mFilteredCalls.clear();
        mOnlyMissedCalls = false;
        mCallRadioGroup.check(R.id.calls_tool_bar_all_radio);
        mCallsRecyclerView.scrollToPosition(0);
    }

    @Override
    public void onGetDescriptors(@NonNull List<CallDescriptor> descriptors) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDescriptors: descriptors=" + descriptors);
        }

        for (CallDescriptor callDescriptor : descriptors) {
            if (!mAllCalls.contains(callDescriptor)) {
                mAllCalls.add(callDescriptor);
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
                    setEnabled(resetMenuItem, false);
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
                mAllCalls.set(i, descriptor);
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

        Iterator<CallDescriptor> iterator = mAllCalls.iterator();
        while (iterator.hasNext()) {
            CallDescriptor callDescriptor = iterator.next();
            DescriptorId descriptorId = callDescriptor.getDescriptorId();

            if (descriptorIdSet.remove(descriptorId)) {
                iterator.remove();
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
                    setEnabled(resetMenuItem, false);
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
        for (int i = 0; i < mAllCalls.size(); i++) {
            CallDescriptor callDescriptor = mAllCalls.get(i);
            if (callDescriptor.getTwincodeOutboundId().equals(conversation.getTwincodeOutboundId()) && callDescriptor.getTwincodeOutboundId() == conversation.getPeerTwincodeOutboundId()) {
                mAllCalls.remove(callDescriptor);
                i--;
            }
        }

        updateCalls();
    }

    @Override
    public void onGetCallReceivers(@NonNull List<CallReceiver> callReceivers) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceivers: callReceivers=" + callReceivers);
        }

        if (mTwinmeActivity == null) {

            return;
        }

        final TwinmeApplication twinmeApplication = mTwinmeActivity.getTwinmeApplication();
        for (CallReceiver callReceiver : callReceivers) {
            UIContact uiContact = new UIContact(twinmeApplication, callReceiver, null);
            if (callReceiver.getTwincodeOutboundId() != null) {
                mUIContacts.put(callReceiver.getTwincodeOutboundId(), uiContact);
            }
        }
    }

    @Override
    public void onGetCallReceiver(@NonNull CallReceiver callReceiver, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceiver: callReceiver=" + callReceiver);
        }
    }

    @Override
    public void onCreateCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateCallReceiver: " + callReceiver);
        }

        // Fragment was detached.
        if (mTwinmeActivity == null) {
            return;
        }
        UIContact uiContact = new UIContact(mTwinmeActivity.getTwinmeApplication(), callReceiver, null);
        if (callReceiver.getTwincodeOutboundId() != null) {
            mUIContacts.put(callReceiver.getTwincodeOutboundId(), uiContact);
        }
    }

    @Override
    public void onUpdateCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateCallReceiver: " + callReceiver);
        }

        UIContact c = mUIContacts.get(callReceiver.getId());
        if (c == null) {

            return;
        }

        updateCalls();
    }

    @Override
    public void onDeleteCallReceiver(@NonNull UUID callReceiverId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteCallReceiver: " + callReceiverId);
        }

        for (Map.Entry<UUID, UIContact> entry : mUIContacts.entrySet()) {
            Originator contact = entry.getValue().getContact();
            if (contact != null && callReceiverId.equals(contact.getId())) {
                mUIContacts.remove(entry.getKey());
                break;
            }
        }

        updateCalls();
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

    private void initViews(View view) {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews: view=" + view);
        }

        if (mTwinmeActivity == null) {

            return;
        }

        mCallRadioGroup = mTwinmeActivity.findViewById(R.id.calls_tool_bar_radio_group);

        mAllRadioButton = mTwinmeActivity.findViewById(R.id.calls_tool_bar_all_radio);
        Design.updateTextFont(mAllRadioButton, Design.FONT_REGULAR32);

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

        mAllRadioButton.setTextColor(colorStateList);

        mMissedRadioButton = mTwinmeActivity.findViewById(R.id.calls_tool_bar_missed_radio);
        Design.updateTextFont(mMissedRadioButton, Design.FONT_REGULAR32);
        mMissedRadioButton.setTextColor(colorStateList);

        if (CommonUtils.isLayoutDirectionRTL()) {
            mAllRadioButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.segmented_control_right, mTwinmeActivity.getTheme()));
            mMissedRadioButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.segmented_control_left, mTwinmeActivity.getTheme()));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAllRadioButton.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
            mMissedRadioButton.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        }

        ViewTreeObserver mMissedRadioButtonViewTreeObserver = mMissedRadioButton.getViewTreeObserver();
        mMissedRadioButtonViewTreeObserver.addOnGlobalLayoutListener(this);

        mCallRadioGroup.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            mCallsRecyclerView.smoothScrollToPosition(0);
            if (checkedId == R.id.calls_tool_bar_all_radio) {
                mOnlyMissedCalls = false;
            } else if (checkedId == R.id.calls_tool_bar_missed_radio) {
                mOnlyMissedCalls = true;
            }

            updateCalls();
        });

        OnCallClickListener onCallClickListener = new OnCallClickListener() {
            @Override
            public void onCallClick(int position) {

                if (mTwinmeActivity != null && mTwinmeActivity.getTwinmeApplication().inCallInfo() == null && position >= 0 && position < mFilteredCalls.size()) {
                    UICall uiCall = mFilteredCalls.get(position);
                    onUICallClick(uiCall);
                }
            }

            @Override
            public void onAddExternalCallClick() {

                onCreateExternalCallClick();
            }
        };

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mTwinmeActivity, RecyclerView.VERTICAL, false);
        mCallsRecyclerView = view.findViewById(R.id.calls_fragment_list_view);
        mCallsRecyclerView.setLayoutManager(linearLayoutManager);
        mCallsRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mCallsRecyclerView.setItemAnimator(null);

        OnSwipeItemClickListener onSwipeItemClickListener = new OnSwipeItemClickListener() {

            @Override
            public void onLeftActionClick(int adapterPosition) {

            }

            @Override
            public void onRightActionClick(int adapterPosition) {

                int position = adapterPosition - 2;
                if (position >= 0 && position < mFilteredCalls.size()) {
                    onUICallDeleteClick(mFilteredCalls.get(position));
                }
            }

            @Override
            public void onOtherActionClick(int adapterPosition) {

            }
        };
        SwipeItemTouchHelper swipeItemTouchHelper = new SwipeItemTouchHelper(mCallsRecyclerView, null, SwipeItemTouchHelper.ButtonType.DELETE_AND_SHARE, onSwipeItemClickListener);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeItemTouchHelper);
        itemTouchHelper.attachToRecyclerView(mCallsRecyclerView);

        mNoCallImageView = view.findViewById(R.id.calls_fragment_no_call_image_view);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mNoCallImageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_PLACEHOLDER_MARGIN * Design.HEIGHT_RATIO);
        mNoCallTitleView = view.findViewById(R.id.calls_fragment_no_call_title_view);
        Design.updateTextFont(mNoCallTitleView, Design.FONT_MEDIUM34);
        mNoCallTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mNoCallTextView = view.findViewById(R.id.calls_fragment_no_call_text_view);
        Design.updateTextFont(mNoCallTextView, Design.FONT_MEDIUM28);
        mNoCallTextView.setTextColor(Design.FONT_COLOR_DESCRIPTION);

        // Setup the service after the view is initialized but before the adapter.
        mCallsService = new CallsService(mTwinmeActivity, mTwinmeActivity.getTwinmeContext(), this, null, null);

        mCallsListAdapter = new CallsAdapter(mTwinmeActivity, mCallsService, mFilteredCalls, onCallClickListener);
        mCallsRecyclerView.setAdapter(mCallsListAdapter);

        mUIInitialized = true;
    }

    private void updateCalls() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateCalls");
        }

        if (!mUIInitialized || mTwinmeActivity == null) {
            return;
        }

        mFilteredCalls.clear();

        ArrayList<UICall> uiCalls = new ArrayList<>();

        for (CallDescriptor callDescriptor : mAllCalls) {
            UIOriginator uiOriginator = mUIContacts.get(callDescriptor.getTwincodeOutboundId());

            if (uiOriginator != null) {
                if (!mOnlyMissedCalls || (!callDescriptor.isAccepted() && callDescriptor.isIncoming())) {
                    UICall uiCall = new UICall(uiOriginator, callDescriptor);
                    uiCalls.add(uiCall);
                }
            }
        }

        if (!uiCalls.isEmpty()) {
            UICall uiCall1 = uiCalls.get(0);
            if (uiCalls.size() == 1) {
                mFilteredCalls.add(uiCall1);
            } else {
                for (int i = 1; i < uiCalls.size(); i++) {
                    UICall uiCall2 = uiCalls.get(i);
                    if (sameCall(uiCall1.getLastCallDescriptor(), uiCall2.getLastCallDescriptor())) {
                        for (int index = uiCall1.getCount() - 1; index >= 0; index--) {
                           uiCall2.addCallDescriptor(uiCall1.getCallDescriptors().get(index));
                        }
                    } else {
                        mFilteredCalls.add(uiCall1);
                    }

                    if (i + 1 == uiCalls.size()) {
                        mFilteredCalls.add(uiCall2);
                    } else {
                        uiCall1 = uiCall2;
                    }
                }
            }
        }

        mCallsListAdapter.notifyDataSetChanged();

        if (mFilteredCalls.isEmpty() && mCallsService.isGetDescriptorDone()) {
            mNoCallImageView.setVisibility(View.VISIBLE);
            mNoCallTitleView.setVisibility(View.VISIBLE);
            mNoCallTextView.setVisibility(View.VISIBLE);
            if (getView() != null) {
                getView().setBackgroundColor(Design.WHITE_COLOR);
            }
        } else {
            mNoCallImageView.setVisibility(View.GONE);
            mNoCallTitleView.setVisibility(View.GONE);
            mNoCallTextView.setVisibility(View.GONE);
            if (getView() != null) {
                getView().setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
            }
        }

        if (mMenu != null) {
            MenuItem resetMenuItem = mMenu.findItem(R.id.reset_calls_action);
            setEnabled(resetMenuItem, !mAllCalls.isEmpty());
        }
    }

    private boolean sameCall(CallDescriptor callDescriptor1, CallDescriptor callDescriptor2) {

        return callDescriptor2.getTwincodeOutboundId().equals(callDescriptor1.getTwincodeOutboundId()) && callDescriptor2.isVideo() == callDescriptor1.isVideo() && isMissedCall(callDescriptor2) == isMissedCall(callDescriptor1);
    }

    private boolean isMissedCall(CallDescriptor callDescriptor) {

        return !callDescriptor.isAccepted() && callDescriptor.isIncoming();
    }

    private void onUICallClick(UICall uiCall) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUICallClick: uiCall=" + uiCall);
        }

        if (uiCall != null && mTwinmeActivity != null) {
            mUICall = uiCall;
            callAgain();
        }
    }

    private void onUICallDeleteClick(UICall uiCall) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUICallDeleteClick: uiCall=" + uiCall);
        }

        if (uiCall != null && uiCall.getContact() != null && uiCall.getContact().getTwincodeOutboundId() != null) {
            for (int i = 0; i < uiCall.getCallDescriptors().size(); i++) {
                CallDescriptor callDescriptor = uiCall.getCallDescriptors().get(i);
                mCallsService.deleteCallDescriptor(callDescriptor);
            }
        }
    }

    private void onCallReceiverDeleteClick(UIOriginator uiOriginator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCallReceiverDeleteClick: " + uiOriginator);
        }

    }

    private void onResetClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResetClick");
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded()) {
            return;
        }

        if (mAllCalls.isEmpty() || mTwinmeActivity == null) {
            return;
        }

        DrawerLayout drawerLayout = mTwinmeActivity.findViewById(R.id.main_activity_drawer_layout);
        mCallsService.getProfileImage(mTwinmeActivity.getProfile(), (Bitmap avatar) -> {
            DeleteConfirmView deleteConfirmView = new DeleteConfirmView(mTwinmeActivity, null);
            deleteConfirmView.setAvatar(avatar, false);

            String message = getString(R.string.application_operation_irreversible) + "\n\n"  + getString(R.string.calls_fragment_reset);
            deleteConfirmView.setMessage(message);
            deleteConfirmView.setConfirmTitle(getString(R.string.calls_fragment_reset_title));

            AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
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
                    drawerLayout.removeView(deleteConfirmView);
                    if (mTwinmeActivity != null) {
                        mTwinmeActivity.setStatusBarColor();
                    }
                }
            };
            deleteConfirmView.setObserver(observer);

            drawerLayout.addView(deleteConfirmView);
            deleteConfirmView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            mTwinmeActivity.setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        });
    }

    private void onCreateExternalCallClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateExternalCallClick");
        }

        if (mTwinmeActivity != null) {
            if (mTwinmeActivity.getTwinmeApplication().startOnboarding(org.twinlife.twinme.ui.TwinmeApplication.OnboardingType.EXTERNAL_CALL)) {
                Intent intent = new Intent();
                intent.setClass(mTwinmeActivity, OnboardingExternalCallActivity.class);
                mTwinmeActivity.startActivityForResult(intent, REQUEST_EXTERNAL_CALL_ONBOARDING);
                mTwinmeActivity.overridePendingTransition(0, 0);
            } else {
                mTwinmeActivity.showPremiumFeatureView(UIPremiumFeature.FeatureType.CLICK_TO_CALL);
            }
        }
    }

    private void resetCalls() {
        if (DEBUG) {
            Log.d(LOG_TAG, "resetCalls");
        }

        showProgressIndicator();

        mResetAllCalls = true;

        if (mMenu != null) {
            MenuItem resetMenuItem = mMenu.findItem(R.id.reset_calls_action);
            setEnabled(resetMenuItem, false);
        }

        for (int i = 0; i < mAllCalls.size(); i++) {
            CallDescriptor callDescriptor = mAllCalls.get(i);
            mCallsService.deleteCallDescriptor(callDescriptor);
        }
    }

    private void callAgain() {
        if (DEBUG) {
            Log.d(LOG_TAG, "callAgain");
        }

        if (mTwinmeActivity == null || mUICall == null) {
            return;
        }

        Originator originator = mUICall.getContact();

        if (originator.getType() == Originator.Type.GROUP) {
            DrawerLayout drawerLayout = mTwinmeActivity.findViewById(R.id.main_activity_drawer_layout);

            PremiumFeatureConfirmView premiumFeatureConfirmView = new PremiumFeatureConfirmView(mTwinmeActivity, null);
            premiumFeatureConfirmView.initWithPremiumFeature(new UIPremiumFeature(mTwinmeActivity, UIPremiumFeature.FeatureType.GROUP_CALL));

            AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
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
                    drawerLayout.removeView(premiumFeatureConfirmView);
                    if (mTwinmeActivity != null) {
                        mTwinmeActivity.setStatusBarColor();
                    }
                }
            };
            premiumFeatureConfirmView.setObserver(observer);

            drawerLayout.addView(premiumFeatureConfirmView);
            premiumFeatureConfirmView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            mTwinmeActivity.setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        } else if(originator.getType() == Originator.Type.CONTACT) {
            if ((mUICall.getLastCallDescriptor().isVideo() && originator.getCapabilities().hasVideo()) || (!mUICall.getLastCallDescriptor().isVideo() && originator.getCapabilities().hasAudio())) {
                showCallAgainConfirmView(mUICall);
            } else {
                Toast.makeText(mTwinmeActivity, R.string.application_not_authorized_operation_by_your_contact, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showCallAgainConfirmView(UICall uiCall) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showCallAgainConfirmView: uiCall=" + uiCall);
        }

        if (uiCall == null || mCallsService == null) {
            return;
        }

        mCallsService.getImage(uiCall.getContact(), (Bitmap avatar) -> {
            // Fragment was detached.
            if (mTwinmeActivity == null) {
                return;
            }

            DrawerLayout drawerLayout = mTwinmeActivity.findViewById(R.id.main_activity_drawer_layout);

            CallAgainConfirmView callAgainConfirmView = new CallAgainConfirmView(mTwinmeActivity, null);
            callAgainConfirmView.setTitle(mUICall.getUIContact().getName());
            callAgainConfirmView.setAvatar(avatar, avatar == null || avatar.equals(mTwinmeActivity.getTwinmeApplication().getDefaultGroupAvatar()));

            if (mUICall.getLastCallDescriptor().isVideo()) {
                callAgainConfirmView.setMessage(getString(R.string.conversation_activity_video_call));
                callAgainConfirmView.setIcon(R.drawable.video_call);
            } else {
                callAgainConfirmView.setMessage(getString(R.string.conversation_activity_audio_call));
                callAgainConfirmView.setIcon(R.drawable.audio_call);
            }

            AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
                @Override
                public void onConfirmClick() {
                    callAgainConfirmView.animationCloseConfirmView();
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
                    drawerLayout.removeView(callAgainConfirmView);
                    if (mTwinmeActivity != null) {
                        mTwinmeActivity.setStatusBarColor();
                    }

                    if (fromConfirmAction) {
                        // Fragment was detached.
                        if (mTwinmeActivity == null) {
                            return;
                        }
                        Intent intent = new Intent();

                        if (mUICall.getContact().isGroup()) {
                            intent.putExtra(Intents.INTENT_GROUP_ID, mUICall.getContact().getId().toString());
                        } else {
                            intent.putExtra(Intents.INTENT_CONTACT_ID, mUICall.getContact().getId().toString());
                        }

                        if (mUICall.getLastCallDescriptor().isVideo()) {
                            intent.putExtra(Intents.INTENT_CALL_MODE, CallStatus.OUTGOING_VIDEO_CALL);
                        } else {
                            intent.putExtra(Intents.INTENT_CALL_MODE, CallStatus.OUTGOING_CALL);
                        }

                        intent.setClass(mTwinmeActivity, CallActivity.class);
                        startActivity(intent);
                    }
                }
            };
            callAgainConfirmView.setObserver(observer);

            drawerLayout.addView(callAgainConfirmView);
            callAgainConfirmView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            mTwinmeActivity.setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        });
    }

    private void addMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "addMenu");
        }

        if (mTwinmeActivity == null) {
            return;
        }

        mTwinmeActivity.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.calls_menu, menu);

                mMenu = menu;

                MenuItem menuItem = menu.findItem(R.id.reset_calls_action);

                if (mResetCallsImageView != null) {
                    // when navigating away from the Calls tab, onCreateOptionsMenu() is called again
                    // before onDestroy(). So we lose the reference to mResetCallsImageView, which causes
                    // a memory leak because of the listener. The new instance of mResetCallsImageView will
                    // be cleaned up in onDestroyView().
                    mResetCallsImageView.setOnClickListener(null);
                }

                mResetCallsImageView = (ImageView) menuItem.getActionView();

                if (mResetCallsImageView != null) {
                    mResetCallsImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.action_bar_delete, null));
                    mResetCallsImageView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);
                    mResetCallsImageView.setOnClickListener(view -> onResetClick());
                    mResetCallsImageView.setContentDescription(getString(R.string.calls_fragment_reset_title));
                }

                // Update to take into account the menu.
                updateCalls();
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Handle menu item clicks here
                return false;
            }

        }, getViewLifecycleOwner());
    }

    public void updateFont() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFont");
        }

        Design.updateTextFont(mAllRadioButton, Design.FONT_REGULAR32);
        Design.updateTextFont(mMissedRadioButton, Design.FONT_REGULAR32);
    }

    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

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

        mAllRadioButton.setTextColor(colorStateList);
    }
}
