/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.accountMigrationActivity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.Formatter;
import android.text.style.ImageSpan;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.AccountMigrationService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.CommonUtils;

import java.util.Locale;

public class UIMigrationStateItem {

    public enum MigrationStateItemType {
        HEADER,
        INIT,
        TRANSFER,
        SETTINGS,
        DATABASE,
        ACCOUNT,
        TERMINATED
    }

    public enum MigrationStateItemState {
        PENDING,
        IN_PROGRESS,
        DONE
    }

    @NonNull
    private final MigrationStateItemType mMigrationStateItemType;
    @NonNull
    private MigrationStateItemState mMigrationStateItemState;
    @NonNull
    private String mTitle = "";
    @Nullable
    private String mSubTitle;
    @Nullable
    private Spanned mSpannedSubTitle;

    public UIMigrationStateItem(@NonNull Context context, @NonNull MigrationStateItemType type) {

        mMigrationStateItemType = type;
        mMigrationStateItemState = MigrationStateItemState.PENDING;
        initTitle(context);
    }

    @NonNull
    public MigrationStateItemType getType() {
        return mMigrationStateItemType;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

    @Nullable
    public String getInfo() {
        return mSubTitle;
    }

    @Nullable
    public Spanned getAttributedInfo() {
        return mSpannedSubTitle;
    }

    @NonNull
    public MigrationStateItemState getState() {
        return mMigrationStateItemState;
    }

    public void update(Context context, @Nullable AccountMigrationService.State state, @Nullable AccountMigrationService.Status status) {

        switch (mMigrationStateItemType) {
            case HEADER:
                updateHeader(context, state, status);
                break;

            case INIT:
                if (state == AccountMigrationService.State.STARTING) {
                    mMigrationStateItemState = MigrationStateItemState.IN_PROGRESS;
                } else if (state == null) {
                    mMigrationStateItemState = MigrationStateItemState.PENDING;
                } else if (state.ordinal() < AccountMigrationService.State.TERMINATED.ordinal()) {
                    mMigrationStateItemState = MigrationStateItemState.DONE;
                }
                break;

            case TRANSFER:
                if (state == AccountMigrationService.State.NEGOTIATE) {
                    mMigrationStateItemState = MigrationStateItemState.IN_PROGRESS;
                } else if (state != null && state.ordinal() < AccountMigrationService.State.NEGOTIATE.ordinal()) {
                    mMigrationStateItemState = MigrationStateItemState.PENDING;
                } else if (state != null && state.ordinal() >= AccountMigrationService.State.LIST_FILES.ordinal()) {
                    updateTransferInfo(context, status);
                }

                break;

            case SETTINGS:
                if (state == AccountMigrationService.State.SEND_SETTINGS) {
                    mMigrationStateItemState = MigrationStateItemState.IN_PROGRESS;
                } else if (state != null && state.ordinal() < AccountMigrationService.State.SEND_SETTINGS.ordinal()) {
                    mMigrationStateItemState = MigrationStateItemState.PENDING;
                } else if (state != null && state.ordinal() < AccountMigrationService.State.TERMINATED.ordinal()) {
                    mMigrationStateItemState = MigrationStateItemState.DONE;
                }
                updateInfo(context);
                break;

            case DATABASE:
                if (state == AccountMigrationService.State.SEND_DATABASE) {
                    mMigrationStateItemState = MigrationStateItemState.IN_PROGRESS;
                } else if (state != null && state.ordinal() < AccountMigrationService.State.SEND_DATABASE.ordinal()) {
                    mMigrationStateItemState = MigrationStateItemState.PENDING;
                } else if (state != null && state.ordinal() < AccountMigrationService.State.TERMINATED.ordinal()) {
                    mMigrationStateItemState = MigrationStateItemState.DONE;
                }
                updateInfo(context);
                break;

            case ACCOUNT:
                if (state == AccountMigrationService.State.SEND_ACCOUNT) {
                    mMigrationStateItemState = MigrationStateItemState.IN_PROGRESS;
                } else if (state != null && state.ordinal() < AccountMigrationService.State.SEND_ACCOUNT.ordinal()) {
                    mMigrationStateItemState = MigrationStateItemState.PENDING;
                } else if (state != null && state.ordinal() < AccountMigrationService.State.TERMINATED.ordinal()) {
                    mMigrationStateItemState = MigrationStateItemState.DONE;
                }
                updateInfo(context);
                break;

            case TERMINATED:
                if (state == AccountMigrationService.State.TERMINATE) {
                    mMigrationStateItemState = MigrationStateItemState.IN_PROGRESS;
                } else if (state != null && state.ordinal() < AccountMigrationService.State.TERMINATE.ordinal()) {
                    mMigrationStateItemState = MigrationStateItemState.PENDING;
                } else if (state == AccountMigrationService.State.TERMINATED) {
                    mMigrationStateItemState = MigrationStateItemState.DONE;
                }
                break;
        }
    }

    private void initTitle(Context context) {

        switch (mMigrationStateItemType) {
            case HEADER:
                mTitle = context.getString(R.string.account_migration_view_ready_to_start);
                break;

            case INIT:
                mTitle = context.getString(R.string.account_migration_view_initializing);
                break;

            case TRANSFER:
                mTitle = CommonUtils.capitalizeString(context.getString(R.string.export_view_files));
                break;

            case SETTINGS:
                mTitle = context.getString(R.string.navigation_view_settings);
                break;

            case DATABASE:
                mTitle = context.getString(R.string.account_migration_view_database);
                break;

            case ACCOUNT:
                mTitle = context.getString(R.string.account_view_title);
                break;

            case TERMINATED:
                mTitle = context.getString(R.string.account_migration_view_finalizing);
                break;
        }
    }

    private void updateHeader(Context context, @Nullable AccountMigrationService.State state, @Nullable AccountMigrationService.Status status) {

        if (status == null) {
            return;
        }

        if (!status.isConnected()) {
            mSubTitle = context.getString(R.string.account_migration_view_state_wait_connect);
        } else if (state == AccountMigrationService.State.STARTING) {
            mSubTitle = context.getString(R.string.account_migration_view_network_message);
        } else {
            mSubTitle = "";
        }

        if (state == null || state == AccountMigrationService.State.CANCELED || state == AccountMigrationService.State.TERMINATED) {
            if (state == AccountMigrationService.State.CANCELED) {
                mTitle = context.getString(R.string.account_migration_view_state_canceled);
                mSubTitle = context.getString(R.string.account_migration_view_cancel_message);
            } else {
                mTitle = context.getString(R.string.account_migration_view_success_message);
                mSubTitle = context.getString(R.string.account_migration_view_close_message);
            }
        } else if (state == AccountMigrationService.State.ERROR) {
            if (status.getErrorCode() == AccountMigrationService.ErrorCode.NO_SPACE_LEFT) {
                mTitle = context.getString(R.string.account_migration_view_not_enough_space_for_files);
                mSubTitle = context.getString(R.string.application_migration_no_storage_space_message);
            } else {
                mTitle = context.getString(R.string.account_migration_view_state_canceled);
                mSubTitle = String.format(Locale.getDefault(), "%s\n%d",
                        context.getString(R.string.cleanup_view_error),
                        status.getErrorCode() == null ? -1 : status.getErrorCode().ordinal());
            }
        } else if (state == AccountMigrationService.State.STOPPED) {
            mTitle = context.getString(R.string.account_migration_view_success_message);
            mSubTitle = context.getString(R.string.account_migration_view_close_message);
        } else {
            double progressPercent = status.getProgress();
            if (progressPercent >= 0.0 && progressPercent <= 100.0) {
                mTitle = String.format(Locale.getDefault(), "%d %%", (int) progressPercent);
            } else if (progressPercent <= 0.0) {
                mTitle = "0%";
            } else {
                mTitle = "100%";
            }
        }
    }

    private void updateInfo(Context context) {

        mSpannedSubTitle = null;
        if (mMigrationStateItemState == MigrationStateItemState.DONE) {
            mSubTitle = context.getString(R.string.account_migration_view_transfer_complete);
        } else if (mMigrationStateItemState == MigrationStateItemState.IN_PROGRESS) {
            mSubTitle = context.getString(R.string.account_migration_view_transfer);
        }
    }

    private void updateTransferInfo(Context context, @Nullable AccountMigrationService.Status status) {

        if (status == null) {
            return;
        }

        String sentInfo = String.format("%s / %s",
                Formatter.formatFileSize(context, status.getBytesSent()),
                Formatter.formatFileSize(context, status.getBytesSent() + status.getEstimatedBytesRemainSend()));

        String receivedInfo = String.format("%s / %s",
                Formatter.formatFileSize(context, status.getBytesReceived()),
                Formatter.formatFileSize(context, status.getBytesReceived() + status.getEstimatedBytesRemainReceive()));

        mSubTitle = sentInfo + "\n" + receivedInfo;
        mSpannedSubTitle = spannedTransferInfoWithSentInfo(context, sentInfo, receivedInfo);

        if (status.getEstimatedBytesRemainReceive() == 0 && status.getEstimatedBytesRemainSend() == 0) {
            mMigrationStateItemState = MigrationStateItemState.DONE;
        } else {
            mMigrationStateItemState = MigrationStateItemState.IN_PROGRESS;
        }
    }

    @NonNull
    private Spanned spannedTransferInfoWithSentInfo(@NonNull Context context, @NonNull String sentInfo, @NonNull String receivedInfo) {

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(spannedTransferInfoWithIcon(context, R.drawable.migration_send_icon, sentInfo));
        spannableStringBuilder.append("\n");
        spannableStringBuilder.append(spannedTransferInfoWithIcon(context, R.drawable.migration_receive_icon, receivedInfo));
        return spannableStringBuilder;
    }

    @NonNull
    private Spanned spannedTransferInfoWithIcon(@NonNull Context context, @DrawableRes int iconId, @NonNull String info) {

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        Drawable icon = ContextCompat.getDrawable(context, iconId);
        if (icon != null) {
            icon = icon.mutate();
            icon.setTint(Design.FONT_COLOR_GREY);
            int iconSize = Math.round(Design.FONT_REGULAR32.size);
            icon.setBounds(0, 0, iconSize, iconSize);
            int start = spannableStringBuilder.length();
            spannableStringBuilder.append("￼");
            spannableStringBuilder.setSpan(new ImageSpan(icon, ImageSpan.ALIGN_BASELINE), start, spannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.append(" ");
        }
        spannableStringBuilder.append(info);
        return spannableStringBuilder;
    }
}
