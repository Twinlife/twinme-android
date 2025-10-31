/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
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
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BaseService;
import org.twinlife.twinlife.TwincodeOutbound;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Invitation;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.services.InvitationCodeService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.DefaultConfirmView;

import java.util.List;
import java.util.UUID;

public class EnterInvitationCodeActivity extends AbstractTwinmeActivity implements InvitationCodeService.Observer {
    private static final String LOG_TAG = "EnterInvitationCode...";
    private static final boolean DEBUG = false;

    public static final float DESIGN_ENTER_CODE_TOP_MARGIN = 80f;
    public static final float DESIGN_ENTER_CODE_HEIGHT = 140f;
    public static final float DESIGN_ENTER_CODE_MARGIN = 20f;
    public static final float DESIGN_CONFIRM_TOP_MARGIN = 60f;
    public static final float DESIGN_MESSAGE_TOP_MARGIN = 40f;

    private PasteEditText mEnterCodeOneEditText;
    private PasteEditText mEnterCodeTwoEditText;
    private PasteEditText mEnterCodeThreeEditText;
    private PasteEditText mEnterCodeFourEditText;
    private PasteEditText mEnterCodeFiveEditText;
    private PasteEditText mEnterCodeSixEditText;
    private View mConfirmView;
    private View mOverlayView;

    private boolean mShowOnboarding = false;
    private boolean mEnableConfirmView = false;

    private InvitationCodeService mInvitationCodeService;

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

