/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.Twinlife;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.Capabilities;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.models.schedule.Date;
import org.twinlife.twinme.models.schedule.DateTime;
import org.twinlife.twinme.models.schedule.DateTimeRange;
import org.twinlife.twinme.models.schedule.Schedule;
import org.twinlife.twinme.models.schedule.Time;
import org.twinlife.twinme.services.CallReceiverService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractEditActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.ui.profiles.MenuPhotoView;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.EditableView;
import org.twinlife.twinme.utils.OnboardingDetailView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.SwitchView;
import org.twinlife.twinme.utils.UIMenuSelectAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CreateExternalCallActivity extends AbstractEditActivity implements CallReceiverService.Observer, MenuCallCapabilitiesView.Observer {
    private static final String LOG_TAG = "CreateExternalCallA...";
    private static final boolean DEBUG = false;

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        ImageView mImageView;
        File mFile;

        public DownloadImageTask(ImageView imageView, File avatarFile) {
            mImageView = imageView;
            mFile = avatarFile;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bitmap = null;
            try (InputStream in = new java.net.URL(urldisplay).openStream()) {
                bitmap = BitmapFactory.decodeStream(in);
                FileOutputStream outStream = new FileOutputStream(mFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap bitmap) {

            if (bitmap != null) {
                mImageView.setImageBitmap(bitmap);
            }
        }
    }

    static final int REQUEST_SHOW_FEATURE = 2;

    protected static final float DESIGN_SETTINGS_TOP_MARGIN = 32f;

    private EditableView mEditableView;
    private TextView mTitleView;
    private TextView mMessageView;
    private ImageView mNoAvatarView;
    private View mSettingsView;
    private TextView mSettingsTextView;
    private View mLimitedView;
    private SwitchView mLimitedSwitchView;
    private View mStartView;
    private TextView mStartDateTextView;
    private TextView mStartTimeTextView;
    private View mEndView;
    private TextView mEndDateTextView;
    private TextView mEndTimeTextView;

    private View mOverlayMenuView;
    private MenuCallCapabilitiesView mMenuCapabilitiesView;

    private boolean mUIInitialized = false;
    private boolean mUpdated = false;

    private boolean mCreateExternalCall = false;

    private Bitmap mUpdatedCallAvatar;
    private Bitmap mUpdatedCallLargeAvatar;
    private File mUpdatedCallFile;

    private Space mSpace;

    private Date mScheduleStartDate;
    private Time mScheduleStartTime;
    private Date mScheduleEndDate;
    private Time mScheduleEndTime;

    private boolean mAllowVoiceCall = true;
    private boolean mAllowVideoCall = true;
    private boolean mAllowGroupCall = false;
    private boolean mScheduleEnable = false;

    private boolean mIsTransferCall = false;

    private UITemplateExternalCall mUITemplateExternalCall;

    private CallReceiverService mCallReceiverService;

    private boolean mShowPremiumFeatureDescription = false;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        setFullscreen();

        Intent intent = getIntent();
        mIsTransferCall = intent.getBooleanExtra(Intents.INTENT_TRANSFER_CALL, false);

        int selection = intent.getIntExtra(Intents.INTENT_TEMPLATE_SELECTION, -1);
        if (selection != -1) {
            UITemplateExternalCall.TemplateType templateType;
            if (selection == UITemplateExternalCall.TemplateType.CLASSIFIED_AD.ordinal()) {
                templateType = UITemplateExternalCall.TemplateType.CLASSIFIED_AD;
            } else if (selection == UITemplateExternalCall.TemplateType.HELP.ordinal()) {
                templateType = UITemplateExternalCall.TemplateType.HELP;
            } else if (selection == UITemplateExternalCall.TemplateType.MEETING.ordinal()) {
                templateType = UITemplateExternalCall.TemplateType.MEETING;
            } else if (selection == UITemplateExternalCall.TemplateType.VIDEO_BELL.ordinal()) {
                templateType = UITemplateExternalCall.TemplateType.VIDEO_BELL;
            } else {
                templateType = UITemplateExternalCall.TemplateType.OTHER;
            }
            mUITemplateExternalCall = new UITemplateExternalCall(this, templateType);
        }

        initViews();

        if (savedInstanceState != null && mEditableView != null) {
            mEditableView.onCreate(savedInstanceState);
            updateSelectedImage();
        }

        mCallReceiverService = new CallReceiverService(this, getTwinmeContext(), this);
    }

    //
    // Override Activity methods
    //

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (!mShowPremiumFeatureDescription && mIsTransferCall && getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.TRANSFER_CALL)) {
            mShowPremiumFeatureDescription = true;
            showOnboardingView();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SHOW_FEATURE && resultCode == RESULT_OK) {
            finish();
        } else if (mEditableView != null) {
            mEditableView.onActivityResult(requestCode, resultCode, data);

            if (resultCode == Activity.RESULT_OK) {
                updateSelectedImage();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        // Cleanup capture and cropped images.
        if (mEditableView != null) {
            mEditableView.onDestroy();
        }

        mCallReceiverService.dispose();

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);

        if (mEditableView != null) {
            mEditableView.onSaveInstanceState(outState);
        }
    }

    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        if (!mEditableView.onRequestPermissions(grantedPermissions)) {
            message(getString(R.string.application_denied_permissions), 0L, new DefaultMessageCallback(R.string.application_ok) {
            });
        }
    }

    //
    // Implement CallReceiverService.Observer methods
    //

    @Override
    public void onGetSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpace: space=" + space);
        }

        mSpace = space;
    }

    @Override
    public void onGetSpaceNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpaceNotFound");
        }

        finish();
    }

    @Override
    public void onCreateCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateCallReceiver: " + callReceiver);
        }

        if (mCreateExternalCall) {
            mCreateExternalCall = false;

            startActivity(mIsTransferCall ? TransferCallActivity.class : InvitationExternalCallActivity.class, Intents.INTENT_CALL_RECEIVER_ID, callReceiver.getId());
        }

        finish();
    }

    //MenuCallCapabilitiesView.Observer

    @Override
    public void onCloseMenuAnimationEnd() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseMenuAnimationEnd");
        }

        mAllowVoiceCall = mMenuCapabilitiesView.isCapabilitiesOn(MenuCallCapabilitiesView.VOICE_CALL_SWITCH);
        mAllowVideoCall = mMenuCapabilitiesView.isCapabilitiesOn(MenuCallCapabilitiesView.VIDEO_CALL_SWITCH);
        mAllowGroupCall = mMenuCapabilitiesView.isCapabilitiesOn(MenuCallCapabilitiesView.GROUP_CALL_SWITCH);

        mMenuCapabilitiesView.setVisibility(View.INVISIBLE);
        mOverlayMenuView.setVisibility(View.INVISIBLE);

        Window window = getWindow();
        window.setNavigationBarColor(Design.WHITE_COLOR);

        updateCallCapabilities();
    }

    //
    // Private methods
    //

    @SuppressLint({"ClickableViewAccessibility"})
    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.create_external_call_activity);

        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.application_profile));

        mEditableView = new EditableView(this);

        mAvatarView = findViewById(R.id.create_external_call_activity_avatar_view);
        mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);

        mAvatarView.setOnClickListener(v -> openMenuPhoto());

        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = Design.AVATAR_MAX_WIDTH;
        layoutParams.height = Design.AVATAR_MAX_HEIGHT;

        mNoAvatarView = findViewById(R.id.create_external_call_activity_no_avatar_view);
        mNoAvatarView.setVisibility(View.VISIBLE);

        mNoAvatarView.setOnClickListener(v -> openMenuPhoto());

        View backClickableView = findViewById(R.id.create_external_call_activity_back_clickable_view);
        GestureDetector backGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_BACK));
        backClickableView.setOnTouchListener((v, motionEvent) -> {
            backGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        layoutParams = backClickableView.getLayoutParams();
        layoutParams.height = Design.BACK_CLICKABLE_VIEW_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) backClickableView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BACK_CLICKABLE_VIEW_LEFT_MARGIN;
        marginLayoutParams.topMargin = Design.BACK_CLICKABLE_VIEW_TOP_MARGIN;

        RoundedView backRoundedView = findViewById(R.id.create_external_call_activity_back_rounded_view);
        backRoundedView.setColor(Design.BACK_VIEW_COLOR);

        mContentView = findViewById(R.id.create_external_call_activity_content_view);
        mContentView.setY(Design.CONTENT_VIEW_INITIAL_POSITION);

        setBackground(mContentView);

        View slideMarkView = findViewById(R.id.create_external_call_activity_slide_mark_view);
        layoutParams = slideMarkView.getLayoutParams();
        layoutParams.height = Design.SLIDE_MARK_HEIGHT;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.rgb(244, 244, 244));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        ViewCompat.setBackground(slideMarkView, gradientDrawable);

        float corner = ((float)Design.SLIDE_MARK_HEIGHT / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) slideMarkView.getLayoutParams();
        marginLayoutParams.topMargin = Design.SLIDE_MARK_TOP_MARGIN;

        mContentView.setOnTouchListener((v, motionEvent) -> touchContent(motionEvent));

        mTitleView = findViewById(R.id.create_external_call_activity_title_view);
        Design.updateTextFont(mTitleView, Design.FONT_BOLD44);
        mTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View headerView = findViewById(R.id.create_external_call_activity_content_header_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) headerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.HEADER_VIEW_TOP_MARGIN;

        View nameContentView = findViewById(R.id.create_external_call_activity_name_content_view);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable nameViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        nameViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        ViewCompat.setBackground(nameContentView, nameViewBackground);

        layoutParams = nameContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) nameContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_NAME_TOP_MARGIN * Design.HEIGHT_RATIO);

        mNameView = findViewById(R.id.create_external_call_activity_name_view);
        Design.updateTextFont(mNameView, Design.FONT_REGULAR28);
        mNameView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mNameView.setHintTextColor(Design.GREY_COLOR);
        mNameView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NAME_LENGTH)});
        mNameView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                mCounterNameView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), MAX_NAME_LENGTH));
                updateExternalCall();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        GestureDetector nameGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_EDIT_NAME));
        mNameView.setOnTouchListener((v, motionEvent) -> {
            boolean result = nameGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return result;
        });

        mCounterNameView = findViewById(R.id.create_external_call_activity_counter_name_view);
        Design.updateTextFont(mCounterNameView, Design.FONT_REGULAR26);
        mCounterNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterNameView.setText(String.format(Locale.getDefault(), "0/%d", MAX_NAME_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        View descriptionContentView = findViewById(R.id.create_external_call_activity_description_content_view);

        ShapeDrawable descriptionContentViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        descriptionContentViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        ViewCompat.setBackground(descriptionContentView, descriptionContentViewBackground);

        layoutParams = descriptionContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = (int) Design.DESCRIPTION_CONTENT_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) descriptionContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_DESCRIPTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        mDescriptionView = findViewById(R.id.create_external_call_activity_description_view);
        Design.updateTextFont(mDescriptionView, Design.FONT_REGULAR28);
        mDescriptionView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mDescriptionView.setHintTextColor(Design.GREY_COLOR);
        mDescriptionView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_DESCRIPTION_LENGTH)});
        mDescriptionView.addTextChangedListener(new TextWatcher() {

            @SuppressLint("DefaultLocale")
            @Override
            public void afterTextChanged(Editable s) {

                mCounterDescriptionView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), MAX_DESCRIPTION_LENGTH));
                updateExternalCall();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        GestureDetector descriptionGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_EDIT_DESCRIPTION));
        mDescriptionView.setOnTouchListener((v, motionEvent) -> {
            boolean result = descriptionGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return result;
        });

        mCounterDescriptionView = findViewById(R.id.create_external_call_activity_counter_description_view);
        Design.updateTextFont(mCounterDescriptionView, Design.FONT_REGULAR26);
        mCounterDescriptionView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterDescriptionView.setText(String.format(Locale.getDefault(), "0/%d", MAX_DESCRIPTION_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterDescriptionView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        mSettingsView = findViewById(R.id.create_external_call_activity_settings_view);

        GestureDetector settingsGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_SETTINGS));
        mSettingsView.setOnTouchListener((v, motionEvent) -> {
            boolean result = settingsGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return result;
        });

        layoutParams = mSettingsView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.SECTION_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSettingsView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SETTINGS_TOP_MARGIN * Design.HEIGHT_RATIO);

        mSettingsTextView = findViewById(R.id.create_external_call_activity_settings_text_view);
        Design.updateTextFont(mSettingsTextView, Design.FONT_REGULAR34);
        mSettingsTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mLimitedView = findViewById(R.id.create_external_call_activity_limited_view);

        layoutParams = mLimitedView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.SECTION_HEIGHT;

        mLimitedSwitchView = findViewById(R.id.create_external_call_activity_limited_checkbox);
        Design.updateTextFont(mLimitedSwitchView, Design.FONT_REGULAR34);
        mLimitedSwitchView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mLimitedSwitchView.setOnCheckedChangeListener((buttonView, isChecked) -> saveLimited());

        mStartView = findViewById(R.id.create_external_call_activity_start_view);

        layoutParams = mStartView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.SECTION_HEIGHT;

        TextView startTextView = findViewById(R.id.create_external_call_activity_start_text_view);
        Design.updateTextFont(startTextView, Design.FONT_REGULAR34);
        startTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View startDateView = findViewById(R.id.create_external_call_activity_start_date_view);

        GestureDetector startDateGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_START_DATE));
        startDateView.setOnTouchListener((v, motionEvent) -> {
            boolean result = startDateGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return result;
        });

        layoutParams = startDateView.getLayoutParams();
        layoutParams.width = Design.DATE_VIEW_WIDTH;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) startDateView.getLayoutParams();
        marginLayoutParams.rightMargin = Design.DATE_VIEW_MARGIN;

        ShapeDrawable startDateViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        startDateViewBackground.getPaint().setColor(Design.DATE_BACKGROUND_COLOR);
        ViewCompat.setBackground(startDateView, startDateViewBackground);

        mStartDateTextView = findViewById(R.id.create_external_call_activity_start_date_text_view);
        Design.updateTextFont(mStartDateTextView, Design.FONT_REGULAR32);
        mStartDateTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mStartDateTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.DATE_VIEW_PADDING;
        marginLayoutParams.rightMargin = Design.DATE_VIEW_PADDING;

        View startHourView = findViewById(R.id.create_external_call_activity_start_hour_view);

        GestureDetector startHourGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_START_TIME));
        startHourView.setOnTouchListener((v, motionEvent) -> {
            boolean result = startHourGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return result;
        });

        layoutParams = startHourView.getLayoutParams();
        layoutParams.width = Design.HOUR_VIEW_WIDTH;

        ShapeDrawable startHourViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        startHourViewBackground.getPaint().setColor(Design.DATE_BACKGROUND_COLOR);
        ViewCompat.setBackground(startHourView, startHourViewBackground);

        mStartTimeTextView = findViewById(R.id.create_external_call_activity_start_hour_text_view);
        Design.updateTextFont(mStartTimeTextView, Design.FONT_REGULAR32);
        mStartTimeTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mStartTimeTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.DATE_VIEW_PADDING;
        marginLayoutParams.rightMargin = Design.DATE_VIEW_PADDING;

        mEndView = findViewById(R.id.create_external_call_activity_end_view);

        layoutParams = mEndView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.SECTION_HEIGHT;

        TextView endTextView = findViewById(R.id.create_external_call_activity_end_text_view);
        Design.updateTextFont(endTextView, Design.FONT_REGULAR34);
        endTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View endDateView = findViewById(R.id.create_external_call_activity_end_date_view);

        GestureDetector endDateGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_END_DATE));
        endDateView.setOnTouchListener((v, motionEvent) -> {
            boolean result = endDateGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return result;
        });

        layoutParams = endDateView.getLayoutParams();
        layoutParams.width = Design.DATE_VIEW_WIDTH;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) endDateView.getLayoutParams();
        marginLayoutParams.rightMargin = Design.DATE_VIEW_MARGIN;

        ShapeDrawable endDateViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        endDateViewBackground.getPaint().setColor(Design.DATE_BACKGROUND_COLOR);
        ViewCompat.setBackground(endDateView, endDateViewBackground);

        mEndDateTextView = findViewById(R.id.create_external_call_activity_end_date_text_view);
        Design.updateTextFont(mEndDateTextView, Design.FONT_REGULAR32);
        mEndDateTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mEndDateTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.DATE_VIEW_PADDING;
        marginLayoutParams.rightMargin = Design.DATE_VIEW_PADDING;

        View endHourView = findViewById(R.id.create_external_call_activity_end_hour_view);

        GestureDetector endHourGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_END_TIME));
        endHourView.setOnTouchListener((v, motionEvent) -> {
            boolean result = endHourGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return result;
        });

        layoutParams = endHourView.getLayoutParams();
        layoutParams.width = Design.HOUR_VIEW_WIDTH;

        ShapeDrawable endHourViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        endHourViewBackground.getPaint().setColor(Design.DATE_BACKGROUND_COLOR);
        ViewCompat.setBackground(endHourView, endHourViewBackground);

        mEndTimeTextView = findViewById(R.id.create_external_call_activity_end_hour_text_view);
        Design.updateTextFont(mEndTimeTextView, Design.FONT_REGULAR32);
        mEndTimeTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mEndTimeTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.DATE_VIEW_PADDING;
        marginLayoutParams.rightMargin = Design.DATE_VIEW_PADDING;

        mSaveClickableView = findViewById(R.id.create_external_call_activity_save_view);
        mSaveClickableView.setOnClickListener(v -> onSaveClick());

        GestureDetector saveGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_SAVE));
        mSaveClickableView.setOnTouchListener((v, motionEvent) -> {
            saveGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        layoutParams = mSaveClickableView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Design.getMainStyle());
        ViewCompat.setBackground(mSaveClickableView, saveViewBackground);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSaveClickableView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SAVE_TOP_MARGIN * Design.HEIGHT_RATIO);

        TextView saveTextView = findViewById(R.id.create_external_call_activity_save_title_view);
        Design.updateTextFont(saveTextView, Design.FONT_BOLD28);
        saveTextView.setTextColor(Color.WHITE);

        mMessageView = findViewById(R.id.create_external_call_activity_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR32);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        layoutParams = mMessageView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_NAME_TOP_MARGIN * Design.HEIGHT_RATIO);

        mOverlayMenuView = findViewById(R.id.create_external_call_activity_overlay_view);
        mOverlayMenuView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayMenuView.setOnClickListener(view -> closeMenu());

        mMenuCapabilitiesView = findViewById(R.id.create_external_call_activity_menu_call_capabilities_view);
        mMenuCapabilitiesView.setVisibility(View.INVISIBLE);
        mMenuCapabilitiesView.setObserver(this);
        mMenuCapabilitiesView.setActivity(this);

        mProgressBarView = findViewById(R.id.create_external_call_activity_progress_bar);

        mUIInitialized = true;

        initCallReceiver();
        updateCallCapabilities();
        updateSchedule();
    }

    private void updateSelectedImage() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSelectedImage");
        }

        mEditableView.getSelectedImage((String path, Bitmap bitmap, Bitmap largeImage) -> {
            mUpdatedCallFile = new File(path);
            mUpdatedCallAvatar = bitmap;
            mUpdatedCallLargeAvatar = largeImage;
            mUpdated = true;
            mNoAvatarView.setVisibility(View.GONE);
            updateExternalCall();
        });
    }

    private void saveLimited() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveLimited");
        }

        mScheduleEnable = mLimitedSwitchView.isChecked();

        if (mScheduleEnable && mScheduleStartDate == null) {
            initSchedule();
        }

        updateSchedule();
    }

    private void initCallReceiver() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initCallReceiver");
        }

        if (mIsTransferCall) {
            mTitleView.setText(getString(R.string.premium_services_activity_transfert_title));
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.transfert_call_placeholder);
            mAvatarView.setImageBitmap(bitmap);
            mUpdatedCallAvatar = bitmap;
            createFileFromPlaceholder();
            mMessageView.setText(getString(R.string.create_transfert_call_activity_message));
            String name = getString(R.string.create_transfert_call_activity_name_placeholder);
            mNameView.setHint(name);
            mNameView.setText(name);
            mCounterNameView.setText(String.format(Locale.getDefault(), "%d/%d", name.length(), MAX_NAME_LENGTH));
        }

        if (mUITemplateExternalCall != null) {
            if (mUITemplateExternalCall.getTemplateType() != UITemplateExternalCall.TemplateType.OTHER) {
                mNameView.setText(mUITemplateExternalCall.getName());
            }

            mNameView.setHint(mUITemplateExternalCall.getPlaceholder());

            if (mUITemplateExternalCall.getAvatarUrl() != null) {
                mUpdatedCallAvatar = BitmapFactory.decodeResource(getResources(), mUITemplateExternalCall.getAvatarId());
                mAvatarView.setImageBitmap(mUpdatedCallAvatar);
                createFileFromTemplate();
            }

            mAllowVoiceCall = mUITemplateExternalCall.voiceCallAllowed();
            mAllowVideoCall = mUITemplateExternalCall.videoCallAllowed();
            mAllowGroupCall  = mUITemplateExternalCall.groupCallAllowed();
            mScheduleEnable = mUITemplateExternalCall.hasSchedule();

            if (mScheduleEnable && mScheduleStartDate == null) {
                initSchedule();
            }
        }
    }

    private void updateCallCapabilities() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateCallCapabilities");
        }

        SpannableStringBuilder spannableCapabilitesStringBuilder = new SpannableStringBuilder();

        if (mAllowVoiceCall) {
            spannableCapabilitesStringBuilder.append(getString(R.string.show_contact_activity_audio));
        }

        if (mAllowVideoCall) {
            if (!spannableCapabilitesStringBuilder.toString().isEmpty()) {
                spannableCapabilitesStringBuilder.append(", ");
            }
            spannableCapabilitesStringBuilder.append(getString(R.string.show_contact_activity_video));
        }

        if (mAllowGroupCall) {
            if (!spannableCapabilitesStringBuilder.toString().isEmpty()) {
                spannableCapabilitesStringBuilder.append(", ");
            }
            spannableCapabilitesStringBuilder.append(getString(R.string.show_group_activity_title));
        }

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(getString(R.string.show_call_activity_settings_call));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (!spannableCapabilitesStringBuilder.toString().isEmpty()) {
            spannableStringBuilder.append("\n");
            int startSubTitle = spannableStringBuilder.length();
            spannableStringBuilder.append(spannableCapabilitesStringBuilder.toString());
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        mSettingsTextView.setText(spannableStringBuilder);
    }

    private void initSchedule() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initSchedule");
        }

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new java.util.Date(System.currentTimeMillis()));

        calendar.add(Calendar.HOUR, 1);
        calendar.set(Calendar.MINUTE, 0);
        mScheduleStartDate = Date.from(calendar);
        mScheduleStartTime = Time.from(calendar);

        calendar.add(Calendar.HOUR, 1);
        mScheduleEndDate = Date.from(calendar);
        mScheduleEndTime = Time.from(calendar);
    }

    private void updateSchedule() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSchedule");
        }

        if (!mUIInitialized) {
            return;
        }

        if (mIsTransferCall) {
            mSettingsView.setVisibility(View.GONE);
            mLimitedView.setVisibility(View.GONE);
        }

        mLimitedSwitchView.setChecked(mScheduleEnable);

        if (mScheduleEnable) {
            mStartView.setVisibility(View.VISIBLE);
            mEndView.setVisibility(View.VISIBLE);

            final Calendar startCalendar = new DateTime(mScheduleStartDate, mScheduleStartTime).toCalendar(TimeZone.getDefault());

            final Calendar endCalendar = new DateTime(mScheduleEndDate, mScheduleEndTime).toCalendar(TimeZone.getDefault());

            String formatDate = "dd MMM yyyy";
            String formatTime;
            if (DateFormat.is24HourFormat(this)) {
                formatTime = "HH:mm";
            } else {
                formatTime = "hh:mm a";
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatDate, Locale.getDefault());
            mStartDateTextView.setText(simpleDateFormat.format(startCalendar.getTime()));
            mEndDateTextView.setText(simpleDateFormat.format(endCalendar.getTime()));

            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat(formatTime, Locale.getDefault());
            mStartTimeTextView.setText(simpleTimeFormat.format(startCalendar.getTime()));
            mEndTimeTextView.setText(simpleTimeFormat.format(endCalendar.getTime()));
        } else {
            mStartView.setVisibility(View.GONE);
            mEndView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStartDateViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStartDateViewClick");
        }

        final Calendar calendar = Calendar.getInstance();
        int day;
        int month;
        int year;
        if (mScheduleStartDate != null) {
            day = mScheduleStartDate.day;
            month = mScheduleStartDate.month - 1;
            year = mScheduleStartDate.year;
        } else {
            day = calendar.get(Calendar.DAY_OF_MONTH);
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
        }

        DatePickerDialog.OnDateSetListener onDateSetListener = (datePicker, y, m, d) -> {
            mScheduleStartDate = new Date(y, m+1, d);
            updateSchedule();
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    @Override
    protected void onStartTimeViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStartTimeViewClick");
        }

        int hour;
        int minute;
        if (mScheduleStartTime != null) {
            hour = mScheduleStartTime.hour;
            minute = mScheduleStartTime.minute;
        } else {
            final Calendar calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }

        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view, h, m) -> {
            mScheduleStartTime = new Time(h, m);
            updateSchedule();
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);
        timePickerDialog.show();
    }

    @Override
    protected void onEndDateViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEndDateViewClick");
        }

        final Calendar calendar = Calendar.getInstance();
        int day;
        int month;
        int year;
        if (mScheduleEndDate != null) {
            day = mScheduleEndDate.day;
            month = mScheduleEndDate.month - 1;
            year = mScheduleEndDate.year;
        } else {
            day = calendar.get(Calendar.DAY_OF_MONTH);
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
        }

        DatePickerDialog.OnDateSetListener onDateSetListener = (datePicker, y, m, d) -> {
            mScheduleEndDate = new Date(y, m+1, d);
            updateSchedule();
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener, year, month, day);

        if (mScheduleStartDate != null) {
            calendar.set(mScheduleStartDate.year, mScheduleStartDate.month - 1, mScheduleStartDate.day);
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        }

        datePickerDialog.show();
    }

    @Override
    protected void onEndTimeViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEndTimeViewClick");
        }

        int hour;
        int minute;
        if (mScheduleEndTime != null) {
            hour = mScheduleEndTime.hour;
            minute = mScheduleEndTime.minute;
        } else {
            final Calendar calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }

        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view, h, m) -> {
            mScheduleEndTime = new Time(h, m);
            updateSchedule();
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);
        timePickerDialog.show();
    }

    @Override
    protected void onSettingsViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingsViewClick");
        }

        hideKeyboard();
        openMenuCapabilities();
    }

    @Override
    protected void onSaveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveClick");
        }

        if (mCreateExternalCall) {

            return;
        }

        hideKeyboard();

        mCreateExternalCall = true;

        String name = mNameView.getText().toString().trim();
        String description = mDescriptionView.getText().toString().trim();

        Capabilities capabilities = new Capabilities();
        if (mIsTransferCall) {
            capabilities.setCapTransfer(true);

            if (mUpdatedCallFile == null) {
                BitmapDrawable drawable = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.transfert_call_placeholder, null);
                if (drawable != null) {
                    mUpdatedCallLargeAvatar = drawable.getBitmap();
                }
            }
        } else {
            capabilities.setCapAudio(mAllowVoiceCall);
            capabilities.setCapVideo(mAllowVideoCall);
            capabilities.setCapGroupCall(mAllowGroupCall);

            if (mScheduleEnable) {
                DateTime startDateTime = new DateTime(mScheduleStartDate, mScheduleStartTime);
                DateTime startEndTime = new DateTime(mScheduleEndDate, mScheduleEndTime);
                DateTimeRange dateTimeRange = new DateTimeRange(startDateTime, startEndTime);
                Schedule schedule = new Schedule(TimeZone.getDefault(), dateTimeRange);
                schedule.setEnabled(mLimitedSwitchView.isChecked());
                capabilities.setSchedule(schedule);
            }
        }

        mCallReceiverService.createCallReceiver(mSpace, name, description, name, description, mUpdatedCallAvatar, mUpdatedCallFile, capabilities);
    }

    private void updateExternalCall() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateExternalCall");
        }

        if (!mUIInitialized) {

            return;
        }

        Bitmap avatar;
        if (mUpdatedCallLargeAvatar != null) {
            avatar = mUpdatedCallLargeAvatar;
            setUpdated();
            mAvatarView.setImageBitmap(avatar);
            mNoAvatarView.setVisibility(View.GONE);
        } else {
            mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);
            mNoAvatarView.setVisibility(View.VISIBLE);
        }
    }

    private void closeMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeMenu");
        }

        if (mMenuCapabilitiesView.getVisibility() == View.VISIBLE) {
            closeMenuCapabilites();
        }
    }

    private void openMenuCapabilities() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuCapabilities");
        }

        if (mMenuCapabilitiesView.getVisibility() == View.INVISIBLE) {
            mMenuCapabilitiesView.setVisibility(View.VISIBLE);
            mOverlayMenuView.setVisibility(View.VISIBLE);

            Capabilities capabilities = new Capabilities();
            capabilities.setCapAudio(mAllowVoiceCall);
            capabilities.setCapVideo(mAllowVideoCall);
            capabilities.setCapGroupCall(mAllowGroupCall);
            mMenuCapabilitiesView.openMenu(capabilities);

            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
        }
    }

    private void closeMenuCapabilites() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeMenuCapabilites");
        }

        mMenuCapabilitiesView.animationCloseMenu();
    }

    private void openMenuPhoto() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuPhoto");
        }

        hideKeyboard();

        ViewGroup viewGroup = findViewById(R.id.create_external_call_activity_layout);

        MenuPhotoView menuPhotoView = new MenuPhotoView(this, null);

        MenuPhotoView.Observer observer = new MenuPhotoView.Observer() {
            @Override
            public void onCameraClick() {

                menuPhotoView.animationCloseMenu();
                mEditableView.onCameraClick();
            }

            @Override
            public void onPhotoGalleryClick() {

                menuPhotoView.animationCloseMenu();
                mEditableView.onGalleryClick();
            }

            @Override
            public void onCloseMenuSelectActionAnimationEnd() {

                viewGroup.removeView(menuPhotoView);

                Window window = getWindow();
                window.setNavigationBarColor(Design.WHITE_COLOR);
            }
        };

        menuPhotoView.setObserver(observer);
        viewGroup.addView(menuPhotoView);

        List<UIMenuSelectAction> actions = new ArrayList<>();
        actions.add(new UIMenuSelectAction(getString(R.string.application_camera), R.drawable.grey_camera));
        actions.add(new UIMenuSelectAction(getString(R.string.application_photo_gallery), R.drawable.from_gallery));
        menuPhotoView.setActions(actions, this);
        menuPhotoView.openMenu(true);

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
    }

    private void setUpdated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setUpdated mUpdated:" + mUpdated);
        }

        if (!mUpdated) {

            return;
        }

        mSaveClickableView.setAlpha(1.f);
    }
    private void createFileFromPlaceholder() {
        if (DEBUG) {
            Log.d(LOG_TAG, "createFileFromPlaceholder");
        }

        File directory = new File(getFilesDir(), Twinlife.TMP_DIR);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs() || !directory.isDirectory()) {
                return;
            }
        }

        mUpdatedCallFile = new File(directory, "twinlife_" + System.currentTimeMillis() + ".tmp");
        try {
            //noinspection ResultOfMethodCallIgnored
            mUpdatedCallFile.createNewFile();

            @SuppressLint("ResourceType")
            InputStream inputStream = getResources().openRawResource(R.drawable.transfert_call_placeholder);
            OutputStream outputStream = new FileOutputStream(mUpdatedCallFile);

            int length;
            byte[] buffer = new byte[1024];
            while((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

        } catch (IOException exception) {
            mUpdatedCallFile = null;
        }
    }

    private void createFileFromTemplate() {
        if (DEBUG) {
            Log.d(LOG_TAG, "createFileFromTemplate");
        }

        File directory = new File(getFilesDir(), Twinlife.TMP_DIR);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs() || !directory.isDirectory()) {
                return;
            }
        }

        mUpdatedCallFile = new File(directory, "twinlife_" + System.currentTimeMillis() + ".tmp");
        try {
            //noinspection ResultOfMethodCallIgnored
            mUpdatedCallFile.createNewFile();
            DownloadImageTask downloadImageTask = new DownloadImageTask(mAvatarView, mUpdatedCallFile);
            downloadImageTask.execute(mUITemplateExternalCall.getAvatarUrl());
        } catch (IOException exception) {
            mUpdatedCallFile = null;
        }
    }

    private void showOnboardingView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showOnboardingView");
        }

        ViewGroup viewGroup = findViewById(R.id.create_external_call_activity_layout);

        OnboardingDetailView onboardingDetailView = new OnboardingDetailView(this, null);

        UIPremiumFeature uiPremiumFeature = new UIPremiumFeature(this, UIPremiumFeature.FeatureType.TRANSFER_CALL);
        onboardingDetailView.setPremiumFeature(uiPremiumFeature);
        onboardingDetailView.setConfirmTitle(getString(R.string.application_ok));
        onboardingDetailView.setCancelTitle(getString(R.string.application_do_not_display));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                onboardingDetailView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                onboardingDetailView.animationCloseConfirmView();
                getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.TRANSFER_CALL, false);
            }

            @Override
            public void onDismissClick() {
                onboardingDetailView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(onboardingDetailView);

                Window window = getWindow();
                window.setNavigationBarColor(Design.WHITE_COLOR);
            }
        };
        onboardingDetailView.setObserver(observer);
        viewGroup.addView(onboardingDetailView);
        onboardingDetailView.show();

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
    }
}
