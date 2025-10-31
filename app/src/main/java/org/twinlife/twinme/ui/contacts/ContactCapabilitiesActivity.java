/*
 *  Copyright (c) 2021-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.contacts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Capabilities;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.schedule.DateTime;
import org.twinlife.twinme.models.schedule.DateTimeRange;
import org.twinlife.twinme.models.schedule.Schedule;
import org.twinlife.twinme.services.EditContactCapabilitiesService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;

import java.util.Collections;
import java.util.TimeZone;
import java.util.UUID;

public class ContactCapabilitiesActivity extends AbstractCapabilitiesActivity implements EditContactCapabilitiesService.Observer {
    private static final String LOG_TAG = "ContactCapabilitiesA...";
    private static final boolean DEBUG = false;

    private Contact mContact;

    private EditContactCapabilitiesService mEditContactCapabiltiesService;

    private boolean mUIInitialized = false;
    private final boolean mUIPostInitialized = false;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mEditContactCapabiltiesService = new EditContactCapabilitiesService(this, getTwinmeContext(), this);

        Intent intent = getIntent();
        String contactId = intent.getStringExtra(Intents.INTENT_CONTACT_ID);
        if (contactId != null) {
            mEditContactCapabiltiesService.getContact(UUID.fromString(contactId));
        } else {
            finish();
        }
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        if (mCanSave || mScheduleEnable) {
            mCapabilities.setCapAudio(mAllowAudioCall);
            mCapabilities.setCapVideo(mAllowVideoCall);

            if (mScheduleEnable) {
                DateTime start = new DateTime(mScheduleStartDate, mScheduleStartTime);
                DateTime end = new DateTime(mScheduleEndDate, mScheduleEndTime);
                Schedule schedule = new Schedule(false, TimeZone.getDefault(), Collections.singletonList(new DateTimeRange(start, end)));
                mCapabilities.setSchedule(schedule);
            }

            mEditContactCapabiltiesService.updateContact(mContact, mCapabilities, null);
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mEditContactCapabiltiesService.dispose();

        super.onDestroy();
    }

    //
    // Implement EditContactCapabilitiesService.Observer methods
    //

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: contact=" + contact);
        }

        mContact = contact;

        final String capabilities = contact.getIdentityCapabilities().toAttributeValue();
        mCapabilities = capabilities == null ? new Capabilities() : new Capabilities(capabilities);

        mAllowAudioCall = mCapabilities.hasAudio();
        mAllowVideoCall = mCapabilities.hasVideo();
        mZoomable = mCapabilities.getZoomable();
        mDiscreetRelation = mCapabilities.hasDiscreet();

        if (mUIInitialized) {
            mCapabilitiesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }

        finish();
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: contact=" + contact);
        }

        finish();
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.contact_capabilities_activity);

        setStatusBarColor();
        setToolBar(R.id.contact_capabilities_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.contact_capabilities_activity_call_settings));
        applyInsets(R.id.capabilities_activity_layout, R.id.contact_capabilities_activity_tool_bar, R.id.contact_capabilities_activity_list_view, Design.TOOLBAR_COLOR, false);

        mCapabilitiesAdapter = new CapabilitiesAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView settingsRecyclerView = findViewById(R.id.contact_capabilities_activity_list_view);
        settingsRecyclerView.setLayoutManager(linearLayoutManager);
        settingsRecyclerView.setAdapter(mCapabilitiesAdapter);
        settingsRecyclerView.setItemAnimator(null);
        settingsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mProgressBarView = findViewById(R.id.contact_capabilities_activity_progress_bar);

        mUIInitialized = true;
    }
}
