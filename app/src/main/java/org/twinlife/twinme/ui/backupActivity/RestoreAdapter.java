/*
 *  Copyright (c) 2024-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.backupActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BackupService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.rooms.InformationViewHolder;
import org.twinlife.twinme.utils.SectionTitleViewHolder;

import java.util.ArrayList;
import java.util.List;

public class RestoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = "RestoreAdapter";
    private static final boolean DEBUG = false;

    private final RestoreActivity mRestoreActivity;

    private final List<UIRestoreItem> mUIRestoreItems = new ArrayList<>();

    private boolean mOpenKeyboard = false;

    public RestoreAdapter(RestoreActivity backupActivity) {

        mRestoreActivity = backupActivity;
        setHasStableIds(true);

        refreshContent();
    }

    public void refreshContent() {
        if (DEBUG) {
            Log.d(LOG_TAG, "refreshContent");
        }

        updateContent();
        notifyItemRangeChanged(0, mUIRestoreItems.size());
    }

    public void selectedWord() {
        if (DEBUG) {
            Log.d(LOG_TAG, "selectedWord");
        }

        mOpenKeyboard = true;
        notifyItemChanged(1);
    }

    @Override
    public int getItemCount() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemCount");
        }

        return mUIRestoreItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getItemViewType: " + position);
        }

        return mUIRestoreItems.get(position).getType().getValue();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBindViewHolder: viewHolder=" + viewHolder + " position=" + position);
        }

        UIRestoreItem restoreItem = mUIRestoreItems.get(position);
        switch (restoreItem.getType()) {
            case HEADER: {
                BackupInfoViewHolder backupInfoViewHolder = (BackupInfoViewHolder) viewHolder;
                backupInfoViewHolder.onBind(mRestoreActivity.getBackupFileName(), Design.LIGHT_GREY_BACKGROUND_COLOR);
                break;
            }

            case WORDS: {
                RestoreWordsViewHolder restoreWordsViewHolder = (RestoreWordsViewHolder) viewHolder;
                restoreWordsViewHolder.onBind(mRestoreActivity.getBackupWords(), mRestoreActivity.getCurrentWord(), mOpenKeyboard);
                mOpenKeyboard = false;
                break;
            }

            case FOOTER: {
                BackupFooterViewHolder backupFooterViewHolder = (BackupFooterViewHolder) viewHolder;
                if (mRestoreActivity.isVerifyBackupMode()) {
                    if (mRestoreActivity.isVerifyBackupTerminated() && !mRestoreActivity.getRestoreReport().isRestoreUpToDate()) {
                        backupFooterViewHolder.onBind(mRestoreActivity.getString(R.string.backup_view_new_backup), mRestoreActivity.canRestore());
                    } else {
                        backupFooterViewHolder.onBind(mRestoreActivity.getString(R.string.account_view_backup_verify), mRestoreActivity.canRestore());
                    }
                } else {
                    backupFooterViewHolder.onBind(mRestoreActivity.getString(R.string.restore_view_restore), mRestoreActivity.canRestore());
                }
                break;
            }

            case STATE: {
                RestoreStateViewHolder restoreStateViewHolder = (RestoreStateViewHolder) viewHolder;
                boolean isRestoreInProgress = mRestoreActivity.getRestoreState() != null && mRestoreActivity.getRestoreState() != BackupService.RestoreState.TERMINATED && mRestoreActivity.getRestoreState() != BackupService.RestoreState.CANCEL;
                restoreStateViewHolder.onBind(mRestoreActivity.getRestoreMessage(), isRestoreInProgress);
                break;
            }

            case SECTION: {
                SectionTitleViewHolder sectionTitleViewHolder = (SectionTitleViewHolder) viewHolder;
                sectionTitleViewHolder.onBind(restoreItem.getText(), false);
                break;
            }

            case CONTENT: {
                BackupContentViewHolder backupContentViewHolder = (BackupContentViewHolder) viewHolder;
                backupContentViewHolder.onBind(restoreItem, Design.WHITE_COLOR, false);
                break;
            }

            case INFO: {
                InformationViewHolder informationViewHolder = (InformationViewHolder) viewHolder;
                informationViewHolder.onBind(restoreItem.getText(), false);
                break;
            }

            default:
                break;
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateViewHolder: parent=" + parent + " viewType=" + viewType);
        }

        LayoutInflater inflater = mRestoreActivity.getLayoutInflater();
        View convertView;

        UIRestoreItem.UIRestoreItemType type = UIRestoreItem.UIRestoreItemType.fromType(viewType);

        switch (type) {
            case HEADER:
                convertView = inflater.inflate(R.layout.restore_activity_backup_info_item, parent, false);
                return new BackupInfoViewHolder(convertView);

            case WORDS:
                convertView = inflater.inflate(R.layout.restore_activity_restore_words_item, parent, false);
                return new RestoreWordsViewHolder(mRestoreActivity, convertView);

            case FOOTER:
                convertView = inflater.inflate(R.layout.create_backup_activity_footer_item, parent, false);
                return new BackupFooterViewHolder(convertView, mRestoreActivity::onFooterActionClick);

            case STATE:
                convertView = inflater.inflate(R.layout.restore_activity_restore_state_item, parent, false);
                return new RestoreStateViewHolder(convertView);

            case SECTION:
                convertView = inflater.inflate(R.layout.section_title_item, parent, false);
                return new SectionTitleViewHolder(convertView);

            case INFO:
                convertView = inflater.inflate(R.layout.settings_room_activity_information_item, parent, false);
                return new InformationViewHolder(convertView);

            default:
                convertView = inflater.inflate(R.layout.create_backup_activity_content_item, parent, false);
                return new BackupContentViewHolder(convertView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled: viewHolder=" + viewHolder);
        }

    }

    private void updateContent() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateContent");
        }

        mUIRestoreItems.clear();
        mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.HEADER, null, -1, -1, -1));

        if (mRestoreActivity.getRestoreState() == null) {
            mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.WORDS, null, -1, -1, -1));
            mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.FOOTER, null, -1, -1, -1));
        } else {
            mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.STATE, null, -1, -1, -1));

            if (mRestoreActivity.isVerifyBackupMode() && mRestoreActivity.isVerifyBackupTerminated() && !mRestoreActivity.getRestoreReport().isRestoreUpToDate()) {

                if (!mRestoreActivity.getRestoreReport().profiles.isStatsUpToDate()) {
                    mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.SECTION, mRestoreActivity.getString(R.string.application_profile), -1, -1, -1));
                    mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.CONTENT, mRestoreActivity.getString(R.string.restore_view_content_profile_reset), R.drawable.generate_code, -1, Design.BLACK_COLOR));
                    mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.INFO, mRestoreActivity.getString(R.string.restore_view_content_profile_reset_message), -1, -1, -1));
                }

                if (!mRestoreActivity.getRestoreReport().contacts.isStatsUpToDate()) {
                    mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.SECTION, mRestoreActivity.getString(R.string.contacts_view_title), -1, -1, -1));

                    if (mRestoreActivity.getRestoreReport().contacts.added != 0) {
                        mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.CONTENT, mRestoreActivity.getString(R.string.backup_view_content_diff_added), R.drawable.contacts_icon, mRestoreActivity.getRestoreReport().contacts.added, Design.BLACK_COLOR));
                    }

                    if (mRestoreActivity.getRestoreReport().contacts.modified != 0) {
                        mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.CONTENT, mRestoreActivity.getString(R.string.backup_view_content_diff_updated), R.drawable.action_edit, mRestoreActivity.getRestoreReport().contacts.modified, Design.BLACK_COLOR));
                    }

                    if (mRestoreActivity.getRestoreReport().contacts.deleted != 0) {
                        mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.CONTENT, mRestoreActivity.getString(R.string.backup_view_content_diff_deleted), R.drawable.delete_item, mRestoreActivity.getRestoreReport().contacts.deleted, -1));
                        mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.INFO, mRestoreActivity.getString(R.string.restore_view_content_contact_deleted_message), -1, -1, -1));
                    }
                }

                if (!mRestoreActivity.getRestoreReport().groups.isStatsUpToDate()) {
                    mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.SECTION, mRestoreActivity.getString(R.string.share_view_group_list), -1, -1, -1));

                    if (mRestoreActivity.getRestoreReport().groups.added != 0) {
                        mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.CONTENT, mRestoreActivity.getString(R.string.backup_view_content_diff_added), R.drawable.groups_icon, mRestoreActivity.getRestoreReport().groups.added, Design.BLACK_COLOR));
                    }

                    if (mRestoreActivity.getRestoreReport().groups.modified != 0) {
                        mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.CONTENT, mRestoreActivity.getString(R.string.backup_view_content_diff_updated), R.drawable.action_edit, mRestoreActivity.getRestoreReport().groups.modified, Design.BLACK_COLOR));
                    }

                    if (mRestoreActivity.getRestoreReport().groups.deleted != 0) {
                        mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.CONTENT, mRestoreActivity.getString(R.string.backup_view_content_diff_deleted), R.drawable.delete_item, mRestoreActivity.getRestoreReport().groups.deleted, -1));
                        mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.INFO, mRestoreActivity.getString(R.string.restore_view_content_contact_deleted_message), -1, -1, -1));
                    }
                }

                if (!mRestoreActivity.getRestoreReport().clickToCall.isStatsUpToDate()) {
                    mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.SECTION, mRestoreActivity.getString(R.string.premium_services_view_click_to_call_title), -1, -1, -1));

                    if (mRestoreActivity.getRestoreReport().clickToCall.added != 0) {
                        mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.CONTENT, mRestoreActivity.getString(R.string.backup_view_content_diff_added), R.drawable.add_external_call, mRestoreActivity.getRestoreReport().clickToCall.added, Design.BLACK_COLOR));
                    }

                    if (mRestoreActivity.getRestoreReport().clickToCall.modified != 0) {
                        mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.CONTENT, mRestoreActivity.getString(R.string.backup_view_content_diff_updated), R.drawable.action_edit, mRestoreActivity.getRestoreReport().clickToCall.modified, Design.BLACK_COLOR));
                    }

                    if (mRestoreActivity.getRestoreReport().clickToCall.deleted != 0) {
                        mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.CONTENT, mRestoreActivity.getString(R.string.backup_view_content_diff_deleted), R.drawable.delete_item, mRestoreActivity.getRestoreReport().clickToCall.deleted, -1));
                        mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.INFO, mRestoreActivity.getString(R.string.restore_view_content_contact_deleted_message), -1, -1, -1));
                    }
                }

                mUIRestoreItems.add(new UIRestoreItem(UIRestoreItem.UIRestoreItemType.FOOTER, null, -1, -1, -1));
            }
        }
    }
}