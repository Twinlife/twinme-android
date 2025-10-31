/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.graphics.drawable.GradientDrawable;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.utils.CommonUtils;

import java.util.Arrays;

public class AbstractCallMessageViewHolder extends RecyclerView.ViewHolder {

    protected abstract static class TextViewLinkMovementMethod extends LinkMovementMethod {

        public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent motionEvent) {

            if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
                return super.onTouchEvent(textView, spannable, motionEvent);
            }

            int x = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();

            x -= textView.getTotalPaddingLeft();
            y -= textView.getTotalPaddingTop();

            x += textView.getScrollX();
            y += textView.getScrollY();

            Layout layout = textView.getLayout();
            int line = layout.getLineForVertical(y);
            int offset = layout.getOffsetForHorizontal(line, x);

            URLSpan[] link = spannable.getSpans(offset, offset, URLSpan.class);
            if (link.length != 0) {
                onLinkClick(link[0].getURL());
            }
            return true;
        }

        abstract public void onLinkClick(String url);
    }

    private static final int MAX_EMOJI = 5;

    private static final float DESIGN_MESSAGE_ITEM_TEXT_HEIGHT_PADDING = 10f;
    private static final float DESIGN_MESSAGE_ITEM_TEXT_WIDTH_PADDING = 32f;
    private static final float DESIGN_ITEM_SMALL_ROUND_CORNER_RADIUS = 8f;
    private static final float DESIGN_ITEM_LARGE_ROUND_CORNER_RADIUS = 38f;
    private static final float DESIGN_ITEM_TOP_MARGIN1 = 4f;
    private static final float DESIGN_ITEM_TOP_MARGIN2 = 18f;
    private static final float DESIGN_ITEM_BOTTOM_MARGIN1 = 4f;
    private static final float DESIGN_ITEM_BOTTOM_MARGIN2 = 18f;
    private static final float DESIGN_MESSAGE_MAX_WIDTH = 508f;
    private static final float DESIGN_MESSAGE_MARGIN = 20f;
    protected static final int MESSAGE_ITEM_TEXT_HEIGHT_PADDING;
    protected static final int MESSAGE_ITEM_TEXT_WIDTH_PADDING;
    private static final float ITEM_SMALL_RADIUS;
    private static final float ITEM_LARGE_RADIUS;
    protected static final int ITEM_TOP_MARGIN1;
    protected static final int ITEM_TOP_MARGIN2;
    protected static final int ITEM_BOTTOM_MARGIN1;
    protected static final int ITEM_BOTTOM_MARGIN2;
    protected static final int MESSAGE_MARGIN;
    protected static final int MESSAGE_MAX_WIDTH;

    static {
        ITEM_SMALL_RADIUS = DESIGN_ITEM_SMALL_ROUND_CORNER_RADIUS * Design.HEIGHT_RATIO;
        ITEM_LARGE_RADIUS = DESIGN_ITEM_LARGE_ROUND_CORNER_RADIUS * Design.HEIGHT_RATIO;
        MESSAGE_ITEM_TEXT_HEIGHT_PADDING = (int) (DESIGN_MESSAGE_ITEM_TEXT_HEIGHT_PADDING * Design.HEIGHT_RATIO);
        MESSAGE_ITEM_TEXT_WIDTH_PADDING = (int) (DESIGN_MESSAGE_ITEM_TEXT_WIDTH_PADDING * Design.WIDTH_RATIO);
        ITEM_TOP_MARGIN1 = (int) (DESIGN_ITEM_TOP_MARGIN1 * Design.HEIGHT_RATIO);
        ITEM_TOP_MARGIN2 = (int) (DESIGN_ITEM_TOP_MARGIN2 * Design.HEIGHT_RATIO);
        ITEM_BOTTOM_MARGIN1 = (int) (DESIGN_ITEM_BOTTOM_MARGIN1 * Design.HEIGHT_RATIO);
        ITEM_BOTTOM_MARGIN2 = (int) (DESIGN_ITEM_BOTTOM_MARGIN2 * Design.HEIGHT_RATIO);
        MESSAGE_MARGIN = (int) (DESIGN_MESSAGE_MARGIN * Design.WIDTH_RATIO);
        MESSAGE_MAX_WIDTH = (int) (DESIGN_MESSAGE_MAX_WIDTH * Design.WIDTH_RATIO);
    }

    protected View mContainerView;

    protected TextView mTextView;
    protected GradientDrawable mGradientDrawable;

    protected int mCorners;

    AbstractCallMessageViewHolder(CallActivity callActivity, View view) {

        super(view);
    }

    protected int countEmoji(String content) {

        int countEmoji = 0;
        int length = content.codePointCount(0, content.length());

        if (length > MAX_EMOJI) {
            return 0;
        }

        int index = 0;

        while (index < content.length()) {
            int codePoint = content.codePointAt(index);
            int charCount = Character.charCount(codePoint);

            boolean isEmoji = true;

            for (int i = index; i < index + charCount; i++) {
                char character = content.charAt(i);
                int type = Character.getType(character);

                if (type != Character.SURROGATE && type != Character.OTHER_SYMBOL) {
                    isEmoji = false;
                    break;
                }
            }

            if (isEmoji) {
                countEmoji++;
            } else {
                return 0;
            }

            index += charCount;

            if (countEmoji == MAX_EMOJI) {
                break;
            }
        }

        return countEmoji;
    }

    protected float[] getCornerRadii() {

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
}
