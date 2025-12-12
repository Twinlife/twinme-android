/*
 *  Copyright (c) 2015-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Denis Campredon (Denis.Campredon@twinlife-systems.com)
 *   Houssem Temanni (Houssem.Temanni@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Yannis Le Gal (Yannis.LeGal@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.AssertPoint;
import org.twinlife.twinlife.BaseService;
import org.twinlife.twinlife.ConversationService.AnnotationType;
import org.twinlife.twinlife.ConversationService.AudioDescriptor;
import org.twinlife.twinlife.ConversationService.CallDescriptor;
import org.twinlife.twinlife.ConversationService.ClearDescriptor;
import org.twinlife.twinlife.ConversationService.ClearMode;
import org.twinlife.twinlife.ConversationService.Conversation;
import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.DescriptorAnnotation;
import org.twinlife.twinlife.ConversationService.DescriptorId;
import org.twinlife.twinlife.ConversationService.GroupConversation;
import org.twinlife.twinlife.ConversationService.ImageDescriptor;
import org.twinlife.twinlife.ConversationService.InvitationDescriptor;
import org.twinlife.twinlife.ConversationService.NamedFileDescriptor;
import org.twinlife.twinlife.ConversationService.ObjectDescriptor;
import org.twinlife.twinlife.ConversationService.TransientObjectDescriptor;
import org.twinlife.twinlife.ConversationService.TwincodeDescriptor;
import org.twinlife.twinlife.ConversationService.UpdateType;
import org.twinlife.twinlife.ConversationService.VideoDescriptor;
import org.twinlife.twinlife.DisplayCallsMode;
import org.twinlife.twinlife.ExportedImageId;
import org.twinlife.twinlife.Filter;
import org.twinlife.twinlife.TwincodeOutbound;
import org.twinlife.twinlife.Twinlife;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.audio.AudioDevice;
import org.twinlife.twinme.audio.AudioListener;
import org.twinlife.twinme.calls.CallStatus;
import org.twinlife.twinme.models.CertificationLevel;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.GroupMember;
import org.twinlife.twinme.models.Originator;
import org.twinlife.twinme.models.Typing;
import org.twinlife.twinme.services.ConversationService;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.skin.TextStyle;
import org.twinlife.twinme.ui.ApplicationAssertPoint;
import org.twinlife.twinme.ui.InfoItemActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.baseItemActivity.AudioItem;
import org.twinlife.twinme.ui.baseItemActivity.AudioItemViewHolder;
import org.twinlife.twinme.ui.baseItemActivity.BaseItemActivity;
import org.twinlife.twinme.ui.baseItemActivity.CallItem;
import org.twinlife.twinme.ui.baseItemActivity.ClearItem;
import org.twinlife.twinme.ui.baseItemActivity.FileItem;
import org.twinlife.twinme.ui.baseItemActivity.ImageItem;
import org.twinlife.twinme.ui.baseItemActivity.InvitationContactItem;
import org.twinlife.twinme.ui.baseItemActivity.InvitationItem;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.ui.baseItemActivity.ItemListAdapter;
import org.twinlife.twinme.ui.baseItemActivity.LinkItem;
import org.twinlife.twinme.ui.baseItemActivity.MessageItem;
import org.twinlife.twinme.ui.baseItemActivity.NameItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerAudioItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerAudioItemViewHolder;
import org.twinlife.twinme.ui.baseItemActivity.PeerCallItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerClearItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerFileItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerImageItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerInvitationContactItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerInvitationItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerLinkItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerMessageItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerVideoItem;
import org.twinlife.twinme.ui.baseItemActivity.ReplyItemTouchHelper;
import org.twinlife.twinme.ui.baseItemActivity.TimeItem;
import org.twinlife.twinme.ui.baseItemActivity.VideoItem;
import org.twinlife.twinme.ui.callActivity.CallActivity;
import org.twinlife.twinme.ui.calls.CallAgainConfirmView;
import org.twinlife.twinme.ui.cleanupActivity.ResetConversationConfirmView;
import org.twinlife.twinme.ui.cleanupActivity.TypeCleanUpActivity;
import org.twinlife.twinme.ui.contacts.DeleteConfirmView;
import org.twinlife.twinme.ui.conversationFilesActivity.ConversationFilesActivity;
import org.twinlife.twinme.ui.conversationFilesActivity.FullscreenMediaActivity;
import org.twinlife.twinme.ui.conversationFilesActivity.ItemSelectedActionView;
import org.twinlife.twinme.ui.exportActivity.ExportActivity;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumFeatureConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.ui.shareActivity.ShareActivity;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.ConversationEditText;
import org.twinlife.twinme.utils.FileInfo;
import org.twinlife.twinme.utils.NetworkStatus;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.SaveAsyncTask;
import org.twinlife.twinme.utils.ShareUtils;
import org.twinlife.twinme.utils.UIMenuSelectAction;
import org.twinlife.twinme.utils.Utils;
import org.twinlife.twinme.utils.async.LoaderListener;
import org.twinlife.twinme.utils.async.Manager;
import org.twinlife.twinme.utils.coachmark.CoachMark;
import org.twinlife.twinme.utils.coachmark.CoachMarkView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ConversationActivity extends BaseItemActivity implements ConversationService.Observer,
        BaseItemActivity.AudioItemObserver, BaseItemActivity.InvitationItemObserver,
        LoaderListener<Item>, AudioListener, ItemSelectedActionView.Observer, AnnotationsView.Observer {
    private static final String LOG_TAG = "ConversationActivity";
    private static final boolean DEBUG = false;

    private static final String SEND_ALLOWED = "sendAllowed";
    private static final String CURRENT_MODE = "mode";
    private static final String CAPTURE_URI = "captureUri";

    private static final String TYPED_TEXT = "typedText";

    private static final int EDIT_TEXT_BORDER_COLOR = Color.rgb(78, 78, 78);
    private static final int EDIT_ITEM_CONFIRM_COLOR = Color.rgb(38, 209, 160);

    private static final int COACH_MARK_DELAY = 500;

    private static final float DESIGN_REPLY_HEIGHT = 120f;
    private static final float DESIGN_MENU_WIDTH = 556f;
    private static final float DESIGN_MENU_REACTION_HEIGHT = 100f;
    private static final float DESIGN_MENU_ACTION_HEIGHT = 90f;
    private static final float DESIGN_EDITBAR_HEIGHT = 90f;
    private static final float DESIGN_TOOLBAR_HEIGHT = 90f;
    private static final float DESIGN_AVATAR_VIEW_HEIGHT = 42f;
    private static final float DESIGN_AVATAR_MARGIN = 10f;
    private static final float DESIGN_PEER_MENU_START_MARGIN_PERCENT = 0.128f;
    private static final float DESIGN_LOCAL_MENU_END_MARGIN_PERCENT = 0.0693f;
    private static final float DESIGN_EDIT_TEXT_WIDTH_INSET = 32f;
    private static final float DESIGN_EDIT_TEXT_HEIGHT_INSET = 20f;
    private static final float DESIGN_EDIT_TEXT_TOP_MARGIN = 28f;
    private static final float DESIGN_EDIT_TEXT_CAMERA_ICON_HEIGHT = 30f;
    private static final float DESIGN_EDIT_TEXT_AUDIO_ICON_HEIGHT = 34f;
    private static final float DESIGN_MENU_ICON_HEIGHT = 36f;
    private static final float DESIGN_SEND_IMAGE_HEIGHT = 30f;
    private static final float DESIGN_MENU_REACTION_MARGIN = 18f;
    private static final float DESIGN_TOOLBAR_ACTION_MARGIN = 12f;
    private static final float DESIGN_EDIT_MESSAGE_VIEW_HEIGHT = 60;
    private static final float DESIGN_EDIT_MESSAGE_ICON_HEIGHT = 26;
    private static final float DESIGN_EDIT_MESSAGE_MARGIN = 24;
    private static final float DESIGN_ZOOM_VIEW_HEIGHT = 160f;
    private static final float DESIGN_ZOOM_VIEW_MARGIN = 80f;

    private static final float DESIGN_MIN_FONT = 10f;
    private static final float DESIGN_MAX_FONT = 80f;

    private static final int DESIGN_SCROLL_INDICATOR_WIDTH = 146;
    private static final int DESIGN_SCROLL_INDICATOR_HEIGHT = 106;
    private static final int DESIGN_SCROLL_INDICATOR_BOTTOM = 46;
    private static final int DESIGN_SCROLL_INDICATOR_MARGIN = 22;
    private static final int DESIGN_SCROLL_INDICATOR_IMAGE_HEIGHT = 64;
    private static final float DESIGN_SELECTED_VIEW_HEIGHT = 128f;
    private static final int DESIGN_TOP_BLUR_CONTAINER_VIEW_HEIGHT = 30;
    private static final int DESIGN_TOP_BLUR_VIEW_HEIGHT = 20;

    private static int REPLY_HEIGHT;
    private static int MENU_REACTION_HEIGHT;
    private static int MENU_WIDTH;
    private static int MENU_HEIGHT;
    private static int AVATAR_VIEW_HEIGHT;
    private static int AVATAR_MARGIN;

    private static final long MAX_DELTA_TIMESTAMP1 = 2 * 60 * 1000; /*- Between message groups */
    private static final long MAX_DELTA_TIMESTAMP2 = 60 * 60 * 1000; /*- Time indicator */

    private static final int REQUEST_GET_FILE = 1;
    private static final int REQUEST_CREATE_DOCUMENT = 2;
    private static final int REQUEST_PREVIEW_MEDIA = 3;
    private static final int REQUEST_PREVIEW_FILE = 4;
    private static final int REQUEST_TAKE_PHOTO = 5;

    private static final int RESULT_DID_SHARE_ACTION = 100;

    // Make sure that TYPING_RESEND_DELAY < TYPING_TIME_DURATION < TYPING_PEER_TIMER_DURATION
    private static final int TYPING_RESEND_DELAY = 8 * 1000;
    private static final int TYPING_TIMER_DURATION = TYPING_RESEND_DELAY + (2 * 1000);
    private static final int TYPING_PEER_TIMER_DURATION = TYPING_TIMER_DURATION + (2 * 1000);

    private enum Mode {
        DEFAULT, TEXT, GALLERY, CAMERA, MICRO, FILE
    }

    private class EditTextTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (DEBUG) {
                Log.d(LOG_TAG, "EditTextTouchListener.onTouch: view=" + view + " event=" + event);
            }

            setSelectedMode(Mode.TEXT);

            return false;
        }
    }

    private class SendListener implements OnClickListener, View.OnLongClickListener {

        // Start disabled, it is enabled first when we get the contact or group (ie, Twinlife is ready).
        private boolean disabled = true;

        @Override
        public void onClick(View view) {
            if (DEBUG) {
                Log.d(LOG_TAG, "SendListener.onClick: view=" + view);
            }

            if (disabled) {

                return;
            }
            disabled = true;

            onSendClick();
        }

        @Override
        public boolean onLongClick(View view) {

            onSendLongClick();

            return true;
        }

        void reset() {

            disabled = false;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {

            mZoomView.setVisibility(View.VISIBLE);
            mZoomView.bringToFront();

            PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1.0f);
            ObjectAnimator alphaViewAnimator = ObjectAnimator.ofPropertyValuesHolder(mZoomView, propertyValuesHolderAlpha);
            alphaViewAnimator.setDuration(300L);
            alphaViewAnimator.start();

            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float size = mMessageFont.size * detector.getScaleFactor();
            float maxSize = DESIGN_MAX_FONT * Design.MIN_RATIO;
            float minSize = DESIGN_MIN_FONT * Design.MIN_RATIO;

            if (size > maxSize) {
                size = maxSize;
            } else if (size < minSize) {
                size = minSize;
            }

            if (mMessageFont.size != size) {
                mMessageFont = Design.customRegularFont(size);
                mItemListAdapter.notifyDataSetChanged();
            }

            float zoomPercent = (size / Design.FONT_REGULAR32.size) * 100;
            mZoomTextView.setText(String.format("%.0f%%", zoomPercent));

            return true;
        }

        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            super.onScaleEnd(detector);

            PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.0f);
            ObjectAnimator alphaViewAnimator = ObjectAnimator.ofPropertyValuesHolder(mZoomView, propertyValuesHolderAlpha);
            alphaViewAnimator.setDuration(300L);
            alphaViewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mZoomView.setVisibility(View.GONE);
                }
            });

            alphaViewAnimator.start();
        }
    }

    private UUID mContactId;
    private UUID mGroupId;
    private DescriptorId mDescriptorId;
    private final Map<UUID, Originator> mGroupMembers = new HashMap<>();
    @Nullable
    private Conversation mConversation;

    private TextView mEmptyConversationView;
    private FrameLayout mContainerRecyclerView;
    private ItemRecyclerView mItemListView;
    private LinearLayoutManager mItemListViewLayoutManager;
    private ItemListAdapter mItemListAdapter;
    private ConversationEditText mEditText;
    private View mEditMessageView;
    private View mMenuClickableView;
    private View mEditConfirmView;
    private View mEditCancelView;
    private View mSendClickableView;
    private View mRecordAudioClickableView;
    private View mCameraClickableView;
    private SendListener mSendButtonListener;
    private RoundedView mNoAvatarView;
    private CircularImageView mAvatarView;
    private View mCertifiedView;
    private CoachMarkView mCoachMarkView;
    private View mScrollIndicatorView;
    private View mScrollIndicatorOverlayView;
    private TextView mScrollIndicatorCountView;
    private ItemSelectedActionView mItemSelectedActionView;
    private View mTopBlurContainerView;
    private View mTopBlurView;
    private View mZoomView;
    private TextView mZoomTextView;
    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;
    private boolean mDeletedGroup = false;
    private Mode mSelectedMode = Mode.DEFAULT;
    private Mode mDeferredMode = Mode.DEFAULT;
    private boolean mDeferredSaveMedia = false;
    private boolean mDeferredSaveFile = false;
    private Bitmap mContactAvatar;
    private Bitmap mIdentityAvatar;
    @Nullable
    private Originator mSubject;
    private final ArrayList<Item> mItems = new ArrayList<>();
    private final ArrayList<Item> mSelectedItems = new ArrayList<>();
    private boolean mBatchUpdate = true;
    private Item mLastReadPeerItem = null;
    private VoiceRecorderMessageView mVoiceRecorderMessageView;
    private Uri mCaptureUri;
    private AudioItemViewHolder mPlayingAudioItemViewHolder;
    private PeerAudioItemViewHolder mPlayingPeerAudioItemViewHolder;
    private View mFooterView;
    private View mEditToolbarView;
    private View mHeaderOverlayView;
    private View mFooterOverlayView;
    private MenuItemView mMenuItemView;
    private MenuReactionView mMenuReactionView;
    private View mOverlayView;
    private ProgressBar mProgressBarView;
    private MenuSendOptionView mMenuSendOptionView;
    private MenuActionConversationView mMenuActionConversationView;
    private ReplyView mReplyView;
    private AnnotationsView mAnnotationsView;
    @Nullable
    private Menu mMenu;
    private boolean mIsMenuSendOptionOpen = false;
    private boolean mAllowCopy = true;

    private boolean mIsTyping = false;
    private boolean mIsPeerTyping = false;
    private boolean mSendAllowed = true;
    private final List<Originator> mTypingOriginators = new ArrayList<>();
    private final List<Bitmap> mTypingOriginatorsImages = new ArrayList<>();
    private ScheduledFuture<?> mTypingTimer = null;
    private ScheduledFuture<?> mPeerTypingTimer = null;
    private long mTypingSendTime = 0;

    private boolean mSelectItemMode = false;

    @Nullable
    private Item mSelectedItem;
    @Nullable
    private Item mReplyItem;
    @Nullable
    private Item mEditItem;
    private boolean mIsMenuOpen = false;
    private int mItemListViewHeight;
    private int mOpenItemIndex = -1;
    private int mScrollIndicatorCount = 0;

    private CharSequence mSharedText;
    private boolean mInitToolbar = false;

    private String mDeferredMessage;
    private boolean mDeferredAllowCopyText;
    private long mDeferredTimeout;
    private DescriptorId mDeferredReplyTo;

    private ConversationService mConversationService;

    private ScaleGestureDetector mScaleDetector;
    private TextStyle mMessageFont;

    private SharedPreferences mSharedPreferences;

    private int mHeaderHeight;

    private Manager<Item> mAsyncItemLoader;

    private boolean mLoadingDescriptors = false;

    private boolean mAllDescriptorsLoaded = false;

    private ActivityResultLauncher<PickVisualMediaRequest> mMediaPicker;

    /**
     * If not null, indicates to {@link ConversationActivity#onGetDescriptors(List)} that the user taped a reply and
     * we're waiting on additional descriptors to scroll back to the "reply-to" message.
     */
    @Nullable
    private DescriptorId mReplyToDescriptorId;

    private final Filter<Descriptor> mDescriptorFilter = new Filter<Descriptor>(null) {
        @Override
        public boolean accept(@NonNull Descriptor descriptor) {
            switch (descriptor.getType()) {
                case AUDIO_DESCRIPTOR:
                    return ((AudioDescriptor) descriptor).isAvailable();
                case NAMED_FILE_DESCRIPTOR:
                    return ((NamedFileDescriptor) descriptor).isAvailable();
                default:
                    return true;
            }
        }
    };

    @Nullable
    public Item getSelectedItem() {

        return mSelectedItem;
    }

    //
    // Overrides TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mAsyncItemLoader = new Manager<>(this, getTwinmeContext(), this);

        // Restore the send allowed flag.  It is important to restore the 'false' state because we are not allowed
        // to send and the editText contains the no-permission message.  When initViews() setup the editText, its
        // afterTextChanged() callback is triggered but we don't want to send the Typing.START message.
        if (savedInstanceState != null) {
            mSendAllowed = savedInstanceState.getBoolean(SEND_ALLOWED, true);
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Intent intent = getIntent();
        mContactId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));
        if (mContactId == null) {
            mGroupId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_GROUP_ID));

            if (mGroupId == null) {
                handleShortcut(intent);
            }
        }

        mDescriptorId = DescriptorId.fromString(intent.getStringExtra(Intents.INTENT_DESCRIPTOR_ID));

        setupDesign();
        initViews();

        mConversationService = new ConversationService(this, getTwinmeContext(), this);

        if (mContactId == null && mGroupId == null) {
            finish();

            return;
        }

        // Restore the selected medias (this is a minimal restore to avoid crashing).
        if (savedInstanceState != null) {
            Mode savedMode = (Mode) savedInstanceState.getSerializable(CURRENT_MODE);
            if (savedMode != null && savedMode != Mode.CAMERA) {
                if (savedMode == Mode.GALLERY) {
                    Permission[] permissions = new Permission[]{
                            Permission.READ_EXTERNAL_STORAGE
                    };
                    mDeferredMode = Mode.GALLERY;
                    if (checkPermissions(permissions)) {
                        setSelectedMode(Mode.GALLERY);
                    }
                } else {
                    setSelectedMode(savedMode);
                }
            }

            mCaptureUri = savedInstanceState.getParcelable(CAPTURE_URI);
        }

        mMediaPicker = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(10), uris -> {
            if (!uris.isEmpty()) {
                onPreviewMedia(uris, false);
            }
        });
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        mResumed = false;

        mAsyncItemLoader.clear();
        mConversationService.resetActiveConversation();

        if (mIsTyping && mSendAllowed) {
            mIsTyping = false;
            Typing typing = new Typing(Typing.Action.STOP);
            mConversationService.pushTyping(typing);
        }

        if (mTypingTimer != null) {
            mTypingTimer.cancel(false);
            mTypingTimer = null;
        }

        if (isRecording()) {
            mVoiceRecorderMessageView.stopRecording();
        }

        if(mPlayingAudioItemViewHolder != null) {
            mPlayingAudioItemViewHolder.resetView();
        }

        if(mPlayingPeerAudioItemViewHolder != null) {
            mPlayingPeerAudioItemViewHolder.resetView();
        }

        hideKeyboard();

        saveTypedText();

        clearConversationPlayingAudio();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();
        if (mDeletedGroup) {
            finish();
        } else {

            // Get the contact and group after we resume so that we can refresh the information.
            final DisplayCallsMode callsMode = getTwinmeApplication().displayCallsMode();
            if (mContactId != null) {
                mLoadingDescriptors = true;
                mConversationService.getContact(mContactId, callsMode, mDescriptorFilter);
            }
            if (mGroupId != null) {
                mLoadingDescriptors = true;
                mConversationService.getGroup(mGroupId, callsMode, mDescriptorFilter);
            }
            mConversationService.setActiveConversation();
        }

        showCoachMark();
    }

    //
    // Overrides Activity methods
    //

    @Override
    protected void onStart() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStart");
        }

        super.onStart();
    }

    @Override
    protected void onStop() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStop");
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mUIInitialized = false;
        mUIPostInitialized = false;

        mAsyncItemLoader.stop();

        if (mIsTyping && mSendAllowed) {
            mIsTyping = false;
            Typing typing = new Typing(Typing.Action.STOP);
            mConversationService.pushTyping(typing);
        }

        // Release the conversation service before releasing the UI components.
        mConversationService.dispose();

        if (mItemListView != null) {
            mItemListView.setAdapter(null);
        }

        if (mTypingTimer != null) {
            mTypingTimer.cancel(false);
            mTypingTimer = null;
        }

        if (mPeerTypingTimer != null) {
            mPeerTypingTimer.cancel(false);
            mPeerTypingTimer = null;
        }

        hideKeyboard();

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);

        outState.putBoolean(SEND_ALLOWED, mSendAllowed);

        if (mSelectedMode != Mode.DEFAULT) {
            outState.putSerializable(CURRENT_MODE, mSelectedMode);
        }
        if (mCaptureUri != null) {
            outState.putParcelable(CAPTURE_URI, mCaptureUri);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult: requestCode=" + requestCode + " resultCode=" + resultCode + " intent=" + intent);
        }

        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GET_FILE:
                    List<Uri> fileUris = new ArrayList<>();
                    ClipData clipData = (intent == null) ? null : intent.getClipData();
                    if (clipData != null && clipData.getItemCount() > 0) {
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            fileUris.add(clipData.getItemAt(i).getUri());
                        }
                    } else {
                        // single selection or old android
                        Uri uri = (intent == null) ? null : intent.getData();
                        if (uri != null) {
                            fileUris.add(uri);
                        }
                    }
                    onPreviewFile(fileUris, false);
                    break;

                case REQUEST_CREATE_DOCUMENT:
                    Uri fileUri = (intent == null) ? null : intent.getData();
                    if (fileUri != null) {
                        saveFileToUri(fileUri);
                    } else {
                        closeMenu();
                    }
                    break;

                case REQUEST_TAKE_PHOTO:
                    if (mCaptureUri != null) {
                        List<Uri> uris = new ArrayList<>();
                        uris.add(mCaptureUri);
                        onPreviewMedia(uris, false);
                    }
                    break;

                case REQUEST_PREVIEW_MEDIA:
                case REQUEST_PREVIEW_FILE:
                    String textMessage = intent.getStringExtra(Intents.INTENT_TEXT_MESSAGE);
                    boolean allowCopyFile = intent.getBooleanExtra(Intents.INTENT_ALLOW_COPY_FILE, true);
                    boolean allowCopyText = intent.getBooleanExtra(Intents.INTENT_ALLOW_COPY_TEXT, true);

                    long expireTimeout = 0;

                    try {
                        if (intent.hasExtra(Intents.INTENT_SELECTED_URI)) {
                            ArrayList<String> urisToString = intent.getStringArrayListExtra(Intents.INTENT_SELECTED_URI);

                            if (urisToString != null) {
                                for (String sendUri : urisToString) {
                                    FileInfo fileInfo = new FileInfo(getApplicationContext(), Uri.parse(sendUri));
                                    if (fileInfo.getFilename() != null) {
                                        if (fileInfo.isImage()) {
                                            sendFile(fileInfo.getUri(), fileInfo.getFilename(), Descriptor.Type.IMAGE_DESCRIPTOR, true, allowCopyFile, expireTimeout);
                                        } else if (fileInfo.isVideo()) {
                                            sendFile(fileInfo.getUri(), fileInfo.getFilename(), Descriptor.Type.VIDEO_DESCRIPTOR, true, allowCopyFile, expireTimeout);
                                        } else {
                                            sendFile(fileInfo.getUri(), fileInfo.getFilename(), Descriptor.Type.NAMED_FILE_DESCRIPTOR, true, allowCopyFile, expireTimeout);
                                        }
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
                                        if (fileInfo.getFilename() != null) {
                                            if (fileInfo.isImage()) {
                                                sendFile(fileInfo.getUri(), fileInfo.getFilename(), Descriptor.Type.IMAGE_DESCRIPTOR, true, allowCopyFile, expireTimeout);
                                            } else if (fileInfo.isVideo()) {
                                                sendFile(fileInfo.getUri(), fileInfo.getFilename(), Descriptor.Type.VIDEO_DESCRIPTOR, true, allowCopyFile, expireTimeout);
                                            } else {
                                                sendFile(fileInfo.getUri(), fileInfo.getFilename(), Descriptor.Type.NAMED_FILE_DESCRIPTOR, true, allowCopyFile, expireTimeout);
                                            }
                                        }
                                    } else if (parcelable instanceof UIPreviewFile) {
                                        UIPreviewFile previewFile = (UIPreviewFile) parcelable;
                                        FileInfo fileInfo = new FileInfo(getApplicationContext(), previewFile.getUri());
                                        sendFile(fileInfo.getUri(), fileInfo.getFilename(), Descriptor.Type.NAMED_FILE_DESCRIPTOR, true, allowCopyFile, expireTimeout);
                                    }
                                }
                            }
                        }

                        if (textMessage != null && !textMessage.trim().isEmpty()) {
                            DescriptorId replyTo = null;
                            if (mReplyItem != null) {
                                replyTo = mReplyItem.getDescriptorId();
                            }

                            if (mConversationService.isSendingFiles()) {
                                mDeferredMessage = textMessage;
                                mDeferredAllowCopyText = allowCopyText;
                                mDeferredTimeout = expireTimeout;
                                mDeferredReplyTo = replyTo;
                            } else {
                                mConversationService.pushMessage(textMessage, allowCopyText, expireTimeout, replyTo);
                            }
                        }

                        mEditText.setText("");
                        closeReplyView();
                    } catch (Exception exception) {
                        Log.d(LOG_TAG, "exception=" + exception.getMessage());
                    }
                    break;
            }
        }
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
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu);
        }

        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conversation_menu, menu);

        mMenu = menu;

        if (mSubject != null) {
            updateOptionsMenu();
        }
        return true;
    }

    private void updateOptionsMenu() {
        if (mMenu == null) {
            return;
        }

        MenuItem menuAudioItem = mMenu.findItem(R.id.audio_call_action);

        ImageView audioImageView = (ImageView) menuAudioItem.getActionView();

        if (audioImageView != null) {
            audioImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.action_bar_audio_call, null));
            audioImageView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);
            audioImageView.setOnClickListener(view -> onAudioClick());
            audioImageView.setContentDescription(getString(R.string.conversation_activity_audio_call));
        }

        MenuItem menuVideoItem = mMenu.findItem(R.id.video_call_action);

        ImageView videoImageView = (ImageView) menuVideoItem.getActionView();

        if (videoImageView != null) {
            videoImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.action_bar_video_call, null));
            videoImageView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);
            videoImageView.setOnClickListener(view -> onVideoClick());
            videoImageView.setContentDescription(getString(R.string.conversation_activity_video_call));
        }

        MenuItem menuCancelItem = mMenu.findItem(R.id.cancel_action);
        TextView titleView = (TextView) menuCancelItem.getActionView();
        String title = menuCancelItem.getTitle().toString();

        if (titleView != null) {
            Design.updateTextFont(titleView, Design.FONT_BOLD36);
            titleView.setTextColor(Color.WHITE);
            titleView.setText(title);
            titleView.setPadding(0, 0, Design.TOOLBAR_TEXT_ITEM_PADDING, 0);
            titleView.setOnClickListener(view -> onCancelSelectItemModeClick());
        }
        menuCancelItem.setVisible(false);

        updateInCall();
    }

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        boolean cameraGranted = false;
        boolean microphoneGranted = false;
        boolean storageReadAccessGranted = false;
        boolean storageWriteAccessGranted = false;
        for (Permission grantedPermission : grantedPermissions) {
            switch (grantedPermission) {
                case CAMERA:
                    cameraGranted = true;
                    break;

                case RECORD_AUDIO:
                    microphoneGranted = true;
                    break;

                case READ_EXTERNAL_STORAGE:
                case READ_MEDIA_AUDIO:
                    storageReadAccessGranted = true;
                    break;

                case WRITE_EXTERNAL_STORAGE:
                    storageWriteAccessGranted = true;
                    break;
            }
        }
        switch (mDeferredMode) {
            case GALLERY:
                mDeferredMode = Mode.DEFAULT;

                if (cameraGranted || storageReadAccessGranted) {
                    setSelectedMode(Mode.GALLERY);
                } else {
                    message(getString(R.string.application_denied_permissions), 0L, new DefaultMessageCallback(R.string.application_ok) {
                    });
                }
                break;

            case MICRO:
                mDeferredMode = Mode.DEFAULT;

                if (microphoneGranted) {
                    setSelectedMode(Mode.MICRO);
                    mVoiceRecorderMessageView.startRecording();
                } else {
                    message(getString(R.string.application_denied_permissions), 0L, new DefaultMessageCallback(R.string.application_ok) {
                    });
                }
                break;

            case FILE:
                mDeferredMode = Mode.DEFAULT;

                if (storageReadAccessGranted) {
                    openFileIntent();
                } else {
                    message(getString(R.string.application_denied_permissions), 0L, new DefaultMessageCallback(R.string.application_ok) {
                    });
                }
                break;
        }

        if (mDeferredSaveMedia) {
            mDeferredSaveMedia = false;

            if (storageWriteAccessGranted) {
                saveMediaInGallery();
            }
        } else if (mDeferredSaveFile) {
            mDeferredSaveFile = false;

            if (storageWriteAccessGranted) {
                saveFile();
            }
        }
    }

    //
    // Override BaseItemActivity methods
    //

    /**
     * Get the avatar picture to be used for the given peer.
     * <p>
     * When the peer twincode is null, use the contact avatar.
     * <p>
     * For a normal conversation, this operation always returns the contact avatar.
     * For a group conversation, we return the group member avatar.
     *
     * @param peerTwincodeOutboundId the peer twincode to get the picture.
     */
    @Override
    public void getContactAvatar(@Nullable UUID peerTwincodeOutboundId, TwinmeContext.Consumer<Bitmap> avatarConsumer) {

        if (peerTwincodeOutboundId == null) {
            avatarConsumer.accept(mContactAvatar);
        }

        Originator member = mGroupMembers.get(peerTwincodeOutboundId);
        if (member != null) {
            mConversationService.getImage(member, avatarConsumer);
        } else {
            avatarConsumer.accept(mContactAvatar);
        }
    }

    @WorkerThread
    @Nullable
    private Bitmap getContactAvatar(@Nullable UUID peerTwincodeOutboundId) {

        if (peerTwincodeOutboundId == null) {
            return mContactAvatar;
        }

        Originator member = mGroupMembers.get(peerTwincodeOutboundId);
        if (member != null) {
            return mConversationService.getImage(member);
        }
        return mContactAvatar;
    }

    @Override
    public @Nullable
    Contact getContact() {
        if (mSubject != null && mSubject.getType() == Originator.Type.CONTACT) {
            return (Contact) mSubject;
        }
        return null;
    }

    @Override
    public @Nullable
    Group getGroup() {
        if (mSubject != null && mSubject.getType() == Originator.Type.GROUP) {
            return (Group) mSubject;
        }
        return null;
    }

    @Override
    public boolean isGroupConversation() {

        return mGroupId != null;
    }

    @Override
    public void markDescriptorRead(@NonNull DescriptorId descriptorId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "markDescriptorRead: descriptorId=" + descriptorId);
        }

        // Don't mark the descriptor read if we are in background.
        if (mResumed) {
            mConversationService.markDescriptorRead(descriptorId);
        }
    }

    @Override
    public void updateDescriptor(boolean allowCopy) {

    }

    @Override
    public boolean isPeerTyping() {

        return mIsPeerTyping;
    }

    @Override
    public boolean isSelectItemMode() {

        return mSelectItemMode;
    }

    @Override
    @Nullable
    public List<Originator> getTypingOriginators() {

        return mTypingOriginators;
    }

    @Override
    @Nullable
    public List<Bitmap> getTypingOriginatorsImages() {

        return mTypingOriginatorsImages;
    }

    @Override
    public boolean isSelectedItem(@NonNull DescriptorId descriptorId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "isSelectedItem: descriptorId=" + descriptorId);
        }

        return mSelectedItem != null && mSelectedItem.getDescriptorId().equals(descriptorId);
    }

    @Override
    public void onReplyClick(@NonNull DescriptorId descriptorId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onReplyClick: descriptorId=" + descriptorId);
        }

        int replyItemIndex = -1;
        for (int index = mItems.size() - 1; index >= 0; index--) {
            Item item = mItems.get(index);

            if (item.getDescriptorId().equals(descriptorId)) {
                replyItemIndex = index;
                break;
            }
        }

        if (replyItemIndex != -1) {
            replyItemIndex++;
            mItemListView.scrollToPosition(replyItemIndex);
            mReplyToDescriptorId = null;
        } else {
            long firstSequenceId = mItems.size() < 2 ? -1 : mItems.get(1).getDescriptorId().sequenceId;
            if (descriptorId.sequenceId < firstSequenceId) {
                mReplyToDescriptorId = descriptorId;
                mConversationService.getPreviousObjectDescriptors();
            } else {
                mReplyToDescriptorId = null;
            }
        }
    }

    @Override
    public void onMediaClick(@NonNull DescriptorId descriptorId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMediaClick: descriptorId=" + descriptorId);
        }

        startFullscreenMediaActivity(descriptorId);
    }

    @Override
    public void onItemClick(Item item) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onItemClick: item=" + item);
        }

        if (item.isSelected()) {
            item.setSelected(false);
            mSelectedItems.remove(item);
        } else {
            item.setSelected(true);
            mSelectedItems.add(item);
        }

        mItemSelectedActionView.updateSelectedItems(mSelectedItems.size());
        mItemListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemLongPress(@NonNull Item item) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onItemLongPress: item=" + item);
        }

        if (mSelectItemMode) {
            return;
        }

        mSelectedItem = item;

        hideKeyboard();
        openMenu();
    }

    @Override
    public void onAnnotationClick(@Nullable DescriptorId descriptorId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAnnotationClick: descriptorId=" + descriptorId);
        }

        if (mSelectItemMode || descriptorId == null) {
            return;
        }

        mConversationService.listAnnotations(descriptorId, (BaseService.ErrorCode errorCode, Map<TwincodeOutbound, org.twinlife.twinlife.ConversationService.DescriptorAnnotation> annotations) -> {
            // This lambda is run by mTwinlifeExecutor, so we can call the blocking getImage() variant.

            if (annotations == null || mSubject == null) {
                return;
            }

            List<UIAnnotation> uiAnnotations = new ArrayList<>();

            for (Map.Entry<TwincodeOutbound, DescriptorAnnotation> annotation : annotations.entrySet()) {
                DescriptorAnnotation descriptorAnnotation = annotation.getValue();

                if (descriptorAnnotation.getType() == AnnotationType.LIKE) {
                    String name = annotation.getKey().getName();
                    Bitmap avatar = mConversationService.getTwincodeImage(annotation.getKey());
                    UIReaction uiReaction = new UIReaction(descriptorAnnotation.getValue());

                    if (name != null && avatar != null) {
                        UIAnnotation uiAnnotation = new UIAnnotation(uiReaction, name, avatar);
                        uiAnnotations.add(uiAnnotation);
                    }
                }
            }

            if (!uiAnnotations.isEmpty()) {
                runOnUiThread(() -> openAnnotationsView(uiAnnotations));
            }
        });
    }

    @Override
    public boolean isMenuOpen() {

        return mIsMenuOpen;
    }

    @Override
    public boolean isReplyViewOpen() {

        return mReplyItem != null;
    }

    //
    // Menu management
    //

    private void openMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu");
        }

        if (mIsMenuOpen || mSelectedItem == null || mItemListView == null || mSelectedItem.getState() == Item.ItemState.BOTH_DELETED) {

            return;
        }

        hapticFeedback();
        mIsMenuOpen = true;

        int menuHeight = getMenuItemViewHeight();
        LayoutParams layoutParams = mMenuItemView.getLayoutParams();
        layoutParams.height = menuHeight;

        ViewGroup.LayoutParams overlayLayoutParams = mHeaderOverlayView.getLayoutParams();
        if (mItemListViewLayoutManager.findFirstVisibleItemPosition() == 0 && mItemListView.getY() > 0) {
            overlayLayoutParams.height = (int) (mContainerRecyclerView.getY() + mItemListView.getY() + BaseItemActivity.HEADER_HEIGHT);
        } else if (mItemListViewLayoutManager.findFirstVisibleItemPosition() == 0) {
            overlayLayoutParams.height = (int) (mContainerRecyclerView.getY() + BaseItemActivity.HEADER_HEIGHT);
        } else {
            overlayLayoutParams.height = (int) (mContainerRecyclerView.getY());
        }
        mHeaderOverlayView.setLayoutParams(overlayLayoutParams);

        mReplyView.hideOverlay(false);
        mScrollIndicatorOverlayView.setVisibility(View.VISIBLE);

        boolean addReaction = false;

        switch (mSelectedItem.getType()) {
            case MESSAGE:
            case PEER_MESSAGE:
            case LINK:
            case PEER_LINK:
            case IMAGE:
            case PEER_IMAGE:
            case VIDEO:
            case PEER_VIDEO:
            case AUDIO:
            case PEER_AUDIO:
            case FILE:
            case PEER_FILE:
                addReaction = true;
                break;

            default:
                break;
        }

        if (mSelectedItem.getState() == Item.ItemState.DELETED) {
            addReaction = false;
        }

        if (addReaction) {
            mMenuReactionView.setVisibility(View.VISIBLE);
            mMenuReactionView.openMenu();
        }

        mMenuItemView.setVisibility(View.VISIBLE);
        mHeaderOverlayView.setVisibility(View.VISIBLE);
        mFooterOverlayView.setVisibility(View.VISIBLE);
        mItemListView.setBackgroundColor(Design.BACKGROUND_COLOR_WHITE_OPACITY85);
        mMenuItemView.openMenu();

        int countVisibleItem = mItemListViewLayoutManager.findLastVisibleItemPosition() - mItemListViewLayoutManager.findFirstVisibleItemPosition() + 1;
        mItemListAdapter.notifyItemRangeChanged(mItemListViewLayoutManager.findFirstVisibleItemPosition(), countVisibleItem);
        mItemListView.setScrollEnable(false);

        int color = ColorUtils.compositeColors(Design.BACKGROUND_COLOR_WHITE_OPACITY85, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    void setMenuPosition() {

        if (mSelectedItem == null) {
            return;
        }

        int itemIndex = -1;
        for (int index = mItems.size() - 1; index >= 0; index--) {
            Item lItem = mItems.get(index);
            if (lItem.getDescriptorId().equals(mSelectedItem.getDescriptorId())) {
                itemIndex = index;
                break;
            }
        }

        if (itemIndex != -1) {
            int position = mItemListAdapter.indexToPosition(itemIndex);
            View viewSelected = mItemListViewLayoutManager.findViewByPosition(position);
            View containerRecyclerView = findViewById(R.id.conversation_activity_item_list_container_view);
            if (viewSelected != null) {
                float viewSelectedY = viewSelected.getY() + mItemListView.getY() + containerRecyclerView.getY();
                int menuHeight = getMenuItemViewHeight();
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                boolean initialPosition = true;
                float menuY;
                if ((viewSelectedY + viewSelected.getHeight() + menuHeight) > metrics.heightPixels) {
                    initialPosition = false;
                    if ((viewSelectedY - menuHeight) < 0) {
                        menuY = ((float) metrics.heightPixels - menuHeight) / 2;
                    } else {
                        menuY = viewSelectedY - menuHeight;
                    }
                } else {
                    menuY = viewSelectedY + viewSelected.getHeight();
                }

                mMenuItemView.setY(menuY);

                float menuReactionY;
                if (initialPosition) {
                    menuReactionY = menuY - viewSelected.getHeight() - MENU_REACTION_HEIGHT;
                } else {
                    menuReactionY = viewSelectedY + viewSelected.getHeight();
                }

                if ((menuReactionY < (menuY + menuHeight) && menuReactionY > menuY) || menuReactionY < 0) {
                    menuReactionY = menuY + menuHeight;
                } else if (menuReactionY > mFooterView.getY()) {
                    menuReactionY = mFooterView.getY() + (DESIGN_MENU_REACTION_MARGIN * Design.HEIGHT_RATIO);
                }

                mMenuReactionView.setY(menuReactionY);

                if (mSelectedItem.isPeerItem()) {
                    mMenuItemView.setX(DESIGN_PEER_MENU_START_MARGIN_PERCENT * Design.DISPLAY_WIDTH);
                    mMenuReactionView.setX(DESIGN_PEER_MENU_START_MARGIN_PERCENT * Design.DISPLAY_WIDTH);
                } else {
                    mMenuItemView.setX(Design.DISPLAY_WIDTH - (MENU_WIDTH + DESIGN_LOCAL_MENU_END_MARGIN_PERCENT * Design.DISPLAY_WIDTH));
                    mMenuReactionView.setX(Design.DISPLAY_WIDTH - (mMenuReactionView.getMenuWidth() + (DESIGN_LOCAL_MENU_END_MARGIN_PERCENT * Design.DISPLAY_WIDTH)));
                }
            }
        }
    }

    @Override
    public void closeMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeMenu");
        }

        if (mIsMenuOpen) {
            mIsMenuOpen = false;
            mSelectedItem = null;
            mMenuItemView.setVisibility(View.INVISIBLE);
            mMenuReactionView.setVisibility(View.INVISIBLE);
            mHeaderOverlayView.setVisibility(View.INVISIBLE);
            mFooterOverlayView.setVisibility(View.INVISIBLE);
            mReplyView.hideOverlay(true);
            mScrollIndicatorOverlayView.setVisibility(View.GONE);

            ViewGroup.LayoutParams overlayLayoutParams = mHeaderOverlayView.getLayoutParams();
            overlayLayoutParams.height = mHeaderHeight;
            mHeaderOverlayView.setLayoutParams(overlayLayoutParams);

            mItemListView.setBackgroundColor(Design.CONVERSATION_BACKGROUND_COLOR);
            int countVisibleItem = mItemListViewLayoutManager.findLastVisibleItemPosition() - mItemListViewLayoutManager.findFirstVisibleItemPosition() + 1;
            mItemListAdapter.notifyItemRangeChanged(mItemListViewLayoutManager.findFirstVisibleItemPosition(), countVisibleItem);

            mItemListView.setScrollEnable(true);
            setStatusBarColor();
        } else if (mIsMenuSendOptionOpen) {
            mIsMenuSendOptionOpen = false;
            mMenuSendOptionView.setVisibility(View.INVISIBLE);
            mOverlayView.setVisibility(View.INVISIBLE);
            setStatusBarColor();
        } else if (mAnnotationsView.getVisibility() == View.VISIBLE) {
            mAnnotationsView.animationCloseMenu();
        }
    }

    @Override
    public void audioCall() {
        if (DEBUG) {
            Log.d(LOG_TAG, "audioCall");
        }

        if (getTwinmeApplication().inCallInfo() == null) {
            if (mSubject == null || !mSubject.getCapabilities().hasAudio()) {
                Toast.makeText(this, R.string.application_not_authorized_operation_by_your_contact, Toast.LENGTH_SHORT).show();
            } else {
                showCallAgainConfirmView(false);
            }
        }
    }

    @Override
    public void videoCall() {
        if (DEBUG) {
            Log.d(LOG_TAG, "videoCall");
        }

        if (getTwinmeApplication().inCallInfo() == null) {
            if (mSubject == null || !mSubject.getCapabilities().hasVideo()) {
                Toast.makeText(this, R.string.application_not_authorized_operation_by_your_contact, Toast.LENGTH_SHORT).show();
            } else {
                showCallAgainConfirmView(true);
            }
        }
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
        if (DEBUG) {
            Log.d(LOG_TAG, "getMessageFont");
        }

        return mMessageFont;
    }

    public void closeReplyView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeReplyView");
        }

        if (mReplyItem != null) {
            mReplyView.setVisibility(View.GONE);
            mReplyItem = null;
            mSelectedItem = null;
            mItemListAdapter.notifyItemChanged(mItemListAdapter.getItemCount() - 1);
        }
    }

    public void onCopyItemClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCopyItemClick");
        }

        if (mSelectedItem != null) {
            if (mSelectedItem.isClearLocalItem()) {
                Toast.makeText(this, R.string.conversation_activity_local_cleanup, Toast.LENGTH_SHORT).show();
                return;
            } else if (mSelectedItem.getState() == Item.ItemState.DELETED || (mSelectedItem.isPeerItem() && (!mSelectedItem.getCopyAllowed() || mSelectedItem.isEphemeralItem()))) {
                Toast.makeText(this, R.string.conversation_activity_menu_item_view_operation_not_allowed, Toast.LENGTH_SHORT).show();
                return;
            } else if (!mSelectedItem.isAvailableItem()) {
                return;
            }

            String content = "";
            if (mSelectedItem.getType() == Item.ItemType.MESSAGE) {
                MessageItem messageItem = (MessageItem) mSelectedItem;
                content = messageItem.getContent();
            } else if (mSelectedItem.getType() == Item.ItemType.PEER_MESSAGE) {
                PeerMessageItem peerMessageItem = (PeerMessageItem) mSelectedItem;
                content = peerMessageItem.getContent();
            } else if (mSelectedItem.getType() == Item.ItemType.LINK) {
                LinkItem linkItem = (LinkItem) mSelectedItem;
                content = linkItem.getContent();
            } else if (mSelectedItem.getType() == Item.ItemType.PEER_LINK) {
                PeerLinkItem peerLinkItem = (PeerLinkItem) mSelectedItem;
                content = peerLinkItem.getContent();
            }

            Utils.setClipboard(this, content);
            closeMenu();
            Toast.makeText(this, R.string.conversation_activity_menu_item_view_copy_message, Toast.LENGTH_SHORT).show();
        }
    }

    public void onReplyItemClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onReplyItemClick");
        }

        if (mSubject != null && mSelectedItem != null && mSelectedItem.getState() != Item.ItemState.DELETED) {

            if (mEditItem != null) {
                onCloseEditViewClick();
            }

            mReplyItem = mSelectedItem;
            mReplyView.setVisibility(View.VISIBLE);
            if (mSubject.getType() == Originator.Type.GROUP) {
                Originator member = mGroupMembers.get(mReplyItem.getPeerTwincodeOutboundId());
                if (mReplyItem.isPeerItem() && member != null) {
                    mReplyView.showReply(mSelectedItem, member.getName());
                } else {
                    mReplyView.showReply(mSelectedItem, mSubject.getIdentityName());
                }
            } else if (mReplyItem.isPeerItem()) {
                mReplyView.showReply(mSelectedItem, mSubject.getName());
            } else {
                mReplyView.showReply(mSelectedItem, mSubject.getIdentityName());
            }
            closeMenu();
            mItemListAdapter.notifyItemChanged(mItemListAdapter.getItemCount() - 1);
        }
    }

    public void onDeleteItemClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteItemClick");
        }

        if (mSelectedItem != null) {
            if (mSelectedItem.isPeerItem()) {
                mConversationService.deleteDescriptor(mSelectedItem.getDescriptorId());
            } else {
                mConversationService.markDescriptorDeleted(mSelectedItem.getDescriptorId());
            }
            closeMenu();
        }
    }

    public void onForwardItemClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onForwardItemClick");
        }

        if (mSelectedItem != null) {
            if (mSelectedItem.isClearLocalItem()) {
                Toast.makeText(this, R.string.conversation_activity_local_cleanup, Toast.LENGTH_SHORT).show();
                return;
            } else if (mSelectedItem.getState() == Item.ItemState.DELETED || (mSelectedItem.isPeerItem() && (!mSelectedItem.getCopyAllowed() || mSelectedItem.isEphemeralItem()))) {
                Toast.makeText(this, R.string.conversation_activity_menu_item_view_operation_not_allowed, Toast.LENGTH_SHORT).show();
                return;
            } else if (!mSelectedItem.isAvailableItem()) {
                return;
            }

            DescriptorId descriptorId = mSelectedItem.getDescriptorId();
            Descriptor.Type type;
            switch (mSelectedItem.getType()) {
                case IMAGE:
                case PEER_IMAGE:
                    type = Descriptor.Type.IMAGE_DESCRIPTOR;
                    break;

                case VIDEO:
                case PEER_VIDEO:
                    type = Descriptor.Type.VIDEO_DESCRIPTOR;
                    break;

                case AUDIO:
                case PEER_AUDIO:
                    type = Descriptor.Type.AUDIO_DESCRIPTOR;
                    break;

                case FILE:
                case PEER_FILE:
                    type = Descriptor.Type.NAMED_FILE_DESCRIPTOR;
                    break;

                case MESSAGE:
                case PEER_MESSAGE:
                case LINK:
                case PEER_LINK:
                default:
                    type = Descriptor.Type.OBJECT_DESCRIPTOR;
                    break;
            }

            boolean isPeerItem = mSelectedItem.isPeerItem();

            closeMenu();

            Intent intent = new Intent(this, ShareActivity.class);
            intent.putExtra(Intents.INTENT_DESCRIPTOR_ID, descriptorId.toString());
            intent.putExtra(Intents.INTENT_IS_PEER_ITEM, isPeerItem);
            intent.putExtra(Intents.INTENT_DESCRIPTOR_TYPE, type);

            startActivity(intent);
        }
    }

    public void onEditItemClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditItemClick");
        }

        if (mSelectedItem != null) {

            mEditItem = mSelectedItem;

            if (mReplyItem != null) {
                closeReplyView();
            }

            String content = "";
            if (mEditItem.getType() == Item.ItemType.MESSAGE) {
                MessageItem messageItem = (MessageItem) mEditItem;
                content = messageItem.getContent();
            } else if (mEditItem.getType() == Item.ItemType.LINK) {
                LinkItem linkItem = (LinkItem) mEditItem;
                content = linkItem.getContent();
            }

            mEditMessageView.setVisibility(View.VISIBLE);
            mEditCancelView.setVisibility(View.VISIBLE);
            mEditConfirmView.setVisibility(View.VISIBLE);

            mSendClickableView.setVisibility(View.GONE);
            mMenuClickableView.setVisibility(View.GONE);

            mEditText.setText(content);
            setSelectedMode(Mode.TEXT);

            mEditText.requestFocus();
            mEditText.setSelection(content.length());
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
            }

            closeMenu();
        }
    }

    public void onInfoItemClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInfoItemClick");
        }

        if (mSelectedItem != null) {
            Intent intent = new Intent(this, InfoItemActivity.class);
            if (mContactId != null) {
                intent.putExtra(Intents.INTENT_CONTACT_ID, mContactId.toString());
            }
            if (mGroupId != null) {
                intent.putExtra(Intents.INTENT_GROUP_ID, mGroupId.toString());
            }
            intent.putExtra(Intents.INTENT_DESCRIPTOR_ID, mSelectedItem.getDescriptorId().toString());
            intent.putExtra(Intents.INTENT_IS_PEER_ITEM, mSelectedItem.isPeerItem());

            if (mSelectedItem.getType() == Item.ItemType.PEER_CLEAR) {
                intent.putExtra(Intents.INTENT_RESET_CONVERSATION_NAME, ((PeerClearItem) mSelectedItem).getName());
            }
            startActivity(intent);
            closeMenu();
        }
    }

    public void onSaveItemClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveItemClick");
        }

        if (mSelectedItem != null) {
            if (mSelectedItem.isClearLocalItem()) {
                Toast.makeText(this, R.string.conversation_activity_local_cleanup, Toast.LENGTH_SHORT).show();
                return;
            } else if (mSelectedItem.getState() == Item.ItemState.DELETED || (mSelectedItem.isPeerItem() && (!mSelectedItem.getCopyAllowed() || mSelectedItem.isEphemeralItem()))) {
                Toast.makeText(this, R.string.conversation_activity_menu_item_view_operation_not_allowed, Toast.LENGTH_SHORT).show();
                return;
            } else if (!mSelectedItem.isAvailableItem()) {
                return;
            }

            switch (mSelectedItem.getType()) {
                case IMAGE:
                case PEER_IMAGE:
                case VIDEO:
                case PEER_VIDEO: {
                    Permission[] permissions = new Permission[]{Permission.WRITE_EXTERNAL_STORAGE};
                    mDeferredSaveMedia = true;
                    if (checkPermissions(permissions)) {
                        mDeferredSaveMedia = false;
                        saveMediaInGallery();
                    }
                    break;
                }
                case AUDIO:
                case PEER_AUDIO:
                case FILE:
                case PEER_FILE: {
                    saveFile();
                    break;
                }
            }
        }
    }

    public void onShareItemClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onShareItemClick");
        }

        if (mSelectedItem != null) {
            if (mSelectedItem.isClearLocalItem()) {
                Toast.makeText(this, R.string.conversation_activity_local_cleanup, Toast.LENGTH_SHORT).show();
                return;
            } else if (mSelectedItem.getState() == Item.ItemState.DELETED || (mSelectedItem.isPeerItem() && (!mSelectedItem.getCopyAllowed() || mSelectedItem.isEphemeralItem()))) {
                Toast.makeText(this, R.string.conversation_activity_menu_item_view_operation_not_allowed, Toast.LENGTH_SHORT).show();
                return;
            } else if (!mSelectedItem.isAvailableItem()) {
                return;
            }

            String content = "";
            String path = "";
            switch (mSelectedItem.getType()) {
                case MESSAGE: {
                    MessageItem messageItem = (MessageItem) mSelectedItem;
                    content = messageItem.getContent();
                    break;
                }
                case PEER_MESSAGE: {
                    PeerMessageItem peerMessageItem = (PeerMessageItem) mSelectedItem;
                    content = peerMessageItem.getContent();
                    break;
                }
                case LINK: {
                    LinkItem linkItem = (LinkItem) mSelectedItem;
                    content = linkItem.getContent();
                    break;
                }
                case PEER_LINK: {
                    PeerLinkItem peerLinkItem = (PeerLinkItem) mSelectedItem;
                    content = peerLinkItem.getContent();
                    break;
                }
                case IMAGE:
                case PEER_IMAGE:
                case VIDEO:
                case PEER_VIDEO:
                case AUDIO:
                case PEER_AUDIO:
                case FILE:
                case PEER_FILE: {
                    path = mSelectedItem.getPath();
                }
            }

            closeMenu();

            Intent intent = new Intent(Intent.ACTION_SEND);

            if (!path.isEmpty()) {
                Uri uri = uriFromPath(path);
                FileInfo media = new FileInfo(getApplicationContext(), uri);
                intent.setType(media.getMimeType());
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            } else if (!content.isEmpty()) {
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, content);
            }

            startActivityForResult(Intent.createChooser(intent, getString(R.string.conversation_activity_menu_item_view_share_title)), RESULT_DID_SHARE_ACTION);
        }
    }

    public void onSelectMoreItemClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectMoreItemClick");
        }

        if (mSelectedItem != null) {
            mSelectedItem.setSelected(true);
            mSelectedItems.add(mSelectedItem);
        }

        closeMenu();

        if (mMenu != null) {
            MenuItem menuAudioItem = mMenu.findItem(R.id.audio_call_action);
            MenuItem menuVideoItem = mMenu.findItem(R.id.video_call_action);
            MenuItem menuCancelItem = mMenu.findItem(R.id.cancel_action);

            menuAudioItem.setVisible(false);
            menuVideoItem.setVisible(false);
            menuCancelItem.setVisible(true);
        }

        mSelectItemMode = true;
        mItemSelectedActionView.setVisibility(View.VISIBLE);

        setSelectedMode(Mode.DEFAULT);

        mEditToolbarView.setVisibility(View.GONE);
        mSendClickableView.setVisibility(View.GONE);
        mRecordAudioClickableView.setVisibility(View.GONE);
        mScrollIndicatorView.setVisibility(View.GONE);
        mItemSelectedActionView.updateSelectedItems(mSelectedItems.size());
        mItemListAdapter.notifyDataSetChanged();

        setStatusBarColor(Design.TOOLBAR_COLOR, Design.TOOLBAR_COLOR);
    }

    public void onAddReactionItemClick(UIReaction uiReaction) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddReactionItemClick: " + uiReaction);
        }

        if (mSelectedItem != null) {
            mConversationService.toggleAnnotation(mSelectedItem.getDescriptorId(), AnnotationType.LIKE, uiReaction.getReactionType().ordinal());

            closeMenu();
        }
    }

    //
    // Implement ConversationService.Observer methods
    //

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: contact=" + contact);
        }

        mContactAvatar = avatar;

        setTitle(contact.getName());
        mSubject = contact;

        if (mMenuItemView != null && mSubject.getPeerTwincodeOutbound() != null && !mSubject.getPeerTwincodeOutbound().isSigned()) {
            mMenuItemView.setCanEditMessage(false);
        }

        updateOptionsMenu();

        mAvatarView.setImage(this, null,
                new CircularImageDescriptor(mContactAvatar, 0.5f, 0.5f, 0.5f));

        if (contact.getCertificationLevel() == CertificationLevel.LEVEL_4) {
            mCertifiedView.setVisibility(View.VISIBLE);
        }

        // Now, make the send button effective.
        mSendButtonListener.reset();

        mConversationService.getIdentityImage(contact, (Bitmap identityAvatar) -> {
            mIdentityAvatar = identityAvatar;

            if (mIdentityAvatar == null) {
                mIdentityAvatar = getTwinmeApplication().getAnonymousAvatar();
            }
        });
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: contact=" + contact);
        }

        mContactAvatar = avatar;

        setTitle(contact.getName());
        mSubject = contact;

        updateOptionsMenu();

        mAvatarView.setImage(this, null,
                new CircularImageDescriptor(mContactAvatar, 0.5f, 0.5f, 0.5f));

        // Now, make the send button effective.
        mSendButtonListener.reset();

        mConversationService.getIdentityImage(contact, (Bitmap identityAvatar) -> {
            mIdentityAvatar = identityAvatar;

            if (mIdentityAvatar == null) {
                mIdentityAvatar = getTwinmeApplication().getAnonymousAvatar();
            }
        });
    }

    @Override
    public void onGetGroup(@NonNull Group group, @NonNull List<GroupMember> groupMembers,
                           @NonNull GroupConversation conversation, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup: group=" + group);
        }

        mContactAvatar = avatar;

        setTitle(group.getName());
        mSubject = group;
        for (GroupMember member : groupMembers) {
            mGroupMembers.put(member.getPeerTwincodeOutboundId(), member);
        }

        // Display the number of group members.
        showSubTitle();
        if (groupMembers.isEmpty()) {
            setSubTitle(getString(R.string.conversation_activity_group_one_member));
        } else {
            int count = groupMembers.size() + 1; // +1 for current user.
            setSubTitle(String.format(getString(R.string.conversation_activity_group_member_information), count));
        }

        if (!conversation.hasPermission(org.twinlife.twinlife.ConversationService.Permission.SEND_MESSAGE)) {
            mSendAllowed = false;
        } else {
            mSendAllowed = true;
        }

        mSendButtonListener.reset();
        updateGroupPermissions();

        if (mContactAvatar != null) {
            mAvatarView.setImage(this, null,
                    new CircularImageDescriptor(mContactAvatar, 0.5f, 0.5f, Design.LIGHT_CIRCULAR_SHADOW_DESCRIPTOR.imageWithShadowRadius));
        }

        if (mSubject != null && mSubject.getAvatarId() == null) {
            mNoAvatarView.setVisibility(View.VISIBLE);
        } else {
            mNoAvatarView.setVisibility(View.GONE);
        }

        updateOptionsMenu();

        mConversationService.getIdentityImage(group, (Bitmap identityAvatar) -> {
            mIdentityAvatar = identityAvatar;

            if (mIdentityAvatar == null) {
                mIdentityAvatar = getTwinmeApplication().getAnonymousAvatar();
            }
        });
    }

    @Override
    public void onGetGroupMembers(@NonNull List<GroupMember> groupMembers) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroupMembers groupMembers=" + groupMembers);
        }

        for (GroupMember member : groupMembers) {
            mGroupMembers.put(member.getPeerTwincodeOutboundId(), member);
        }
    }

    @Override
    public void onDeleteGroup(UUID groupId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteGroup groupId=" + groupId);
        }

        if (!groupId.equals(mGroupId)) {

            return;
        }
        mDeletedGroup = true;
        if (mResumed) {
            finish();
        }
    }

    @Override
    public void onJoinGroup(@NonNull GroupConversation groupConversation, @Nullable InvitationDescriptor descriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onJoinGroup groupConversation=" + groupConversation);
        }

        if (mGroupId != null) {
            mResumed = false;
            mConversationService.getGroup(mGroupId, getTwinmeApplication().displayCallsMode(), mDescriptorFilter);
        }
    }

    @Override
    public void onLeaveGroup(@NonNull GroupConversation groupConversation, @NonNull UUID memberTwincodeId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLeaveGroup group=" + groupConversation);
        }

        if (groupConversation.getState() == GroupConversation.State.LEAVING) {
            mDeletedGroup = true;
            if (mResumed) {
                finish();
            }
        } else {
            mConversationService.getGroup(mGroupId, getTwinmeApplication().displayCallsMode(), mDescriptorFilter);
        }
    }

    @Override
    public void onGetConversation(@NonNull Conversation conversation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetConversation: conversation=" + conversation);
        }

        if (mResumed) {
            mConversationService.setActiveConversation();
        }
        mConversation = conversation;
    }

    @Override
    public void onResetConversation(@NonNull Conversation conversation, @NonNull ClearMode clearMode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResetConversation: conversation=" + conversation);
        }

        if (clearMode == ClearMode.CLEAR_MEDIA) {
            mConversationService.clearMediaAndFile();
        } else {
            mItems.clear();
            mItemListAdapter.notifyDataSetChanged();
            mEmptyConversationView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onGetDescriptors(@NonNull List<Descriptor> descriptors) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDescriptors: descriptors=" + descriptors);
        }

        if (descriptors.isEmpty()) {
            mAllDescriptorsLoaded = true;
        }

        boolean firstObjects = mItems.isEmpty();
        int firstNewItem = mItems.size();

        mBatchUpdate = firstObjects;

        for (Descriptor descriptor : descriptors) {
            switch (descriptor.getType()) {
                case OBJECT_DESCRIPTOR:
                    ObjectDescriptor objectDescriptor = (ObjectDescriptor) descriptor;
                    addObjectDescriptor(objectDescriptor);
                    break;

                case IMAGE_DESCRIPTOR:
                    ImageDescriptor imageDescriptor = (ImageDescriptor) descriptor;
                    addImageDescriptor(imageDescriptor);
                    break;

                case AUDIO_DESCRIPTOR:
                    AudioDescriptor audioDescriptor = (AudioDescriptor) descriptor;
                    addAudioDescriptor(audioDescriptor);
                    break;

                case VIDEO_DESCRIPTOR:
                    VideoDescriptor videoDescriptor = (VideoDescriptor) descriptor;
                    addVideoDescriptor(videoDescriptor);
                    break;

                case NAMED_FILE_DESCRIPTOR:
                    NamedFileDescriptor namedFileDescriptor = (NamedFileDescriptor) descriptor;
                    addNamedFileDescriptor(namedFileDescriptor);
                    break;

                case INVITATION_DESCRIPTOR:
                    InvitationDescriptor invitationDescriptor = (InvitationDescriptor) descriptor;
                    addInvitationDescriptor(invitationDescriptor);
                    break;

                case CALL_DESCRIPTOR:
                    CallDescriptor callDescriptor = (CallDescriptor) descriptor;
                    addCallDescriptor(callDescriptor);
                    break;

                case TWINCODE_DESCRIPTOR:
                    TwincodeDescriptor twincodeDescriptor = (TwincodeDescriptor) descriptor;
                    addTwincodeDescriptor(twincodeDescriptor);
                    break;

                case CLEAR_DESCRIPTOR:
                    ClearDescriptor clearDescriptor = (ClearDescriptor) descriptor;
                    addClearDescriptor(clearDescriptor);
                    break;

                default:
                    break;
            }
        }

        if (mItems.isEmpty()) {
            mEmptyConversationView.setVisibility(View.VISIBLE);
        }

        if (mBatchUpdate) {
            mBatchUpdate = false;

            if (mUIInitialized && !descriptors.isEmpty()) {
                mItemListAdapter.notifyItemRangeInserted(firstNewItem, descriptors.size());
            }
        }

        if (firstObjects) {
            scrollToBottom();
        }

        mLoadingDescriptors = false;

        if (mReplyToDescriptorId != null) {
            onReplyClick(mReplyToDescriptorId);
        } else if (mDescriptorId != null) {
            int itemIndex = -1;
            for (int index = mItems.size() - 1; index >= 0; index--) {
                Item item = mItems.get(index);
                if (item.getDescriptorId().equals(mDescriptorId)) {
                    itemIndex = index;
                    break;
                }
            }

            if (itemIndex != -1) {
                mDescriptorId = null;
                Handler handler = new Handler();
                int finalItemIndex = itemIndex;
                handler.postDelayed(() -> {
                    // Scroll only if the view is still valid.
                    if (mItemListView != null) {
                        mItemListView.scrollToPosition(finalItemIndex);
                    }
                }, 300);

            } else if (!mAllDescriptorsLoaded) {
                mLoadingDescriptors = true;
                mConversationService.getPreviousObjectDescriptors();
            }
        }
    }

    @Override
    public void onPushDescriptor(@NonNull Descriptor descriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPushDescriptor: descriptor=" + descriptor);
        }

        switch (descriptor.getType()) {
            case OBJECT_DESCRIPTOR:
                addObjectDescriptor((ObjectDescriptor) descriptor);
                scrollToBottom();
                break;

            case IMAGE_DESCRIPTOR:
                addImageDescriptor((ImageDescriptor) descriptor);
                scrollToBottom();
                break;

            case AUDIO_DESCRIPTOR:
                addAudioDescriptor((AudioDescriptor) descriptor);
                scrollToBottom();
                break;

            case VIDEO_DESCRIPTOR:
                addVideoDescriptor((VideoDescriptor) descriptor);
                scrollToBottom();
                break;

            case NAMED_FILE_DESCRIPTOR:
                addNamedFileDescriptor((NamedFileDescriptor) descriptor);
                scrollToBottom();
                break;

            case TWINCODE_DESCRIPTOR:
                addTwincodeDescriptor((TwincodeDescriptor) descriptor);
                scrollToBottom();
                break;

            case CLEAR_DESCRIPTOR:
                addClearDescriptor((ClearDescriptor) descriptor);
                scrollToBottom();
                break;

            default:
                break;
        }
    }

    @Override
    public void onPopDescriptor(@NonNull Descriptor descriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPopDescriptor: descriptor=" + descriptor);
        }

        int countItem = mItems.size();

        boolean lastItemVisible = isLastItemVisible();

        switch (descriptor.getType()) {
            case OBJECT_DESCRIPTOR:
                addObjectDescriptor((ObjectDescriptor) descriptor);
                break;

            case IMAGE_DESCRIPTOR:
                addImageDescriptor((ImageDescriptor) descriptor);
                break;

            case NAMED_FILE_DESCRIPTOR:
                addNamedFileDescriptor((NamedFileDescriptor) descriptor);
                break;

            case VIDEO_DESCRIPTOR:
                addVideoDescriptor((VideoDescriptor) descriptor);
                break;

            case INVITATION_DESCRIPTOR:
                addInvitationDescriptor((InvitationDescriptor) descriptor);
                break;

            case TRANSIENT_OBJECT_DESCRIPTOR:
                if (getTypingAction(descriptor) == Typing.Action.START && !lastItemVisible) {
                    return;
                }
                addTransientObjectDescriptor((TransientObjectDescriptor) descriptor);
                break;

            case CALL_DESCRIPTOR:
                addCallDescriptor((CallDescriptor) descriptor);
                break;

            case TWINCODE_DESCRIPTOR:
                addTwincodeDescriptor((TwincodeDescriptor) descriptor);
                break;

            case CLEAR_DESCRIPTOR:
                addClearDescriptor((ClearDescriptor) descriptor);
                break;

            default:
                break;
        }

        if (mItems.size() > countItem || descriptor.getType() == Descriptor.Type.TRANSIENT_OBJECT_DESCRIPTOR) {

            if (shouldRunHapticFeedback(descriptor)) {
                hapticFeedback();
            }
            if (!lastItemVisible && descriptor.getType() != Descriptor.Type.TRANSIENT_OBJECT_DESCRIPTOR) {
                mScrollIndicatorCount++;
                updateScrollIndicator();
            } else if (getTypingAction(descriptor) == null || lastItemVisible) {
                scrollToBottom();
            }
        }
    }

    private boolean isLastItemVisible() {
        // item position can be greater than mItems.size() if it's a typing indicator.
        return mItemListViewLayoutManager != null && mItemListViewLayoutManager.findLastVisibleItemPosition() >= mItems.size();
    }

    @Nullable
    private Typing.Action getTypingAction(@NonNull Descriptor descriptor) {
        if (!(descriptor instanceof TransientObjectDescriptor)) {
            return null;
        }

        if (!(((TransientObjectDescriptor) descriptor).getObject() instanceof Typing)) {
            return null;
        }

        return ((Typing) ((TransientObjectDescriptor) descriptor).getObject()).getAction();
    }

    private boolean shouldRunHapticFeedback(@NonNull Descriptor descriptor) {
        if (descriptor.getType() == Descriptor.Type.TRANSIENT_OBJECT_DESCRIPTOR) {
            Object object = ((TransientObjectDescriptor) descriptor).getObject();

            if (object instanceof Typing) {
                Typing typing = (Typing) object;
                if (typing.getAction() == Typing.Action.STOP) {
                    // No haptic feedback when typing stops.
                    return false;
                }

                // No haptic feedback if typing indicator is not visible.
                return isLastItemVisible();
            }
        }

        return true;
    }

    @Override
    public void onUpdateDescriptor(@NonNull Descriptor descriptor, UpdateType updateType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateDescriptor: descriptor=" + descriptor + " updateType=" + updateType);
        }

        int itemIndex = -1;
        switch (updateType) {
            case CONTENT:
                switch (descriptor.getType()) {
                    case IMAGE_DESCRIPTOR:
                        for (int index = mItems.size() - 1; index >= 0; index--) {
                            Item item = mItems.get(index);
                            if (item.getDescriptorId().equals(descriptor.getDescriptorId())) {
                                itemIndex = index;
                                break;
                            }
                        }

                        if (itemIndex == -1) {
                            addImageDescriptor((ImageDescriptor) descriptor);
                        } else {
                            Item item = mItems.get(itemIndex);

                            // Don't create another ImageItem if the descriptor object was not changed
                            // If we do it, this creates perf and display issues on the image item because we
                            // receive a lot of onUpdateDescriptor event.
                            if (!item.isSameObject(descriptor)) {
                                if (item.getType() == Item.ItemType.PEER_IMAGE) {
                                    PeerImageItem peerImageItem = new PeerImageItem((ImageDescriptor) descriptor, descriptor.getReplyToDescriptor());
                                    mItems.set(itemIndex, peerImageItem);
                                } else {
                                    ImageItem imageItem = new ImageItem((ImageDescriptor) descriptor, descriptor.getReplyToDescriptor());
                                    mItems.set(itemIndex, imageItem);
                                }
                            }
                            if (mUIInitialized) {
                                mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(itemIndex));
                            }
                        }
                        break;

                    case AUDIO_DESCRIPTOR:
                        AudioDescriptor audioDescriptor = (AudioDescriptor) descriptor;
                        if (audioDescriptor.isAvailable()) {
                            addAudioDescriptor(audioDescriptor);
                            scrollToBottom();
                        }
                        break;

                    case VIDEO_DESCRIPTOR:
                        for (int index = mItems.size() - 1; index >= 0; index--) {
                            Item item = mItems.get(index);
                            if (item.getDescriptorId().equals(descriptor.getDescriptorId())) {
                                itemIndex = index;
                                break;
                            }
                        }

                        if (itemIndex == -1) {
                            addVideoDescriptor((VideoDescriptor) descriptor);
                        } else {
                            Item item = mItems.get(itemIndex);

                            // Don't create another VideoItem if the descriptor object was not changed.
                            if (!item.isSameObject(descriptor)) {
                                if (item.getType() == Item.ItemType.PEER_VIDEO) {
                                    PeerVideoItem peerVideoItem = new PeerVideoItem((VideoDescriptor) descriptor, descriptor.getReplyToDescriptor());
                                    mItems.set(itemIndex, peerVideoItem);
                                } else {
                                    VideoItem videoItem = new VideoItem((VideoDescriptor) descriptor, descriptor.getReplyToDescriptor());
                                    mItems.set(itemIndex, videoItem);
                                }
                            }
                            if (mUIInitialized) {
                                mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(itemIndex));
                            }
                        }
                        break;

                    case NAMED_FILE_DESCRIPTOR:
                        for (int index = mItems.size() - 1; index >= 0; index--) {
                            Item item = mItems.get(index);
                            if (item.getDescriptorId().equals(descriptor.getDescriptorId())) {
                                itemIndex = index;
                                break;
                            }
                        }

                        if (itemIndex == -1) {
                            addNamedFileDescriptor((NamedFileDescriptor) descriptor);
                        } else {
                            Item item = mItems.get(itemIndex);

                            // Don't create another FileItem if the descriptor object was not changed.
                            if (!item.isSameObject(descriptor)) {
                                if (item.getType() == Item.ItemType.PEER_FILE) {
                                    PeerFileItem peerFileItem = new PeerFileItem((NamedFileDescriptor) descriptor, descriptor.getReplyToDescriptor());
                                    mItems.set(itemIndex, peerFileItem);
                                } else {
                                    FileItem fileItem = new FileItem((NamedFileDescriptor) descriptor, descriptor.getReplyToDescriptor());
                                    mItems.set(itemIndex, fileItem);
                                }
                            }
                            if (mUIInitialized) {
                                mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(itemIndex));
                            }
                        }
                        break;

                    case CALL_DESCRIPTOR:
                        for (int index = mItems.size() - 1; index >= 0; index--) {
                            Item item = mItems.get(index);
                            if (item.getDescriptorId().equals(descriptor.getDescriptorId())) {
                                itemIndex = index;
                                break;
                            }
                        }

                        if (itemIndex == -1) {
                            addCallDescriptor((CallDescriptor) descriptor);
                        } else {
                            Item item = mItems.get(itemIndex);

                            if (item.getType() == Item.ItemType.PEER_CALL) {
                                PeerCallItem peerCallItem = new PeerCallItem((CallDescriptor) descriptor);
                                mItems.set(itemIndex, peerCallItem);
                            } else {
                                CallItem callItem = new CallItem((CallDescriptor) descriptor);
                                mItems.set(itemIndex, callItem);
                            }
                            if (mUIInitialized) {
                                mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(itemIndex));
                            }
                        }
                        break;

                    case OBJECT_DESCRIPTOR:
                        for (int index = mItems.size() - 1; index >= 0; index--) {
                            Item item = mItems.get(index);
                            if (item.getDescriptorId().equals(descriptor.getDescriptorId())) {
                                itemIndex = index;
                                break;
                            }
                        }

                        if (itemIndex == -1) {
                            addObjectDescriptor((ObjectDescriptor) descriptor);
                        } else {
                            Item item = mItems.get(itemIndex);

                            // Don't create another MessageItem if the descriptor object was not changed.
                            if (!item.isSameObject(descriptor)) {
                                if (item.getType() == Item.ItemType.PEER_MESSAGE) {
                                    PeerMessageItem peerMessageItem = new PeerMessageItem((ObjectDescriptor) descriptor, descriptor.getReplyToDescriptor());
                                    mItems.set(itemIndex, peerMessageItem);
                                } else {
                                    MessageItem fileItem = new MessageItem((ObjectDescriptor) descriptor, descriptor.getReplyToDescriptor());
                                    mItems.set(itemIndex, fileItem);
                                }
                            }
                            if (mUIInitialized) {
                                mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(itemIndex));
                            }
                        }
                        break;

                    default:
                        break;
                }
                break;

            case TIMESTAMPS:
                int lastReadPeerItemIndex = -1;
                int updatedItemIndex = -1;
                for (int index = mItems.size() - 1; index >= 0; index--) {
                    Item item = mItems.get(index);
                    if (item == mLastReadPeerItem) {
                        lastReadPeerItemIndex = index;
                    }
                    if (item.getDescriptorId().equals(descriptor.getDescriptorId())) {
                        updatedItemIndex = index;
                        break;
                    }
                }
                if (updatedItemIndex != -1) {
                    Item updatedItem = mItems.get(updatedItemIndex);
                    updatedItem.updateTimestamps(descriptor);

                    if (!updatedItem.isPeerItem() && updatedItem.getReadTimestamp() != 0 && updatedItem.getReadTimestamp() != -1) {
                        Item lastReadPeerItem = mLastReadPeerItem;
                        long lastReadPeerItemTimestamp = lastReadPeerItem != null ? lastReadPeerItem.getTimestamp() : -1;
                        if (updatedItem.getTimestamp() < lastReadPeerItemTimestamp) {
                            updatedItem.resetState();
                        } else if (updatedItem.getTimestamp() > lastReadPeerItemTimestamp) {
                            mLastReadPeerItem = updatedItem;
                            if (lastReadPeerItem != null) {
                                lastReadPeerItem.resetState();
                                if (lastReadPeerItemIndex == -1) {
                                    for (int index = updatedItemIndex - 1; index >= 0; index--) {
                                        Item item = mItems.get(index);
                                        if (item == lastReadPeerItem) {
                                            lastReadPeerItemIndex = index;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        lastReadPeerItemIndex = -1;
                    }

                    // If this descriptor is now expired because we have received the READ timestamp from the peer
                    // and the timeout has ellapsed, we can remove it.
                    if (descriptor.isExpired()) {
                        deleteItem(updatedItem, updatedItemIndex);
                        updatedItemIndex = -1;
                    }
                }

                if (mUIInitialized) {
                    if (lastReadPeerItemIndex != -1) {
                        mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(lastReadPeerItemIndex));
                    }
                    if (updatedItemIndex != -1) {
                        mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(updatedItemIndex));
                    }
                }
                break;

            case LOCAL_ANNOTATIONS:
            case PEER_ANNOTATIONS:

                int annotationItemIndex = -1;
                for (int index = mItems.size() - 1; index >= 0; index--) {
                    Item item = mItems.get(index);
                    if (item.getDescriptorId().equals(descriptor.getDescriptorId())) {
                        annotationItemIndex = index;
                        break;
                    }
                }

                if (annotationItemIndex != -1) {
                    Item updatedItem = mItems.get(annotationItemIndex);
                    updatedItem.updateAnnotations(descriptor);

                    if (mUIInitialized) {
                        mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(annotationItemIndex));

                        if (annotationItemIndex == mItems.size() - 1) {
                            scrollToBottom();
                        }
                    }
                }

                break;
        }
    }

    @Override
    public void onMarkDescriptorRead(@NonNull Descriptor descriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMarkDescriptorRead: descriptor=" + descriptor);
        }

        for (int index = mItems.size() - 1; index >= 0; index--) {
            Item item = mItems.get(index);
            if (item.getDescriptorId().equals(descriptor.getDescriptorId())) {
                item.updateTimestamps(descriptor);

                mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(index));
                break;
            }
        }
    }

    @Override
    public void onMarkDescriptorDeleted(@NonNull Descriptor descriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMarkDescriptorDeleted: descriptor=" + descriptor);
        }

        for (int index = mItems.size() - 1; index >= 0; index--) {
            Item item = mItems.get(index);
            if (item.getDescriptorId().equals(descriptor.getDescriptorId())) {
                item.updateTimestamps(descriptor);

                mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(index));
                break;
            }
        }
    }

    @Override
    public void onGetConversationImage(@NonNull ExportedImageId imageId, @NonNull Bitmap bitmap) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetConversationImage: imageId=" + imageId);
        }
    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }

        onError(null, getString(R.string.application_contact_not_found), this::finish);
    }

    @Override
    public void onError(BaseService.ErrorCode errorCode, @Nullable String message, @Nullable Runnable errorCallback) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError: errorCode=" + errorCode + " message=" + message + " errorCallback=" + errorCallback);
        }

        super.onError(errorCode, message, () -> { /* do nothing */ });
    }

    @Override
    public void onErrorFeatureNotSupportedByPeer() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onErrorFeatureNotSupportedByPeer");
        }

        error(getString(R.string.conversation_activity_feature_not_supported_by_peer), null);
    }

    @Override
    public void onErrorNoPermission() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onErrorNoPermission");
        }

        error(getString(R.string.conversation_activity_group_not_allowed_post_message), null);
    }

    @Override
    public void onDeleteDescriptors(@NonNull Set<DescriptorId> descriptorIdSet) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteDescriptor: descriptorIdSet=" + descriptorIdSet.size());
        }

        for (int index = mItems.size() - 1; index >= 0; index--) {
            Item lItem = mItems.get(index);
            DescriptorId descriptorId = lItem.getDescriptorId();
            if (descriptorIdSet.remove(descriptorId)) {
                if (!lItem.isPeerItem() && lItem.getSentTimestamp() > 0) {
                    if (lItem.getType() == Item.ItemType.CALL) {
                        lItem.setState(Item.ItemState.BOTH_DELETED);
                    } else {
                        lItem.updateState();
                    }
                    mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(index));
                } else {
                    deleteItem(lItem, index);
                }

                if (descriptorIdSet.isEmpty()) {
                    return;
                }
            }
        }
    }

    @Override
    public void onLoaded(@NonNull List<Item> list) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLoaded");
        }

        Set<Item> toUpdate = new HashSet<>(list);

        int lastVisibleItemPosition = mItemListViewLayoutManager.findLastVisibleItemPosition();

        for (int index = mItems.size() - 1; index >= 0; index--) {
            Item lItem = mItems.get(index);
            if (toUpdate.remove(lItem)) {
                mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(index));

                if (toUpdate.isEmpty()) {
                    break;
                }
            }
        }

        if (lastVisibleItemPosition == mItems.size()) {
            // Scroll only if the view is still valid.
            if (mItemListView != null) {
                mItemListViewLayoutManager.scrollToPosition(lastVisibleItemPosition);
            }
        }
    }

    @Override
    public void addLoadableItem(@NonNull final org.twinlife.twinme.utils.async.Loader<Item> item) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addLoadableItem: item=" + item);
        }

        mAsyncItemLoader.addItem(item);
    }

    public void deleteItem(@NonNull DescriptorId descriptorId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "deleteItem: descriptorId=" + descriptorId);
        }

        if (mSelectedItem != null && mSelectedItem.getDescriptorId() == descriptorId && mIsMenuOpen) {
            mMenuItemView.post(this::closeMenu);
        }

        for (int index = mItems.size() - 1; index >= 0; index--) {
            Item lItem = mItems.get(index);
            if (lItem.getDescriptorId().equals(descriptorId)) {
                this.deleteItem(lItem, index);
                break;
            }
        }
    }

    //
    // Implements AnnotationsView observer methods
    //

    @Override
    public void onCloseAnnotationsAnimationEnd() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseAnnotationsAnimationEnd");
        }

        mAnnotationsView.setVisibility(View.INVISIBLE);
        mOverlayView.setVisibility(View.INVISIBLE);
        setStatusBarColor();
    }

    //
    // Implement BaseItemActivity.AudioItemObserver methods
    //

    @Override
    public void onStartPlaying(@NonNull AudioItemViewHolder audioItemViewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStartPlaying: audioItemViewHolder=" + audioItemViewHolder);
        }

        if ((mPlayingAudioItemViewHolder != null)) {
            if (mPlayingAudioItemViewHolder.getItemSequenceId() != audioItemViewHolder.getItemSequenceId()) {
                mPlayingAudioItemViewHolder.resetView();
            }
        }
        if ((mPlayingPeerAudioItemViewHolder != null)) {
            mPlayingPeerAudioItemViewHolder.resetView();
        }
        mPlayingAudioItemViewHolder = audioItemViewHolder;
    }

    @Override
    public void onStartPlaying(@NonNull PeerAudioItemViewHolder peerAudioItemViewHolder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStartPlaying: peerAudioItemViewHolder=" + peerAudioItemViewHolder);
        }

        if (peerAudioItemViewHolder.getItem().needsUpdateReadTimestamp()) {
            markDescriptorRead(peerAudioItemViewHolder.getItem().getDescriptorId());
        }

        if ((mPlayingPeerAudioItemViewHolder != null)) {
            if (mPlayingPeerAudioItemViewHolder.getItemSequenceId() != peerAudioItemViewHolder.getItemSequenceId()) {
                mPlayingPeerAudioItemViewHolder.resetView();
            }
        }
        if ((mPlayingAudioItemViewHolder != null)) {
            mPlayingAudioItemViewHolder.resetView();
        }
        mPlayingPeerAudioItemViewHolder = peerAudioItemViewHolder;
    }

    public void showProgressBar(boolean visible) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showProgressBar: " + visible);
        }

        mOverlayView.bringToFront();
        mProgressBarView.bringToFront();

        if (visible) {
            mOverlayView.setVisibility(View.VISIBLE);
            mProgressBarView.setVisibility(View.VISIBLE);

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color,  ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.POPUP_BACKGROUND_COLOR));
        } else {
            mOverlayView.setVisibility(View.GONE);
            mProgressBarView.setVisibility(View.GONE);
            setStatusBarColor();
        }
    }

    public void onSendVoiceRecord(@NotNull File recording) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendVoiceRecord: recording=" + recording);
        }

        sendFile(Uri.fromFile(recording), recording.getName(), Descriptor.Type.AUDIO_DESCRIPTOR, true, getTwinmeApplication().fileCopyAllowed(), 0);

        setSelectedMode(Mode.DEFAULT);
    }

    public void onSendVoiceRecordLongPress() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendVoiceRecordLongPress");
        }

        if (isRecording()) {
            //TODO REC
            onSendLongClick();
        }
    }

    public void onStartRecording() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStartRecording");
        }

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void onDeleteRecording() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteRecording");
        }

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setSelectedMode(Mode.DEFAULT);
    }

    private boolean isRecording() {
        return mVoiceRecorderMessageView != null && mVoiceRecorderMessageView.isRecording();
    }

    //
    // Implement ItemSelectedActionView.Observer methods
    //

    @Override
    public void onShareActionClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onShareActionClick");
        }

        StringBuilder stringBuilder = new StringBuilder();

        ArrayList<Uri> uriToShare = new ArrayList<>();

        for (Item item : mSelectedItems) {
            if (isShareItem(item)) {
                switch (item.getType()) {
                    case MESSAGE: {
                        MessageItem messageItem = (MessageItem) item;
                        if (!stringBuilder.toString().isEmpty()) {
                            stringBuilder.append("\n");
                        }
                        stringBuilder.append(messageItem.getContent());
                        break;
                    }
                    case PEER_MESSAGE: {
                        PeerMessageItem peerMessageItem = (PeerMessageItem) item;
                        if (!stringBuilder.toString().isEmpty()) {
                            stringBuilder.append("\n");
                        }
                        stringBuilder.append(peerMessageItem.getContent());
                        break;
                    }

                    case LINK: {
                        LinkItem linkItem = (LinkItem) item;
                        if (!stringBuilder.toString().isEmpty()) {
                            stringBuilder.append("\n");
                        }
                        stringBuilder.append(linkItem.getUrl().toString());
                        break;
                    }
                    case PEER_LINK: {
                        PeerLinkItem peerLinkItem = (PeerLinkItem) item;
                        if (!stringBuilder.toString().isEmpty()) {
                            stringBuilder.append("\n");
                        }
                        stringBuilder.append(peerLinkItem.getUrl().toString());
                        break;
                    }
                    case IMAGE:
                    case PEER_IMAGE:
                    case VIDEO:
                    case PEER_VIDEO:
                    case FILE:
                    case PEER_FILE: {
                        uriToShare.add(uriFromPath(item.getPath()));
                    }
                }
            }
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        if (!uriToShare.isEmpty()) {
            FileInfo media = new FileInfo(getApplicationContext(), uriToShare.get(0));
            intent.setType(media.getMimeType());
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriToShare);
        }

        if (!stringBuilder.toString().isEmpty()) {
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());
        }

        startActivityForResult(Intent.createChooser(intent, getString(R.string.conversation_activity_menu_item_view_share_title)), RESULT_DID_SHARE_ACTION);
        onCancelSelectItemModeClick();
    }

    @Override
    public void onDeleteActionClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteActionClick");
        }

        ViewGroup viewGroup = findViewById(R.id.conversation_activity_layout);

        DeleteConfirmView deleteConfirmView = new DeleteConfirmView(this, null);
        deleteConfirmView.setAvatar(mContactAvatar, mContactAvatar == null || mContactAvatar.equals(getTwinmeApplication().getDefaultGroupAvatar()));
        deleteConfirmView.setMessage(getString(R.string.cleanup_activity_delete_confirmation_message));

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                deleteItems();
                deleteConfirmView.animationCloseConfirmView();
                onCancelSelectItemModeClick();
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

    //
    // Private methods
    //

    @SuppressLint("DefaultLocale")
    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mMessageFont = Design.FONT_REGULAR32;

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.conversation_activity);
        setStatusBarColor();
        setTitle("");
        setToolBar(R.id.conversation_activity_top_tool_bar);
        showToolBar(true);
        showBackButton(true);

        applyInsets(R.id.conversation_activity_layout, R.id.conversation_activity_top_tool_bar, R.id.conversation_activity_footer, Design.TOOLBAR_COLOR, false);

        mNoAvatarView = findViewById(R.id.toolbar_no_image);
        mNoAvatarView.setColor(Design.GREY_ITEM_COLOR);
        mNoAvatarView.setVisibility(View.GONE);

        mAvatarView = findViewById(R.id.toolbar_image);
        LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.height = AVATAR_VIEW_HEIGHT;
        //noinspection SuspiciousNameCombination
        layoutParams.width = AVATAR_VIEW_HEIGHT;
        mAvatarView.setLayoutParams(layoutParams);
        mNoAvatarView.setLayoutParams(layoutParams);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mAvatarView.getLayoutParams();
        if (CommonUtils.isLayoutDirectionRTL()) {
            marginLayoutParams.leftMargin = AVATAR_MARGIN;
            marginLayoutParams.setMarginStart(AVATAR_MARGIN);
        } else {
            marginLayoutParams.rightMargin = AVATAR_MARGIN;
            marginLayoutParams.setMarginEnd(AVATAR_MARGIN);
        }

        mAvatarView.setLayoutParams(layoutParams);
        mNoAvatarView.setLayoutParams(layoutParams);

        mCertifiedView = findViewById(R.id.toolbar_certified_image);
        mCertifiedView.setVisibility(View.GONE);

        layoutParams = mCertifiedView.getLayoutParams();
        layoutParams.height = (int) (AVATAR_VIEW_HEIGHT * 0.5f);
        //noinspection SuspiciousNameCombination
        layoutParams.width = (int) (AVATAR_VIEW_HEIGHT * 0.5f);
        mCertifiedView.setLayoutParams(layoutParams);

        View identityClickableView = findViewById(R.id.toolbar_content_view);
        identityClickableView.setOnClickListener(view -> onTitleClick());

        mEmptyConversationView = findViewById(R.id.conversation_activity_empty_view);
        Design.updateTextFont(mEmptyConversationView, Design.FONT_REGULAR36);
        mEmptyConversationView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mItemListView = findViewById(R.id.conversation_activity_item_list_view);
        mItemListViewLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mItemListView.setLayoutManager(mItemListViewLayoutManager);
        mItemListAdapter = new ItemListAdapter(this, this, mItems);
        mItemListAdapter.setHasStableIds(true);
        mItemListView.setHasFixedSize(false);
        mItemListView.setAdapter(mItemListAdapter);
        mItemListView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mItemListView.setItemAnimator(null);
        mItemListView.setBackgroundColor(Design.CONVERSATION_BACKGROUND_COLOR);

        mItemListView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            int lastListSize = -1;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (mSelectItemMode) {
                    return;
                }

                updateScrollIndicator();

                if (mItemListViewLayoutManager.findFirstVisibleItemPosition() < 20) {
                    if (!mLoadingDescriptors && !mAllDescriptorsLoaded && lastListSize != mItems.size()) {
                        mLoadingDescriptors = true;
                        lastListSize = mItems.size();
                        mConversationService.getPreviousObjectDescriptors();
                    }
                }
            }
        });

        mContainerRecyclerView = findViewById(R.id.conversation_activity_item_list_container_view);
        mContainerRecyclerView.setBackgroundColor(Design.CONVERSATION_BACKGROUND_COLOR);

        mContainerRecyclerView.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {

            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                mContainerRecyclerView.post(this::scrollToBottom);
            }

            if (mIsMenuOpen) {
                setMenuPosition();
                mMenuItemView.post(mMenuItemView::animationMenu);
                mMenuReactionView.post(mMenuReactionView::animationMenu);
            }
        });

        ReplyItemTouchHelper.OnSwipeItemReplyListener onSwipeItemReplyListener = adapterPosition -> {
            mSelectedItem = mItems.get(adapterPosition - 1);

            hapticFeedback();
            onReplyItemClick();
        };
        ReplyItemTouchHelper swipeItemTouchHelper = new ReplyItemTouchHelper(mItemListView, onSwipeItemReplyListener);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeItemTouchHelper);
        itemTouchHelper.attachToRecyclerView(mItemListView);

        if (!mItems.isEmpty()) {
            mItemListAdapter.notifyDataSetChanged();
        }

        mFooterView = findViewById(R.id.conversation_activity_footer);
        mFooterView.setBackgroundColor(Design.WHITE_COLOR);

        mEditToolbarView = findViewById(R.id.conversation_activity_edit_toolbar);
        mEditToolbarView.setBackgroundColor(Design.WHITE_COLOR);

        mEditMessageView = findViewById(R.id.conversation_activity_toolbar_edit_message_view);
        mEditMessageView.setBackgroundColor(Color.TRANSPARENT);

        layoutParams = mEditMessageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_EDIT_MESSAGE_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mEditMessageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_EDIT_TEXT_WIDTH_INSET * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_EDIT_TEXT_WIDTH_INSET * Design.WIDTH_RATIO));

        ImageView editMessageImageView = findViewById(R.id.conversation_activity_toolbar_edit_message_image_view);
        editMessageImageView.setColorFilter(Design.FONT_COLOR_DEFAULT);

        layoutParams = editMessageImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_EDIT_MESSAGE_ICON_HEIGHT * Design.HEIGHT_RATIO);

        TextView editMessageTextView = findViewById(R.id.conversation_activity_toolbar_edit_message_text_view);
        Design.updateTextFont(editMessageTextView, Design.FONT_MEDIUM30);
        editMessageTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) editMessageTextView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_EDIT_MESSAGE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_EDIT_MESSAGE_MARGIN * Design.WIDTH_RATIO));

        View menuView = findViewById(R.id.conversation_activity_toolbar_menu_view);
        layoutParams = menuView.getLayoutParams();

        // Handle virtual keyboard
        layoutParams.width = (int) (DESIGN_EDITBAR_HEIGHT * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_EDITBAR_HEIGHT * Design.HEIGHT_RATIO);
        menuView.setLayoutParams(layoutParams);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) menuView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_TOOLBAR_ACTION_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_TOOLBAR_ACTION_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_EDIT_TEXT_TOP_MARGIN * Design.HEIGHT_RATIO);

        mMenuClickableView = findViewById(R.id.conversation_activity_toolbar_menu_clickable_view);
        mMenuClickableView.setOnClickListener(view -> onMenuClick());

        ImageView menuImageView = findViewById(R.id.conversation_activity_toolbar_menu_image_view);
        menuImageView.setColorFilter(Color.WHITE);
        layoutParams = menuImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_MENU_ICON_HEIGHT * Design.HEIGHT_RATIO);

        mEditCancelView = findViewById(R.id.conversation_activity_toolbar_cancel_edit_clickable_view);
        mEditCancelView.setOnClickListener(view -> onCloseEditViewClick());

        mRecordAudioClickableView = findViewById(R.id.conversation_activity_toolbar_audio_record_view);
        layoutParams = mRecordAudioClickableView.getLayoutParams();
        // Handle virtual keyboard
        layoutParams.height = (int) (DESIGN_EDITBAR_HEIGHT * Design.HEIGHT_RATIO);
        mRecordAudioClickableView.setLayoutParams(layoutParams);
        mRecordAudioClickableView.setOnClickListener(view -> onMicroClick());

        ImageView recordImageView = findViewById(R.id.conversation_activity_toolbar_audio_record_image_view);
        recordImageView.setColorFilter(Design.getMainStyle());
        layoutParams = recordImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_EDIT_TEXT_AUDIO_ICON_HEIGHT * Design.HEIGHT_RATIO);

        mCameraClickableView = findViewById(R.id.conversation_activity_toolbar_camera_view);
        layoutParams = mCameraClickableView.getLayoutParams();
        // Handle virtual keyboard
        layoutParams.height = (int) (DESIGN_EDITBAR_HEIGHT * Design.HEIGHT_RATIO);
        mCameraClickableView.setLayoutParams(layoutParams);
        mCameraClickableView.setOnClickListener(view -> onCameraPhotoClick());

        ImageView cameraImageView = findViewById(R.id.conversation_activity_toolbar_camera_image_view);
        cameraImageView.setColorFilter(Design.getMainStyle());
        layoutParams = cameraImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_EDIT_TEXT_CAMERA_ICON_HEIGHT * Design.HEIGHT_RATIO);

        mSendButtonListener = new SendListener();
        mSendClickableView = findViewById(R.id.conversation_activity_toolbar_send_clickable_view);
        layoutParams = mSendClickableView.getLayoutParams();
        // Handle virtual keyboard
        layoutParams.width = (int) (DESIGN_EDITBAR_HEIGHT * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_EDITBAR_HEIGHT * Design.HEIGHT_RATIO);
        mSendClickableView.setLayoutParams(layoutParams);
        mSendClickableView.setOnClickListener(mSendButtonListener);
        mSendClickableView.setOnLongClickListener(mSendButtonListener);

        mEditConfirmView = findViewById(R.id.conversation_activity_toolbar_confirm_edit_clickable_view);
        mEditConfirmView.setOnClickListener(view -> onConfirmEditViewClick());

        ImageView editItemImageView = findViewById(R.id.conversation_activity_toolbar_confirm_edit_image_view);
        layoutParams = editItemImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SEND_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        View rightView = findViewById(R.id.conversation_activity_toolbar_right_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) rightView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_TOOLBAR_ACTION_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_TOOLBAR_ACTION_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_EDIT_TEXT_TOP_MARGIN * Design.HEIGHT_RATIO);

        ImageView sendImageView = findViewById(R.id.conversation_activity_toolbar_send_image_view);
        layoutParams = sendImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SEND_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        mVoiceRecorderMessageView = findViewById(R.id.conversation_activity_voice_recorder_message_view);
        mVoiceRecorderMessageView.setVisibility(View.INVISIBLE);

        layoutParams = mVoiceRecorderMessageView.getLayoutParams();
        float voiceRecorderHeight = (DESIGN_EDITBAR_HEIGHT * Design.HEIGHT_RATIO) + (2 * (DESIGN_EDIT_TEXT_TOP_MARGIN * Design.HEIGHT_RATIO));
        layoutParams.height = (int) (voiceRecorderHeight);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mVoiceRecorderMessageView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_EDIT_TEXT_TOP_MARGIN * Design.HEIGHT_RATIO);

        View editTextConstraintView = findViewById(R.id.conversation_activity_edit_text_constraint_view);
        layoutParams = editTextConstraintView.getLayoutParams();
        // Handle virtual keyboard
        layoutParams.height = (int) (DESIGN_EDITBAR_HEIGHT * Design.HEIGHT_RATIO);
        editTextConstraintView.setLayoutParams(layoutParams);

        mEditText = findViewById(R.id.conversation_activity_edit_text);
        Design.updateTextFont(mEditText, Design.FONT_REGULAR32);
        mEditText.setOnTouchListener(new EditTextTouchListener());
        mEditText.setTextColor(Design.BLACK_COLOR);
        mEditText.setHintTextColor(Design.GREY_COLOR);

        ViewTreeObserver viewTreeObserver = mEditText.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver viewTreeObserver = mEditText.getViewTreeObserver();
                viewTreeObserver.removeOnGlobalLayoutListener(this);

                int editTextSize = mEditText.getHeight();

                int defaultHeight = (int) (DESIGN_EDITBAR_HEIGHT * Design.HEIGHT_RATIO);

                if (editTextSize > defaultHeight) {
                    editTextSize = defaultHeight;
                }

                float radius = editTextSize * 0.5f;
                float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

                boolean darkMode = false;
                int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                int displayMode = Settings.displayMode.getInt();
                if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
                    darkMode = true;
                }

                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setColor(Design.EDIT_TEXT_CONVERSATION_BACKGROUND_COLOR);
                gradientDrawable.setCornerRadius(radius);
                if (darkMode) {
                    gradientDrawable.setStroke(3, EDIT_TEXT_BORDER_COLOR);
                }

                mEditText.setBackground(gradientDrawable);

                if (TextUtils.isEmpty(mEditText.getText())) {
                    mEditText.setText(getTypedText());
                    if (mSendAllowed) {
                        updateSendButton();
                    }
                }

                mEditText.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                        if (mSendAllowed) {
                            String text = getSendText();

                            long now = System.currentTimeMillis();
                            if (!text.isEmpty() && (!mIsTyping || mTypingSendTime + TYPING_RESEND_DELAY < now)) {
                                mIsTyping = true;
                                mTypingSendTime = now;
                                Typing typing = new Typing(Typing.Action.START);
                                mConversationService.pushTyping(typing);
                            } else if (text.isEmpty() && mIsTyping) {
                                mIsTyping = false;
                                Typing typing = new Typing(Typing.Action.STOP);
                                mConversationService.pushTyping(typing);
                            }

                            if (text.contains(Utils.SMILEY_HAPPY) || text.contains(Utils.SMILEY_SAD)) {
                                int position = mEditText.getSelectionStart() - 1;
                                mEditText.removeTextChangedListener(this);
                                mEditText.setText(Utils.convertEmoji(text));
                                if (position > text.length() - 1) {
                                    position = text.length() - 1;
                                }
                                mEditText.setSelection(position);
                                mEditText.addTextChangedListener(this);
                            }

                            if (!text.isEmpty() && mIsTyping) {
                                initTypingTimer();
                            }

                            updateSendButton();
                        }
                    }
                });

                LayoutParams layoutParams  = mSendClickableView.getLayoutParams();
                layoutParams.height = editTextSize;
                layoutParams.width = editTextSize;

                View sendRoundedView = findViewById(R.id.conversation_activity_toolbar_send_rounded_view);
                layoutParams = sendRoundedView.getLayoutParams();
                layoutParams.height = editTextSize;
                layoutParams.width = editTextSize;
                sendRoundedView.setLayoutParams(layoutParams);

                ShapeDrawable sendBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
                sendBackground.getPaint().setColor(Design.getMainStyle());
                sendRoundedView.setBackground(sendBackground);

                View editConfirmRoundedView = findViewById(R.id.conversation_activity_toolbar_confirm_edit_rounded_view);
                layoutParams = editConfirmRoundedView.getLayoutParams();
                layoutParams.height = editTextSize;
                layoutParams.width = editTextSize;
                editConfirmRoundedView.setLayoutParams(layoutParams);

                ShapeDrawable editConfirmBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
                editConfirmBackground.getPaint().setColor(EDIT_ITEM_CONFIRM_COLOR);
                editConfirmRoundedView.setBackground(editConfirmBackground);

                layoutParams = mRecordAudioClickableView.getLayoutParams();
                layoutParams.height = editTextSize;
                layoutParams.width = editTextSize;

                View recordRoundedView = findViewById(R.id.conversation_activity_toolbar_audio_record_rounded_view);
                layoutParams = recordRoundedView.getLayoutParams();
                layoutParams.height = editTextSize;
                layoutParams.width = editTextSize;

                layoutParams  = mMenuClickableView.getLayoutParams();
                layoutParams.height = editTextSize;
                layoutParams.width = editTextSize;

                View menuRoundedView = findViewById(R.id.conversation_activity_toolbar_menu_rounded_view);
                layoutParams = menuRoundedView.getLayoutParams();
                layoutParams.height = editTextSize;
                layoutParams.width = editTextSize;
                menuRoundedView.setLayoutParams(layoutParams);

                ShapeDrawable menuBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
                menuBackground.getPaint().setColor(Design.getMainStyle());
                menuRoundedView.setBackground(menuBackground);

                ImageView cancelEditView = findViewById(R.id.conversation_activity_toolbar_cancel_edit_image_view);
                cancelEditView.setColorFilter(Design.BLACK_COLOR);

                layoutParams = cancelEditView.getLayoutParams();
                layoutParams.height = editTextSize;
                layoutParams.width = editTextSize;
                cancelEditView.setLayoutParams(layoutParams);

                layoutParams = mCameraClickableView.getLayoutParams();
                layoutParams.height = editTextSize;
                layoutParams.width = editTextSize;

                View cameraRoundedView = findViewById(R.id.conversation_activity_toolbar_camera_rounded_view);
                layoutParams = cameraRoundedView.getLayoutParams();
                layoutParams.height = editTextSize;
                layoutParams.width = editTextSize;

                layoutParams = mVoiceRecorderMessageView.getLayoutParams();
                layoutParams.height = editTextSize + getBarBottomInset();
                mVoiceRecorderMessageView.updateViews(editTextSize);

                layoutParams = mItemSelectedActionView.getLayoutParams();
                layoutParams.height = (int) (DESIGN_SELECTED_VIEW_HEIGHT * Design.HEIGHT_RATIO) + getBarBottomInset();

                mInitToolbar = true;

                if (mSharedText != null) {
                    mEditText.setText(mSharedText);
                    saveShortCutText(mSharedText.toString());
                    mSharedText = null;
                }
            }
        });

        mEditText.setPadding((int) (DESIGN_EDIT_TEXT_WIDTH_INSET * Design.WIDTH_RATIO), (int) (DESIGN_EDIT_TEXT_HEIGHT_INSET * Design.HEIGHT_RATIO), (int) (DESIGN_EDIT_TEXT_WIDTH_INSET * Design.WIDTH_RATIO), (int) (DESIGN_EDIT_TEXT_HEIGHT_INSET * Design.HEIGHT_RATIO));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mEditText.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_EDIT_TEXT_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_EDIT_TEXT_TOP_MARGIN * Design.HEIGHT_RATIO);
        mEditText.setLayoutParams(marginLayoutParams);

        mEditText.setKeyBoardInputCallbackListener((inputContentInfo, flags, opts) -> {
            final Uri uri = inputContentInfo.getContentUri();
            if (uri.getLastPathSegment() != null) {
                getTwinmeContext().execute(() -> {
                    final File contentFile = new File(getFilesDir(), uri.getLastPathSegment());
                    final BaseService.ErrorCode errorCode = Utils.copyUriToFile(getContentResolver(), uri, contentFile);
                    if (errorCode == BaseService.ErrorCode.SUCCESS) {
                        Intent intent = new Intent(this, PreviewFileActivity.class);

                        ArrayList<String> urisToString = new ArrayList<>();
                        urisToString.add(Uri.fromFile(contentFile).toString());
                        intent.putStringArrayListExtra(Intents.INTENT_SELECTED_URI, urisToString);
                        intent.putExtra(Intents.INTENT_ALLOW_COPY_FILE, getTwinmeApplication().fileCopyAllowed());

                        if (mSubject != null) {
                            intent.putExtra(Intents.INTENT_CONTACT_ID, mSubject.getId().toString());
                        }
                        if (mEditText.getText() != null) {
                            intent.putExtra(Intents.INTENT_TEXT_MESSAGE, mEditText.getText().toString());
                        }

                        startActivityForResult(intent, REQUEST_PREVIEW_MEDIA);
                    } else {
                        runOnUiThread(() -> onExecutionError(errorCode));
                    }
                });
            }
        });

        initTypingTimer();

        mZoomView = findViewById(R.id.conversation_activity_zoom_view);
        mZoomView.setVisibility(View.GONE);

        layoutParams = mZoomView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ZOOM_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mZoomView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_ZOOM_VIEW_MARGIN * Design.HEIGHT_RATIO);

        RoundedView roundedView = findViewById(R.id.conversation_activity_zoom_rounded_view);
        roundedView.setColor(Color.BLACK);
        roundedView.setAlpha(0.5f);

        mZoomTextView = findViewById(R.id.conversation_activity_zoom_text_view);
        Design.updateTextFont(mZoomTextView, Design.FONT_BOLD44);
        mZoomTextView.setTextColor(Design.ZOOM_COLOR);

        mHeaderOverlayView = findViewById(R.id.conversation_activity_header_overlay_view);
        mHeaderOverlayView.setBackgroundColor(Design.BACKGROUND_COLOR_WHITE_OPACITY85);
        mHeaderOverlayView.setOnClickListener(view -> closeMenu());
        
        mFooterOverlayView = findViewById(R.id.conversation_activity_footer_overlay_view);
        mFooterOverlayView.setBackgroundColor(Design.BACKGROUND_COLOR_WHITE_OPACITY85);
        mFooterOverlayView.setOnClickListener(view -> closeMenu());

        ViewGroup.LayoutParams  overlayLayoutParams = mFooterOverlayView.getLayoutParams();
        overlayLayoutParams.height = (int) (DESIGN_EDITBAR_HEIGHT * Design.HEIGHT_RATIO) + (int) (DESIGN_TOOLBAR_HEIGHT * Design.HEIGHT_RATIO);
        mFooterOverlayView.setLayoutParams(overlayLayoutParams);

        mMenuItemView = findViewById(R.id.conversation_activity_menu_item_view);
        layoutParams = mMenuItemView.getLayoutParams();
        layoutParams.width = MENU_WIDTH;
        mMenuItemView.setVisibility(View.INVISIBLE);

        mMenuReactionView = findViewById(R.id.conversation_activity_menu_reaction_view);
        layoutParams = mMenuReactionView.getLayoutParams();
        layoutParams.width = mMenuReactionView.getMenuWidth();
        layoutParams.height = MENU_REACTION_HEIGHT;
        mMenuReactionView.setVisibility(View.INVISIBLE);

        mReplyView = findViewById(R.id.conversation_activity_reply_view);
        layoutParams = mReplyView.getLayoutParams();
        layoutParams.height = REPLY_HEIGHT;
        mReplyView.setVisibility(View.GONE);

        mScrollIndicatorView = findViewById(R.id.conversation_activity_scroll_indicator_view);
        mScrollIndicatorView.setOnClickListener(view -> onScrollIndicatorClick());
        mScrollIndicatorView.setVisibility(View.GONE);

        mScrollIndicatorOverlayView = findViewById(R.id.conversation_activity_scroll_indicator_overlay_view);
        mScrollIndicatorOverlayView.setVisibility(View.GONE);

        float radius = DESIGN_SCROLL_INDICATOR_HEIGHT * Design.HEIGHT_RATIO * 0.5f * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{0, 0, radius, radius, radius, radius, 0, 0};

        if (CommonUtils.isLayoutDirectionRTL()) {
            outerRadii = new float[]{radius, radius, 0, 0, 0, 0, radius, radius};
        }

        ShapeDrawable scrollIndicatorBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorBackground.getPaint().setColor(Design.getMainStyle());
        mScrollIndicatorView.setBackground(scrollIndicatorBackground);

        ShapeDrawable scrollIndicatorOverlayBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        scrollIndicatorOverlayBackground.getPaint().setColor(Design.BACKGROUND_COLOR_WHITE_OPACITY85);
        mScrollIndicatorOverlayView.setBackground(scrollIndicatorOverlayBackground);

        layoutParams = mScrollIndicatorView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_SCROLL_INDICATOR_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_SCROLL_INDICATOR_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mScrollIndicatorView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_SCROLL_INDICATOR_BOTTOM * Design.HEIGHT_RATIO);

        mScrollIndicatorCountView = findViewById(R.id.conversation_activity_scroll_indicator_count_view);
        Design.updateTextFont(mScrollIndicatorCountView, Design.FONT_MEDIUM42);
        mScrollIndicatorCountView.setTextColor(Color.WHITE);

        ImageView scrollIndicatorImageView = findViewById(R.id.conversation_activity_scroll_indicator_image_view);

        layoutParams = scrollIndicatorImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SCROLL_INDICATOR_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) scrollIndicatorImageView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_SCROLL_INDICATOR_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginEnd((int) (DESIGN_SCROLL_INDICATOR_MARGIN * Design.WIDTH_RATIO));

        mOverlayView = findViewById(R.id.conversation_activity_overlay_view);
        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);
        mOverlayView.setOnClickListener(view -> closeMenu());

        mProgressBarView = findViewById(R.id.conversation_activity_progress_bar);

        mMenuSendOptionView = findViewById(R.id.conversation_activity_menu_send_option_view);
        mMenuSendOptionView.setVisibility(View.INVISIBLE);

        MenuSendOptionView.Observer menuSendOptionObserver = new MenuSendOptionView.Observer() {
            @Override
            public void onCloseMenuAnimationEnd() {

            }

            @Override
            public void onAllowEphemeralClick() {
                showPremiumFeatureView(UIPremiumFeature.FeatureType.PRIVACY, true);
            }

            @Override
            public void onSendFromMenuOptionClick(boolean allowCopy, boolean allowEphemeral, int timeout) {
                mAllowCopy = allowCopy;
                onSendClick();
            }
        };

        mMenuSendOptionView.setOnMenuSendOptionObserver(this, menuSendOptionObserver);

        mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());

        mItemListView.setOnTouchListener((view, motionEvent) -> {
            mScaleDetector.onTouchEvent(motionEvent);
            return false;
        });

        updateSendButton();

        mCoachMarkView = findViewById(R.id.conversation_activity_coach_mark_view);
        CoachMarkView.OnCoachMarkViewListener onCoachMarkViewListener = new CoachMarkView.OnCoachMarkViewListener() {
            @Override
            public void onCloseCoachMark() {

                mCoachMarkView.setVisibility(View.GONE);
            }

            @Override
            public void onTapCoachMarkFeature() {

            }

            @Override
            public void onLongPressCoachMarkFeature() {

                mCoachMarkView.setVisibility(View.GONE);
                getTwinmeApplication().hideCoachMark(CoachMark.CoachMarkTag.CONVERSATION_EPHEMERAL);
                onSendLongClick();
            }
        };

        mCoachMarkView.setOnCoachMarkViewListener(onCoachMarkViewListener);

        mItemSelectedActionView = findViewById(R.id.conversation_activity_item_selected_action_view);
        mItemSelectedActionView.setVisibility(View.GONE);
        mItemSelectedActionView.setObserver(this);

        layoutParams = mItemSelectedActionView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SELECTED_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        mAnnotationsView = findViewById(R.id.conversation_activity_annotations_view);
        mAnnotationsView.setVisibility(View.INVISIBLE);
        mAnnotationsView.setActivity(this);
        mAnnotationsView.setObserver(this);

        mTopBlurContainerView = findViewById(R.id.conversation_activity_top_blur_container_view);

        layoutParams = mTopBlurContainerView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_TOP_BLUR_CONTAINER_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        mTopBlurView = findViewById(R.id.conversation_activity_top_blur_view);

        layoutParams = mTopBlurView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_TOP_BLUR_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (DEBUG) {
                    Log.d(LOG_TAG, "handleOnBackPressed");
                }

                backPressed();
            }
        });

        mUIInitialized = true;

        if (mOpenItemIndex != -1) {
            mItemListView.scrollToPosition(mOpenItemIndex);
            mOpenItemIndex = -1;
        }
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
        mItemListViewHeight = mItemListView.getHeight();

        if (mOpenItemIndex != -1) {
            mItemListView.scrollToPosition(mOpenItemIndex);
            mOpenItemIndex = -1;
        }
    }

    private void scrollToBottom() {
        if (DEBUG) {
            Log.d(LOG_TAG, "scrollToBottom");
        }

        if (mUIInitialized) {

            mItemListView.scrollToPosition(mItemListAdapter.getItemCount() - 1);

            mScrollIndicatorCount = 0;
            updateScrollIndicator();
            mScrollIndicatorView.setVisibility(View.GONE);
        }
    }

    protected void onBackClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackClick");
        }

        clearConversationPlayingAudio();
        hideKeyboard();

        if (isEmptyText() && mIsTyping) {
            mIsTyping = false;
            Typing typing = new Typing(Typing.Action.STOP);
            mConversationService.pushTyping(typing);
        }

        finish();
    }

    private void onTitleClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTitleClick");
        }

        clearConversationPlayingAudio();
        hideKeyboard();

        if(mSubject == null || mSelectItemMode){
            return;
        }

        showContactActivity(mSubject);
    }

    private void onAudioClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAudioClick");
        }

        if (mMenu != null) {
            MenuItem menuAudioItem = mMenu.findItem(R.id.audio_call_action);
            if (getTwinmeApplication().inCallInfo() == null && menuAudioItem.isEnabled()) {
                if (mGroupId != null) {
                    showPremiumFeatureView(UIPremiumFeature.FeatureType.GROUP_CALL, false);
                } else {
                    Intent intent = new Intent();
                    if (mContactId != null) {
                        intent.putExtra(Intents.INTENT_CONTACT_ID, mContactId.toString());
                    }

                    intent.putExtra(Intents.INTENT_CALL_MODE, CallStatus.OUTGOING_CALL);

                    startActivity(CallActivity.class, intent);
                }
            }
        }
    }

    private void onVideoClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onVideoClick");
        }

        if (mMenu != null) {
            MenuItem menuVideoItem = mMenu.findItem(R.id.video_call_action);
            if (getTwinmeApplication().inCallInfo() == null && menuVideoItem.isEnabled()) {
                if (mGroupId != null) {
                    showPremiumFeatureView(UIPremiumFeature.FeatureType.GROUP_CALL, false);
                } else {
                    Intent intent = new Intent();

                    if (mContactId != null) {
                        intent.putExtra(Intents.INTENT_CONTACT_ID, mContactId.toString());
                    }

                    intent.putExtra(Intents.INTENT_CALL_MODE, CallStatus.OUTGOING_VIDEO_CALL);

                    startActivity(CallActivity.class, intent);
                }
            }
        }
    }

    private void onScrollIndicatorClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onScrollIndicatorClick");
        }

        if (mIsMenuOpen) {
            return;
        }

        hapticFeedback();
        scrollToBottom();
    }

    private void sendFile(@NonNull Uri file, @NonNull String filename, @NonNull Descriptor.Type type, boolean toDelete, boolean allowCopy, long expireTimeout) {
        if (DEBUG) {
            Log.d(LOG_TAG, "sendFile");
        }

        DescriptorId replyTo = null;
        if (mReplyItem != null) {
            replyTo = mReplyItem.getDescriptorId();
        }

        if (mConversation != null) {
            mConversationService.pushFile(file, filename, type, toDelete, allowCopy, null, replyTo, expireTimeout);
        }
    }

    @Override
    public void onSendFilesFinished() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendFilesFinished");
        }

        if (mDeferredMessage != null) {
            mConversationService.pushMessage(mDeferredMessage, mDeferredAllowCopyText, mDeferredTimeout, mDeferredReplyTo);
            mDeferredMessage = null;
            mDeferredAllowCopyText = false;
            mDeferredTimeout = 0;
            mDeferredReplyTo = null;
        }
    }

    public void onSendClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendClick");
        }

        hapticFeedback();

        if (!mSendAllowed) {
            toast(getString(R.string.conversation_activity_group_not_allowed_post_message));
            return;
        }

        boolean allowCopyText = getTwinmeApplication().messageCopyAllowed();
        if (mIsMenuSendOptionOpen) {
            allowCopyText = mAllowCopy;
        }
        long timeout = 0;

        String text = getSendText();
        if (!text.trim().isEmpty()) {
            mEditText.setText("");

            DescriptorId replyTo = null;
            if (mReplyItem != null) {
                replyTo = mReplyItem.getDescriptorId();
            }

            if (mConversationService.isSendingFiles()) {
                mDeferredMessage = text;
                mDeferredAllowCopyText = allowCopyText;
                mDeferredTimeout = timeout;
                mDeferredReplyTo = replyTo;
            } else {
                mConversationService.pushMessage(text, allowCopyText, timeout, replyTo);
            }
        }

        // When we send something and we are not connected, report a toast message to explain the network issue.
        if (!getTwinmeContext().isConnected()) {
            final NetworkStatus net = new NetworkStatus();
            net.getNetworkDiagnostic(getApplicationContext());

            toast(getString(R.string.conversation_activity_cannot_send) + "\n" + getString(net.getMessage()));
        }

        mSendButtonListener.reset();
        closeMenu();
        closeReplyView();
    }

    public void onSendLongClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendLongClick");
        }

        if (!mIsMenuSendOptionOpen && (mSendClickableView.getAlpha() == 1.0 || mVoiceRecorderMessageView.isSendButtonEnable())) {
            setSelectedMode(Mode.DEFAULT);

            mAllowCopy = false;

            boolean allowCopyText = getTwinmeApplication().messageCopyAllowed();
            boolean allowCopyFile = getTwinmeApplication().fileCopyAllowed();

            boolean isTextToSend = !isEmptyText();

            if (isTextToSend) {
                if (allowCopyText || allowCopyFile) {
                    mAllowCopy = true;
                }
            }

            mIsMenuSendOptionOpen = true;
            mMenuSendOptionView.setVisibility(View.VISIBLE);
            mOverlayView.setVisibility(View.VISIBLE);
            mMenuSendOptionView.openMenu(mAllowCopy);

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    private void onCloseEditViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseEditViewClick");
        }

        hapticFeedback();
        hideKeyboard();

        mEditMessageView.setVisibility(View.GONE);
        mEditCancelView.setVisibility(View.GONE);
        mEditConfirmView.setVisibility(View.GONE);

        mSendClickableView.setVisibility(View.VISIBLE);
        mMenuClickableView.setVisibility(View.VISIBLE);

        mEditItem = null;
        mEditText.setText("");
        updateSendButton();
    }

    private void onConfirmEditViewClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConfirmEditViewClick");
        }

        hapticFeedback();
        setSelectedMode(Mode.DEFAULT);
        hideKeyboard();

        mEditMessageView.setVisibility(View.GONE);
        mEditCancelView.setVisibility(View.GONE);
        mEditConfirmView.setVisibility(View.GONE);

        mSendClickableView.setVisibility(View.VISIBLE);
        mMenuClickableView.setVisibility(View.VISIBLE);

        if (mEditItem != null) {
            String text = getSendText();
            if (!text.trim().isEmpty()) {
                mEditText.setText("");
                mConversationService.updateDescriptor(mEditItem.getDescriptorId(), text);
            }

            mEditItem = null;
        }

        updateSendButton();
    }

    private void setSelectedMode(Mode mode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setSelectedMode: mode=" + mode);
        }

        switch (mSelectedMode) {
            case DEFAULT:
            case TEXT:
                break;

            case GALLERY:
                if (mode == Mode.GALLERY) {
                    mode = Mode.TEXT;
                }
                break;

            case MICRO:
                if (mode == Mode.MICRO) {
                    mode = Mode.TEXT;
                }
                break;
        }

        mSelectedMode = mode;

        mEditText.setVisibility(View.VISIBLE);

        if (mSelectedMode != Mode.MICRO) {
            mVoiceRecorderMessageView.releaseRecorder();
            mVoiceRecorderMessageView.setVisibility(View.INVISIBLE);
        }

        switch (mSelectedMode) {
            case DEFAULT:
                hideKeyboard();
                break;

            case TEXT:
                break;

            case GALLERY:
                hideKeyboard();
                if (mVoiceRecorderMessageView != null) {
                    mVoiceRecorderMessageView.setVisibility(View.GONE);
                }
                break;

            case MICRO:
                hideKeyboard();
                mEditText.setVisibility(View.INVISIBLE);
                mVoiceRecorderMessageView.setVisibility(View.VISIBLE);
                break;
        }

        Handler mHandler = new Handler();
        mHandler.postDelayed(() -> {
            // Scroll only if the view is still valid.
            if (mItemListView != null) {
                mItemListView.smoothScrollToPosition(mItems.size() + 1);
            }
        }, 500);
    }

    private void hideKeyboard() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideKeyboard");
        }

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
    }

    @NonNull
    private String getSendText() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getSendText");
        }

        Editable text = mEditText.getText();
        if (text == null) {
            return "";
        }

        return text.toString();
    }

    private boolean isEmptyText() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isEmptyText");
        }

        return getSendText().trim().isEmpty();
    }

    private void updateSendButton() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSendButton");
        }

        if (!isEmptyText()) {
            mSendClickableView.setAlpha(1.0f);
            mCameraClickableView.setVisibility(View.GONE);
            mRecordAudioClickableView.setVisibility(View.GONE);
        } else {
            mSendClickableView.setAlpha(0.7f);
            mCameraClickableView.setVisibility(View.VISIBLE);
            mRecordAudioClickableView.setVisibility(View.VISIBLE);
        }

        int editTextTopPadding =  (int) (DESIGN_EDIT_TEXT_HEIGHT_INSET * Design.HEIGHT_RATIO);
        if (mEditItem != null) {
            editTextTopPadding += (int) (DESIGN_EDIT_MESSAGE_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        }

        mEditText.setPadding((int) (DESIGN_EDIT_TEXT_WIDTH_INSET * Design.WIDTH_RATIO), editTextTopPadding, (int) (DESIGN_EDIT_TEXT_WIDTH_INSET * Design.WIDTH_RATIO), (int) (DESIGN_EDIT_TEXT_HEIGHT_INSET * Design.HEIGHT_RATIO));
    }

    private void addObjectDescriptor(ObjectDescriptor objectDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addObjectDescriptor: objectDescriptor=" + objectDescriptor);
        }

        final String message = objectDescriptor.getMessage();
        if (mConversationService.isLocalDescriptor(objectDescriptor)) {
            URL url = Utils.extractURLFromString(message);
            if (url != null && getTwinmeApplication().visualizationLink()) {
                LinkItem linkItem = new LinkItem(objectDescriptor, objectDescriptor.getReplyToDescriptor(), url);
                addItem(linkItem);
            } else {
                MessageItem messageItem = new MessageItem(objectDescriptor, objectDescriptor.getReplyToDescriptor());
                addItem(messageItem);
            }
        } else if (mConversationService.isPeerDescriptor(objectDescriptor)) {
            URL url = Utils.extractURLFromString(message);
            if (url != null && getTwinmeApplication().visualizationLink()) {
                PeerLinkItem peerLinkItem = new PeerLinkItem(objectDescriptor, objectDescriptor.getReplyToDescriptor(), url);
                addItem(peerLinkItem);
            } else {
                PeerMessageItem peerMessageItem = new PeerMessageItem(objectDescriptor, objectDescriptor.getReplyToDescriptor());
                addItem(peerMessageItem);
            }
        } else {
            getTwinmeContext().assertion(ApplicationAssertPoint.INVALID_DESCRIPTOR, AssertPoint.create(mSubject)
                    .putTwincodeId(objectDescriptor.getDescriptorId().twincodeOutboundId)
                    .put(objectDescriptor.getType()));
        }
    }

    private void addImageDescriptor(ImageDescriptor imageDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addImageDescriptor: imageDescriptor=" + imageDescriptor);
        }

        if (mConversationService.isLocalDescriptor(imageDescriptor)) {
            ImageItem imageItem = new ImageItem(imageDescriptor, imageDescriptor.getReplyToDescriptor());
            addItem(imageItem);
        } else if (mConversationService.isPeerDescriptor(imageDescriptor)) {
            PeerImageItem peerImageItem = new PeerImageItem(imageDescriptor, imageDescriptor.getReplyToDescriptor());
            addItem(peerImageItem);
        } else {
            getTwinmeContext().assertion(ApplicationAssertPoint.INVALID_DESCRIPTOR, AssertPoint.create(mSubject)
                    .putTwincodeId(imageDescriptor.getDescriptorId().twincodeOutboundId)
                    .put(imageDescriptor.getType()));
        }
    }

    private void addAudioDescriptor(AudioDescriptor audioDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addAudioDescriptor: audioDescriptor=" + audioDescriptor);
        }

        if (mConversationService.isLocalDescriptor(audioDescriptor)) {
            AudioItem audioItem = new AudioItem(audioDescriptor, audioDescriptor.getReplyToDescriptor());
            addItem(audioItem);
        } else if (mConversationService.isPeerDescriptor(audioDescriptor)) {
            PeerAudioItem audioItem = new PeerAudioItem(audioDescriptor, audioDescriptor.getReplyToDescriptor());
            addItem(audioItem);
        } else {
            getTwinmeContext().assertion(ApplicationAssertPoint.INVALID_DESCRIPTOR, AssertPoint.create(mSubject)
                    .putTwincodeId(audioDescriptor.getDescriptorId().twincodeOutboundId)
                    .put(audioDescriptor.getType()));
        }
    }

    private void addVideoDescriptor(VideoDescriptor videoDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addVideoDescriptor: videoDescriptor=" + videoDescriptor);
        }

        if (mConversationService.isLocalDescriptor(videoDescriptor)) {
            VideoItem videoItem = new VideoItem(videoDescriptor, videoDescriptor.getReplyToDescriptor());
            addItem(videoItem);
        } else if (mConversationService.isPeerDescriptor(videoDescriptor)) {
            PeerVideoItem peerVideoItem = new PeerVideoItem(videoDescriptor, videoDescriptor.getReplyToDescriptor());
            addItem(peerVideoItem);
        } else {
            getTwinmeContext().assertion(ApplicationAssertPoint.INVALID_DESCRIPTOR, AssertPoint.create(mSubject)
                    .putTwincodeId(videoDescriptor.getDescriptorId().twincodeOutboundId)
                    .put(videoDescriptor.getType()));
        }
    }

    private void addInvitationDescriptor(InvitationDescriptor invitationDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addInvitationDescriptor: objectDescriptor=" + invitationDescriptor);
        }

        if (mConversationService.isLocalDescriptor(invitationDescriptor)) {
            InvitationItem invitationItem = new InvitationItem(this, this, invitationDescriptor);
            addItem(invitationItem);
        } else if (mConversationService.isPeerDescriptor(invitationDescriptor)) {
            PeerInvitationItem invitationItem = new PeerInvitationItem(this, this, invitationDescriptor);
            addItem(invitationItem);
        } else {
            getTwinmeContext().assertion(ApplicationAssertPoint.INVALID_DESCRIPTOR, AssertPoint.create(mSubject)
                    .putTwincodeId(invitationDescriptor.getDescriptorId().twincodeOutboundId)
                    .put(invitationDescriptor.getType()));
        }
    }

    private void addCallDescriptor(CallDescriptor callDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addCallDescriptor: callDescriptor=" + callDescriptor);
        }

        Item callItem = callDescriptor.isIncoming() ? new PeerCallItem(callDescriptor) : new CallItem(callDescriptor);
        addItem(callItem);
    }

    private void addTwincodeDescriptor(TwincodeDescriptor twincodeDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addTwincodeDescriptor: twincodeDescriptor=" + twincodeDescriptor);
        }

        if (mConversationService.isLocalDescriptor(twincodeDescriptor)) {
            InvitationContactItem invitationContactItem = new InvitationContactItem(this, this, twincodeDescriptor);
            addItem(invitationContactItem);
        } else if (mConversationService.isPeerDescriptor(twincodeDescriptor)) {
            PeerInvitationContactItem peerInvitationContactItem = new PeerInvitationContactItem(this, this, twincodeDescriptor);
            addItem(peerInvitationContactItem);
        } else {
            getTwinmeContext().assertion(ApplicationAssertPoint.INVALID_DESCRIPTOR, AssertPoint.create(mSubject)
                    .putTwincodeId(twincodeDescriptor.getDescriptorId().twincodeOutboundId)
                    .put(twincodeDescriptor.getType()));
        }
    }

    private void addClearDescriptor(ClearDescriptor clearDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addClearDescriptor: clearDescriptor=" + clearDescriptor);
        }

        if (mConversationService.isLocalDescriptor(clearDescriptor)) {
            ClearItem clearItem = new ClearItem(clearDescriptor);
            addItem(clearItem);
        } else if (mConversationService.isPeerDescriptor(clearDescriptor)) {
            PeerClearItem peerClearItem = new PeerClearItem(clearDescriptor);
            String name = "";
            if(mSubject != null) {
                if (mSubject.getType() == Originator.Type.GROUP) {
                    Originator member = mGroupMembers.get(peerClearItem.getPeerTwincodeOutboundId());
                    if (member != null) {
                        name = member.getName();
                    }
                } else {
                    name = mSubject.getName();
                }
            }
            peerClearItem.setName(name);
            addItem(peerClearItem);
        } else {
            getTwinmeContext().assertion(ApplicationAssertPoint.INVALID_DESCRIPTOR, AssertPoint.create(mSubject)
                    .putTwincodeId(clearDescriptor.getDescriptorId().twincodeOutboundId)
                    .put(clearDescriptor.getType()));
        }
    }

    private void addTransientObjectDescriptor(TransientObjectDescriptor transientObjectDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addTransientObjectDescriptor: objectDescriptor=" + transientObjectDescriptor);
        }

        boolean typistsUpdated = false;

        Object object = transientObjectDescriptor.getObject();

        if (object instanceof Typing && mSubject != null) {
            Originator typist = mSubject.getType() == Originator.Type.GROUP ?
                    mGroupMembers.get(transientObjectDescriptor.getTwincodeOutboundId()) :
                    mSubject;

            if (typist != null) {
                if (((Typing) object).getAction() == Typing.Action.START) {
                    if (!mTypingOriginators.contains(typist)) {
                        typistsUpdated = true;
                        mTypingOriginators.add(typist);
                    }
                } else {
                    typistsUpdated = mTypingOriginators.remove(typist);
                }
            }
        }

        if (typistsUpdated) {
            mIsPeerTyping = !mTypingOriginators.isEmpty();
            if (mUIInitialized) {
                if (mIsPeerTyping) {
                    if (mTypingOriginators.size() == 1) {
                        // -2 to account for footer
                        mItemListAdapter.notifyItemInserted(mItemListAdapter.getItemCount() - 2);
                    } else {
                        mItemListAdapter.notifyItemChanged(mItemListAdapter.getItemCount() - 2);
                    }
                } else {
                    mItemListAdapter.notifyItemRemoved(mItemListAdapter.getItemCount() - 2);
                }
            }
        }

        mTypingOriginatorsImages.clear();

        if (mTypingOriginators.isEmpty()) {
            if (mIsPeerTyping) {
                initPeerTypingTimer();
            }
        } else {
            AtomicInteger avatarCounter = new AtomicInteger(mTypingOriginators.size());
            for (Originator originator : mTypingOriginators) {
                if (originator instanceof Contact) {
                    mConversationService.getImage(originator, (Bitmap avatar) -> {
                        mTypingOriginatorsImages.add(avatar);
                        if (avatarCounter.decrementAndGet() == 0) {
                            if (mIsPeerTyping) {
                                initPeerTypingTimer();
                            }
                            mItemListAdapter.notifyItemChanged(mItemListAdapter.getItemCount() - 2);
                        }
                    });
                } else if (originator instanceof GroupMember) {
                    mConversationService.getGroupMemberImage((GroupMember) originator, (Bitmap avatar) -> {
                        mTypingOriginatorsImages.add(avatar);
                        if (avatarCounter.decrementAndGet() == 0) {
                            if (mIsPeerTyping) {
                                initPeerTypingTimer();
                            }
                            mItemListAdapter.notifyItemChanged(mItemListAdapter.getItemCount() - 2);
                        }
                    });
                }
            }
        }
    }

    private void addNamedFileDescriptor(NamedFileDescriptor namedFileDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addNamedFileDescriptor: namedFileDescriptor=" + namedFileDescriptor);
        }

        if (mConversationService.isLocalDescriptor(namedFileDescriptor)) {
            FileItem fileItem = new FileItem(namedFileDescriptor, namedFileDescriptor.getReplyToDescriptor());
            addItem(fileItem);
        } else if (mConversationService.isPeerDescriptor(namedFileDescriptor)) {
            PeerFileItem peerFileItem = new PeerFileItem(namedFileDescriptor, namedFileDescriptor.getReplyToDescriptor());
            addItem(peerFileItem);
        } else {
            getTwinmeContext().assertion(ApplicationAssertPoint.INVALID_DESCRIPTOR, AssertPoint.create(mSubject)
                    .putTwincodeId(namedFileDescriptor.getDescriptorId().twincodeOutboundId)
                    .put(namedFileDescriptor.getType()));
        }
    }

    private void addItem(Item item) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addItem: item=" + item);
        }

        if (item.getDescriptorId().sequenceId != DEFAULT_SEQUENCE_ID) {
            for (Item lItem : mItems) {
                if (lItem.getDescriptorId().equals(item.getDescriptorId())) {

                    return;
                }
            }
        }

        Item lastReadPeerItem = null;
        long lastReadPeerItemTimestamp = mLastReadPeerItem != null ? mLastReadPeerItem.getTimestamp() : -1;
        if (!item.isPeerItem() && item.getReadTimestamp() != 0 && item.getReadTimestamp() != -1) {
            if (item.getTimestamp() < lastReadPeerItemTimestamp) {
                item.resetState();
            } else if (item.getTimestamp() > lastReadPeerItemTimestamp) {
                lastReadPeerItem = mLastReadPeerItem;
                mLastReadPeerItem = item;
            }
        }

        boolean notifyAdapter = mUIInitialized && !mBatchUpdate;

        int itemIndex = -1;
        for (int index = mItems.size() - 1; index >= 0; index--) {
            Item lItem = mItems.get(index);
            if (lItem.compareTo(item) <= 0) {
                itemIndex = index + 1;
                break;
            }
        }
        if (itemIndex == -1) {
            itemIndex = 0;
        }
        mItems.add(itemIndex, item);
        if (notifyAdapter) {
            mItemListAdapter.notifyItemInserted(mItemListAdapter.indexToPosition(itemIndex));
        }

        int previousItemIndex = itemIndex - 1;
        Item previousItem = null;
        if (previousItemIndex >= 0) {
            previousItem = mItems.get(previousItemIndex);
        }

        int nextItemIndex = itemIndex + 1;
        Item nextItem = null;
        if (nextItemIndex < mItems.size()) {
            nextItem = mItems.get(nextItemIndex);
        }

        if (nextItem != null && nextItem.getType() == Item.ItemType.TIME) {
            int nextNextItemIndex = itemIndex + 2;
            Item nextNextItem;
            if (nextNextItemIndex < mItems.size()) {
                nextNextItem = mItems.get(nextNextItemIndex);
                if (nextNextItem != null && Math.abs(item.getTimestamp() - nextNextItem.getTimestamp()) < MAX_DELTA_TIMESTAMP2) {
                    mItems.remove(nextItemIndex);
                    if (notifyAdapter) {
                        mItemListAdapter.notifyItemRemoved(mItemListAdapter.indexToPosition(nextItemIndex));
                    }
                    nextItem = nextNextItem;
                }
            }
        }

        boolean previousItemChanged = false;
        boolean nextItemChanged = false;
        switch (item.getType()) {
            case MESSAGE:
            case LINK:
            case IMAGE:
            case AUDIO:
            case VIDEO:
            case FILE:
            case INVITATION:
            case CALL:
            case CLEAR:
            case INVITATION_CONTACT:
                if (previousItem != null) {
                    switch (previousItem.getType()) {
                        case MESSAGE:
                        case LINK:
                        case IMAGE:
                        case AUDIO:
                        case VIDEO:
                        case FILE:
                        case INVITATION:
                        case CALL:
                        case INVITATION_CONTACT:
                            if (item.getTimestamp() - previousItem.getTimestamp() < MAX_DELTA_TIMESTAMP1) {
                                int corners = previousItem.getCorners();
                                previousItem.cornersBitwiseAnd(~(Item.BOTTOM_RIGHT | Item.BOTTOM_LARGE_MARGIN));
                                previousItemChanged = corners != previousItem.getCorners();
                                item.cornersBitwiseAnd(~(Item.TOP_RIGHT | Item.TOP_LARGE_MARGIN));
                            } else {
                                int corners = previousItem.getCorners();
                                previousItem.cornersBitwiseOr(Item.BOTTOM_RIGHT | Item.BOTTOM_LARGE_MARGIN);
                                previousItemChanged = corners != previousItem.getCorners();
                            }
                            break;

                        case PEER_MESSAGE:
                        case PEER_LINK:
                        case PEER_IMAGE:
                        case PEER_AUDIO:
                        case PEER_VIDEO:
                        case PEER_FILE:
                        case PEER_INVITATION:
                        case PEER_CALL:
                        case PEER_INVITATION_CONTACT:
                            int corners = previousItem.getCorners();
                            previousItem.cornersBitwiseOr(Item.BOTTOM_LEFT | Item.BOTTOM_LARGE_MARGIN);
                            boolean visibleAvatar = previousItem.getVisibleAvatar();
                            previousItem.setVisibleAvatar(true);
                            previousItemChanged = corners != previousItem.getCorners() || visibleAvatar != previousItem.getVisibleAvatar();
                            item.cornersBitwiseOr(Item.TOP_RIGHT | Item.TOP_LARGE_MARGIN);
                            break;

                        case NAME:
                        default:
                            break;
                    }
                }

                if (nextItem != null) {
                    switch (nextItem.getType()) {
                        case MESSAGE:
                        case LINK:
                        case IMAGE:
                        case AUDIO:
                        case VIDEO:
                        case FILE:
                        case INVITATION:
                        case CALL:
                        case INVITATION_CONTACT:
                            if (nextItem.getTimestamp() - item.getTimestamp() < MAX_DELTA_TIMESTAMP1) {
                                item.cornersBitwiseAnd(~(Item.BOTTOM_RIGHT | Item.BOTTOM_LARGE_MARGIN));
                                int corners = nextItem.getCorners();
                                nextItem.cornersBitwiseAnd(~(Item.TOP_RIGHT | Item.TOP_LARGE_MARGIN));
                                nextItemChanged = corners != nextItem.getCorners();
                            } else {
                                int corners = nextItem.getCorners();
                                nextItem.cornersBitwiseOr(Item.TOP_RIGHT | Item.TOP_LARGE_MARGIN);
                                nextItemChanged = corners != nextItem.getCorners();
                            }
                            break;

                        case PEER_MESSAGE:
                        case PEER_LINK:
                        case PEER_IMAGE:
                        case PEER_AUDIO:
                        case PEER_VIDEO:
                        case PEER_FILE:
                        case PEER_INVITATION:
                        case PEER_CALL:
                        case PEER_INVITATION_CONTACT: {
                            item.cornersBitwiseOr(Item.BOTTOM_RIGHT | Item.BOTTOM_LARGE_MARGIN);
                            int corners = nextItem.getCorners();
                            nextItem.cornersBitwiseOr(Item.TOP_LEFT | Item.TOP_LARGE_MARGIN);
                            nextItemChanged = corners != nextItem.getCorners();
                            break;
                        }

                        default:
                            break;
                    }
                }
                break;

            case PEER_MESSAGE:
            case PEER_LINK:
            case PEER_IMAGE:
            case PEER_AUDIO:
            case PEER_VIDEO:
            case PEER_FILE:
            case PEER_INVITATION:
            case PEER_CALL:
            case PEER_INVITATION_CONTACT:
                if (previousItem == null || !previousItem.isSamePeer(item)) {
                    // For a group conversation, add the member's name before its item.
                    Originator member = mGroupMembers.get(item.getPeerTwincodeOutboundId());
                    if (member != null && member.getName() != null) {
                        mItems.add(itemIndex, new NameItem(item.getTimestamp(), member.getName()));
                        item.cornersBitwiseAnd(~Item.TOP_LARGE_MARGIN);
                        nextItemIndex++;
                    }
                }
                item.setVisibleAvatar(true);
                if (previousItem != null) {
                    switch (previousItem.getType()) {
                        case MESSAGE:
                        case LINK:
                        case IMAGE:
                        case AUDIO:
                        case VIDEO:
                        case FILE:
                        case INVITATION:
                        case CALL:
                        case INVITATION_CONTACT: {
                            int corners = previousItem.getCorners();
                            previousItem.cornersBitwiseOr(Item.BOTTOM_RIGHT | Item.BOTTOM_LARGE_MARGIN);
                            previousItemChanged = corners != previousItem.getCorners();
                            item.cornersBitwiseOr(Item.TOP_LEFT | Item.TOP_LARGE_MARGIN);
                            break;
                        }

                        case PEER_MESSAGE:
                        case PEER_LINK:
                        case PEER_IMAGE:
                        case PEER_AUDIO:
                        case PEER_FILE:
                        case PEER_VIDEO:
                        case PEER_INVITATION:
                        case PEER_CALL:
                        case PEER_INVITATION_CONTACT: {
                            if (item.isSamePeer(previousItem) && item.getTimestamp() - previousItem.getTimestamp() < MAX_DELTA_TIMESTAMP1) {
                                int corners = previousItem.getCorners();
                                previousItem.cornersBitwiseAnd(~Item.BOTTOM_LEFT);
                                boolean visibleAvatar = previousItem.getVisibleAvatar();
                                previousItem.setVisibleAvatar(false);
                                previousItemChanged = corners != previousItem.getCorners() || visibleAvatar != previousItem.getVisibleAvatar();
                                item.cornersBitwiseAnd(~Item.TOP_LEFT);
                            } else {
                                int corners = previousItem.getCorners();
                                previousItem.cornersBitwiseOr(Item.BOTTOM_LEFT);
                                boolean visibleAvatar = previousItem.getVisibleAvatar();
                                previousItem.setVisibleAvatar(true);
                                previousItemChanged = corners != previousItem.getCorners() || visibleAvatar != previousItem.getVisibleAvatar();
                            }
                            if (item.isSamePeer(previousItem)) {
                                previousItem.cornersBitwiseAnd(~Item.BOTTOM_LARGE_MARGIN);
                            }
                            item.cornersBitwiseAnd(~Item.TOP_LARGE_MARGIN);
                            break;
                        }

                        case NAME:
                            if (previousItemIndex > 0) {
                                previousItemIndex--;
                                previousItem = mItems.get(previousItemIndex);
                            }
                            break;

                        default:
                            break;
                    }
                }

                if (nextItem != null) {
                    if (nextItem.getType() == Item.ItemType.NAME) {
                        // For a group conversation, the next item can be a name that precedes the message.
                        // If this is the same name, remove it.
                        // @todo SCz: we should not compare on a name.
                        Originator member = mGroupMembers.get(item.getPeerTwincodeOutboundId());
                        if (member != null && member.getName() != null && member.getName().equals(((NameItem) nextItem).getName())) {
                            item.setVisibleAvatar(false);
                            mItems.remove(nextItemIndex);
                            mItemListAdapter.notifyItemRemoved(mItemListAdapter.indexToPosition(nextItemIndex));
                            if (mItems.size() > nextItemIndex) {
                                nextItem = mItems.get(nextItemIndex);
                            }
                        }
                    }
                    switch (nextItem.getType()) {
                        case MESSAGE:
                        case LINK:
                        case IMAGE:
                        case AUDIO:
                        case VIDEO:
                        case FILE:
                        case INVITATION:
                        case CALL:
                        case INVITATION_CONTACT: {
                            item.cornersBitwiseOr(Item.BOTTOM_LEFT | Item.BOTTOM_LARGE_MARGIN);
                            int corners = nextItem.getCorners();
                            nextItem.cornersBitwiseOr(Item.TOP_RIGHT | Item.TOP_LARGE_MARGIN);
                            nextItemChanged = corners != nextItem.getCorners();
                            break;
                        }

                        case PEER_MESSAGE:
                        case PEER_LINK:
                        case PEER_IMAGE:
                        case PEER_AUDIO:
                        case PEER_VIDEO:
                        case PEER_FILE:
                        case PEER_INVITATION:
                        case PEER_CALL:
                        case PEER_INVITATION_CONTACT:
                            if (item.isSamePeer(nextItem)) {
                                if (nextItem.getTimestamp() - item.getTimestamp() < MAX_DELTA_TIMESTAMP1) {
                                    item.cornersBitwiseAnd(~(Item.BOTTOM_LEFT | Item.BOTTOM_LARGE_MARGIN));
                                    item.setVisibleAvatar(false);
                                    int corners = nextItem.getCorners();
                                    nextItem.cornersBitwiseAnd(~(Item.TOP_LEFT | Item.TOP_LARGE_MARGIN));
                                    nextItemChanged = corners != nextItem.getCorners();
                                } else {
                                    item.setVisibleAvatar(false);
                                    int corners = nextItem.getCorners();
                                    nextItem.cornersBitwiseOr(Item.TOP_LEFT | Item.TOP_LARGE_MARGIN);
                                    nextItemChanged = corners != nextItem.getCorners();
                                }
                            } else {
                                // For a group conversation, add the member's name before its item.
                                Originator member = mGroupMembers.get(nextItem.getPeerTwincodeOutboundId());
                                if (member != null && member.getName() != null) {
                                    mItems.add(nextItemIndex, new NameItem(nextItem.getTimestamp(), member.getName()));
                                    nextItemIndex++;
                                    if (notifyAdapter) {
                                        mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(nextItemIndex));
                                    }
                                }
                            }
                            break;

                        case NAME:
                        default:
                            break;
                    }
                }
                break;

            default:
                break;
        }
        if (notifyAdapter && previousItemChanged) {
            mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(previousItemIndex));
        }
        if (notifyAdapter && nextItemChanged) {
            mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(nextItemIndex));
        }

        if (lastReadPeerItem != null) {
            for (int index = mItems.size() - 1; index >= 0; index--) {
                Item lItem = mItems.get(index);
                if (lItem == lastReadPeerItem) {
                    lItem.resetState();
                    if (notifyAdapter) {
                        mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(index));
                    }
                    break;
                }
            }
        }

        if (item.getType() == Item.ItemType.TIME) {

            return;
        }

        boolean addTime;
        if (previousItem != null) {
            Item lastTimeItem = null;
            for (int index = itemIndex - 1; index >= 0; index--) {
                Item lItem = mItems.get(index);
                if (lItem.getType() == Item.ItemType.TIME) {
                    lastTimeItem = lItem;
                    break;
                }
            }

            if (lastTimeItem != null) {
                addTime = previousItem.getType() != Item.ItemType.TIME && item.getTimestamp() - lastTimeItem.getTimestamp() > MAX_DELTA_TIMESTAMP2;
            } else {
                addTime = previousItem.getType() != Item.ItemType.TIME && item.getTimestamp() - previousItem.getTimestamp() > MAX_DELTA_TIMESTAMP2;
            }
        } else {
            addTime = true;
        }

        if (addTime) {
            mItems.add(itemIndex, new TimeItem(item.getTimestamp()));
            if (notifyAdapter) {
                mItemListAdapter.notifyItemInserted(mItemListAdapter.indexToPosition(itemIndex));
            }
        }

        if (mItems.isEmpty()) {
            mEmptyConversationView.setVisibility(View.VISIBLE);
        } else {
            mEmptyConversationView.setVisibility(View.GONE);
        }
    }

    private void deleteItem(Item item, int indexDelete) {
        if (DEBUG) {
            Log.d(LOG_TAG, "deleteItem: item=" + item + " indexDelete= " + indexDelete);
        }

        int previousItemIndex = indexDelete - 1;
        Item previousItem = null;
        if (previousItemIndex >= 0) {
            previousItem = mItems.get(previousItemIndex);
        }

        int nextItemIndex = indexDelete + 1;
        Item nextItem = null;
        if (nextItemIndex < mItems.size()) {
            nextItem = mItems.get(nextItemIndex);
        }

        Item nameItemToRemove = null;
        Item nextNameItemToRemove = null;
        if (previousItem != null) {
            // Check if the previous name must be removed.
            if (previousItem.getType() == Item.ItemType.NAME) {
                if (nextItem == null) {
                    nameItemToRemove = previousItem;
                } else {
                    switch (nextItem.getType()) {
                        case NAME:
                        case MESSAGE:
                        case LINK:
                        case AUDIO:
                        case VIDEO:
                        case FILE:
                        case INVITATION:
                        case CALL:
                        case CLEAR:
                        case INVITATION_CONTACT:
                            nameItemToRemove = previousItem;
                            break;

                        default:
                            break;
                    }
                }
                // See what is before that name if we remove it.
                if (nameItemToRemove != null && previousItemIndex > 1) {
                    previousItemIndex--;
                    previousItem = mItems.get(previousItemIndex);
                }
            }
            switch (previousItem.getType()) {
                case MESSAGE:
                case LINK:
                case IMAGE:
                case AUDIO:
                case VIDEO:
                case FILE:
                case INVITATION:
                case CALL:
                case CLEAR:
                case INVITATION_CONTACT:
                    if (nextItem == null) {
                        previousItem.cornersBitwiseAnd(~Item.BOTTOM_RIGHT);
                        previousItem.updateState();
                    } else if (!previousItem.isSamePeer(nextItem)) {
                        previousItem.cornersBitwiseOr(Item.BOTTOM_RIGHT);
                        nextItem.cornersBitwiseOr(Item.TOP_RIGHT);
                    } else if (nextItem.getTimestamp() - previousItem.getTimestamp() < MAX_DELTA_TIMESTAMP1) {
                        previousItem.cornersBitwiseAnd(~Item.BOTTOM_RIGHT);
                        nextItem.cornersBitwiseAnd(~Item.TOP_RIGHT);
                    } else {
                        previousItem.cornersBitwiseOr(Item.BOTTOM_RIGHT);
                    }
                    break;

                case PEER_MESSAGE:
                case PEER_LINK:
                case PEER_IMAGE:
                case PEER_AUDIO:
                case PEER_VIDEO:
                case PEER_FILE:
                case PEER_INVITATION:
                case PEER_CALL:
                case PEER_CLEAR:
                case PEER_INVITATION_CONTACT:
                    if (nextItem == null) {
                        previousItem.cornersBitwiseOr(Item.BOTTOM_LEFT);
                        previousItem.setVisibleAvatar(true);
                    } else if (!previousItem.isSamePeer(nextItem)) {
                        previousItem.cornersBitwiseOr(Item.BOTTOM_LEFT);
                        nextItem.cornersBitwiseOr(Item.TOP_LEFT);
                        previousItem.setVisibleAvatar(true);
                    } else if (nextItem.getTimestamp() - previousItem.getTimestamp() < MAX_DELTA_TIMESTAMP1) {
                        previousItem.cornersBitwiseAnd(~Item.BOTTOM_LEFT);
                        nextItem.cornersBitwiseAnd(~Item.TOP_LEFT);
                        previousItem.setVisibleAvatar(false);
                    } else {
                        previousItem.cornersBitwiseOr(Item.BOTTOM_LEFT);
                        nextItem.cornersBitwiseOr(Item.TOP_LEFT);
                        previousItem.setVisibleAvatar(false);
                    }
                    break;

                default:
                    break;
            }
        }

        if (nextItem != null) {
            switch (nextItem.getType()) {
                case MESSAGE:
                case LINK:
                case IMAGE:
                case AUDIO:
                case VIDEO:
                case FILE:
                case INVITATION:
                case CALL:
                case CLEAR:
                case INVITATION_CONTACT:
                    if (previousItem == null) {
                        nextItem.cornersBitwiseOr(Item.TOP_RIGHT);
                    } else if (!previousItem.isSamePeer(nextItem)) {
                        previousItem.cornersBitwiseOr(Item.BOTTOM_RIGHT);
                        nextItem.cornersBitwiseOr(Item.TOP_RIGHT);
                    } else if ((nextItem.getTimestamp() - previousItem.getTimestamp() < MAX_DELTA_TIMESTAMP1)) {
                        previousItem.cornersBitwiseAnd(~Item.BOTTOM_RIGHT);
                        nextItem.cornersBitwiseAnd(~Item.TOP_RIGHT);
                    } else {
                        nextItem.cornersBitwiseOr(Item.TOP_RIGHT);
                    }
                    break;

                case PEER_MESSAGE:
                case PEER_LINK:
                case PEER_IMAGE:
                case PEER_AUDIO:
                case PEER_VIDEO:
                case PEER_FILE:
                case PEER_INVITATION:
                case PEER_CALL:
                case PEER_CLEAR:
                case PEER_INVITATION_CONTACT:
                    // Propagate the top left corner to the next item because current item is removed.
                    if ((item.getCorners() & Item.TOP_LEFT) != 0) {
                        nextItem.cornersBitwiseOr(Item.TOP_LEFT);
                    }
                    break;

                case NAME:
                    if (nameItemToRemove != null && previousItem.isSamePeer(nextItem)) {
                        nextNameItemToRemove = nextItem;
                    }
                    break;

                default:
                    break;
            }
        }

        if (!mUIInitialized) {
            return;
        }

        // If we have removed one of our message, we must scan our messages from the last sent
        // and call updateState() to make sure the avatar is displayed if necessary on the last
        // message that we have sent.
        Item lastSentItem = null;
        int lastSentItemIndex = 0;
        if (!item.isPeerItem()) {
            for (int index = mItems.size() - 1; index >= 0; index--) {
                Item lItem = mItems.get(index);
                if (item != lItem && !lItem.isPeerItem()) {
                    lItem.updateState();
                    lastSentItem = lItem;
                    lastSentItemIndex = index;
                    break;
                }
            }
        }

        final Item lPreviousItemToRemove = nameItemToRemove;
        final Item lNextItemToRemove = nextNameItemToRemove;
        final int lPreviousItemIndex = previousItemIndex;
        final Item lPreviousItem = previousItem;
        final Item lNextItem = nextItem;
        final Item lLastSentItem = lastSentItem;
        final int lLastSentItemIndex = lastSentItemIndex;

        mItemListView.post(() -> {
            if (lPreviousItem != null && lPreviousItem.getState() != Item.ItemState.BOTH_DELETED) {
                mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(lPreviousItemIndex));
            }

            if (lNextItem != null && lNextItem.getState() != Item.ItemState.BOTH_DELETED) {
                mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(nextItemIndex));
            }

            if (lLastSentItem != null && lLastSentItem.getState() != Item.ItemState.BOTH_DELETED) {
                mItemListAdapter.notifyItemChanged(mItemListAdapter.indexToPosition(lLastSentItemIndex));
            }

            if (lNextItemToRemove != null) {
                mItems.remove(lNextItemToRemove);
                mItemListAdapter.notifyItemRemoved(mItemListAdapter.indexToPosition(indexDelete + 1));
            }
            mItems.remove(item);
            mItemListAdapter.notifyItemRemoved(mItemListAdapter.indexToPosition(indexDelete));

            if (lPreviousItem != null && lPreviousItem.getType() == Item.ItemType.TIME) {
                if (indexDelete < mItems.size()) {
                    Item nextDeleteItem = mItems.get(indexDelete);
                    if (nextDeleteItem.getType() == Item.ItemType.TIME) {
                        mItems.remove(lPreviousItem);
                        mItemListAdapter.notifyItemRemoved(mItemListAdapter.indexToPosition(indexDelete - 1));
                    }
                } else if (indexDelete == mItems.size()) {
                    mItems.remove(lPreviousItem);
                    mItemListAdapter.notifyItemRemoved(mItemListAdapter.indexToPosition(indexDelete - 1));
                }
            }

            if (lPreviousItemToRemove != null) {
                mItems.remove(lPreviousItemToRemove);
                mItemListAdapter.notifyItemRemoved(mItemListAdapter.indexToPosition(indexDelete - 1));
            }

            if (!mItems.isEmpty()) {
                Item lastItem = mItems.get(mItems.size() - 1);

                if (lastItem.getType() == Item.ItemType.TIME || lastItem.getType() == Item.ItemType.NAME) {
                    mItems.remove(lastItem);
                    mItemListAdapter.notifyItemRemoved(mItemListAdapter.indexToPosition(mItems.size() - 1));
                }
            }

            if (mItems.isEmpty()) {
                mEmptyConversationView.setVisibility(View.VISIBLE);
            } else {
                mEmptyConversationView.setVisibility(View.GONE);
            }
        });
    }

    private void deleteItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "deleteItems");
        }

        for (Item item : mSelectedItems) {
            if (item.isPeerItem()) {
                mConversationService.deleteDescriptor(item.getDescriptorId());
            } else {
                mConversationService.markDescriptorDeleted(item.getDescriptorId());
            }

            item.setSelected(false);
        }

        mItemSelectedActionView.updateSelectedItems(0);
        mItemListAdapter.notifyDataSetChanged();
    }

    private void resetSelectedItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "resetSelectedItems");
        }

        for (Item item : mSelectedItems) {
            item.setSelected(false);
        }

        mSelectedItems.clear();
    }

    private void clearConversationPlayingAudio() {

        if (mPlayingAudioItemViewHolder != null) {
            mPlayingAudioItemViewHolder.resetView();
            mPlayingAudioItemViewHolder = null;
        }

        if (mPlayingPeerAudioItemViewHolder != null) {
            mPlayingPeerAudioItemViewHolder.resetView();
            mPlayingPeerAudioItemViewHolder = null;
        }
    }

    private void onMicroClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMicroClick");
        }

        hapticFeedback();

        if (!mSendAllowed) {
            toast(getString(R.string.conversation_activity_group_not_allowed_post_message));
            return;
        }

        Permission[] permissions = new Permission[]{Permission.RECORD_AUDIO};
        if (checkPermissions(permissions)) {
            setSelectedMode(Mode.MICRO);
            mVoiceRecorderMessageView.startRecording();
        } else {
            mDeferredMode = Mode.MICRO;
        }
    }

    private void onMenuClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMenuClick");
        }

        hideKeyboard();
        hapticFeedback();

        ViewGroup viewGroup = findViewById(R.id.conversation_activity_layout);
        mMenuActionConversationView = new MenuActionConversationView(this, null);

        MenuActionConversationView.Observer observer = new MenuActionConversationView.Observer() {
            @Override
            public void onCloseMenu() {

                viewGroup.removeView(mMenuActionConversationView);
                mMenuActionConversationView = null;
                setStatusBarColor();
                removeBlurEffect();
            }

            @Override
            public void onSelectAction(UIActionConversation actionConversation) {

                viewGroup.removeView(mMenuActionConversationView);
                mMenuActionConversationView = null;
                setStatusBarColor();
                removeBlurEffect();

                switch (actionConversation.getConversationActionType()) {
                    case GALLERY:
                        onGalleryClick();
                        break;

                    case FILE:
                        onFileClick();
                        break;

                    case MANAGE_CONVERSATION:
                        onManageConversationClick();
                        break;

                    case MEDIAS_AND_FILES:
                        onMediasAndFilesClick();
                        break;

                    case PHOTO:
                        onCameraPhotoClick();
                        break;

                    case RESET:
                        onResetConversationConfirmClick();
                        break;

                    case VIDEO:
                        onCameraVideoClick();
                        break;

                    default:
                        break;
                }
            }
        };

        mMenuActionConversationView.initViews(this, observer, mSendAllowed);
        viewGroup.addView(mMenuActionConversationView);

        int navColor = ColorUtils.compositeColors(Design.CONVERSATION_OVERLAY_COLOR, Design.WHITE_COLOR);

        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.mutate();
        backgroundDrawable.setColor(Design.TOOLBAR_COLOR);
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColors(new int[]{Design.WHITE_COLOR, Design.CONVERSATION_OVERLAY_COLOR});
        gradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{backgroundDrawable, gradientDrawable});
        setStatusBarDrawable(layerDrawable, navColor);
        
        addBlurEffect();
    }

    private void onCameraPhotoClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCameraClick");
        }

        hapticFeedback();

        if (!mSendAllowed) {
            toast(getString(R.string.conversation_activity_group_not_allowed_post_message));
            return;
        }

        Permission[] permissions = new Permission[]{Permission.CAMERA};
        if (checkPermissions(permissions)) {
            takePhoto();
        } else {
            mDeferredMode = Mode.CAMERA;
        }
    }

    private void onCameraVideoClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCameraVideoClick");
        }

        hapticFeedback();

        if (!mSendAllowed) {
            toast(getString(R.string.conversation_activity_group_not_allowed_post_message));
            return;
        }

        Permission[] permissions = new Permission[]{Permission.CAMERA};
        if (checkPermissions(permissions)) {
            takeVideo();
        } else {
            mDeferredMode = Mode.CAMERA;
        }
    }

    private void takePhoto() {
        if (DEBUG) {
            Log.d(LOG_TAG, "takePhoto");
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            File directory = new File(getFilesDir(), Twinlife.TMP_DIR);
            if (!directory.isDirectory()) {
                if (!directory.mkdirs() || !directory.isDirectory()) {
                    return;
                }
            }

            File captureFile = new File(directory, System.currentTimeMillis() + ".jpg");
            try {
                //noinspection ResultOfMethodCallIgnored
                captureFile.createNewFile();
            } catch (IOException exception) {
                return;
            }

            mCaptureUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", captureFile);
            List<ResolveInfo> resolvedIntentActivities = getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                String packageName = resolvedIntentInfo.activityInfo.packageName;
                grantUriPermission(packageName, mCaptureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            cameraIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mCaptureUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void takeVideo() {
        if (DEBUG) {
            Log.d(LOG_TAG, "takePhoto");
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        try {
            File directory = new File(getFilesDir(), Twinlife.TMP_DIR);
            if (!directory.isDirectory()) {
                if (!directory.mkdirs() || !directory.isDirectory()) {
                    return;
                }
            }

            File captureFile = new File(directory, System.currentTimeMillis() + ".mp4");
            try {
                //noinspection ResultOfMethodCallIgnored
                captureFile.createNewFile();
            } catch (IOException exception) {
                return;
            }

            mCaptureUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", captureFile);
            List<ResolveInfo> resolvedIntentActivities = getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                String packageName = resolvedIntentInfo.activityInfo.packageName;
                grantUriPermission(packageName, mCaptureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mCaptureUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void onGalleryClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGalleryClick");
        }

        hapticFeedback();

        if (!mSendAllowed) {
            toast(getString(R.string.conversation_activity_group_not_allowed_post_message));
            return;
        }

        if (mMediaPicker != null) {
            launch(mMediaPicker, new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                    .build());
        }
    }

    private void onPreviewMedia(List<Uri> uris, boolean fromDirectShare) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPreviewMedia: uris=" + uris + " fromDirectShare=" + fromDirectShare);
        }

        Intent intent = new Intent(this, PreviewFileActivity.class);

        if (mContactId != null || mGroupId != null) {
            intent.putExtra(Intents.INTENT_CONTACT_ID, mContactId != null ? mContactId.toString() : mGroupId.toString());
        }

        if (mEditText.getText() != null) {
            intent.putExtra(Intents.INTENT_TEXT_MESSAGE, mEditText.getText().toString());
        }

        if (mSharedText != null) {
            intent.putExtra(Intents.INTENT_TEXT_MESSAGE, mSharedText.toString());
        }

        ArrayList<String> urisToString = new ArrayList<>();
        for (Uri uri : uris) {
            urisToString.add(uri.toString());
        }

        intent.putExtra(Intents.INTENT_PREVIEW_START_WITH_MEDIA, true);
        intent.putStringArrayListExtra(Intents.INTENT_SELECTED_URI, urisToString);
        intent.putExtra(Intents.INTENT_ALLOW_COPY_FILE,  getTwinmeApplication().fileCopyAllowed());

        if (fromDirectShare) {
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        }

        startActivityForResult(intent, REQUEST_PREVIEW_MEDIA);
    }

    private void onFileClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onFileClick");
        }

        hapticFeedback();

        if (!mSendAllowed) {
            toast(getString(R.string.conversation_activity_group_not_allowed_post_message));
            return;
        }

        if (isRecording()) {
            return;
        }

        Permission[] permissions = new Permission[]{
                Permission.READ_EXTERNAL_STORAGE,
                Permission.READ_MEDIA_AUDIO
        };

        if (checkPermissions(permissions)) {
            openFileIntent();
        } else {
            mDeferredMode = Mode.FILE;
        }
    }

    private void openFileIntent() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openFileIntent");
        }

        Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFileIntent.setType("*/*");
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        startActivityForResult(chooseFileIntent, REQUEST_GET_FILE);
    }

    private void onMediasAndFilesClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMediasAndFilesClick");
        }

        hapticFeedback();

        if (mContactId != null) {
            startActivity(ConversationFilesActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
        } else if (mGroupId != null) {
            startActivity(ConversationFilesActivity.class, Intents.INTENT_GROUP_ID, mGroupId);
        }
    }

    private void onManageConversationClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onManageConversationClick");
        }

        hapticFeedback();

        ViewGroup viewGroup = findViewById(R.id.conversation_activity_layout);

        MenuManageConversationView menuManageConversationView = new MenuManageConversationView(this, null);
        MenuManageConversationView.Observer observer = new MenuManageConversationView.Observer() {
            @Override
            public void onCleanupClick() {
                menuManageConversationView.animationCloseMenu();

                if (mContactId != null) {
                    startActivity(TypeCleanUpActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
                } else if (mGroupId != null) {
                    startActivity(TypeCleanUpActivity.class, Intents.INTENT_GROUP_ID, mGroupId);
                }
            }

            @Override
            public void onExportClick() {
                menuManageConversationView.animationCloseMenu();

                if (mContactId != null) {
                    startActivity(ExportActivity.class, Intents.INTENT_CONTACT_ID, mContactId);
                } else if (mGroupId != null) {
                    startActivity(ExportActivity.class, Intents.INTENT_GROUP_ID, mGroupId);
                }
            }

            @Override
            public void onCloseMenuSelectActionAnimationEnd() {

                viewGroup.removeView(menuManageConversationView);
                setStatusBarColor();
            }
        };

        menuManageConversationView.setObserver(observer);
        viewGroup.addView(menuManageConversationView);

        List<UIMenuSelectAction> actions = new ArrayList<>();
        actions.add(new UIMenuSelectAction(getString(R.string.show_contact_activity_cleanup), R.drawable.cleanup_icon));
        actions.add(new UIMenuSelectAction(getString(R.string.show_contact_activity_export_contents), R.drawable.share_icon));
        menuManageConversationView.setActions(actions, this);
        menuManageConversationView.openMenu(false);

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void onPreviewFile(List<Uri> uris, boolean fromDirectShare) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPreviewFile: " + uris);
        }

        Intent intent = new Intent(this, PreviewFileActivity.class);

        ArrayList<String> urisToString = new ArrayList<>();
        for (Uri uri : uris) {
            urisToString.add(uri.toString());
        }

        intent.putStringArrayListExtra(Intents.INTENT_SELECTED_URI, urisToString);

        if (mContactId != null || mGroupId != null) {
            intent.putExtra(Intents.INTENT_CONTACT_ID, mContactId != null ? mContactId.toString() : mGroupId.toString());
        }

        if (mEditText.getText() != null) {
            intent.putExtra(Intents.INTENT_TEXT_MESSAGE, mEditText.getText().toString());
        }

        if (mSharedText != null) {
            intent.putExtra(Intents.INTENT_TEXT_MESSAGE, mSharedText.toString());
        }

        if (fromDirectShare) {
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        }

        startActivityForResult(intent, REQUEST_PREVIEW_FILE);
    }

    private void onResetConversationConfirmClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResetConversationConfirmClick");
        }

        Spanned message = Html.fromHtml(getString(R.string.main_activity_reset_conversation_message));
        if (mSubject != null && mSubject.isGroup()) {
            Group group = (Group) mSubject;
            if (group.isOwner()) {
                message = Html.fromHtml(getString(R.string.main_activity_reset_group_conversation_admin_message));
            } else {
                message = Html.fromHtml(getString(R.string.main_activity_reset_group_conversation_message));
            }
        }

        ViewGroup viewGroup = findViewById(R.id.conversation_activity_layout);

        ResetConversationConfirmView resetConversationConfirmView = new ResetConversationConfirmView(this, null);
        resetConversationConfirmView.setAvatar(mContactAvatar, mContactAvatar == null || mContactAvatar.equals(getTwinmeApplication().getDefaultGroupAvatar()));
        resetConversationConfirmView.setMessage(message.toString());

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                mConversationService.resetConversation();
                resetConversationConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                resetConversationConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                resetConversationConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(resetConversationConfirmView);
                setStatusBarColor();
            }
        };
        resetConversationConfirmView.setObserver(observer);
        viewGroup.addView(resetConversationConfirmView);
        resetConversationConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    /**
     * Called when a media button is pressed.
     *
     * @param keyEvent the media button being pressed.
     */
    @Override
    public void onMediaButton(@NonNull KeyEvent keyEvent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMediaButton keyEvent=" + keyEvent);
        }
    }

    @Override
    public void onAudioDeviceChanged(@NonNull final AudioDevice device, @NonNull final Set<AudioDevice> availableDevices) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAudioDeviceChanged: " + availableDevices + ", " + "selected: " + device);
        }
    }

    @Override
    public void onPlayerReady() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPlayerReady");
        }
    }

    private void saveMediaInGallery() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveMediaInGallery");
        }

        if (mSelectedItem != null) {
            String path = mSelectedItem.getPath();
            File file = new File(getTwinmeContext().getFilesDir(), path);
            SaveAsyncTask save = new SaveAsyncTask(this, file, uriFromPath(path));
            save.execute();
            closeMenu();
        }
    }

    private void onCancelSelectItemModeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCancelSelectItemModeClick");
        }

        if (mMenu != null) {
            MenuItem menuAudioItem = mMenu.findItem(R.id.audio_call_action);
            MenuItem menuVideoItem = mMenu.findItem(R.id.video_call_action);
            MenuItem menuCancelItem = mMenu.findItem(R.id.cancel_action);

            menuAudioItem.setVisible(true);
            menuVideoItem.setVisible(true);
            menuCancelItem.setVisible(false);
        }

        mSelectItemMode = false;
        mItemSelectedActionView.setVisibility(View.GONE);
        resetSelectedItems();

        mEditToolbarView.setVisibility(View.VISIBLE);
        mSendClickableView.setVisibility(View.VISIBLE);
        mRecordAudioClickableView.setVisibility(View.VISIBLE);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mItemListView.getLayoutParams();
        marginLayoutParams.bottomMargin = 0;

        mItemListAdapter.notifyDataSetChanged();
        setSelectedMode(Mode.DEFAULT);

        setStatusBarColor();
    }

    private void saveFile() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveFile");
        }

        if (mSelectedItem != null) {
            String path = mSelectedItem.getPath();
            File fileToSave = new File(getTwinmeContext().getFilesDir(), path);

            String mimeType = getContentResolver().getType(Uri.fromFile(fileToSave));
            if (mimeType == null) {
                mimeType = URLConnection.guessContentTypeFromName(fileToSave.getPath());
            }

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(mimeType);
            switch (mSelectedItem.getType()) {
                case FILE: {
                    FileItem fileItem = (FileItem) mSelectedItem;
                    NamedFileDescriptor namedFileDescriptor = fileItem.getNamedFileDescriptor();
                    intent.putExtra(Intent.EXTRA_TITLE, namedFileDescriptor.getName());
                    break;
                }
                case PEER_FILE: {
                    PeerFileItem fileItem = (PeerFileItem) mSelectedItem;
                    NamedFileDescriptor namedFileDescriptor = fileItem.getNamedFileDescriptor();
                    intent.putExtra(Intent.EXTRA_TITLE, namedFileDescriptor.getName());
                    break;
                }
                default: {
                    intent.putExtra(Intent.EXTRA_TITLE, fileToSave.getName());
                    break;
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, getTwinmeApplication().defaultUriToSaveFiles());
            }

            startActivityForResult(intent, REQUEST_CREATE_DOCUMENT);
        }
    }

    private void saveFileToUri(Uri uri) {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveFileToUri " + uri);
        }

        if (mSelectedItem != null) {
            File path = new File(getTwinmeContext().getFilesDir(), mSelectedItem.getPath());
            SaveAsyncTask save = new SaveAsyncTask(this, path, uri);
            save.execute();
            closeMenu();
        }
    }

    private void initTypingTimer() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initTypingTimer");
        }

        if (mTypingTimer != null) {
            mTypingTimer.cancel(false);
        }
        mTypingTimer = getTwinmeContext().getJobService().schedule(this::typingTimeout, TYPING_TIMER_DURATION);
    }

    private void typingTimeout() {
        if (DEBUG) {
            Log.d(LOG_TAG, "typingTimeout");
        }

        mIsTyping = false;
        if (mSendAllowed) {
            Typing typing = new Typing(Typing.Action.STOP);
            mConversationService.pushTyping(typing);
        }

        mTypingTimer = null;
    }

    private void initPeerTypingTimer() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initPeerTypingTimer");
        }

        if (mPeerTypingTimer != null) {
            mPeerTypingTimer.cancel(false);
        }

        mPeerTypingTimer = getTwinmeContext().getJobService().schedule(this::peerTypingTimeout, TYPING_PEER_TIMER_DURATION);
    }

    private void peerTypingTimeout() {
        if (DEBUG) {
            Log.d(LOG_TAG, "peerTypingTimeout");
        }

        mPeerTypingTimer = null;
        if (mUIInitialized) {
            // Cleanup the peer typing from the main UI thread.
            mItemListView.post(() -> {
                if (mIsPeerTyping) {
                    mIsPeerTyping = false;
                    mTypingOriginators.clear();
                    mTypingOriginatorsImages.clear();

                    mItemListAdapter.notifyItemRemoved(mItemListAdapter.getItemCount() - 2);
                }
            });
        }
    }

    private boolean isShareItem(Item item) {
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

    private Uri uriFromPath(String path) {
        if (DEBUG) {
            Log.d(LOG_TAG, "uriFromPath: " + path);
        }

        File file = new File(getTwinmeContext().getFilesDir(), path);
        return FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
    }

    private int getMenuItemViewHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getMenuItemViewHeight");
        }

        if (mSelectedItem == null) {
            return 0;
        }

        int menuHeight = MENU_HEIGHT;
        switch (mSelectedItem.getType()) {
            case MESSAGE:
                menuHeight = MENU_HEIGHT * 8;
                break;
            case PEER_MESSAGE:
            case LINK:
            case PEER_LINK:
            case IMAGE:
            case PEER_IMAGE:
            case VIDEO:
            case PEER_VIDEO:
            case AUDIO:
            case PEER_AUDIO:
            case FILE:
            case PEER_FILE:
                menuHeight = MENU_HEIGHT * 7;
                break;

            case INVITATION:
            case PEER_INVITATION:
            case INVITATION_CONTACT:
            case PEER_INVITATION_CONTACT:
            case CALL:
            case PEER_CALL:
            case CLEAR:
            case PEER_CLEAR:
                menuHeight = MENU_HEIGHT * 3;
                break;

            default:
                break;
        }

        return menuHeight;
    }

    private void showCoachMark() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showCoachMark");
        }

        if (getTwinmeApplication().showCoachMark(CoachMark.CoachMarkTag.CONVERSATION_EPHEMERAL)) {
            mCoachMarkView.postDelayed(() -> {
                mCoachMarkView.setVisibility(View.VISIBLE);
                CoachMark coachMark = new CoachMark(getString(R.string.conversation_activity_ephemeral_coach_mark), CoachMark.CoachMarkTag.CONVERSATION_EPHEMERAL, false, true, new Point((int) (mSendClickableView.getX() + ((float) (mSendClickableView.getWidth() - mSendClickableView.getHeight()) / 2)), (int) mSendClickableView.getY()), mSendClickableView.getHeight(), mSendClickableView.getHeight(), mSendClickableView.getHeight() * 0.5f);
                mCoachMarkView.openCoachMark(coachMark);
            }, COACH_MARK_DELAY);
        }
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void updateScrollIndicator() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateScrollIndicator");
        }

        // We want to display the indicator if the user scrolled up "a lot",
        // or if they're near the bottom and have unread messages.
        int diffScroll = mItemListView.computeVerticalScrollRange() - mItemListView.computeVerticalScrollOffset();
        if (diffScroll > mItemListView.getHeight() * 2 || (!isLastItemVisible() && mScrollIndicatorCount > 0)) {
            mScrollIndicatorView.setVisibility(View.VISIBLE);
            LayoutParams layoutParams = mScrollIndicatorView.getLayoutParams();
            if (mScrollIndicatorCount == 0) {
                mScrollIndicatorCountView.setText("");
                mScrollIndicatorCountView.setVisibility(View.GONE);
                layoutParams.width = (int) ((DESIGN_SCROLL_INDICATOR_MARGIN * Design.WIDTH_RATIO * 2) + (DESIGN_SCROLL_INDICATOR_IMAGE_HEIGHT * Design.HEIGHT_RATIO));
            } else {
                mScrollIndicatorCountView.setText(String.format("%d", mScrollIndicatorCount));
                mScrollIndicatorCountView.setVisibility(View.VISIBLE);
                layoutParams.width = (int) (DESIGN_SCROLL_INDICATOR_WIDTH * Design.WIDTH_RATIO);
            }
        } else {
            mScrollIndicatorCount = 0;
            mScrollIndicatorView.setVisibility(View.GONE);
        }
    }

    private void startFullscreenMediaActivity(DescriptorId descriptorId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "startFullscreenMediaActivity");
        }

        if (mConversation == null) {
            return;
        }
        List<Item> medias = new ArrayList<>();
        for (Item item : mItems) {
            if (item.getType() == Item.ItemType.IMAGE || item.getType() == Item.ItemType.PEER_IMAGE
            || item.getType() == Item.ItemType.VIDEO || item.getType() == Item.ItemType.PEER_VIDEO) {
                medias.add(item);
            }
        }

        int index = 0;
        int itemIndex = 0;
        StringBuilder stringBuilder = new StringBuilder();
        for (Item item : medias) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(item.getDescriptorId());

            if (item.getDescriptorId().equals(descriptorId)) {
                itemIndex = index;
            }
            index++;
        }

        if (!medias.isEmpty()) {
            Item item = medias.get(itemIndex);
            if (item.needsUpdateReadTimestamp() && item.isPeerItem()) {
                markDescriptorRead(item.getDescriptorId());
            }
        }

        Intent intent = new Intent(this, FullscreenMediaActivity.class);
        intent.putExtra(Intents.INTENT_DESCRIPTOR_ID, stringBuilder.toString());
        intent.putExtra(Intents.INTENT_ITEM_INDEX, itemIndex);
        if (mGroupId != null) {
            intent.putExtra(Intents.INTENT_GROUP_ID, mGroupId.toString());
        } else {
            intent.putExtra(Intents.INTENT_CONTACT_ID, mContactId.toString());
        }
        startActivity(intent);
    }

    @Override
    public void updateInCall() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateInCall");
        }

        if (mMenu != null) {
            MenuItem menuAudioItem = mMenu.findItem(R.id.audio_call_action);
            MenuItem menuVideoItem = mMenu.findItem(R.id.video_call_action);

            boolean hasAudio = true;
            boolean hasVideo = true;

            if(mSubject != null) {
                if (mSubject.getType() == Originator.Type.GROUP) {
                    int count = mGroupMembers.size() + 1;
                    if (count == 1 || count > Settings.MAX_CALL_GROUP_PARTICIPANTS) {
                        hasAudio = false;
                        hasVideo = false;
                    }
                } else {
                    if (!mSubject.hasPrivatePeer()) {
                        hasAudio = false;
                        hasVideo = false;
                    } else {
                        hasAudio = mSubject.getCapabilities().hasAudio();
                        hasVideo = mSubject.getCapabilities().hasVideo();
                    }
                }
            }

            if (menuAudioItem != null) {
                if (getTwinmeApplication().inCallInfo() != null || !hasAudio) {
                    menuAudioItem.getActionView().setAlpha(0.5f);
                    menuAudioItem.setEnabled(false);
                } else {
                    menuAudioItem.getActionView().setAlpha(1.0f);
                    menuAudioItem.setEnabled(true);
                }
            }

            if (menuVideoItem != null) {
                if (getTwinmeApplication().inCallInfo() != null || !hasVideo) {
                    menuVideoItem.getActionView().setAlpha(0.5f);
                    menuVideoItem.setEnabled(false);
                } else {
                    menuVideoItem.getActionView().setAlpha(1.0f);
                    menuVideoItem.setEnabled(true);
                }
            }
        }
    }

    private void openAnnotationsView(List<UIAnnotation> uiAnnotations) {
        if (DEBUG) {
            Log.d(LOG_TAG, "openAnnotationsView" + uiAnnotations);
        }

        hideKeyboard();
        mAnnotationsView.setVisibility(View.VISIBLE);
        mOverlayView.setVisibility(View.VISIBLE);
        mAnnotationsView.open(uiAnnotations);

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void showCallAgainConfirmView(boolean isVideoCall) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showCallAgainConfirmView: isVideoCall=" + isVideoCall);
        }

        hideKeyboard();

        if (mGroupId != null) {
            showPremiumFeatureView(UIPremiumFeature.FeatureType.GROUP_CALL, false);
            return;
        }

        ViewGroup viewGroup = findViewById(R.id.conversation_activity_layout);

        CallAgainConfirmView callAgainConfirmView = new CallAgainConfirmView(this, null);

        if (mSubject != null) {
            callAgainConfirmView.setTitle(mSubject.getName());
        }

        callAgainConfirmView.setAvatar(mContactAvatar, mContactAvatar == null || mContactAvatar.equals(getTwinmeApplication().getDefaultGroupAvatar()));

        if (isVideoCall) {
            callAgainConfirmView.setMessage(getString(R.string.conversation_activity_video_call));
            callAgainConfirmView.setIcon(R.drawable.video_call);
        } else {
            callAgainConfirmView.setMessage(getString(R.string.conversation_activity_audio_call));
            callAgainConfirmView.setIcon(R.drawable.audio_call);
        }

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {

                Intent intent = new Intent(getApplicationContext(), CallActivity.class);

                if (mSubject.isGroup()) {
                    intent.putExtra(Intents.INTENT_GROUP_ID, mSubject.getId().toString());
                } else {
                    intent.putExtra(Intents.INTENT_CONTACT_ID, mSubject.getId().toString());
                }

                if (isVideoCall) {
                    intent.putExtra(Intents.INTENT_CALL_MODE, CallStatus.OUTGOING_VIDEO_CALL);
                } else {
                    intent.putExtra(Intents.INTENT_CALL_MODE, CallStatus.OUTGOING_CALL);
                }

                startActivity(intent);

                callAgainConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                callAgainConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                callAgainConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(callAgainConfirmView);
                setStatusBarColor();
            }
        };
        callAgainConfirmView.setObserver(observer);
        viewGroup.addView(callAgainConfirmView);
        callAgainConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void showPremiumFeatureView(UIPremiumFeature.FeatureType featureType, boolean hideOverlay) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showPremiumFeatureView: " + featureType);
        }

        ViewGroup viewGroup = findViewById(R.id.conversation_activity_layout);

        PremiumFeatureConfirmView premiumFeatureConfirmView = new PremiumFeatureConfirmView(this, null);
        premiumFeatureConfirmView.initWithPremiumFeature(new UIPremiumFeature(this, featureType));

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
                viewGroup.removeView(premiumFeatureConfirmView);

                if (!mIsMenuSendOptionOpen) {
                    setStatusBarColor();
                }
            }
        };
        premiumFeatureConfirmView.setObserver(observer);
        viewGroup.addView(premiumFeatureConfirmView);
        premiumFeatureConfirmView.show();

        if (hideOverlay) {
            premiumFeatureConfirmView.hideOverlay();
        }

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void addBlurEffect() {
        if (DEBUG) {
            Log.d(LOG_TAG, "removeBlurEffect");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mTopBlurView.setBackgroundColor(Design.TOOLBAR_COLOR);
            mTopBlurContainerView.setVisibility(View.VISIBLE);

            RenderEffect blurEffect = RenderEffect.createBlurEffect(16.0f, 16.0f, Shader.TileMode.CLAMP);
            Toolbar toolbar = findViewById(R.id.conversation_activity_top_tool_bar);
            toolbar.setRenderEffect(blurEffect);
            mTopBlurContainerView.setRenderEffect(blurEffect);
            mItemListView.setRenderEffect(blurEffect);
            mFooterView.setRenderEffect(blurEffect);
            mEmptyConversationView.setRenderEffect(blurEffect);
        }
    }

    private void removeBlurEffect() {
        if (DEBUG) {
            Log.d(LOG_TAG, "removeBlurEffect");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mTopBlurContainerView.setVisibility(View.GONE);
            Toolbar toolbar = findViewById(R.id.conversation_activity_top_tool_bar);
            toolbar.setRenderEffect(null);
            mItemListView.setRenderEffect(null);
            mFooterView.setRenderEffect(null);
            mTopBlurContainerView.setRenderEffect(null);
            mEmptyConversationView.setRenderEffect(null);
        }
    }

    private void updateGroupPermissions() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateGroupPermissions");
        }

        if (!mSendAllowed) {
            mRecordAudioClickableView.setAlpha(0.5f);
            mCameraClickableView.setAlpha(0.5f);
            mSendClickableView.setAlpha(0.5f);
            mEditText.setPadding((int) (DESIGN_EDIT_TEXT_WIDTH_INSET * Design.WIDTH_RATIO), (int) (DESIGN_EDIT_TEXT_HEIGHT_INSET * Design.HEIGHT_RATIO), (int) (DESIGN_EDITBAR_HEIGHT * Design.HEIGHT_RATIO * 2), (int) (DESIGN_EDIT_TEXT_HEIGHT_INSET * Design.HEIGHT_RATIO));
            mEditText.setEnabled(false);
            mEditText.setHint(getString(R.string.conversation_activity_group_not_allowed_post_message));
        } else {
            mRecordAudioClickableView.setAlpha(1.0f);
            mCameraClickableView.setAlpha(1.0f);
            mSendClickableView.setAlpha(1.0f);
            mEditText.setPadding((int) (DESIGN_EDIT_TEXT_WIDTH_INSET * Design.WIDTH_RATIO), (int) (DESIGN_EDIT_TEXT_HEIGHT_INSET * Design.HEIGHT_RATIO), (int) (DESIGN_EDIT_TEXT_WIDTH_INSET * Design.WIDTH_RATIO), (int) (DESIGN_EDIT_TEXT_HEIGHT_INSET * Design.HEIGHT_RATIO));
            mEditText.setEnabled(true);
            mEditText.setHint(getString(R.string.conversation_activity_message));
        }
    }

    @NonNull
    private String getTypedText() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getTypedText");
        }

        if (mSharedPreferences == null) {
            return "";
        }

        String key = TYPED_TEXT;
        if (mContactId != null) {
            key += "_" + mContactId;
        } else if (mGroupId != null) {
            key += "_" + mGroupId;
        }

        return mSharedPreferences.getString(key, "");
    }

    private void saveTypedText() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveTypedText");
        }

        if (mSharedPreferences == null) {
            return;
        }

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        String key = TYPED_TEXT;
        if (mContactId != null) {
            key += "_" + mContactId;
        } else if (mGroupId != null) {
            key += "_" + mGroupId;
        }

        if (isEmptyText()) {
            editor.remove(key);
        } else {
            editor.putString(key, getSendText());
        }

        editor.apply();
    }

    private void saveShortCutText(String text) {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveShortCutText " + text);
        }

        if (mSharedPreferences == null) {
            return;
        }

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        String key = TYPED_TEXT;
        if (mContactId != null) {
            key += "_" + mContactId;
        } else if (mGroupId != null) {
            key += "_" + mGroupId;
        }

        editor.putString(key, text);
        editor.apply();
    }

    private void handleShortcut(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "handleShortcut: intent=" + intent);
        }

        String shortcutId = intent.getStringExtra(ShortcutManagerCompat.EXTRA_SHORTCUT_ID);

        if (shortcutId == null) {
            return;
        }

        try {
            Originator.Type type = Originator.Type.valueOf(shortcutId.split("_")[0]);
            if (type == Originator.Type.CONTACT) {
                mContactId = Utils.UUIDFromString(shortcutId.split("_")[1]);
            } else {
                mGroupId = Utils.UUIDFromString(shortcutId.split("_")[1]);
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, "Invalid share target ID: " + shortcutId);
            return;
        }

        handleDirectShare(intent);
    }

    private void handleDirectShare(@NonNull Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "handleDirectShare: intent=" + intent);
        }

        getTwinmeContext().execute(() -> {

            CharSequence sharedText = ShareUtils.getSharedText(intent);
            List<Uri> sharedUris = ShareUtils.getSharedFiles(intent);

            boolean mediaOnly = true;
            for (Uri uri : sharedUris) {
                String type = getContentResolver().getType(uri);
                if (type == null || (!type.startsWith("image/") && !type.startsWith("video/"))) {
                    mediaOnly = false;
                    break;
                }
            }

            final boolean finalMediaOnly = mediaOnly;
            runOnUiThread(() -> {
                if (!TextUtils.isEmpty(sharedText)) {
                    if (mInitToolbar) {
                        mEditText.setText(sharedText);
                        saveShortCutText(sharedText.toString());
                    } else {
                        mSharedText = sharedText;
                    }
                }

                if (!sharedUris.isEmpty()) {
                    if (finalMediaOnly) {
                        onPreviewMedia(sharedUris, true);
                    } else {
                        onPreviewFile(sharedUris, true);
                    }
                }
            });

        });
    }

    private void backPressed() {
        if (DEBUG) {
            Log.d(LOG_TAG, "backPressed");
        }

        // When the keyboard is opened, the back button closes the keyboard.
        // When the media selector is opened, close it to have the same behavior.
        if (mSelectedMode != Mode.DEFAULT) {
            setSelectedMode(Mode.DEFAULT);
            return;
        }

        if (mMenuActionConversationView != null) {
            ViewGroup viewGroup = findViewById(R.id.conversation_activity_layout);
            viewGroup.removeView(mMenuActionConversationView);
            mMenuActionConversationView = null;
            setStatusBarColor();
            removeBlurEffect();

            return;
        }

        finish();
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        super.setupDesign();

        REPLY_HEIGHT = (int) (DESIGN_REPLY_HEIGHT * Design.HEIGHT_RATIO);
        MENU_WIDTH = (int) (DESIGN_MENU_WIDTH * Design.WIDTH_RATIO);
        MENU_HEIGHT = (int) (DESIGN_MENU_ACTION_HEIGHT * Design.HEIGHT_RATIO);
        MENU_REACTION_HEIGHT = (int) (DESIGN_MENU_REACTION_HEIGHT * Design.HEIGHT_RATIO);
        AVATAR_VIEW_HEIGHT = (int) (DESIGN_AVATAR_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        AVATAR_MARGIN = (int) (DESIGN_AVATAR_MARGIN * Design.HEIGHT_RATIO);
    }
}
