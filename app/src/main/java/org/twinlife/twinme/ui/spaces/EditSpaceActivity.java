/*
 *  Copyright (c) 2019-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import static org.twinlife.twinme.ui.Intents.INTENT_SPACE_ID;
import static org.twinlife.twinme.ui.Intents.INTENT_SPACE_SELECTION;
import static org.twinlife.twinme.utils.Utils.getScaledAvatar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.Twinlife;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Group;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.models.SpaceSettings;
import org.twinlife.twinme.services.EditSpaceService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractEditActivity;
import org.twinlife.twinme.ui.EditProfileActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.profiles.MenuPhotoView;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.EditableView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.UIMenuSelectAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class EditSpaceActivity extends AbstractEditActivity implements EditSpaceService.Observer, OnColorSpaceTouchListener.OnColorObserver {
    private static final String LOG_TAG = "EditSpaceActivity";
    private static final boolean DEBUG = false;

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        ImageView mImageView;
        File mFile;

        public DownloadImageTask(ImageView imageView, File avatarFile) {
            mImageView = imageView;
            mFile = avatarFile;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bitmap = null;
            try (InputStream in = new java.net.URL(urldisplay).openStream()) {
                bitmap = BitmapFactory.decodeStream(in);
                FileOutputStream outStream = new FileOutputStream(mFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap bitmap) {

            if (bitmap != null) {
                mImageView.setImageBitmap(bitmap);
            }
        }
    }

    private static final int DESIGN_HINT_COLOR = Color.parseColor("#bdbdbd");

    private static final String PICK_SPACE_AVATAR = "PickSpaceAvatar";
    private static final String SPACE_AVATAR_FILE = "SpaceAvatarFile";

    private EditableView mEditableView;
    private View mDeleteView;
    private boolean mCanSave = false;
    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;
    private String mName;
    private String mDescription;
    private Space mSpace;
    private Bitmap mSpaceAvatar;
    private Bitmap mUpdatedSpaceAvatar;
    private Bitmap mUpdatedSpaceLargeAvatar;
    private File mUpdatedSpaceAvatarFile;
    private boolean mPickSpaceAvatar;

    protected ColorSpaceAdapter mUIColorSpaceListAdapter;
    protected List<UIColorSpace> mUIColors;
    protected UIColorSpace mSelectedColor;

    private EditSpaceService mEditSpaceService;

    private boolean mHasContacts = false;
    private boolean mHasGroups = false;

    private boolean mCreateSpace = false;

    private boolean mInitTemplate = false;

    private UITemplateSpace mUITemplateSpace;

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
        UUID spaceId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_SPACE_ID));
        if (spaceId == null) {
            mCreateSpace = true;
        }

        initViews();

        int selection = intent.getIntExtra(Intents.INTENT_SPACE_SELECTION, -1);
        if (selection != -1) {
            UITemplateSpace.TemplateType templateType;
            if (selection == UITemplateSpace.TemplateType.BUSINESS_1.ordinal()) {
                templateType = UITemplateSpace.TemplateType.BUSINESS_1;
            } else if (selection == UITemplateSpace.TemplateType.BUSINESS_2.ordinal()) {
                templateType = UITemplateSpace.TemplateType.BUSINESS_2;
            } else if (selection == UITemplateSpace.TemplateType.FAMILY_1.ordinal()) {
                templateType = UITemplateSpace.TemplateType.FAMILY_1;
            } else if (selection == UITemplateSpace.TemplateType.FAMILY_2.ordinal()) {
                templateType = UITemplateSpace.TemplateType.FAMILY_2;
            } else if (selection == UITemplateSpace.TemplateType.FRIENDS_1.ordinal()) {
                templateType = UITemplateSpace.TemplateType.FRIENDS_1;
            } else if (selection == UITemplateSpace.TemplateType.FRIENDS_2.ordinal()) {
                templateType = UITemplateSpace.TemplateType.FRIENDS_2;
            } else {
                templateType = UITemplateSpace.TemplateType.OTHER;
            }
            mUITemplateSpace = new UITemplateSpace(this, templateType);
            updateViews();
        }

        mPickSpaceAvatar = false;

        if (savedInstanceState != null) {
            mPickSpaceAvatar = savedInstanceState.getBoolean(PICK_SPACE_AVATAR);
            if (mEditableView != null) {
                mEditableView.onCreate(savedInstanceState);
                updateSelectedImage();
            }
            String value = savedInstanceState.getString(SPACE_AVATAR_FILE);
            if (value != null) {
                mUpdatedSpaceAvatarFile = new File(value);
                mUpdatedSpaceAvatar = getScaledAvatar(Uri.fromFile(mUpdatedSpaceAvatarFile));
            }
            updateViews();
        }

        mEditSpaceService = new EditSpaceService(this, getTwinmeContext(), this, spaceId);
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mNameView.getWindowToken(), 0);
        }
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
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();

        mEditSpaceService.dispose();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);

        if (mUpdatedSpaceAvatarFile != null) {
            outState.putString(SPACE_AVATAR_FILE, mUpdatedSpaceAvatarFile.getPath());
        }
        outState.putBoolean(PICK_SPACE_AVATAR, mPickSpaceAvatar);
        if (mEditableView != null) {
            mEditableView.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (mEditableView != null) {
                mEditableView.onActivityResult(requestCode, resultCode, data);

                if (resultCode == Activity.RESULT_OK) {
                    updateSelectedImage();
                }
        }

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

    //
    // Implement SpaceService.Observer methods
    //

    @Override
    public void onGetSpace(@NonNull Space space, @Nullable Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpace: space=" + space);
        }

        if (!mCreateSpace) {
            mSpace = space;

            mName = space.getName();
            mDescription = space.getDescription();

            mSpaceAvatar = avatar;

            mEditSpaceService.findContactsBySpace(mSpace);
            mEditSpaceService.findGroupsBySpace(mSpace);
        }

        updateSpace();
    }

    @Override
    public void onGetSpaceNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpaceNotFound");
        }

        updateSpace();
    }

    @Override
    public void onSetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpace: space=" + space);
        }

        finish();
    }

    @Override
    public void onCreateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateSpace: space=" + space);
        }

        Intent data = new Intent();
        data.putExtra(Intents.INTENT_SPACE_ID, space.getId().toString());
        setResult(RESULT_OK, data);

        Intent intent = new Intent();
        intent.putExtra(INTENT_SPACE_ID, space.getId().toString());

        if (mUITemplateSpace != null) {
            intent.putExtra(INTENT_SPACE_SELECTION, mUITemplateSpace.getTemplateType().ordinal());
        }
        intent.setClass(this, EditProfileActivity.class);
        startActivity(intent);

        finish();
    }

    @Override
    public void onUpdateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateSpace: space=" + space);
        }

        finish();
    }

    @Override
    public void onCreateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateProfile: profile=" + profile);
        }

        updateSpace();
    }

    @Override
    public void onUpdateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateProfile: profile=" + profile);
        }

        updateSpace();
    }

    @Override
    public void onGetContacts(@NonNull List<Contact> contacts) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContacts: contacts=" + contacts);
        }

        mHasContacts = contacts.size() > 0;
    }

    @Override
    public void onGetGroups(@NonNull List<Group> groups) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetGroups: groups=" + groups);
        }

        mHasGroups = groups.size() > 0;
    }

    @Override
    public void onDeleteSpace(@NonNull UUID spaceId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteSpace: spaceId=" + spaceId);
        }

        if (mSpace.getId() == spaceId) {
            finish();
        }
    }

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        if (!mEditableView.onRequestPermissions(grantedPermissions)) {
            message(getString(R.string.application_denied_permissions), 0L, new DefaultMessageCallback(R.string.application_ok) {
            });
        }
    }

    //
    // Implement OnColorSpaceTouchListener.OnColorObserver methods
    //

    @Override
    public boolean onUIColorSpaceClick(RecyclerView recyclerView, int position) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUIColorSpaceClick position=" + position);
        }

        if (position >= 0 && position < mUIColors.size()) {
            mSelectedColor = mUIColors.get(position);
            updateSpaceColor();

            return true;
        }

        return false;
    }

    //
    // Private methods
    //

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.edit_space_activity);

        setTitle(getString(R.string.application_name));
        showToolBar(false);
        showBackButton(true);
        setBackgroundColor(Design.WHITE_COLOR);

        mEditableView = new EditableView(this);

        mAvatarView = findViewById(R.id.edit_space_activity_avatar_view);

        mAvatarView.setOnClickListener(v -> openMenuPhoto());

        ViewGroup.LayoutParams layoutParams = mAvatarView.getLayoutParams();
        layoutParams.width = Design.AVATAR_MAX_WIDTH;
        layoutParams.height = Design.AVATAR_MAX_HEIGHT;

        View backClickableView = findViewById(R.id.edit_space_activity_back_clickable_view);
        GestureDetector backGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_BACK));
        backClickableView.setOnTouchListener((v, motionEvent) -> {
            backGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        layoutParams = backClickableView.getLayoutParams();
        layoutParams.height = Design.BACK_CLICKABLE_VIEW_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) backClickableView.getLayoutParams();
        marginLayoutParams.leftMargin = Design.BACK_CLICKABLE_VIEW_LEFT_MARGIN;
        marginLayoutParams.topMargin = Design.BACK_CLICKABLE_VIEW_TOP_MARGIN;

        RoundedView backRoundedView = findViewById(R.id.edit_space_activity_back_rounded_view);
        backRoundedView.setColor(Design.BACK_VIEW_COLOR);

        mContentView = findViewById(R.id.edit_space_activity_content_view);
        mContentView.setY(Design.CONTENT_VIEW_INITIAL_POSITION);

        View slideMarkView = findViewById(R.id.edit_space_activity_slide_mark_view);
        layoutParams = slideMarkView.getLayoutParams();
        layoutParams.height = Design.SLIDE_MARK_HEIGHT;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.mutate();
        gradientDrawable.setColor(Color.rgb(244, 244, 244));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        ViewCompat.setBackground(slideMarkView, gradientDrawable);

        float corner = ((float) Design.SLIDE_MARK_HEIGHT / 2) * Resources.getSystem().getDisplayMetrics().density;
        gradientDrawable.setCornerRadius(corner);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) slideMarkView.getLayoutParams();
        marginLayoutParams.topMargin = Design.SLIDE_MARK_TOP_MARGIN;

        mContentView.setOnTouchListener((v, motionEvent) -> touchContent(motionEvent));

        TextView titleView = findViewById(R.id.edit_space_activity_title_view);
        titleView.setTypeface(Design.FONT_BOLD44.typeface);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD44.size);
        titleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View headerView = findViewById(R.id.edit_space_activity_content_header_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) headerView.getLayoutParams();
        marginLayoutParams.topMargin = Design.HEADER_VIEW_TOP_MARGIN;

        View nameContentView = findViewById(R.id.edit_space_activity_name_content_view);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable nameViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        nameViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        ViewCompat.setBackground(nameContentView, nameViewBackground);

        layoutParams = nameContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) nameContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_NAME_TOP_MARGIN * Design.HEIGHT_RATIO);

        mNameView = findViewById(R.id.edit_space_activity_name_view);
        mNameView.setTypeface(Design.FONT_REGULAR32.typeface);
        mNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        mNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mNameView.setHintTextColor(DESIGN_HINT_COLOR);
        mNameView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NAME_LENGTH)});
        mNameView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                updateViews();
            }
            return false;
        });
        mNameView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                mCounterNameView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), MAX_NAME_LENGTH));
                setUpdated();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        GestureDetector nameGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_EDIT_NAME));
        mNameView.setOnTouchListener((v, motionEvent) -> {
            boolean result = nameGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return result;
        });

        mCounterNameView = findViewById(R.id.edit_space_activity_counter_name_view);
        mCounterNameView.setTypeface(Design.FONT_REGULAR26.typeface);
        mCounterNameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR26.size);
        mCounterNameView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterNameView.setText(String.format(Locale.getDefault(), "0/%d", MAX_NAME_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterNameView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        View descriptionContentView = findViewById(R.id.edit_space_activity_description_content_view);

        ShapeDrawable descriptionContentViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        descriptionContentViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        ViewCompat.setBackground(descriptionContentView, descriptionContentViewBackground);

        layoutParams = descriptionContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = (int) Design.DESCRIPTION_CONTENT_VIEW_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) descriptionContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_DESCRIPTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        mDescriptionView = findViewById(R.id.edit_space_activity_description_view);
        mDescriptionView.setTypeface(Design.FONT_REGULAR32.typeface);
        mDescriptionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        mDescriptionView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        mDescriptionView.setHintTextColor(Design.GREY_COLOR);
        mDescriptionView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_DESCRIPTION_LENGTH)});
        mDescriptionView.addTextChangedListener(new TextWatcher() {

            @SuppressLint("DefaultLocale")
            @Override
            public void afterTextChanged(Editable s) {

                mCounterDescriptionView.setText(String.format(Locale.getDefault(), "%d/%d", s.length(), MAX_DESCRIPTION_LENGTH));
                setUpdated();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        GestureDetector descriptionGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_EDIT_DESCRIPTION));
        mDescriptionView.setOnTouchListener((v, motionEvent) -> {
            boolean result = descriptionGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return result;
        });

        mCounterDescriptionView = findViewById(R.id.edit_space_activity_counter_description_view);
        mCounterDescriptionView.setTypeface(Design.FONT_REGULAR26.typeface);
        mCounterDescriptionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR26.size);
        mCounterDescriptionView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mCounterDescriptionView.setText(String.format(Locale.getDefault(), "0/%d", MAX_DESCRIPTION_LENGTH));

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCounterDescriptionView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_COUNTER_TOP_MARGIN * Design.HEIGHT_RATIO);

        View colorContentView = findViewById(R.id.edit_space_activity_color_content_view);

        ShapeDrawable colorViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        colorViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        ViewCompat.setBackground(colorContentView, colorViewBackground);

        layoutParams = colorContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) colorContentView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_DESCRIPTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        mUIColors = Design.spaceColors();

        if (mCreateSpace) {
            UIColorSpace colorSpace = mUIColors.get(0);
            colorSpace.setSelected(true);
            mSelectedColor = colorSpace;
        }

        mUIColorSpaceListAdapter = new ColorSpaceAdapter(this, mUIColors, null, 0);

        if (mSelectedColor != null) {
            mUIColorSpaceListAdapter.setDefaultColor(Design.DEFAULT_COLOR);
            mUIColorSpaceListAdapter.setSelectedColor(mSelectedColor.getStringColor());
        }

        LinearLayoutManager uiColorsLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        RecyclerView colorRecyclerView = findViewById(R.id.edit_space_activity_color_list_view);
        colorRecyclerView.setLayoutManager(uiColorsLinearLayoutManager);
        colorRecyclerView.setAdapter(mUIColorSpaceListAdapter);
        colorRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        colorRecyclerView.setItemAnimator(null);
        OnColorSpaceTouchListener onTouchListener = new OnColorSpaceTouchListener(this, colorRecyclerView, this);
        colorRecyclerView.addOnItemTouchListener(onTouchListener);

        mSaveClickableView = findViewById(R.id.edit_space_activity_save_view);
        mSaveClickableView.setOnClickListener(v -> onSaveClick());
        mSaveClickableView.setAlpha(0.5f);

        GestureDetector saveGestureDetector = new GestureDetector(this, new ViewTapGestureDetector(ACTION_SAVE));
        mSaveClickableView.setOnTouchListener((v, motionEvent) -> {
            saveGestureDetector.onTouchEvent(motionEvent);
            touchContent(motionEvent);
            return true;
        });

        layoutParams = mSaveClickableView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSaveClickableView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SAVE_TOP_MARGIN * Design.HEIGHT_RATIO);

        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Design.getMainStyle());
        ViewCompat.setBackground(mSaveClickableView, saveViewBackground);

        TextView saveTextView = findViewById(R.id.edit_space_activity_save_title_view);
        saveTextView.setTypeface(Design.FONT_BOLD28.typeface);
        saveTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD28.size);
        saveTextView.setTextColor(Color.WHITE);

        mDeleteView = findViewById(R.id.edit_space_activity_delete_view);
        mDeleteView.setOnClickListener(v -> onDeleteClick());

        layoutParams = mDeleteView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView removeLabelView = findViewById(R.id.edit_space_activity_delete_text_view);
        removeLabelView.setTypeface(Design.FONT_REGULAR34.typeface);
        removeLabelView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR34.size);
        removeLabelView.setTextColor(Design.FONT_COLOR_RED);

        TextView messageView = findViewById(R.id.edit_space_activity_message_view);
        messageView.setTypeface(Design.FONT_REGULAR32.typeface);
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR32.size);
        messageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        layoutParams = messageView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;

        if (mCreateSpace) {
            marginLayoutParams = (ViewGroup.MarginLayoutParams) messageView.getLayoutParams();
            marginLayoutParams.topMargin = (int) (DESIGN_NAME_TOP_MARGIN * Design.HEIGHT_RATIO);
        } else {
            colorContentView.setVisibility(View.GONE);
        }

        mProgressBarView = findViewById(R.id.edit_space_activity_progress_bar);

        mUIInitialized = true;
    }

    private void setUpdated() {
        if (DEBUG) {
            Log.d(LOG_TAG, "setUpdated");
        }

        if (!mUIInitialized) {
            return;
        }

        mName = mNameView.getText().toString().trim();
        mDescription = mDescriptionView.getText().toString().trim();

        if (mSpace != null) {
            if ((mName.isEmpty() || mName.equals(mSpace.getName())) && (mDescription.isEmpty() || mDescription.equals(mSpace.getDescription())) && mUpdatedSpaceAvatarFile == null) {
                if (!mCanSave) {
                    return;
                }
                mCanSave = false;
                mSaveClickableView.setAlpha(0.5f);

            } else {
                if (mCanSave) {
                    return;
                }
                mCanSave = true;
                mSaveClickableView.setAlpha(1.0f);
            }
        } else if (mName.isEmpty()) {
            if (!mCanSave) {
                return;
            }
            mCanSave = false;
            mSaveClickableView.setAlpha(0.5f);
        } else {
            if (mCanSave) {
                return;
            }
            mCanSave = true;
            mSaveClickableView.setAlpha(1.0f);
        }
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;
    }

    private void updateSelectedImage() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSelectedImage");
        }

        mEditableView.getSelectedImage((String path, Bitmap bitmap, Bitmap largeImage) -> {
            mUpdatedSpaceAvatarFile = new File(path);
            mUpdatedSpaceAvatar = bitmap;
            mUpdatedSpaceLargeAvatar = largeImage;
            updateViews();
            setUpdated();
        });
    }

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        mName = mNameView.getText().toString().trim();
        mDescription = mDescriptionView.getText().toString().trim();

        if (mUpdatedSpaceAvatar != null) {
            mAvatarView.setImageBitmap(mUpdatedSpaceLargeAvatar);
        } else {
            mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);
        }

        if (mUITemplateSpace != null && !mInitTemplate) {
            mInitTemplate = true;

            if (mUITemplateSpace.getTemplateType() != UITemplateSpace.TemplateType.OTHER) {
                mNameView.setText(mUITemplateSpace.getSpace());
                mName = mNameView.getText().toString().trim();
            }

            if (mUITemplateSpace.getColor() != null) {
                mSelectedColor = new UIColorSpace(mUITemplateSpace.getColor());
                mSelectedColor.setSelected(true);
                mUIColorSpaceListAdapter.setSelectedColor(mSelectedColor.getStringColor());
                int color = Color.parseColor(mUITemplateSpace.getColor());
                mAvatarView.setBackgroundColor(color);
                float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
                float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
                ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
                saveViewBackground.getPaint().setColor(color);
                ViewCompat.setBackground(mSaveClickableView, saveViewBackground);

                updateSpaceColor();
            }

            if (mUITemplateSpace.getAvatarUrl() != null) {
                mUpdatedSpaceAvatar = BitmapFactory.decodeResource(getResources(), mUITemplateSpace.getAvatarId());
                mAvatarView.setImageBitmap(mUpdatedSpaceAvatar);
                createFileFromTemplate();
            }
        }
    }

    private void onDeleteClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteClick");
        }

        if (mEditSpaceService.numberSpaces(false) <= 1 && !mSpace.isSecret()) {
            showAlertMessageView(R.id.edit_space_activity_layout, getString(R.string.deleted_account_activity_warning), getString(R.string.edit_space_activity_delete_only_one_space_message), true, null);
            return;
        }

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.edit_space_activity_layout);

        DeleteSpaceConfirmView deleteSpaceConfirmView = new DeleteSpaceConfirmView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        deleteSpaceConfirmView.setLayoutParams(layoutParams);

        deleteSpaceConfirmView.setSpaceName(mSpace.getSpaceSettings().getName(), mSpace.getSpaceSettings().getStyle());
        deleteSpaceConfirmView.setAvatar(mSpaceAvatar, false);

        String title = getString(R.string.application_are_you_sure) + "\n"  + getString(R.string.application_operation_irreversible);
        deleteSpaceConfirmView.setTitle(title);
        deleteSpaceConfirmView.setMessage(getString(R.string.edit_space_activity_delete_message));

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                mEditSpaceService.updateDefaultSpace(mSpace);
                mEditSpaceService.deleteSpace(mSpace);
                deleteSpaceConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                deleteSpaceConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                deleteSpaceConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                percentRelativeLayout.removeView(deleteSpaceConfirmView);
                setFullscreen();
            }
        };
        deleteSpaceConfirmView.setObserver(observer);

        percentRelativeLayout.addView(deleteSpaceConfirmView);
        deleteSpaceConfirmView.show();

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
    }

    @Override
    protected void onSaveClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveClick");
        }

        if (!mCanSave) {
            return;
        }

        hideKeyboard();

        saveSpace();
    }

    private void openMenuPhoto() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenuPhoto");
        }

        hideKeyboard();

        PercentRelativeLayout percentRelativeLayout = findViewById(R.id.edit_space_activity_layout);

        MenuPhotoView menuPhotoView = new MenuPhotoView(this, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        menuPhotoView.setLayoutParams(layoutParams);

        MenuPhotoView.Observer observer = new MenuPhotoView.Observer() {
            @Override
            public void onCameraClick() {

                menuPhotoView.animationCloseMenu();
                mEditableView.onCameraClick();
            }

            @Override
            public void onPhotoGalleryClick() {

                menuPhotoView.animationCloseMenu();
                mEditableView.onGalleryClick();
            }

            @Override
            public void onCloseMenuSelectActionAnimationEnd() {

                percentRelativeLayout.removeView(menuPhotoView);

                Window window = getWindow();
                window.setNavigationBarColor(Design.WHITE_COLOR);
            }
        };

        menuPhotoView.setObserver(observer);
        percentRelativeLayout.addView(menuPhotoView);

        List<UIMenuSelectAction> actions = new ArrayList<>();
        actions.add(new UIMenuSelectAction(getString(R.string.application_camera), R.drawable.grey_camera));
        actions.add(new UIMenuSelectAction(getString(R.string.application_photo_gallery), R.drawable.from_gallery));
        menuPhotoView.setActions(actions, this);
        menuPhotoView.openMenu(true);

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
    }

    private void saveSpace() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveSpace");
        }

        if (mSpace != null) {
            SpaceSettings spaceSettings = mSpace.getSpaceSettings();
            spaceSettings.setName(mName);
            spaceSettings.setDescription(mDescription);
            if (mSelectedColor != null) {
                spaceSettings.setStyle(mSelectedColor.getColor());
            }
            mEditSpaceService.updateSpace(mSpace, spaceSettings, mUpdatedSpaceAvatarFile == null ? null : mUpdatedSpaceAvatar, mUpdatedSpaceAvatarFile);
        } else {
            SpaceSettings spaceSettings = new SpaceSettings(mName);
            spaceSettings.setDescription(mDescription);
            if (mSelectedColor != null) {
                spaceSettings.setStyle(mSelectedColor.getColor());
                spaceSettings.setBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_APPEARANCE_SETTINGS, false);
            }
            mEditSpaceService.createSpace(spaceSettings, mUpdatedSpaceAvatarFile == null ? null : mUpdatedSpaceAvatar, mUpdatedSpaceAvatarFile);
        }
    }

    private void updateSpace() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSpace");
        }

        if (!mUIInitialized) {

            return;
        }

        if (mSpace != null) {
            mNameView.setText(mSpace.getName());
            mDescriptionView.setText(mSpace.getDescription());

            if (mSpaceAvatar != null) {
                mAvatarView.setImageBitmap(mSpaceAvatar);
            } else {
                mAvatarView.setBackgroundColor(Design.AVATAR_PLACEHOLDER_COLOR);
            }

            mDeleteView.setVisibility(View.VISIBLE);
            if (mEditSpaceService.numberSpaces(false) <= 1 && !mSpace.isSecret()) {
                mDeleteView.setAlpha(0.5f);
            } else {
                mDeleteView.setAlpha(1.0f);
            }
        } else {
            mAvatarView.setBackgroundColor(Design.GREY_BACKGROUND_COLOR);
            mDeleteView.setVisibility(View.GONE);
        }

        updateSpaceColor();
        setUpdated();
    }

    private void updateSpaceColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSpaceColor");
        }

        if (!mUIInitialized || mSelectedColor == null) {

            return;
        }

        for (UIColorSpace colorSpace : mUIColors) {
            colorSpace.setSelected(colorSpace.getColor().equals(mSelectedColor.getColor()));
        }

        int color = Design.AVATAR_PLACEHOLDER_COLOR;
        int saveColor = Design.getMainStyle();

        if (!mSelectedColor.useDefaultColor()) {
            color = Color.parseColor(mSelectedColor.getColor());
            saveColor = Color.parseColor(mSelectedColor.getColor());
        }

        mAvatarView.setBackgroundColor(color);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(saveColor);
        ViewCompat.setBackground(mSaveClickableView, saveViewBackground);

        mUIColorSpaceListAdapter.setSelectedColor(mSelectedColor.getStringColor());
    }

    private void createFileFromTemplate() {
        if (DEBUG) {
            Log.d(LOG_TAG, "createFileFromTemplate");
        }

        File directory = new File(getFilesDir(), Twinlife.TMP_DIR);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs() || !directory.isDirectory()) {
                return;
            }
        }

        mUpdatedSpaceAvatarFile = new File(directory, "twinlife_" + System.currentTimeMillis() + ".tmp");
        try {
            //noinspection ResultOfMethodCallIgnored
            mUpdatedSpaceAvatarFile.createNewFile();
            DownloadImageTask downloadImageTask = new DownloadImageTask(mAvatarView, mUpdatedSpaceAvatarFile);
            downloadImageTask.execute(mUITemplateSpace.getAvatarUrl());
        } catch (IOException exception) {
            mUpdatedSpaceAvatarFile = null;
        }
    }
}
