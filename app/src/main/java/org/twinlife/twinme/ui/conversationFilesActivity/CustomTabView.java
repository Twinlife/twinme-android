/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.List;

public class CustomTabView extends PercentRelativeLayout {
    private static final String LOG_TAG = "CustomTabView";
    private static final boolean DEBUG = false;

    protected static final float DESIGN_CUSTOM_TAB_VIEW_HEIGHT = 70f;
    private static final float DESIGN_CUSTOM_TAB_VIEW_MARGIN = 40f;

    public interface Observer {

        void onSelectCustomTab(UICustomTab customTab);
    }

    private AbstractTwinmeActivity mActivity;
    private CustomTabAdapter mCustomTabAdapter;

    private List<UICustomTab> mCustomTabs;
    private Observer mObserver;

    private View mTabView;

    public CustomTabView(Context context) {
        super(context);
    }

    public CustomTabView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        mActivity = (AbstractTwinmeActivity) context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view;
            view = inflater.inflate(R.layout.conversation_files_activity_custom_tab_view, (ViewGroup) getParent());
            addView(view);

            initViews();
        }
    }

    public CustomTabView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initTabs(List<UICustomTab> tabs, Observer observer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "initTabs");
        }

        mObserver = observer;

        mCustomTabs = tabs;

        CustomTabAdapter.OnCustomTabClickListener onCustomTabClickListener = this::onSelectCustomTab;

        mCustomTabAdapter = new CustomTabAdapter(tabs, mActivity, onCustomTabClickListener);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity, RecyclerView.HORIZONTAL, false);
        RecyclerView listView = findViewById(R.id.custom_tab_view_container_list_view);
        listView.setBackgroundColor(Color.TRANSPARENT);
        listView.setLayoutManager(linearLayoutManager);
        listView.setItemAnimator(null);
        listView.setAdapter(mCustomTabAdapter);

        float maxWidth = Design.DISPLAY_WIDTH - (2 * DESIGN_CUSTOM_TAB_VIEW_MARGIN * Design.WIDTH_RATIO);

        float contentWidth = 0;
        for (UICustomTab tab : mCustomTabs) {
            contentWidth += tab.getWidth();
        }

        int tabViewMargin;
        if (contentWidth < maxWidth) {
            tabViewMargin = (int) ((Design.DISPLAY_WIDTH - contentWidth) * 0.5);
        } else {
            tabViewMargin = (int) (DESIGN_CUSTOM_TAB_VIEW_MARGIN * Design.WIDTH_RATIO);
        }

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mTabView.getLayoutParams();
        marginLayoutParams.leftMargin = tabViewMargin;
        marginLayoutParams.rightMargin = tabViewMargin;
        marginLayoutParams.topMargin = (int) (DESIGN_CUSTOM_TAB_VIEW_MARGIN * Design.HEIGHT_RATIO);
    }

    public void updateColor(int backgroundColor, int mainColor, int textSelectedColor, int borderColor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        setBackgroundColor(backgroundColor);

        int tabHeight = (int) (DESIGN_CUSTOM_TAB_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        float radius = tabHeight * 0.5f * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadii(outerRadii);
        gradientDrawable.setColor(Design.CUSTOM_TAB_BACKGROUND_COLOR);
        gradientDrawable.setCornerRadius(radius);
        if (borderColor != -1) {
            gradientDrawable.setStroke(3, borderColor);
        }

        mTabView.setBackground(gradientDrawable);

        mCustomTabAdapter.updateColor(mainColor, textSelectedColor);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setBackgroundColor(Design.TOOLBAR_COLOR);

        mTabView = findViewById(R.id.custom_tab_view_container_view);

        int tabHeight = (int) (DESIGN_CUSTOM_TAB_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        ViewGroup.LayoutParams layoutParams = mTabView.getLayoutParams();
        layoutParams.height = tabHeight;

        float radius = tabHeight * 0.5f * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable tabViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        tabViewBackground.getPaint().setColor(Design.CUSTOM_TAB_BACKGROUND_COLOR);
        mTabView.setBackground(tabViewBackground);
    }

    private void onSelectCustomTab(UICustomTab uiCustomTab) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSelectCustomTab: " + uiCustomTab);
        }

        for (UICustomTab customTab : mCustomTabs) {
            customTab.setSelected(false);
        }
        uiCustomTab.setSelected(true);

        if (mObserver != null) {
            mObserver.onSelectCustomTab(uiCustomTab);
        }

        mCustomTabAdapter.notifyDataSetChanged();
    }
}
