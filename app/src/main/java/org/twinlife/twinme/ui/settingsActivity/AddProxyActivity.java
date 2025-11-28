/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import org.libwebsockets.ErrorCategory;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConnectivityService;
import org.twinlife.twinlife.ProxyDescriptor;
import org.twinlife.twinlife.SNIProxyDescriptor;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinme.services.ProxyService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.contacts.PasteEditText;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.OnboardingConfirmView;

import java.util.List;

public class AddProxyActivity extends AbstractTwinmeActivity implements ProxyService.Observer {
    private static final String LOG_TAG = "AddProxyActivity";
    private static final boolean DEBUG = false;

    public static final float DESIGN_PROXY_TOP_MARGIN = 60f;
    public static final float DESIGN_SAVE_TOP_MARGIN = 40f;

    private TextView mMessageView;
    private PasteEditText mProxyView;
    private View mSaveClickableView;
    private int mProxyPosition = -1;
    private ProxyDescriptor mProxyDescriptor;
    private ProxyService mProxyService;

    private boolean mShowOnboarding = false;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mProxyPosition = intent.getIntExtra(Intents.INTENT_PROXY, -1);

        if (mProxyPosition != -1) {
            List<ProxyDescriptor> proxies = getTwinmeContext().getConnectivityService().getUserProxies();
            if (mProxyPosition < proxies.size()) {
                mProxyDescriptor = proxies.get(mProxyPosition);
            }
        }

        initViews();

        mProxyService = new ProxyService(this, getTwinmeContext(), this);
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mProxyService.dispose();

