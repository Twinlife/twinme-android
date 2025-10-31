/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.services.ConversationFilesService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.baseItemActivity.FileItem;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.ui.baseItemActivity.LinkItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerFileItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerLinkItem;
import org.twinlife.twinme.ui.contacts.DeleteConfirmView;
import org.twinlife.twinme.ui.conversationActivity.NamedFileProvider;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.FileInfo;
import org.twinlife.twinme.utils.async.Loader;
import org.twinlife.twinme.utils.async.LoaderListener;
import org.twinlife.twinme.utils.async.Manager;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

public class ConversationFilesActivity extends AbstractFilesActivity implements LoaderListener<Item>, ItemSelectedActionView.Observer, CustomTabView.Observer {
    private static final String LOG_TAG = "ConversationFilesAc...";
    private static final boolean DEBUG = false;

    private static final float DESIGN_SELECTED_VIEW_HEIGHT = 128f;
    private static final float DESIGN_CUSTOM_TAB_VIEW_HEIGHT = 148f;

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;

    private ConversationFilesAdapter mConversationFilesAdapter;

    private UICustomTab.CustomTabType mCustomTabTypeSelect = UICustomTab.CustomTabType.IMAGE;

    private RecyclerView mFilesRecyclerView;
    private ItemSelectedActionView mItemSelectedActionView;

    private ImageView mNoItemFoundImageView;
    private TextView mNoItemTitleView;

    private Menu mMenu;

    private List<UIFileSection> mFileSections = new ArrayList<>();
    private final List<Item> mSelectedItems = new ArrayList<>();

    private UUID mContactId;
    private UUID mGroupId;
    private Bitmap mAvatar;

    @Nullable
    private Manager<Item> mAsyncItemLoader;

    private boolean mIsSelectMode = false;

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

