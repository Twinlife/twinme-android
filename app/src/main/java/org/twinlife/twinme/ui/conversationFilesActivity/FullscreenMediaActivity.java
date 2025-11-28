/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.services.ConversationFilesService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.TwinmeActivity;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.ui.contacts.DeleteConfirmView;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.FileInfo;
import org.twinlife.twinme.utils.SaveAsyncTask;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FullscreenMediaActivity extends AbstractFilesActivity {
    private static final String LOG_TAG = "FullscreenMediaActivity";
    private static final boolean DEBUG = false;

    private class MediaLinearLayoutManager extends LinearLayoutManager {

        public MediaLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public boolean canScrollHorizontally() {

            return mCanScroll;
        }
    }

    private static final float DESIGN_HEADER_HEIGHT = 100f;
    private static final float DESIGN_FOOTER_HEIGHT = 130f;
    private static final float DESIGN_ACTION_WIDTH = 120f;

    private FullScreenMediaRecyclerView mMediaListView;
    private FullscreenMediaAdapter mFullscreenMediaAdapter;
    private int mCurrentPosition = 0;
    private int mStartIndex = 0;

    private LinearLayoutManager mLinearLayoutManager;

    private View mHeaderView;
    private View mFooterView;
    private View mSaveView;
    private View mShareView;

    private Bitmap mAvatar;

    private boolean mCanScroll = true;

    private boolean mDeferredSaveMedia = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        initViews();

        Intent intent = getIntent();
        mStartIndex = intent.getIntExtra(Intents.INTENT_ITEM_INDEX, 0);
        UUID contactId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));
        UUID groupId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_GROUP_ID));
        String descriptorIds = intent.getStringExtra(Intents.INTENT_DESCRIPTOR_ID);
        if (descriptorIds == null || (contactId == null && groupId == null)) {
            finish();
            return;
        }

        mConversationFilesService = new ConversationFilesService(this, getTwinmeContext(), this,
                descriptorIds, contactId, groupId);
    }
    
    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        mFullscreenMediaAdapter.pausePlayer();

        runOnUiThread(() -> {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        });

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        if (mConversationFilesService != null) {
            mConversationFilesService.dispose();
        }

        // trigger FullscreenMediaAdapter.onDetachedFromRecyclerView()
        mMediaListView.setAdapter(null);

        super.onDestroy();
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onWindowFocusChanged: hasFocus=" + hasFocus);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConfigurationChanged: newConfig=" + newConfig);
        }

        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        boolean storageWriteAccessGranted = false;
        for (Permission grantedPermission : grantedPermissions) {
            if (grantedPermission == Permission.WRITE_EXTERNAL_STORAGE) {
                storageWriteAccessGranted = true;
                break;
            }
        }

        if (mDeferredSaveMedia) {
            mDeferredSaveMedia = false;

            if (storageWriteAccessGranted) {
                saveMediaInGallery();
            }
        }
    }

    public int getCurrentPosition() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getCurrentPosition");
        }

        return mCurrentPosition;
    }

    public void onMediaClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMediaClick");
        }

        if (mHeaderView.getVisibility() == View.VISIBLE) {
            mHeaderView.setVisibility(View.INVISIBLE);
            mFooterView.setVisibility(View.INVISIBLE);
        } else {
            mHeaderView.setVisibility(View.VISIBLE);
            mFooterView.setVisibility(View.VISIBLE);
        }
    }

    public void onImageScaleStateChanged(boolean isScale) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onImageScaleStateChanged");
        }

        mCanScroll = !isScale;
        mMediaListView.setNestedScrollingEnabled(mCanScroll);
    }

    public void onVideoSeekBarUpdate(boolean touch) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onVideoSeekBarUpdate");
        }

        mCanScroll = !touch;
    }

    //
    // Override implements methods
    //

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: " + contact);
        }

        mAvatar = avatar;
    }

    @Override
    public void onGetGroup(@NonNull Group group, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup: " + group);
        }

        mAvatar = avatar;
    }

    @Override
    public void onMarkDescriptorDeleted(@NonNull ConversationService.Descriptor descriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMarkDescriptorDeleted: " + descriptor);
        }

        for (Item item : mItems) {
            if (item.getDescriptorId().equals(descriptor.getDescriptorId())) {
                mItems.remove(item);
                break;
            }
        }

        if (mItems.isEmpty()) {
            finish();
        } else {
            mFullscreenMediaAdapter.setItems(mItems);
        }
    }

    @Override
    public void onDeleteDescriptors(@NonNull Set<ConversationService.DescriptorId> descriptorList) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteDescriptors: " + descriptorList);
        }

        Iterator<Item> iterator = mItems.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            ConversationService.DescriptorId descriptorId = item.getDescriptorId();
            if (descriptorList.remove(descriptorId)) {
                iterator.remove();
                if (descriptorList.isEmpty()) {
                    break;
                }
            }
        }

        if (mItems.isEmpty()) {
            finish();
        } else {
            mFullscreenMediaAdapter.setItems(mItems);
        }
    }

    @Override
    public void onGetDescriptors(@NonNull List<ConversationService.Descriptor> descriptors) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDescriptors: " + descriptors);
        }

        for (ConversationService.Descriptor descriptor  : descriptors) {
            switch (descriptor.getType()) {
                case IMAGE_DESCRIPTOR:
                    ConversationService.ImageDescriptor imageDescriptor = (ConversationService.ImageDescriptor) descriptor;
                    addImageDescriptor(imageDescriptor);
                    break;

                case VIDEO_DESCRIPTOR:
                    ConversationService.VideoDescriptor videoDescriptor = (ConversationService.VideoDescriptor) descriptor;
                    addVideoDescriptor(videoDescriptor);
                    break;

                default:
                    break;
            }

        }

        if (!mItems.isEmpty()) {
            mFullscreenMediaAdapter.setItems(mItems);

            if (mStartIndex != 0 && mItems.size() > mStartIndex) {
                updateFooterView(mStartIndex);
                mLinearLayoutManager.scrollToPosition(mStartIndex);
                mCurrentPosition = mStartIndex;
                mStartIndex = 0;
            } else if (mCurrentPosition < mItems.size()){
                updateFooterView(mCurrentPosition);
            }
        } else {
            finish();
        }
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.fullscreen_media_activity);

        setStatusBarColor(Color.BLACK);

        Window window = getWindow();
        window.setNavigationBarColor(Color.BLACK);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        applyInsets(R.id.fullscreen_media_activity_layout, -1, -1, Color.BLACK, false);

        mHeaderView = findViewById(R.id.fullscreen_media_activity_header_view);
        mHeaderView.setBackgroundColor(Color.BLACK);

        ViewGroup.LayoutParams layoutParams = mHeaderView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_HEADER_HEIGHT * Design.HEIGHT_RATIO);

        View closeView = findViewById(R.id.fullscreen_media_activity_header_close_view);
        closeView.setOnClickListener(view -> onCloseClick());

        layoutParams = closeView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ACTION_WIDTH * Design.WIDTH_RATIO);

        mFooterView = findViewById(R.id.fullscreen_media_activity_footer_view);
        mFooterView.setBackgroundColor(Color.BLACK);

        layoutParams = mFooterView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_FOOTER_HEIGHT * Design.HEIGHT_RATIO);

        mSaveView = findViewById(R.id.fullscreen_media_activity_footer_save_view);
        mSaveView.setOnClickListener(view -> onSaveClick());

        layoutParams = mSaveView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ACTION_WIDTH * Design.WIDTH_RATIO);

        mShareView = findViewById(R.id.fullscreen_media_activity_footer_share_view);
        mShareView.setOnClickListener(view -> onShareClick());

        layoutParams = mShareView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ACTION_WIDTH * Design.WIDTH_RATIO);

        View deleteView = findViewById(R.id.fullscreen_media_activity_footer_delete_view);
        deleteView.setOnClickListener(view -> onDeleteClick());

        layoutParams = deleteView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ACTION_WIDTH * Design.WIDTH_RATIO);

        mFullscreenMediaAdapter = new FullscreenMediaAdapter(this, mItems);
        mLinearLayoutManager = new MediaLinearLayoutManager(this, RecyclerView.HORIZONTAL, false);

        mMediaListView = findViewById(R.id.fullscreen_media_activity_list_view);
        mMediaListView.setLayoutManager(mLinearLayoutManager);
        mMediaListView.setAdapter(mFullscreenMediaAdapter);
        mMediaListView.setItemAnimator(null);
        mMediaListView.setItemAnimator(null);

        SnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(mMediaListView);

        mMediaListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = pagerSnapHelper.findSnapView(mLinearLayoutManager);
                    if (centerView != null) {
                        mCurrentPosition = mLinearLayoutManager.getPosition(centerView);
                        updateFooterView(mCurrentPosition);
                    }
                }
            }
        });
    }

    @Override
    public void finish() {
        if (DEBUG) {
            Log.d(LOG_TAG, "finish");
        }

        mFullscreenMediaAdapter.stopPlayer();

        super.finish();
    }

    private void onCloseClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseClick");
        }

        finish();
    }

    @Nullable
    private Item getCurrentItem() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getCurrentItem");
        }

        if (mCurrentPosition < mItems.size() && mCurrentPosition >= 0) {
            return mItems.get(mCurrentPosition);
        } else {
            return null;
        }
    }

    private void onSaveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveClick");
        }

        final Item currentItem = getCurrentItem();

        if (currentItem != null) {
            if (!isShareItem(currentItem)) {
                Toast.makeText(this, R.string.conversation_activity_menu_item_view_operation_not_allowed, Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentItem.getPath() == null) {
                return;
            }

            TwinmeActivity.Permission[] permissions = new TwinmeActivity.Permission[]{TwinmeActivity.Permission.WRITE_EXTERNAL_STORAGE};
            mDeferredSaveMedia = true;
            if (checkPermissions(permissions)) {
                mDeferredSaveMedia = false;
                saveMediaInGallery();
            }
        }
    }

    private void saveMediaInGallery() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveMediaInGallery");
        }

        final Item currentItem = getCurrentItem();
        if (currentItem != null) {
            File file = new File(getTwinmeContext().getFilesDir(), currentItem.getPath());
            SaveAsyncTask save = new SaveAsyncTask(this, file, uriFromPath(currentItem.getPath()));
            save.execute();
        }
    }

    private void onShareClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onShareClick");
        }

        final Item currentItem = getCurrentItem();

        if (currentItem != null) {
            if (!isShareItem(currentItem)) {
                Toast.makeText(this, R.string.conversation_activity_menu_item_view_operation_not_allowed, Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentItem.getPath() == null) {
                return;
            }

            Intent intent = new Intent(Intent.ACTION_SEND);
            if (!currentItem.getPath().isEmpty()) {
                Uri uri = uriFromPath(currentItem.getPath());
                FileInfo media = new FileInfo(getApplicationContext(), uri);
                intent.setType(media.getMimeType());
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            }

            startActivityForResult(Intent.createChooser(intent, getString(R.string.conversation_activity_menu_item_view_share_title)), RESULT_DID_SHARE_ACTION);
        }
    }

    private void onDeleteClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteClick");
        }

        final Item currentItem = getCurrentItem();

        if (currentItem == null) {
            return;
        }

        mFullscreenMediaAdapter.pausePlayer();

        ViewGroup viewGroup = findViewById(R.id.fullscreen_media_activity_layout);

        DeleteConfirmView deleteConfirmView = new DeleteConfirmView(this, null);
        deleteConfirmView.setAvatar(mAvatar, mAvatar == null || mAvatar.equals(getTwinmeApplication().getDefaultGroupAvatar()));

        if (!isShareItem(currentItem)) {
            deleteConfirmView.setMessage(getString(R.string.application_operation_irreversible));
        } else {
            deleteConfirmView.setMessage(getString(R.string.cleanup_activity_delete_confirmation_message));
        }

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                deleteItem();
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
            }
        };
        deleteConfirmView.setObserver(observer);

        viewGroup.addView(deleteConfirmView);
        deleteConfirmView.show();
    }

    private void deleteItem() {
        if (DEBUG) {
            Log.d(LOG_TAG, "deleteItem");
        }

        final Item currentItem = getCurrentItem();
        if (currentItem != null && mConversationFilesService != null) {
            if (currentItem.isPeerItem()) {
                mConversationFilesService.deleteDescriptor(currentItem.getDescriptorId());
            } else {
                mConversationFilesService.markDescriptorDeleted(currentItem.getDescriptorId());
            }
        }
    }

    private void updateFooterView(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "deleteItem");
        }

        if (position >= 0 && position < mItems.size()) {
            Item currentItem = mItems.get(position);

            if (currentItem != null) {
                if (!isShareItem(currentItem)) {
                    mShareView.setAlpha(0.5f);
                    mSaveView.setAlpha(0.5f);
                } else {
                    mShareView.setAlpha(1.f);
                    mSaveView.setAlpha(1.f);
                }
            }
        }
    }
}
