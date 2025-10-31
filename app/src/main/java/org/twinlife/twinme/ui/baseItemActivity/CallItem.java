/*
 *  Copyright (c) 2020-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.Context;

import androidx.annotation.NonNull;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService.CallDescriptor;

public class CallItem extends Item {

    private final CallDescriptor mCallDescriptor;

    public CallItem(CallDescriptor callDescriptor) {

        super(ItemType.CALL, callDescriptor, null);

        mCallDescriptor = callDescriptor;
        setCopyAllowed(false);
    }

    CallDescriptor getCallDescriptor() {

        return mCallDescriptor;
    }

    //
    // Override Item methods
    //

    @Override
    public boolean isPeerItem() {

        return false;
    }

    @Override
    public long getTimestamp() {

        return getCreatedTimestamp();
    }

    String getInformation(Context context, String contactName) {

        String callStatus = "";
        if (mCallDescriptor.getTerminateReason() != null) {
            switch (mCallDescriptor.getTerminateReason()) {
                case BUSY:
                    callStatus = String.format(context.getResources().getString(R.string.info_item_activity_call_terminated_reason_busy), contactName);
                    break;

                case GONE:
                    callStatus = String.format(context.getResources().getString(R.string.info_item_activity_call_terminated_reason_gone), contactName);
                    break;

                case DECLINE:
                    callStatus = String.format(context.getResources().getString(R.string.info_item_activity_call_terminated_reason_decline), contactName);
                    break;

                case REVOKED:
                    callStatus = String.format(context.getResources().getString(R.string.info_item_activity_call_terminated_reason_revoked), contactName);
                    break;

                case NOT_AUTHORIZED:
                    callStatus = context.getResources().getString(R.string.info_item_activity_call_terminated_reason_not_authorized);
                    break;

                case CANCEL:
                    callStatus = String.format(context.getResources().getString(R.string.info_item_activity_call_terminated_reason_cancel), contactName);
                    break;

                case TIMEOUT:
                    callStatus = String.format(context.getResources().getString(R.string.info_item_activity_call_terminated_reason_timeout), contactName);
                    break;

                default:
                    break;
            }
        }

        return callStatus;
    }

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CallItem:\n");
        appendTo(stringBuilder);
        stringBuilder.append(" callDescriptor=");
        stringBuilder.append(mCallDescriptor);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
