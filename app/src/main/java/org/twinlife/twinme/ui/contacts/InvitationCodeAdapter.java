/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.ui.calls.SectionCallViewHolder;

import java.util.List;

public class InvitationCodeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "InvitationCodeAdapter";
    private static final boolean DEBUG = false;

    @NonNull
    private final OnInvitationCodeListener mOnInvitationCodeListener;

    @NonNull
    private final InvitationCodeActivity mInvitationCodeActivity;

    @NonNull
    private final List<UIInvitationCode> mUIInvitationCode;

    private static final int SECTION_ADD = 0;
    private static final int SECTION_INVITATION_CODE = 1;

    private static final int TITLE = 0;
    private static final int ADD_INVITATION_CODE = 1;
    private static final int INVITATION_CODE = 2;

    public interface OnInvitationCodeListener {

        void onInvitationCodeClick(int position);

        void onAddInvitationCodeClick();
    }

    public InvitationCodeAdapter(@NonNull InvitationCodeActivity listActivity,
                        @NonNull List<UIInvitationCode> uiInvitationCodes, @NonNull OnInvitationCodeListener onInvitationCodeListener) {

        mOnInvitationCodeListener = onInvitationCodeListener;
        mInvitationCodeActivity = listActivity;
        mUIInvitationCode = uiInvitationCodes;

        setHasStableIds(false);
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        if (mUIInvitationCode.isEmpty()) {
            return 1;
        }

        return mUIInvitationCode.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        if (position == SECTION_INVITATION_CODE) {
            return TITLE;
        } else if (position == SECTION_ADD) {
            return ADD_INVITATION_CODE;
        } else {
            return INVITATION_CODE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mInvitationCodeActivity.getLayoutInflater();
        View convertView;

        if (viewType == TITLE) {
            convertView = inflater.inflate(R.layout.calls_fragment_section_call_item, parent, false);
            return new SectionCallViewHolder(convertView, null);
        } else if (viewType == ADD_INVITATION_CODE) {
            convertView = inflater.inflate(R.layout.invitation_code_activity_add_code_item, parent, false);
            return new AddInvitationCodeViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.invitation_code_activity_code_item, parent, false);
            return new InvitationCodeViewHolder(convertView);
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
            sectionCallViewHolder.onBind(mInvitationCodeActivity.getString(R.string.invitation_code_activity_history), false, false);
        } else if (viewType == ADD_INVITATION_CODE) {
            AddInvitationCodeViewHolder addInvitationCodeViewHolder = (AddInvitationCodeViewHolder) viewHolder;
            addInvitationCodeViewHolder.itemView.setOnClickListener(view -> mOnInvitationCodeListener.onAddInvitationCodeClick());
            addInvitationCodeViewHolder.onBind(mInvitationCodeActivity.getString(R.string.invitation_code_activity_create_code), mInvitationCodeActivity.getString(R.string.invitation_code_activity_create_code_subtitle));
        } else {
            InvitationCodeViewHolder invitationCodeViewHolder = (InvitationCodeViewHolder) viewHolder;
            boolean hideSeparator = position == mUIInvitationCode.size() + SECTION_INVITATION_CODE;
            invitationCodeViewHolder.itemView.setOnClickListener(v -> {
                if (position >= 0) {
                    mOnInvitationCodeListener.onInvitationCodeClick(position - SECTION_INVITATION_CODE - 1);
                }
            });

            invitationCodeViewHolder.onBind(mUIInvitationCode.get(position - SECTION_INVITATION_CODE - 1), hideSeparator);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

    }
}
