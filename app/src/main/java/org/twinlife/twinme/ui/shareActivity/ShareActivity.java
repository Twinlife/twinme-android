/*
 *  Copyright (c) 2018-2025 twinlife SA.
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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BaseService;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.Conversation;
import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.DescriptorId;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.ShareService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.TextStyle;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.baseItemActivity.AudioItem;
import org.twinlife.twinme.ui.baseItemActivity.BaseItemActivity;
import org.twinlife.twinme.ui.baseItemActivity.ImageItem;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.ui.baseItemActivity.MessageItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerAudioItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerImageItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerMessageItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerVideoItem;
import org.twinlife.twinme.ui.baseItemActivity.PreviewItemListAdapter;
import org.twinlife.twinme.ui.baseItemActivity.VideoItem;
import org.twinlife.twinme.ui.conversationActivity.ConversationActivity;
import org.twinlife.twinme.ui.users.OnContactTouchListener;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.ui.users.UIContactListAdapter;
import org.twinlife.twinme.ui.users.UISelectableContact;
import org.twinlife.twinme.utils.FileInfo;
import org.twinlife.twinme.utils.ShareUtils;
import org.twinlife.twinme.utils.async.Loader;
import org.twinlife.twinme.utils.async.LoaderListener;
import org.twinlife.twinme.utils.async.Manager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activity controller called to share content (file, text)
 */

public class ShareActivity extends BaseItemActivity implements ShareService.Observer, OnContactTouchListener.OnContactObserver, LoaderListener<Item> {
    private static final String LOG_TAG = "ShareActivity";
    private static final boolean DEBUG = false;

    private static final float DESIGN_SELECTED_BOTTOM_MARGIN = 40f;
    private static final float DESIGN_EDIT_TEXT_WIDTH_INSET = 32f;
    private static final float DESIGN_EDIT_TEXT_HEIGHT_INSET = 20f;
    private static final float DESIGN_EDIT_TEXT_RADIUS = 18f;
    private static final float DESIGN_COMMENT_VIEW_HEIGHT = 140f;
    private static int SELECTED_BOTTOM_MARGIN;

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;
    private View mSelectedUIContactView;
    private UIContactListAdapter mSelectedUIContactListAdapter;
    private RecyclerView mSelectedUIContactRecyclerView;
    private RecyclerView mUIContactRecyclerView;
    private ShareListAdapter mShareListAdapter;
    private EditText mSearchEditText;
    private View mClearSearchView;
    @Nullable
    private PreviewItemListAdapter mPreviewListAdapter;
    private EditText mEditText;
    private final List<UISelectableContact> mUIContacts = new ArrayList<>();
    private final List<UISelectableContact> mUIGroups = new ArrayList<>();
    private final List<FileInfo> mSharedUri = new ArrayList<>();
    private final List<UIContact> mSelectedUIContact = new ArrayList<>();

    private CharSequence mMessageFromIntent;
    private String mDeferredMessage;
    private boolean mDeferredAllowCopyText;
    private boolean mSendFileError = false;

    private ShareService mShareService;

    private Menu mMenu;

    @Nullable
    private DescriptorId mForwardDescriptorId;
    @Nullable
    private Descriptor.Type mForwardDescriptorType;
    @Nullable
    private Item mItem;
    private boolean mIsPeerItem;

    private Manager<Item> mAsyncItemLoader;

    private int maxRootViewHeight = 0;

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
            Design.setTheme(this, getTwinmeApplication());
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
                intent.putParcelableArrayListExtra(Intents.INTENT_DIRECT_SHARE_URIS, importFiles(incomingIntent));
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
        mForwardDescriptorType = (Descriptor.Type) incomingIntent.getSerializableExtra(Intents.INTENT_DESCRIPTOR_TYPE);
        mIsPeerItem = incomingIntent.getBooleanExtra(Intents.INTENT_IS_PEER_ITEM, false);

