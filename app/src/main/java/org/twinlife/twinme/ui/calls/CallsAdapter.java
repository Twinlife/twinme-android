/*
 *  Copyright (c) 2019-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.calls;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.services.AbstractTwinmeService;
import org.twinlife.twinme.ui.mainActivity.MainActivity;

import java.util.List;

public class CallsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "CallsAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final OnCallClickListener mOnCallClickListener;

    @NonNull
    private final MainActivity mListActivity;

    @NonNull
    private final AbstractTwinmeService mService;

    @NonNull
    private final List<UICall> mUICalls;

    private static final int SECTION_EXTERNAL_CALLS = 0;
    private static final int SECTION_LAST_CALLS = 1;

    private static final int TITLE = 0;
    private static final int ADD_EXTERNAL_CALL = 1;
    private static final int CALL = 2;

    public interface OnCallClickListener {

        void onCallClick(int position);

        void onAddExternalCallClick();
    }

    public CallsAdapter(@NonNull MainActivity listActivity, @NonNull AbstractTwinmeService service,
                        @NonNull List<UICall> uiCalls, @NonNull OnCallClickListener onCallClickListener) {

        mService = service;
        mOnCallClickListener = onCallClickListener;
        mListActivity = listActivity;
        mUICalls = uiCalls;

        setHasStableIds(false);
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
            return 1;
        }

        return mUICalls.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position == SECTION_LAST_CALLS) {
            return TITLE;
        } else if (position == SECTION_EXTERNAL_CALLS) {
            return ADD_EXTERNAL_CALL;
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
            sectionCallViewHolder.onBind(mListActivity.getString(R.string.show_contact_activity_history_title));
        } else if (viewType == ADD_EXTERNAL_CALL) {
            AddExternalCallViewHolder addExternalCallViewHolder = (AddExternalCallViewHolder) viewHolder;
            addExternalCallViewHolder.itemView.setOnClickListener(view -> mOnCallClickListener.onAddExternalCallClick());
            addExternalCallViewHolder.onBind(mListActivity.getString(R.string.premium_services_activity_click_to_call_title), mListActivity.getString(R.string.show_call_activity_information_code));
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
}
