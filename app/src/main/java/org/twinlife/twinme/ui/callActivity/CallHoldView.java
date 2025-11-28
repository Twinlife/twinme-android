/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.RoundedView;

public class CallHoldView extends PercentRelativeLayout {
    private static final String LOG_TAG = "CallHoldView";
    private static final boolean DEBUG = false;

    public interface CallHoldListener {

        void onAddToCall();

        void onHangup();

        void onSwapCall();
    }

    private static final int DESIGN_PLACEHOLDER_COLOR = Color.rgb(229, 229, 229);

    private static final int DESIGN_AVATAR_VIEW_RADIUS = 6;
    private static final int DESIGN_CONTAINER_VIEW_COLOR = Color.rgb(60, 60, 60);
    private static final int DESIGN_CONTAINER_VIEW_RADIUS = 14;
    private static final int DESIGN_AVATAR_MARGIN = 12;
    private static final int DESIGN_ACTION_MARGIN = 18;
    private static final float DESIGN_CONTAINER_HEIGHT = 136f;
    private static final float DESIGN_ACTION_WIDTH = 80f;
    private static final float DESIGN_SIDE_MARGIN = 34f;
    private static final int CONTAINER_HEIGHT;
    private static final int SIDE_MARGIN;
    private static final int AVATAR_MARGIN;
    private static final int ACTION_MARGIN;
    private static final int ACTION_WIDTH;

    static {
        CONTAINER_HEIGHT = (int) (DESIGN_CONTAINER_HEIGHT * Design.HEIGHT_RATIO);
        SIDE_MARGIN = (int) (DESIGN_SIDE_MARGIN * Design.WIDTH_RATIO);
        AVATAR_MARGIN = (int) (DESIGN_AVATAR_MARGIN * Design.WIDTH_RATIO);
        ACTION_MARGIN = (int) (DESIGN_ACTION_MARGIN * Design.WIDTH_RATIO);
        ACTION_WIDTH = (int) (DESIGN_ACTION_WIDTH * Design.WIDTH_RATIO);
    }

    private ImageView mAvatarView;
    private TextView mNameView;

    private CallHoldListener mCallHoldListener;

    public CallHoldView(Context context) {

        super(context);
    }

    public CallHoldView(Context context, AttributeSet attrs) {

        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.call_activity_hold_view, this, true);
        initViews();
    }

    public CallHoldView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    public void setCallHoldListener(CallHoldListener callHoldListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCallHoldListener: " + callHoldListener);
        }

        mCallHoldListener = callHoldListener;
    }

    public void setCallInfo(String name, Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setCallInfo: " + name + " avatar: " + avatar);
        }

        mNameView.setText(name);
        mAvatarView.setImageBitmap(avatar);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        View containerView = findViewById(R.id.call_activity_hold_container_view);

        ViewGroup.LayoutParams viewLayoutParams = containerView.getLayoutParams();
        viewLayoutParams.height = CONTAINER_HEIGHT;
        viewLayoutParams.width = Design.DISPLAY_WIDTH - (SIDE_MARGIN * 2);

        float radius = DESIGN_CONTAINER_VIEW_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable containerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        containerViewBackground.getPaint().setColor(DESIGN_CONTAINER_VIEW_COLOR);
        containerView.setBackground(containerViewBackground);

        View avatarContainerView = findViewById(R.id.call_activity_hold_avatar_container_view);
        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) avatarContainerView.getLayoutParams();

        marginLayoutParams.leftMargin = AVATAR_MARGIN;
        marginLayoutParams.rightMargin = AVATAR_MARGIN;
        marginLayoutParams.setMarginStart(AVATAR_MARGIN);
        marginLayoutParams.setMarginEnd(AVATAR_MARGIN);

        viewLayoutParams = avatarContainerView.getLayoutParams();
        viewLayoutParams.height = CONTAINER_HEIGHT - (AVATAR_MARGIN * 2);

        radius = DESIGN_AVATAR_VIEW_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable coverViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        coverViewBackground.getPaint().setColor(DESIGN_PLACEHOLDER_COLOR);
        avatarContainerView.setBackground(coverViewBackground);

        mAvatarView = findViewById(R.id.call_activity_hold_avatar_view);
        mAvatarView.setClipToOutline(true);

        mNameView = findViewById(R.id.call_activity_hold_name_view);
        Design.updateTextFont(mNameView, Design.FONT_MEDIUM34);
        mNameView.setTextColor(Color.WHITE);

        int nameMargin = (ACTION_WIDTH * 3) + (ACTION_MARGIN * 4);
        marginLayoutParams = (MarginLayoutParams) mNameView.getLayoutParams();
        marginLayoutParams.rightMargin = nameMargin;
        marginLayoutParams.setMarginEnd(nameMargin);

        View swapView = findViewById(R.id.call_activity_hold_swap_view);
        swapView.setOnClickListener(v -> onSwapCallClick());

        viewLayoutParams = swapView.getLayoutParams();
        viewLayoutParams.width = ACTION_WIDTH;

        marginLayoutParams = (MarginLayoutParams) swapView.getLayoutParams();
        marginLayoutParams.rightMargin = ACTION_MARGIN;
        marginLayoutParams.setMarginEnd(ACTION_MARGIN);

        RoundedView swapRoundedView = findViewById(R.id.call_activity_hold_swap_background_view);
        swapRoundedView.setColor(Color.WHITE);

        View addToCallView = findViewById(R.id.call_activity_hold_add_to_call_view);
        addToCallView.setOnClickListener(v -> onAddToCallClick());

        viewLayoutParams = addToCallView.getLayoutParams();
        viewLayoutParams.width = ACTION_WIDTH;

        marginLayoutParams = (MarginLayoutParams) addToCallView.getLayoutParams();
        marginLayoutParams.rightMargin = ACTION_MARGIN;
        marginLayoutParams.setMarginEnd(ACTION_MARGIN);

        RoundedView addToCallRoundedView = findViewById(R.id.call_activity_hold_add_to_call_background_view);
        addToCallRoundedView.setColor(Color.WHITE);

        ImageView addToCallImageView = findViewById(R.id.ccall_activity_hold_add_to_call_image_view);
        addToCallImageView.setColorFilter(Color.BLACK);

        View hangupView = findViewById(R.id.call_activity_hold_hangup_view);
        hangupView.setOnClickListener(v -> onHangupClick());

        viewLayoutParams = hangupView.getLayoutParams();
        viewLayoutParams.width = ACTION_WIDTH;

        marginLayoutParams = (MarginLayoutParams) hangupView.getLayoutParams();
        marginLayoutParams.rightMargin = ACTION_MARGIN;
        marginLayoutParams.setMarginEnd(ACTION_MARGIN);

        RoundedView hangupRoundedView = findViewById(R.id.call_activity_hold_hangup_rounded_view);
        hangupRoundedView.setColor(Design.BUTTON_RED_COLOR);
    }

    private void onAddToCallClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddToCallClick");
        }

        mCallHoldListener.onAddToCall();
    }

    private void onHangupClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onHangupClick");
        }

        mCallHoldListener.onHangup();
    }

    private void onSwapCallClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSwapCallClick");
        }

        mCallHoldListener.onSwapCall();
    }
}