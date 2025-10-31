/*
 *  Copyright (c) 2012-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Zhuoyu Ma (Zhuoyu.Ma@twinlife-systems.com)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Thibaud David (contact@thibauddavid.com)
 */

package org.twinlife.twinme.ui;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.SwitchView;

public class FeedbackActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "FeedbackActivity";
    private static final boolean DEBUG = false;

    private static final float DESIGN_MESSAGE_CONTENT_VIEW_HEIGHT = 244f;
    private static final float DESIGN_FORM_TOP_MARGIN = 58f;
    private static final float DESIGN_FIELD_MARGIN = 14f;
    private static final float DESIGN_INFO_TOP_MARGIN = 10f;
    private static final float DESIGN_INFO_BOTTOM_MARGIN = 20f;
    private static final float DESIGN_REPORT_BOTTOM_MARGIN = 30f;
    private static final float DESIGN_DEVICE_INFO_MARGIN = 40f;

    private EditText mEmailView;
    private EditText mSubjectView;
    private EditText mMessageView;
    private SwitchView mLogsSwitchView;
    private TextView mInfoLogsView;
    private TextView mLogsReportView;
    private TextView mDeviceInfoTextView;
    private TextView mSendView;

    private String mSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        initViews();
    }

    @Override
    protected void onBackClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackClick");
        }

        finish();
    }

    @Override
    protected void onPause() {

        super.onPause();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mEmailView.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();

        finish();
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.feedback_activity);

        setStatusBarColor();
        setToolBar(R.id.feedback_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setTitle(getString(R.string.navigation_activity_feedback));
        applyInsets(R.id.feedback_activity_content_view, R.id.feedback_activity_tool_bar, R.id.feedback_activity_contact_scrollview, Design.TOOLBAR_COLOR, false);

        ScrollView scrollView = findViewById(R.id.feedback_activity_contact_scrollview);
        scrollView.setBackgroundColor(Design.WHITE_COLOR);

        View emailContentView = findViewById(R.id.feedback_activity_contact_content_view);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable emailViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        emailViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        emailContentView.setBackground(emailViewBackground);

        ViewGroup.LayoutParams layoutParams = emailContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) emailContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_FORM_TOP_MARGIN * Design.HEIGHT_RATIO);

        mEmailView = findViewById(R.id.feedback_activity_contact);
        Design.updateTextFont(mEmailView, Design.FONT_REGULAR28);
        mEmailView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mEmailView.setHintTextColor(Design.GREY_COLOR);

        View subjectContentView = findViewById(R.id.feedback_activity_subject_content_view);

        ShapeDrawable subjectViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        subjectViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        subjectContentView.setBackground(subjectViewBackground);

        layoutParams = subjectContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) subjectContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_FIELD_MARGIN * Design.HEIGHT_RATIO);

        mSubjectView = findViewById(R.id.feedback_activity_subject);
        Design.updateTextFont(mSubjectView, Design.FONT_REGULAR28);
        mSubjectView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mSubjectView.setHintTextColor(Design.GREY_COLOR);

        mSubject = mSubjectView.getText().toString();

        View messageContentView = findViewById(R.id.feedback_activity_message_content_view);

        ShapeDrawable messageViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        messageViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        messageContentView.setBackground(messageViewBackground);

        layoutParams = messageContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = (int) (DESIGN_MESSAGE_CONTENT_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) messageContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_FIELD_MARGIN * Design.HEIGHT_RATIO);

        mMessageView = findViewById(R.id.feedback_activity_message);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR28);
        mMessageView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mMessageView.setHintTextColor(Design.GREY_COLOR);

        View logsView = findViewById(R.id.feedback_activity_logs_view);

        layoutParams = logsView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.SECTION_HEIGHT;

        mLogsSwitchView = findViewById(R.id.feedback_activity_logs_checkbox);
        Design.updateTextFont(mLogsSwitchView, Design.FONT_REGULAR34);
        mLogsSwitchView.setChecked(getTwinmeApplication().lastScreenHidden());
        mLogsSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mLogsSwitchView.setChecked(true);

        mInfoLogsView = findViewById(R.id.feedback_activity_logs_info);
        Design.updateTextFont(mInfoLogsView, Design.FONT_REGULAR24);
        mInfoLogsView.setTextColor(Design.FONT_COLOR_GREY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mInfoLogsView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        layoutParams = mInfoLogsView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mInfoLogsView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_INFO_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_INFO_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        String infoLogs = getString(R.string.feedback_activity_info_logs) + "\n\n" + getString(R.string.feedback_activity_help);
        mInfoLogsView.setText(infoLogs);

        mLogsReportView = findViewById(R.id.feedback_activity_logs_report);
        Design.updateTextFont(mLogsReportView, Design.FONT_REGULAR24);
        mLogsReportView.setTextColor(Design.getMainStyle());
        mLogsReportView.setPaintFlags(mLogsReportView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        mLogsReportView.setOnClickListener(v -> onLogsClick());

        View sendContentView = findViewById(R.id.feedback_activity_send_content_view);
        sendContentView.setOnClickListener(new SendListener());

        ShapeDrawable sendViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        sendViewBackground.getPaint().setColor(Design.getMainStyle());
        sendContentView.setBackground(sendViewBackground);

        layoutParams = sendContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) sendContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_REPORT_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        mSendView = findViewById(R.id.feedback_activity_send_title_view);
        Design.updateTextFont(mSendView, Design.FONT_BOLD28);
        mSendView.setTextColor(Color.WHITE);

        mDeviceInfoTextView = findViewById(R.id.feedback_activity_device_info);
        Design.updateTextFont(mDeviceInfoTextView, Design.FONT_REGULAR28);

        String deviceInfo = "Device Model: " + getDeviceName() + "\nOS version: " + Build.VERSION.RELEASE + "\nApp version: " + getVersionName();
        deviceInfo += " " + BuildConfig.FLAVOR;
        deviceInfo += "\n" + getString(R.string.feedback_activity_gdpr_notice);
        mDeviceInfoTextView.setText(deviceInfo);
        mDeviceInfoTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) sendContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_DEVICE_INFO_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_DEVICE_INFO_MARGIN * Design.HEIGHT_RATIO);
    }

    //
    // Override TwinlifeActivityImpl methods
    //

    private String getDeviceName() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getDeviceName");
        }

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {

            return model;
        }

        return manufacturer + " " + model;
    }

    //
    // Private methods
    //

    private String getVersionName() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getVersionName");
        }

        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException lException) {
            Log.e(LOG_TAG, "gatherInformation: exception=" + lException);
        }

        if (packageInfo != null) {
            return packageInfo.versionName;
        }

        return "";
    }

    private void onLogsClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLogsClick");
        }

        startActivity(LogsActivity.class);
    }

    private void onSendClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendClick");
        }

        hideKeyboard();

        String email = mEmailView.getText().toString();
        String subject = mSubjectView.getText().toString();
        String description = mMessageView.getText().toString();
        if (!description.isEmpty() || !subject.equals(mSubject)) {

            if (getTwinmeContext().hasTwinlife()) {
                String logReport = null;
                if (mLogsSwitchView.isChecked()) {
                    logReport = getTwinmeContext().getManagementService().getLogReport();
                }
                getTwinmeContext().getManagementService().sendFeedback(email, subject, description, logReport);
            }
        }

        Toast.makeText(this, R.string.feedback_activity_send_message, Toast.LENGTH_SHORT).show();

        finish();
    }

    private void hideKeyboard() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideKeyboard");
        }

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mEmailView.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(mSubjectView.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(mMessageView.getWindowToken(), 0);
        }
    }

    private class SendListener implements OnClickListener {

        private boolean disabled = false;

        @Override
        public void onClick(View view) {
            if (DEBUG) {
                Log.d(LOG_TAG, "SendListener.onClick: view=" + view);
            }

            if (disabled) {

                return;
            }
            disabled = true;

            onSendClick();
        }
    }

    @Override
    public void updateFont() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFont");
        }

        super.updateFont();

        Design.updateTextFont(mEmailView, Design.FONT_REGULAR28);
        Design.updateTextFont(mSubjectView, Design.FONT_REGULAR28);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR28);
        Design.updateTextFont(mLogsSwitchView, Design.FONT_REGULAR34);
        Design.updateTextFont(mInfoLogsView, Design.FONT_REGULAR24);
        Design.updateTextFont(mLogsReportView, Design.FONT_REGULAR24);
        Design.updateTextFont(mSendView, Design.FONT_BOLD28);
        Design.updateTextFont(mDeviceInfoTextView, Design.FONT_REGULAR28);
    }

    @Override
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        super.updateColor();

        mEmailView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mEmailView.setHintTextColor(Design.GREY_COLOR);
        mSubjectView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mSubjectView.setHintTextColor(Design.GREY_COLOR);
        mMessageView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mMessageView.setHintTextColor(Design.GREY_COLOR);
        mLogsSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mInfoLogsView.setTextColor(Design.FONT_COLOR_GREY);
        mLogsReportView.setTextColor(Design.getMainStyle());
        mDeviceInfoTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
    }
}
