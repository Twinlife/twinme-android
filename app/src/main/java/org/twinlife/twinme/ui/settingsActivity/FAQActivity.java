/*
 *  Copyright (c) 2025-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.faq.FAQ;
import org.twinlife.twinme.utils.faq.FAQAction;
import org.twinlife.twinme.utils.faq.FAQImpl;
import org.twinlife.twinme.utils.faq.UIFAQArticle;
import org.twinlife.twinme.utils.faq.UIFAQCategory;
import org.twinlife.twinme.utils.faq.UIFAQItem;

import java.util.ArrayList;
import java.util.List;

public class FAQActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "FAQActivity";
    private static final boolean DEBUG = false;

    private static final int CONNECT_PEOPLE_ARTICLE_ID = 9;

    private FAQAdapter mFAQAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    private List<UIFAQCategory> mUIFAQCategories =  new ArrayList<>();

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
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();
    }

    @Override
    public void onExecutionSuccess() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onExecutionSuccess");
        }

        runOnUiThread(() -> {
            FAQ faq = FAQImpl.load(FAQActivity.this);
            mUIFAQCategories = faq.getCategories(FAQActivity.this);
            hideProgressIndicator();
            mFAQAdapter.updateFAQCategories(mUIFAQCategories);
        });
    }

    public void onFAQClick(UIFAQItem uifaqItem) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onFAQClick " + uifaqItem.getTitle());
        }

        hideKeyboard();

        if (uifaqItem.getType() != UIFAQItem.FAQItemType.ARTICLE) {
            return;
        }

        ViewGroup viewGroup = findViewById(R.id.faq_activity_layout);

        FAQView faqView = new FAQView(this, null);
        faqView.setArticle((UIFAQArticle) uifaqItem);
        faqView.setConfirmTitle(getString(R.string.application_ok));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                faqView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                faqView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                faqView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(faqView);
                setStatusBarColor();
            }
        };
        faqView.setObserver(observer);

        FAQView.LinkObserver linkObserver = url -> {
            if (DEBUG) {
                Log.d(LOG_TAG, "onFAQLinkClick: url=" + url);
            }

            if (url.equals(FAQView.CONNECT_PEOPLE_LINK)) {
                getFAQArticle(CONNECT_PEOPLE_ARTICLE_ID);
            }
        };

        faqView.setLinkObserver(linkObserver);

        viewGroup.addView(faqView);
        faqView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu);
        }

        getMenuInflater().inflate(R.menu.faq_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search_action);
        if (searchItem != null) {

            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(@NonNull MenuItem menuItem) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(@NonNull MenuItem menuItem) {

                    if (mFAQAdapter != null) {
                        mFAQAdapter.updateFAQCategories(mUIFAQCategories);
                    }

                    return true;
                }
            });

            SearchView searchView = (SearchView) searchItem.getActionView();

            if (searchView != null) {
                searchView.setQueryHint(getString(R.string.application_search_hint));
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {

                        filterFAQ(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {

                        filterFAQ(newText);
                        return true;
                    }
                });
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.faq_activity);

        setStatusBarColor();
        setToolBar(R.id.faq_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setTitle(getString(R.string.navigation_view_faq));
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        applyInsets(R.id.faq_activity_layout, R.id.faq_activity_tool_bar, R.id.faq_activity_list_view, Design.TOOLBAR_COLOR, false);

        mFAQAdapter = new FAQAdapter(this, mUIFAQCategories);
        mLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView faqRecyclerView = findViewById(R.id.faq_activity_list_view);
        faqRecyclerView.setLayoutManager(mLinearLayoutManager);
        faqRecyclerView.setAdapter(mFAQAdapter);
        faqRecyclerView.setItemAnimator(null);
        faqRecyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        mProgressBarView = findViewById(R.id.faq_activity_progress_bar);

        loadFAQ();
    }

    private void loadFAQ() {
        if (DEBUG) {
            Log.d(LOG_TAG, "loadFAQ");
        }

        showProgressIndicator();

        FAQAction faqAction = new FAQAction(this, new FAQImpl(), BuildConfig.FAQ_URL);
        faqAction.start();
    }

    private void getFAQArticle(int faqArticleId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getFAQArticle: faqArticleId=" + faqArticleId);
        }

        for (UIFAQCategory faqCategory : mUIFAQCategories) {
            for (UIFAQArticle faqArticle : faqCategory.getArticles()) {
                if (faqArticle.getArticleId() == faqArticleId) {
                    onFAQClick(faqArticle);
                    return;
                }
            }
        }
    }

    private void filterFAQ(String query) {
        if (DEBUG) {
            Log.d(LOG_TAG, "filterFAQ: query=" + query);
        }

        if (query == null || query.trim().isEmpty()) {
            mFAQAdapter.updateFAQCategories(mUIFAQCategories);
            return;
        }

        List<UIFAQCategory> filteredCategories = new ArrayList<>();
        for (UIFAQCategory category : mUIFAQCategories) {
            List<UIFAQArticle> filteredArticles = new ArrayList<>();
            for (UIFAQArticle article : category.getArticles()) {
                if (article.containsSearchText(query)) {
                    filteredArticles.add(article);
                }
            }
            if (!filteredArticles.isEmpty()) {
                filteredCategories.add(new UIFAQCategory(category.getTitle(), filteredArticles));
            }
        }
        mFAQAdapter.updateFAQCategories(filteredCategories);
        mLinearLayoutManager.scrollToPosition(0);
    }

    private void hideKeyboard() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideKeyboard");
        }

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
