/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.externalCallActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.services.CallReceiverService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;

import java.util.ArrayList;
import java.util.List;

public class TemplateExternalCallActivity extends AbstractTwinmeActivity implements CallReceiverService.Observer {
    private static final String LOG_TAG = "TemplateExternalCall...";
    private static final boolean DEBUG = false;

    private final List<UITemplateExternalCall> mUITemplates = new ArrayList<>();

    private CallReceiverService mCallReceiverService;

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

        initTemplates();
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

        setTitle(getString(R.string.template_space_activity_template_title));

        TemplateExternalCallAdapter.OnTemplateExternalCallClickListener onTemplateClickListener = this::onTemplateClick;

        TemplateExternalCallAdapter templateExternalCallAdapter = new TemplateExternalCallAdapter(this, mUITemplates, onTemplateClickListener);
        LinearLayoutManager uiSpacesLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView templateRecyclerView = findViewById(R.id.template_external_call_activity_list_view);
        templateRecyclerView.setLayoutManager(uiSpacesLinearLayoutManager);
        templateRecyclerView.setAdapter(templateExternalCallAdapter);
        templateRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        templateRecyclerView.setItemAnimator(null);

        mProgressBarView = findViewById(R.id.template_external_call_activity_progress_bar);
    }

    private void initTemplates() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initTemplates");
        }

        mUITemplates.clear();
        mUITemplates.add(new UITemplateExternalCall(this, UITemplateExternalCall.TemplateType.MEETING));
        mUITemplates.add(new UITemplateExternalCall(this, UITemplateExternalCall.TemplateType.HELP));
        mUITemplates.add(new UITemplateExternalCall(this, UITemplateExternalCall.TemplateType.CLASSIFIED_AD));
        mUITemplates.add(new UITemplateExternalCall(this, UITemplateExternalCall.TemplateType.VIDEO_BELL));
        mUITemplates.add(new UITemplateExternalCall(this, UITemplateExternalCall.TemplateType.OTHER));
    }

    private void onTemplateClick(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTemplateClick: position=" + position);
        }

        if (position >= 0 && position < mUITemplates.size()) {
            Intent intent = new Intent();
            intent.putExtra(Intents.INTENT_TEMPLATE_SELECTION, mUITemplates.get(position).getTemplateType().ordinal());
            intent.setClass(this, CreateExternalCallActivity.class);
            startActivity(intent);
        }
    }
}
