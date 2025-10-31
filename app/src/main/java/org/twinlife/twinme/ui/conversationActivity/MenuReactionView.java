/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
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

import androidx.annotation.NonNull;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

import java.util.ArrayList;
import java.util.List;

public class MenuReactionView extends PercentRelativeLayout {
    private static final String LOG_TAG = "MenuReactionView";
    private static final boolean DEBUG = false;

    private static final float DESIGN_MENU_MARGIN = 12f;
    private static final float DESIGN_REACTION_WIDTH = 76f;
    private static final float DESIGN_REACTION_HEIGHT = 92f;

    private static final long ANIMATION_DURATION = 100;

    private ConversationActivity mConversationActivity;

    private final List<View> animationList = new ArrayList<>();
    private final List<UIReaction> mUIReactions = new ArrayList<>();
    private View mMenuView;
    private MenuReactionAdapter mMenuReactionAdapter;

    private boolean isAnimationEnded = false;

    public MenuReactionView(Context context) {
        super(context);
    }

    public MenuReactionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        mConversationActivity = (ConversationActivity) context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view;
            view = inflater.inflate(R.layout.conversation_activity_menu_reaction_view, (ViewGroup) getParent());
            view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            addView(view);

            initViews();
        }
    }

    public MenuReactionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void openMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openMenu");
        }

        isAnimationEnded = false;

        animationList.clear();
        animationList.add(mMenuView);
    }

    public void animationMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "animationMenu");
        }

        if (isAnimationEnded) {
            return;
        }

        PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.0f, 1.0f);

        List<Animator> animators = new ArrayList<>();

        for (View view : animationList) {
            ObjectAnimator alphaViewAnimator = ObjectAnimator.ofPropertyValuesHolder(view, propertyValuesHolderAlpha);
            alphaViewAnimator.setDuration(ANIMATION_DURATION);
            animators.add(alphaViewAnimator);
        }

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animators);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                isAnimationEnded = true;
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {

            }
        });
    }

    public int getMenuWidth() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getMenuWidth");
        }

        return (int) ((DESIGN_REACTION_WIDTH * Design.WIDTH_RATIO * mUIReactions.size()) + (DESIGN_MENU_MARGIN * Design.WIDTH_RATIO * 2));
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mMenuView = findViewById(R.id.menu_reaction_content_view);

        MenuReactionAdapter.OnReactionClickListener onReactionClickListener = uiReaction -> mConversationActivity.onAddReactionItemClick(uiReaction);

        mMenuReactionAdapter = new MenuReactionAdapter(mConversationActivity, onReactionClickListener, mUIReactions);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mConversationActivity, RecyclerView.HORIZONTAL, false);
        RecyclerView listView = findViewById(R.id.menu_reaction_list_view);
        listView.setBackgroundColor(Color.TRANSPARENT);
        listView.setLayoutManager(linearLayoutManager);
        listView.setItemAnimator(null);
        listView.setAdapter(mMenuReactionAdapter);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) listView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_MENU_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_MENU_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_MENU_MARGIN * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_MENU_MARGIN * Design.WIDTH_RATIO));

        initReactions();

        ViewGroup.LayoutParams layoutParams = mMenuView.getLayoutParams();
        layoutParams.width = getMenuWidth();
        layoutParams.height = (int) (DESIGN_REACTION_HEIGHT * Design.HEIGHT_RATIO);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable menuViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        menuViewBackground.getPaint().setColor(Design.MENU_REACTION_BACKGROUND_COLOR);
        mMenuView.setBackground(menuViewBackground);
    }

    private void initReactions() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initReactions");
        }

        mUIReactions.clear();
        mUIReactions.add(new UIReaction(UIReaction.ReactionType.LIKE, R.drawable.reaction_like));
        mUIReactions.add(new UIReaction(UIReaction.ReactionType.UNLIKE, R.drawable.reaction_unlike));
        mUIReactions.add(new UIReaction(UIReaction.ReactionType.LOVE, R.drawable.reaction_love));
        mUIReactions.add(new UIReaction(UIReaction.ReactionType.CRY, R.drawable.reaction_cry));
        mUIReactions.add(new UIReaction(UIReaction.ReactionType.HUNGER, R.drawable.reaction_hunger));
        mUIReactions.add(new UIReaction(UIReaction.ReactionType.SURPRISED, R.drawable.reaction_surprised));
        mUIReactions.add(new UIReaction(UIReaction.ReactionType.SCREAMING, R.drawable.reaction_screaming));
        mUIReactions.add(new UIReaction(UIReaction.ReactionType.FIRE, R.drawable.reaction_fire));
        mMenuReactionAdapter.setReactions(mUIReactions);
    }
}
