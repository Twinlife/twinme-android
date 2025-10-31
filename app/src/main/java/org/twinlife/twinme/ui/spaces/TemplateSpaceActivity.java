/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.EditSpaceService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TemplateSpaceActivity extends AbstractTwinmeActivity implements EditSpaceService.Observer {
    private static final String LOG_TAG = "TemplateSpaceActivity";
    private static final boolean DEBUG = false;

    private final List<UITemplateSpace> mUITemplateSpaces = new ArrayList<>();

    private EditSpaceService mEditSpaceService;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mEditSpaceService = new EditSpaceService(this, getTwinmeContext(), this, null);

        initTemplates();
        initViews();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();

        mEditSpaceService.dispose();
    }

    //
    // Implement SpaceService.Observer methods
    //

    @Override
    public void onGetSpace(@NonNull Space space, Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpace: space=" + space);
        }
    }

    @Override
    public void onGetSpaceNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpaceNotFound");
        }
    }

    @Override
    public void onSetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpace: space=" + space);
        }
    }

    @Override
    public void onCreateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateSpace: space=" + space);
        }

        finish();
    }

    @Override
    public void onUpdateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateSpace: space=" + space);
        }
    }

    @Override
    public void onCreateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateProfile: profile=" + profile);
        }
    }

    @Override
    public void onUpdateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateProfile: profile=" + profile);
        }
    }

    @Override
    public void onGetContacts(@NonNull List<Contact> contacts) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContacts: contacts=" + contacts);
        }
    }

    @Override
    public void onGetGroups(@NonNull List<Group> groups) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroups: groups=" + groups);
        }
    }

    @Override
    public void onDeleteSpace(@NonNull UUID spaceId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteSpace: spaceId=" + spaceId);
        }
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.template_space_activity);

        setStatusBarColor();
        setToolBar(R.id.template_space_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.template_space_activity_template_title));

        applyInsets(R.id.template_space_activity_layout, R.id.template_space_activity_tool_bar, R.id.template_space_activity_list_view, Design.TOOLBAR_COLOR, false);

        TemplateSpaceAdapter.OnTemplateSpaceClickListener onTemplateSpaceClickListener = this::onSpaceClick;

        TemplateSpaceAdapter templateSpaceAdapter = new TemplateSpaceAdapter(this, mUITemplateSpaces, onTemplateSpaceClickListener);
        LinearLayoutManager uiSpacesLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView templateRecyclerView = findViewById(R.id.template_space_activity_list_view);
        templateRecyclerView.setLayoutManager(uiSpacesLinearLayoutManager);
        templateRecyclerView.setAdapter(templateSpaceAdapter);
        templateRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        templateRecyclerView.setItemAnimator(null);
        templateRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mProgressBarView = findViewById(R.id.template_space_activity_progress_bar);
    }

    private void initTemplates() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initTemplates");
        }

        mUITemplateSpaces.clear();
        mUITemplateSpaces.add(new UITemplateSpace(this, UITemplateSpace.TemplateType.FAMILY_1));
        mUITemplateSpaces.add(new UITemplateSpace(this, UITemplateSpace.TemplateType.FAMILY_2));
        mUITemplateSpaces.add(new UITemplateSpace(this, UITemplateSpace.TemplateType.FRIENDS_1));
        mUITemplateSpaces.add(new UITemplateSpace(this, UITemplateSpace.TemplateType.FRIENDS_2));
        mUITemplateSpaces.add(new UITemplateSpace(this, UITemplateSpace.TemplateType.BUSINESS_1));
        mUITemplateSpaces.add(new UITemplateSpace(this, UITemplateSpace.TemplateType.BUSINESS_2));
        mUITemplateSpaces.add(new UITemplateSpace(this, UITemplateSpace.TemplateType.OTHER));
    }

    private void onSpaceClick(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSpaceClick: position=" + position);
        }

        if (position >= 0 && position < mUITemplateSpaces.size()) {
            Intent intent = new Intent();
            intent.putExtra(Intents.INTENT_SPACE_SELECTION, mUITemplateSpaces.get(position).getTemplateType().ordinal());
            intent.setClass(this, EditSpaceActivity.class);
            startActivity(intent);
        }
    }
}
