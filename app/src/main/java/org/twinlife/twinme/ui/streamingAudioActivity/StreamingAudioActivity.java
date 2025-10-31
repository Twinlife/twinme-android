/*
 *  Copyright (c) 2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.streamingAudioActivity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.conversationActivity.MusicItem;
import org.twinlife.twinme.utils.FileInfo;
import org.twinlife.twinme.utils.MediaMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StreamingAudioActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "StreamingAudioActivity";
    private static final boolean DEBUG = false;

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;
    private StreamingAudioAdapter mStreamingAudioAdapter;
    private EditText mSearchEditText;
    private View mClearSearchView;
    private ImageView mNoMusicFoundImageView;
    private TextView mNoMusicTitleView;
    private TextView mNoMusicTextView;
    private final List<MusicItem> mMusicItems = new ArrayList<>();
    private final List<MusicItem> mFilteredMusicItems = new ArrayList<>();
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private MusicItem mSelectedMusicItem;
    private Handler mHandler;

    private boolean mStorageReadAccessGranted = false;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mHandler = new Handler(Looper.getMainLooper());
        initViews();
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        Permission[] permissions = new Permission[]{ Permission.READ_EXTERNAL_STORAGE, Permission.READ_MEDIA_AUDIO };
        mStorageReadAccessGranted = false;
        if (checkPermissions(permissions)) {
            mStorageReadAccessGranted = true;
            mExecutor.execute(this::getSongs);
        }
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mExecutor.shutdown();
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

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

        if (hasFocus && mUIInitialized && !mUIPostInitialized) {
            postInitViews();
        }
    }

    @Override
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        boolean storageReadAccessGranted = false;
        for (Permission grantedPermission : grantedPermissions) {
            if (grantedPermission == Permission.READ_EXTERNAL_STORAGE || grantedPermission == Permission.READ_MEDIA_AUDIO) {
                storageReadAccessGranted = true;
                break;
            }
        }

        mStorageReadAccessGranted = storageReadAccessGranted;

        if (mStorageReadAccessGranted) {
            mExecutor.execute(this::getSongs);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu);
        }

        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.invitation_room_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.add_action);

        TextView titleView = (TextView) menuItem.getActionView();
        String title = menuItem.getTitle().toString();

        if (titleView != null) {
            Design.updateTextFont(titleView, Design.FONT_BOLD36);
            titleView.setTextColor(Color.WHITE);
            titleView.setText(title);
            titleView.setPadding(0, 0, Design.TOOLBAR_TEXT_ITEM_PADDING, 0);
            titleView.setOnClickListener(view -> onShareClicked());
        }

        return true;
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.streaming_audio_activity);

        setStatusBarColor();
        setToolBar(R.id.streaming_audio_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setTitle(getString(R.string.streaming_audio_activity_title));
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        applyInsets(R.id.streaming_audio_activity_layout, R.id.streaming_audio_activity_tool_bar, R.id.streaming_audio_activity_list_view, Design.TOOLBAR_COLOR, false);

        View searchView = findViewById(R.id.streaming_audio_activity_search_view);
        searchView.setBackgroundColor(Design.TOOLBAR_COLOR);

        ViewGroup.LayoutParams layoutParams = searchView.getLayoutParams();
        layoutParams.height = Design.SEARCH_VIEW_HEIGHT;

        mClearSearchView = findViewById(R.id.streaming_audio_activity_clear_image_view);
        mClearSearchView.setVisibility(View.GONE);
        mClearSearchView.setOnClickListener(v -> {
            mSearchEditText.setText("");
            mClearSearchView.setVisibility(View.GONE);
        });

        mSearchEditText = findViewById(R.id.streaming_audio_activity_search_edit_text_view);
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

                searchSongs(s.toString());
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

        mNoMusicFoundImageView = findViewById(R.id.streaming_audio_activity_no_music_found_image_view);

        mNoMusicTitleView = findViewById(R.id.streaming_audio_activity_no_music_title_view);
        Design.updateTextFont(mNoMusicTitleView, Design.FONT_MEDIUM34);
        mNoMusicTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mNoMusicTextView = findViewById(R.id.streaming_audio_activity_no_music_text_view);
        Design.updateTextFont(mNoMusicTextView, Design.FONT_MEDIUM28);
        mNoMusicTextView.setTextColor(Design.FONT_COLOR_DESCRIPTION);

        LinearLayoutManager streamingAudioLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        RecyclerView songsRecyclerView = findViewById(R.id.streaming_audio_activity_list_view);
        songsRecyclerView.setLayoutManager(streamingAudioLinearLayoutManager);
        songsRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        songsRecyclerView.setItemAnimator(null);
        songsRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        StreamingAudioAdapter.OnSongClickListener onSongClickListener = position -> {

            MusicItem musicItem = mFilteredMusicItems.get(position);
            if (musicItem != null) {
                mSelectedMusicItem = musicItem;
                mStreamingAudioAdapter.setSelectedItem(mSelectedMusicItem);
            }
        };

        mStreamingAudioAdapter = new StreamingAudioAdapter(this, onSongClickListener);
        songsRecyclerView.setAdapter(mStreamingAudioAdapter);

        mProgressBarView = findViewById(R.id.streaming_audio_activity_progress_bar);

        mUIInitialized = true;
    }

    private void getSongs() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getSongs");
        }

        if (mMusicItems.size() > 0) {
            return;
        }

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION};

        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, projection, selection, null, sortOrder);
        if (cursor != null) {
            List<MusicItem> items = new ArrayList<>();
            while(cursor.moveToNext()){
                String title = FileInfo.getColumnString(cursor, MediaStore.Audio.Media.TITLE);
                String artist = FileInfo.getColumnString(cursor, MediaStore.Audio.Media.ARTIST);
                long albumId = FileInfo.getColumnLong(cursor, MediaStore.Audio.Media.ALBUM_ID);
                String album = FileInfo.getColumnString(cursor, MediaStore.Audio.Media.ALBUM);
                Uri artworkUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
                Bitmap artwork;
                try {
                    artwork = MediaStore.Images.Media.getBitmap(getContentResolver(), artworkUri);
                } catch (Exception exception) {
                    artwork = null;
                } catch (OutOfMemoryError error) {
                    artwork = null;
                }

                long duration = FileInfo.getColumnLong(cursor, MediaStore.Audio.Media.DURATION);

                int id = FileInfo.getColumnInt(cursor, MediaStore.Audio.Media._ID);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

                if (artist != null && artist.equals(getString(R.string.streaming_audio_activity_unknown_artist))) {
                    artist = null;
                }

                MusicItem musicItem = new MusicItem(contentUri.toString(), new MediaMetaData(MediaMetaData.Type.AUDIO, artist,
                        album, title, artwork, duration, artworkUri));
                items.add(musicItem);
                // Give the music items in block of 10 for the UI thread.
                if (items.size() > 10) {
                    final List<MusicItem> newItems = items;
                    mHandler.post(() -> addItems(newItems));
                    items = new ArrayList<>();
                }
            }
            cursor.close();

            if (items.size() > 0) {
                final List<MusicItem> newItems = items;
                mHandler.post(() -> addItems(newItems));
            }
        }
    }

    private void addItems(@NonNull List<MusicItem> items) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addItems");
        }

        mMusicItems.addAll(items);
        resetSongs();
    }

    private void searchSongs(String searchText) {
        if (DEBUG) {
            Log.d(LOG_TAG, "searchSongs");
        }

        if (searchText.isEmpty()) {
            resetSongs();
            return;
        }

        mFilteredMusicItems.clear();

        for (MusicItem musicItem : mMusicItems) {
            if (musicItem.isMediaMetaDataContains(searchText.toLowerCase())) {
                mFilteredMusicItems.add(musicItem);
            }
        }

        reloadSongs();
    }

    private void resetSongs() {
        if (DEBUG) {
            Log.d(LOG_TAG, "resetSongs");
        }

        mFilteredMusicItems.clear();
        mFilteredMusicItems.addAll(mMusicItems);

        reloadSongs();
    }

    private void reloadSongs() {
        if (DEBUG) {
            Log.d(LOG_TAG, "reloadSongs");
        }

        mStreamingAudioAdapter.setMusicItems(mFilteredMusicItems);

        if (mFilteredMusicItems.size() == 0) {
            mNoMusicFoundImageView.setVisibility(View.VISIBLE);
            mNoMusicTitleView.setVisibility(View.VISIBLE);
            mNoMusicTextView.setVisibility(View.VISIBLE);
        } else {
            mNoMusicFoundImageView.setVisibility(View.GONE);
            mNoMusicTitleView.setVisibility(View.GONE);
            mNoMusicTextView.setVisibility(View.GONE);
        }
    }

    private void onShareClicked() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onShareClicked");
        }

        if (mSelectedMusicItem != null) {
            Intent data = new Intent();
            data.putExtra(Intents.INTENT_AUDIO_SELECTION, mSelectedMusicItem.getPath());
            data.putExtra(Intents.INTENT_AUDIO_METADATA, mSelectedMusicItem.getMediaMetaData());
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }
}