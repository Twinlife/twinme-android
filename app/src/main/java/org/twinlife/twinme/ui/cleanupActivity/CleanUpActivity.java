/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.cleanupActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.twinme.export.ExportState;
import org.twinlife.twinme.export.ExportStats;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.CleanUpService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.contacts.DeleteConfirmView;
import org.twinlife.twinme.ui.exportActivity.UIExport;
import org.twinlife.twinme.ui.inAppSubscriptionActivity.InAppSubscriptionActivity;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumFeatureConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.ui.spaces.DeleteSpaceConfirmView;
import org.twinlife.twinme.utils.AbstractBottomSheetView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CleanUpActivity extends AbstractTwinmeActivity implements CleanUpService.Observer {
    private static final String LOG_TAG = "CleanUpActivity";
    private static final boolean DEBUG = false;

    private CleanUpAdapter mCleanUpAdapter;

    private final List<UIExport> mContents = new ArrayList<>();
    private final List<UIStorage> mStorages = new ArrayList<>();
    private UICleanUpExpiration.ExpirationPeriod mDefaultExpirationPeriod = UICleanUpExpiration.ExpirationPeriod.THREE_MONTHS;
    private UICleanUpExpiration.ExpirationType mDefaultExpirationType = UICleanUpExpiration.ExpirationType.VALUE;
    private Date mDefaultExpirationDate = new Date();
    private UICleanUpExpiration mUICleanUpExpiration = new UICleanUpExpiration(UICleanUpExpiration.ExpirationType.VALUE, UICleanUpExpiration.ExpirationPeriod.THREE_MONTHS);

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;

    private boolean mLocalCleanUpOnly = false;
    private boolean mInitContentToClean = false;

    private CleanUpService mCleanupService;
    private String mConversationName;

    private Contact mContact;
    private Group mGroup;
    private Space mSpace;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mLocalCleanUpOnly = intent.getBooleanExtra(Intents.INTENT_LOCAL_CLEANUP, false);

        initViews();

        UUID contactId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));
        UUID groupId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_GROUP_ID));
        UUID spaceId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_SPACE_ID));
        mCleanupService = new CleanUpService(this, getTwinmeContext(), this, spaceId, contactId, groupId);

        ConversationService.Descriptor.Type[] filter = new ConversationService.Descriptor.Type[]{ConversationService.Descriptor.Type.OBJECT_DESCRIPTOR, ConversationService.Descriptor.Type.IMAGE_DESCRIPTOR, ConversationService.Descriptor.Type.VIDEO_DESCRIPTOR, ConversationService.Descriptor.Type.AUDIO_DESCRIPTOR, ConversationService.Descriptor.Type.NAMED_FILE_DESCRIPTOR};
        mCleanupService.setTypeFilter(filter);
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
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mCleanupService.dispose();

        super.onDestroy();
    }

    public boolean isLocalCleanUpOnly() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isLocalCleanUpOnly");
        }

        return mLocalCleanUpOnly;
    }

    public void onActionClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActionClick");
        }

        if(!isFeatureSubscribed(TwinmeApplication.Feature.GROUP_CALL)) {
            ViewGroup viewGroup = findViewById(R.id.cleanup_activity_layout);

            PremiumFeatureConfirmView premiumFeatureConfirmView = new PremiumFeatureConfirmView(this, null);
            premiumFeatureConfirmView.initWithPremiumFeature(new UIPremiumFeature(this, UIPremiumFeature.FeatureType.CONVERSATION));

            AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
                @Override
                public void onConfirmClick() {
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), InAppSubscriptionActivity.class);
                    startActivity(intent);
                    premiumFeatureConfirmView.animationCloseConfirmView();
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
                    viewGroup.removeView(premiumFeatureConfirmView);
                    setStatusBarColor();
                }
            };
            premiumFeatureConfirmView.setObserver(observer);

            viewGroup.addView(premiumFeatureConfirmView);
            premiumFeatureConfirmView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        } else if (canCleanup()) {
            if (mContact != null) {
                mCleanupService.getImage(mContact, this::showConfirmView);
            } else if (mGroup != null) {
                mCleanupService.getImage(mGroup, this::showConfirmView);
            } else if (mSpace != null) {
                mCleanupService.getSpaceImage(mSpace, this::showConfirmView);
            } else {
                showConfirmView(null);
            }
        }
    }

    //
    // Implements CleanUpService.Observer
    //
    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: " + contact);
        }

        mContact = contact;
        mConversationName = contact.getName();
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
            Log.d(LOG_TAG, "onUpdateContact: " + contact);
        }
    }

    @Override
    public void onGetGroup(@NonNull Group group, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup: " + group);
        }

        mGroup = group;
        mConversationName = group.getName();
    }

    @Override
    public void onGetGroupNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroupNotFound");
        }

        finish();
    }

    @Override
    public void onGetSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpace: " + space);
        }

        mSpace = space;
        mConversationName = space.getName();
    }

    @Override
    public void onProgress(@NonNull ExportState state, @NonNull ExportStats stats) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onProgress: " + state + " stats=" + stats);
        }

        if (state == ExportState.EXPORT_WAIT) {
            updateContent(stats);
        }
    }

    @Override
    public void onError(@NonNull String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError: " + message);
        }

    }

    @Override
    public void onClearConversation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onClearConversation");
        }

        Toast.makeText(this, R.string.cleanup_activity_success, Toast.LENGTH_SHORT).show();
        finish();
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.cleanup_activity);

        setStatusBarColor();
        setToolBar(R.id.cleanup_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        applyInsets(R.id.cleanup_activity_layout, R.id.cleanup_activity_tool_bar, R.id.cleanup_activity_list_view, Design.TOOLBAR_COLOR, false);

        if (mLocalCleanUpOnly) {
            setTitle(getString(R.string.cleanup_activity_local_cleanup_title));
        } else {
            setTitle(getString(R.string.cleanup_activity_both_clean_title));
        }

        initExport();
        initStorage();

        CleanUpAdapter.OnCleanupClickListener onCleanupClickListener = new CleanUpAdapter.OnCleanupClickListener() {
            @Override
            public void onContentClick(UIExport content) {

                if (content.getCount() > 0) {
                    content.setChecked(!content.isChecked());

                    if (content.getExportContentType() == UIExport.ExportContentType.ALL && content.isChecked()) {
                        UIExport contentMedia = mContents.get(0);
                        contentMedia.setChecked(false);
                    } else if (content.getExportContentType() == UIExport.ExportContentType.MEDIA_AND_FILE && content.isChecked()) {
                        UIExport contentAll = mContents.get(1);
                        contentAll.setChecked(false);
                    }

                    mCleanUpAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onSelectExpiration() {

                openMenuCleanupExpiration();
            }
        };

        CleanupSwitchViewHolder.Observer cleanupExpirationSwitchObserver = value -> {
            if (DEBUG) {
                Log.d(LOG_TAG, "onCleanupExpirationChangeValue: " + value);
            }

            if (value) {
                mDefaultExpirationType = mUICleanUpExpiration.getExpirationType();
                mDefaultExpirationPeriod = mUICleanUpExpiration.getExpirationPeriod();
                mDefaultExpirationDate = mUICleanUpExpiration.getExpirationDate();
                mUICleanUpExpiration.setExpirationType(UICleanUpExpiration.ExpirationType.ALL);
            } else {
                mUICleanUpExpiration.setExpirationType(mDefaultExpirationType);
                mUICleanUpExpiration.setExpirationPeriod(mDefaultExpirationPeriod);
                mUICleanUpExpiration.setExpirationDate(mDefaultExpirationDate);
            }

            mCleanUpAdapter.setCleanUpExpiration(mUICleanUpExpiration);
            mCleanupService.setDateFilter(mUICleanUpExpiration.getClearDate());
        };

        mCleanUpAdapter = new CleanUpAdapter(this, mContents, mStorages, mUICleanUpExpiration, onCleanupClickListener, cleanupExpirationSwitchObserver);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.cleanup_activity_list_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mCleanUpAdapter);
        recyclerView.setItemAnimator(null);
        recyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mUIInitialized = true;
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }

    private void updateContent(ExportStats stats) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateContent: " + stats);
        }

        UIExport mediaContent = mContents.get(0);
        UIExport allContent = mContents.get(1);

        long mediaAndFileCount = stats.imageCount + stats.videoCount + stats.audioCount + stats.fileCount;
        long mediaAndFileSize = stats.imageSize + stats.videoSize + stats.audioSize + stats.fileSize;
        mediaContent.setCount(mediaAndFileCount);
        mediaContent.setSize(mediaAndFileSize);

        long allContentCount = stats.msgCount + stats.imageCount + stats.videoCount + stats.audioCount + stats.fileCount;
        allContent.setCount(allContentCount);
        allContent.setSize(0);

        if (!mInitContentToClean) {
            mInitContentToClean = true;
            UIStorage storageConversation = null;
            for (UIStorage storage : mStorages) {
                if (storage.getStorageType() == UIStorage.StorageType.CONVERSATION) {
                    storageConversation = storage;
                    break;
                }
            }

            if (storageConversation != null) {
                storageConversation.setSize(mediaContent.getSize());
                storageConversation.setName(mConversationName);
            }

            mCleanupService.setDateFilter(mUICleanUpExpiration.getClearDate());
        }

        mCleanUpAdapter.updateData(mContents, mStorages);
    }

    private void initExport() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initExport");
        }

        mContents.clear();
        mContents.add(new UIExport(UIExport.ExportContentType.MEDIA_AND_FILE, R.drawable.toolbar_picture_grey, false));
        mContents.add(new UIExport(UIExport.ExportContentType.ALL, R.drawable.tab_bar_chat_grey, false));
    }

    private void initStorage() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initStorage");
        }

        long totalSize = Utils.getDiskTotalSpace(Environment.getDataDirectory());
        long freeSize = Utils.getDiskSpace(Environment.getDataDirectory());
        long appSize = Utils.getDiskUsedSpace(new File(getApplication().getFilesDir(), "conversations"));

        mStorages.clear();
        mStorages.add(new UIStorage(UIStorage.StorageType.USED, totalSize - freeSize));
        mStorages.add(new UIStorage(UIStorage.StorageType.FREE, freeSize));
        mStorages.add(new UIStorage(UIStorage.StorageType.APP, appSize));
        mStorages.add(new UIStorage(UIStorage.StorageType.CONVERSATION, 0));
        mStorages.add(new UIStorage(UIStorage.StorageType.TOTAL, totalSize));
    }

    private void openMenuCleanupExpiration() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuCleanupExpiration");
        }

        ViewGroup viewGroup = findViewById(R.id.cleanup_activity_layout);

        MenuCleanUpExpirationView menuCleanUpExpirationView = new MenuCleanUpExpirationView(this, null);

        MenuCleanUpExpirationView.OnCleanUpExpirationListener onCleanUpExpirationListener = new MenuCleanUpExpirationView.OnCleanUpExpirationListener() {
            @Override
            public void onCloseMenuExpiration() {

                viewGroup.removeView(menuCleanUpExpirationView);
                setStatusBarColor();
            }

            @Override
            public void onSelectExpiration(UICleanUpExpiration cleanUpExpiration) {

                mUICleanUpExpiration = cleanUpExpiration;
                mCleanUpAdapter.setCleanUpExpiration(mUICleanUpExpiration);
                mCleanupService.setDateFilter(mUICleanUpExpiration.getClearDate());
            }
        };

        menuCleanUpExpirationView.setOnCleanUpExpirationListener(onCleanUpExpirationListener);
        menuCleanUpExpirationView.setLocalCleanUpActivity(this);
        viewGroup.addView(menuCleanUpExpirationView);
        menuCleanUpExpirationView.openMenu(mUICleanUpExpiration);

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    public boolean canCleanup() {
        if (DEBUG) {
            Log.d(LOG_TAG, "canCleanup");
        }

        for (UIExport uiContent : mContents) {
            if (uiContent.isChecked() && uiContent.getCount() > 0) {
                return true;
            }
        }

        return false;
    }

    private void showConfirmView(@Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showConfirmView");
        }

        ViewGroup viewGroup = findViewById(R.id.cleanup_activity_layout);
        if (mSpace != null) {

            DeleteSpaceConfirmView deleteSpaceConfirmView = new DeleteSpaceConfirmView(this, null);
            deleteSpaceConfirmView.setSpaceName(mSpace.getSpaceSettings().getName(), mSpace.getSpaceSettings().getStyle());
            deleteSpaceConfirmView.setAvatar(avatar, false);
            deleteSpaceConfirmView.setMessage(getString(R.string.cleanup_activity_delete_confirmation_message));

            AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
                @Override
                public void onConfirmClick() {
                    clearConversation();
                    deleteSpaceConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCancelClick() {
                    deleteSpaceConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onDismissClick() {
                    deleteSpaceConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    viewGroup.removeView(deleteSpaceConfirmView);
                    setStatusBarColor();
                }
            };
            deleteSpaceConfirmView.setObserver(observer);

            viewGroup.addView(deleteSpaceConfirmView);
            deleteSpaceConfirmView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        } else {
            DeleteConfirmView deleteConfirmView = new DeleteConfirmView(this, null);
            if (mContact == null && mGroup == null) {
                deleteConfirmView.hideAvatar();
            } else {
                deleteConfirmView.setAvatar(avatar, avatar == null || avatar.equals(getTwinmeApplication().getDefaultGroupAvatar()));
            }

            deleteConfirmView.setMessage(getString(R.string.cleanup_activity_delete_confirmation_message));

            AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
                @Override
                public void onConfirmClick() {
                    clearConversation();
                    deleteConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCancelClick() {
                    deleteConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onDismissClick() {
                    deleteConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    viewGroup.removeView(deleteConfirmView);
                    setStatusBarColor();
                }
            };
            deleteConfirmView.setObserver(observer);

            viewGroup.addView(deleteConfirmView);
            deleteConfirmView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    private void clearConversation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "clearConversation");
        }

        UIExport allContent = mContents.get(1);

        if (allContent.isChecked()) {
            if (mLocalCleanUpOnly) {
                mCleanupService.startCleanUp(mUICleanUpExpiration.getClearDate(), ConversationService.ClearMode.CLEAR_LOCAL);
            } else {
                mCleanupService.startCleanUp(mUICleanUpExpiration.getClearDate(), ConversationService.ClearMode.CLEAR_BOTH);
            }
        } else {
            if (mLocalCleanUpOnly) {
                mCleanupService.startCleanUp(mUICleanUpExpiration.getClearDate(), ConversationService.ClearMode.CLEAR_MEDIA);
            } else {
                mCleanupService.startCleanUp(mUICleanUpExpiration.getClearDate(), ConversationService.ClearMode.CLEAR_BOTH_MEDIA);
            }
        }
    }
}
