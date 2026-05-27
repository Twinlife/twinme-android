/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  GifPickerActivity: full-screen GIF picker launched from the conversation
 *  "+" menu. Shows trending GIFs and recently sent ones, supports search,
 *  pagination, and a provider switch (Tenor / Giphy) when both are configured.
 *  When a GIF is chosen it is downloaded to the cache dir and its path is
 *  returned to the caller, which sends it as a normal image message.
 */

package org.twinlife.twinme.ui.gifPickerActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.gif.GifItem;
import org.twinlife.twinme.gif.GifProvider;
import org.twinlife.twinme.gif.GifService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GifPickerActivity extends AppCompatActivity implements GifAdapter.Listener {

    public static final String EXTRA_GIF_PATH = "org.twinlife.twinme.gif.PATH";
    public static final String EXTRA_GIF_NAME = "org.twinlife.twinme.gif.NAME";

    private static final int PAGE_LIMIT = 30;
    private static final long SEARCH_DEBOUNCE_MS = 350L;

    private GifService mGifService;
    private RecyclerView mRecyclerView;
    private EditText mSearchView;
    private ProgressBar mProgressBar;
    private TextView mMessageView;
    private TextView mAttributionView;
    private RadioGroup mProviderGroup;

    private final List<GifItem> mItems = new ArrayList<>();
    private GifAdapter mAdapter;
    private int mSpanCount = 3;

    @Nullable
    private String mNextPosition;
    @Nullable
    private String mCurrentQuery;
    private boolean mLoading;
    private boolean mReachedEnd;
    private boolean mDownloading;
    private int mGeneration;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    @Nullable
    private Runnable mSearchRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_picker);

        mGifService = GifService.getInstance(this);

        Toolbar toolbar = findViewById(R.id.gif_picker_toolbar);
        toolbar.setTitle(R.string.conversation_activity_gif);
        toolbar.setNavigationIcon(R.drawable.arrow_back);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        mSearchView = findViewById(R.id.gif_picker_search);
        mProgressBar = findViewById(R.id.gif_picker_progress);
        mMessageView = findViewById(R.id.gif_picker_message);
        mAttributionView = findViewById(R.id.gif_picker_attribution);
        mProviderGroup = findViewById(R.id.gif_picker_provider_group);

        mSpanCount = computeSpanCount();
        mRecyclerView = findViewById(R.id.gif_picker_recycler);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, mSpanCount));
        mAdapter = new GifAdapter(mItems, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0) {
                    return;
                }
                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) {
                    return;
                }
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                if (!mLoading && !mReachedEnd && lastVisible >= layoutManager.getItemCount() - mSpanCount * 2) {
                    loadNextPage(false);
                }
            }
        });

        setupSearch();

        if (!mGifService.isConfigured()) {
            showMessage(getString(R.string.gif_picker_not_configured));
            return;
        }

        setupProviderSwitch();
        updateAttribution();
        reloadFromStart();
    }

    private int computeSpanCount() {
        float widthDp = getResources().getConfiguration().screenWidthDp;
        int columns = (int) (widthDp / 120f);
        return Math.max(3, columns);
    }

    private void setupSearch() {
        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mSearchRunnable != null) {
                    mHandler.removeCallbacks(mSearchRunnable);
                }
                mSearchRunnable = GifPickerActivity.this::performSearch;
                mHandler.postDelayed(mSearchRunnable, SEARCH_DEBOUNCE_MS);
            }
        });
        mSearchView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (mSearchRunnable != null) {
                    mHandler.removeCallbacks(mSearchRunnable);
                }
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void setupProviderSwitch() {
        List<GifProvider> providers = mGifService.getAvailableProviders();
        if (providers.size() < 2) {
            return;
        }
        mProviderGroup.setVisibility(View.VISIBLE);
        GifProvider active = mGifService.getActiveProvider();
        for (int i = 0; i < providers.size(); i++) {
            GifProvider provider = providers.get(i);
            RadioButton button = new RadioButton(this);
            button.setId(View.generateViewId());
            button.setText(provider.getDisplayName());
            button.setTag(provider);
            mProviderGroup.addView(button);
            if (provider == active) {
                button.setChecked(true);
            }
        }
        mProviderGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton button = group.findViewById(checkedId);
            if (button != null && button.getTag() instanceof GifProvider) {
                mGifService.setActiveProvider((GifProvider) button.getTag());
                updateAttribution();
                reloadFromStart();
            }
        });
    }

    private void performSearch() {
        String text = mSearchView.getText().toString().trim();
        mCurrentQuery = text.isEmpty() ? null : text;
        reloadFromStart();
    }

    private void reloadFromStart() {
        mGeneration++;
        mLoading = false;
        mReachedEnd = false;
        mNextPosition = null;
        mItems.clear();
        mAdapter.notifyDataSetChanged();
        loadNextPage(true);
    }

    private void loadNextPage(boolean firstPage) {
        GifProvider provider = mGifService.getActiveProvider();
        if (provider == null || mLoading || mReachedEnd) {
            return;
        }
        mLoading = true;
        if (mItems.isEmpty()) {
            mProgressBar.setVisibility(View.VISIBLE);
            mMessageView.setVisibility(View.GONE);
        }

        final int generation = mGeneration;
        final String position = firstPage ? null : mNextPosition;
        GifProvider.Callback callback = new GifProvider.Callback() {
            @Override
            public void onResult(@NonNull List<GifItem> items, @Nullable String nextPosition) {
                if (generation != mGeneration) {
                    return; // superseded by a newer request
                }
                mLoading = false;
                mProgressBar.setVisibility(View.GONE);
                appendItems(items, firstPage, nextPosition);
            }

            @Override
            public void onError(@NonNull Exception error) {
                if (generation != mGeneration) {
                    return;
                }
                mLoading = false;
                mProgressBar.setVisibility(View.GONE);
                if (mItems.isEmpty()) {
                    showMessage(getString(R.string.gif_picker_error));
                }
            }
        };

        if (mCurrentQuery != null) {
            provider.search(mCurrentQuery, PAGE_LIMIT, position, callback);
        } else {
            provider.fetchTrending(PAGE_LIMIT, position, callback);
        }
    }

    private void appendItems(@NonNull List<GifItem> newItems, boolean firstPage, @Nullable String nextPosition) {
        List<GifItem> toAdd = new ArrayList<>();
        if (firstPage && mCurrentQuery == null) {
            // Surface recently sent GIFs first on the trending feed.
            for (GifItem recent : mGifService.getRecentGifs()) {
                if (!toAdd.contains(recent)) {
                    toAdd.add(recent);
                }
            }
        }
        for (GifItem item : newItems) {
            if (!toAdd.contains(item) && !mItems.contains(item)) {
                toAdd.add(item);
            }
        }
        int start = mItems.size();
        mItems.addAll(toAdd);
        mNextPosition = nextPosition;
        mReachedEnd = (nextPosition == null);
        if (start == 0) {
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.notifyItemRangeInserted(start, toAdd.size());
        }
        updateMessageVisibility();
    }

    private void updateMessageVisibility() {
        if (mItems.isEmpty() && !mLoading) {
            showMessage(mCurrentQuery != null ? getString(R.string.gif_picker_no_results) : "");
        } else {
            mMessageView.setVisibility(View.GONE);
        }
    }

    private void showMessage(@NonNull String message) {
        mMessageView.setText(message);
        mMessageView.setVisibility(message.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateAttribution() {
        GifProvider provider = mGifService.getActiveProvider();
        if (provider != null) {
            mAttributionView.setText(getString(R.string.gif_picker_powered_by, provider.getDisplayName()));
        } else {
            mAttributionView.setText("");
        }
    }

    @Override
    public void onGifClick(@NonNull GifItem item) {
        if (mDownloading) {
            return;
        }
        mDownloading = true;
        mProgressBar.setVisibility(View.VISIBLE);
        mGifService.download(item, new GifService.DownloadCallback() {
            @Override
            public void onDownloaded(@NonNull File file) {
                mDownloading = false;
                mProgressBar.setVisibility(View.GONE);
                mGifService.addRecentGif(item);
                Intent data = new Intent();
                data.putExtra(EXTRA_GIF_PATH, file.getAbsolutePath());
                data.putExtra(EXTRA_GIF_NAME, file.getName());
                setResult(RESULT_OK, data);
                finish();
            }

            @Override
            public void onError(@NonNull Exception error) {
                mDownloading = false;
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(GifPickerActivity.this, R.string.gif_picker_download_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mSearchRunnable != null) {
            mHandler.removeCallbacks(mSearchRunnable);
        }
        super.onDestroy();
    }
}
