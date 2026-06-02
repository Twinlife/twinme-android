/*
 *  Copyright (c) 2023-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.conversationActivity.UIReaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationAdapter extends RecyclerView.Adapter<AnnotationViewHolder> {

    private static class AnnotationWithCount {
        @NonNull
        final ConversationService.DescriptorAnnotation descriptorAnnotation;
        final int count;

        private AnnotationWithCount(@NonNull ConversationService.DescriptorAnnotation descriptorAnnotation, int count) {
            this.descriptorAnnotation = descriptorAnnotation;
            this.count = count;
        }
    }

    private final BaseItemActivity mActivity;
    @NonNull
    private final List<AnnotationWithCount> mDescriptorAnnotations;
    private boolean mIsForwaded;
    private boolean mIsUpdated;
    private final boolean mIsPeerItem;
    private ConversationService.DescriptorId mDescriptorId;

    private static int POSITION_FORWARDED = -1;
    private static int POSITION_UPDATED = -1;

    AnnotationAdapter(BaseItemActivity activity, boolean isPeerItem) {

        mActivity = activity;
        mDescriptorAnnotations = new ArrayList<>();
        mIsForwaded = false;
        mIsUpdated = false;
        mIsPeerItem = isPeerItem;
    }

    public void setAnnotations(@NonNull List<ConversationService.DescriptorAnnotation> annotations, @NonNull ConversationService.DescriptorId descriptorId) {

        mDescriptorId = descriptorId;

        mDescriptorAnnotations.clear();

        Map<ConversationService.DescriptorAnnotation, Integer> counts = new HashMap<>();

        for (ConversationService.DescriptorAnnotation annotation : annotations) {
            Integer count = counts.get(annotation);
            if (count == null) {
                count = 0;
            }

            counts.put(annotation, ++count);
        }

        for (Map.Entry<ConversationService.DescriptorAnnotation, Integer> entry : counts.entrySet()) {
            mDescriptorAnnotations.add(new AnnotationWithCount(entry.getKey(), entry.getValue()));
        }

        synchronized (this) {
            notifyDataSetChanged();
        }
    }

    public void setIsForwarded(boolean isForwarded) {

        mIsForwaded = isForwarded;
        synchronized (this) {
            notifyDataSetChanged();
        }
    }

    public void setIsUpdated(boolean isUpdated) {

        mIsUpdated = isUpdated;
        synchronized (this) {
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public AnnotationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.base_item_activity_annotation_item, parent, false);
        return new AnnotationViewHolder(mActivity, convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnotationViewHolder viewHolder, int position) {

        if (mIsForwaded && position == POSITION_FORWARDED) {
            viewHolder.onBindAnnotationWithImage(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.forward_icon, mActivity.getTheme()), mIsPeerItem);
        } else if (mIsUpdated && position == POSITION_UPDATED) {
            viewHolder.onBindAnnotationWithImage(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.edit_annotation_icon, mActivity.getTheme()), mIsPeerItem);
        } else {
            int annotationPosition = position;
            if (mIsPeerItem) {
                if (mIsUpdated) {
                    annotationPosition--;
                }

                if (mIsForwaded) {
                    annotationPosition--;
                }
            }

            AnnotationWithCount annotationWithCount = mDescriptorAnnotations.get(annotationPosition);
            ConversationService.DescriptorAnnotation descriptorAnnotation = annotationWithCount.descriptorAnnotation;

            Drawable drawable;
            int colorFilter = Color.TRANSPARENT;
            if (descriptorAnnotation.getValue() < 0 || descriptorAnnotation.getValue() >= UIReaction.ReactionType.values().length) {
                drawable = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.reaction_unknown, mActivity.getTheme());
                colorFilter = Design.BLACK_COLOR;
            } else {
                // LIKE annotations always have an int value, so the cast is safe.
                UIReaction.ReactionType reactionType = UIReaction.ReactionType.values()[(int)descriptorAnnotation.getValue()];

                switch (reactionType) {
                    case UNLIKE:
                        drawable = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.reaction_unlike, mActivity.getTheme());
                        break;

                    case LOVE:
                        drawable = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.reaction_love, mActivity.getTheme());
                        break;

                    case CRY:
                        drawable = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.reaction_cry, mActivity.getTheme());
                        break;

                    case HUNGER:
                        drawable = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.reaction_hunger, mActivity.getTheme());
                        break;

                    case SURPRISED:
                        drawable = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.reaction_surprised, mActivity.getTheme());
                        break;

                    case SCREAMING:
                        drawable = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.reaction_screaming, mActivity.getTheme());
                        break;

                    case FIRE:
                        drawable = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.reaction_fire, mActivity.getTheme());
                        break;

                    default:
                        drawable = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.reaction_like, mActivity.getTheme());
                        break;
                }
            }

            viewHolder.onBind(mDescriptorId, drawable, annotationWithCount.count, colorFilter, mIsPeerItem);
        }
    }

    @Override
    public int getItemCount() {

        int count = 0;

        POSITION_FORWARDED = -1;
        POSITION_UPDATED = -1;

        if (mIsForwaded) {
            count++;

            if (mIsPeerItem) {
                POSITION_FORWARDED = 0;
            } else {
                POSITION_FORWARDED = mDescriptorAnnotations.size();
            }
        }

        if (mIsUpdated) {
            count++;

            if (mIsPeerItem) {
                POSITION_UPDATED = POSITION_FORWARDED != -1 ? POSITION_FORWARDED + 1 : 0;
            } else {
                POSITION_UPDATED = POSITION_FORWARDED != -1 ? POSITION_FORWARDED + 1 : mDescriptorAnnotations.size();
            }
        }

        count += mDescriptorAnnotations.size();

        return count;
    }

    @Override
    public void onViewRecycled(@NonNull AnnotationViewHolder viewHolder) {

        viewHolder.onViewRecycled();
    }
}