        mMessageFromIntent = ShareUtils.getSharedText(incomingIntent);

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
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu);
        }

        super.onCreateOptionsMenu(menu);

        mMenu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.forward_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.add_action);

        TextView titleView = (TextView) menuItem.getActionView();
        String title = menuItem.getTitle().toString();

        if (titleView != null) {
            Design.updateTextFont(titleView, Design.FONT_BOLD36);
            titleView.setTextColor(Color.WHITE);
            titleView.setText(title);
            titleView.setPadding(0, 0, Design.TOOLBAR_TEXT_ITEM_PADDING, 0);
            titleView.setOnClickListener(view -> onSendClicked());
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

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    public void onGetSpace(@NonNull Space space, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpace: space=" + space);
        }
    }

    @Override
    public void onSetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpace: space=" + space);
        }

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

        for (int i = 0; i < mSharedUri.size(); i++) {
            FileInfo media = mSharedUri.get(i);
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
            if (media.isImage()) {
                sendFile(media.getUri(), filename, Descriptor.Type.IMAGE_DESCRIPTOR, getTwinmeApplication().fileCopyAllowed());
            } else if (media.isVideo()) {
                sendFile(media.getUri(), filename, Descriptor.Type.VIDEO_DESCRIPTOR, getTwinmeApplication().fileCopyAllowed());
            } else if (media.isAudio()) {
                sendFile(media.getUri(), filename, Descriptor.Type.AUDIO_DESCRIPTOR, getTwinmeApplication().fileCopyAllowed());
            } else {
                sendFile(media.getUri(), filename, Descriptor.Type.NAMED_FILE_DESCRIPTOR, getTwinmeApplication().fileCopyAllowed());
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

        if (!mEditText.getText().toString().isEmpty()) {
            if (mShareService.isSendingFiles()) {
                mDeferredMessage = mEditText.getText().toString();
                mDeferredAllowCopyText = getTwinmeApplication().messageCopyAllowed();
            } else {
                mShareService.pushMessage(mEditText.getText().toString(), getTwinmeApplication().messageCopyAllowed());
            }
        }

        if (!mSelectedUIContact.isEmpty()) {
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

            if (mItem != null) {
                mItem.setMode(Item.ItemMode.PREVIEW);

                LinearLayoutManager previewLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
                RecyclerView previewRecyclerView = findViewById(R.id.share_activity_preview_view);
                previewRecyclerView.setLayoutManager(previewLinearLayoutManager);
                previewRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
                previewRecyclerView.setItemAnimator(null);
                previewRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

                mPreviewListAdapter = new PreviewItemListAdapter(this, mItem);
                previewRecyclerView.setAdapter(mPreviewListAdapter);
            }
        } else {
            // Forwarding a descriptor that does not exist anymore: stop the activity.
            finish();
        }
    }

    @Override
    public void onSendFilesFinished() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendFilesFinished");
        }

        if (mDeferredMessage != null) {
            mShareService.pushMessage(mDeferredMessage, mDeferredAllowCopyText);
            mDeferredMessage = null;
            mDeferredAllowCopyText = false;
        }

        if (!mSendFileError && mSelectedUIContact.isEmpty()) {
            finish();
        }
    }

    @Override
    public void onError(BaseService.ErrorCode errorCode, @Nullable String message, @Nullable Runnable errorCallback) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError " + errorCode);
        }

        if (errorCode == BaseService.ErrorCode.FILE_NOT_FOUND || errorCode == BaseService.ErrorCode.FILE_NOT_SUPPORTED) {
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
    public void onMediaClick(@NonNull DescriptorId descriptorId) {

    }

    @Override
    public void onItemClick(Item item) {

    }

    @Override
    public void onAnnotationClick(@Nullable DescriptorId descriptorId) {

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

    //
    // Implement LoaderListener methods
    //

    @Override
    public void onLoaded(@NonNull List<Item> list) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLoaded");
        }

        if (mPreviewListAdapter != null) {
            mPreviewListAdapter.notifyDataSetChanged();
        }
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.share_activity);

        setStatusBarColor();
        setToolBar(R.id.share_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        applyInsets(R.id.share_activity_view, R.id.share_activity_tool_bar, R.id.share_activity_list_view, Design.TOOLBAR_COLOR, false);

        if (mForwardDescriptorId != null) {
            setTitle(getString(R.string.conversation_activity_menu_item_view_forward_title));
        } else {
            setTitle(getString(R.string.share_activity_title));
        }

        View rootView = findViewById(R.id.share_activity_view);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                    int currentRootViewHeight = rootView.getHeight();
                    if (currentRootViewHeight > maxRootViewHeight) {
                        maxRootViewHeight = currentRootViewHeight;
                    }

                    if (mItem != null) {
                        Item.ItemMode mode = mItem.getMode();
                        if (currentRootViewHeight >= maxRootViewHeight) {
                            mItem.setMode(Item.ItemMode.PREVIEW);
                        } else {
                            mItem.setMode(Item.ItemMode.SMALL_PREVIEW);
                        }

                        if (mode != mItem.getMode() && mPreviewListAdapter != null) {
                            mPreviewListAdapter.notifyDataSetChanged();
                        }
                    }
        });

        View searchView = findViewById(R.id.share_activity_search_view);
        searchView.setBackgroundColor(Design.TOOLBAR_COLOR);

        ViewGroup.LayoutParams layoutParams = searchView.getLayoutParams();
        layoutParams.height = Design.SEARCH_VIEW_HEIGHT;

        mClearSearchView = findViewById(R.id.share_activity_clear_image_view);
        mClearSearchView.setVisibility(View.GONE);
        mClearSearchView.setOnClickListener(v -> {
            mSearchEditText.setText("");
            mClearSearchView.setVisibility(View.GONE);
        });

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

        mSelectedUIContactView = findViewById(R.id.share_activity_layout_selected_view);
        mSelectedUIContactView.setBackgroundColor(Design.WHITE_COLOR);

        layoutParams = mSelectedUIContactView.getLayoutParams();
        layoutParams.height = Design.SELECTED_ITEM_VIEW_HEIGHT;
        mSelectedUIContactView.setLayoutParams(layoutParams);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mSelectedUIContactView.getLayoutParams();
        marginLayoutParams.bottomMargin = SELECTED_BOTTOM_MARGIN;

        View commentView = findViewById(R.id.share_activity_comment_view);
        commentView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        layoutParams = commentView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_COMMENT_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        mEditText = findViewById(R.id.share_activity_comment_edit_text);
        Design.updateTextFont(mEditText, Design.FONT_REGULAR30);
        mEditText.setTextColor(Design.FONT_COLOR_DEFAULT);
        mEditText.setHintTextColor(Design.PLACEHOLDER_COLOR);

        float radius = DESIGN_EDIT_TEXT_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable editTextBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        editTextBackground.getPaint().setColor(Design.FORWARD_COMMENT_COLOR);
        mEditText.setBackground(editTextBackground);

        mEditText.setPadding((int) (DESIGN_EDIT_TEXT_WIDTH_INSET * Design.WIDTH_RATIO), (int) (DESIGN_EDIT_TEXT_HEIGHT_INSET * Design.HEIGHT_RATIO), (int) (DESIGN_EDIT_TEXT_WIDTH_INSET * Design.WIDTH_RATIO), (int) (DESIGN_EDIT_TEXT_HEIGHT_INSET * Design.HEIGHT_RATIO));

        if (mMessageFromIntent != null) {
            mEditText.setText(mMessageFromIntent);
        }

        LinearLayoutManager uiContactLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mUIContactRecyclerView= findViewById(R.id.share_activity_list_view);
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

        mProgressBarView = findViewById(R.id.share_activity_progress_bar);

        // Setup the service after the view is initialized but before the adapter!
        mShareService = new ShareService(this, getTwinmeContext(), this, mForwardDescriptorId);

        mAsyncItemLoader = new Manager<>(this, getTwinmeContext(), this);

        mShareListAdapter = new ShareListAdapter(this, mShareService, Design.ITEM_VIEW_HEIGHT, mUIContacts, mUIGroups, R.layout.add_group_member_contact_item, R.id.add_group_member_activity_contact_item_name_view,
                R.id.add_group_member_activity_contact_item_avatar_view,  R.id.add_group_member_activity_contact_item_certified_image_view, R.id.add_group_member_activity_contact_item_separator_view);
        mUIContactRecyclerView.setAdapter(mShareListAdapter);

        mSelectedUIContactListAdapter = new UIContactListAdapter(this, mShareService, Design.SELECTED_ITEM_VIEW_HEIGHT, mSelectedUIContact,
                R.layout.add_group_member_selected_contact, 0, R.id.add_group_member_activity_contact_item_avatar_view, 0, 0, 0, 0);
        mSelectedUIContactRecyclerView.setAdapter(mSelectedUIContactListAdapter);

        mUIInitialized = true;
    }

    private void sendFile(Uri file, String filename, Descriptor.Type type, boolean allowCopy) {
        if (DEBUG) {
            Log.d(LOG_TAG, "sendFile");
        }

        // Send the file asynchronously to avoid blocking the UI thread.
        mShareService.pushFile(file, filename, type, false, allowCopy, null, null, 0);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void notifyShareListChanged() {
        if (DEBUG) {
            Log.d(LOG_TAG, "notifyShareListChanged");
        }

        if (mUIInitialized) {

            mShareListAdapter.notifyDataSetChanged();

            if (mSelectedUIContact.isEmpty()) {
                mUIContactRecyclerView.requestLayout();
                mSelectedUIContactView.setVisibility(View.GONE);

                if (mMenu != null) {
                    MenuItem sendMenuItem = mMenu.findItem(R.id.add_action);
                    sendMenuItem.getActionView().setAlpha(0.5f);
                    sendMenuItem.setEnabled(false);
                }
            } else {
                mSelectedUIContactView.setVisibility(View.VISIBLE);

                if (mMenu != null) {
                    MenuItem sendMenuItem = mMenu.findItem(R.id.add_action);
                    sendMenuItem.getActionView().setAlpha(1f);
                    sendMenuItem.setEnabled(true);
                }

                ViewGroup.LayoutParams layoutParams = mSelectedUIContactRecyclerView.getLayoutParams();
                layoutParams.height = Design.SELECTED_ITEM_VIEW_HEIGHT;
                layoutParams.width = (mSelectedUIContact.size() + 1) * Design.SELECTED_ITEM_VIEW_HEIGHT;
                mSelectedUIContactRecyclerView.setLayoutParams(layoutParams);
                mSelectedUIContactRecyclerView.requestLayout();
                mSelectedUIContactListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void onSendClicked() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendClicked");
        }

        if (mSelectedUIContact.isEmpty()) {
            finish();

            return;
        }

        List<Uri> sharedUris = ShareUtils.getSharedFiles(getIntent());

        if (sharedUris.isEmpty()) {
            // Get the first selected contact and remove it from the list immediately.
            UIContact uiContact = mSelectedUIContact.remove(0);
            mShareService.getConversation(uiContact.getContact());

        } else {
            getTwinmeContext().execute(() -> {

                for (Uri uri: sharedUris) {
                    FileInfo media = new FileInfo(getApplicationContext(), uri);
                    if (media.getFilename() != null) {
                        // The file can be sent only when it has a filename.
                        mSharedUri.add(media);
                    }
                }

                runOnUiThread(() -> {
                    if (!mSelectedUIContact.isEmpty()) {
                        // Get the first selected contact and remove it from the list immediately.
                        UIContact uiContact = mSelectedUIContact.remove(0);
                        mShareService.getConversation(uiContact.getContact());
                    }
                });
            });
        }
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

    @NonNull
    private ArrayList<Uri> importFiles(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "importFiles");
        }

        ArrayList<Uri> res = new ArrayList<>();
        for (Uri sharedFile : ShareUtils.getSharedFiles(intent)) {
            FileInfo fileInfo = new FileInfo(getApplicationContext(), sharedFile);
            FileInfo copy;
            if (fileInfo.isImage() || fileInfo.isVideo()) {
                copy = fileInfo.saveMedia(getApplicationContext(), getTwinmeApplication().sendImageSize());
            } else {
                copy = fileInfo.saveFile(getApplicationContext());
            }
            if (copy != null) {
                res.add(copy.getUri());
            }
        }

        return res;
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        SELECTED_BOTTOM_MARGIN = (int) (DESIGN_SELECTED_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
    }
}
