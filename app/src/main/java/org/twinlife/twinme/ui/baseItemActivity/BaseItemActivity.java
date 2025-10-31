/*
 *  Copyright (c) 2019-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.DescriptorId;
import org.twinlife.twinlife.ConversationService.FileDescriptor;
import org.twinlife.twinlife.ConversationService.UpdateType;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.actions.GetTwincodeAction;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.TextStyle;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.utils.async.Loader;

import java.util.List;
import java.util.UUID;

public abstract class BaseItemActivity extends AbstractTwinmeActivity {

    public static final long DEFAULT_SEQUENCE_ID = -1;

    private static final float DESIGN_HEADER_HEIGHT = 139f;
    private static final float DESIGN_FOOTER_HEIGHT = 120f;
    private static final float DESIGN_AVATAR_HEIGHT = 52f;

    public static int HEADER_HEIGHT;
    public static int FOOTER_HEIGHT;
    public static int AVATAR_HEIGHT;

    public interface AudioItemObserver {

        void onStartPlaying(@NonNull AudioItemViewHolder audioItemViewHolder);

        void onStartPlaying(@NonNull PeerAudioItemViewHolder peerAudioItemViewHolder);
    }

    public interface InvitationItemObserver {

        void onUpdateDescriptor(@NonNull Descriptor descriptor, UpdateType updateType);
    }

    public abstract
    void getContactAvatar(@Nullable UUID peerTwincodeOutboundId, TwinmeContext.Consumer<Bitmap> avatarConsumer);

    public abstract @Nullable
    Contact getContact();

    public abstract @Nullable
    Group getGroup();

    public abstract boolean isGroupConversation();

    public abstract void markDescriptorRead(@NonNull DescriptorId descriptorId);

    public abstract void updateDescriptor(boolean allowCopy);

    public abstract boolean isPeerTyping();

    public abstract boolean isSelectItemMode();

    public abstract @Nullable
    List<Originator> getTypingOriginators();

    public abstract @Nullable
    List<Bitmap> getTypingOriginatorsImages();

    public abstract void deleteItem(@NonNull DescriptorId descriptorId);

    public abstract boolean isSelectedItem(@NonNull DescriptorId descriptorId);

    public abstract void onItemLongPress(@NonNull Item item);

    public abstract void onReplyClick(@NonNull DescriptorId descriptorId);

    public abstract boolean isMenuOpen();

    public abstract boolean isReplyViewOpen();

    public abstract void closeMenu();

    public abstract void onItemClick(Item item);

    public abstract void onAnnotationClick(@Nullable DescriptorId descriptorId);

    public abstract void audioCall();

    public abstract void videoCall();

    public abstract void onMediaClick(@NonNull DescriptorId descriptorId);

    public abstract @NonNull
    TextStyle getMessageFont();

    @Nullable
    public abstract Bitmap getThumbnail(@NonNull FileDescriptor descriptor);

    /**
     * Add an item to be loaded by the background executor.
     *
     * @param loader the loader that load some item data.
     */
    public abstract void addLoadableItem(@NonNull final Loader<Item> loader);

    public void getTwincodeOutbound(@NonNull UUID twincodeOutboundId, @NonNull GetTwincodeAction.Consumer observer) {

        GetTwincodeAction action = new GetTwincodeAction(getTwinmeContext(), twincodeOutboundId);
        action.onResult(observer).start();
    }

    @Override
    public void setupDesign() {

        HEADER_HEIGHT = (int) (DESIGN_HEADER_HEIGHT * Design.HEIGHT_RATIO);
        FOOTER_HEIGHT = (int) (DESIGN_FOOTER_HEIGHT * Design.HEIGHT_RATIO);
        AVATAR_HEIGHT = (int) (DESIGN_AVATAR_HEIGHT * Design.HEIGHT_RATIO);
    }
}
