/*
 *  Copyright (c) 2017-2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.DescriptorId;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public abstract class Item implements Comparable<Item> {

    static final DescriptorId DEFAULT_DESCRIPTOR_ID;

    public static final int TOP_LEFT = 1;
    public static final int TOP_RIGHT = 1 << 1;
    public static final int BOTTOM_RIGHT = 1 << 2;
    public static final int BOTTOM_LEFT = 1 << 3;
    public static final int TOP_LARGE_MARGIN = 1 << 4;
    public static final int BOTTOM_LARGE_MARGIN = 1 << 5;

    static {
        DEFAULT_DESCRIPTOR_ID = new DescriptorId(0, new UUID(0, 0), BaseItemActivity.DEFAULT_SEQUENCE_ID);
    }

    public enum ItemType {
        HEADER,
        FOOTER,
        INFO_DATE,
        INFO_COPY,
        INFO_FILE,
        TIME,
        TYPING,
        MESSAGE,
        PEER_MESSAGE,
        LINK,
        PEER_LINK,
        IMAGE,
        PEER_IMAGE,
        AUDIO,
        PEER_AUDIO,
        VIDEO,
        PEER_VIDEO,
        FILE,
        PEER_FILE,
        NAME,
        INVITATION,
        PEER_INVITATION,
        CALL,
        PEER_CALL,
        INVITATION_CONTACT,
        PEER_INVITATION_CONTACT,
        CLEAR,
        PEER_CLEAR
    }

    public enum ItemState {
        DEFAULT,
        SENDING,
        RECEIVED,
        READ,
        NOT_SENT,
        // Item is deleted locally.
        DELETED,
        // Peer has deleted the message.
        PEER_DELETED,
        // Item is deleted locally and by peer.
        BOTH_DELETED
    }

    public enum ItemMode {
        NORMAL,
        PREVIEW,
        SMALL_PREVIEW
    }

    private static int sItemId = 0;

    private final long mItemId;
    private final ItemType mType;
    private final DescriptorId mDescriptorId;
    private long mCreatedTimestamp;
    private long mUpdatedTimestamp;
    private long mSentTimestamp;
    private long mReceivedTimestamp;
    private long mReadTimestamp;
    private long mDeletedTimestamp;
    private long mPeerDeletedTimestamp;
    private long mExpireTimeout;
    private int mCorners;
    private ItemState mState;
    private boolean mVisibleAvatar;
    private boolean mCopyAllowed;
    private float mDeleteProgress;
    private Timer mDeleteTimer;
    private boolean mCanReply;
    private boolean mForwarded;
    private boolean mSelected;
    private ItemMode mMode;

    private List<ConversationService.DescriptorAnnotation> mLikeDescriptorAnnotations;

    @Nullable
    private final Descriptor mReplyToDescriptor;

    Item(@NonNull ItemType type, @NonNull Descriptor descriptor, @Nullable Descriptor replyToDescriptor) {

        mItemId = sItemId++;
        mDeleteProgress = 0;
        mType = type;
        mDescriptorId = descriptor.getDescriptorId();
        mReplyToDescriptor = replyToDescriptor;
        mMode = ItemMode.NORMAL;

        mCreatedTimestamp = descriptor.getCreatedTimestamp();
        mUpdatedTimestamp = descriptor.getUpdatedTimestamp();
        mSentTimestamp = descriptor.getSentTimestamp();
        mReceivedTimestamp = descriptor.getReceivedTimestamp();
        mReadTimestamp = descriptor.getReadTimestamp();
        mDeletedTimestamp = descriptor.getDeletedTimestamp();
        mPeerDeletedTimestamp = descriptor.getPeerDeletedTimestamp();
        mExpireTimeout = descriptor.getExpireTimeout();

        mCorners = TOP_LEFT | TOP_RIGHT | BOTTOM_LEFT | BOTTOM_RIGHT | TOP_LARGE_MARGIN | BOTTOM_LARGE_MARGIN;

        mState = ItemState.DEFAULT;
        if (mPeerDeletedTimestamp != 0 && mDeletedTimestamp != 0) {
            mState = ItemState.BOTH_DELETED;
        } else if (mPeerDeletedTimestamp != 0) {
            mState = ItemState.PEER_DELETED;
        } else if (mDeletedTimestamp != 0) {
            mState = ItemState.DELETED;
        } else if (mReceivedTimestamp == -1) {
            mState = ItemState.NOT_SENT;
        } else if (mReadTimestamp != 0) {
            if (mReadTimestamp != -1) {
                mState = ItemState.READ;
            }
        } else if (mReceivedTimestamp != 0) {
            mState = ItemState.RECEIVED;
        } else {
            mState = ItemState.SENDING;
        }

        mVisibleAvatar = false;
        mCanReply = false;
        mSelected = false;
        mForwarded = descriptor.getAnnotation(ConversationService.AnnotationType.FORWARDED) != null;
        mLikeDescriptorAnnotations = descriptor.getAnnotations(ConversationService.AnnotationType.LIKE);
    }

    Item(ItemType type, DescriptorId descriptorId, long timestamp) {

        mItemId = sItemId++;
        mType = type;
        mDescriptorId = descriptorId;

        mDeleteProgress = 0;
        mCreatedTimestamp = timestamp;
        mUpdatedTimestamp = 0;
        mSentTimestamp = 0;
        mReceivedTimestamp = 0;
        mReadTimestamp = 0;
        mDeletedTimestamp = 0;
        mPeerDeletedTimestamp = 0;

        mCorners = TOP_LEFT | TOP_RIGHT | BOTTOM_LEFT | BOTTOM_RIGHT | TOP_LARGE_MARGIN | BOTTOM_LARGE_MARGIN;

        mState = ItemState.DEFAULT;
        mMode = ItemMode.NORMAL;
        mVisibleAvatar = false;
        mCopyAllowed = false;
        mCanReply = false;
        mForwarded = false;
        mSelected = false;
        mReplyToDescriptor = null;
    }

    public abstract boolean isPeerItem();

    public abstract long getTimestamp();

    public long getItemId() {

        return mItemId;
    }

    public ItemType getType() {

        return mType;
    }

    public DescriptorId getDescriptorId() {

        return mDescriptorId;
    }

    public long getCreatedTimestamp() {

        return mCreatedTimestamp;
    }

    public long getUpdatedTimestamp() {

        return mUpdatedTimestamp;
    }

    public long getReadTimestamp() {

        return mReadTimestamp;
    }

    long getReceivedTimestamp() {

        return mReceivedTimestamp;
    }

    public long getSentTimestamp() {

        return mSentTimestamp;
    }

    public long getPeerDeletedTimestamp() {

        return mPeerDeletedTimestamp;
    }

    public int getCorners() {

        return mCorners;
    }

    public void cornersBitwiseOr(int mask) {

        mCorners |= mask;
    }

    public void cornersBitwiseAnd(int mask) {

        mCorners &= mask;
    }

    public boolean getVisibleAvatar() {

        return mVisibleAvatar;
    }

    public void setVisibleAvatar(boolean visibleAvatar) {

        mVisibleAvatar = visibleAvatar;
    }

    public ItemState getState() {

        return mState;
    }

    public ItemMode getMode() {

        return mMode;
    }

    public void setMode(ItemMode mode) {

        mMode = mode;
    }

    public void resetState() {

        if (mState != ItemState.DELETED && mState != ItemState.PEER_DELETED && mState != ItemState.BOTH_DELETED) {
            mState = ItemState.DEFAULT;
        }
    }

    public UUID getPeerTwincodeOutboundId() {

        return null;
    }

    public boolean isSamePeer(Item item) {

        return item.getPeerTwincodeOutboundId() == null;
    }

    public boolean isSameObject(@Nullable Descriptor descriptor) {

        return false;
    }

    public boolean getCopyAllowed() {

        return mCopyAllowed;
    }

    public  void setCopyAllowed(boolean copyAllowed) {

        mCopyAllowed = copyAllowed;
    }

    public boolean canReply() {

        return mCanReply;
    }

    void setCanReply(boolean canReply) {

        mCanReply = canReply;
    }

    public boolean isForwarded() {

        return mForwarded;
    }

    public boolean isEdited() {

        return false;
    }

    public boolean needsUpdateReadTimestamp() {

        return mReadTimestamp == 0 || mReadTimestamp < mUpdatedTimestamp;
    }

    void setForwarded(boolean forwarded) {

        mForwarded = forwarded;
    }

    public boolean isSelected() {

        return mSelected;
    }

    public void setSelected(boolean selected) {

        mSelected = selected;
    }

    public List<ConversationService.DescriptorAnnotation> getLikeDescriptorAnnotations() {

        return mLikeDescriptorAnnotations;
    }

    void setLikeDescriptorAnnotations(List<ConversationService.DescriptorAnnotation> descriptorAnnotations) {

        mLikeDescriptorAnnotations = descriptorAnnotations;
    }

    public String getPath() {

        return null;
    }

    String getInformation(Context context) {

        return "";
    }

    public float getDeleteProgress() {

        return mDeleteProgress;
    }

    public boolean isEphemeralItem() {

        return mExpireTimeout > 0;
    }

    public boolean isAvailableItem() {

        return true;
    }

    public boolean isClearLocalItem() {

        return false;
    }

    public long getExpireTimeout() {

        return mExpireTimeout;
    }

    @Nullable
    public Descriptor getReplyToDescriptor() {

        return mReplyToDescriptor;
    }

    @Nullable
    public DescriptorId getReplyToDescriptorId() {

        if (mReplyToDescriptor == null) {

            return null;
        }

        return mReplyToDescriptor.getDescriptorId();
    }

    public boolean hasLikeAnnotation(int value) {

        for (ConversationService.DescriptorAnnotation descriptorAnnotation : mLikeDescriptorAnnotations) {
            if (descriptorAnnotation.getValue() == value) {
                return true;
            }
        }
        return false;
    }

    public void updateAnnotations(Descriptor descriptor) {

        mForwarded = descriptor.getAnnotation(ConversationService.AnnotationType.FORWARDED) != null;
        mLikeDescriptorAnnotations = descriptor.getAnnotations(ConversationService.AnnotationType.LIKE);
    }

    public void updateTimestamps(Descriptor descriptor) {

        mCreatedTimestamp = descriptor.getCreatedTimestamp();
        mUpdatedTimestamp = descriptor.getUpdatedTimestamp();
        mSentTimestamp = descriptor.getSentTimestamp();
        mExpireTimeout = descriptor.getExpireTimeout();
        // Temporary fix - should be handled in the library
        if (mReceivedTimestamp <= 0) {
            mReceivedTimestamp = descriptor.getReceivedTimestamp();
        }
        if (mReadTimestamp <= 0) {
            mReadTimestamp = descriptor.getReadTimestamp();
        }
        mReceivedTimestamp = descriptor.getReceivedTimestamp();
        mReadTimestamp = descriptor.getReadTimestamp();
        mDeletedTimestamp = descriptor.getDeletedTimestamp();
        mPeerDeletedTimestamp = descriptor.getPeerDeletedTimestamp();

        updateState();
    }

    public void updateState() {

        if (mPeerDeletedTimestamp != 0 && mDeletedTimestamp != 0) {
            mState = ItemState.BOTH_DELETED;
        } else if (mPeerDeletedTimestamp != 0) {
            mState = ItemState.PEER_DELETED;
        } else if (mDeletedTimestamp != 0) {
            mState = ItemState.DELETED;
        } else if (mReceivedTimestamp == -1) {
            mState = ItemState.NOT_SENT;
        } else if (mReadTimestamp != 0) {
            if (mReadTimestamp != -1) {
                mState = ItemState.READ;
            } else {
                mState = ItemState.DEFAULT;
            }
        } else if (mReceivedTimestamp != 0) {
            mState = ItemState.RECEIVED;
        } else {
            mState = ItemState.SENDING;
        }
    }

    public void setState(ItemState state) {

        mState = state;
    }

    void startDeleteItem() {

        if (mDeleteTimer == null) {
            mDeleteTimer = new Timer();
            mDeleteTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (mDeleteProgress >= 100) {
                        mDeleteProgress = 100f;
                        mDeleteTimer.cancel();
                        mDeleteTimer = null;
                    }

                    mDeleteProgress = mDeleteProgress + 0.5f;
                }
            }, 0, 25);
        }
    }

    void appendTo(StringBuilder stringBuilder) {

        stringBuilder.append(" itemId=");
        stringBuilder.append(mItemId);
        stringBuilder.append("\n");
        stringBuilder.append(" type=");
        stringBuilder.append(mType);
        stringBuilder.append("\n");
        stringBuilder.append(" descriptorId=");
        stringBuilder.append(mDescriptorId);
        stringBuilder.append("\n");
        stringBuilder.append(" createdTimestamp=");
        stringBuilder.append(mCreatedTimestamp);
        stringBuilder.append("\n");
        stringBuilder.append(" updatedTimestamp=");
        stringBuilder.append(mUpdatedTimestamp);
        stringBuilder.append("\n");
        stringBuilder.append(" sentTimestamp=");
        stringBuilder.append(mSentTimestamp);
        stringBuilder.append("\n");
        stringBuilder.append(" receivedTimestamp=");
        stringBuilder.append(mReceivedTimestamp);
        stringBuilder.append("\n");
        stringBuilder.append(" readTimestamp=");
        stringBuilder.append(mReadTimestamp);
        stringBuilder.append("\n");
        stringBuilder.append(" deletedTimestamp=");
        stringBuilder.append(mDeletedTimestamp);
        stringBuilder.append("\n");
        stringBuilder.append(" peerDeletedTimestamp=");
        stringBuilder.append(mPeerDeletedTimestamp);
        stringBuilder.append("\n");
        stringBuilder.append(" corners=");
        if ((mCorners & TOP_LEFT) != 0) {
            stringBuilder.append("|TOP_LEFT");
        }
        if ((mCorners & TOP_RIGHT) != 0) {
            stringBuilder.append("|TOP_RIGHT");
        }
        if ((mCorners & BOTTOM_RIGHT) != 0) {
            stringBuilder.append("|BOTTOM_RIGHT");
        }
        if ((mCorners & BOTTOM_LEFT) != 0) {
            stringBuilder.append("|BOTTOM_LEFT");
        }
        if ((mCorners & TOP_LARGE_MARGIN) != 0) {
            stringBuilder.append("|TOP_LARGE_MARGIN");
        }
        if ((mCorners & BOTTOM_LARGE_MARGIN) != 0) {
            stringBuilder.append("|BOTTOM_LARGE_MARGIN");
        }
        stringBuilder.append("\n");
        stringBuilder.append(" state=");
        stringBuilder.append(mState);
        stringBuilder.append("\n");
    }

    //
    // Override Object methods
    //

    @Override
    @NonNull
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Item:\n");
        appendTo(stringBuilder);

        return stringBuilder.toString();
    }

    @Override
    public int compareTo(Item second) {

        int result = Long.compare(getTimestamp(), second.getTimestamp());
        if (result != 0) {
            return result;
        }

        // Peer item first.
        if (isPeerItem() && !second.isPeerItem()) {
            return -1;
        }
        if (!isPeerItem() && second.isPeerItem()) {
            return 1;
        }
        return Long.compare(mDescriptorId.sequenceId, second.mDescriptorId.sequenceId);
    }
}
