/*
 *  Copyright (c) 2014-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Houssem Temanni (Houssem.Temanni@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.welcomeActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.WebViewActivity;
import org.twinlife.twinme.ui.mainActivity.MainActivity;
import org.twinlife.twinme.utils.DotsAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class WelcomeActivity extends AbstractTwinmeActivity {
    private static final String LOG_TAG = "WelcomeActivity";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_VIEW_HEIGHT = 860f;
    private static final float DESIGN_NEXT_VIEW_HEIGHT = 60f;
    private static final float DESIGN_NEXT_VIEW_MARGIN = 30f;
    private static int ITEM_VIEW_HEIGHT;

    private final List<UIWelcome> mUIWelcome = new ArrayList<>();

    private DotsAdapter mDotsAdapter;
    private RecyclerView mWelcomeRecyclerView;
    private TextView mMessageView;
    private TextView mPrevTextView;
    private TextView mNextTextView;
    private View mPrevClickableView;
    private View mNextClickableView;

    private int mCurrentPosition = 0;
    private boolean mHasConversations = false;
    private boolean mFromMenu = false;

    private class TextViewLinkHandler extends LinkMovementMethod {

        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {

            if (event.getAction() != MotionEvent.ACTION_UP) {

                return super.onTouchEvent(widget, buffer, event);
            }

            int x = (int) event.getX() - widget.getTotalPaddingLeft() + widget.getScrollX();
            int y = (int) event.getY() - widget.getTotalPaddingTop() + widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int offset = layout.getOffsetForHorizontal(line, x);

            URLSpan[] links = buffer.getSpans(offset, offset, URLSpan.class);
            if (links.length != 0) {
                Intent intent = new Intent(WelcomeActivity.this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.INTENT_WEB_VIEW_ACTIVITY_URL, links[0].getURL());

                startActivity(intent);
            }

            return true;
        }
    }

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mHasConversations = getIntent().getBooleanExtra(Intents.INTENT_HAS_CONVERSATIONS, false);
        mFromMenu = getIntent().getBooleanExtra(Intents.INTENT_FROM_MENU, false);

        setupWelcome();
        initViews();
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();
    }

    //
    // Override Activity methods
    //

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
    }

    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        // Android 13, finish profile creation after asking for POST_NOTIFICATIONS permission
        // (even if that permission is not granted).
        if ((grantedPermissions.length == 0 || grantedPermissions[0] == Permission.POST_NOTIFICATIONS)) {
            startMain();
        }
    }

    //
    // Private methods
    //

    private void startMain() {
        if (DEBUG) {
            Log.d(LOG_TAG, "startMain");
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intents.INTENT_HAS_CONVERSATIONS, mHasConversations);
        startActivity(intent);
        finish();
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.welcome_activity);

        setStatusBarColor(Design.WHITE_COLOR);

        applyInsets(R.id.welcome_activity_layout, -1, -1, Design.WHITE_COLOR, false);

        WelcomeAdapter welcomeAdapter = new WelcomeAdapter(this, mUIWelcome);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);

        mWelcomeRecyclerView = findViewById(R.id.welcome_activity_list_view);
        mWelcomeRecyclerView.setLayoutManager(linearLayoutManager);
        mWelcomeRecyclerView.setAdapter(welcomeAdapter);
        mWelcomeRecyclerView.setItemAnimator(null);

        SnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(mWelcomeRecyclerView);

        mWelcomeRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = pagerSnapHelper.findSnapView(linearLayoutManager);
                    if (centerView != null) {
                        mCurrentPosition = linearLayoutManager.getPosition(centerView);
                        setupAction();
                    }
                }
            }
        });

        RecyclerView dotsRecyclerView = findViewById(R.id.welcome_activity_dots_view);
        mDotsAdapter = new DotsAdapter(mUIWelcome.size(), getLayoutInflater());
        LinearLayoutManager dotsLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        dotsRecyclerView.setLayoutManager(dotsLinearLayoutManager);
        dotsRecyclerView.setAdapter(mDotsAdapter);
        dotsRecyclerView.setItemAnimator(null);

        ViewGroup.LayoutParams layoutParams = dotsRecyclerView.getLayoutParams();
        layoutParams.width = mUIWelcome.size() * Design.DOT_SIZE;
        dotsRecyclerView.setLayoutParams(layoutParams);

        mPrevTextView = findViewById(R.id.welcome_activity_prev_view);
        Design.updateTextFont(mPrevTextView, Design.FONT_REGULAR34);
        mPrevTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mPrevTextView.setVisibility(View.INVISIBLE);

        mPrevClickableView = findViewById(R.id.welcome_activity_prev_clickable_view);
        mPrevClickableView.setOnClickListener(v -> onBackRecyclerClick());
        mPrevClickableView.setVisibility(View.INVISIBLE);

        mNextTextView = findViewById(R.id.welcome_activity_next_view);
        Design.updateTextFont(mNextTextView, Design.FONT_BOLD34);
        mNextTextView.setTextColor(Color.WHITE);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mNextTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BUTTON_MARGIN;
        marginLayoutParams.rightMargin = Design.BUTTON_MARGIN;

        mNextClickableView = findViewById(R.id.welcome_activity_next_clickable_view);
        mNextClickableView.setOnClickListener(v -> onNextRecyclerClick());

        layoutParams = mNextClickableView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_NEXT_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mNextClickableView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_NEXT_VIEW_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginEnd((int) (DESIGN_NEXT_VIEW_MARGIN * Design.WIDTH_RATIO));

        float radius = DESIGN_NEXT_VIEW_HEIGHT * Design.HEIGHT_RATIO * 0.5f * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable createViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        createViewBackground.getPaint().setColor(Design.getMainStyle());
        mNextClickableView.setBackground(createViewBackground);

        layoutParams = mNextClickableView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        mMessageView = findViewById(R.id.welcome_activity_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR28);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);
        String messageText = String.format(getString(R.string.welcome_activity_accept), getString(R.string.welcome_activity_pass)) + " " + getResources().getString(R.string.welcome_activity_terms_of_use) + " - " + getResources().getString(R.string.welcome_activity_privacy_policy);
        mMessageView.setText(messageText);
        addLinks();

        setupAction();

        View backClickableView = findViewById(R.id.welcome_activity_back_clickable_view);
        backClickableView.setOnClickListener(v -> finish());

        if (mFromMenu) {
            backClickableView.setVisibility(View.VISIBLE);
        } else {
            backClickableView.setVisibility(View.GONE);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (DEBUG) {
                    Log.d(LOG_TAG, "handleOnBackPressed");
                }

                backPressed();
            }
        });
    }

    private void onEnterClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEnterClick");
        }

        getTwinmeApplication().hideWelcomeScreen();

        // On Android 13, we must ask for the POST_NOTIFICATIONS permission to be able to post notifications.
        // If the permission is not granted, messages and calls are received but notifications are not displayed.
        // It is not possible to answer an incoming call!
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || checkPermissions(new Permission[]{Permission.POST_NOTIFICATIONS})) {
            startMain();
        }
    }

    private void onBackRecyclerClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackRecyclerClick");
        }

        if (mCurrentPosition - 1 >= 0) {
            mWelcomeRecyclerView.smoothScrollToPosition(mCurrentPosition - 1);
        }
    }

    private void onNextRecyclerClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onNextRecyclerClick");
        }

        if (mCurrentPosition + 1 < mUIWelcome.size()) {
            mWelcomeRecyclerView.smoothScrollToPosition(mCurrentPosition + 1);
        } else if (mCurrentPosition + 1 == mUIWelcome.size()) {
            onEnterClick();
        }
    }

    private void addLinks() {
        if (DEBUG) {
            Log.d(LOG_TAG, "addLinks");
        }

        addLinks(mMessageView, getString(R.string.welcome_activity_terms_of_use), "file:///android_res/raw/terms_of_service.html");
        addLinks(mMessageView, getString(R.string.welcome_activity_privacy_policy), "file:///android_res/raw/privacy_policy.html");
        mMessageView.setMovementMethod(new TextViewLinkHandler() {
        });
    }

    private static void addLinks(TextView textView, String link, String scheme) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addLinks textView=" + textView + " link=" + link + " scheme=" + scheme);
        }

        Pattern pattern = Pattern.compile(link);
        try {
            android.text.util.Linkify.addLinks(textView, pattern, scheme, (s, start, end) -> true, (match, url) -> "");
        } catch (Exception ex) {
            // Possible exception: android.webkit.WebViewFactory.MissingWebViewPackageException when there is no WebView implementation.
        }
    }

    private void setupWelcome() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupWelcome");
        }

        boolean darkMode = false;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = Settings.displayMode.getInt();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        mUIWelcome.add(new UIWelcome(getString(R.string.welcome_activity_step1_message), darkMode ? R.drawable.onboarding_step1_dark : R.drawable.onboarding_step1));
        mUIWelcome.add(new UIWelcome(getString(R.string.welcome_activity_step2_message), darkMode ? R.drawable.onboarding_step2_dark : R.drawable.onboarding_step2));
        mUIWelcome.add(new UIWelcome(getString(R.string.welcome_activity_step3_message), darkMode ? R.drawable.onboarding_step3_dark : R.drawable.onboarding_step3));
        mUIWelcome.add(new UIWelcome(getString(R.string.quality_of_service_activity_step2_message), darkMode ? R.drawable.quality_service_step2_dark : R.drawable.quality_service_step2));
    }

    private void setupAction() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupAction");
        }

        if (mCurrentPosition == 0) {
            mPrevTextView.setVisibility(View.INVISIBLE);
            mPrevClickableView.setVisibility(View.INVISIBLE);
        } else {
            mPrevTextView.setVisibility(View.VISIBLE);
            mPrevClickableView.setVisibility(View.VISIBLE);
        }

        if (mCurrentPosition + 1 == mUIWelcome.size()) {
            if (mFromMenu) {
                mNextTextView.setVisibility(View.INVISIBLE);
                mNextClickableView.setVisibility(View.INVISIBLE);
                mMessageView.setVisibility(View.INVISIBLE);
            } else {
                mNextTextView.setVisibility(View.VISIBLE);
                mNextClickableView.setVisibility(View.VISIBLE);
                mNextTextView.setText(getString(R.string.welcome_activity_start));
                mMessageView.setVisibility(View.VISIBLE);
                String messageText = String.format(getString(R.string.welcome_activity_accept), getString(R.string.welcome_activity_start)) + " " + getResources().getString(R.string.welcome_activity_terms_of_use) + " - " + getResources().getString(R.string.welcome_activity_privacy_policy);
                mMessageView.setText(messageText);
            }
        } else {
            mNextTextView.setText(getString(R.string.welcome_activity_next));
            mMessageView.setVisibility(View.GONE);
            String messageText = String.format(getString(R.string.welcome_activity_accept), getString(R.string.welcome_activity_pass)) + " " + getResources().getString(R.string.welcome_activity_terms_of_use) + " - " + getResources().getString(R.string.welcome_activity_privacy_policy);
            mMessageView.setText(messageText);
            mNextTextView.setVisibility(View.VISIBLE);
            mNextClickableView.setVisibility(View.VISIBLE);
        }

        mDotsAdapter.setCurrentPosition(mCurrentPosition);
        addLinks();
    }

    @Override
    public void setupDesign() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setupDesign");
        }

        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
    }

    private void backPressed() {
        if (DEBUG) {
            Log.d(LOG_TAG, "backPressed");
        }

        if (!mFromMenu) {
            getTwinmeApplication().stop();
        }
    }
}
