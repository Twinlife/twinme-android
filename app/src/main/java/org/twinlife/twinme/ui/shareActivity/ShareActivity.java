/*
 *  Copyright (c) 2018-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.shareActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.bumptech.glide.Glide;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.Conversation;
import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.DescriptorId;
import org.twinlife.twinlife.ErrorCode;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.glide.Modes;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.ShareService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.skin.TextStyle;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.baseItemActivity.AudioItem;
import org.twinlife.twinme.ui.baseItemActivity.BaseItemActivity;
import org.twinlife.twinme.ui.baseItemActivity.ImageItem;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.ui.baseItemActivity.MessageItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerAudioItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerImageItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerMessageItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerVideoItem;
import org.twinlife.twinme.ui.baseItemActivity.VideoItem;
import org.twinlife.twinme.ui.conversationActivity.ConversationActivity;
import org.twinlife.twinme.ui.privacyActivity.LockScreenActivity;
import org.twinlife.twinme.ui.spaces.CustomAppearance;
import org.twinlife.twinme.ui.spaces.SpacesActivity;
import org.twinlife.twinme.ui.conversationActivity.PreviewFileActivity;
import org.twinlife.twinme.ui.conversationActivity.UIPreviewFile;
import org.twinlife.twinme.ui.users.OnContactTouchListener;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.ui.users.UIContactListAdapter;
import org.twinlife.twinme.ui.users.UISelectableContact;
import org.twinlife.twinme.utils.FileInfo;
import org.twinlife.twinme.utils.ShareUtils;
import org.twinlife.twinme.utils.async.Loader;
import org.twinlife.twinme.utils.async.LoaderListener;
import org.twinlife.twinme.utils.async.Manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activity controller called to share content (file, text)
 */

public class ShareActivity extends BaseItemActivity implements ShareService.Observer, OnContactTouchListener.OnContactObserver, LoaderListener<Item> {
    private static final String LOG_TAG = "ShareActivity";
    private static final boolean DEBUG = false;

    private static final int CHANGE_SPACE = 1;
    private static final int REQUEST_PREVIEW_FILE = 4;
    private static final int EDIT_TEXT_BORDER_COLOR = Color.rgb(78, 78, 78);
    private static final float DESIGN_SEARCH_CONTENT_MARGIN = 32f;
    private static final float DESIGN_SEARCH_CONTENT_HEIGHT = 58f;
    private static final float DESIGN_SEARCH_GLASS_ICON_SIZE = 22f;
    private static final float DESIGN_SEARCH_GLASS_ICON_MARGIN = 14f;
    private static final float DESIGN_SEARCH_CLEAR_ICON_SIZE = 38f;
    private static final float DESIGN_EDIT_TEXT_WIDTH_INSET = 32f;
    private static final float DESIGN_EDIT_TEXT_HEIGHT_INSET = 20f;
    private static final float DESIGN_EDIT_TEXT_HEIGHT = 90f;
    private static final float DESIGN_EDIT_TEXT_MARGIN = 24f;
    private static final float DESIGN_SEND_VIEW_SIZE = 72f;
    private static final float DESIGN_SEND_ICON_SIZE = 30f;
    private static final float DESIGN_HORIZONTAL_MARGIN = 32f;
    private static final float DESIGN_PREVIEW_SIZE = 100f;

    private static final float DESIGN_ITEM_ROUND_CORNER_RADIUS_DP = 9;

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;
    private View mBottomView;
    private UIContactListAdapter mSelectedUIContactListAdapter;
    private RecyclerView mSelectedUIContactRecyclerView;
    private RecyclerView mUIContactRecyclerView;
    private ImageView mPreviewView;
    private ShareListAdapter mShareListAdapter;
    private EditText mSearchEditText;
    private View mClearSearchView;
    private EditText mEditText;

    private ShapeableImageView mSpaceAvatarView;
    private GradientDrawable mNoAvatarGradientDrawable;
    private View mNoAvatarView;
    private TextView mSpaceNameView;

    private final List<UISelectableContact> mUIContacts = new ArrayList<>();
    private final List<UISelectableContact> mUIGroups = new ArrayList<>();
    private final List<FileInfo> mSharedFiles = new ArrayList<>();
    private final List<UIContact> mSelectedUIContact = new ArrayList<>();

    private CharSequence mMessageFromIntent;
    private String mDeferredMessage;
    private boolean mDeferredAllowCopyText;
    private boolean mDeferredAllowCopyFile;
    private long mDeferredTimeout;
    private boolean mSendFileError = false;

    private ShareService mShareService;

    private Space mSpace;

    @Nullable
    private DescriptorId mForwardDescriptorId;
    @Nullable
    private Descriptor.Type mForwardDescriptorType;
    @Nullable
    private Item mItem;
    private boolean mIsPeerItem;

    private UUID mCurrentConversationId;

    private Manager<Item> mAsyncItemLoader;

    private boolean mShareExternalContent = false;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Intent incomingIntent = getIntent();

