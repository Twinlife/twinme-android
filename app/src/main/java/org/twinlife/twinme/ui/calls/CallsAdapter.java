/*
 *  Copyright (c) 2019-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.calls;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.externalCallActivity.UICallReceiver;
import org.twinlife.twinme.ui.mainActivity.MainActivity;
import org.twinlife.twinme.ui.users.UIContactViewHolder;
import org.twinlife.twinme.ui.users.UIOriginator;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CallsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "CallsAdapter";
    private static final boolean DEBUG = false;

    private static final int NB_CALL_RECEIVER = 3;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 126f;

    @NonNull
    private final OnCallClickListener mOnCallClickListener;

    @NonNull
    private final MainActivity mListActivity;

    @NonNull
    private final AbstractTwinmeService mService;

    @NonNull
    private final List<UICall> mUICalls;
    private final List<UICallReceiver> mUICallReceivers;

    private static final int POSITION_ADD_EXTERNAL_CALL = 0;
    private static final int SECTION_EXTERNAL_CALLS = 1;
    private static int SECTION_LAST_CALLS = 2;

    private static final int TITLE = 0;
    private static final int ADD_EXTERNAL_CALL = 1;
    private static final int EXTERNAL_CALL = 2;
    private static final int CALL = 3;

    private boolean mDisplayAllCallReceiver = false;
    private int mNbCallReceiverToDisplay = 0;

    public interface OnCallClickListener {

        void onCallClick(int position);

        void onAddExternalCallClick();

        void onDisplayAllExternalCallClick();

        void onExternalCallClick(int position);
    }

    public CallsAdapter(@NonNull MainActivity listActivity, @NonNull AbstractTwinmeService service,
                        @NonNull List<UICall> uiCalls, @NonNull List<UICallReceiver> uiCallReceivers, @NonNull OnCallClickListener onCallClickListener) {

        mService = service;
        mOnCallClickListener = onCallClickListener;
        mListActivity = listActivity;
        mUICalls = uiCalls;
        mUICallReceivers = uiCallReceivers;

        SECTION_LAST_CALLS = mNbCallReceiverToDisplay + 2;

        setHasStableIds(false);
    }

    public int getFirstCallPosition() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getFirstCallPosition");
        }

        return SECTION_LAST_CALLS + 1;
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        Profile profile = mListActivity.getProfile();
        if (profile == null) {
            return 0;
        }

        if (mUICalls.isEmpty()) {
            return 2 + mNbCallReceiverToDisplay;
        }
        return mNbCallReceiverToDisplay + mUICalls.size() + 3;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position == SECTION_EXTERNAL_CALLS || position == SECTION_LAST_CALLS) {
            return TITLE;
        } else if (position == POSITION_ADD_EXTERNAL_CALL) {
            return ADD_EXTERNAL_CALL;
        } else if (position < SECTION_LAST_CALLS) {
            return EXTERNAL_CALL;
        } else {
            return CALL;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mListActivity.getLayoutInflater();
        View convertView;

        if (viewType == TITLE) {
            convertView = inflater.inflate(R.layout.calls_fragment_section_call_item, parent, false);
            return new SectionCallViewHolder(convertView, mOnCallClickListener);
        } else if (viewType == ADD_EXTERNAL_CALL) {
            convertView = inflater.inflate(R.layout.calls_fragment_add_external_call_item, parent, false);
            return new AddExternalCallViewHolder(convertView);
        } else if (viewType == EXTERNAL_CALL) {
            convertView = inflater.inflate(R.layout.contacts_fragment_contact_item, parent, false);
            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
            layoutParams.height = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
            convertView.setLayoutParams(layoutParams);
            return new UIContactViewHolder<>(mService, convertView,  R.id.contacts_fragment_contact_item_name_view, R.id.contacts_fragment_contact_item_avatar_view, 0, 0, R.id.contacts_fragment_contact_item_tag_image_view,  R.id.contacts_fragment_contact_item_certified_image_view, R.id.contacts_fragment_contact_item_separator_view, Design.FONT_REGULAR34);
        } else {
            convertView = inflater.inflate(R.layout.calls_fragment_call_item, parent, false);
            return new CallViewHolder(mService, convertView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);

        if (viewType == TITLE) {
            SectionCallViewHolder sectionCallViewHolder = (SectionCallViewHolder) viewHolder;
            if (position == SECTION_EXTERNAL_CALLS) {
                boolean showRightView = !mDisplayAllCallReceiver && mUICallReceivers.size() > NB_CALL_RECEIVER;
                sectionCallViewHolder.onBind(mListActivity.getString(R.string.premium_services_activity_click_to_call_title), showRightView, mNbCallReceiverToDisplay == 0);
                sectionCallViewHolder.mRightView.setOnClickListener(v -> {
                    mDisplayAllCallReceiver = true;
                    updateIndexes();
                    notifyDataSetChanged();
                });
            } else {
                sectionCallViewHolder.onBind(mListActivity.getString(R.string.show_contact_activity_history_title), false, false);
            }
        } else if (viewType == ADD_EXTERNAL_CALL) {
            AddExternalCallViewHolder addExternalCallViewHolder = (AddExternalCallViewHolder) viewHolder;
            addExternalCallViewHolder.itemView.setOnClickListener(view -> mOnCallClickListener.onAddExternalCallClick());
            addExternalCallViewHolder.onBind(mListActivity.getString(R.string.calls_fragment_create_link), mListActivity.getString(R.string.show_call_activity_information_code));
        } else if (viewType == EXTERNAL_CALL) {
            UIContactViewHolder<UIOriginator> externalCallViewHolder = (UIContactViewHolder<UIOriginator>) viewHolder;
            externalCallViewHolder.itemView.setOnClickListener(v -> {
                if (position >= 0) {
                    mOnCallClickListener.onExternalCallClick(position - 2);
                }
            });

            externalCallViewHolder.onBind(mListActivity, mUICallReceivers.get(position - 2), false);
        } else {
            CallViewHolder callViewHolder = (CallViewHolder) viewHolder;
            boolean hideSeparator = position + 1 == mUICalls.size() + SECTION_LAST_CALLS;
            callViewHolder.itemView.setOnClickListener(v -> {
                if (position >= 0) {
                    mOnCallClickListener.onCallClick(position - SECTION_LAST_CALLS - 1);
                }
            });

            callViewHolder.onBind(mListActivity, mUICalls.get(position - SECTION_LAST_CALLS - 1), hideSeparator);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        int position = viewHolder.getBindingAdapterPosition();
        int viewType = getItemViewType(position);
        if (viewType == CALL && position != -1) {
            CallViewHolder callViewHolder = (CallViewHolder) viewHolder;
            boolean hideSeparator = position + 1 == mUICalls.size() + 3;
            callViewHolder.itemView.setOnClickListener(v -> {
                if (position >= 0) {
                    mOnCallClickListener.onCallClick(position - SECTION_LAST_CALLS - 1);
                }
            });

            callViewHolder.onBind(mListActivity, mUICalls.get(position - SECTION_LAST_CALLS - 1), hideSeparator);
        }
    }

    public void setCallReicevers(@NonNull List<CallReceiver> callReicevers) {

        TwinmeApplication twinmeApplication = mListActivity.getTwinmeApplication();

        mUICallReceivers.clear();
        for (CallReceiver callReceiver : callReicevers) {
            mUICallReceivers.add(create(twinmeApplication, callReceiver, null));
        }

        updateIndexes();

        Collections.sort(mUICallReceivers);
    }

    /**
     * Update the originator in the list.
     *
     * @param originator the originator to update or add.
     */
    public void updateUIOriginator(Originator originator) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateUIOriginator: originator=" + originator);
        }

        UICallReceiver uiOriginator = null;
        for (UICallReceiver lUIOriginator : mUICallReceivers) {
            if (lUIOriginator.getContact().getId().equals(originator.getId())) {
                uiOriginator = lUIOriginator;
                break;
            }
        }

        if (uiOriginator != null) {
            mUICallReceivers.remove(uiOriginator);
            uiOriginator.update(mListActivity.getTwinmeApplication(), originator, null);
        } else {
            uiOriginator = create(mListActivity.getTwinmeApplication(), originator, null);
        }

        // TBD Sort using id order when name are equals
        boolean added = false;
        int size = mUICallReceivers.size();
        for (int i = 0; i < size; i++) {
            String callReceiverName1 = mUICallReceivers.get(i).getName();
            String callReceiverName2 = uiOriginator.getName();
            if (callReceiverName1 != null && callReceiverName2 != null && callReceiverName1.compareToIgnoreCase(callReceiverName2) > 0) {
                mUICallReceivers.add(i, uiOriginator);
                added = true;
                break;
            }
        }

        if (!added) {
            mUICallReceivers.add(uiOriginator);
        }

        updateIndexes();
    }

    public UICallReceiver create(TwinmeApplication application, Originator originator, Bitmap avatar) {

        return new UICallReceiver(application, originator, avatar);
    }

    public void removeUIOriginator(UUID originatorId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "removeUIOriginator: originatorId=" + originatorId);
        }

        for (UIOriginator uiOriginator : mUICallReceivers) {
            if (uiOriginator.getContact().getId().equals(originatorId)) {
                mUICallReceivers.remove(uiOriginator);
                break;
            }
        }

        updateIndexes();
    }

    private void updateIndexes() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateIndexes");
        }

        if (!mDisplayAllCallReceiver && mUICallReceivers.size() > NB_CALL_RECEIVER) {
            mNbCallReceiverToDisplay = NB_CALL_RECEIVER;
        } else {
            mNbCallReceiverToDisplay = mUICallReceivers.size();
        }

        SECTION_LAST_CALLS = mNbCallReceiverToDisplay + 2;
    }

    /**
     * Get the list of call receiver.
     *
     * @return the list of call receiver.
     */
    public List<UICallReceiver> getCallReceivers() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getCallReceivers");
        }

        return mUICallReceivers;
    }
}