        mInvitationCodeService = new InvitationCodeService(this, getTwinmeContext(), this);
        mInvitationCodeService.getInvitations();
    }

    //
    // Override Activity methods
    //

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (!mShowOnboarding) {
            mShowOnboarding = true;

            if (getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.ENTER_MINI_CODE)) {
                showOnboarding(false);
            } else {
                mEnterCodeOneEditText.postDelayed(() -> {
                    mEnterCodeOneEditText.requestFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null) {
                        inputMethodManager.showSoftInput(mEnterCodeOneEditText, InputMethodManager.SHOW_IMPLICIT);
                    }
                }, 500);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();

        if (mInvitationCodeService != null) {
            mInvitationCodeService.dispose();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);
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
            imageView.setColorFilter(Color.WHITE);
            imageView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);
            imageView.setOnClickListener(view -> showOnboarding(true));
        }

        return true;
    }

    //
    // InvitationCodeService.Observer methods
    //


    @Override
    public void onGetDefaultProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDefaultProfile: profile=" + profile);
        }
    }

    @Override
    public void onGetDefaultProfileNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDefaultProfileNotFound");
        }

        finish();
    }

    @Override
    public void onCreateInvitationWithCode(@Nullable Invitation invitation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateInvitationWithCode: invitation=" + invitation);
        }

    }

    @Override
    public void onGetInvitationCode(@Nullable TwincodeOutbound twincodeOutbound, @Nullable Bitmap bitmap, @Nullable String publicKey) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetInvitationCode: twincodeOutbound=" + twincodeOutbound + " publicKey=" + publicKey);
        }

        mOverlayView.setVisibility(View.GONE);
        mProgressBarView.setVisibility(View.GONE);
        setStatusBarColor();

        if (twincodeOutbound != null) {
            showInvitation(twincodeOutbound);
        }
    }

    @Override
    public void onGetInvitationCodeNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetInvitationCodeNotFound");
        }

        mOverlayView.setVisibility(View.GONE);
        mProgressBarView.setVisibility(View.GONE);

        error(getString(R.string.enter_invitation_code_activity_error_message), this::resetCode);
    }

    @Override
    public void onGetLocalInvitationCode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetLocalInvitationCode");
        }

        mOverlayView.setVisibility(View.GONE);
        mProgressBarView.setVisibility(View.GONE);

        error(getString(R.string.accept_invitation_activity_local_twincode), this::finish);
    }

    @Override
    public void onGetInvitations(@Nullable List<Invitation> invitations) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetInvitations: invitations=" + invitations);
        }
    }

    @Override
    public void onDeleteInvitation(@NonNull UUID invitationId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteInvitation: invitationId=" + invitationId);
        }
    }

    @Override
    public void onCreateContact(@NonNull Contact contact) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateContact: contact=" + contact);
        }

        showContactActivity(contact);
        finish();
    }

    @Override
    public void onError(BaseService.ErrorCode errorCode, @Nullable String message, @Nullable Runnable errorCallback) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError errorCode=" + errorCode);
        }

        mOverlayView.setVisibility(View.GONE);
        mProgressBarView.setVisibility(View.GONE);
        setStatusBarColor();

        super.onError(errorCode, message, this::resetCode);
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.enter_invitation_code_activity);

        setStatusBarColor();
        setTitle(getString(R.string.add_contact_activity_invitation_code_title));
        setToolBar(R.id.enter_invitation_code_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.GREY_BACKGROUND_COLOR);

        applyInsets(R.id.enter_invitation_code_activity_layout, R.id.enter_invitation_code_activity_tool_bar, R.id.enter_invitation_code_activity_container_view, Design.TOOLBAR_COLOR, false);

        View containerView = findViewById(R.id.enter_invitation_code_activity_container_view);
        containerView.setBackgroundColor(Design.GREY_BACKGROUND_COLOR);

        View enterCodeView = findViewById(R.id.enter_invitation_code_activity_enter_code_view);

        ViewGroup.LayoutParams layoutParams = enterCodeView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = (int) (DESIGN_ENTER_CODE_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) enterCodeView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ENTER_CODE_TOP_MARGIN * Design.HEIGHT_RATIO);

        int editTextMargin = (int) (DESIGN_ENTER_CODE_MARGIN * Design.WIDTH_RATIO);
        int editTextWidth = (int) ((Design.BUTTON_WIDTH - (editTextMargin * 5)) / 6);

        View enterCodeOneView = findViewById(R.id.enter_invitation_code_activity_enter_code_one_content_view);

        layoutParams = enterCodeOneView.getLayoutParams();
        layoutParams.width = editTextWidth;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) enterCodeOneView.getLayoutParams();
        marginLayoutParams.rightMargin = editTextMargin;

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        GradientDrawable enterCodeViewBackground = new GradientDrawable();
        enterCodeViewBackground.setColor(Design.POPUP_BACKGROUND_COLOR);
        enterCodeViewBackground.setCornerRadius((int) (radius));
        enterCodeViewBackground.setStroke(2, Color.BLACK);
        enterCodeOneView.setBackground(enterCodeViewBackground);

        InputFilter alphaNumericFilter = (charSequence, i, i1, spanned, i2, i3) -> {
            try {
                char c = charSequence.charAt(0);
                if (Character.isLetter(c) || (Character.isDigit(c) && c != '0' && c != '1' && c != '5')) {
                    return "" + Character.toUpperCase(c);
                } else {
                    return "";
                }
            } catch (Exception e) {
            }

            return null;
        };

        InputFilter lengthFilter = new InputFilter.LengthFilter(1);

        mEnterCodeOneEditText = findViewById(R.id.enter_invitation_code_activity_enter_code_one_edit_text_view);
        Design.updateTextFont(mEnterCodeOneEditText, Design.FONT_BOLD68);
        mEnterCodeOneEditText.setTextColor(Design.FONT_COLOR_DEFAULT);
        mEnterCodeOneEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        mEnterCodeOneEditText.setFilters(new InputFilter[]{alphaNumericFilter, lengthFilter});
        mEnterCodeOneEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().isEmpty()) {
                    mEnterCodeTwoEditText.requestFocus();
                    updateViews();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mEnterCodeOneEditText.setPasteObserver(this::onPasteText);

        View enterCodeTwoView = findViewById(R.id.enter_invitation_code_activity_enter_code_two_content_view);

        layoutParams = enterCodeTwoView.getLayoutParams();
        layoutParams.width = editTextWidth;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) enterCodeTwoView.getLayoutParams();
        marginLayoutParams.rightMargin = editTextMargin;

        enterCodeTwoView.setBackground(enterCodeViewBackground);

        mEnterCodeTwoEditText = findViewById(R.id.enter_invitation_code_activity_enter_code_two_edit_text_view);

        Design.updateTextFont(mEnterCodeTwoEditText, Design.FONT_BOLD68);
        mEnterCodeTwoEditText.setTextColor(Design.FONT_COLOR_DEFAULT);
        mEnterCodeTwoEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        mEnterCodeTwoEditText.setFilters(new InputFilter[]{alphaNumericFilter, lengthFilter});
        mEnterCodeTwoEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().isEmpty()) {
                    mEnterCodeThreeEditText.requestFocus();
                    updateViews();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mEnterCodeTwoEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN && mEnterCodeTwoEditText.getText() != null && mEnterCodeTwoEditText.getText().toString().isEmpty()) {
                mEnterCodeOneEditText.setText("");
                mEnterCodeOneEditText.requestFocus();
                updateViews();
            }
            return false;
        });

        mEnterCodeTwoEditText.setPasteObserver(this::onPasteText);

        View enterCodeThreeView = findViewById(R.id.enter_invitation_code_activity_enter_code_three_content_view);

        layoutParams = enterCodeThreeView.getLayoutParams();
        layoutParams.width = editTextWidth;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) enterCodeThreeView.getLayoutParams();
        marginLayoutParams.rightMargin = editTextMargin;

        enterCodeThreeView.setBackground(enterCodeViewBackground);

        mEnterCodeThreeEditText = findViewById(R.id.enter_invitation_code_activity_enter_code_three_edit_text_view);

        Design.updateTextFont(mEnterCodeThreeEditText, Design.FONT_BOLD68);
        mEnterCodeThreeEditText.setTextColor(Design.FONT_COLOR_DEFAULT);
        mEnterCodeThreeEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        mEnterCodeThreeEditText.setFilters(new InputFilter[]{alphaNumericFilter, lengthFilter});
        mEnterCodeThreeEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().isEmpty()) {
                    mEnterCodeFourEditText.requestFocus();
                    updateViews();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mEnterCodeThreeEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN  && mEnterCodeThreeEditText.getText() != null && mEnterCodeThreeEditText.getText().toString().isEmpty()) {
                mEnterCodeTwoEditText.setText("");
                mEnterCodeTwoEditText.requestFocus();
                updateViews();
            }
            return false;
        });

        mEnterCodeThreeEditText.setPasteObserver(this::onPasteText);

        View enterCodeFourView = findViewById(R.id.enter_invitation_code_activity_enter_code_four_content_view);

        layoutParams = enterCodeFourView.getLayoutParams();
        layoutParams.width = editTextWidth;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) enterCodeFourView.getLayoutParams();
        marginLayoutParams.rightMargin = editTextMargin;

        enterCodeFourView.setBackground(enterCodeViewBackground);

        mEnterCodeFourEditText = findViewById(R.id.enter_invitation_code_activity_enter_code_four_edit_text_view);

        Design.updateTextFont(mEnterCodeFourEditText, Design.FONT_BOLD68);
        mEnterCodeFourEditText.setTextColor(Design.FONT_COLOR_DEFAULT);
        mEnterCodeFourEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        mEnterCodeFourEditText.setFilters(new InputFilter[]{alphaNumericFilter, lengthFilter});
        mEnterCodeFourEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().isEmpty()) {
                    mEnterCodeFiveEditText.requestFocus();
                    updateViews();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mEnterCodeFourEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN && mEnterCodeFourEditText.getText() != null && mEnterCodeFourEditText.getText().toString().isEmpty()) {
                mEnterCodeThreeEditText.setText("");
                mEnterCodeThreeEditText.requestFocus();
                updateViews();
            }
            return false;
        });

        mEnterCodeFourEditText.setPasteObserver(this::onPasteText);

        View enterCodeFiveView = findViewById(R.id.enter_invitation_code_activity_enter_code_five_content_view);

        layoutParams = enterCodeFiveView.getLayoutParams();
        layoutParams.width = editTextWidth;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) enterCodeFiveView.getLayoutParams();
        marginLayoutParams.rightMargin = editTextMargin;

        enterCodeFiveView.setBackground(enterCodeViewBackground);

        mEnterCodeFiveEditText = findViewById(R.id.enter_invitation_code_activity_enter_code_five_edit_text_view);

        Design.updateTextFont(mEnterCodeFiveEditText, Design.FONT_BOLD68);
        mEnterCodeFiveEditText.setTextColor(Design.FONT_COLOR_DEFAULT);
        mEnterCodeFiveEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        mEnterCodeFiveEditText.setFilters(new InputFilter[]{alphaNumericFilter, lengthFilter});
        mEnterCodeFiveEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().isEmpty()) {
                    mEnterCodeSixEditText.requestFocus();
                    updateViews();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mEnterCodeFiveEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN && mEnterCodeFiveEditText.getText() != null && mEnterCodeFiveEditText.getText().toString().isEmpty()) {
                mEnterCodeFourEditText.setText("");
                mEnterCodeFourEditText.requestFocus();
                updateViews();
            }
            return false;
        });

        mEnterCodeFiveEditText.setPasteObserver(this::onPasteText);

        View enterCodeSixView = findViewById(R.id.enter_invitation_code_activity_enter_code_six_content_view);

        layoutParams = enterCodeSixView.getLayoutParams();
        layoutParams.width = editTextWidth;

        enterCodeSixView.setBackground(enterCodeViewBackground);

        mEnterCodeSixEditText = findViewById(R.id.enter_invitation_code_activity_enter_code_six_edit_text_view);

        Design.updateTextFont(mEnterCodeSixEditText, Design.FONT_BOLD68);
        mEnterCodeSixEditText.setTextColor(Design.FONT_COLOR_DEFAULT);
        mEnterCodeSixEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        mEnterCodeSixEditText.setFilters(new InputFilter[]{alphaNumericFilter, lengthFilter});
        mEnterCodeSixEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                updateViews();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mEnterCodeSixEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN && mEnterCodeSixEditText.getText() != null && mEnterCodeSixEditText.getText().toString().isEmpty()) {
                mEnterCodeFiveEditText.setText("");
                mEnterCodeFiveEditText.requestFocus();
                updateViews();
            }
            return false;
        });

        mEnterCodeSixEditText.setPasteObserver(this::onPasteText);

        mConfirmView = findViewById(R.id.enter_invitation_code_activity_confirm_view);
        mConfirmView.setAlpha(0.5f);
        mConfirmView.setOnClickListener(v -> onConfirmClick());

        layoutParams = mConfirmView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Design.getMainStyle());
        mConfirmView.setBackground(saveViewBackground);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mConfirmView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONFIRM_TOP_MARGIN * Design.HEIGHT_RATIO);

        TextView confirmTextView = findViewById(R.id.enter_invitation_code_activity_confirm_title_view);
        Design.updateTextFont(confirmTextView, Design.FONT_BOLD28);
        confirmTextView.setTextColor(Color.WHITE);

        TextView messageView = findViewById(R.id.enter_invitation_code_activity_message_view);
        Design.updateTextFont(messageView, Design.FONT_REGULAR32);
        messageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        layoutParams = messageView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) messageView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_TOP_MARGIN * Design.HEIGHT_RATIO);

        mOverlayView = findViewById(R.id.enter_invitation_code_activity_overlay_view);
        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);

        mProgressBarView = findViewById(R.id.enter_invitation_code_activity_progress_bar);
    }

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        mEnableConfirmView = false;

        if (mEnterCodeOneEditText.getText() == null
                || mEnterCodeTwoEditText.getText() == null
                || mEnterCodeThreeEditText.getText() == null
                || mEnterCodeFourEditText.getText() == null
                || mEnterCodeFiveEditText.getText() == null
                || mEnterCodeSixEditText.getText() == null) {
            return;
        }

        if (!mEnterCodeOneEditText.getText().toString().isEmpty()
        && !mEnterCodeTwoEditText.getText().toString().isEmpty()
        && !mEnterCodeThreeEditText.getText().toString().isEmpty()
        && !mEnterCodeFourEditText.getText().toString().isEmpty()
        && !mEnterCodeFiveEditText.getText().toString().isEmpty()
        && !mEnterCodeSixEditText.getText().toString().isEmpty()) {
            mEnableConfirmView = true;
        }

        mConfirmView.setAlpha(mEnableConfirmView ? 1.0f : 0.5f);
    }

    private void onConfirmClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConfirmClick");
        }

        if (mEnterCodeOneEditText.getText() == null
                || mEnterCodeTwoEditText.getText() == null
                || mEnterCodeThreeEditText.getText() == null
                || mEnterCodeFourEditText.getText() == null
                || mEnterCodeFiveEditText.getText() == null
                || mEnterCodeSixEditText.getText() == null) {
            return;
        }

        if (mEnableConfirmView) {

            mOverlayView.setVisibility(View.VISIBLE);
            mProgressBarView.setVisibility(View.VISIBLE);

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);

            hideKeyboard();

            String code = "";
            code += mEnterCodeOneEditText.getText().toString();
            code += mEnterCodeTwoEditText.getText().toString();
            code += mEnterCodeThreeEditText.getText().toString();
            code += mEnterCodeFourEditText.getText().toString();
            code += mEnterCodeFiveEditText.getText().toString();
            code += mEnterCodeSixEditText.getText().toString();

            mInvitationCodeService.getInvitationCode(code);
        }
    }

    private void showOnboarding(boolean fromInfo) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showOnboarding");
        }

        hideKeyboard();

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.enter_invitation_code_activity_layout);

        DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        defaultConfirmView.setLayoutParams(layoutParams);
        defaultConfirmView.hideTitleView();

        defaultConfirmView.useLargeImage();
        defaultConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.onboarding_mini_code, null));

        String message = getString(R.string.enter_invitation_code_activity_onboarding_message) + "\n\n" + getString(R.string.enter_invitation_code_activity_certified_message);
        defaultConfirmView.setMessage(message);
        defaultConfirmView.setConfirmTitle(getString(R.string.enter_invitation_code_activity_enter_code));

        if (fromInfo) {
            defaultConfirmView.hideCancelView();
        } else {
            defaultConfirmView.setCancelTitle(getString(R.string.application_do_not_display));
        }

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                defaultConfirmView.animationCloseConfirmView();
                getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.ENTER_MINI_CODE, false);
            }

            @Override
            public void onDismissClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                percentRelativeLayout.removeView(defaultConfirmView);
                setStatusBarColor();

                mEnterCodeOneEditText.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.showSoftInput(mEnterCodeOneEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        };
        defaultConfirmView.setObserver(observer);
        percentRelativeLayout.addView(defaultConfirmView);
        defaultConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void showInvitation(TwincodeOutbound twincodeOutbound) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showInvitation");
        }

        mInvitationCodeService.getImage(twincodeOutbound.getAvatarId(), (Bitmap avatar) -> {
            PercentRelativeLayout percentRelativeLayout = findViewById(R.id.enter_invitation_code_activity_layout);

            InvitationCodeConfirmView invitationCodeConfirmView = new InvitationCodeConfirmView(this, null);
            PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            invitationCodeConfirmView.setLayoutParams(layoutParams);

            invitationCodeConfirmView.setAvatar(avatar, false);
            invitationCodeConfirmView.setTitle(twincodeOutbound.getName());

            String message = String.format(getString(R.string.accept_invitation_activity_message), twincodeOutbound.getName())+ "\n\n" + getString(R.string.enter_invitation_code_activity_invitation_message);
            invitationCodeConfirmView.setMessage(message);

            AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
                @Override
                public void onConfirmClick() {
                    mInvitationCodeService.createContact(twincodeOutbound);
                    invitationCodeConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCancelClick() {
                    invitationCodeConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onDismissClick() {
                    invitationCodeConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    percentRelativeLayout.removeView(invitationCodeConfirmView);
                    setStatusBarColor();
                }
            };
            invitationCodeConfirmView.setObserver(observer);

            percentRelativeLayout.addView(invitationCodeConfirmView);
            invitationCodeConfirmView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        });
    }

    private void onPasteText() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPasteText");
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription() != null && (clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) || clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML))) {
            if (clipboard.getPrimaryClip() != null) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                String code = item.getText().toString();
                if (code.length() > 5) {
                    mEnterCodeOneEditText.setText(code.substring(0,1));
                    mEnterCodeTwoEditText.setText(code.substring(1,2));
                    mEnterCodeThreeEditText.setText(code.substring(2,3));
                    mEnterCodeFourEditText.setText(code.substring(3,4));
                    mEnterCodeFiveEditText.setText(code.substring(4,5));
                    mEnterCodeSixEditText.setText(code.substring(5,6));
                }

            }
        }
    }

    private void hideKeyboard() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideKeyboard");
        }

        InputMethodManager inputMethodManager = (InputMethodManager) mEnterCodeOneEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mEnterCodeOneEditText.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(mEnterCodeTwoEditText.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(mEnterCodeThreeEditText.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(mEnterCodeFourEditText.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(mEnterCodeFiveEditText.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(mEnterCodeSixEditText.getWindowToken(), 0);
        }
    }

    private void resetCode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "resetCode");
        }

        mEnterCodeOneEditText.setText("");
        mEnterCodeTwoEditText.setText("");
        mEnterCodeThreeEditText.setText("");
        mEnterCodeFourEditText.setText("");
        mEnterCodeFiveEditText.setText("");
        mEnterCodeSixEditText.setText("");
        mEnterCodeOneEditText.requestFocus();
    }

}
