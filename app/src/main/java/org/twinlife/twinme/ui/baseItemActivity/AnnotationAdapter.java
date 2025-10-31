/*
 *  Copyright (c) 2023-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
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

import java.util.List;

public class AnnotationAdapter extends RecyclerView.Adapter<AnnotationViewHolder> {

    private final BaseItemActivity mActivity;
    private List<ConversationService.DescriptorAnnotation> mDescriptorAnnotations;
    private boolean mIsForwaded;
    private boolean mIsUpdated;
    private final boolean mIsPeerItem;
    private ConversationService.DescriptorId mDescriptorId;

    private static int POSITION_FORWARDED = -1;
    private static int POSITION_UPDATED = -1;

    AnnotationAdapter(BaseItemActivity activity, List<ConversationService.DescriptorAnnotation> annotations, boolean isPeerItem) {

        mActivity = activity;
        mDescriptorAnnotations = annotations;
        mIsForwaded = false;
        mIsUpdated = false;
        mIsPeerItem = isPeerItem;
    }

    public void setAnnotations(List<ConversationService.DescriptorAnnotation> annotations, ConversationService.DescriptorId descriptorId) {

        mDescriptorAnnotations = annotations;
        mDescriptorId = descriptorId;
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
            ConversationService.DescriptorAnnotation descriptorAnnotation = mDescriptorAnnotations.get(annotationPosition);
            Drawable drawable;
            int colorFilter = Color.TRANSPARENT;
            if (descriptorAnnotation.getValue() < 0 || descriptorAnnotation.getValue() >= UIReaction.ReactionType.values().length) {
                drawable = ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.reaction_unknown, mActivity.getTheme());
                colorFilter = Design.BLACK_COLOR;
            } else {
                UIReaction.ReactionType reactionType = UIReaction.ReactionType.values()[descriptorAnnotation.getValue()];

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

            viewHolder.onBind(mDescriptorId, drawable, descriptorAnnotation.getCount(), colorFilter, mIsPeerItem);
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
                POSITION_FORWARDED = mDescriptorAnnotations != null ? mDescriptorAnnotations.size() : 0;
            }
        }

        if (mIsUpdated) {
            count++;

            if (mIsPeerItem) {
                POSITION_UPDATED = POSITION_FORWARDED != -1 ? POSITION_FORWARDED + 1 : 0;
            } else {
                POSITION_UPDATED = POSITION_FORWARDED != -1 ? POSITION_FORWARDED + 1 : mDescriptorAnnotations != null ? mDescriptorAnnotations.size() : 0;
            }
        }

        if (mDescriptorAnnotations != null) {
            count += mDescriptorAnnotations.size();
        }

        return count;
    }

    @Override
    public void onViewRecycled(@NonNull AnnotationViewHolder viewHolder) {

        viewHolder.onViewRecycled();
    }
}

