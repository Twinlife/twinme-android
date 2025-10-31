/*
 *  Copyright (c) 2023-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.services.ConversationFilesService;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.baseItemActivity.ImageItem;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.ui.baseItemActivity.PeerImageItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerVideoItem;
import org.twinlife.twinme.ui.baseItemActivity.VideoItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AbstractFilesActivity extends AbstractTwinmeActivity implements ConversationFilesService.Observer  {
    private static final String LOG_TAG = "AbstractFilesActivity";
    private static final boolean DEBUG = false;

    protected static final int RESULT_DID_SHARE_ACTION = 100;

    @Nullable
    protected ConversationFilesService mConversationFilesService;

    protected final List<Item> mItems = new ArrayList<>();

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: " + contact);
        }

    }

    @Override
    public void onGetContactNotFound() {

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

    }

    @Override
    public void onGetGroupNotFound() {

    }

    @Override
    public void onGetDescriptors(@NonNull List<ConversationService.Descriptor> descriptors) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDescriptors: " + descriptors);
        }
    }

    @Override
    public void onMarkDescriptorDeleted(@NonNull ConversationService.Descriptor descriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMarkDescriptorDeleted: " + descriptor);
        }

    }

    @Override
    public void onDeleteDescriptors(@NonNull Set<ConversationService.DescriptorId> descriptorList) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteDescriptors: " + descriptorList);
        }

    }

    protected void addImageDescriptor(ConversationService.ImageDescriptor imageDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addImageDescriptor: imageDescriptor=" + imageDescriptor);
        }

        if (mConversationFilesService.isLocalDescriptor(imageDescriptor)) {
            ImageItem imageItem = new ImageItem(imageDescriptor, null);
            mItems.add(imageItem);
        } else if (mConversationFilesService.isPeerDescriptor(imageDescriptor)) {
            PeerImageItem peerImageItem = new PeerImageItem(imageDescriptor, null);
            mItems.add(peerImageItem);
        }
    }

    protected void addVideoDescriptor(ConversationService.VideoDescriptor videoDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addVideoDescriptor: videoDescriptor=" + videoDescriptor);
        }

        if (mConversationFilesService.isLocalDescriptor(videoDescriptor)) {
            VideoItem videoItem = new VideoItem(videoDescriptor, null);
            mItems.add(videoItem);
        } else if (mConversationFilesService.isPeerDescriptor(videoDescriptor)) {
            PeerVideoItem peerVideoItem = new PeerVideoItem(videoDescriptor, null);
            mItems.add(peerVideoItem);
        }
    }

    protected boolean isShareItem(Item item) {
        if (DEBUG) {
            Log.d(LOG_TAG, "isShareItem" + item);
        }

        if (item.isClearLocalItem()) {
            return false;
        } else if (item.getState() == Item.ItemState.DELETED || item.isPeerItem() && (!item.getCopyAllowed() || item.isEphemeralItem())) {
            return false;
        } else {
            return item.isAvailableItem();
        }
    }

    protected Uri uriFromPath(String path) {
        if (DEBUG) {
            Log.d(LOG_TAG, "uriFromPath: " + path);
        }

        File file = new File(getTwinmeContext().getFilesDir(), path);
        return FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
    }
}