        Intent intent = getIntent();
        mContactId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));
        mGroupId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_GROUP_ID));
        if (mContactId == null && mGroupId == null) {
            finish();
            return;
        }

        mConversationFilesService = new ConversationFilesService(this, getTwinmeContext(), this,
                null, mContactId, mGroupId);
        mAsyncItemLoader = new Manager<>(this, getTwinmeContext(), this);
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

        super.onDestroy();

        if (mAsyncItemLoader != null) {
            mAsyncItemLoader.stop();
        }
        if (mConversationFilesService != null) {
            mConversationFilesService.dispose();
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
        inflater.inflate(R.menu.conversation_files_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.select_action);

        TextView titleView = (TextView) menuItem.getActionView();
        String title = menuItem.getTitle().toString();

        if (titleView != null) {
            Design.updateTextFont(titleView, Design.FONT_BOLD36);
            titleView.setTextColor(Color.WHITE);
            titleView.setText(title);
            titleView.setPadding(0, 0, Design.TOOLBAR_TEXT_ITEM_PADDING, 0);
            titleView.setOnClickListener(view -> onSelectModeClick());
        }

        return true;
    }

    public UICustomTab.CustomTabType getCustomTabType() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getCustomTabType");
        }

        return mCustomTabTypeSelect;
    }

    public void loadMoreDescriptors() {
        if (DEBUG) {
            Log.d(LOG_TAG, "loadMoreDescriptors");
        }

        mConversationFilesService.getPreviousObjectDescriptors();
    }

    public boolean isSelectMode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isSelectMode");
        }

        return mIsSelectMode;
    }

    public void onItemClick(Item item) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onItemClick: " + item);
        }

        if (mIsSelectMode) {
            if (item.isSelected()) {
                item.setSelected(false);
                mSelectedItems.remove(item);
            } else {
                item.setSelected(true);
                mSelectedItems.add(item);
            }

            mItemSelectedActionView.updateSelectedItems(mSelectedItems.size());

            String periodKey = getPeriodKey(item);
            for (UIFileSection fileSection : mFileSections) {
                if (fileSection.getPeriod().equals(periodKey)) {
                    mConversationFilesAdapter.updateItemInFileSection(fileSection);
                    break;
                }
            }
        } else {
            switch (item.getType()) {
                case IMAGE:
                case PEER_IMAGE:
                case VIDEO:
                case PEER_VIDEO: {
                    startFullscreenMediaActivity(item);
                    break;
                }

                case LINK:
                case PEER_LINK: {
                    URL url;
                    if (item.isPeerItem()) {
                        PeerLinkItem peerLinkItem = (PeerLinkItem) item;
                        url = peerLinkItem.getUrl();
                    } else {
                        LinkItem linkItem = (LinkItem) item;
                        url = linkItem.getUrl();
                    }

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url.toString()));
                    startActivity(intent);
                    break;
                }

                case FILE:
                case PEER_FILE: {
                    try {
                        ConversationService.NamedFileDescriptor namedFileDescriptor;

                        if (item.isPeerItem()) {
                            PeerFileItem peerFileItem = (PeerFileItem) item;
                            namedFileDescriptor = peerFileItem.getNamedFileDescriptor();
                        } else {
                            FileItem fileItem = (FileItem) item;
                            namedFileDescriptor = fileItem.getNamedFileDescriptor();
                        }

                        String path = namedFileDescriptor.getPath();
                        File file = new File(getTwinmeContext().getFilesDir(), path);

                        Uri uri = NamedFileProvider.getInstance().getUriForFile(this, new File(file.getPath()), namedFileDescriptor.getName());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        startActivity(intent);
                    } catch (Exception exception) {
                        Log.e(LOG_TAG, "onItemClick() exception=" + exception);
                    }
                    break;
                }

                default:
                    break;
            }
        }
    }

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

        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
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
    }

    @Override
    public void onDeleteActionClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteActionClick");
        }

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.conversation_files_activity_layout);

        DeleteConfirmView deleteConfirmView = new DeleteConfirmView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        deleteConfirmView.setLayoutParams(layoutParams);
        deleteConfirmView.setAvatar(mAvatar, mAvatar == null || mAvatar.equals(getTwinmeApplication().getDefaultGroupAvatar()));
        deleteConfirmView.setMessage(getString(R.string.cleanup_activity_delete_confirmation_message));

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                deleteItems();
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
                percentRelativeLayout.removeView(deleteConfirmView);
                setStatusBarColor();
            }
        };
        deleteConfirmView.setObserver(observer);

        percentRelativeLayout.addView(deleteConfirmView);
        deleteConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    @Override
    public void onLoaded(@NonNull List<Item> list) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLoaded");
        }

        Set<Item> toUpdate = new HashSet<>(list);

        for (int index = mItems.size() - 1; index >= 0; index--) {
            Item lItem = mItems.get(index);
            if (toUpdate.remove(lItem)) {
                String periodKey = getPeriodKey(lItem);
                for (UIFileSection fileSection : mFileSections) {
                    if (fileSection.getPeriod().equals(periodKey)) {
                        mConversationFilesAdapter.updateItemInFileSection(fileSection);
                        break;
                    }
                }

                if (toUpdate.isEmpty()) {
                    break;
                }
            }
        }
    }

    public void addLoadableItem(@NonNull final Loader<Item> item) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addLoadableItem: item=" + item);
        }

        mAsyncItemLoader.addItem(item);
    }

    //
    // CustomTabView.Observer implements methods
    //

    @Override
    public void onSelectCustomTab(UICustomTab customTab) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectCustomTab: " + customTab);
        }

        mCustomTabTypeSelect = customTab.getCustomTabType();

        reloadData();
    }

    //
    // Override implements methods
    //

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: " + contact);
        }


        String name = contact.getName();
        mNoItemTitleView.setText(String.format(getString(R.string.conversation_files_activity_no_files), name));
        setTitle(name);
        mAvatar = avatar;
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: " + contact);
        }

        String name = contact.getName();
        mNoItemTitleView.setText(String.format(getString(R.string.conversation_files_activity_no_files), name));
        setTitle(name);
        mAvatar = avatar;
    }

    @Override
    public void onGetGroup(@NonNull Group group, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup: " + group);
        }

        String name = group.getName();
        mNoItemTitleView.setText(String.format(getString(R.string.conversation_files_activity_no_files), name));
        setTitle(name);
        mAvatar = avatar;
    }

    @Override
    public void onGetDescriptors(@NonNull List<ConversationService.Descriptor> descriptors) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDescriptors: " + descriptors);
        }

        for (ConversationService.Descriptor descriptor : descriptors) {
            switch (descriptor.getType()) {
                case OBJECT_DESCRIPTOR:
                    ConversationService.ObjectDescriptor objectDescriptor = (ConversationService.ObjectDescriptor) descriptor;
                    addObjectDescriptor(objectDescriptor);
                    break;

                case IMAGE_DESCRIPTOR:
                    ConversationService.ImageDescriptor imageDescriptor = (ConversationService.ImageDescriptor) descriptor;
                    addImageDescriptor(imageDescriptor);
                    break;

                case VIDEO_DESCRIPTOR:
                    ConversationService.VideoDescriptor videoDescriptor = (ConversationService.VideoDescriptor) descriptor;
                    addVideoDescriptor(videoDescriptor);
                    break;

                case NAMED_FILE_DESCRIPTOR:
                    ConversationService.NamedFileDescriptor namedFileDescriptor = (ConversationService.NamedFileDescriptor) descriptor;
                    if (namedFileDescriptor.isAvailable()) {
                        addNamedFileDescriptor(namedFileDescriptor);
                    }
                    break;

                default:
                    break;
            }
        }

        reloadData();
    }

    @Override
    public void onMarkDescriptorDeleted(@NonNull ConversationService.Descriptor descriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMarkDescriptorDeleted: " + descriptor);
        }

        if (!mIsSelectMode) {
            for (Item item : mItems) {
                if (item.getDescriptorId().equals(descriptor.getDescriptorId())) {
                    mItems.remove(item);
                    break;
                }
            }
            reloadData();
        } else {
            for (Item item : mSelectedItems) {
                if (item.getDescriptorId().equals(descriptor.getDescriptorId())) {
                    mItems.remove(item);
                    mSelectedItems.remove(item);
                    break;
                }
            }

            if (mSelectedItems.isEmpty()) {
                mItemSelectedActionView.updateSelectedItems(0);
                reloadData();
            }
        }
    }

    @Override
    public void onDeleteDescriptors(@NonNull Set<ConversationService.DescriptorId> descriptorList) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteDescriptors: " + descriptorList);
        }

        if (!mIsSelectMode) {
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

            reloadData();
        } else {
            Iterator<Item> iterator = mItems.iterator();
            while (iterator.hasNext()) {
                Item item = iterator.next();
                ConversationService.DescriptorId descriptorId = item.getDescriptorId();
                if (descriptorList.remove(descriptorId)) {
                    iterator.remove();
                    mSelectedItems.remove(item);
                    if (descriptorList.isEmpty()) {
                        break;
                    }
                }
            }

            if (mSelectedItems.isEmpty()) {
                mItemSelectedActionView.updateSelectedItems(0);
                reloadData();
            }
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
        setContentView(R.layout.conversation_files_activity);

        setStatusBarColor();
        setToolBar(R.id.conversation_files_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        setTitle(getString(R.string.conversation_files_activity_title));
        applyInsets(R.id.conversation_files_activity_layout, R.id.conversation_files_activity_tool_bar, R.id.conversation_files_activity_list_view, Design.TOOLBAR_COLOR, false);

        mNoItemFoundImageView = findViewById(R.id.conversation_files_activity_no_item_found_image_view);

        mNoItemTitleView = findViewById(R.id.conversation_files_activity_no_item_title_view);
        Design.updateTextFont(mNoItemTitleView, Design.FONT_MEDIUM34);
        mNoItemTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mConversationFilesAdapter = new ConversationFilesAdapter(this, mFileSections);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mFilesRecyclerView = findViewById(R.id.conversation_files_activity_list_view);
        mFilesRecyclerView.setLayoutManager(linearLayoutManager);
        mFilesRecyclerView.setAdapter(mConversationFilesAdapter);
        mFilesRecyclerView.setItemAnimator(null);
        mFilesRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mFilesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (mFileSections != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == mFileSections.size() - 1) {
                        mConversationFilesService.getPreviousObjectDescriptors();
                    }
                }
            }
        });

        initTabs();

        mItemSelectedActionView = findViewById(R.id.conversation_files_activity_item_selected_action_view);
        mItemSelectedActionView.setVisibility(View.GONE);
        mItemSelectedActionView.setObserver(this);

        ViewGroup.LayoutParams layoutParams = mItemSelectedActionView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SELECTED_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        mUIInitialized = true;
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }

    private void initTabs() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initTabs");
        }

        List<UICustomTab> customTabs = new ArrayList<>();
        customTabs.add(new UICustomTab(getString(R.string.export_activity_images), UICustomTab.CustomTabType.IMAGE, true));
        customTabs.add(new UICustomTab(getString(R.string.export_activity_videos), UICustomTab.CustomTabType.VIDEO, false));
        customTabs.add(new UICustomTab(getString(R.string.conversation_files_activity_documents), UICustomTab.CustomTabType.DOCUMENT, false));
        customTabs.add(new UICustomTab(getString(R.string.conversation_files_activity_links), UICustomTab.CustomTabType.LINK, false));

        CustomTabView customTabView = findViewById(R.id.conversation_files_activity_tab_view);

        ViewGroup.LayoutParams layoutParams = customTabView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CUSTOM_TAB_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        customTabView.initTabs(customTabs, this);
    }

    private void addObjectDescriptor(ConversationService.ObjectDescriptor objectDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addObjectDescriptor: objectDescriptor=" + objectDescriptor);
        }

        String message = objectDescriptor.getMessage();
        if (mConversationFilesService.isLocalDescriptor(objectDescriptor)) {
            URL url = org.twinlife.twinme.utils.Utils.extractURLFromString(message);
            if (url != null) {
                LinkItem linkItem = new LinkItem(objectDescriptor, null, url);
                mItems.add(linkItem);
            }
        } else if (mConversationFilesService.isPeerDescriptor(objectDescriptor)) {
            URL url = org.twinlife.twinme.utils.Utils.extractURLFromString(message);
            if (url != null && getTwinmeApplication().visualizationLink()) {
                PeerLinkItem peerLinkItem = new PeerLinkItem(objectDescriptor, null, url);
                mItems.add(peerLinkItem);
            }
        }
    }

    private void addNamedFileDescriptor(ConversationService.NamedFileDescriptor namedFileDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addNamedFileDescriptor: namedFileDescriptor=" + namedFileDescriptor);
        }

        if (mConversationFilesService.isLocalDescriptor(namedFileDescriptor)) {
            FileItem fileItem = new FileItem(namedFileDescriptor, null);
            mItems.add(fileItem);
        } else if (mConversationFilesService.isPeerDescriptor(namedFileDescriptor)) {
            PeerFileItem peerFileItem = new PeerFileItem(namedFileDescriptor, null);
            mItems.add(peerFileItem);
        }
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

    private void onSelectModeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectModeClick");
        }

        if (mIsSelectMode) {
            mIsSelectMode = false;
            mItemSelectedActionView.setVisibility(View.GONE);

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mFilesRecyclerView.getLayoutParams();
            marginLayoutParams.bottomMargin = 0;

            if (mMenu != null) {
                MenuItem selectMenuItem = mMenu.findItem(R.id.select_action);
                TextView titleView = (TextView) selectMenuItem.getActionView();
                titleView.setText(getString(R.string.application_select));
            }

            resetSelectedItems();
        } else {
            mIsSelectMode = true;
            mItemSelectedActionView.setVisibility(View.VISIBLE);

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mFilesRecyclerView.getLayoutParams();
            marginLayoutParams.bottomMargin = (int) (DESIGN_SELECTED_VIEW_HEIGHT * Design.HEIGHT_RATIO);

            if (mMenu != null) {
                MenuItem selectMenuItem = mMenu.findItem(R.id.select_action);
                TextView titleView = (TextView) selectMenuItem.getActionView();
                titleView.setText(getString(R.string.application_cancel));
            }

            mItemSelectedActionView.updateSelectedItems(mSelectedItems.size());
        }

        reloadData();
    }
    
    private boolean isSelectedType(Item item) {
        if (DEBUG) {
            Log.d(LOG_TAG, "isSelectedType");
        }

        if (mCustomTabTypeSelect == UICustomTab.CustomTabType.IMAGE && (item.getType() == Item.ItemType.IMAGE || item.getType() == Item.ItemType.PEER_IMAGE)) {
            return true;
        } else if (mCustomTabTypeSelect == UICustomTab.CustomTabType.VIDEO && (item.getType() == Item.ItemType.VIDEO || item.getType() == Item.ItemType.PEER_VIDEO)) {
            return true;
        } else if (mCustomTabTypeSelect == UICustomTab.CustomTabType.LINK && (item.getType() == Item.ItemType.LINK || item.getType() == Item.ItemType.PEER_LINK)) {
            return true;
        }

        return mCustomTabTypeSelect == UICustomTab.CustomTabType.DOCUMENT && (item.getType() == Item.ItemType.FILE || item.getType() == Item.ItemType.PEER_FILE);
    }

    private void reloadData() {
        if (DEBUG) {
            Log.d(LOG_TAG, "reloadData");
        }

        Map<String, UIFileSection> periods = new HashMap<>();

        for (Item item : mItems) {
            if (isSelectedType(item) && !item.isClearLocalItem()) {
                String periodKey = getPeriodKey(item);
                if (periods.containsKey(periodKey)) {
                    UIFileSection fileSection = periods.get(periodKey);
                    if (fileSection != null) {
                        fileSection.addItem(item);
                    }
                } else {
                    UIFileSection fileSection = new UIFileSection(periodKey);
                    fileSection.addItem(item);
                    periods.put(periodKey, fileSection);
                }
            }
        }

        Map<String, UIFileSection> sortedPeriod = new TreeMap<>(periods);
        mFileSections = new ArrayList<>(sortedPeriod.values());
        Collections.reverse(mFileSections);

        if (mFileSections.isEmpty()) {
            mNoItemFoundImageView.setVisibility(View.VISIBLE);
            mNoItemTitleView.setVisibility(View.VISIBLE);

            if (mMenu != null) {
                MenuItem selectMenuItem = mMenu.findItem(R.id.select_action);
                selectMenuItem.setEnabled(false);
                selectMenuItem.getActionView().setAlpha(0.5f);
            }
        } else {
            mNoItemFoundImageView.setVisibility(View.GONE);
            mNoItemTitleView.setVisibility(View.GONE);

            if (mMenu != null) {
                MenuItem selectMenuItem = mMenu.findItem(R.id.select_action);
                selectMenuItem.setEnabled(true);
                selectMenuItem.getActionView().setAlpha(1.0f);
            }
        }

        mConversationFilesAdapter.setFileSections(mFileSections);

        if (mFileSections.isEmpty()) {
            mConversationFilesService.getPreviousObjectDescriptors();
        }
    }

    private void deleteItems() {
        if (DEBUG) {
            Log.d(LOG_TAG, "deleteItems");
        }

        for (Item item : mSelectedItems) {
            if (item.isPeerItem()) {
                mConversationFilesService.deleteDescriptor(item.getDescriptorId());
            } else {
                mConversationFilesService.markDescriptorDeleted(item.getDescriptorId());
            }
        }
    }

    private String getPeriodKey(Item item) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getPeriodKey: " + item);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        return simpleDateFormat.format(new Date(item.getCreatedTimestamp()));
    }

    private void startFullscreenMediaActivity(Item item) {
        if (DEBUG) {
            Log.d(LOG_TAG, "startFullscreenMediaActivity");
        }

        int index = 0;
        int itemIndex = 0;

        StringBuilder stringBuilder = new StringBuilder();
        for (UIFileSection fileSection : mFileSections) {
            for (Item lItem : fileSection.getItems()) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(",");
                }
                stringBuilder.append(lItem.getDescriptorId());
                if (lItem.getDescriptorId().equals(item.getDescriptorId())) {
                    itemIndex = index;
                }
                index++;
            }
        }

        Intent intent = new Intent(this, FullscreenMediaActivity.class);
        intent.putExtra(Intents.INTENT_DESCRIPTOR_ID, stringBuilder.toString());
        intent.putExtra(Intents.INTENT_ITEM_INDEX, itemIndex);
        if (mContactId != null) {
            intent.putExtra(Intents.INTENT_CONTACT_ID, mContactId.toString());
        }
        if (mGroupId != null) {
            intent.putExtra(Intents.INTENT_GROUP_ID, mGroupId.toString());
        }
        startActivity(intent);
    }
}