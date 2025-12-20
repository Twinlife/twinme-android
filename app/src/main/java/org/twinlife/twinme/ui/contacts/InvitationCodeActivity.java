/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.AssertPoint;
import org.twinlife.twinlife.BaseService;
import org.twinlife.twinlife.TwincodeOutbound;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Invitation;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.services.InvitationCodeService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.ApplicationAssertPoint;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.DefaultConfirmView;
import org.twinlife.twinme.utils.SwipeItemTouchHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InvitationCodeActivity extends AbstractTwinmeActivity implements InvitationCodeService.Observer {
    private static final String LOG_TAG = "InvitationCodeActivity";
    private static final boolean DEBUG = false;

    private View mOverlayView;

    private InvitationCodeAdapter mInvitationCodeAdapter;

    private final ArrayList<UIInvitationCode> mUIInvitationCodeList = new ArrayList<>();
    private UIInvitationCode mUIInvitationCode;

    private boolean mShowOnboarding = false;

    private InvitationCodeService mInvitationCodeService;

    private Profile mProfile;

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
    }

    //
    // Override Activity methods
    //

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
    public void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (!mShowOnboarding && getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.MINI_CODE)) {
            mShowOnboarding = true;
            showOnboarding(false);
        }

        if (mUIInvitationCodeList.isEmpty()) {
            mInvitationCodeService.getInvitations();
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
    public void onCreateInvitationWithCode(@Nullable Invitation invitation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateInvitationWithCode: invitation=" + invitation);
        }

        mOverlayView.setVisibility(View.GONE);
        mProgressBarView.setVisibility(View.GONE);
        setStatusBarColor();

        mUIInvitationCode = addInvitationCode(invitation);

        if (mUIInvitationCode != null) {
            showShareView(mUIInvitationCode);
        }
    }

    @Override
    public void onGetInvitationCode(@Nullable TwincodeOutbound twincodeOutbound, @Nullable Bitmap bitmap, @Nullable String publicKey) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetInvitationCode: twincodeOutbound=" + twincodeOutbound + " publicKey=" + publicKey);
        }

    }

    @Override
    public void onGetInvitationCodeNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetInvitationCodeNotFound");
        }

        mOverlayView.setVisibility(View.GONE);
        mProgressBarView.setVisibility(View.GONE);
    }

    @Override
    public void onLimitInvitationCodeReach() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLimitInvitationCodeReach");
        }

        mOverlayView.setVisibility(View.GONE);
        mProgressBarView.setVisibility(View.GONE);
        error(getString(R.string.invitation_code_activity_limit_message), null);
    }

    @Override
    public void onGetLocalInvitationCode() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetLocalInvitationCode");
        }

    }

    @Override
    public void onGetInvitations(@Nullable List<Invitation> invitations) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetInvitations: invitations=" + invitations);
        }

        if (invitations == null) {
            return;
        }

        for (Invitation invitation : invitations) {
            UIInvitationCode uiInvitationCode = addInvitationCode(invitation);
            if (uiInvitationCode != null) {
                mUIInvitationCodeList.add(0, uiInvitationCode);
            }
        }

        mInvitationCodeAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetDefaultProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProfile profile=" + profile);
        }

        mProfile = profile;
    }

    @Override
    public void onGetDefaultProfileNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDefaultProfileNotFound");
        }

        finish();
    }

    @Override
    public void onDeleteInvitation(@NonNull UUID invitationId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteInvitation invitationId=" + invitationId);
        }

        for (UIInvitationCode invitationCode : mUIInvitationCodeList) {
            if (invitationCode.getInvitationId() == invitationId) {
                mUIInvitationCodeList.remove(invitationCode);
                break;
            }
        }

        mInvitationCodeAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContact(@NonNull Contact contact) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateContact contact=" + contact);
        }

    }

    @Override
    public void onError(BaseService.ErrorCode errorCode, @Nullable String message, @Nullable Runnable errorCallback) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError errorCode=" + errorCode);
        }

        mOverlayView.setVisibility(View.GONE);
        mProgressBarView.setVisibility(View.GONE);

        setStatusBarColor();

        super.onError(errorCode, message, errorCallback);
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.invitation_code_activity);

        setStatusBarColor();
        setTitle(getString(R.string.add_contact_activity_invitation_code_title));
        setToolBar(R.id.invitation_code_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setBackgroundColor(Design.GREY_BACKGROUND_COLOR);

        applyInsets(R.id.invitation_code_activity_layout, R.id.invitation_code_activity_tool_bar, R.id.invitation_code_activity_list_view, Design.TOOLBAR_COLOR, false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        RecyclerView codeInvitationRecyclerView = findViewById(R.id.invitation_code_activity_list_view);
        codeInvitationRecyclerView.setLayoutManager(linearLayoutManager);
        codeInvitationRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        codeInvitationRecyclerView.setItemAnimator(null);
        codeInvitationRecyclerView.setBackgroundColor(Design.GREY_BACKGROUND_COLOR);

        SwipeItemTouchHelper.OnSwipeItemClickListener onSwipeItemClickListener = new SwipeItemTouchHelper.OnSwipeItemClickListener() {
            @Override
            public void onLeftActionClick(int adapterPosition) {

            }

            @Override
            public void onRightActionClick(int adapterPosition) {
                // Account for title and "create code" rows
                adapterPosition -= 2;
                if (adapterPosition >= 0 && adapterPosition < mUIInvitationCodeList.size()) {
                    onUIInvitationCodeDeleteClick(mUIInvitationCodeList.get(adapterPosition));
                } else {
                    getTwinmeContext().assertion(ApplicationAssertPoint.INVALID_POSITION,
                            AssertPoint.createLength(mUIInvitationCodeList.size()).put(adapterPosition));
                }
            }

            @Override
            public void onOtherActionClick(int adapterPosition) {

            }
        };
        SwipeItemTouchHelper swipeItemTouchHelper = new SwipeItemTouchHelper(codeInvitationRecyclerView, null, SwipeItemTouchHelper.ButtonType.DELETE, onSwipeItemClickListener);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeItemTouchHelper);
        itemTouchHelper.attachToRecyclerView(codeInvitationRecyclerView);

        InvitationCodeAdapter.OnInvitationCodeListener invitationCodeListener = new InvitationCodeAdapter.OnInvitationCodeListener() {
            @Override
            public void onInvitationCodeClick(int position) {

                if (position >= 0) {
                    onUIInvitationCodeClick(mUIInvitationCodeList.get(position));
                }
            }

            @Override
            public void onAddInvitationCodeClick() {

                onAddCodeClick();
            }
        };

        mInvitationCodeAdapter = new InvitationCodeAdapter(this, mUIInvitationCodeList, invitationCodeListener);
        codeInvitationRecyclerView.setAdapter(mInvitationCodeAdapter);

        mOverlayView = findViewById(R.id.invitation_code_activity_overlay_view);
        mOverlayView.setBackgroundColor(Design.OVERLAY_VIEW_COLOR);

        mProgressBarView = findViewById(R.id.invitation_code_activity_progress_bar);
    }

    private void onUIInvitationCodeClick(UIInvitationCode uiInvitationCode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUIInvitationCodeClick: uiInvitationCode=" + uiInvitationCode);
        }

    }

    private void onUIInvitationCodeDeleteClick(UIInvitationCode uiInvitationCode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUIInvitationCodeDeleteClick: uiInvitationCode=" + uiInvitationCode);
        }

        mInvitationCodeService.deleteInvitation(uiInvitationCode.getInvitation());
    }

    private void onAddCodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddCodeClick");
        }

        mOverlayView.setVisibility(View.VISIBLE);
        mProgressBarView.setVisibility(View.VISIBLE);

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);

        mInvitationCodeService.createInvitationWithCode(false);
    }

    private UIInvitationCode addInvitationCode(Invitation invitation) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addInvitationCode: " + invitation);
        }

        if (invitation != null && invitation.getInvitationCode() != null) {
            long expirationDate = (invitation.getCreationDate() / 1000) + (60L * 60 * invitation.getInvitationCode().getValidityPeriod());
            UIInvitationCode invitationCode = new UIInvitationCode(invitation, invitation.getInvitationCode().getCode(), expirationDate);
            return invitationCode;
        }

        return null;
    }

    private void showOnboarding(boolean fromInfo) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showOnboarding");
        }

        ViewGroup viewGroup = findViewById(R.id.invitation_code_activity_layout);

        DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
        defaultConfirmView.hideTitleView();

        defaultConfirmView.useLargeImage();
        defaultConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.onboarding_mini_code, null));

        String message = getString(R.string.invitation_code_activity_onboarding_message) + "\n\n" + getString(R.string.invitation_code_activity_success_message);
        defaultConfirmView.setMessage(message);

        if (fromInfo) {
            defaultConfirmView.hideCancelView();
            defaultConfirmView.setConfirmTitle(getString(R.string.application_ok));
        } else {
            defaultConfirmView.setCancelTitle(getString(R.string.application_do_not_display));
            defaultConfirmView.setConfirmTitle(getString(R.string.welcome_activity_next));
        }

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                defaultConfirmView.animationCloseConfirmView();
                getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.MINI_CODE, false);
            }

            @Override
            public void onDismissClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(defaultConfirmView);
                setStatusBarColor();
            }
        };
        defaultConfirmView.setObserver(observer);
        viewGroup.addView(defaultConfirmView);
        defaultConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void showShareView(UIInvitationCode invitationCode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showShareView: " + invitationCode);
        }

        ViewGroup viewGroup = findViewById(R.id.invitation_code_activity_layout);
        InvitationCodeShareView invitationCodeShareView = new InvitationCodeShareView(this, null);
        invitationCodeShareView.setTitle(invitationCode.getCode());

        String message = getString(R.string.invitation_code_activity_onboarding_message) + "\n\n" + getString(R.string.invitation_code_activity_success_message);
        invitationCodeShareView.setMessage(message);

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                invitationCodeShareView.animationCloseConfirmView();

                if (mUIInvitationCode != null) {
                    mUIInvitationCodeList.add(0, mUIInvitationCode);
                    mInvitationCodeAdapter.notifyDataSetChanged();
                    mUIInvitationCode = null;
                }
            }

            @Override
            public void onCancelClick() {
                invitationCodeShareView.animationCloseConfirmView();

                if (mUIInvitationCode != null) {
                    mInvitationCodeService.deleteInvitation(mUIInvitationCode.getInvitation());
                    mUIInvitationCode = null;
                }
            }

            @Override
            public void onDismissClick() {
                invitationCodeShareView.animationCloseConfirmView();

                if (mUIInvitationCode != null) {
                    mInvitationCodeService.deleteInvitation(mUIInvitationCode.getInvitation());
                    mUIInvitationCode = null;
                }
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(invitationCodeShareView);
                setStatusBarColor();
            }
        };
        invitationCodeShareView.setObserver(observer);
        viewGroup.addView(invitationCodeShareView);
        invitationCodeShareView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }
}
