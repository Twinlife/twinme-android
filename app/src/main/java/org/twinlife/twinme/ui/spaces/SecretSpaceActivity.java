/*
 *  Copyright (c) 2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.spaces;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.services.SecretSpaceService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.List;

public class SecretSpaceActivity extends AbstractTwinmeActivity implements SecretSpaceService.Observer {
    private static final String LOG_TAG = "SecretSpaceActivity";
    private static final boolean DEBUG = false;

    private static final int DESIGN_CONTENT_VIEW_WIDTH = 686;
    private static final int DESIGN_CONTENT_VIEW_HEIGHT = 320;

    private SecretSpaceService mSecretSpaceService;
    private Space mSecretSpace;

    //
    // Override TwinlifeActivityImpl methods
    //

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mSecretSpaceService = new SecretSpaceService(this, getTwinmeContext(), this);

        initViews();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();

        mSecretSpaceService.dispose();
    }

    //
    // Implement SecretSpaceService.Observer methods
    //

    @Override
    public void onGetSpaces(@NonNull List<Space> spaces) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpaces: spaces=" + spaces);
        }

        mSecretSpace = null;

        if (spaces.size() > 0) {
            mSecretSpace = spaces.get(0);
        }
    }

    @Override
    public void onSetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpace: space=" + space);
        }

        finish();
    }

    //
    // Override Activity methods
    //

    @Override
    public void finish() {
        if (DEBUG) {
            Log.d(LOG_TAG, "finish");
        }

        super.finish();
        overridePendingTransition(0, 0);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color,  ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.WHITE_COLOR));
        setContentView(R.layout.secret_space_activity);
        setBackgroundColor(Design.OVERLAY_VIEW_COLOR);

        View contentView = findViewById(R.id.secret_space_activity_content_view);

        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CONTENT_VIEW_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_CONTENT_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        float radius = Design.POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable popupViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        popupViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        ViewCompat.setBackground(contentView, popupViewBackground);

        View nameContentView = findViewById(R.id.secret_space_activity_content_name_view);

        ShapeDrawable colorContentViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        colorContentViewBackground.getPaint().setColor(Design.EDIT_TEXT_BACKGROUND_COLOR);
        ViewCompat.setBackground(nameContentView, colorContentViewBackground);

        layoutParams = nameContentView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = Design.BUTTON_HEIGHT;

        EditText nameView = findViewById(R.id.secret_space_activity_name_view);
        nameView.setTypeface(Design.FONT_REGULAR44.typeface);
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR44.size);
        nameView.setTextColor(Design.EDIT_TEXT_TEXT_COLOR);
        nameView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                mSecretSpaceService.findSecretSpaceByName(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        View sendClickableView = findViewById(R.id.secret_space_activity_send_view);
        sendClickableView.setOnClickListener(v -> onSendClick());

        layoutParams = sendClickableView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        ShapeDrawable saveViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        saveViewBackground.getPaint().setColor(Design.BLUE_NORMAL);
        ViewCompat.setBackground(sendClickableView, saveViewBackground);

        TextView saveTextView = findViewById(R.id.secret_space_activity_send_text_view);
        saveTextView.setTypeface(Design.FONT_BOLD28.typeface);
        saveTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD28.size);
        saveTextView.setTextColor(Color.WHITE);

        View cancelView = findViewById(R.id.secret_space_activity_cancel_view);
        cancelView.setOnClickListener(v -> finish());

        layoutParams = cancelView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        ShapeDrawable cancelViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        cancelViewBackground.getPaint().setColor(Design.BUTTON_RED_COLOR);
        ViewCompat.setBackground(cancelView, cancelViewBackground);

        TextView cancelTextView = findViewById(R.id.secret_space_activity_cancel_text_view);
        cancelTextView.setTypeface(Design.FONT_BOLD28.typeface);
        cancelTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD28.size);
        cancelTextView.setTextColor(Color.WHITE);

        View closeView = findViewById(R.id.secret_space_activity_close_view);
        closeView.setOnClickListener(view -> finish());
    }

    private void onSendClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendClick");
        }

        if (mSecretSpace != null) {
            mSecretSpaceService.setSpace(mSecretSpace);
        } else {
            finish();
        }
    }
}
