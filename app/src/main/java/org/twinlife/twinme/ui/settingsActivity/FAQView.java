/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.FeedbackActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.WebViewActivity;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.faq.UIFAQArticle;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FAQView extends AbstractBottomSheetView {

    private static final String LOG_TAG = "FAQView";
    private static final boolean DEBUG = false;

    public interface LinkObserver {

        void onFAQLinkClick(String url);
    }

    private static final int DESIGN_TITLE_TOP_MARGIN = 40;
    private static final int DESIGN_MESSAGE_HORIZONTAL_MARGIN = 52;
    private static final int DESIGN_MIN_HEIGHT = 460;

    private static final String IMAGE_PATTERN = "\\[\\[(.*?)\\]\\]";

    // Links
    protected static final String PRIVACY_POLICY_LINK = "https://twin.me/privacy-policy/";
    protected static final String CONTACT_LINK = "https://twin.me/en/contact/";
    protected static final String VIDEO_PRESENTATION_LINK = "https://youtu.be/uj2bFKQ_L60";
    protected static final String KURIO_LINK = "https://www.youtube.com/embed/BZCIT-g5tBo/";
    protected static final String DONT_KILL_MY_APP_LINK = "https://dontkillmyapp.com/";
    protected static final String CONNECT_PEOPLE_LINK = "https://twin.me/support/connect-people";
    protected static final String OPEN_SOURCE_LINK = "https://github.com/France-en-Ligne";

    //Images
    protected static final String ADD_CONTACT_IMAGE = "AddContact";
    protected static final String CHAT_IMAGE = "Chat";
    protected static final String SEND_IMAGE = "Send";
    protected static final String TRASH_IMAGE = "Trash";
    protected static final String CAMERA_IMAGE = "Camera";
    protected static final String GALLERY_IMAGE = "Gallery";
    protected static final String SPINNER_IMAGE = "Spinner";
    protected static final String RECEIVED_STATE_IMAGE = "ReceivedState";
    protected static final String NOT_SENT_STATE_IMAGE = "NotSentState";
    protected static final String MICRO_MUTE_IMAGE = "MicroMute";
    protected static final String SPEAKER_ON_IMAGE = "SpeakerOn";
    protected static final String AUDIO_CALL_IMAGE = "AudioCall";
    protected static final String VIDEO_CALL_IMAGE = "VideoCall";
    protected static final String SWITCH_CAMERA_IMAGE = "SwitchCamera";
    protected static final String VIDEO_MUTE_IMAGE = "VideoMute";
    protected static final String MENU_IMAGE = "Menu";

    protected View mRootView;
    private final Map<String, Integer> mImagesMap = new HashMap<>();

    private LinkObserver mLinkObserver;

    public FAQView(Context context) {
        super(context);
    }

    public FAQView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = inflater.inflate(R.layout.faq_view, this, true);
        initImages();
        initViews();
    }

    public void setLinkObserver(LinkObserver observer) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setObserver: " + observer);
        }

        mLinkObserver = observer;
    }

    public void setArticle(UIFAQArticle uifaqArticle) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setArticle");
        }

        mTitleView.setText(uifaqArticle.getTitle());

        Spanned spannable = Html.fromHtml(uifaqArticle.getAnswer());
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(spannable);

        Pattern pattern = Pattern.compile(IMAGE_PATTERN);
        Matcher matcher = pattern.matcher(spannable);

        while (matcher.find()) {
            String imageName = matcher.group(1);

            if (imageName != null && mImagesMap.containsKey(imageName)) {
                Integer imageResId = mImagesMap.get(imageName);
                if (imageResId != null) {
                    Drawable drawable = ContextCompat.getDrawable(getContext(), imageResId);
                    if (drawable != null) {
                        int size = (int) mMessageView.getTextSize();
                        drawable.setBounds(0, 0, size, size);

                        if (needsTintForDrawable(imageName)) {
                            drawable.setTint(Design.FONT_COLOR_DEFAULT);
                        }


                        int start = matcher.start();
                        int end = matcher.end();

                        ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
                        spannableStringBuilder.setSpan(imageSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }

        URLSpan[] urls = spannableStringBuilder.getSpans(0, spannableStringBuilder.length(), URLSpan.class);
        for (URLSpan span : urls) {
            int start = spannable.getSpanStart(span);
            int end = spannable.getSpanEnd(span);

            spannableStringBuilder.removeSpan(span);

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View v) {
                    interactWithURL(span.getURL());
                }
            };
            spannableStringBuilder.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        mMessageView.setText(spannableStringBuilder);
        mMessageView.setMovementMethod(LinkMovementMethod.getInstance());
        mMessageView.setLinksClickable(true);
        updateMessageHeight(uifaqArticle.getTitle());
    }

    @Override
    protected void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        mOverlayView = mRootView.findViewById(R.id.faq_view_overlay_view);
        mActionView = mRootView.findViewById(R.id.faq_view_action_view);
        mSlideMarkView = mRootView.findViewById(R.id.faq_view_slide_mark_view);
        mTitleView = mRootView.findViewById(R.id.faq_view_title_view);
        mMessageView = mRootView.findViewById(R.id.faq_view_message_view);
        mConfirmView = mRootView.findViewById(R.id.faq_view_confirm_view);
        mConfirmTextView = mRootView.findViewById(R.id.faq_view_confirm_text_view);

        super.initViews();

        Design.updateTextFont(mTitleView, Design.FONT_MEDIUM36);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_MESSAGE_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_MESSAGE_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        mMessageView.setMovementMethod(new ScrollingMovementMethod());

        Design.updateTextFont(mMessageView, Design.FONT_REGULAR32);
        mMessageView.setTextColor(Design.FONT_COLOR_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mMessageView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_MESSAGE_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_MESSAGE_HORIZONTAL_MARGIN * Design.WIDTH_RATIO);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable confirmViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        confirmViewBackground.getPaint().setColor(Design.getMainStyle());
        mConfirmView.setBackground(confirmViewBackground);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mConfirmView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_TITLE_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_TITLE_TOP_MARGIN * Design.HEIGHT_RATIO);
    }

    private void initImages() {

        mImagesMap.put(ADD_CONTACT_IMAGE, R.drawable.action_bar_add_contact);
        mImagesMap.put(CHAT_IMAGE, R.drawable.call_chat_icon);
        mImagesMap.put(SEND_IMAGE, R.drawable.send_icon);
        mImagesMap.put(TRASH_IMAGE, R.drawable.toolbar_trash_grey);
        mImagesMap.put(CAMERA_IMAGE, R.drawable.grey_camera);
        mImagesMap.put(GALLERY_IMAGE, R.drawable.gallery_icon);
        mImagesMap.put(RECEIVED_STATE_IMAGE, R.drawable.received_state);
        mImagesMap.put(NOT_SENT_STATE_IMAGE, R.drawable.not_sent_state);
        mImagesMap.put(MICRO_MUTE_IMAGE, R.drawable.micro_mute_on);
        mImagesMap.put(SPEAKER_ON_IMAGE, R.drawable.loud_speaker_action_call_on);
        mImagesMap.put(AUDIO_CALL_IMAGE, R.drawable.audio_call);
        mImagesMap.put(VIDEO_CALL_IMAGE, R.drawable.video_call);
        mImagesMap.put(SWITCH_CAMERA_IMAGE, R.drawable.turn_action_call);
        mImagesMap.put(VIDEO_MUTE_IMAGE, R.drawable.video_mute_action_on);
        mImagesMap.put(MENU_IMAGE, R.drawable.side_menu);
    }

    private void interactWithURL(String url) {
        if (DEBUG) {
            Log.d(LOG_TAG, "interactWithURL: " + url);
        }

        if (url.equals(VIDEO_PRESENTATION_LINK) || url.equals(KURIO_LINK) || url.equals(DONT_KILL_MY_APP_LINK) || url.equals(OPEN_SOURCE_LINK)) {
            Context context = getContext();
            if (context != null) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        } else {
            Context context = getContext();
            if (context == null) {
                return;
            }

            switch (url) {
                case PRIVACY_POLICY_LINK: {
                    Intent intent = new Intent(context, WebViewActivity.class);
                    intent.putExtra(WebViewActivity.INTENT_WEB_VIEW_ACTIVITY_URL, "file:///android_res/raw/privacy_policy.html");
                    intent.putExtra(Intents.INTENT_TITLE, context.getString(R.string.about_view_privacy_policy));
                    context.startActivity(intent);
                    break;
                }
                case CONTACT_LINK: {
                    Intent intent = new Intent(context, FeedbackActivity.class);
                    context.startActivity(intent);
                    break;
                }
                case CONNECT_PEOPLE_LINK:
                    mLinkObserver.onFAQLinkClick(url);
                    animationCloseConfirmView();
                    break;
            }
        }
    }

    private boolean needsTintForDrawable(@NonNull String imageName) {

        return !imageName.equals(RECEIVED_STATE_IMAGE) && !imageName.equals(NOT_SENT_STATE_IMAGE);
    }

    private void updateMessageHeight(String text) {

        Paint paint = new Paint();
        paint.setTextSize(mTitleView.getTextSize());

        TextPaint textPaint = new TextPaint(paint);

        int widthPx = Design.DISPLAY_WIDTH - (int) ((DESIGN_MESSAGE_HORIZONTAL_MARGIN * 2) * Design.WIDTH_RATIO);
        StaticLayout staticLayout = new StaticLayout(
                text,
                textPaint,
                widthPx,
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                0.0f,
                false
        );

        int height = staticLayout.getHeight();

        int messageMaxHeight = (int) (Design.DISPLAY_HEIGHT - (height + (DESIGN_MIN_HEIGHT * Design.HEIGHT_RATIO)));
        mMessageView.setMaxHeight(messageMaxHeight);
    }
}