        super.onDestroy();
    }

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (!mShowOnboarding && mProxyDescriptor == null && getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.PROXY)) {
            mShowOnboarding = true;
            showInfo(true, true);
        } else if (mProxyView != null) {
            mProxyView.postDelayed(() -> {
                mProxyView.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.showSoftInput(mProxyView, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 500);
        }
    }

    @Override
    public void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mProxyView.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu);
        }

        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.onboarding_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.info_action);
        ImageView imageView = (ImageView) menuItem.getActionView();

        if (imageView != null) {
            imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.onboarding_info_icon, null));
            imageView.setOnClickListener(view -> onOnboardingClick());

            imageView.setColorFilter(Color.WHITE);
            imageView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);
        }

        return true;
    }

    //
    // ProxyService.Observer methods
    //

    @Override
    public void onAddProxy(@NonNull SNIProxyDescriptor proxyDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddProxy: " + proxyDescriptor);
        }

        finish();
    }

    @Override
    public void onDeleteProxy(@NonNull ProxyDescriptor proxyDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteProxy: " + proxyDescriptor);
        }
    }

    @Override
    public void onGetProxyUri(@Nullable TwincodeURI twincodeURI, @NonNull ProxyDescriptor proxyDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProxyUri: " + twincodeURI + " proxyDescriptor: " + proxyDescriptor);
        }
    }

    @Override
    public void onErrorAddProxy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onErrorAddProxy");
        }

        showAlertMessageView(R.id.add_proxy_activity_layout, getString(R.string.deleted_account_activity_warning), getString(R.string.proxy_activity_invalid_format), false, null);
    }

    @Override
    public void onErrorAlreadyUsed() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onErrorAlreadyUsed");
        }

        showAlertMessageView(R.id.add_proxy_activity_layout, getString(R.string.deleted_account_activity_warning), getString(R.string.proxy_activity_already_use), false, null);
    }

    @Override
    public void onErrorLimitReached() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onErrorLimitReached");
        }

        showAlertMessageView(R.id.add_proxy_activity_layout, getString(R.string.deleted_account_activity_warning), String.format(getString(R.string.proxy_activity_limit), ConnectivityService.MAX_PROXIES), false, null);
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.add_proxy_activity);

        setStatusBarColor();
        setToolBar(R.id.add_proxy_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.proxy_activity_title));
        setBackgroundColor(Design.WHITE_COLOR);

        applyInsets(R.id.add_proxy_activity_layout, R.id.add_proxy_activity_tool_bar, R.id.add_proxy_activity_container_view, Design.TOOLBAR_COLOR, false);

        View containerView = findViewById(R.id.add_proxy_activity_container_view);
        containerView.setBackgroundColor(Design.WHITE_COLOR);

        View urlContentView = findViewById(R.id.add_proxy_activity_url_content_view);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable nameViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        nameViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        urlContentView.setBackground(nameViewBackground);

        ViewGroup.LayoutParams layoutParams = urlContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        ViewGroup.MarginLayoutParams  marginLayoutParams = (ViewGroup.MarginLayoutParams) urlContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_PROXY_TOP_MARGIN * Design.HEIGHT_RATIO);

        mProxyView = findViewById(R.id.add_proxy_activity_url_view);
        mProxyView.setTypeface(Design.FONT_REGULAR28.typeface);
        mProxyView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR28.size);
        mProxyView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mProxyView.setHintTextColor(Design.GREY_COLOR);
        mProxyView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                if (mProxyDescriptor != null) {
                    if (s.toString().isEmpty() || s.toString().equals(mProxyDescriptor.getDescriptor())) {
                        mSaveClickableView.setAlpha(0.5f);
                    } else {
                        mSaveClickableView.setAlpha(1.0f);
                    }
                } else {
                    if (!s.toString().isEmpty()) {
                        mSaveClickableView.setAlpha(1.0f);
                    } else {
                        mSaveClickableView.setAlpha(0.5f);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mProxyView.setPasteObserver(() -> {
            if (mProxyView.getText() != null) {
                String pasteText = mProxyView.getText().toString();
                if (pasteText.contains(TwincodeURI.PROXY_ACTION + "/")) {
                    pasteText = pasteText.replace(TwincodeURI.PROXY_ACTION + "/", "");
                    mProxyView.setText(pasteText);
                }
            }
        });

        mSaveClickableView = findViewById(R.id.add_proxy_activity_save_view);
        mSaveClickableView.setAlpha(0.5f);
        mSaveClickableView.setOnClickListener(v -> onSaveClick());

        layoutParams = mSaveClickableView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Design.getMainStyle());
        mSaveClickableView.setBackground(saveViewBackground);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSaveClickableView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SAVE_TOP_MARGIN * Design.HEIGHT_RATIO);

        TextView saveTextView = findViewById(R.id.add_proxy_activity_save_title_view);
        saveTextView.setTypeface(Design.FONT_BOLD28.typeface);
        saveTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD28.size);
        saveTextView.setTextColor(Color.WHITE);

        TextView formatView = findViewById(R.id.add_proxy_activity_format_view);
        formatView.setText(String.format("%s \n %s", getString(R.string.proxy_activity_format), getString(R.string.proxy_activity_format_sample)));
        formatView.setTypeface(Design.FONT_ITALIC_28.typeface);
        formatView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_ITALIC_28.size);
        formatView.setTextColor(Design.FONT_COLOR_GREY);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) formatView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SAVE_TOP_MARGIN * Design.HEIGHT_RATIO);

        mMessageView = findViewById(R.id.add_proxy_activity_message_view);
        mMessageView.setTypeface(Design.FONT_REGULAR32.typeface);
        mMessageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mMessageView.setVisibility(View.GONE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_PROXY_TOP_MARGIN * Design.HEIGHT_RATIO);

        if (mProxyDescriptor != null) {
            mProxyView.setText(mProxyDescriptor.getDescriptor());

            if (mProxyDescriptor.getLastError() != null && mProxyDescriptor.getLastError() != ErrorCategory.ERR_NONE) {
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.proxy_activity_warning);
            }
        }
    }

    private void onSaveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveClick");
        }

        if (mSaveClickableView.getAlpha() < 1.0f) {
            return;
        }
        final Editable text = mProxyView.getText();
        if (text == null) {
            return;
        }

        hideKeyboard();

        final String proxyURL = text.toString();
        mProxyService.verifyProxyURI(Uri.parse(proxyURL), mProxyPosition);
    }

    private void onOnboardingClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onOnboardingClick");
        }

        showInfo(false, false);
    }

    private void showInfo(boolean showKeyboard, boolean cancelAction) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showInfo");
        }

        hideKeyboard();

        ViewGroup viewGroup = findViewById(R.id.add_proxy_activity_layout);

        OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);
        onboardingConfirmView.setTitle(getString(R.string.proxy_activity_title));
        onboardingConfirmView.setImage(ResourcesCompat.getDrawable(getResources(),  R.drawable.onboarding_proxy, null));
        onboardingConfirmView.setMessage(getString(R.string.proxy_activity_onboarding));
        onboardingConfirmView.setConfirmTitle(getString(R.string.application_ok));
        onboardingConfirmView.setCancelTitle(getString(R.string.application_do_not_display));

        if (!cancelAction) {
            onboardingConfirmView.hideCancelView();
        }

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                onboardingConfirmView.animationCloseConfirmView();
                getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.PROXY, false);
            }

            @Override
            public void onDismissClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(onboardingConfirmView);
                setStatusBarColor();

                if (showKeyboard) {
                    mProxyView.requestFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(mProxyView, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        };
        onboardingConfirmView.setObserver(observer);
        viewGroup.addView(onboardingConfirmView);
        onboardingConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void hideKeyboard() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideKeyboard");
        }

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mProxyView.getWindowToken(), 0);
        }
    }
}
