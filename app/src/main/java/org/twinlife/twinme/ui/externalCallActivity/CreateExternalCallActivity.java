/*
 *  Copyright (c) 2023-2026 twinlife SA.
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
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.Twinlife;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.Capabilities;
import org.twinlife.twinme.models.LinkValidity;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.models.TwincodeKind;
import org.twinlife.twinme.models.schedule.Date;
import org.twinlife.twinme.models.schedule.DateTime;
import org.twinlife.twinme.models.schedule.DateTimeRange;
import org.twinlife.twinme.models.schedule.Schedule;
import org.twinlife.twinme.models.schedule.Time;
import org.twinlife.twinme.models.schedule.WeeklyTimeRange;
import org.twinlife.twinme.services.CallReceiverService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractEditActivity;
import org.twinlife.twinme.ui.Permission;
import org.twinlife.twinme.ui.externalCallActivity.UIConfigExternalCall.ConfigExternalCallTypeCall;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.ui.privacyActivity.UITimeout;
import org.twinlife.twinme.ui.profiles.MenuPhotoView;
import org.twinlife.twinme.ui.settingsActivity.MenuSelectValueView;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.DownloadImageBackgroundAction;
import org.twinlife.twinme.utils.EditableView;
import org.twinlife.twinme.utils.OnboardingDetailView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.UIMenuSelectAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CreateExternalCallActivity extends AbstractEditActivity implements CallReceiverService.Observer, MenuCallCapabilitiesView.Observer {
    private static final String LOG_TAG = "CreateExternalCallA...";
    private static final boolean DEBUG = false;

    static final int REQUEST_SHOW_FEATURE = 2;

    private EditableView mEditableView;
    private TextView mTitleView;
    private TextView mMessageView;
    private ImageView mNoAvatarView;

    private RecyclerView mSettingsRecyclerView;
    private ExternalCallConfigAdapter mExternalCallConfigAdapter;

    private View mOverlayMenuView;
    private MenuCallCapabilitiesView mMenuCapabilitiesView;

    private boolean mUIInitialized = false;
    private boolean mUpdated = false;

    private boolean mCreateExternalCall = false;

    private Bitmap mUpdatedCallAvatar;
    private Bitmap mUpdatedCallLargeAvatar;
    private File mUpdatedCallFile;

    private Space mSpace;

    private boolean mIsTransferCall = false;

    private UITemplateExternalCall mUITemplateExternalCall;
    private UIConfigExternalCall mConfigExternalCall;

    private CallReceiverService mCallReceiverService;
    @Nullable
    private UITemplateExternalCall.TemplateType mTemplateType;
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
            UITemplateExternalCall.TemplateType[] values = UITemplateExternalCall.TemplateType.values();
            if (selection >= 0 && selection < values.length) {
                mTemplateType = values[selection];
            } else {
                mTemplateType = UITemplateExternalCall.TemplateType.OTHER;
            }
            mUITemplateExternalCall = new UITemplateExternalCall(this, mTemplateType);
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

        updateContentHeight();
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

    @Override
    protected void updateContentHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateContentHeight");
        }

        mSettingsRecyclerView.post(() -> {
            mSettingsRecyclerView.requestLayout();
            ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
            layoutParams.height = (int) (mMessageView.getY() + mMessageView.getHeight() + AVATAR_MAX_SIZE);
            mContentView.setLayoutParams(layoutParams);
        });
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
        initCallReceiver();
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

    @Override
    public void onGetCallReceiverNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCallReceiverNotFound");
        }

        finish();
    }

    @Override
    public void onGetProfileAvatar(@NonNull Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProfileAvatar: " + avatar);
        }

        mUpdatedCallFile = CommonUtils.saveBitmap(avatar);
        mUpdatedCallLargeAvatar = avatar;
        mUpdated = true;
        mNoAvatarView.setVisibility(View.GONE);
        updateExternalCall();
    }

    //MenuCallCapabilitiesView.Observer

    @Override
    public void onCloseMenuAnimationEnd() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseMenuAnimationEnd");
        }

        mConfigExternalCall.setAllowVoiceCall(mMenuCapabilitiesView.isCapabilitiesOn(MenuCallCapabilitiesView.VOICE_CALL_SWITCH));
        mConfigExternalCall.setAllowVideoCall(mMenuCapabilitiesView.isCapabilitiesOn(MenuCallCapabilitiesView.VIDEO_CALL_SWITCH));
        mConfigExternalCall.setAllowGroupCall(mMenuCapabilitiesView.isCapabilitiesOn(MenuCallCapabilitiesView.GROUP_CALL_SWITCH));

        mMenuCapabilitiesView.setVisibility(View.INVISIBLE);
        mOverlayMenuView.setVisibility(View.INVISIBLE);

        Window window = getWindow();
        window.setNavigationBarColor(Design.WHITE_COLOR);

        updateConfig();
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
        setupBackPressedCallBack(R.id.create_external_call_activity_layout);

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
        backClickableView.setOnClickListener(view -> onBackClick());

        layoutParams = backClickableView.getLayoutParams();
        layoutParams.height = Design.BACK_CLICKABLE_VIEW_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) backClickableView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BACK_CLICKABLE_VIEW_LEFT_MARGIN;
        marginLayoutParams.topMargin = Design.BACK_CLICKABLE_VIEW_TOP_MARGIN;

        RoundedView backRoundedView = findViewById(R.id.create_external_call_activity_back_rounded_view);
        backRoundedView.setColor(Design.BACK_VIEW_COLOR);

        mContentView = findViewById(R.id.create_external_call_activity_content_view);
        mContentView.setOnClickListener(view -> hideKeyboard());

        View editAvatarView = findViewById(R.id.create_external_call_activity_edit_avatar_clickable_view);
        editAvatarView.setOnClickListener(view -> openMenuPhoto());

        layoutParams = editAvatarView.getLayoutParams();
        layoutParams.height = AVATAR_MAX_SIZE - Design.ACTION_VIEW_MIN_MARGIN;

        setBackground(mContentView);

        mScrollView = findViewById(R.id.create_external_call_activity_scroll_view);
        ViewTreeObserver viewTreeObserver = mScrollView.getViewTreeObserver();
        viewTreeObserver.addOnScrollChangedListener(() -> {
            if (mScrollPosition == -1) {
                mScrollPosition = AVATAR_OVER_SIZE;
            }

            float delta = mScrollPosition - mScrollView.getScrollY();
            updateAvatarSize(delta);
            mScrollPosition = mScrollView.getScrollY();
        });

        View slideMarkView = findViewById(R.id.create_external_call_activity_slide_mark_view);
        layoutParams = slideMarkView.getLayoutParams();
        layoutParams.width = Design.SLIDE_MARK_WIDTH;
        layoutParams.height = Design.SLIDE_MARK_HEIGHT;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.rgb(244, 244, 244));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        slideMarkView.setBackground(gradientDrawable);

        float corner = ((float)Design.SLIDE_MARK_HEIGHT / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) slideMarkView.getLayoutParams();
        marginLayoutParams.topMargin = Design.SLIDE_MARK_TOP_MARGIN;

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
        nameContentView.setBackground(nameViewBackground);

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

        mNameView.setOnFocusChangeListener((view, focus) -> {
            if (focus) {
                mScrollView.postDelayed(() -> mScrollView.smoothScrollTo(0, mSettingsRecyclerView.getTop()), 100);
            }
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
        descriptionContentView.setBackground(descriptionContentViewBackground);

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

        mDescriptionView.setOnFocusChangeListener((view, focus) -> {
            if (focus) {
                mScrollView.postDelayed(() -> mScrollView.smoothScrollTo(0, mSettingsRecyclerView.getTop()), 100);
            }
        });

        mCounterDescriptionView = findViewById(R.id.create_external_call_activity_counter_description_view);
        Design.updateTextFont(mCounterDescriptionView, Design.FONT_REGULAR26);
        mCounterDescriptionView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterDescriptionView.setText(String.format(Locale.getDefault(), "0/%d", MAX_DESCRIPTION_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterDescriptionView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        mSettingsRecyclerView = findViewById(R.id.create_external_call_activity_settings_view);

        layoutParams = mSettingsRecyclerView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSettingsRecyclerView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_DESCRIPTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        mConfigExternalCall = new UIConfigExternalCall(this, true);

        ExternalCallConfigAdapter.OnExternalCallConfigClickListener onExternalCallConfigClickListener = new ExternalCallConfigAdapter.OnExternalCallConfigClickListener() {
            @Override
            public void onExternalCallConfigClick(UIConfigExternalCallItem configExternalCall) {

                if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.PERMISSIONS) {
                    openMenuCapabilities();
                } else if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.CALL_TYPE) {
                    openMenuSelectValue(MenuSelectValueView.MenuType.EXTERNAL_CALL_TYPE, mConfigExternalCall.getConfigCallType().ordinal());
                } else if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.EXPIRATION) {
                    openMenuSelectValue(MenuSelectValueView.MenuType.EXTERNAL_CALL_EXPIRATION, mConfigExternalCall.getLinkValidity().ordinal());
                }
            }

            @Override
            public void onDateViewClick(UIConfigExternalCallItem configExternalCall) {

                if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.SCHEDULE_START) {
                   onStartDateViewClick();
                } else if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.SCHEDULE_END) {
                   onEndDateViewClick();
                }
            }

            @Override
            public void onTimeViewClick(UIConfigExternalCallItem configExternalCall) {

                if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.SCHEDULE_START) {
                    onStartTimeViewClick();
                } else if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.SCHEDULE_END) {
                    onEndTimeViewClick();
                }
            }

            @Override
            public void onSelectDayClick(UIScheduleDay scheduleDay) {

                onSelectScheduleDayClick(scheduleDay);
            }

            @Override
            public void onSwitchValueChanged(UIConfigExternalCallItem configExternalCall, boolean value) {

                if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.DELETE) {
                    mConfigExternalCall.setDeleteLinkSetting(value);
                } if (configExternalCall.getConfigExternalCallSettings() == UIConfigExternalCall.ConfigExternalCallSettings.NOTIFICATION) {
                    mConfigExternalCall.setNotificationJoinSetting(value);
                }

                updateConfig();
            }
        };

        mExternalCallConfigAdapter = new ExternalCallConfigAdapter(this, mConfigExternalCall, onExternalCallConfigClickListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

        mSettingsRecyclerView.setLayoutManager(linearLayoutManager);
        mSettingsRecyclerView.setAdapter(mExternalCallConfigAdapter);
        mSettingsRecyclerView.setItemAnimator(null);
        mSettingsRecyclerView.setHasFixedSize(false);

        mSaveClickableView = findViewById(R.id.create_external_call_activity_save_view);
        mSaveClickableView.setOnClickListener(v -> onSaveClick());

        layoutParams = mSaveClickableView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Design.getMainStyle());
        mSaveClickableView.setBackground(saveViewBackground);

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
        updateConfig();
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

    private void initCallReceiver() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initCallReceiver");
        }

        if (mSpace == null) {
            return;
        }

        if (mIsTransferCall) {
            mTitleView.setText(getString(R.string.premium_services_view_transfert_title));
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.transfert_call_placeholder);
            mAvatarView.setImageBitmap(bitmap);
            mUpdatedCallAvatar = bitmap;
            createFileFromPlaceholder();
            mMessageView.setText(getString(R.string.create_transfert_call_view_message));
            String name = getString(R.string.create_transfert_call_view_name_placeholder);
            mNameView.setHint(name);
            mNameView.setText(name);
            mCounterNameView.setText(String.format(Locale.getDefault(), "%d/%d", name.length(), MAX_NAME_LENGTH));
        }

        if (mUITemplateExternalCall != null) {
            if (mUITemplateExternalCall.getTemplateType() != UITemplateExternalCall.TemplateType.OTHER) {
                if (mUITemplateExternalCall.getTemplateType() == UITemplateExternalCall.TemplateType.PROFILE && mSpace.getProfile() != null) {
                    mNameView.setText(mSpace.getProfile().getName());
                } else {
                    mNameView.setText(mUITemplateExternalCall.getName());
                }
            }

            mNameView.setHint(mUITemplateExternalCall.getPlaceholder());

            if (mUITemplateExternalCall.getAvatarUrl() != null) {
                mUpdatedCallAvatar = BitmapFactory.decodeResource(getResources(), mUITemplateExternalCall.getAvatarId());
                mAvatarView.setImageBitmap(mUpdatedCallAvatar);
                createFileFromTemplate();
            } else if (mUITemplateExternalCall.getTemplateType() == UITemplateExternalCall.TemplateType.PROFILE) {
                if (mSpace.getProfile() != null) {
                    mCallReceiverService.getImage(mSpace.getProfile().getAvatarId(), (Bitmap avatar) -> mAvatarView.setImageBitmap(avatar));
                    mCallReceiverService.getProfileAvatar(mSpace.getProfile().getAvatarId());
                }
            }

            mConfigExternalCall.initWithTemplate(mUITemplateExternalCall);
        } else {
            mConfigExternalCall.initDefault();
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
        if (mConfigExternalCall.getScheduleStartDate() != null) {
            day = mConfigExternalCall.getScheduleStartDate().day;
            month = mConfigExternalCall.getScheduleStartDate().month - 1;
            year = mConfigExternalCall.getScheduleStartDate().year;
        } else {
            day = calendar.get(Calendar.DAY_OF_MONTH);
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
        }

        DatePickerDialog.OnDateSetListener onDateSetListener = (datePicker, y, m, d) -> {
            mConfigExternalCall.setScheduleStartDate(new Date(y, m+1, d));
            updateConfig();
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
        if (mConfigExternalCall.getScheduleStartTime() != null) {
            hour = mConfigExternalCall.getScheduleStartTime() .hour;
            minute = mConfigExternalCall.getScheduleStartTime() .minute;
        } else {
            final Calendar calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }

        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view, h, m) -> {
            mConfigExternalCall.setScheduleStartTime(new Time(h, m));
            updateConfig();
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
        if (mConfigExternalCall.getScheduleEndDate() != null) {
            day = mConfigExternalCall.getScheduleEndDate().day;
            month = mConfigExternalCall.getScheduleEndDate().month - 1;
            year = mConfigExternalCall.getScheduleEndDate().year;
        } else {
            day = calendar.get(Calendar.DAY_OF_MONTH);
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
        }

        DatePickerDialog.OnDateSetListener onDateSetListener = (datePicker, y, m, d) -> {
            mConfigExternalCall.setScheduleEndDate(new Date(y, m+1, d));
            updateConfig();
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener, year, month, day);

        if (mConfigExternalCall.getScheduleStartDate() != null) {
            calendar.set(mConfigExternalCall.getScheduleStartDate() .year, mConfigExternalCall.getScheduleStartDate() .month - 1, mConfigExternalCall.getScheduleStartDate() .day);
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
        if (mConfigExternalCall.getScheduleEndTime() != null) {
            hour = mConfigExternalCall.getScheduleEndTime().hour;
            minute = mConfigExternalCall.getScheduleEndTime().minute;
        } else {
            final Calendar calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }

        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view, h, m) -> {
            mConfigExternalCall.setScheduleEndTime(new Time(h, m));
            updateConfig();
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);
        timePickerDialog.show();
    }

    private void onSelectScheduleDayClick(UIScheduleDay scheduleDay) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectScheduleDayClick: scheduleDay=" + scheduleDay);
        }

        scheduleDay.setSelected(!scheduleDay.isSelected());
        mConfigExternalCall.updateDaySelected(scheduleDay.getDayOfWeek(), scheduleDay.isSelected());
        updateConfig();
    }

    @Override
    protected void onSettingsViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingsViewClick");
        }

        hideKeyboard();
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

        // Note: with the PROFILE template, we don't give any avatar to the createCallReceiver() as it will copy the profile image.
        String name = mNameView.getText().toString().trim();
        if (name.isEmpty() || (mUpdatedCallAvatar == null && mTemplateType != UITemplateExternalCall.TemplateType.PROFILE)) {
            showAlertMessageView(R.id.create_external_call_activity_layout, getString(R.string.deleted_account_view_warning), getString(R.string.create_external_call_view_name_required), true, null);
            return;
        }

        mCreateExternalCall = true;

        String description = mDescriptionView.getText().toString().trim();
        Capabilities capabilities;

        if (mIsTransferCall) {
            capabilities = new Capabilities();
            capabilities.setCapTransfer(true);

            if (mUpdatedCallFile == null) {
                BitmapDrawable drawable = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.transfert_call_placeholder, null);
                if (drawable != null) {
                    mUpdatedCallLargeAvatar = drawable.getBitmap();
                }
            }
        } else {
            if (mConfigExternalCall.getConfigCallType() == ConfigExternalCallTypeCall.CALL_CONFERENCE) {
                capabilities = new Capabilities(TwincodeKind.CONFERENCE, false);
            } else {
                capabilities = new Capabilities();
            }

            capabilities.setCapAudio(mConfigExternalCall.allowVoiceCall());
            capabilities.setCapVideo(mConfigExternalCall.allowVideoCall());
            capabilities.setCapGroupCall(mConfigExternalCall.allowGroupCall());
            capabilities.setCapNotifyJoin(mConfigExternalCall.notificationJoinCall());
            capabilities.setLinkValidity(mConfigExternalCall.getLinkValidity());

            if (mConfigExternalCall.getLinkValidity() == LinkValidity.SINGLE_USE) {
                DateTime startDateTime = new DateTime(mConfigExternalCall.getScheduleStartDate(), mConfigExternalCall.getScheduleStartTime());
                DateTime startEndTime = new DateTime(mConfigExternalCall.getScheduleEndDate(), mConfigExternalCall.getScheduleEndTime());
                DateTimeRange dateTimeRange = new DateTimeRange(startDateTime, startEndTime);
                Schedule schedule = new Schedule(TimeZone.getDefault(), dateTimeRange);
                schedule.setEnabled(true);
                capabilities.setSchedule(schedule);
            } else if (mConfigExternalCall.getLinkValidity() == LinkValidity.PERIODIC) {
                WeeklyTimeRange weeklyTimeRange = new WeeklyTimeRange(mConfigExternalCall.getSelectedDaysOfWeek(), mConfigExternalCall.getScheduleStartTime(), mConfigExternalCall.getScheduleEndTime());
                Schedule schedule = new Schedule(TimeZone.getDefault(), weeklyTimeRange);
                schedule.setEnabled(true);
                capabilities.setSchedule(schedule);
            }
        }

        mCallReceiverService.createCallReceiver(mSpace, name, description, mUpdatedCallAvatar, mUpdatedCallFile, capabilities);
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
            closeMenuCapabilities();
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
            capabilities.setCapAudio(mConfigExternalCall.allowVoiceCall());
            capabilities.setCapVideo(mConfigExternalCall.allowVideoCall());
            capabilities.setCapGroupCall(mConfigExternalCall.allowGroupCall());
            mMenuCapabilitiesView.openMenu(capabilities);

            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
        }
    }

    private void closeMenuCapabilities() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeMenuCapabilities");
        }

        mMenuCapabilitiesView.animationCloseMenu();
    }

    private void openMenuSelectValue(MenuSelectValueView.MenuType menuType, int selectedValue) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuSelectValue");
        }

        ViewGroup viewGroup = findViewById(R.id.create_external_call_activity_layout);

        MenuSelectValueView menuSelectValueView = new MenuSelectValueView(this, null);

        menuSelectValueView.setActivity(this);
        menuSelectValueView.setObserver(new MenuSelectValueView.Observer() {
            @Override
            public void onCloseMenuAnimationEnd() {

                viewGroup.removeView(menuSelectValueView);

                Window window = getWindow();
                window.setNavigationBarColor(Design.WHITE_COLOR);
            }

            @Override
            public void onSelectValue(int value) {

                menuSelectValueView.animationCloseMenu();

                if (menuType == MenuSelectValueView.MenuType.EXTERNAL_CALL_TYPE) {
                    mConfigExternalCall.setConfigCallType(ConfigExternalCallTypeCall.values()[value]);
                } else if (menuType == MenuSelectValueView.MenuType.EXTERNAL_CALL_EXPIRATION) {
                    mConfigExternalCall.setLinkValidity(LinkValidity.values()[value]);
                }

                 updateConfig();
            }

            @Override
            public void onSelectTimeout(UITimeout timeout) {

            }
        });

        viewGroup.addView(menuSelectValueView);
        menuSelectValueView.openMenu(menuType, selectedValue);

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
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
            public void onCloseAbstractMenuViewAnimationEnd() {

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
            DownloadImageBackgroundAction downloadImageTask = new DownloadImageBackgroundAction(getTwinmeContext(), mAvatarView, mUpdatedCallFile, mUITemplateExternalCall.getAvatarUrl());
            downloadImageTask.start();
        } catch (IOException exception) {
            mUpdatedCallFile = null;
        }
    }

    private void updateConfig() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateConfig");
        }

        if (mIsTransferCall) {
            mSettingsRecyclerView.setVisibility(View.GONE);
        }

        mExternalCallConfigAdapter.updateConfigItems(mConfigExternalCall);
        updateContentHeight();
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
