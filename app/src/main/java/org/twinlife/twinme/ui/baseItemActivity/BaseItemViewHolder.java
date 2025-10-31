/*
 *  Copyright (c) 2018-2021 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.ConversationService.FileDescriptor;
import org.twinlife.twinlife.ConversationService.ImageDescriptor;
import org.twinlife.twinlife.ConversationService.VideoDescriptor;
import org.twinlife.twinme.glide.Modes;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.TextStyle;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.RoundedImageView;
import org.twinlife.twinme.utils.async.Loader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class BaseItemViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "BaseItemViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_ITEM_SMALL_ROUND_CORNER_RADIUS = 8f;
    private static final float DESIGN_ITEM_LARGE_ROUND_CORNER_RADIUS = 38f;
    private static final float DESIGN_REPLY_IMAGE_ROUND_CORNER_RADIUS = 6f;
    private static final float DESIGN_ITEM_TOP_MARGIN1 = 4f;
    private static final float DESIGN_ITEM_TOP_MARGIN2 = 18f;
    private static final float DESIGN_ITEM_BOTTOM_MARGIN1 = 4f;
    private static final float DESIGN_ITEM_BOTTOM_MARGIN2 = 18f;
    private static final float DESIGN_TIME_ITEM_HEIGHT = 56f;
    private static final float DESIGN_NAME_ITEM_HEIGHT = 36f;
    private static final float DESIGN_TYPING_ITEM_HEIGHT = 100f;
    private static final float DESIGN_FORWARD_ITEM_HEIGHT = 34f;
    private static final float DESIGN_MESSAGE_ITEM_TEXT_HEIGHT_PADDING = 10f;
    private static final float DESIGN_MESSAGE_ITEM_TEXT_WIDTH_PADDING = 32f;
    private static final float DESIGN_IMAGE_ITEM_MAX_WIDTH = 500f;
    private static final float DESIGN_IMAGE_ITEM_MAX_HEIGHT = 889f;
    private static final float DESIGN_FORWARDED_IMAGE_ITEM_MAX_HEIGHT = 240f;
    private static final float DESIGN_FORWARDED_SMALL_IMAGE_ITEM_MAX_HEIGHT = 120f;
    private static final float DESIGN_REPLY_IMAGE_ITEM_MAX_WIDTH = 290f;
    private static final float DESIGN_REPLY_IMAGE_ITEM_MAX_HEIGHT = 290f;
    private static final float DESIGN_REPLY_IMAGE_ITEM_HEIGHT_MARGIN = 28f;
    private static final float DESIGN_REPLY_IMAGE_ITEM_WIDTH_MARGIN = 28f;
    private static final float DESIGN_FILE_ITEM_HEIGHT_PADDING = 20f;
    private static final float DESIGN_FILE_ITEM_WIDTH_PADDING = 20f;
    private static final float DESIGN_OVERLAY_DEFAULT_HEIGHT = 10f;
    private static final float DESIGN_LINK_IMAGE_MAX_HEIGHT = 600f;
    private static final float DESIGN_LINK_IMAGE_MAX_WIDTH = 400f;
    private static final float DESIGN_LINK_PREVIEW_BOTTOM_MARGIN = 20f;
    private static final int DESIGN_CHECKBOX_MARGIN = 26;
    private static final int DESIGN_CHECKBOX_HEIGHT = 44;


    private static final long DESIGN_LONG_CLICK_DURATION = 500;
    static final int DESIGN_DELETE_ANIMATION_DURATION = 5000; // ms

    private static final float ITEM_SMALL_RADIUS;
    private static final float ITEM_LARGE_RADIUS;
    static final float REPLY_IMAGE_RADIUS;
    static final int ITEM_TOP_MARGIN1;
    static final int ITEM_TOP_MARGIN2;
    static final int ITEM_BOTTOM_MARGIN1;
    static final int ITEM_BOTTOM_MARGIN2;
    static final int TIME_ITEM_HEIGHT;
    static final int NAME_ITEM_HEIGHT;
    static final int TYPING_ITEM_HEIGHT;
    static final int FORWARD_ITEM_HEIGHT;
    static final int MESSAGE_ITEM_TEXT_DEFAULT_PADDING;
    static final int MESSAGE_ITEM_TEXT_WIDTH_PADDING;
    static final int IMAGE_ITEM_MAX_HEIGHT;
    static final int IMAGE_ITEM_MAX_WIDTH;
    static final int FORWARDED_IMAGE_ITEM_MAX_HEIGHT;
    static final int FORWARDED_SMALL_IMAGE_ITEM_MAX_HEIGHT;
    static final int REPLY_IMAGE_ITEM_MAX_HEIGHT;
    static final int REPLY_IMAGE_ITEM_MAX_WIDTH;
    static final int REPLY_IMAGE_HEIGHT_MARGIN;
    static final int REPLY_IMAGE_WIDTH_MARGIN;
    static final int FILE_ITEM_HEIGHT_PADDING;
    static final int FILE_ITEM_WIDTH_PADDING;
    static final int OVERLAY_DEFAULT_HEIGHT;
    static final int LINK_IMAGE_MAX_HEIGHT;
    static final int LINK_IMAGE_MAX_WIDTH;
    static final int LINK_PREVIEW_BOTTOM_MARGIN;
    protected static final int CHECKBOX_MARGIN;
    protected static final int CHECKBOX_HEIGHT;

    static {
        ITEM_SMALL_RADIUS = DESIGN_ITEM_SMALL_ROUND_CORNER_RADIUS * Design.HEIGHT_RATIO;
        ITEM_LARGE_RADIUS = DESIGN_ITEM_LARGE_ROUND_CORNER_RADIUS * Design.HEIGHT_RATIO;
        REPLY_IMAGE_RADIUS = DESIGN_REPLY_IMAGE_ROUND_CORNER_RADIUS * Design.HEIGHT_RATIO;
        ITEM_TOP_MARGIN1 = (int) (DESIGN_ITEM_TOP_MARGIN1 * Design.HEIGHT_RATIO);
        ITEM_TOP_MARGIN2 = (int) (DESIGN_ITEM_TOP_MARGIN2 * Design.HEIGHT_RATIO);
        ITEM_BOTTOM_MARGIN1 = (int) (DESIGN_ITEM_BOTTOM_MARGIN1 * Design.HEIGHT_RATIO);
        ITEM_BOTTOM_MARGIN2 = (int) (DESIGN_ITEM_BOTTOM_MARGIN2 * Design.HEIGHT_RATIO);
        TIME_ITEM_HEIGHT = (int) (DESIGN_TIME_ITEM_HEIGHT * Design.HEIGHT_RATIO);
        NAME_ITEM_HEIGHT = (int) (DESIGN_NAME_ITEM_HEIGHT * Design.HEIGHT_RATIO);
        TYPING_ITEM_HEIGHT = (int) (DESIGN_TYPING_ITEM_HEIGHT * Design.HEIGHT_RATIO);
        FORWARD_ITEM_HEIGHT = (int) (DESIGN_FORWARD_ITEM_HEIGHT * Design.HEIGHT_RATIO);
        MESSAGE_ITEM_TEXT_DEFAULT_PADDING = (int) (DESIGN_MESSAGE_ITEM_TEXT_HEIGHT_PADDING * Design.HEIGHT_RATIO);
        MESSAGE_ITEM_TEXT_WIDTH_PADDING = (int) (DESIGN_MESSAGE_ITEM_TEXT_WIDTH_PADDING * Design.WIDTH_RATIO);
        IMAGE_ITEM_MAX_HEIGHT = (int) (DESIGN_IMAGE_ITEM_MAX_HEIGHT * Design.WIDTH_RATIO);
        IMAGE_ITEM_MAX_WIDTH = (int) (DESIGN_IMAGE_ITEM_MAX_WIDTH * Design.WIDTH_RATIO);
        FORWARDED_IMAGE_ITEM_MAX_HEIGHT = (int) (DESIGN_FORWARDED_IMAGE_ITEM_MAX_HEIGHT * Design.HEIGHT_RATIO);
        FORWARDED_SMALL_IMAGE_ITEM_MAX_HEIGHT = (int) (DESIGN_FORWARDED_SMALL_IMAGE_ITEM_MAX_HEIGHT * Design.HEIGHT_RATIO);
        REPLY_IMAGE_ITEM_MAX_HEIGHT = (int) (DESIGN_REPLY_IMAGE_ITEM_MAX_HEIGHT * Design.HEIGHT_RATIO);
        REPLY_IMAGE_ITEM_MAX_WIDTH = (int) (DESIGN_REPLY_IMAGE_ITEM_MAX_WIDTH * Design.WIDTH_RATIO);
        REPLY_IMAGE_HEIGHT_MARGIN = (int) (DESIGN_REPLY_IMAGE_ITEM_HEIGHT_MARGIN * Design.HEIGHT_RATIO);
        REPLY_IMAGE_WIDTH_MARGIN = (int) (DESIGN_REPLY_IMAGE_ITEM_WIDTH_MARGIN * Design.WIDTH_RATIO);
        FILE_ITEM_HEIGHT_PADDING = (int) (DESIGN_FILE_ITEM_HEIGHT_PADDING * Design.HEIGHT_RATIO);
        FILE_ITEM_WIDTH_PADDING = (int) (DESIGN_FILE_ITEM_WIDTH_PADDING * Design.WIDTH_RATIO);
        OVERLAY_DEFAULT_HEIGHT = (int) (DESIGN_OVERLAY_DEFAULT_HEIGHT * Design.HEIGHT_RATIO);
        LINK_IMAGE_MAX_WIDTH = (int) (DESIGN_LINK_IMAGE_MAX_WIDTH * Design.WIDTH_RATIO);
        LINK_IMAGE_MAX_HEIGHT = (int) (DESIGN_LINK_IMAGE_MAX_HEIGHT * Design.HEIGHT_RATIO);
        LINK_PREVIEW_BOTTOM_MARGIN = (int) (DESIGN_LINK_PREVIEW_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        CHECKBOX_MARGIN = (int) (DESIGN_CHECKBOX_MARGIN * Design.WIDTH_RATIO);
        CHECKBOX_HEIGHT = (int) (DESIGN_CHECKBOX_HEIGHT * Design.HEIGHT_RATIO);
    }

    private class ItemViewHolderTouchListener implements View.OnTouchListener {

        private long startTime = 0;
        private boolean mayBeLongPress = false;
        private final Handler handler = new Handler();
        private Runnable currentLongPressRunnable;

        @SuppressLint({"ClickableViewAccessibility"})
        @Override
        public boolean onTouch(View view, final MotionEvent motionEvent) {

            if (mContainer != null) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        startTime = System.currentTimeMillis();
                        mayBeLongPress = true;
                        if (currentLongPressRunnable != null) {
                            handler.removeCallbacks(currentLongPressRunnable);
                        }
                        currentLongPressRunnable = () -> {
                            if (mayBeLongPress) {
                                mayBeLongPress = false;
                                View clickedView = getClickedView((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
                                if (clickedView != null) clickedView.performLongClick();
                            }
                        };
                        handler.postDelayed(currentLongPressRunnable, DESIGN_LONG_CLICK_DURATION);
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        handleTouchEnded(motionEvent);
                        mayBeLongPress = false;
                        if (startTime > 0) {
                            long touchTime = System.currentTimeMillis() - startTime;
                            if (touchTime < DESIGN_LONG_CLICK_DURATION) {
                                View clickedView = getClickedView((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
                                if (clickedView != null && clickedView.getAlpha() > 0)
                                    clickedView.performClick();
                            }
                            startTime = 0;
                        }
                        break;
                    }

                    case MotionEvent.ACTION_CANCEL:
                        handleTouchEnded(motionEvent);
                        mayBeLongPress = false;
                        break;
                }
            }
            return true;
        }

        @SuppressWarnings("UnusedParameters")
        private void handleTouchEnded(MotionEvent event) {

        }
    }

    /**
     * Set the image view to display the image or video descriptor.  The image view is configured according to
     * the current item mode.
     *
     * @param imageView the reply image view.
     * @param fileDescriptor the file descriptor to display.
     */
    protected void setImage(@NonNull RoundedImageView imageView, @NonNull FileDescriptor fileDescriptor) {

        final PercentRelativeLayout.LayoutParams layoutParams = (PercentRelativeLayout.LayoutParams) imageView.getLayoutParams();
        int width, height;
        final ImageDescriptor imageDescriptor;
        final VideoDescriptor videoDescriptor;
        if (fileDescriptor instanceof ImageDescriptor) {
            imageDescriptor = (ImageDescriptor) fileDescriptor;
            videoDescriptor = null;
            width = imageDescriptor.getWidth();
            height = imageDescriptor.getHeight();

        } else if (fileDescriptor instanceof VideoDescriptor) {
            videoDescriptor = (VideoDescriptor) fileDescriptor;
            imageDescriptor = null;
            width = videoDescriptor.getWidth();
            height = videoDescriptor.getHeight();

        } else {
            return;
        }

        final boolean dimensionKnown = width > 0 && height > 0;
        final float aspectRatio = !dimensionKnown ? 1.0f : (float) width / (float) height;
        if (layoutParams.getPercentLayoutInfo().aspectRatio != aspectRatio) {
            layoutParams.getPercentLayoutInfo().aspectRatio = aspectRatio;
        }

        final Item.ItemMode mode = mItem.getMode();
        if (mode == Item.ItemMode.PREVIEW || mode == Item.ItemMode.SMALL_PREVIEW) {
            final int maxWidth, maxHeight;

            switch (mItem.getMode()) {
                case NORMAL:
                default:
                    maxWidth = IMAGE_ITEM_MAX_WIDTH;
                    maxHeight = IMAGE_ITEM_MAX_HEIGHT;
                    break;

                case PREVIEW:
                    maxWidth = IMAGE_ITEM_MAX_WIDTH;
                    maxHeight = FORWARDED_IMAGE_ITEM_MAX_HEIGHT;
                    layoutParams.getPercentLayoutInfo().widthPercent = layoutParams.width / (float) Design.DISPLAY_WIDTH;
                    break;

                case SMALL_PREVIEW:
                    maxWidth = IMAGE_ITEM_MAX_WIDTH;
                    maxHeight = FORWARDED_SMALL_IMAGE_ITEM_MAX_HEIGHT;
                    layoutParams.getPercentLayoutInfo().widthPercent = layoutParams.width / (float) Design.DISPLAY_WIDTH;
                    break;
            }

            if (!dimensionKnown) {
                width = maxWidth;
                height = maxHeight;
            }

            // If the image height fits in the reply view, try to use the image dimension
            // otherwise resize to the maximum height keeping the ascpect ratio.
            if (height <= maxHeight) {
                layoutParams.height = height;
                layoutParams.width = width;
            } else {
                layoutParams.height = maxHeight;
                layoutParams.width = (int) (maxHeight * aspectRatio);
            }

            // And if the width is too big, resize to the maximum width.
            if (layoutParams.width > maxWidth) {
                layoutParams.width = maxWidth;
                layoutParams.height = (int) (maxWidth / aspectRatio);
            }
            layoutParams.getPercentLayoutInfo().widthPercent = layoutParams.width / (float) Design.DISPLAY_WIDTH;
        }

        imageView.setLayoutParams(layoutParams);
        imageView.setVisibility(View.VISIBLE);

        float[] radii = getCornerRadii();
        imageView.setCornerRadii(radii);

        if (DEBUG) {
            Log.d(LOG_TAG, "Set image layout=" + layoutParams.width + "x" + layoutParams.height + " for image "
                    + " " + width + "x" + height);
        }

        Glide.with(imageView)
                .load(fileDescriptor)
                .placeholder(getPlaceholder(width, height, radii))
                .thumbnail(Glide.with(imageView).load(fileDescriptor).apply(Modes.AS_THUMBNAIL))
                .into(imageView);
    }

    private Drawable getPlaceholder(int width, int height, float[] radii) {
        GradientDrawable background = new GradientDrawable();
        background.mutate();
        background.setColor(Design.getMainStyle());
        background.setShape(GradientDrawable.RECTANGLE);
        background.setStroke(Design.BORDER_WIDTH, Color.TRANSPARENT);
        background.setCornerRadii(radii);
        background.setSize(width, height);

        Drawable icon = ContextCompat.getDrawable(mContainer.getContext(), R.drawable.add_photo);

        Drawable[] layers = {background, icon};

        LayerDrawable layerDrawable = new LayerDrawable(layers);

        float iconSizeRatio = 0.2164f;

        // Calculate insets to center the scaled icon
        int minDimension = Math.min(width, height);
        int iconSize = (int) (minDimension * iconSizeRatio);
        int insetHorizontal = (width - iconSize) / 2; // Inset based on width
        int insetVertical = (height - iconSize) / 2;   // Inset based on height

        layerDrawable.setLayerInset(1, insetHorizontal, insetVertical, insetHorizontal, insetVertical);

        return layerDrawable;
    }

    /**
     * Set the reply-to image view to display the image descriptor.  The image view is configured to use the
     * maximum REPLY_IMAGE_ITEM_MAX_HEIGHT or REPLY_IMAGE_ITEM_MAX_WIDTH.
     *
     * @param imageView the reply image view.
     * @param fileDescriptor the file descriptor to display.
     */
    protected void setReplyImage(@NonNull RoundedImageView imageView, @NonNull FileDescriptor fileDescriptor) {

        final PercentRelativeLayout.LayoutParams layoutParams = (PercentRelativeLayout.LayoutParams) imageView.getLayoutParams();
        int width, height;
        if (fileDescriptor instanceof ImageDescriptor) {
            final ImageDescriptor imageDescriptor = (ImageDescriptor) fileDescriptor;
            width = imageDescriptor.getWidth();
            height = imageDescriptor.getHeight();

        } else if (fileDescriptor instanceof VideoDescriptor) {
            final VideoDescriptor videoDescriptor = (VideoDescriptor) fileDescriptor;
            width = videoDescriptor.getWidth();
            height = videoDescriptor.getHeight();

        } else {
            return;
        }

        final boolean dimensionKnown = width > 0 && height > 0;
        if (!dimensionKnown) {
            width = REPLY_IMAGE_ITEM_MAX_WIDTH;
            height = REPLY_IMAGE_ITEM_MAX_HEIGHT;
        }

        final float aspectRatio = height == 0 ? 1.0f : (float) width / (float) height;
        if (layoutParams.getPercentLayoutInfo().aspectRatio != aspectRatio) {
            layoutParams.getPercentLayoutInfo().aspectRatio = aspectRatio;
        }

        // If the image height fits in the reply view, try to use the image dimension
        // otherwise resize to the maximum height keeping the ascpect ratio.
        if (height <= REPLY_IMAGE_ITEM_MAX_HEIGHT) {
            layoutParams.height = height;
            layoutParams.width = width;
        } else {
            layoutParams.height = REPLY_IMAGE_ITEM_MAX_HEIGHT;
            layoutParams.width = (int) (REPLY_IMAGE_ITEM_MAX_HEIGHT * aspectRatio);
        }

        // And if the width is too big, resize to the maximum width.
        if (layoutParams.width > REPLY_IMAGE_ITEM_MAX_WIDTH) {
            layoutParams.width = REPLY_IMAGE_ITEM_MAX_WIDTH;
            layoutParams.height = (int) (REPLY_IMAGE_ITEM_MAX_WIDTH / aspectRatio);
        }
        imageView.setLayoutParams(layoutParams);
        imageView.setVisibility(View.VISIBLE);

        float[] radii = new float[8];
        Arrays.fill(radii, REPLY_IMAGE_RADIUS);
        imageView.setCornerRadii(radii);

        if (DEBUG) {
            Log.d(LOG_TAG, "Reply image layout=" + layoutParams.width + "x" + layoutParams.height + " for image "
                    + " " + width + "x" + height);
        }

        Glide.with(imageView).load(fileDescriptor)
                .override(layoutParams.width, layoutParams.height).fitCenter().into(imageView);
    }

    private final BaseItemActivity mBaseItemActivity;

    private final View mContainer;
    private final View mOverlayView;

    private final View mSelectedView;
    private final ImageView mSelectedImageView;

    private Item mItem;
    private int mCorners;

    BaseItemViewHolder(BaseItemActivity baseItemActivity, View view) {

        super(view);

        mBaseItemActivity = baseItemActivity;

        mContainer = null;
        mOverlayView = null;

        mSelectedView = null;
        mSelectedImageView = null;
    }

    BaseItemViewHolder(BaseItemActivity baseItemActivity, View view, int overlayViewId) {

        super(view);

        mBaseItemActivity = baseItemActivity;

        mContainer = null;

        mOverlayView = view.findViewById(overlayViewId);
        mOverlayView.setBackgroundColor(Design.BACKGROUND_COLOR_WHITE_OPACITY85);
        mOverlayView.setOnClickListener(v -> mBaseItemActivity.closeMenu());

        mSelectedView = null;
        mSelectedImageView = null;
    }

    BaseItemViewHolder(BaseItemActivity baseItemActivity, View view, int containerViewId, int overlayViewId, int selectViewId, int selectViewImageId) {

        super(view);

        mBaseItemActivity = baseItemActivity;

        mContainer = view.findViewById(containerViewId);
        mContainer.setOnTouchListener(new BaseItemViewHolder.ItemViewHolderTouchListener());
        mContainer.setOnClickListener(v -> onContainerClick());
        mContainer.setClickable(false);

        mOverlayView = view.findViewById(overlayViewId);
        mOverlayView.setBackgroundColor(Design.BACKGROUND_COLOR_WHITE_OPACITY85);
        mOverlayView.setOnClickListener(v -> mBaseItemActivity.closeMenu());

        mSelectedView = view.findViewById(selectViewId);

        ViewGroup.LayoutParams layoutParams = mSelectedView.getLayoutParams();
        layoutParams.height = CHECKBOX_HEIGHT;

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mSelectedView.getLayoutParams();
        marginLayoutParams.leftMargin = CHECKBOX_MARGIN;
        marginLayoutParams.setMarginStart(CHECKBOX_MARGIN);

        mSelectedImageView = view.findViewById(selectViewImageId);
        mSelectedImageView.setColorFilter(Design.getMainStyle());
    }

    BaseItemActivity getBaseItemActivity() {

        return mBaseItemActivity;
    }

    public Item getItem() {

        return mItem;
    }

    @NonNull
    View getContainer() {

        return mContainer;

    }

    @NonNull
    View getOverlayView() {

        return mOverlayView;

    }

    void onBind(Item item) {

        mItem = item;
        mCorners = item.getCorners();

        if (mSelectedView != null) {
            if (mBaseItemActivity.isSelectItemMode()) {
                mSelectedView.setVisibility(View.VISIBLE);
            } else {
                mSelectedView.setVisibility(View.GONE);
            }

            if (mItem.isSelected()) {
                mSelectedImageView.setVisibility(View.VISIBLE);
            } else {
                mSelectedImageView.setVisibility(View.GONE);
            }
        }
    }

    void onViewAttachedToWindow() {
    }

    void onViewRecycled() {
    }

    protected void onReplyClick() {

        ConversationService.DescriptorId replyDescriptorId = getItem().getReplyToDescriptorId();

        if (replyDescriptorId != null) {
            mBaseItemActivity.onReplyClick(replyDescriptorId);
        }
    }

    protected void onContainerClick() {

        if (mBaseItemActivity.isSelectItemMode()) {
            if (mSelectedView != null) {
                mBaseItemActivity.onItemClick(mItem);
            }
        } else {
            mBaseItemActivity.closeMenu();
        }
    }

    void updateCornerWithMask(int mask) {

        mCorners &= mask;
    }

    float[] getCornerRadii() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getCornerRadii");
        }

        float[] radii = new float[8];
        Arrays.fill(radii, ITEM_SMALL_RADIUS);

        if (CommonUtils.isLayoutDirectionRTL()) {
            if ((mCorners & Item.TOP_RIGHT) != 0) {
                radii[0] = ITEM_LARGE_RADIUS;
                radii[1] = ITEM_LARGE_RADIUS;
            }
            if ((mCorners & Item.TOP_LEFT) != 0) {
                radii[2] = ITEM_LARGE_RADIUS;
                radii[3] = ITEM_LARGE_RADIUS;
            }
            if ((mCorners & Item.BOTTOM_LEFT) != 0) {
                radii[4] = ITEM_LARGE_RADIUS;
                radii[5] = ITEM_LARGE_RADIUS;
            }
            if ((mCorners & Item.BOTTOM_RIGHT) != 0) {
                radii[6] = ITEM_LARGE_RADIUS;
                radii[7] = ITEM_LARGE_RADIUS;
            }
        } else {
            if ((mCorners & Item.TOP_LEFT) != 0) {
                radii[0] = ITEM_LARGE_RADIUS;
                radii[1] = ITEM_LARGE_RADIUS;
            }
            if ((mCorners & Item.TOP_RIGHT) != 0) {
                radii[2] = ITEM_LARGE_RADIUS;
                radii[3] = ITEM_LARGE_RADIUS;
            }
            if ((mCorners & Item.BOTTOM_RIGHT) != 0) {
                radii[4] = ITEM_LARGE_RADIUS;
                radii[5] = ITEM_LARGE_RADIUS;
            }
            if ((mCorners & Item.BOTTOM_LEFT) != 0) {
                radii[6] = ITEM_LARGE_RADIUS;
                radii[7] = ITEM_LARGE_RADIUS;
            }
        }

        return radii;
    }

    List<View> clickableViews() {

        return new ArrayList<View>() {
        };
    }

    @NonNull
    protected String getPath(@NonNull FileDescriptor fileDescriptor) {

        String path = fileDescriptor.getPath();
        File file = new File(mBaseItemActivity.getTwinmeContext().getFilesDir(), path);
        return file.getPath();
    }

    @NonNull
    protected TextStyle getMessageFont() {

        return mBaseItemActivity.getMessageFont();
    }

    protected boolean isMenuOpen() {

        return mBaseItemActivity.isMenuOpen();
    }

    protected boolean isSelectedItem(@NonNull ConversationService.DescriptorId descriptorId) {

        return mBaseItemActivity.isSelectedItem(descriptorId);
    }

    protected void addLoader(@NonNull Loader<Item> loader) {

        mBaseItemActivity.addLoadableItem(loader);
    }

    protected void deleteItem(@NonNull Item item) {

        mBaseItemActivity.toast(getString(R.string.conversation_activity_delete_message));

        mBaseItemActivity.deleteItem(item.getDescriptorId());
    }

    @NonNull
    protected final String getString(@StringRes int resId) {

        return mBaseItemActivity.getString(resId);
    }

    //
    // Private methods
    //

    private View getClickedView(int x, int y) {

        for (View subview : clickableViews()) {
            if (isPointInsideView(x, y, subview)) {

                return subview;
            }
        }

        return null;
    }

    private boolean isPointInsideView(float x, float y, View view) {

        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        return x > viewX && x < viewX + view.getWidth() && y > viewY && y < viewY + view.getHeight();
    }
}
