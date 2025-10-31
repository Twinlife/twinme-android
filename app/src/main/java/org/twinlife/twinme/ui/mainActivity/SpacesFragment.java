/*
 *  Copyright (c) 2019-2022 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
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
import androidx.appcompat.widget.SearchView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.MenuProvider;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.NotificationService.NotificationStat;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.SpaceService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.spaces.OnboardingSpaceActivity;
import org.twinlife.twinme.ui.spaces.ShowSpaceActivity;
import org.twinlife.twinme.ui.spaces.SpacesAdapter;
import org.twinlife.twinme.ui.spaces.UISpace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class SpacesFragment extends TabbarFragment implements SpaceService.Observer {
    private static final String LOG_TAG = "SpacesFragment";
    private static final boolean DEBUG = false;

    private static final int DESIGN_SHADOW_COLOR = Color.argb(51, 0, 0, 0);
    private static final int DESIGN_SHADOW_OFFSET = 6;
    private static final int DESIGN_SHADOW_RADIUS = 12;

    private static final float DESIGN_SAMPLE_VIEW_WIDTH = 420;
    private static final float DESIGN_SAMPLE_VIEW_HEIGHT = 120;
    private static final float DESIGN_SAMPLE_TOP_MARGIN = 180;
    private static final float DESIGN_SAMPLE_LEFT_MARGIN = 40;
    private static final float DESIGN_SAMPLE_PADDING = 12;
    private static final float DESIGN_MESSAGE_TOP_MARGIN = 100;
    private static final float DESIGN_MORE_INFO_BOTTOM_MARGIN = 20;
    private static final float DESIGN_ITEM_VIEW_HEIGHT = 124;
    private static final float DESIGN_NO_RESULT_IMAGE_VIEW_HEIGHT = 260f;
    private static final float DESIGN_NO_RESULT_IMAGE_VIEW_WIDTH = 380f;
    private static final int SAMPLE_VIEW_WIDTH;
    private static final int SAMPLE_VIEW_HEIGHT;
    private static final int SAMPLE_TOP_MARGIN;
    private static final int SAMPLE_LETF_MARGIN;
    private static final int SAMPLE_PADDING;
    private static final int MESSAGE_TOP_MARGIN;
    private static final int MORE_INFO_BOTTOM_MARGIN;
    private static final int ITEM_VIEW_HEIGHT;

    private static final int ITEM_LIST_CACHE_SIZE = 32;

    static {
        ITEM_VIEW_HEIGHT = (int) (DESIGN_ITEM_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        SAMPLE_VIEW_WIDTH = (int) (DESIGN_SAMPLE_VIEW_WIDTH * Design.WIDTH_RATIO);
        SAMPLE_VIEW_HEIGHT = (int) (DESIGN_SAMPLE_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        SAMPLE_TOP_MARGIN = (int) (DESIGN_SAMPLE_TOP_MARGIN * Design.HEIGHT_RATIO);
        SAMPLE_LETF_MARGIN = (int) (DESIGN_SAMPLE_LEFT_MARGIN * Design.WIDTH_RATIO);
        SAMPLE_PADDING = (int) (DESIGN_SAMPLE_PADDING * Design.HEIGHT_RATIO);
        MESSAGE_TOP_MARGIN = (int) (DESIGN_MESSAGE_TOP_MARGIN * Design.HEIGHT_RATIO);
        MORE_INFO_BOTTOM_MARGIN = (int) (DESIGN_MORE_INFO_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
    }

    private boolean mUIInitialized = false;

    private SearchView mSearchView;
    private RecyclerView mSpacesRecyclerView;
    private View mNoSpaceView;
    private View mNoResultFoundView;
    private TextView mNoResultFoundTitleView;

    private SpacesAdapter mSpacesListAdapter;

    private final List<UISpace> mUISpaces = new ArrayList<>();

    private SpaceService mSpaceService;

    private Contact mContact;
    private Group mGroup;
    private Space mCurrentSpace;

    private boolean mOnGetSpacesDone = false;
    private String mLastSearch = "";

    @Nullable
    private Menu mMenu;

    // Default constructor is required by Android for proper activity restoration.
    public SpacesFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateView: inflater=" + inflater + " container=" + container + " savedInstanceState=" + savedInstanceState);
        }

        View view = inflater.inflate(R.layout.spaces_fragment, container, false);

        mTwinmeActivity = (MainActivity) requireActivity();
        initViews(view);
        addMenu();

        mSpaceService = new SpaceService(mTwinmeActivity, mTwinmeActivity.getTwinmeContext(), this, null);

        return view;
    }

    @Override
    public void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        updateColor();

        if (mSearchView != null && !mSearchView.isIconified()) {
            mLastSearch = mSearchView.getQuery().toString();
        } else {
            mSpaceService.getSpaces();
        }
    }

    private void hideKeyboard() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideKeyboard");
        }

        InputMethodManager inputMethodManager = (InputMethodManager) mTwinmeActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && mSearchView != null) {
            inputMethodManager.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
        }
    }

    //
    // Override Activity methods
    //

    @Override
    public void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mSpaceService.dispose();
        hideKeyboard();

        if (mSearchView != null) {
            mSearchView.setOnQueryTextListener(null);
        }

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);
    }

    //
    // Implement SpaceService.Observer methods
    //

    @Override
    public void showProgressIndicator() {

    }

    @Override
    public void hideProgressIndicator() {

    }

    @Override
    public void onGetSpaces(@NonNull List<Space> spaces) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpaces: spaces=" + spaces);
        }

        mOnGetSpacesDone = true;

        mUISpaces.clear();

        if (mCurrentSpace != null && mCurrentSpace.isSecret()) {
            spaces = new ArrayList<>(spaces);
            spaces.add(mCurrentSpace);
        }

        if (spaces.isEmpty()) {
            mSpaceService.findSpacesNotifications();
            return;
        }

        AtomicInteger avatarCounter = new AtomicInteger(spaces.size());
        for (Space space : spaces) {
            mSpaceService.getSpaceImage(space, (Bitmap spaceAvatar) ->
                    mSpaceService.getProfileImage(space.getProfile(), (Bitmap profileAvatar) -> {
                        mSpacesListAdapter.updateUISpace(space, spaceAvatar, profileAvatar);

                        if (avatarCounter.decrementAndGet() == 0) {
                            mSpaceService.findSpacesNotifications();
                        }
                    }));
        }
    }

    @Override
    public void onUpdateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateSpace: space=" + space);
        }

        mSpaceService.getSpaceImage(space, (Bitmap spaceAvatar) ->
                mSpaceService.getProfileImage(space.getProfile(), (Bitmap profileAvatar) -> {
                    mSpacesListAdapter.updateUISpace(space, spaceAvatar, profileAvatar);

                    if (mUIInitialized) {
                        notifySpacesListChanged();
                    }
                }));
    }

    @Override
    public void onGetSpace(@NonNull Space space, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpace: space=" + space);
        }

        mCurrentSpace = space;

        for (UISpace lSpace : mSpacesListAdapter.getUISpaces()) {
            lSpace.setIsCurrentSpace(lSpace.getSpace().getId() == mCurrentSpace.getId());
        }

        if (mUIInitialized) {
            notifySpacesListChanged();
        }
    }

    @Override
    public void onGetSpaceNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpaceNotFound");
        }
    }

    @Override
    public void onCreateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateSpace: space=" + space);
        }

        mSpaceService.getSpaceImage(space, (Bitmap spaceAvatar) ->
                mSpaceService.getProfileImage(space.getProfile(), (Bitmap profileAvatar) -> {
                    mSpacesListAdapter.updateUISpace(space, spaceAvatar, profileAvatar);

                    if (mUIInitialized) {
                        notifySpacesListChanged();
                    }
                }));
    }

    @Override
    public void onDeleteSpace(@NonNull UUID spaceId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteSpace: spaceId=" + spaceId);
        }

        for (UISpace lSpace : mSpacesListAdapter.getUISpaces()) {
            if (lSpace.getSpace().getId() == spaceId) {
                mSpacesListAdapter.removeUISpace(spaceId);
                break;
            }
        }

        if (mUIInitialized) {
            notifySpacesListChanged();
        }
    }

    @Override
    public void onSetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpace: space=" + space);
        }

        mCurrentSpace = space;

        if (mSpacesListAdapter.getUISpaces().isEmpty()) {
            mSpaceService.getSpaces();
        }

        for (UISpace lSpace : mSpacesListAdapter.getUISpaces()) {
            lSpace.setIsCurrentSpace(lSpace.getSpace().getId() == mCurrentSpace.getId());
        }

        if (space.isSecret()) {
            mSpaceService.getSpaceImage(space, (Bitmap spaceAvatar) ->
                    mSpaceService.getProfileImage(space.getProfile(), (Bitmap profileAvatar) -> {
                        mSpacesListAdapter.updateUISpace(space, spaceAvatar, profileAvatar);
                        if (mUIInitialized) {
                            notifySpacesListChanged();
                        }
                    }));
        }

        for (UISpace lSpace : mSpacesListAdapter.getUISpaces()) {
            if (!lSpace.isCurrentSpace() && lSpace.getSpace().isSecret()) {
                mSpacesListAdapter.removeUISpace(lSpace.getId());
                break;
            }
        }

        if (mUIInitialized) {
            notifySpacesListChanged();
        }
    }

    @Override
    public void onGetSpacesNotifications(@NonNull Map<Space, NotificationStat> spacesNotifications) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpacesNotifications: space=" + spacesNotifications);
        }

        for (UISpace space : mUISpaces) {
            NotificationStat stat = spacesNotifications.get(space.getSpace());
            space.setHasNotification(stat != null && stat.getPendingCount() > 0);

            if (mContact != null && mContact.isSpace(space.getSpace())) {
                space.setIsCurrentSpace(true);
            } else if (mGroup != null && mGroup.isSpace(space.getSpace())) {
                space.setIsCurrentSpace(true);
            } else if (mContact == null && mGroup == null && mCurrentSpace != null && mCurrentSpace.getId() == space.getId()) {
                space.setIsCurrentSpace(true);
            }
        }

        if (mUIInitialized) {
            notifySpacesListChanged();
        }
    }

    @Override
    public void onUpdatePendingNotifications(boolean hasPendingNotification) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdatePendingNotifications: hasPendingNotification=" + hasPendingNotification);
        }

        mSpaceService.findSpacesNotifications();
    }

    @Override
    public void onUpdateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateProfile: profile=" + profile);
        }

    }

    @Override
    public void onGetContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContact: contact=" + contact);
        }

        mContact = contact;
    }

    @Override
    public void onUpdateContact(@NonNull Contact contact, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateContact: contact=" + contact);
        }

    }

    @Override
    public void onGetContactNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContactNotFound");
        }

    }

    @Override
    public void onGetContacts(@NonNull List<Contact> contacts) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContacts: contacts=" + contacts);
        }

    }

    @Override
    public void onGetGroups(@NonNull List<Group> groups) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroups: groups=" + groups);
        }

    }

    @Override
    public void onGetGroup(@NonNull Group group, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroup");
        }

        mGroup = group;
    }

    @Override
    public void onUpdateGroup(@NonNull Group group) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateGroup: group=" + group);
        }

    }

    @Override
    public void onGetGroupNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroupNotFound");
        }

    }

    @Override
    public void onCreateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateProfile: profile=" + profile);
        }

    }

    public void onCreateSpaceClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateSpaceClick");
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded() || mTwinmeActivity == null) {
            return;
        }

        mTwinmeActivity.onCreateSpaceClick();
    }

    public void onMoreInfoClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMoreInfoClick");
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded() || mTwinmeActivity == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(Intents.INTENT_SHOW_FIRST_PART_ONBOARDING, false);
        intent.setClass(mTwinmeActivity, OnboardingSpaceActivity.class);
        startActivity(intent);
        mTwinmeActivity.overridePendingTransition(0, 0);
    }

    //
    // Private methods
    //

    private void initViews(View view) {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        SpacesAdapter.OnSpaceClickListener onSpaceClickListener = position -> {
            UISpace uiSpace = mUISpaces.get(position);
            onShowSpaceClick(uiSpace.getSpace());
        };

        SpacesAdapter.OnSpaceLongClickListener onSpaceLongClickListener = this::onSetCurrentSpaceClick;

        LinearLayoutManager uiSpacesLinearLayoutManager = new LinearLayoutManager(mTwinmeActivity, RecyclerView.VERTICAL, false);
        mSpacesRecyclerView = view.findViewById(R.id.spaces_fragment_spaces_list_view);
        mSpacesRecyclerView.setLayoutManager(uiSpacesLinearLayoutManager);
        mSpacesRecyclerView.setItemViewCacheSize(ITEM_LIST_CACHE_SIZE);
        mSpacesRecyclerView.setItemAnimator(null);

        mSpacesListAdapter = new SpacesAdapter(mTwinmeActivity, ITEM_VIEW_HEIGHT, mUISpaces, onSpaceClickListener, onSpaceLongClickListener);
        mSpacesRecyclerView.setAdapter(mSpacesListAdapter);

        mNoSpaceView = view.findViewById(R.id.spaces_fragment_no_spaces_view);
        mNoSpaceView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        View friendsSpaceContainerView = view.findViewById(R.id.spaces_fragment_sample_space_friends_container_view);
        friendsSpaceContainerView.setPadding(SAMPLE_PADDING,SAMPLE_PADDING,SAMPLE_PADDING,SAMPLE_PADDING);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) friendsSpaceContainerView.getLayoutParams();
        marginLayoutParams.topMargin = SAMPLE_TOP_MARGIN;

        View friendsSpaceView = view.findViewById(R.id.spaces_fragment_sample_space_friends_view);
        ViewGroup.LayoutParams layoutParams = friendsSpaceView.getLayoutParams();
        layoutParams.width = SAMPLE_VIEW_WIDTH;
        layoutParams.height = SAMPLE_VIEW_HEIGHT;

        float radius = Design.POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable spaceViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        spaceViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        spaceViewBackground.getPaint().setShadowLayer(DESIGN_SHADOW_RADIUS, 0, DESIGN_SHADOW_OFFSET, DESIGN_SHADOW_COLOR);
        ViewCompat.setBackground(friendsSpaceView, spaceViewBackground);

        TextView friendsSpaceTextView = view.findViewById(R.id.spaces_fragment_sample_space_friends_text_view);
        friendsSpaceTextView.setTypeface(Design.FONT_MEDIUM34.typeface);
        friendsSpaceTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
        friendsSpaceTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(view.getContext().getString(R.string.spaces_activity_sample_friends));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("\n");
        int startName = spannableStringBuilder.length();
        spannableStringBuilder.append(view.getContext().getString(R.string.spaces_activity_sample_friends_name));
        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.94f), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        friendsSpaceTextView.setText(spannableStringBuilder);

        View familySpaceContainerView = view.findViewById(R.id.spaces_fragment_sample_space_family_container_view);
        familySpaceContainerView.setPadding(SAMPLE_PADDING,SAMPLE_PADDING,SAMPLE_PADDING + SAMPLE_LETF_MARGIN,SAMPLE_PADDING);

        View familySpaceView = view.findViewById(R.id.spaces_fragment_sample_space_family_view);

        layoutParams = familySpaceView.getLayoutParams();
        layoutParams.width = SAMPLE_VIEW_WIDTH;
        layoutParams.height = SAMPLE_VIEW_HEIGHT;

        ViewCompat.setBackground(familySpaceView, spaceViewBackground);

        TextView familySpaceTextView = view.findViewById(R.id.spaces_fragment_sample_space_family_text_view);
        familySpaceTextView.setTypeface(Design.FONT_MEDIUM34.typeface);
        familySpaceTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
        familySpaceTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(view.getContext().getString(R.string.spaces_activity_sample_family));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("\n");
        startName = spannableStringBuilder.length();
        spannableStringBuilder.append(view.getContext().getString(R.string.spaces_activity_sample_family_name));
        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.94f), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        familySpaceTextView.setText(spannableStringBuilder);

        View businessSpaceContainerView = view.findViewById(R.id.spaces_fragment_sample_space_business_container_view);
        businessSpaceContainerView.setPadding(SAMPLE_PADDING,SAMPLE_PADDING,SAMPLE_PADDING,SAMPLE_PADDING);

        View businessSpaceView = view.findViewById(R.id.spaces_fragment_sample_space_business_view);

        layoutParams = businessSpaceView.getLayoutParams();
        layoutParams.width = SAMPLE_VIEW_WIDTH;
        layoutParams.height = SAMPLE_VIEW_HEIGHT;

        ViewCompat.setBackground(businessSpaceView, spaceViewBackground);

        TextView businessSpaceTextView = view.findViewById(R.id.spaces_fragment_sample_space_business_text_view);
        businessSpaceTextView.setTypeface(Design.FONT_MEDIUM34.typeface);
        businessSpaceTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
        businessSpaceTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(view.getContext().getString(R.string.spaces_activity_sample_business));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("\n");
        startName = spannableStringBuilder.length();
        spannableStringBuilder.append(view.getContext().getString(R.string.spaces_activity_sample_business_name));
        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.94f), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        businessSpaceTextView.setText(spannableStringBuilder);

        TextView noSpaceTextView = view.findViewById(R.id.spaces_fragment_no_spaces_text_view);
        noSpaceTextView.setTypeface(Design.FONT_MEDIUM34.typeface);
        noSpaceTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
        noSpaceTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) noSpaceTextView.getLayoutParams();
        marginLayoutParams.topMargin = MESSAGE_TOP_MARGIN;

        View createSpaceView = view.findViewById(R.id.spaces_fragment_create_space_view);
        createSpaceView.setOnClickListener(v -> onCreateSpaceClick());

        radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable createViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        createViewBackground.getPaint().setColor(Design.getMainStyle());
        ViewCompat.setBackground(createSpaceView, createViewBackground);

        layoutParams = createSpaceView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView createSpaceTextView = view.findViewById(R.id.spaces_fragment_create_space_text_view);
        createSpaceTextView.setTypeface(Design.FONT_MEDIUM34.typeface);
        createSpaceTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
        createSpaceTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) createSpaceTextView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BUTTON_MARGIN;
        marginLayoutParams.rightMargin = Design.BUTTON_MARGIN;

        View moreInfoView = view.findViewById(R.id.spaces_fragment_more_info_view);
        moreInfoView.setOnClickListener(v -> onMoreInfoClick());

        layoutParams = moreInfoView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) moreInfoView.getLayoutParams();
        marginLayoutParams.bottomMargin = MORE_INFO_BOTTOM_MARGIN;

        TextView moreInfoTextView = view.findViewById(R.id.spaces_fragment_more_info_text_view);
        moreInfoTextView.setTypeface(Design.FONT_BOLD28.typeface);
        moreInfoTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD28.size);
        moreInfoTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mNoResultFoundView = view.findViewById(R.id.spaces_fragment_no_result_found_view);

        ImageView noResultFoundImageView = view.findViewById(R.id.spaces_fragment_no_result_found_image_view);
        layoutParams = noResultFoundImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_NO_RESULT_IMAGE_VIEW_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_NO_RESULT_IMAGE_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        mNoResultFoundTitleView = view.findViewById(R.id.spaces_fragment_no_result_found_title_view);
        mNoResultFoundTitleView.setTypeface(Design.FONT_MEDIUM34.typeface);
        mNoResultFoundTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
        mNoResultFoundTitleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mUIInitialized = true;
    }

    private void onSetCurrentSpaceClick(int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpaceClick: position=" + position);
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded() && mTwinmeActivity != null) {
            return;
        }

        if (mTwinmeActivity.getTwinmeApplication().hapticFeedbackMode() == TwinmeApplication.HapticFeedbackMode.SYSTEM.ordinal()) {
            mTwinmeActivity.getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        } else if (mTwinmeActivity.getTwinmeApplication().hapticFeedbackMode() == TwinmeApplication.HapticFeedbackMode.ON.ordinal()) {
            mTwinmeActivity.getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }

        if (position >= 0 && position < mUISpaces.size()) {
            Space space = mUISpaces.get(position).getSpace();
            mSpaceService.setSpace(space);
        }
    }

    private void onShowSpaceClick(Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onShowSpaceClick: space=" + space);
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded()) {
            return;
        }

        InputMethodManager inputMethodManager = (InputMethodManager) mTwinmeActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && mSearchView != null) {
            mSearchView.setFocusable(false);
            inputMethodManager.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
        }

        mSpaceService.setSpace(space);

        Intent intent = new Intent();
        intent.putExtra(Intents.INTENT_SPACE_ID, space.getId().toString());
        startActivity(intent, ShowSpaceActivity.class);
    }

    private void notifySpacesListChanged() {
        if (DEBUG) {
            Log.d(LOG_TAG, "notifySpacesListChanged");
        }

        if (mUIInitialized) {
            mSpacesListAdapter.notifyDataSetChanged();

            if (mUISpaces.isEmpty() && mOnGetSpacesDone && mSearchView != null && mSearchView.isIconified()) {
                if (mMenu != null) {
                    MenuItem searchMenuItem = mMenu.findItem(R.id.search_action);
                    setEnabled(searchMenuItem, false);
                    searchMenuItem.setVisible(false);
                }
                mNoSpaceView.setVisibility(View.VISIBLE);
                mNoResultFoundView.setVisibility(View.GONE);
                mSpacesRecyclerView.setVisibility(View.GONE);
            } else if (mOnGetSpacesDone) {
                if (mMenu != null) {
                    MenuItem searchMenuItem = mMenu.findItem(R.id.search_action);
                    setEnabled(searchMenuItem, true);
                    searchMenuItem.setVisible(true);
                }
                mNoSpaceView.setVisibility(View.GONE);
                mSpacesRecyclerView.setVisibility(View.VISIBLE);

                if (mUISpaces.isEmpty() && mSearchView != null && !mSearchView.isIconified()) {
                    mNoResultFoundView.setVisibility(View.VISIBLE);
                } else {
                    mNoResultFoundView.setVisibility(View.GONE);
                }
            }
        }
    }

    private void searchSpaces(String text) {
        if (DEBUG) {
            Log.d(LOG_TAG, "searchSpaces: " + text);
        }

        // This fragment is detached and has no activity: ignore the action.
        if (!isAdded() || mTwinmeActivity == null) {
            return;
        }

        if (!text.isEmpty()) {

            if (mNoResultFoundTitleView != null) {
                mNoResultFoundTitleView.setText(String.format(getString(R.string.conversations_fragment_no_result_found), text));
            }

            mSpaceService.findSpaceByName(text);
        } else {
            mSpaceService.getSpaces();
        }
    }

    private void updateNoSpaceView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateNoSpaceView");
        }

        if (getView() != null) {
            View view = getView();

            View friendsSpaceView = view.findViewById(R.id.spaces_fragment_sample_space_friends_view);
            float radius = Design.POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
            float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

            ShapeDrawable spaceViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
            spaceViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
            spaceViewBackground.getPaint().setShadowLayer(DESIGN_SHADOW_RADIUS, 0, DESIGN_SHADOW_OFFSET, DESIGN_SHADOW_COLOR);
            ViewCompat.setBackground(friendsSpaceView, spaceViewBackground);

            TextView friendsSpaceTextView = view.findViewById(R.id.spaces_fragment_sample_space_friends_text_view);
            friendsSpaceTextView.setTypeface(Design.FONT_MEDIUM34.typeface);
            friendsSpaceTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
            friendsSpaceTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(view.getContext().getString(R.string.spaces_activity_sample_friends));
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.append("\n");
            int startName = spannableStringBuilder.length();
            spannableStringBuilder.append(view.getContext().getString(R.string.spaces_activity_sample_friends_name));
            spannableStringBuilder.setSpan(new RelativeSizeSpan(0.94f), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            friendsSpaceTextView.setText(spannableStringBuilder);

            View familySpaceView = view.findViewById(R.id.spaces_fragment_sample_space_family_view);
            ViewCompat.setBackground(familySpaceView, spaceViewBackground);

            TextView familySpaceTextView = view.findViewById(R.id.spaces_fragment_sample_space_family_text_view);
            familySpaceTextView.setTypeface(Design.FONT_MEDIUM34.typeface);
            familySpaceTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
            familySpaceTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

            spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(view.getContext().getString(R.string.spaces_activity_sample_family));
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.append("\n");
            startName = spannableStringBuilder.length();
            spannableStringBuilder.append(view.getContext().getString(R.string.spaces_activity_sample_family_name));
            spannableStringBuilder.setSpan(new RelativeSizeSpan(0.94f), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            familySpaceTextView.setText(spannableStringBuilder);

            View businessSpaceView = view.findViewById(R.id.spaces_fragment_sample_space_business_view);
            ViewCompat.setBackground(businessSpaceView, spaceViewBackground);

            TextView businessSpaceTextView = view.findViewById(R.id.spaces_fragment_sample_space_business_text_view);
            businessSpaceTextView.setTypeface(Design.FONT_MEDIUM34.typeface);
            businessSpaceTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
            businessSpaceTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

            spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(view.getContext().getString(R.string.spaces_activity_sample_business));
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.append("\n");
            startName = spannableStringBuilder.length();
            spannableStringBuilder.append(view.getContext().getString(R.string.spaces_activity_sample_business_name));
            spannableStringBuilder.setSpan(new RelativeSizeSpan(0.94f), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startName, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            businessSpaceTextView.setText(spannableStringBuilder);

            TextView noSpaceTextView = view.findViewById(R.id.spaces_fragment_no_spaces_text_view);
            noSpaceTextView.setTypeface(Design.FONT_MEDIUM34.typeface);
            noSpaceTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
            noSpaceTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

            View createSpaceView = view.findViewById(R.id.spaces_fragment_create_space_view);
            createSpaceView.setOnClickListener(v -> onCreateSpaceClick());

            radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
            outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
            ShapeDrawable createViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
            createViewBackground.getPaint().setColor(Design.getMainStyle());
            ViewCompat.setBackground(createSpaceView, createViewBackground);

            TextView moreInfoTextView = getView().findViewById(R.id.spaces_fragment_more_info_text_view);
            moreInfoTextView.setTypeface(Design.FONT_BOLD28.typeface);
            moreInfoTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD28.size);
            moreInfoTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        }
    }

    private void addMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "addMenu");
        }

        if (mTwinmeActivity == null) {
            return;
        }

        mTwinmeActivity.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.spaces_menu, menu);

                mMenu = menu;

                MenuItem menuItem = menu.findItem(R.id.add_space_action);

                ImageView imageView = (ImageView) menuItem.getActionView();

                if (imageView != null) {
                    imageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.action_bar_add_space, null));
                    imageView.setPadding(Design.TOOLBAR_IMAGE_ITEM_PADDING, 0, Design.TOOLBAR_IMAGE_ITEM_PADDING, 0);
                    imageView.setOnClickListener(view -> onCreateSpaceClick());
                }

                MenuItem searchItem = menu.findItem(R.id.search_action);
                searchItem.setVisible(false);

                if (mSearchView != null) {
                    mSearchView.setOnQueryTextListener(null);
                }

                mSearchView = (SearchView) searchItem.getActionView();

                if (mSearchView != null) {
                    mSearchView.setQueryHint(getString(R.string.application_search_hint));

                    SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {

                            searchSpaces(newText);
                            return false;
                        }
                    };

                    mSearchView.setOnQueryTextListener(onQueryTextListener);
                }

                if (!mLastSearch.isEmpty()) {
                    searchItem.expandActionView();
                    mSearchView.setQuery(mLastSearch, false);
                    mLastSearch = "";
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Handle menu item clicks here
                return false;
            }

        }, getViewLifecycleOwner());
    }

    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        mNoSpaceView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        updateNoSpaceView();
    }
}
