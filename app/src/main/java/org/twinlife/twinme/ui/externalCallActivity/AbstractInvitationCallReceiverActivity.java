/*
 *  Copyright (c) 2023-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinlife.util.Logger;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.Capabilities;
import org.twinlife.twinme.models.schedule.Date;
import org.twinlife.twinme.models.schedule.DateTime;
import org.twinlife.twinme.models.schedule.DateTimeRange;
import org.twinlife.twinme.models.schedule.Schedule;
import org.twinlife.twinme.models.schedule.Time;
import org.twinlife.twinme.models.schedule.WeeklyTimeRange;
import org.twinlife.twinme.services.CallReceiverService;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.contacts.ResetInvitationConfirmView;
import org.twinlife.twinme.ui.conversationActivity.NamedFileProvider;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.ClickToCallView;
import org.twinlife.twinme.utils.SaveTwincodeAsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class AbstractInvitationCallReceiverActivity extends AbstractTwinmeActivity implements CallReceiverService.Observer {
    private static final String LOG_TAG = "AbstractInvitation...";
    private static final boolean DEBUG = false;

    protected static final int DESIGN_NAVIGATION_BAR_COLOR = Color.rgb(30, 30, 30);
    protected static final int DESIGN_INVITATION_VIEW_COLOR = Color.rgb(69, 69, 69);
    protected static final int DESIGN_INVITATION_VIEW_BORDER_COLOR = Color.argb(120, 151, 151, 151);
    protected static final int DESIGN_HEADER_COLOR = Color.rgb(102, 102, 102);
    protected static final int DESIGN_NAME_VIEW_COLOR = Color.rgb(81, 79, 79);
    protected static final int DESIGN_RED_VIEW_COLOR = Color.rgb(191, 60, 52);
    protected static final int DESIGN_YELLOW_VIEW_COLOR = Color.rgb(255, 207, 8);
    protected static final int DESIGN_GREEN_VIEW_COLOR = Color.rgb(23, 196, 164);
    protected static final int DESIGN_ACTION_BORDER_COLOR = Color.rgb(84, 84, 84);

    public static final float DESIGN_CONTAINER_RADIUS = 6f;
    protected static final float DESIGN_CONTAINER_BORDER = 3f;
    protected static final int QRCODE_PIXEL_WIDTH = 295;
    protected static final int QRCODE_PIXEL_HEIGHT = 295;
    protected static final int WHITE = 0xFFFFFFFF;
    protected static final int BLACK = 0xFF000000;

    protected static final float DESIGN_SHARE_PADDING = 36f;
    protected static final float DESIGN_SHARE_ICON_SIZE = 42f;
    protected static final float DESIGN_HEADER_VIEW_HEIGHT = 90f;
    protected static final float DESIGN_AVATAR_VIEW_HEIGHT = 52f;

    protected static final float DESIGN_ROUNDED_VIEW_MARGIN = 20f;
    protected static final float DESIGN_AVATAR_VIEW_MARGIN = 24f;

    protected boolean mDeferredSaveTwincode = false;

    protected CircularImageView mAvatarView;
    protected TextView mNameView;
    protected TextView mTwincodeView;
    protected ImageView mQRCodeView;
    protected ClickToCallView mSaveClickToCallView;
    protected Bitmap mQRCodeBitmap;

    protected CallReceiver mCallReceiver;
    protected CallReceiverService mCallReceiverService;
    protected TwincodeURI mInvitationLink;
    protected Bitmap mAvatar;
    protected String mScheduleMessage;

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

        mCallReceiverService = new CallReceiverService(this, getTwinmeContext(), this);

        Intent intent = getIntent();
        String callReceiverId = intent.getStringExtra(Intents.INTENT_CALL_RECEIVER_ID);
        if (callReceiverId != null) {
            mCallReceiverService.getCallReceiver(UUID.fromString(callReceiverId));
        } else {
            finish();
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

        super.onDestroy();

        mCallReceiverService.dispose();
    }

    @Override
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        super.onRequestPermissions(grantedPermissions);

        boolean storageWriteAccessGranted = false;
        for (Permission grantedPermission : grantedPermissions) {
            if (grantedPermission == Permission.WRITE_EXTERNAL_STORAGE) {
                storageWriteAccessGranted = true;
                break;
            }
        }

        if (mDeferredSaveTwincode) {
            mDeferredSaveTwincode = false;
            if (storageWriteAccessGranted && (mCallReceiver != null && mCallReceiver.getTwincodeOutboundId() != null)) {
                mCallReceiverService.getImage(mCallReceiver, (Bitmap avatar) -> {
                    String message = String.format(getString(R.string.invitation_call_activity_save_message), mCallReceiver.getName());
                    mSaveClickToCallView.setTwincodeInformation(this, mCallReceiver.getName(), avatar, mQRCodeBitmap, mCallReceiver.getTwincodeOutboundId().toString(), message);
                    Bitmap bitmapToSave = getBitmapFromTwincodeView();
                    if (bitmapToSave != null) {
                        new SaveTwincodeAsyncTask(this, bitmapToSave).execute();
                    } else {
                        toast(getString(R.string.application_operation_failure));
                    }
                });

            }
        }
    }

    //
    // Implement CallReceiverService.Observer methods
    //

    @Override
    public void onGetCallReceiver(@Nullable CallReceiver callReceiver, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceiver: " + callReceiver);
        }

        mCallReceiver = callReceiver;

        updateExternalCall();
    }

    @Override
    public void onGetCallReceiverNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceiverNotFound");
        }

        // @todo report an error message.
        finish();
    }

    @Override
    public void onGetTwincodeURI(@NonNull TwincodeURI uri) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetTwincodeURI uri=" + uri);
        }

        mInvitationLink = uri;
        updateQRCode();
    }

    @Override
    public void onChangeCallReceiverTwincode(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onChangeCallReceiverTwincode: " + callReceiver);
        }

        mCallReceiver = callReceiver;

        updateExternalCall();
    }

    @Override
    public void onUpdateCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateCallReceiver: " + callReceiver);
        }

        if (callReceiver.getId().equals(mCallReceiver.getId())) {
            mCallReceiver = callReceiver;
            updateExternalCall();
        }
    }

    @Override
    public void onUpdateCallReceiverAvatar(@NonNull Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateCallReceiverAvatar: " + avatar);
        }

        updateExternalCall();
    }

    @Override
    public void onUpdateCallReceiverIdentityAvatar(@NonNull Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateCallReceiverIdentityAvatar: " + avatar);
        }

        updateExternalCall();
    }

    @Override
    public void onDeleteCallReceiver(@NonNull UUID callReceiverId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteCallReceiver: " + callReceiverId);
        }

        if (callReceiverId.equals(mCallReceiver.getId())) {
            finish();
        }
    }

    //
    // Private methods
    //

    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }
    }

    private void updateExternalCall() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateExternalCall");
        }

        if (mCallReceiver == null) {

            return;
        }

        if (mCallReceiver.getTwincodeOutboundId() != null) {
            mNameView.setText(mCallReceiver.getName());
            mCallReceiverService.getImage(mCallReceiver, (Bitmap avatar) -> {
                if (avatar != null) {
                    mAvatar = avatar;
                    mAvatarView.setImage(this, null, new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
                }
                updateQRCode();

            });
        } else {
            updateQRCode();
        }

    }

    protected void updateQRCode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateQRCode");
        }

        if (mQRCodeView == null || mCallReceiver == null || mInvitationLink == null) {

            return;
        }

        BitMatrix result;
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 0);
            result = new QRCodeWriter().encode(mInvitationLink.uri, BarcodeFormat.QR_CODE, QRCODE_PIXEL_WIDTH, QRCODE_PIXEL_HEIGHT, hints);
        } catch (Exception exception) {
            Log.e(LOG_TAG, "updateQrcode: exception=" + exception);

            return;
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        mQRCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mQRCodeBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        mQRCodeView.setImageBitmap(mQRCodeBitmap);
        mTwincodeView.setText(mInvitationLink.label);
    }

    /** @noinspection UnnecessaryUnicodeEscape*/
    protected void onSocialClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSocialClick");
        }

        if (mCallReceiver == null || mInvitationLink == null) {
            return;
        }
        String callReceiverName = mCallReceiver.getName();
        UUID twincodeOutboundId = mCallReceiver.getTwincodeOutboundId();
        if (twincodeOutboundId == null) {
            return;
        }
        mScheduleMessage = null;
        File file = new File(getExternalCacheDir() + "/qrcode.png");
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            mQRCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            // Exception could happen if there is no space left on the device.
            file = null;
            Log.e(LOG_TAG, "Cannot save QR-code: " + e.getMessage());
        }

        File icsFile = generateFileICS();

        String name = callReceiverName.replace('.', '\u2024').replace(':', '\u02d0');
        Intent intent = new Intent();

        String subject;
        if (mCallReceiver.isTransfer()) {
            subject = getString(R.string.premium_services_activity_transfert_title);
        } else {
            subject = mCallReceiver.getName();
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);

        ArrayList<Uri> uris = new ArrayList<>();

        if (file != null) {
            Uri uriCode = NamedFileProvider.getInstance().getUriForFile(this, file, name + "-QR-code.png");
            uris.add(uriCode);
        }

        if (icsFile != null) {
            String formatDate = "yyMMdd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatDate, Locale.getDefault());
            String callType = "-call-";
            if (mCallReceiver.isConference()) {
                callType = "-meet-";
            }

            Uri uriSchedule = NamedFileProvider.getInstance().getUriForFile(this, icsFile, "skred" + callType + simpleDateFormat.format(new java.util.Date()) + ".ics");
            uris.add(uriSchedule);
        }

        if (!uris.isEmpty()) {
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("*/*");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setType("text/plain");
            intent.setAction(Intent.ACTION_SEND);
        }

        String message;
        if (mCallReceiver.isConference()) {
            message = String.format(getString(R.string.invitation_call_activity_invite_meeting_message),
                    mInvitationLink.uri, name);
        } else {
            message = String.format(getString(R.string.invitation_call_activity_invite_message),
                    mInvitationLink.uri, name);
        }

        if (mScheduleMessage != null) {
            message += mScheduleMessage;
        }

        intent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(intent, null));
    }

    protected void onTwincodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTwincodeClick");
        }

        if (mCallReceiver == null) {
            return;
        }

        if (mInvitationLink != null) {
            org.twinlife.twinme.utils.Utils.setClipboard(this, mInvitationLink.uri);
            Toast.makeText(this, R.string.conversation_activity_menu_item_view_copy_message, Toast.LENGTH_SHORT).show();
        }
    }

    protected void onSaveInGalleryClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInGalleryClick");
        }

        Permission[] permissions = new Permission[]{Permission.WRITE_EXTERNAL_STORAGE};
        mDeferredSaveTwincode = true;
        if (checkPermissions(permissions)) {
            mDeferredSaveTwincode = false;
            if (mCallReceiver != null && mCallReceiver.getTwincodeOutboundId() != null) {
                String message;
                if (mCallReceiver.isTransfer()) {
                    message = getString(R.string.transfert_call_activity_gallery_message);
                } else {
                    message = String.format(getString(R.string.invitation_call_activity_save_message), mCallReceiver.getName());
                }
                mCallReceiverService.getImage(mCallReceiver, (Bitmap avatar) -> {
                    mSaveClickToCallView.setTwincodeInformation(this, mCallReceiver.getName(), avatar, mQRCodeBitmap, mCallReceiver.getTwincodeOutboundId().toString(), message);
                    Bitmap bitmapToSave = getBitmapFromTwincodeView();
                    if (bitmapToSave != null) {
                        new SaveTwincodeAsyncTask(this, bitmapToSave, mCallReceiver.isTransfer()).execute();
                    } else {
                        toast(getString(R.string.application_operation_failure));
                    }
                });
            }

        }
    }

    @Nullable
    protected Bitmap getBitmapFromTwincodeView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getBitmapFromTwincodeView");
        }

        try {
            Bitmap bitmap = Bitmap.createBitmap(Design.DISPLAY_WIDTH, Design.DISPLAY_HEIGHT, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            mSaveClickToCallView.layout(0, 0, Design.DISPLAY_WIDTH, Design.DISPLAY_HEIGHT);
            mSaveClickToCallView.draw(canvas);
            return bitmap;

        } catch (Throwable exception) {

            if (Logger.ERROR) {
                Log.e(LOG_TAG, "Exception when creating bitmap", exception);
            }
            return null;
        }
    }

    protected void onGenerateCodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGenerateCodeClick");
        }

        String message;
        if (mCallReceiver.isTransfer()) {
            message = getString(R.string.transfert_call_activity_reset_message);
        } else {
            message = getString(R.string.invitation_call_activity_generate_code_message);
        }

        ViewGroup viewGroup = findViewById(R.id.invitation_external_call_activity_layout);

        ResetInvitationConfirmView resetInvitationConfirmView = new ResetInvitationConfirmView(this, null);
        resetInvitationConfirmView.setAvatar(mAvatar, false);
        resetInvitationConfirmView.setMessage(message);

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                mCallReceiverService.changeCallReceiverTwincode(mCallReceiver);
                resetInvitationConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                resetInvitationConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                resetInvitationConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(resetInvitationConfirmView);
            }
        };
        resetInvitationConfirmView.setObserver(observer);
        viewGroup.addView(resetInvitationConfirmView);
        resetInvitationConfirmView.show();
    }

    @Nullable
    private File generateFileICS() {
        if (DEBUG) {
            Log.d(LOG_TAG, "generateFileICS");
        }

        Capabilities capabilities = mCallReceiver.getCapabilities();

        final Schedule schedule = capabilities.getSchedule();
        if (schedule != null && schedule.isEnabled()) {

            if (!schedule.getTimeRanges().isEmpty()) {

                if (schedule.getTimeRanges().get(0) instanceof DateTimeRange) {
                    DateTimeRange dateTimeRange = (DateTimeRange) schedule.getTimeRanges().get(0);
                    Date scheduleStartDate = dateTimeRange.start.date;
                    Time scheduleStartTime = dateTimeRange.start.time;
                    Date scheduleEndDate = dateTimeRange.end.date;
                    Time ScheduleEndTime = dateTimeRange.end.time;

                    final Calendar startCalendar = new DateTime(scheduleStartDate, scheduleStartTime).toCalendar(TimeZone.getDefault());
                    final Calendar endCalendar = new DateTime(scheduleEndDate, ScheduleEndTime).toCalendar(TimeZone.getDefault());

                    String formatDate = "yyyyMMdd'T'HHmmss";
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatDate, Locale.getDefault());
                    simpleDateFormat.setTimeZone(TimeZone.getDefault());

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("BEGIN:VCALENDAR\r\n");
                    stringBuilder.append("VERSION:2.0\r\n");
                    stringBuilder.append("PRODID:");
                    stringBuilder.append(getString(R.string.application_name));
                    stringBuilder.append("\r\n");
                    stringBuilder.append("METHOD:REQUEST\r\n");
                    stringBuilder.append("BEGIN:VEVENT\r\n");
                    stringBuilder.append("UID:");
                    stringBuilder.append(mCallReceiver.getId());
                    stringBuilder.append("\r\n");
                    if (mCallReceiver.getLong(UIConfigExternalCall.PROPERTY_CALL_RECEIVER_UPDATE_COUNTER, 0) > 0) {
                        stringBuilder.append("SEQUENCE:");
                        stringBuilder.append(mCallReceiver.getLong(UIConfigExternalCall.PROPERTY_CALL_RECEIVER_UPDATE_COUNTER, 0));
                        stringBuilder.append("\r\n");
                    }
                    stringBuilder.append("DTSTAMP:");
                    stringBuilder.append(simpleDateFormat.format(new java.util.Date()));
                    stringBuilder.append("\r\n");
                    stringBuilder.append("DTSTART;TZID=");
                    stringBuilder.append(TimeZone.getDefault().getID());
                    stringBuilder.append(":");
                    stringBuilder.append(simpleDateFormat.format(startCalendar.getTime()));
                    stringBuilder.append("\r\n");
                    stringBuilder.append("DTEND;TZID=");
                    stringBuilder.append(TimeZone.getDefault().getID());
                    stringBuilder.append(":");
                    stringBuilder.append(simpleDateFormat.format(endCalendar.getTime()));
                    stringBuilder.append("\r\n");
                    stringBuilder.append("SUMMARY:");
                    stringBuilder.append(mCallReceiver.getName());
                    stringBuilder.append("\r\n");
                    stringBuilder.append("LOCATION:");
                    stringBuilder.append(mInvitationLink.uri);
                    stringBuilder.append("\r\n");
                    if (mCallReceiver.getDescription() != null && !mCallReceiver.getDescription().isEmpty()) {
                        stringBuilder.append("DESCRIPTION:");
                        stringBuilder.append(getDescriptionForICSFile());
                        stringBuilder.append("\r\n");
                    }
                    stringBuilder.append("END:VEVENT\r\n");
                    stringBuilder.append("END:VCALENDAR\r\n");
                    
                    StringBuilder messageStringBuilder = new StringBuilder();
                    formatDate = "YYYY/MM/dd HH:mm";
                    SimpleDateFormat messageDateFormat = new SimpleDateFormat(formatDate, Locale.getDefault());
                    messageStringBuilder.append("\n\n");
                    messageStringBuilder.append(getString(R.string.create_external_call_activity_link_validity));
                    messageStringBuilder.append("\n");
                    messageStringBuilder.append(getString(R.string.show_call_activity_settings_start));
                    messageStringBuilder.append(" : ");
                    messageStringBuilder.append(messageDateFormat.format(startCalendar.getTime()));
                    messageStringBuilder.append("\n");
                    messageStringBuilder.append(getString(R.string.show_call_activity_settings_end));
                    messageStringBuilder.append(" : ");
                    messageStringBuilder.append(messageDateFormat.format(endCalendar.getTime()));

                    mScheduleMessage = messageStringBuilder.toString();

                    File file = null;
                    try {
                        formatDate = "YYYY-MM-dd";
                        SimpleDateFormat fileDateFormat = new SimpleDateFormat(formatDate, Locale.getDefault());
                        file = File.createTempFile("skred-call-" + fileDateFormat.format(startCalendar.getTime()), ".ics", getCacheDir());

                        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                            fileOutputStream.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
                        }
                        return file;
                    } catch (Exception exception) {
                        Log.e(LOG_TAG, "exception = ", exception);
                        if (file != null && !file.delete()) {
                            Log.w(LOG_TAG, "Cannot remove file");
                        }

                        return null;
                    }
                } else if (schedule.getTimeRanges().get(0) instanceof WeeklyTimeRange) {
                    WeeklyTimeRange weeklyTimeRange = (WeeklyTimeRange) schedule.getTimeRanges().get(0);
                    Time scheduleStartTime = weeklyTimeRange.start;
                    Time scheduleEndTime = weeklyTimeRange.end;
                    StringBuilder messageStringBuilder = new StringBuilder();
                    messageStringBuilder.append("\n\n");
                    messageStringBuilder.append(getString(R.string.create_external_call_activity_link_validity));
                    messageStringBuilder.append("\n");
                    messageStringBuilder.append(getString(R.string.show_call_activity_settings_start));
                    messageStringBuilder.append(" : ");
                    messageStringBuilder.append(scheduleStartTime);
                    messageStringBuilder.append("\n");
                    messageStringBuilder.append(getString(R.string.show_call_activity_settings_end));
                    messageStringBuilder.append(" : ");
                    messageStringBuilder.append(scheduleEndTime);
                    messageStringBuilder.append("\n\n");

                    DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(Locale.getDefault());
                    String[] weekDays = dateFormatSymbols.getWeekdays();

                    for (WeeklyTimeRange.DayOfWeek dayOfWeek : weeklyTimeRange.days) {
                        String dayString = "";
                        switch (dayOfWeek) {
                            case MONDAY:
                                dayString = weekDays[2];
                                break;
                            case TUESDAY:
                                dayString = weekDays[3];
                                break;
                            case WEDNESDAY:
                                dayString = weekDays[4];
                                break;
                            case THURSDAY:
                                dayString = weekDays[5];
                                break;
                            case FRIDAY:
                                dayString = weekDays[6];
                                break;
                            case SATURDAY:
                                dayString = weekDays[7];
                                break;
                            case SUNDAY:
                                dayString = weekDays[1];
                                break;
                        }

                        if (!dayString.isEmpty()) {
                            messageStringBuilder.append(dayString);
                            messageStringBuilder.append("\n");
                        }

                    }

                    mScheduleMessage = messageStringBuilder.toString();
                }
            }
        }
        return null;
    }

    private String getDescriptionForICSFile() {

        if (mCallReceiver.getDescription().isEmpty()) {
            return "";
        }

        return mCallReceiver.getDescription()
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\n", "\\n");
    }
}