        if (incomingIntent.hasExtra(ShortcutManagerCompat.EXTRA_SHORTCUT_ID)) {
            // set up the view and display the progress indicator, as copying large files might take a few seconds.
            setActivityTheme(getTwinmeApplication());
            setContentView(R.layout.share_activity);
            mProgressBarView = findViewById(R.id.share_activity_progress_bar);
            mProgressBarView.setIndeterminate(true);
            showProgressIndicator();
            getTwinmeContext().execute(() -> {
                // We have a direct share target, open the conversation
                // which will in turn open the preview activity.
                Intent intent = new Intent(this, ConversationActivity.class);
                intent.setAction(incomingIntent.getAction());
                intent.putExtra(ShortcutManagerCompat.EXTRA_SHORTCUT_ID, incomingIntent.getStringExtra(ShortcutManagerCompat.EXTRA_SHORTCUT_ID));
                intent.putExtra(Intent.EXTRA_TEXT, ShareUtils.getSharedText(incomingIntent));
                intent.putParcelableArrayListExtra(Intents.INTENT_DIRECT_SHARE_FILES, importFiles(incomingIntent));
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                runOnUiThread(() -> {
                    hideProgressIndicator();
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                });
            });
            return;
        }

        mForwardDescriptorId = DescriptorId.fromString(incomingIntent.getStringExtra(Intents.INTENT_DESCRIPTOR_ID));
        // The activity is exported: the extra can come from another application.
        final Serializable descriptorType = incomingIntent.getSerializableExtra(Intents.INTENT_DESCRIPTOR_TYPE);
        mForwardDescriptorType = descriptorType instanceof Descriptor.Type ? (Descriptor.Type) descriptorType : null;
        mIsPeerItem = incomingIntent.getBooleanExtra(Intents.INTENT_IS_PEER_ITEM, false);
        mMessageFromIntent = ShareUtils.getSharedText(incomingIntent);
        mDeferredAllowCopyFile = getTwinmeApplication().fileCopyAllowed();
        mDeferredAllowCopyText = getTwinmeApplication().messageCopyAllowed();
        mDeferredTimeout = 0;

