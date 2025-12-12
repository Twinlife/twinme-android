/*
 *  Copyright (c) 2016-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Houssem Temanni (Houssem.Temanni@twinlife-systems.com)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Yannis Le Gal (Yannis.LeGal@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService.Descriptor;
import org.twinlife.twinlife.ConversationService.ImageDescriptor;
import org.twinlife.twinlife.ConversationService.NamedFileDescriptor;
import org.twinlife.twinlife.ConversationService.ObjectDescriptor;
import org.twinlife.twinlife.ConversationService.VideoDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.AudioTrack;
import org.twinlife.twinme.utils.AudioTrackView;
import org.twinlife.twinme.utils.EphemeralView;
import org.twinlife.twinme.utils.RoundedImageView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.Utils;
import org.twinlife.twinme.utils.async.AudioTrackLoader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PeerAudioItemViewHolder extends PeerItemViewHolder {
    private static final String LOG_TAG = "PeerAudioItemViewHolder";
    private static final boolean DEBUG = false;

    private static final int DESIGN_AUDIO_CONTAINER_WIDTH = 580;
    private static final int DESIGN_AUDIO_CONTAINER_HEIGHT = 134;
    private static final int DESIGN_AUDIO_TRACK_WIDTH = 416;
    private static final int DESIGN_AUDIO_TRACK_HEIGHT = 60;
    private static final int DESIGN_AUDIO_SPEED_MARGIN = 28;
    private static final int DESIGN_AUDIO_SPEED_BOTTOM_MARGIN = 12;
    private static final int DESIGN_AUDIO_SPEED_WIDTH = 80;
    private static final int DESIGN_AUDIO_SPEED_HEIGHT = 44;

    private static final float DESIGN_EPHEMERAL_HEIGHT = 28f;
    private static final float DESIGN_EPHEMERAL_LEFT_MARGIN = 14f;
    private static final float DESIGN_EPHEMERAL_TOP_MARGIN = 4f;
    private static final float DESIGN_EPHEMERAL_BOTTOM_MARGIN = 4f;

    private final BaseItemActivity.AudioItemObserver mAudioItemObserver;

    private final View mAudioItemContainer;
    private final GradientDrawable mGradientDrawable;
    private final View mPlayButton;
    private final View mStopButton;
    private final View mSpeedView;
    private final TextView mSpeedTextView;
    private final TextView mCounter;
    private final View mReplyView;
    private final TextView mReplyTextView;
    private final View mReplyToImageContentView;
    private final RoundedImageView mReplyImageView;
    private final GradientDrawable mReplyGradientDrawable;
    private final GradientDrawable mReplyToImageContentGradientDrawable;
    private final EphemeralView mEphemeralView;
    private final AudioTrackView mAudioTrackView;

    @Nullable
    private CountDownTimer mTimer;

    @Nullable
    private ExoPlayer mExoPlayer;
    @Nullable
    private CountDownTimer mCounterDown = null;
    private long mPlayerPosition;
    private float mPlayerPositionPercent = -1;
    private String mDuration;
    @Nullable
    private AudioTrackLoader<Item> mAudioLoader;

    private boolean mIsAudioTrackTouch = false;

    @SuppressLint({"NewApi", "ClickableViewAccessibility"})
    PeerAudioItemViewHolder(BaseItemActivity baseItemActivity, View view, BaseItemActivity.AudioItemObserver audioItemObserver) {

        super(baseItemActivity, view,
                R.id.base_item_activity_peer_audio_item_container,
                R.id.base_item_activity_peer_audio_item_avatar,
                R.id.base_item_activity_peer_audio_item_overlay_view,
                R.id.base_item_activity_peer_audio_item_annotation_view,
                R.id.base_item_activity_peer_audio_item_selected_view,
                R.id.base_item_activity_peer_audio_item_selected_image_view);

        mAudioItemObserver = audioItemObserver;

        mAudioItemContainer = view.findViewById(R.id.base_item_activity_peer_audio_item_view);

        ViewGroup.LayoutParams layoutParams = mAudioItemContainer.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AUDIO_CONTAINER_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_AUDIO_CONTAINER_HEIGHT * Design.HEIGHT_RATIO);

        mAudioItemContainer.setOnClickListener(v -> {
            if (getBaseItemActivity().isSelectItemMode()) {
                onContainerClick();
            }
        });

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.GREY_ITEM_COLOR);
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mAudioItemContainer.setBackground(mGradientDrawable);

        RoundedView controlRoundedView = view.findViewById(R.id.base_item_activity_peer_audio_control_rounded_view);
        controlRoundedView.setColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mPlayButton = view.findViewById(R.id.base_item_activity_peer_audio_item_play);

        ImageView playImageView = view.findViewById(R.id.base_item_activity_peer_audio_item_play_image_view);
        playImageView.setColorFilter(Design.getMainStyle());

        mStopButton = view.findViewById(R.id.base_item_activity_peer_audio_item_stop);

        ImageView stopImageView = view.findViewById(R.id.base_item_activity_peer_audio_item_stop_image_view);
        stopImageView.setColorFilter(Design.getMainStyle());

        mCounter = view.findViewById(R.id.base_item_activity_peer_audio_item_counter);
        mCounter.setTextColor(Design.FONT_COLOR_DEFAULT);
        Design.updateTextFont(mCounter, Design.FONT_MEDIUM26);
        mCounter.setTextColor(getBaseItemActivity().getCustomAppearance().getPeerMessageTextColor());

        mAudioTrackView = view.findViewById(R.id.base_item_activity_peer_audio_item_track_view);

        layoutParams = mAudioTrackView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AUDIO_TRACK_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_AUDIO_TRACK_HEIGHT * Design.HEIGHT_RATIO);

        mAudioTrackView.setOnTouchListener((v, motionEvent) -> {

            float touchX = motionEvent.getRawX() - (mAudioItemContainer.getX() + mAudioTrackView.getX());

            if (touchX < 0) {
                touchX = 0;
            } else if (touchX > mAudioTrackView.getWidth()) {
                touchX = mAudioTrackView.getWidth();
            }

            PeerAudioItem peerAudioItem = getPeerAudioItem();
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mIsAudioTrackTouch = true;
                    if (peerAudioItem != null) {
                        peerAudioItem.setCanReply(false);
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mIsAudioTrackTouch = false;
                    if (peerAudioItem != null) {
                        peerAudioItem.setCanReply(true);
                    }
                    break;
            }

            setProgressTrack((int) touchX);

            if (!mIsAudioTrackTouch && peerAudioItem != null) {
                mPlayerPositionPercent = touchX / mAudioTrackView.getWidth();

                if (mExoPlayer != null) {
                    mExoPlayer.seekTo(getPlayerPosition());
                    startCountdown();
                }

                int remainingTime = (int) (peerAudioItem.getAudioDescriptor().getDuration() - mPlayerPosition / 1000);
                String format = "mm:ss";
                int hour = 60 * 60;
                if (remainingTime > hour) {
                    format = "hh:mm:ss";
                }
                mCounter.setText(Utils.formatInterval(remainingTime, format));

            }

            return true;
        });

        mPlayButton.setOnClickListener(this::startPlayer);

        mStopButton.setOnClickListener(this::stopPlayer);

        mAudioItemContainer.setOnLongClickListener(v -> {
            if (!mIsAudioTrackTouch) {
                baseItemActivity.onItemLongPress(getItem());
            }
            return false;
        });

        mSpeedView = view.findViewById(R.id.base_item_activity_peer_audio_item_speed_view);
        mSpeedView.setVisibility(View.GONE);
        mSpeedView.setOnClickListener(v -> onSpeedViewClick());

        float radius = DESIGN_AUDIO_SPEED_HEIGHT * Design.HEIGHT_RATIO * 0.5f * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable speedViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        speedViewBackground.getPaint().setColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        mSpeedView.setBackground(speedViewBackground);

        layoutParams = mSpeedView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_AUDIO_SPEED_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_AUDIO_SPEED_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mSpeedView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_AUDIO_SPEED_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_AUDIO_SPEED_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        mSpeedTextView = view.findViewById(R.id.base_item_activity_peer_audio_item_speed_text_view);
        mSpeedTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        Design.updateTextFont(mSpeedTextView, Design.FONT_MEDIUM26);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSpeedTextView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (4 * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (4 * Design.WIDTH_RATIO);

        mReplyTextView = view.findViewById(R.id.base_item_activity_peer_audio_item_reply_text);
        mReplyTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);
        mReplyTextView.setTypeface(getMessageFont().typeface);
        mReplyTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getMessageFont().size);
        mReplyTextView.setTextColor(Design.REPLY_FONT_COLOR);
        mReplyTextView.setMaxLines(3);
        mReplyTextView.setEllipsize(TextUtils.TruncateAt.END);

        mReplyView = view.findViewById(R.id.base_item_activity_peer_audio_item_reply_view);

        mReplyView.setOnClickListener(v -> onReplyClick());

        mReplyView.setOnLongClickListener(v -> {
            if (!mIsAudioTrackTouch) {
                baseItemActivity.onItemLongPress(getItem());
            }
            return true;
        });

        mReplyGradientDrawable = new GradientDrawable();
        mReplyGradientDrawable.mutate();
        mReplyGradientDrawable.setColor(Design.REPLY_BACKGROUND_COLOR);
        mReplyGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mReplyView.setBackground(mReplyGradientDrawable);

        mReplyImageView = view.findViewById(R.id.base_item_activity_peer_audio_item_reply_image_view);
        layoutParams = mReplyImageView.getLayoutParams();
        layoutParams.width = REPLY_IMAGE_ITEM_MAX_WIDTH;
        layoutParams.height = REPLY_IMAGE_ITEM_MAX_HEIGHT;

        View replyContainerImageView = view.findViewById(R.id.base_item_activity_peer_audio_item_reply_container_image_view);
        replyContainerImageView.setPadding(REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN, REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN);

        mReplyToImageContentView = view.findViewById(R.id.base_item_activity_peer_audio_item_reply_image_content_view);

        mReplyToImageContentView.setOnClickListener(v -> onReplyClick());

        mReplyToImageContentView.setOnLongClickListener(v -> {
            if (!mIsAudioTrackTouch) {
                baseItemActivity.onItemLongPress(getItem());
            }
            return true;
        });

        mReplyToImageContentGradientDrawable = new GradientDrawable();
        mReplyToImageContentGradientDrawable.mutate();
        mReplyToImageContentGradientDrawable.setColor(Design.REPLY_BACKGROUND_COLOR);
        mReplyToImageContentGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mReplyToImageContentView.setBackground(mReplyToImageContentGradientDrawable);

        View containerEphemeralView = view.findViewById(R.id.base_item_activity_peer_audio_item_ephemeral_content_view);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) containerEphemeralView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_AUDIO_SPEED_MARGIN * Design.WIDTH_RATIO);

        mEphemeralView = view.findViewById(R.id.base_item_activity_peer_audio_item_ephemeral_view);
        mEphemeralView.setColor(Design.BLACK_COLOR);

        layoutParams = mEphemeralView.getLayoutParams();
        mEphemeralView.setColor(Design.BLACK_COLOR);

        layoutParams.height = (int) (DESIGN_EPHEMERAL_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mEphemeralView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_EPHEMERAL_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_EPHEMERAL_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_EPHEMERAL_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_EPHEMERAL_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_EPHEMERAL_LEFT_MARGIN * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_EPHEMERAL_LEFT_MARGIN * Design.WIDTH_RATIO));
    }

    @Override
    public void onBind(Item item) {

        if (!(item instanceof PeerAudioItem)) {
            return;
        }

        super.onBind(item);

        final PeerAudioItem audioItem = (PeerAudioItem) item;

        // Compute the corner radii only once!
        final float[] cornerRadii = getCornerRadii();

        mGradientDrawable.setCornerRadii(cornerRadii);
        mGradientDrawable.setColor(getBaseItemActivity().getCustomAppearance().getPeerMessageBackgroundColor());
        if (getBaseItemActivity().getCustomAppearance().getPeerMessageBorderColor() != Color.TRANSPARENT) {
            mGradientDrawable.setStroke(Design.BORDER_WIDTH, getBaseItemActivity().getCustomAppearance().getPeerMessageBorderColor());
        }

        if (mAudioLoader == null) {
            mAudioLoader = new AudioTrackLoader<>(item, audioItem.getAudioDescriptor(), getAudioTrackNbLines());
            addLoader(mAudioLoader);
        }

        // Use an async loader to get the audio track.
        AudioTrack audioTrack = mAudioLoader.getAudioTrack();
        if (audioTrack != null) {
            mAudioTrackView.initTrack(audioTrack, Design.PEER_AUDIO_TRACK_COLOR, Design.getMainStyle());
        }

        int duration = (int) audioItem.getAudioDescriptor().getDuration();
        String format = "mm:ss";
        int hour = 60 * 60;
        if (duration > hour) {
            format = "hh:mm:ss";
        }
        mDuration = Utils.formatInterval(duration, format);
        mCounter.setText(mDuration);
        updateSpeed();

        mReplyGradientDrawable.setCornerRadii(cornerRadii);
        mReplyToImageContentGradientDrawable.setCornerRadii(cornerRadii);

        mReplyView.setVisibility(View.GONE);
        mReplyTextView.setVisibility(View.GONE);
        mReplyToImageContentView.setVisibility(View.GONE);
        mReplyImageView.setVisibility(View.GONE);

        final Descriptor replyToDescriptor = item.getReplyToDescriptor();
        if (replyToDescriptor != null) {

            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) mAudioItemContainer.getLayoutParams();

            switch (replyToDescriptor.getType()) {
                case OBJECT_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_audio_item_reply_text);

                    ObjectDescriptor objectDescriptor = (ObjectDescriptor) replyToDescriptor;
                    mReplyTextView.setText(objectDescriptor.getMessage());
                    break;

                case IMAGE_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_audio_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (ImageDescriptor) replyToDescriptor);
                    break;

                case VIDEO_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_audio_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (VideoDescriptor) replyToDescriptor);
                    break;

                case AUDIO_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_audio_item_reply_text);

                    mReplyTextView.setText(getString(R.string.conversation_activity_audio_message));
                    break;

                case NAMED_FILE_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_peer_audio_item_reply_text);

                    NamedFileDescriptor fileDescriptor = (NamedFileDescriptor) replyToDescriptor;
                    mReplyTextView.setText(fileDescriptor.getName());
                    break;
            }
        }

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mSpeedView.getLayoutParams();
        if (item.isEphemeralItem()) {
            mEphemeralView.setVisibility(View.VISIBLE);
            marginLayoutParams.rightMargin = (int) (DESIGN_AUDIO_SPEED_MARGIN * Design.WIDTH_RATIO * 2) + (int) (DESIGN_EPHEMERAL_HEIGHT * Design.HEIGHT_RATIO);
            startEphemeralAnimation();
        } else {
            mEphemeralView.setVisibility(View.GONE);
            marginLayoutParams.rightMargin = (int) (DESIGN_AUDIO_SPEED_MARGIN * Design.WIDTH_RATIO);
        }
    }

    public void resetView() {

        mPlayerPosition = 0;
        mCounter.setText(mDuration);
        mStopButton.setVisibility(View.INVISIBLE);
        mPlayButton.setVisibility(View.VISIBLE);
        mSpeedView.setVisibility(View.GONE);

        if (mExoPlayer != null) {
            if (mCounterDown != null) {
                mCounterDown.cancel();
            }
            mExoPlayer.release();
            mExoPlayer = null;
            mCounterDown = null;
        }
    }

    public long getItemSequenceId() {

        return getItem().getDescriptorId().sequenceId;
    }

    @Override
    List<View> clickableViews() {

        return new ArrayList<View>() {
            {
                add(mAudioItemContainer);
                add(getContainer());
            }
        };
    }

    @Override
    void onViewRecycled() {

        super.onViewRecycled();

        mAudioTrackView.initTrack(null, Design.PEER_AUDIO_TRACK_COLOR, Design.getMainStyle());
        mReplyImageView.setImageBitmap(null, null);
        mSpeedView.setVisibility(View.GONE);

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    //
    // Private methods
    //

    private void startPlayer(@Nullable View v) {
        if (getBaseItemActivity().isSelectItemMode()) {
            onContainerClick();
            return;
        }

        PeerAudioItem audioItem = getPeerAudioItem();

        if (audioItem == null) {
            if (DEBUG) {
                Log.d(LOG_TAG, "No PeerAudioItem, aborting playback");
            }
            return;
        }

        if (mAudioItemObserver != null) {
            mAudioItemObserver.onStartPlaying(this);
        }

        updateSpeed();

        if (mExoPlayer != null && mExoPlayer.getPlaybackState() == Player.STATE_READY) {
            mExoPlayer.setPlaybackSpeed(getBaseItemActivity().getTwinmeApplication().audioItemPlaybackSpeed());
            mExoPlayer.play();
            startCountdown();
            mPlayButton.setVisibility(View.INVISIBLE);
            mStopButton.setVisibility(View.VISIBLE);
            mSpeedView.setVisibility(View.VISIBLE);
            return;
        }

        mExoPlayer = new ExoPlayer.Builder(getBaseItemActivity()).build();
        mExoPlayer.setPlaybackSpeed(getBaseItemActivity().getTwinmeApplication().audioItemPlaybackSpeed());
        mExoPlayer.setMediaItem(MediaItem.fromUri(getPath(getPeerAudioItem().getAudioDescriptor())));
        mExoPlayer.addListener(new Player.Listener() {
            /**
             * Used to ignore additional calls to onPlaybackStateChanged(Player.STATE_READY),
             * e.g. when playback is paused/resumed.
             */
            private boolean hasStartedPlayingForFirstTime = false;

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (Player.STATE_READY == playbackState && !hasStartedPlayingForFirstTime && mExoPlayer != null) {
                    hasStartedPlayingForFirstTime = true;

                    long position = getPlayerPosition();
                    if (position > 0) {
                        mExoPlayer.seekTo(position);
                    }

                    mExoPlayer.play();

                    startCountdown();

                    mPlayButton.setVisibility(View.INVISIBLE);
                    mStopButton.setVisibility(View.VISIBLE);
                    mSpeedView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Log.e(LOG_TAG, "onPlayerError, item=" + getPeerAudioItem(), error);

                if (mExoPlayer != null) {
                    mExoPlayer.release();
                    mExoPlayer = null;
                }
            }
        });

        mExoPlayer.prepare();
    }

    private void stopPlayer(@Nullable View v) {

        if (getBaseItemActivity().isSelectItemMode()) {
            onContainerClick();
            return;
        }

        if (mCounterDown != null) {
            mCounterDown.cancel();
        }

        if (mExoPlayer != null) {
            mExoPlayer.pause();
            mPlayerPosition = mExoPlayer.getCurrentPosition();
        }
        mStopButton.setVisibility(View.INVISIBLE);
        mPlayButton.setVisibility(View.VISIBLE);
    }

    private void startCountdown() {

        if (mExoPlayer == null) {
            return;
        }

        long counterStartValue = mExoPlayer.getDuration() - mExoPlayer.getCurrentPosition();
        if (counterStartValue <= 0) {
            return;
        }

        if (mCounterDown != null) {
            mCounterDown.cancel();
        }

        mCounterDown = new CountDownTimer((long) (counterStartValue / getBaseItemActivity().getTwinmeApplication().audioItemPlaybackSpeed()), 100) {
            private final Handler handler = new Handler(Looper.getMainLooper());

            @SuppressLint("DefaultLocale")
            public void onTick(long millisUntilFinished) {

                if (mExoPlayer != null) {
                    if (!mIsAudioTrackTouch) {
                        float progressTrack = (float) mExoPlayer.getCurrentPosition() / (float) mExoPlayer.getDuration();
                        float audioTrackWidth = DESIGN_AUDIO_TRACK_WIDTH * Design.WIDTH_RATIO;
                        float progressWidth = (int) (audioTrackWidth * progressTrack);

                        handler.post(() -> setProgressTrack((int) progressWidth));
                    }

                    int remaining = (int) (mExoPlayer.getDuration() - mExoPlayer.getCurrentPosition()) / 1000;
                    String format = "mm:ss";
                    int hour = 60 * 60;
                    if (remaining > hour) {
                        format = "hh:mm:ss";
                    }
                    mCounter.setText(Utils.formatInterval(remaining, format));

                    if (remaining <= 0) {
                        onFinish();
                        cancel();
                    }
                }
            }

            public void onFinish() {

                mPlayerPosition = 0;
                mCounter.setText(mDuration);
                mStopButton.setVisibility(View.INVISIBLE);
                mPlayButton.setVisibility(View.VISIBLE);
                mSpeedView.setVisibility(View.GONE);

                float audioTrackWidth = DESIGN_AUDIO_TRACK_WIDTH * Design.WIDTH_RATIO;

                handler.post(() -> setProgressTrack((int) audioTrackWidth));
                handler.postDelayed(() -> setProgressTrack(0), 500);
            }
        };

        mCounterDown.start();
    }


    private void setProgressTrack(int progressTrack) {

        mAudioTrackView.setProgress(progressTrack);
        mAudioTrackView.invalidate();
    }

    @Nullable
    private PeerAudioItem getPeerAudioItem() {

        Item item = getItem();
        if (item instanceof PeerAudioItem) {

            return (PeerAudioItem) item;
        }

        return null;
    }

    private long getPlayerPosition() {
        PeerAudioItem item = getPeerAudioItem();
        if (mPlayerPositionPercent >= 0 && (mExoPlayer != null || item != null)) {
            long duration = mExoPlayer != null ? mExoPlayer.getDuration() : item.getAudioDescriptor().getDuration() * 1000;

            mPlayerPosition = (long) (duration * mPlayerPositionPercent);
            mPlayerPositionPercent = -1;
        }

        return mPlayerPosition;
    }

    private void onSpeedViewClick() {

        getBaseItemActivity().getTwinmeApplication().updateAudioItemPlaybackSpeed();
        if (mExoPlayer != null) {
            mExoPlayer.setPlaybackSpeed(getBaseItemActivity().getTwinmeApplication().audioItemPlaybackSpeed());
        }
        updateSpeed();
        startCountdown();
    }

    @SuppressLint("DefaultLocale")
    private void updateSpeed() {

        if (getBaseItemActivity().getTwinmeApplication().audioItemPlaybackSpeed() % 1 != 0) {
            mSpeedTextView.setText(String.format("%.1fx", getBaseItemActivity().getTwinmeApplication().audioItemPlaybackSpeed()));
        } else {
            mSpeedTextView.setText(String.format("%.0fx", getBaseItemActivity().getTwinmeApplication().audioItemPlaybackSpeed()));
        }
    }

    private int getAudioTrackNbLines() {

        int lineSpace = (int) (AudioTrackView.AUDIO_TRACK_LINE_SPACE * itemView.getContext().getResources().getDisplayMetrics().density);
        return (int) (DESIGN_AUDIO_TRACK_WIDTH * Design.WIDTH_RATIO) / lineSpace;
    }

    private void startEphemeralAnimation() {

        if (mTimer == null && getItem().getState() == Item.ItemState.READ) {
            mTimer = new CountDownTimer(getItem().getExpireTimeout(), 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Date now = new Date();
                    float timeSinceRead = (now.getTime() - getItem().getReadTimestamp());
                    float percent = (float) (1.0 - (timeSinceRead / getItem().getExpireTimeout()));
                    if (percent < 0) {
                        percent = 0;
                    } else if (percent > 1) {
                        percent = 1;
                    }
                    mEphemeralView.updateWithProgress(percent);
                }

                @Override
                public void onFinish() {

                }
            };
            mTimer.start();
        } else {
            mEphemeralView.updateWithProgress(1);
        }
    }
}
