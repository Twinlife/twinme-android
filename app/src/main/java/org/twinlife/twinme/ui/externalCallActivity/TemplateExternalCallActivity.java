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
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.CallReceiverService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.OnboardingConfirmView;

public class TemplateExternalCallActivity extends AbstractTwinmeActivity implements CallReceiverService.Observer {
    private static final String LOG_TAG = "TemplateExternalCall...";
    private static final boolean DEBUG = false;

    private CallReceiverService mCallReceiverService;

    private TemplateExternalCallAdapter mTemplateExternalCallAdapter;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mCallReceiverService = new CallReceiverService(this, getTwinmeContext(), this);

        initViews();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mCallReceiverService.dispose();

        super.onDestroy();
    }

    //
    // Implement CallReceiverService.Observer methods
    //

    @Override
    public void onCreateCallReceiver(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateCallReceiver: " + callReceiver);
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
    public void onGetSpace(Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpace: " + space);
        }

        if (space.getProfile() != null) {
            mCallReceiverService.getImage(space.getProfile().getAvatarId(), (Bitmap avatar) -> {
                mTemplateExternalCallAdapter.updateProfileTemplate(space.getProfile().getName(), avatar);
            });
        }
    }

    public void showOnboardingView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showOnboardingView");
        }

        ViewGroup viewGroup = findViewById(R.id.template_external_call_activity_layout);

        OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);
        onboardingConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.onboarding_click_to_call, null));
        onboardingConfirmView.setTitle(getString(R.string.premium_services_view_click_to_call_title));
        onboardingConfirmView.setMessage(getString(R.string.create_external_call_view_onboarding_part_1_message_1));
        onboardingConfirmView.setConfirmTitle(getString(R.string.application_ok));
        onboardingConfirmView.hideCancelView();

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(onboardingConfirmView);
                setStatusBarColor();
            }
        };
        onboardingConfirmView.setObserver(observer);
        viewGroup.addView(onboardingConfirmView);
        onboardingConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.template_external_call_activity);

        setStatusBarColor();
        setToolBar(R.id.template_external_call_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.template_space_view_template_title));

        applyInsets(R.id.template_external_call_activity_layout, R.id.template_external_call_activity_tool_bar, R.id.template_external_call_activity_list_view, Design.TOOLBAR_COLOR, false);

        TemplateExternalCallAdapter.OnTemplateExternalCallClickListener onTemplateClickListener = this::onTemplateClick;

        mTemplateExternalCallAdapter = new TemplateExternalCallAdapter(this, onTemplateClickListener);
        LinearLayoutManager uiSpacesLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView templateRecyclerView = findViewById(R.id.template_external_call_activity_list_view);
        templateRecyclerView.setLayoutManager(uiSpacesLinearLayoutManager);
        templateRecyclerView.setAdapter(mTemplateExternalCallAdapter);
        templateRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        templateRecyclerView.setItemAnimator(null);
        templateRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mProgressBarView = findViewById(R.id.template_external_call_activity_progress_bar);
    }

    private void onTemplateClick(UITemplateExternalCall templateExternalCall) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTemplateClick: position=" + templateExternalCall);
        }

        if (templateExternalCall != null) {
            Intent intent = new Intent();
            intent.putExtra(Intents.INTENT_TEMPLATE_SELECTION, templateExternalCall.getTemplateType().ordinal());
            intent.setClass(this, CreateExternalCallActivity.class);
            startActivity(intent);
        }
    }
}
