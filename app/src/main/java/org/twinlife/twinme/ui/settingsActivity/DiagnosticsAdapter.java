/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.List;

public class DiagnosticsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "DiagnosticsAdapter";
    private static final boolean DEBUG = false;

    private final DiagnosticsActivity mDiagnosticsActivity;

    private static final int SECTION = 0;
    private static final int SUBSECTION = 1;

    private final List<UIDiagnosticItem> mItems = new java.util.ArrayList<>();

    DiagnosticsAdapter(DiagnosticsActivity diagnosticsActivity) {

        mDiagnosticsActivity = diagnosticsActivity;
        initItems();
        setHasStableIds(false);
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        UIDiagnosticItem item = mItems.get(position);
        if (item instanceof UIDiagnosticSection) {
            return SECTION;
        } else {
            return SUBSECTION;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        int viewType = getItemViewType(position);
        UIDiagnosticItem item = mItems.get(position);
        if (viewType == SECTION) {
            SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
            sectionTitleViewHolder.onBind(item.getTitle(), false);
        } else if (viewType == SUBSECTION) {
            UIDiagnosticSubsection subSection = (UIDiagnosticSubsection) item;
            DiagnosticViewHolder diagnosticViewHolder = (DiagnosticViewHolder) viewHolder;
            diagnosticViewHolder.onBind(subSection, false);
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mDiagnosticsActivity.getLayoutInflater();
        View convertView;

        if (viewType == SECTION) {
            convertView = inflater.inflate(R.layout.section_title_item, parent, false);
            return new SectionTitleViewHolder(convertView);
        } else {
            convertView = inflater.inflate(R.layout.diagnosctic_item, parent, false);
            return new DiagnosticViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

        int position = viewHolder.getBindingAdapterPosition();

        if (position != -1) {
            int viewType = getItemViewType(position);
            UIDiagnosticItem item = mItems.get(position);
            if (viewType == SECTION) {
                SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
                sectionTitleViewHolder.onBind(item.getTitle(), false);
            } else if (viewType == SUBSECTION) {
                UIDiagnosticSubsection subSection = (UIDiagnosticSubsection) item;
                DiagnosticViewHolder diagnosticViewHolder = (DiagnosticViewHolder) viewHolder;
                diagnosticViewHolder.onBind(subSection, false);
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewDetachedFromWindow: viewHolder=" + viewHolder);
        }

        super.onViewDetachedFromWindow(viewHolder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewAttachedToWindow: viewHolder=" + viewHolder);
        }

        super.onViewAttachedToWindow(viewHolder);

        int position = viewHolder.getBindingAdapterPosition();

        if (position != -1) {
            int viewType = getItemViewType(position);
            UIDiagnosticItem item = mItems.get(position);
            if (viewType == SECTION) {
                SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
                sectionTitleViewHolder.onBind(item.getTitle(), false);
            } else if (viewType == SUBSECTION) {
                UIDiagnosticSubsection subSection = (UIDiagnosticSubsection) item;
                DiagnosticViewHolder diagnosticViewHolder = (DiagnosticViewHolder) viewHolder;
                diagnosticViewHolder.onBind(subSection, false);
            }
        }
    }

    private void initItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initItems");
        }

        mItems.clear();

        List<UIDiagnosticSection> sections = new java.util.ArrayList<>();
        sections.add(new UIDiagnosticSection(mDiagnosticsActivity, UIDiagnosticSection.DiagnosticSectionType.CONNECTION));
        sections.add(new UIDiagnosticSection(mDiagnosticsActivity, UIDiagnosticSection.DiagnosticSectionType.PERMISSIONS));
        sections.add(new UIDiagnosticSection(mDiagnosticsActivity, UIDiagnosticSection.DiagnosticSectionType.NOTIFICATIONS));
        sections.add(new UIDiagnosticSection(mDiagnosticsActivity, UIDiagnosticSection.DiagnosticSectionType.KEYSTORE));

        for (UIDiagnosticSection section : sections) {
            if (section.getTitle() != null && !section.getTitle().isEmpty()) {
                mItems.add(section);
            }
            mItems.addAll(section.getItems());
        }

        notifyItemRangeChanged(0, mItems.size());
    }

    public void updatePermissions(UIDiagnosticSubsection.DiagnosticSubSectionType permissionType, UIDiagnosticSubsection.DiagnosticState state) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateItems");
        }

        Pair<UIDiagnosticSubsection, Integer> item = getItem(permissionType);
        if (item != null) {
            UIDiagnosticSubsection subSection = item.first;
            int position = item.second;
            subSection.setState(state);
            notifyItemChanged(position);
        }
    }

    public void updateConnectionState(String title, String subTitle, UIDiagnosticSubsection.DiagnosticState state) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateConnectionState");
        }

        Pair<UIDiagnosticSubsection, Integer> item = getItem(UIDiagnosticSubsection.DiagnosticSubSectionType.CONNECTION);
        if (item != null) {
            UIDiagnosticSubsection subSection = item.first;
            int position = item.second;
            subSection.setTitle(title);
            subSection.setSubtitle(subTitle);
            subSection.setState(state);
            notifyItemChanged(position);
        }
    }

    public void updatePushState(UIDiagnosticSubsection.DiagnosticState state) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updatePushState");
        }

        Pair<UIDiagnosticSubsection, Integer> item = getItem(UIDiagnosticSubsection.DiagnosticSubSectionType.PUSH);
        if (item != null) {
            UIDiagnosticSubsection subSection = item.first;
            int position = item.second;
            subSection.setState(state);
            if (state == UIDiagnosticSubsection.DiagnosticState.KO) {
                subSection.setSubtitle(mDiagnosticsActivity.getString(R.string.diagnostics_view_unavailable));
            } else {
                subSection.setSubtitle(mDiagnosticsActivity.getString(R.string.diagnostics_view_available));
            }
            notifyItemChanged(position);
        }
    }

    public void updateKeystore(@Nullable String method) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateKeystore");
        }

        Pair<UIDiagnosticSubsection, Integer> item = getItem(UIDiagnosticSubsection.DiagnosticSubSectionType.SECURE_STORAGE);
        if (item != null) {
            UIDiagnosticSubsection subSection = item.first;
            int position = item.second;
            if (method != null) {
                subSection.setSubtitle(method);
                subSection.setState(UIDiagnosticSubsection.DiagnosticState.OK);
            } else {
                subSection.setState(UIDiagnosticSubsection.DiagnosticState.KO);
            }
            notifyItemChanged(position);
        }
    }

    private Pair<UIDiagnosticSubsection, Integer> getItem(UIDiagnosticSubsection.DiagnosticSubSectionType type) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItem");
        }

        for (int i = 0; i < mItems.size(); i++) {
            UIDiagnosticItem item = mItems.get(i);
            if (item instanceof UIDiagnosticSubsection) {
                UIDiagnosticSubsection subSection = (UIDiagnosticSubsection) item;
                if (subSection.getType() == type) {
                    return new Pair<>(subSection, i);
                }
            }
        }

        return null;
    }
}
