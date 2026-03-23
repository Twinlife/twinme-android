/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class CallInfoView extends RelativeLayout {
    private static final String LOG_TAG = "CallInfoView";
    private static final boolean DEBUG = false;

    private static final int DESIGN_CONTAINER_VIEW_RADIUS = 14;
    private static final int DESIGN_CONTAINER_VIEW_COLOR = Color.rgb(60, 60, 60);
    private static final float DESIGN_CONTAINER_HEIGHT = 136f;
    private static final float DESIGN_SIDE_MARGIN = 34f;
    private static final int CONTAINER_HEIGHT;
    private static final int SIDE_MARGIN;

    static {
        CONTAINER_HEIGHT = (int) (DESIGN_CONTAINER_HEIGHT * Design.HEIGHT_RATIO);
        SIDE_MARGIN = (int) (DESIGN_SIDE_MARGIN * Design.WIDTH_RATIO);
    }


    private TextView mMessageView;


    public CallInfoView(Context context) {

        super(context);
    }

    public CallInfoView(Context context, AttributeSet attrs) {

        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.call_activity_info_view, this, true);
        initViews();
    }

    public CallInfoView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    public void updateMessage(String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateMessage: " + message);
        }

        mMessageView.setText(message);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        View containerView = findViewById(R.id.call_activity_info_container_view);

        ViewGroup.LayoutParams viewLayoutParams = containerView.getLayoutParams();
        viewLayoutParams.height = CONTAINER_HEIGHT;
        viewLayoutParams.width = Design.DISPLAY_WIDTH - (SIDE_MARGIN * 2);

        float radius = DESIGN_CONTAINER_VIEW_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable containerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        containerViewBackground.getPaint().setColor(DESIGN_CONTAINER_VIEW_COLOR);
        containerView.setBackground(containerViewBackground);

        mMessageView = findViewById(R.id.call_activity_info_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_MEDIUM34);
        mMessageView.setTextColor(Color.WHITE);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.leftMargin = SIDE_MARGIN;
        marginLayoutParams.rightMargin = SIDE_MARGIN;
    }
}