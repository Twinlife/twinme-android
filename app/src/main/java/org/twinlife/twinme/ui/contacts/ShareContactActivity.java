/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (fabrice.trescartes@twin.life)
 */

package org.twinlife.twinme.ui.contacts;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.ContactsService;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.spaces.SpacesActivity;
import org.twinlife.twinme.ui.users.UIContact;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.OnboardingConfirmView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ShareContactActivity extends AbstractTwinmeActivity implements ContactsService.Observer {
    private static final String LOG_TAG = "ShareContactActivity";
    private static final boolean DEBUG = false;

    private static final int CHANGE_SPACE = 1;

    private static final float DESIGN_BOTTOM_VIEW_HEIGHT = 128f;
    private static final float DESIGN_AVATAR_SIZE = 86f;
    private static final float DESIGN_SEND_VIEW_SIZE = 72f;
    private static final float DESIGN_SEND_ICON_SIZE = 30f;
    private static final float DESIGN_HORIZONTAL_MARGIN = 32f;

    private static final float DESIGN_ITEM_ROUND_CORNER_RADIUS_DP = 9;

    private ContactsService mContactService;
    private ShareContactAdapter mShareContactAdapter;
    private boolean mUIInitialized = false;

    private final List<UIContact> mUIContacts = new ArrayList<>();

    private View mBottomView;
    private CircularImageView mAvatarView;
    private TextView mShareContactTextView;

    private UUID mContactId;

    private UIContact mSelectedContact;
    private boolean mShowOnboardingView = false;

    private ShapeableImageView mSpaceAvatarView;
    private GradientDrawable mNoAvatarGradientDrawable;
    private View mNoAvatarView;
    private TextView mSpaceNameView;

    private Space mSpace;

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
        String contactId = intent.getStringExtra(Intents.INTENT_CONTACT_ID);
        if (contactId != null) {
            mContactId = UUID.fromString(contactId);
        }

        mContactService = new ContactsService(this, getTwinmeContext(), this);

        initViews();
    }

    //
    // Override Activity methods
    //

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mContactService.dispose();

        super.onDestroy();
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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            showOnboardingView();
        }
    }

    @Override
    public void onApplyInsetsFinish() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onApplyInsetsFinish");
        }

        super.onApplyInsetsFinish();

        if (mBottomView != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mBottomView.getLayoutParams();
            marginLayoutParams.bottomMargin = getBarBottomInset();
        }

        showOnboardingView();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateOptionsMenu: menu=" + menu);
        }

        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.share_contact_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.space_action);

        android.view.View actionView = menuItem.getActionView();
        if (actionView == null) {
            return true;
        }

        if (mContactService.numberSpaces(false) <= 1) {
            actionView.setVisibility(View.GONE);
            return false;
        }

        actionView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);

        mSpaceAvatarView = actionView.findViewById(R.id.toolbar_space_item_avatar_view);
        if (mSpaceAvatarView != null) {
            float corner = DESIGN_ITEM_ROUND_CORNER_RADIUS_DP * Resources.getSystem().getDisplayMetrics().density;
            mSpaceAvatarView.setShapeAppearanceModel(
                    mSpaceAvatarView.getShapeAppearanceModel()
                            .toBuilder()
                            .setAllCornerSizes(corner)
                            .build());

            mSpaceAvatarView.setOnClickListener(view -> onSpaceClick());
        }

        mNoAvatarView = actionView.findViewById(R.id.toolbar_space_item_no_avatar_view);
        if (mNoAvatarView != null) {
            mNoAvatarGradientDrawable = new GradientDrawable();
            mNoAvatarGradientDrawable.mutate();
            mNoAvatarGradientDrawable.setStroke((int) (2 * Resources.getSystem().getDisplayMetrics().density), Color.WHITE);
            mNoAvatarGradientDrawable.setColor(Design.BACKGROUND_COLOR_GREY);
            mNoAvatarGradientDrawable.setShape(GradientDrawable.RECTANGLE);
            mNoAvatarView.setBackground(mNoAvatarGradientDrawable);
            mNoAvatarView.setOnClickListener(view -> onSpaceClick());
        }

        mSpaceNameView = actionView.findViewById(R.id.toolbar_space_item_no_avatar_text_view);
        if (mSpaceNameView != null) {
            mSpaceNameView.setTypeface(Design.FONT_BOLD36.typeface);
            mSpaceNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD36.size);
            mSpaceNameView.setTextColor(Color.WHITE);
        }

        if (mSpace != null) {
            updateSpace(mSpace);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHANGE_SPACE) {
            String value = data != null ? data.getStringExtra(Intents.INTENT_SPACE_SELECTION) : null;
            if (value != null) {
                mContactService.updateSpace(UUID.fromString(value));
            }
        }
    }

    //
    // Implement ContactsService.Observer methods
    //

    @Override
    public void showProgressIndicator() {

    }

    @Override
    public void hideProgressIndicator() {

    }

    @Override
    public void onUpdateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateSpace: space=" + space);
        }

        mSpace = space;
        updateSpace(space);
    }

    @Override
    public void onGetContacts(@NonNull List<Contact> contacts) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContacts: contacts=" + contacts);
        }

        mShareContactAdapter.setContacts(contacts, mContactId);

        if (mUIInitialized) {
            mShareContactAdapter.updateContacts();
        }
    }

    @Override
    public void onCreateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateContact: contact=" + contact);
        }

        mShareContactAdapter.updateUIContact(contact, avatar);

        if (mUIInitialized) {
            mShareContactAdapter.updateContacts();
        }
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: contact=" + contact);
        }

        mShareContactAdapter.updateUIContact(contact, avatar);

        if (mUIInitialized) {
            mShareContactAdapter.updateContacts();
        }
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
    public void onDeleteContact(@NonNull UUID contactId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteContact: contactId=" + contactId);
        }

        mShareContactAdapter.removeUIContact(contactId);

        if (mUIInitialized) {
            mShareContactAdapter.updateContacts();
        }
    }

    @Override
    public void onSetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpace: space=" + space);
        }

    }

    public void onUIContactClick(UIContact uiContact) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUIContactLongClick uiContact=" + uiContact);
        }

        if (uiContact.getContact().hasPeer()) {
            mSelectedContact = uiContact;
            updateBottomView();
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
        setContentView(R.layout.share_contact_activity);

        setStatusBarColor();
        setToolBar(R.id.share_contact_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);

        setTitle(getString(R.string.show_contact_view_share_contact));
        setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        applyInsets(R.id.share_contact_activity_layout, R.id.share_contact_activity_tool_bar, R.id.share_contact_activity_list_view, Design.TOOLBAR_COLOR, false);

        mShareContactAdapter = new ShareContactAdapter(this, mContactService, Design.ITEM_VIEW_HEIGHT, mUIContacts, R.layout.contacts_fragment_contact_item, R.id.contacts_fragment_contact_item_name_view, R.id.contacts_fragment_contact_item_avatar_view, R.id.contacts_fragment_contact_item_tag_view, R.id.contacts_fragment_contact_item_tag_title_view, R.id.contacts_fragment_contact_item_certified_image_view, R.id.contacts_fragment_contact_item_separator_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        RecyclerView recyclerView = findViewById(R.id.share_contact_activity_list_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mShareContactAdapter);
        recyclerView.setItemAnimator(null);
        recyclerView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mBottomView = findViewById(R.id.share_contact_activity_bottom_view);
        mBottomView.setBackgroundColor(Design.WHITE_COLOR);
        mBottomView.setVisibility(View.GONE);

        ViewGroup.LayoutParams layoutParams = mBottomView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_BOTTOM_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mBottomView.getLayoutParams();
        marginLayoutParams.bottomMargin = getBarBottomInset();

        View separatorView = findViewById(R.id.share_contact_activity_bottom_header_view);
        separatorView.setBackgroundColor(Design.SEPARATOR_COLOR);

        mAvatarView = findViewById(R.id.share_contact_activity_avatar_view);

        layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_AVATAR_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mAvatarView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        mShareContactTextView = findViewById(R.id.share_contact_activity_share_text_view);
        mShareContactTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        Design.updateTextFont(mShareContactTextView, Design.FONT_REGULAR32);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mShareContactTextView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        View sendView = findViewById(R.id.share_contact_activity_send_clickable_view);
        sendView.setOnClickListener(v -> onSendClick());

        layoutParams = sendView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_SEND_VIEW_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_SEND_VIEW_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) sendView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        View sendRoundedView = findViewById(R.id.share_contact_activity_send_rounded_view);

        float radius =  (DESIGN_SEND_VIEW_SIZE * Design.HEIGHT_RATIO) * 0.5f;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable sendBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        sendBackground.getPaint().setColor(Design.getMainStyle());
        sendRoundedView.setBackground(sendBackground);

        ImageView sendImageView = findViewById(R.id.share_contact_activity_send_image_view);
        sendImageView.setColorFilter(Color.WHITE);

        layoutParams = sendImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_SEND_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_SEND_ICON_SIZE * Design.HEIGHT_RATIO);

        mUIInitialized = true;
    }

    private void onSendClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendClick");
        }

        if (mSelectedContact != null) {
            Intent data = new Intent();
            data.putExtra(Intents.INTENT_SHARE_CONTACT_ID, mSelectedContact.getContact().getId().toString());
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void onSpaceClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSwitchSpaceClick");
        }

        Intent intent = new Intent();
        intent.putExtra(Intents.INTENT_PICKER_MODE, true);
        intent.setClass(this, SpacesActivity.class);

        startActivityForResult(intent, CHANGE_SPACE);
    }

    private void showOnboardingView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showOnboardingView");
        }

        if (mShowOnboardingView || !getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.SHARE_CONTACT)) {
           return;
        }

        mShowOnboardingView = true;
        ViewGroup viewGroup = findViewById(R.id.share_contact_activity_layout);

        String message = getString(R.string.share_contact_view_onboarding_part_1)
                + "\n\n" + getString(R.string.share_contact_view_onboarding_part_2)
                + "\n\n" + getString(R.string.share_contact_view_onboarding_part_3);

        OnboardingConfirmView onboardingConfirmView = new OnboardingConfirmView(this, null);
        onboardingConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), R.drawable.onboarding_share_contact, null));
        onboardingConfirmView.setTitle(getString(R.string.privacy_view_share_invitation_title));
        onboardingConfirmView.setMessage(message);
        onboardingConfirmView.setConfirmTitle(getString(R.string.application_ok));
        onboardingConfirmView.setCancelTitle(getString(R.string.application_do_not_display));

        AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
            @Override
            public void onConfirmClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                onboardingConfirmView.animationCloseConfirmView();
                getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.SHARE_CONTACT, false);
            }

            @Override
            public void onDismissClick() {
                onboardingConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                viewGroup.removeView(onboardingConfirmView);

                setStatusBarColor();
            }
        };
        onboardingConfirmView.setObserver(observer);
        viewGroup.addView(onboardingConfirmView);
        onboardingConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void updateBottomView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateBottomView");
        }

        if (mSelectedContact != null && mShareContactAdapter.getShareContactName() != null) {
            mBottomView.setVisibility(View.VISIBLE);

            mContactService.getImage(mSelectedContact.getContact(), (Bitmap avatar) -> {
                mAvatarView.setImage(this, null,
                        new CircularImageDescriptor(avatar, 0.5f, 0.5f, 0.5f));
            });

            mShareContactTextView.setText(String.format(getString(R.string.conversation_view_share_contact_item_local_message), mSelectedContact.getName(), mShareContactAdapter.getShareContactName()));
        }
    }

    private void updateSpace(Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSpace");
        }

        if (space == null || mSpaceAvatarView == null || mNoAvatarView == null || mSpaceNameView == null) {
            return;
        }

        float corner = DESIGN_ITEM_ROUND_CORNER_RADIUS_DP * Resources.getSystem().getDisplayMetrics().density;
        float[] radii = new float[8];
        Arrays.fill(radii, corner);
        if (space.getSpaceAvatarId() != null) {
            mSpaceAvatarView.setVisibility(View.VISIBLE);
            mNoAvatarView.setVisibility(View.GONE);
            mContactService.getSpaceImage(space, (Bitmap avatar) -> {
                mSpaceAvatarView.setImageBitmap(avatar);
            });
            mSpaceNameView.setVisibility(View.GONE);
        } else {
            mNoAvatarGradientDrawable.setCornerRadii(radii);
            mSpaceAvatarView.setVisibility(View.GONE);
            mNoAvatarView.setVisibility(View.VISIBLE);
            mSpaceNameView.setVisibility(View.VISIBLE);

            String name = space.getName();
            if (!name.isEmpty()) {
                mSpaceNameView.setText(name.substring(0, 1).toUpperCase());
            }
            mNoAvatarGradientDrawable.setColor(Design.getDefaultColor(space.getSpaceSettings().getStyle()));
        }
    }
}
