/*
 *  Copyright (c) 2020-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumFeatureConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.FileInfo;

public class MessagesSettingsActivity extends AbstractSettingsActivity implements MenuSelectValueView.Observer {
    private static final String LOG_TAG = "MessagesSettingsActi...";
    private static final boolean DEBUG = false;

    private static final int REQUEST_PICK_DIRECTORY = 1;

    private MessagesSettingsAdapter mMessagesSettingsAdapter;

    private MenuSelectValueView mMenuSelectValueView;
    private View mOverlayView;

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;
    private MenuSelectValueView.MenuType mMenuType = MenuSelectValueView.MenuType.IMAGE;

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
    }

    //
    // Override Activity methods
    //

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onWindowFocusChanged: hasFocus=" + hasFocus);
        }

        if (hasFocus && mUIInitialized && !mUIPostInitialized) {
            postInitViews();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult: requestCode=" + requestCode + " requestCode=" + resultCode + " intent=" + intent);
        }

        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_DIRECTORY) {
                Uri uri = intent.getData();
                String path;
                String documentId;

                Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri));
                FileInfo fileInfo = new FileInfo(this, documentUri);
                path = fileInfo.getPath();

                String[] projection = {
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME};

                Cursor cursor = getContentResolver().query(documentUri, projection, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        documentId = FileInfo.getColumnString(cursor, DocumentsContract.Document.COLUMN_DOCUMENT_ID);

                        if (path == null) {
                            path = "/" + FileInfo.getColumnString(cursor, DocumentsContract.Document.COLUMN_DISPLAY_NAME);
                        }

                        Settings.defaultDocumentIdToSave.setString(documentId);
                    }
                    cursor.close();
                }

                if (path == null) {
                    path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                }

                if (uri != null) {
                    Settings.defaultUriAuthorityToSave.setString(uri.getAuthority());
                }
                Settings.defaultDirectoryToSave.setString(path).save();

                mMessagesSettingsAdapter.notifyDataSetChanged();
            }
        }
    }

    //MenuSelectValueView.Observer

    @Override
    public void onCloseMenuAnimationEnd() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseMenuAnimationEnd");
        }

        mMenuSelectValueView.setVisibility(View.INVISIBLE);
        mOverlayView.setVisibility(View.INVISIBLE);
        setStatusBarColor();
    }

    @Override
    public void onSelectValue(int value) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectValue: " + value);
        }

        closeMenuSelectValue();

        if (mMenuType == MenuSelectValueView.MenuType.IMAGE) {
            Settings.reduceSizeImage.setInt(value).save();
        } else if (mMenuType == MenuSelectValueView.MenuType.VIDEO) {
            Settings.reduceSizeVideo.setInt(value).save();
        } else if (mMenuType == MenuSelectValueView.MenuType.DISPLAY_CALLS) {
            Settings.displayCallsMode.setInt(value).save();
        }

        mMessagesSettingsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSettingClick(@NonNull UISetting<?> setting) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSettingClick");
        }

        if (setting.getTypeSetting() == UISetting.TypeSetting.DIRECTORY) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, getTwinmeApplication().defaultUriToSaveFiles());
            }
            startActivityForResult(intent, REQUEST_PICK_DIRECTORY);
        } else if (setting.getTypeSetting() == UISetting.TypeSetting.VALUE) {
            if (setting.isSetting(Settings.reduceSizeImage)) {
                mMenuType = MenuSelectValueView.MenuType.IMAGE;
            } else if (setting.isSetting(Settings.reduceSizeVideo)) {
                mMenuType = MenuSelectValueView.MenuType.VIDEO;
            } else {
                mMenuType = MenuSelectValueView.MenuType.DISPLAY_CALLS;
            }
            openMenuSelectValue();
        }
    }

    @Override
    public void onRingToneClick(@NonNull UISetting<String> setting) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRingToneClick");
        }
    }

    public void onPremiumFeatureClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPremiumFeatureClick");
        }

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.messages_settings_activity_layout);

        PremiumFeatureConfirmView premiumFeatureConfirmView = new PremiumFeatureConfirmView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        premiumFeatureConfirmView.setLayoutParams(layoutParams);
        premiumFeatureConfirmView.initWithPremiumFeature(new UIPremiumFeature(this, UIPremiumFeature.FeatureType.PRIVACY));

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                premiumFeatureConfirmView.redirectStore();
            }

            @Override
            public void onCancelClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                percentRelativeLayout.removeView(premiumFeatureConfirmView);
                setStatusBarColor();
            }
        };
        premiumFeatureConfirmView.setObserver(observer);

        percentRelativeLayout.addView(premiumFeatureConfirmView);
        premiumFeatureConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.messages_settings_activity);

        setStatusBarColor();
        setToolBar(R.id.messages_settings_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.settings_activity_chat_category_title));
        applyInsets(R.id.messages_settings_activity_layout, R.id.messages_settings_activity_tool_bar, R.id.messages_settings_activity_list_view, Design.TOOLBAR_COLOR, false);

        mMessagesSettingsAdapter = new MessagesSettingsAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView settingsRecyclerView = findViewById(R.id.messages_settings_activity_list_view);
        settingsRecyclerView.setLayoutManager(linearLayoutManager);
        settingsRecyclerView.setAdapter(mMessagesSettingsAdapter);
        settingsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        settingsRecyclerView.setItemAnimator(null);

        mProgressBarView = findViewById(R.id.messages_settings_activity_progress_bar);

        mOverlayView = findViewById(R.id.messages_settings_activity_overlay_view);
        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setOnClickListener(view -> closeMenuSelectValue());

        mMenuSelectValueView = findViewById(R.id.messages_settings_activity_menu_select_value_view);
        mMenuSelectValueView.setVisibility(View.INVISIBLE);
        mMenuSelectValueView.setObserver(this);
        mMenuSelectValueView.setActivity(this);

        mUIInitialized = true;
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }

    private void openMenuSelectValue() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuSelectValue");
        }

        if (mMenuSelectValueView.getVisibility() == View.INVISIBLE) {
            mMenuSelectValueView.setVisibility(View.VISIBLE);
            mOverlayView.setVisibility(View.VISIBLE);

            if (mMenuType == MenuSelectValueView.MenuType.IMAGE) {
                mMenuSelectValueView.openMenu(MenuSelectValueView.MenuType.IMAGE);
            } else if (mMenuType == MenuSelectValueView.MenuType.VIDEO) {
                mMenuSelectValueView.openMenu(MenuSelectValueView.MenuType.VIDEO);
            } else {
                mMenuSelectValueView.openMenu(MenuSelectValueView.MenuType.DISPLAY_CALLS);
            }

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    private void closeMenuSelectValue() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeMenuSelectValue");
        }

        mMenuSelectValueView.animationCloseMenu();
    }
}