        initViews();
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && mSearchEditText != null) {
            inputMethodManager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (mShareExternalContent && getTwinmeApplication().screenLocked() && getTwinmeApplication().isInBackground()) {
            mShareExternalContent = false;
            Intent intent = new Intent();
            intent.setClass(this, LockScreenActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu);
        }

        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.share_contact_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.space_action);

        android.view.View actionView = menuItem.getActionView();
        if (actionView == null) {
            return true;
        }

        if (mShareService.numberSpaces(false) <= 1) {
            actionView.setVisibility(View.GONE);
            return false;
        }

        actionView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);

        mSpaceAvatarView = actionView.findViewById(R.id.toolbar_space_item_avatar_view);
        if (mSpaceAvatarView != null) {
            float corner = DESIGN_ITEM_ROUND_CORNER_RADIUS_DP * Resources.getSystem().getDisplayMetrics().density;
            mSpaceAvatarView.setShapeAppearanceModel(
                    mSpaceAvatarView.getShapeAppearanceModel()
                            .toBuilder()
                            .setAllCornerSizes(corner)
                            .build());

            mSpaceAvatarView.setOnClickListener(view -> onSpaceClick());
        }

        mNoAvatarView = actionView.findViewById(R.id.toolbar_space_item_no_avatar_view);
        if (mNoAvatarView != null) {
            mNoAvatarGradientDrawable = new GradientDrawable();
            mNoAvatarGradientDrawable.mutate();
            mNoAvatarGradientDrawable.setStroke((int) (2 * Resources.getSystem().getDisplayMetrics().density), Color.WHITE);
            mNoAvatarGradientDrawable.setColor(Design.BACKGROUND_COLOR_GREY);
            mNoAvatarGradientDrawable.setShape(GradientDrawable.RECTANGLE);
            mNoAvatarView.setBackground(mNoAvatarGradientDrawable);
            mNoAvatarView.setOnClickListener(view -> onSpaceClick());
        }

        mSpaceNameView = actionView.findViewById(R.id.toolbar_space_item_no_avatar_text_view);
        if (mSpaceNameView != null) {
            mSpaceNameView.setTypeface(Design.FONT_BOLD36.typeface);
            mSpaceNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD36.size);
            mSpaceNameView.setTextColor(Color.WHITE);
        }

        if (mSpace != null) {
            updateSpace(mSpace);
        }

        return true;
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        if (mAsyncItemLoader != null) {
            mAsyncItemLoader.stop();
        }

        if (mShareService != null) {
            mShareService.dispose();
        }

        super.onDestroy();
    }

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
    public void onApplyInsetsFinish() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onApplyInsetsFinish");
        }

        super.onApplyInsetsFinish();

        if (mBottomView != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mBottomView.getLayoutParams();
            int bottomMargin = getBarBottomInset();
            if (marginLayoutParams.bottomMargin != bottomMargin) {
                marginLayoutParams.bottomMargin = bottomMargin;
                mBottomView.requestLayout();
            }

            marginLayoutParams = (ViewGroup.MarginLayoutParams) mUIContactRecyclerView.getLayoutParams();
            if (mSelectedUIContact.isEmpty()) {
                marginLayoutParams.bottomMargin = bottomMargin;
            } else {
                marginLayoutParams.bottomMargin = 0;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult: requestCode=" + requestCode + " resultCode=" + resultCode + " intent=" + intent);
        }

        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == CHANGE_SPACE) {
            String value = intent != null ? intent.getStringExtra(Intents.INTENT_SPACE_SELECTION) : null;
            if (value != null) {
                mShareService.getSpace(UUID.fromString(value));
            }
        } else if (requestCode == REQUEST_PREVIEW_FILE) {
            List<FileInfo> fileInfos;
            if (resultCode == RESULT_OK) {
                mSharedFiles.clear();

                String textMessage = intent.getStringExtra(Intents.INTENT_TEXT_MESSAGE);
                boolean allowCopyFile = intent.getBooleanExtra(Intents.INTENT_ALLOW_COPY_FILE, true);
                boolean allowCopyText = intent.getBooleanExtra(Intents.INTENT_ALLOW_COPY_TEXT, true);
                long expireTimeout = intent.getIntExtra(Intents.INTENT_EXPIRE_TIMEOUT, 0);

                try {
                    if (intent.hasExtra(Intents.INTENT_SELECTED_FILES)) {
                        fileInfos = intent.getParcelableArrayListExtra(Intents.INTENT_SELECTED_FILES);

                        if (fileInfos != null) {
                            for (FileInfo fileInfo : fileInfos) {
                                final String filename = fileInfo.getFilename();
                                if (filename != null) {
                                    mSharedFiles.add(fileInfo);
                                }
                            }
                        }
                    }

                    if (intent.hasExtra(Intents.INTENT_CAPTURED_FILE)) {
                        ArrayList<Parcelable> captureFiles = intent.getParcelableArrayListExtra(Intents.INTENT_CAPTURED_FILE);

                        if (captureFiles != null) {
                            for (Parcelable parcelable : captureFiles) {

                                if (parcelable instanceof FileInfo) {
                                    FileInfo fileInfo = (FileInfo) parcelable;
                                    final String filename = fileInfo.getFilename();
                                    if (filename != null) {
                                        mSharedFiles.add(fileInfo);
                                    }
                                } else if (parcelable instanceof UIPreviewFile) {
                                    UIPreviewFile previewFile = (UIPreviewFile) parcelable;
                                    FileInfo fileInfo = new FileInfo(getApplicationContext(), previewFile.getUri());
                                    mSharedFiles.add(fileInfo);
                                }
                            }
                        }
                    }

                    if (textMessage != null && !textMessage.isEmpty()) {
                        mDeferredMessage = textMessage;
                    }

                    mDeferredAllowCopyText = allowCopyText;
                    mDeferredAllowCopyFile = allowCopyFile;
                    mDeferredTimeout = expireTimeout;
                    sendFilesFromPreview();
                } catch (Exception exception) {
                    Log.d(LOG_TAG, "exception=" + exception.getMessage());
                }
            }
        }
    }

    //
    // Override TwinmeActivityImpl methods
    //


    @Override
    public void onGetSpace(@NonNull Space space, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpace: space=" + space);
        }

        mSpace = space;

        updateSpace(mSpace);
    }

    @Override
    public void onSetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpace: space=" + space);
        }

        mSpace = space;

        updateSpace(mSpace);
    }

    @Override
    public void onGetContacts(@NonNull List<Contact> contacts) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContacts: contacts=" + contacts);
        }

        mUIContacts.clear();
        for (Contact contact : contacts) {
            mShareListAdapter.updateUIContact(contact, null);
        }

        notifyShareListChanged();
    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: contact=" + contact);
        }
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: contact=" + contact);
        }

        notifyShareListChanged();
    }

    @Override
    public void onDeleteContact(@NonNull UUID contactId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteContact: contactId=" + contactId);
        }

        mShareListAdapter.removeUIContact(contactId);

        notifyShareListChanged();
    }

    @Override
    public void onGetGroups(@NonNull List<Group> groups) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroups: groups=" + groups);
        }

        mUIGroups.clear();

        if (groups.isEmpty()) {
            notifyShareListChanged();
            return;
        }

        AtomicInteger avatarCounter = new AtomicInteger(groups.size());

        for (Group group : groups) {
            mShareService.getImage(group, (Bitmap avatar) -> {
                mShareListAdapter.updateUIGroup(group, avatar);

                if (avatarCounter.decrementAndGet() == 0) {
                    notifyShareListChanged();
                }
            });
        }
    }

    @Override
    public void onGetConversation(@NonNull Conversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetConversation: conversation=" + conversation);
        }

        mCurrentConversationId = conversation.getId();

        for (int i = 0; i < mSharedFiles.size(); i++) {
            FileInfo media = mSharedFiles.get(i);
            String filename = media.getFilename();
            if (filename == null) {
                String mimeType = media.getMimeType();
                filename = "tmp" + System.currentTimeMillis() + ".";
                if (mimeType != null) {
                    filename += MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                } else {
                    filename += ".tmp";
                }
            }
            final Uri uri = media.getUri();
            if (media.isImage()) {
                sendFile(uri, filename, Descriptor.Type.IMAGE_DESCRIPTOR, mDeferredAllowCopyFile, mDeferredTimeout);
            } else if (media.isVideo()) {
                sendFile(uri, filename, Descriptor.Type.VIDEO_DESCRIPTOR, mDeferredAllowCopyFile, mDeferredTimeout);
            } else if (media.isAudio()) {
                sendFile(uri, filename, Descriptor.Type.AUDIO_DESCRIPTOR, mDeferredAllowCopyFile, mDeferredTimeout);
            } else {
                sendFile(uri, filename, Descriptor.Type.NAMED_FILE_DESCRIPTOR, mDeferredAllowCopyFile, mDeferredTimeout);
            }
        }

        if (mForwardDescriptorId != null) {
            boolean copyAllowed;
            if (mForwardDescriptorType == null || mForwardDescriptorType == Descriptor.Type.OBJECT_DESCRIPTOR) {
                copyAllowed = getTwinmeApplication().messageCopyAllowed();
            } else {
                copyAllowed = getTwinmeApplication().fileCopyAllowed();
            }
            mShareService.forwardDescriptor(mForwardDescriptorId, copyAllowed);
        }

        if (mDeferredMessage != null && !mDeferredMessage.isEmpty()) {
            if (!mShareService.isSendingFiles()) {
                mShareService.pushMessage(mDeferredMessage, mDeferredAllowCopyFile, mDeferredTimeout);
            }
        }

        if (!mSelectedUIContact.isEmpty() && !mShareService.isSendingFiles()) {
            UIContact uiContact = mSelectedUIContact.remove(0);
            mShareService.getConversation(uiContact.getContact());
        } else if (!mShareService.isSendingFiles()) {
            finish();
        }
    }

    @Override
    public void onGetDescriptor(@Nullable ConversationService.Descriptor descriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDescriptor: descriptor=" + descriptor);
        }

        if (descriptor != null) {

            ConversationService.Descriptor replyToDescriptor = null;
            if (descriptor.getReplyToDescriptorId() != null) {
                replyToDescriptor = getTwinmeContext().getConversationService().getDescriptor(descriptor.getReplyToDescriptorId());
            }

            switch (descriptor.getType()) {
                case OBJECT_DESCRIPTOR:
                    if (mIsPeerItem) {
                        mItem = new PeerMessageItem((ConversationService.ObjectDescriptor) descriptor, replyToDescriptor);
                    } else {
                        mItem = new MessageItem((ConversationService.ObjectDescriptor) descriptor, replyToDescriptor);
                    }
                    break;

                case IMAGE_DESCRIPTOR:
                    if (mIsPeerItem) {
                        mItem = new PeerImageItem((ConversationService.ImageDescriptor) descriptor, replyToDescriptor);
                    } else {
                        mItem = new ImageItem((ConversationService.ImageDescriptor) descriptor, replyToDescriptor);
                    }
                    break;

                case AUDIO_DESCRIPTOR:
                    if (mIsPeerItem) {
                        mItem = new PeerAudioItem((ConversationService.AudioDescriptor) descriptor, replyToDescriptor);
                    } else {
                        mItem = new AudioItem((ConversationService.AudioDescriptor) descriptor, replyToDescriptor);
                    }
                    break;

                case VIDEO_DESCRIPTOR:
                    if (mIsPeerItem) {
                        mItem = new PeerVideoItem((ConversationService.VideoDescriptor) descriptor, replyToDescriptor);
                    } else {
                        mItem = new VideoItem((ConversationService.VideoDescriptor) descriptor, replyToDescriptor);
                    }
                    break;
            }

            updateViews();
        } else {
            // Forwarding a descriptor that does not exist anymore: stop the activity.
            finish();
        }
    }

    @Override
    public void onSendFilesFinished(@Nullable UUID conversationId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendFilesFinished");
        }

        if (conversationId != null && !conversationId.equals(mCurrentConversationId)) {
            return;
        }

        if (mDeferredMessage != null) {
            mShareService.pushMessage(mDeferredMessage, mDeferredAllowCopyText, 0);
            if (mSelectedUIContact.isEmpty()) {
                mDeferredMessage = null;
                mDeferredAllowCopyText = false;
            }
        }

        if (!mSendFileError && mSelectedUIContact.isEmpty()) {
            finish();
        } else if (!mSelectedUIContact.isEmpty()) {
            mCurrentConversationId = null;
            UIContact uiContact = mSelectedUIContact.remove(0);
            mShareService.getConversation(uiContact.getContact());
        }
    }

    @Override
    public void onError(ErrorCode errorCode, @Nullable String message, @Nullable Runnable errorCallback) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError " + errorCode);
        }

        if (errorCode == ErrorCode.FILE_NOT_FOUND || errorCode == ErrorCode.FILE_NOT_SUPPORTED) {
            mSendFileError = true;
        }

        super.onError(errorCode, message, errorCallback);
    }

    @Override
    public void onErrorNoPermission() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onErrorNoPermission");
        }

        // We should report an error: the user is not allowed to post the content
    }

    @Override
    public void getContactAvatar(@Nullable UUID peerTwincodeOutboundId, TwinmeContext.Consumer<Bitmap> avatarConsumer) {
        avatarConsumer.accept(null);
    }

    @Override
    public void getPollAvatar(@Nullable UUID peerTwincodeOutboundId, TwinmeContext.Consumer<Bitmap> avatarConsumer) {

        avatarConsumer.accept(null);
    }

    @Override
    public void getShareContactAvatar(@NonNull ConversationService.ContactShareDescriptor contactShareDescriptor, TwinmeContext.Consumer<Bitmap> avatarConsumer) {

        mShareService.getContactShareAvatar(contactShareDescriptor, avatarConsumer);
    }

    @Override
    public void getShareContactIdentityAvatar(TwinmeContext.Consumer<Bitmap> avatarConsumer) {

        avatarConsumer.accept(null);
    }

    @Override
    public boolean isUserVote(@Nullable UUID peerTwincodeOutboundId) {
        return false;
    }

    @Nullable
    @Override
    public Contact getContact() {

        return null;
    }

    @Nullable
    @Override
    public Group getGroup() {

        return null;
    }

    @Override
    public @Nullable Bitmap getContactAvatar() {

        return null;
    }

    @Override
    public @Nullable Bitmap getIdentityAvatar() {

        return null;
    }

    @Override
    public boolean isGroupConversation() {

        return false;
    }

    @Override
    public void markDescriptorRead(@NonNull DescriptorId descriptorId) {

    }

    @Override
    public void updateDescriptor(boolean allowCopy) {

    }

    @Override
    public boolean isPeerTyping() {

        return false;
    }

    @Override
    public boolean isSelectItemMode() {

        return false;
    }

    @Override
    public boolean displayPeerItemAvatar() {

        return false;
    }

    @Override
    @Nullable
    public List<Originator> getTypingOriginators() {

        return null;
    }

    @Override
    @Nullable
    public List<Bitmap> getTypingOriginatorsImages() {

        return null;
    }

    @Override
    public void deleteItem(@NonNull DescriptorId descriptorId) {

    }

    @Override
    public void addLoadableItem(@NonNull final Loader<Item> item) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addLoadableItem: item=" + item);
        }

        mAsyncItemLoader.addItem(item);
    }

    public boolean isSelectedItem(@NonNull DescriptorId descriptorId) {

        return false;
    }

    @Override
    public void onItemLongPress(@Nullable Item item) {

    }

    @Override
    public void onReplyClick(@NonNull DescriptorId descriptorId) {

    }

    @Override
    public void onInfoErrorClick(@NonNull Item item) {

    }

    @Override
    public void onMediaClick(@NonNull DescriptorId descriptorId) {

    }

    @Override
    public void onItemClick(Item item) {

    }

    @Override
    public void onAnnotationClick(@Nullable DescriptorId descriptorId) {

    }

    @Override
    public void onSelectPollChoiceClick(@NonNull ConversationService.PollDescriptor pollDescriptor, @NonNull ConversationService.PollDescriptor.Choice choice, @NonNull Map<UUID, List<ConversationService.PollDescriptor.Choice>> votes) {

    }

    @Override
    public void onPollResultClick(@NonNull org.twinlife.twinlife.ConversationService.PollDescriptor pollDescriptor) {

    }

    @Override
    public void onShareContactClick(@NonNull ConversationService.ContactShareDescriptor contactShareDescriptor) {

    }

    @Override
    public void onShareContactInvitationClick(@NonNull ConversationService.TwincodeDescriptor twincodeDescriptor) {

    }

    @Override
    public boolean isMenuOpen() {

        return false;
    }

    @Override
    public void closeMenu() {

    }

    @Override
    public boolean isReplyViewOpen() {

        return false;
    }

    @Override
    public void audioCall() {

    }

    @Override
    public void videoCall() {

    }

    @Override
    public Bitmap getThumbnail(@NonNull org.twinlife.twinlife.ConversationService.FileDescriptor descriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getThumbnail");
        }

        return getTwinmeContext().getConversationService().getDescriptorThumbnail(descriptor);
    }

    @Override
    @NonNull
    public TextStyle getMessageFont() {

        return Design.FONT_REGULAR32;
    }

    @Override
    @NonNull
    public CustomAppearance getCustomAppearance() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getCustomAppearance");
        }

        return new CustomAppearance();
    }

    @Override
    public void saveGeolocationMap(@NonNull Uri path, @NonNull DescriptorId descriptorId) {

    }


    public void getMapAvatar(@Nullable UUID peerTwincodeOutboundId, @NonNull TwinmeContext.Consumer<Bitmap> uiConsumer) {
        uiConsumer.accept(null);
    }

    @Override
    public String getPeerName(@Nullable UUID peerTwincodeOutboundId) {
        return "";
    }

    //
    // Implement LoaderListener methods
    //

    @Override
    public void onLoaded(@NonNull List<Item> list) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLoaded");
        }

    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setActivityTheme(getTwinmeApplication());
        setContentView(R.layout.share_activity);

        setStatusBarColor();
        setToolBar(R.id.share_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        applyInsets(R.id.share_activity_view, R.id.share_activity_tool_bar, R.id.share_activity_background, Design.TOOLBAR_COLOR, false);

        if (mForwardDescriptorId != null) {
            setTitle(getString(R.string.conversation_view_menu_item_view_forward_title));
        } else {
            setTitle(getString(R.string.share_view_title));
        }

        View backgroundView = findViewById(R.id.share_activity_background);
        backgroundView.setBackgroundColor(Design.WHITE_COLOR);

        View searchView = findViewById(R.id.share_activity_search_view);
        searchView.setBackgroundColor(Design.TOOLBAR_COLOR);

        ViewGroup.LayoutParams layoutParams = searchView.getLayoutParams();
        layoutParams.height = Design.SEARCH_VIEW_HEIGHT;

        View searchContentView = findViewById(R.id.share_activity_search_content_view);

        layoutParams = searchContentView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SEARCH_CONTENT_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) searchContentView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SEARCH_CONTENT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_SEARCH_CONTENT_MARGIN * Design.WIDTH_RATIO);

        ImageView searchIconView = findViewById(R.id.share_activity_search_image_view);

        layoutParams = searchIconView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_SEARCH_GLASS_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_SEARCH_GLASS_ICON_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) searchIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) ((DESIGN_SEARCH_GLASS_ICON_MARGIN + DESIGN_SEARCH_CONTENT_MARGIN) * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_SEARCH_GLASS_ICON_MARGIN * Design.WIDTH_RATIO);

        mClearSearchView = findViewById(R.id.share_activity_clear_image_view);
        mClearSearchView.setVisibility(View.GONE);
        mClearSearchView.setOnClickListener(v -> {
            mSearchEditText.setText("");
            mClearSearchView.setVisibility(View.GONE);
        });

        layoutParams = mClearSearchView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_SEARCH_CLEAR_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_SEARCH_CLEAR_ICON_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mClearSearchView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) ((DESIGN_SEARCH_GLASS_ICON_MARGIN + DESIGN_SEARCH_CONTENT_MARGIN) * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_SEARCH_GLASS_ICON_MARGIN * Design.WIDTH_RATIO);

        mSearchEditText = findViewById(R.id.share_activity_search_edit_text_view);
        Design.updateTextFont(mSearchEditText, Design.FONT_REGULAR34);
        mSearchEditText.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mSearchEditText.setHintTextColor(Design.GREY_COLOR);
        mSearchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() > 0) {
                    mClearSearchView.setVisibility(View.VISIBLE);
                } else {
                    mClearSearchView.setVisibility(View.GONE);
                }
                mShareService.findContactsAndGroupsByName(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mSearchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                mSearchEditText.clearFocus();
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
                }

                return true;
            }

            return false;
        });

        mBottomView = findViewById(R.id.share_activity_bottom_view);
        mBottomView.setBackgroundColor(Design.WHITE_COLOR);
        mBottomView.setVisibility(View.GONE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mBottomView.getLayoutParams();
        marginLayoutParams.bottomMargin = getBarBottomInset();

        View separatorView = findViewById(R.id.share_activity_bottom_header_view);
        separatorView.setBackgroundColor(Design.SEPARATOR_COLOR);

        mPreviewView = findViewById(R.id.share_activity_bottom_preview_view);
        mPreviewView.setClipToOutline(true);

        layoutParams = mPreviewView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_PREVIEW_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_PREVIEW_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mPreviewView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_EDIT_TEXT_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        mEditText = findViewById(R.id.share_activity_comment_edit_text);
        Design.updateTextFont(mEditText, Design.FONT_REGULAR30);
        mEditText.setTextColor(Design.FONT_COLOR_DEFAULT);
        mEditText.setHintTextColor(Design.PLACEHOLDER_COLOR);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mEditText.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        mEditText.setPadding((int) (DESIGN_EDIT_TEXT_WIDTH_INSET * Design.WIDTH_RATIO), (int) (DESIGN_EDIT_TEXT_HEIGHT_INSET * Design.HEIGHT_RATIO), (int) (DESIGN_EDIT_TEXT_WIDTH_INSET * Design.WIDTH_RATIO), (int) (DESIGN_EDIT_TEXT_HEIGHT_INSET * Design.HEIGHT_RATIO));

        if (mMessageFromIntent != null) {
            mEditText.setText(mMessageFromIntent);
        }

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mEditText.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_EDIT_TEXT_MARGIN * Design.HEIGHT_RATIO);

        ViewTreeObserver viewTreeObserver = mEditText.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = mEditText.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalLayoutListener(this);

                int defaultHeight = (int) (DESIGN_EDIT_TEXT_HEIGHT * Design.HEIGHT_RATIO);
                float radius = defaultHeight * 0.5f;
                boolean darkMode = false;
                int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                int displayMode = Settings.displayMode.getInt();
                if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal()) || displayMode == DisplayMode.DARK.ordinal()) {
                    darkMode = true;
                }

                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setColor(Design.EDIT_TEXT_CONVERSATION_BACKGROUND_COLOR);
                gradientDrawable.setCornerRadius(radius);
                if (darkMode) {
                    gradientDrawable.setStroke(3, EDIT_TEXT_BORDER_COLOR);
                }

                mEditText.setBackground(gradientDrawable);
            }
        });

        List<FileInfo> sharedFiles = ShareUtils.getSharedFiles(getApplicationContext(), getIntent());
        if (!sharedFiles.isEmpty()) {
            mEditText.setVisibility(View.GONE);
        }

        View sendView = findViewById(R.id.share_activity_send_clickable_view);
        sendView.setOnClickListener(v -> onSendClick());

        layoutParams = sendView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_SEND_VIEW_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_SEND_VIEW_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) sendView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        View sendRoundedView = findViewById(R.id.share_activity_send_rounded_view);

        float radius = (DESIGN_SEND_VIEW_SIZE * Design.HEIGHT_RATIO) * 0.5f;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable sendBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        sendBackground.getPaint().setColor(Design.getMainStyle());
        sendRoundedView.setBackground(sendBackground);

        ImageView sendImageView = findViewById(R.id.share_activity_send_image_view);
        sendImageView.setColorFilter(Color.WHITE);

        layoutParams = sendImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_SEND_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_SEND_ICON_SIZE * Design.HEIGHT_RATIO);

        LinearLayoutManager uiContactLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mUIContactRecyclerView = findViewById(R.id.share_activity_list_view);
        mUIContactRecyclerView.setLayoutManager(uiContactLinearLayoutManager);
        mUIContactRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mUIContactRecyclerView.setItemAnimator(null);
        mUIContactRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        OnContactTouchListener onTouchContactListener = new OnContactTouchListener(this, mUIContactRecyclerView, this);
        mUIContactRecyclerView.addOnItemTouchListener(onTouchContactListener);

        LinearLayoutManager selectedUIContactLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mSelectedUIContactRecyclerView = findViewById(R.id.share_activity_selected_list_view);
        mSelectedUIContactRecyclerView.setLayoutManager(selectedUIContactLinearLayoutManager);
        mSelectedUIContactRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mSelectedUIContactRecyclerView.setItemAnimator(null);

        layoutParams = mSelectedUIContactRecyclerView.getLayoutParams();
        layoutParams.height = Design.SELECTED_ITEM_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSelectedUIContactRecyclerView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_SEND_VIEW_SIZE * Design.HEIGHT_RATIO) + (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO * 2);

        mProgressBarView = findViewById(R.id.share_activity_progress_bar);

        // Setup the service after the view is initialized but before the adapter!
        mShareService = new ShareService(this, getTwinmeContext(), this, mForwardDescriptorId);

        mAsyncItemLoader = new Manager<>(this, getTwinmeContext(), this);

        mShareListAdapter = new ShareListAdapter(this, mShareService, Design.ITEM_VIEW_HEIGHT, mUIContacts, mUIGroups, R.layout.add_group_member_contact_item, R.id.add_group_member_activity_contact_item_name_view,
                R.id.add_group_member_activity_contact_item_avatar_view, R.id.add_group_member_activity_contact_item_certified_image_view, R.id.add_group_member_activity_contact_item_separator_view);
        mUIContactRecyclerView.setAdapter(mShareListAdapter);

        mSelectedUIContactListAdapter = new UIContactListAdapter(this, mShareService, Design.SELECTED_ITEM_VIEW_HEIGHT, mSelectedUIContact,
                R.layout.add_group_member_selected_contact, 0, R.id.add_group_member_activity_contact_item_avatar_view, 0, 0, 0, 0);
        mSelectedUIContactRecyclerView.setAdapter(mSelectedUIContactListAdapter);

        mUIInitialized = true;
    }

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (!mUIInitialized) {
            return;
        }

        if (mItem != null && isMediaItem()) {
            mPreviewView.setVisibility(View.VISIBLE);
            mPreviewView.setImageBitmap(null);
            ConversationService.FileDescriptor descriptor = getFileDescriptor(mItem);

            Glide.with(this)
                    .asBitmap()
                    .load(descriptor)
                    .apply(Modes.AS_THUMBNAIL)
                    .centerInside()
                    .into(mPreviewView);
        }  else {
            mPreviewView.setVisibility(View.GONE);
        }
    }

    private void sendFile(Uri file, String filename, Descriptor.Type type, boolean allowCopy, long expireTimeout) {
        if (DEBUG) {
            Log.d(LOG_TAG, "sendFile");
        }

        // Send the file asynchronously to avoid blocking the UI thread.
        mShareService.pushFile(file, filename, type, false, allowCopy, null, null, expireTimeout);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void notifyShareListChanged() {
        if (DEBUG) {
            Log.d(LOG_TAG, "notifyShareListChanged");
        }

        if (mUIInitialized) {
            mShareListAdapter.notifyDataSetChanged();

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mUIContactRecyclerView.getLayoutParams();
            int bottomMargin = getBarBottomInset();
            if (mSelectedUIContact.isEmpty()) {
                mUIContactRecyclerView.requestLayout();
                mBottomView.setVisibility(View.GONE);
                marginLayoutParams.bottomMargin = bottomMargin;
            } else {
                mBottomView.setVisibility(View.VISIBLE);
                mSelectedUIContactListAdapter.notifyDataSetChanged();
                marginLayoutParams.bottomMargin = 0;
            }
        }
    }

    private void onSendClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendClick");
        }

        if (mSelectedUIContact.isEmpty()) {
            finish();

            return;
        }

        if (mEditText.getVisibility() == View.VISIBLE && mEditText.getText() != null && !mEditText.getText().toString().isEmpty()) {
            mDeferredMessage = mEditText.getText().toString();
        }

        List<FileInfo> sharedFiles = ShareUtils.getSharedFiles(getApplicationContext(), getIntent());
        if (sharedFiles.isEmpty()) {
            // Get the first selected contact and remove it from the list immediately.
            UIContact uiContact = mSelectedUIContact.remove(0);
            mShareService.getConversation(uiContact.getContact());
        } else {
            startPreview();
        }
    }

    private void startPreview() {
        if (DEBUG) {
            Log.d(LOG_TAG, "startPreview");
        }

        List<FileInfo> sharedFiles = ShareUtils.getSharedFiles(getApplicationContext(), getIntent());

        for (FileInfo media : sharedFiles) {
            if (media.getFilename() != null) {
                // The file can be sent only when it has a filename.
                mSharedFiles.add(media);
            }
        }

        Intent intent = new Intent(this, PreviewFileActivity.class);
        if (mSelectedUIContact.size() == 1) {
            UUID contactId = mSelectedUIContact.get(0).getId();
            intent.putExtra(Intents.INTENT_CONTACT_ID, contactId.toString());
        } else {
            StringBuilder contactName = new StringBuilder();
            for (UIContact uiContact : mSelectedUIContact) {
                if (contactName.length() != 0) {
                    contactName.append(", ");
                }
                contactName.append(uiContact.getName());
            }
            intent.putExtra(Intents.INTENT_CONTACT_NAME, contactName.toString());
        }

        intent.putExtra(Intents.INTENT_PREVIEW_START_WITH_MEDIA, true);
        intent.putExtra(Intents.INTENT_SELECTED_FILES, new ArrayList<>(mSharedFiles));
        intent.putExtra(Intents.INTENT_ALLOW_COPY_FILE, getTwinmeApplication().fileCopyAllowed());
        startActivityForResult(intent, REQUEST_PREVIEW_FILE);
    }

    private void sendFilesFromPreview() {
        if (DEBUG) {
            Log.d(LOG_TAG, "sendFilesFromPreview");
        }

        // Get the first selected contact and remove it from the list immediately.
        UIContact uiContact = mSelectedUIContact.remove(0);
        mShareService.getConversation(uiContact.getContact());
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }

    @Override
    public boolean onUIContactClick(RecyclerView recyclerView, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUIContactClick: recyclerView=" + recyclerView + "position=" + position);
        }

        if (position >= 0) {

            UISelectableContact contact = null;

            if (!mUIContacts.isEmpty() && position <= mUIContacts.size()) {
                contact = mUIContacts.get(position - mShareListAdapter.getMinContactPosition());
            } else if (!mUIGroups.isEmpty()) {
                contact = mUIGroups.get(position - mShareListAdapter.getMinGroupPosition());
            }

            if (contact == null) {
                return false;
            }

            if (contact.isSelected()) {
                contact.setSelected(false);
                mSelectedUIContact.remove(contact);

            } else {
                contact.setSelected(true);
                mSelectedUIContact.add(contact);
            }
            notifyShareListChanged();

            mSelectedUIContactRecyclerView.scrollToPosition(mSelectedUIContact.size() - 1);
            return true;

        }

        return false;
    }

    @Override
    public boolean onUIContactFling(RecyclerView recyclerView, int position, OnContactTouchListener.Direction direction) {

        return false;
    }

    private void onSpaceClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSwitchSpaceClick");
        }

        Intent intent = new Intent();
        intent.putExtra(Intents.INTENT_PICKER_MODE, true);
        intent.setClass(this, SpacesActivity.class);

        startActivityForResult(intent, CHANGE_SPACE);
    }

    private void updateSpace(Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSpace");
        }

        if (space == null || mSpaceAvatarView == null || mNoAvatarView == null || mSpaceNameView == null) {
            return;
        }

        float corner = DESIGN_ITEM_ROUND_CORNER_RADIUS_DP * Resources.getSystem().getDisplayMetrics().density;
        float[] radii = new float[8];
        Arrays.fill(radii, corner);
        if (space.getSpaceAvatarId() != null) {
            mSpaceAvatarView.setVisibility(View.VISIBLE);
            mNoAvatarView.setVisibility(View.GONE);
            mShareService.getSpaceImage(space, (Bitmap avatar) -> {
                mSpaceAvatarView.setImageBitmap(avatar);
            });
            mSpaceNameView.setVisibility(View.GONE);
        } else {
            mNoAvatarGradientDrawable.setCornerRadii(radii);
            mSpaceAvatarView.setVisibility(View.GONE);
            mNoAvatarView.setVisibility(View.VISIBLE);
            mSpaceNameView.setVisibility(View.VISIBLE);

            String name = space.getName();
            if (!name.isEmpty()) {
                mSpaceNameView.setText(name.substring(0, 1).toUpperCase());
            }
            mNoAvatarGradientDrawable.setColor(Design.getDefaultColor(space.getSpaceSettings().getStyle()));
        }
    }

    private boolean isMediaItem() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        return mItem != null
                && (mItem.getType() == Item.ItemType.IMAGE
                || mItem.getType() == Item.ItemType.PEER_IMAGE
                || mItem.getType() == Item.ItemType.VIDEO
                || mItem.getType() == Item.ItemType.PEER_VIDEO);
    }

    @NonNull
    private static ConversationService.FileDescriptor getFileDescriptor(Item item) {
        ConversationService.FileDescriptor descriptor;

        if (item.getType() == Item.ItemType.IMAGE || item.getType() == Item.ItemType.PEER_IMAGE) {
            if (item.isPeerItem()) {
                final PeerImageItem peerImageItem = (PeerImageItem) item;
                descriptor = peerImageItem.getImageDescriptor();
            } else {
                final ImageItem imageItem = (ImageItem) item;
                descriptor = imageItem.getImageDescriptor();
            }
        } else {
            if (item.isPeerItem()) {
                final PeerVideoItem peerVideoItem = (PeerVideoItem) item;
                descriptor = peerVideoItem.getVideoDescriptor();
            } else {
                final VideoItem videoItem = (VideoItem) item;
                descriptor = videoItem.getVideoDescriptor();
            }
        }
        return descriptor;
    }

    @NonNull
    private ArrayList<FileInfo> importFiles(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "importFiles");
        }

        ArrayList<FileInfo> res = new ArrayList<>();
        for (FileInfo fileInfo : ShareUtils.getSharedFiles(getApplicationContext(), intent)) {
            FileInfo copy;
            if (fileInfo.isImage() || fileInfo.isVideo()) {
                copy = fileInfo.saveMedia(getApplicationContext(), getTwinmeApplication().qualityMedia());
            } else {
                copy = fileInfo.saveFile(getApplicationContext());
            }
            if (copy != null) {
                res.add(copy);
            }
        }

        return res;
    }
}
