/*
 *  Copyright (c) 2016-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Houssem Temanni (Houssem.Temanni@twinlife-systems.com)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Yannis Le Gal (Yannis.LeGal@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.baseItemActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

public class AudioItemViewHolder extends ItemViewHolder {
    private static final String LOG_TAG = "AudioItemViewHolder";
    private static final boolean DEBUG = false;

    private static final float DESIGN_AUDIO_ITEM_CONTAINER_WIDTH_PERCENT = 0.7733f;
    private static final float DESIGN_AUDIO_TRACK_WIDTH_PERCENT = 0.7172f;
    private static final float DESIGN_EPHEMERAL_HEIGHT = 28f;
    private static final float DESIGN_EPHEMERAL_LEFT_MARGIN = 4f;
    private static final float DESIGN_EPHEMERAL_TOP_MARGIN = 4f;
    private static final float DESIGN_EPHEMERAL_BOTTOM_MARGIN = 4f;

    private final BaseItemActivity.AudioItemObserver mAudioItemObserver;

    private final View mAudioItemContainer;
    private final GradientDrawable mGradientDrawable;
    private final View mPlayButton;
    private final View mStopButton;
    private final TextView mCounter;
    private final View mReplyView;
    private final TextView mReplyTextView;
    private final View mReplyToImageContentView;
    private final RoundedImageView mReplyImageView;
    private final GradientDrawable mReplyGradientDrawable;
    private final GradientDrawable mReplyToImageContentGradientDrawable;
    private final DeleteProgressView mDeleteView;
    private final AudioTrackView mAudioTrackView;
    private final EphemeralView mEphemeralView;

    private CountDownTimer mTimer;

    @Nullable
    ExoPlayer mExoPlayer = null;
    @Nullable
    private CountDownTimer mCounterDown = null;
    private String mDuration;
    private long mPlayerPosition;
    /**
     * If mPlayerPositionPercent >= 0, the next call to getPlayerPosition() will compute and update
     * mPlayerPosition according to mPlayerPositionPercent.
     * <p>
     * This is needed in case the user seeks before starting playback : until the player is initialized,
     * we only know the track's duration in seconds so we can't seek precisely.
     */
    private float mPlayerPositionPercent = -1;
    private AudioTrackLoader<Item> mAudioLoader;

    private boolean mIsAudioTrackTouch = false;

    @SuppressLint({"NewApi", "ClickableViewAccessibility"})
    AudioItemViewHolder(BaseItemActivity baseItemActivity, View view, BaseItemActivity.AudioItemObserver audioItemObserver) {

        super(baseItemActivity, view,
                R.id.base_item_activity_audio_item_container,
                R.id.base_item_activity_audio_item_state_view,
                R.id.base_item_activity_audio_item_state_avatar_view,
                R.id.base_item_activity_audio_item_overlay_view,
                R.id.base_item_activity_audio_item_annotation_view,
                R.id.base_item_activity_audio_item_selected_view,
                R.id.base_item_activity_audio_item_selected_image_view);

        mAudioItemObserver = audioItemObserver;

        mAudioItemContainer = view.findViewById(R.id.base_item_activity_audio_item_view);

        mAudioItemContainer.setOnClickListener(v -> {
            if (getBaseItemActivity().isSelectItemMode()) {
                onContainerClick();
            }
        });

        mGradientDrawable = new GradientDrawable();
        mGradientDrawable.mutate();
        mGradientDrawable.setColor(Design.getMainStyle());
        mGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        mAudioItemContainer.setBackground(mGradientDrawable);

        RoundedView controlRoundedView = view.findViewById(R.id.base_item_activity_audio_control_rounded_view);
        controlRoundedView.setColor(Color.WHITE);

        mPlayButton = view.findViewById(R.id.base_item_activity_audio_item_play);

        ImageView playImageView = view.findViewById(R.id.base_item_activity_audio_item_play_image_view);
        playImageView.setColorFilter(Design.getMainStyle());

        mStopButton = view.findViewById(R.id.base_item_activity_audio_item_stop);

        ImageView stopImageView = view.findViewById(R.id.base_item_activity_audio_item_stop_image_view);
        stopImageView.setColorFilter(Design.getMainStyle());

        mCounter = view.findViewById(R.id.base_item_activity_audio_item_counter);
        Design.updateTextFont(mCounter, Design.FONT_MEDIUM26);
        mCounter.setTextColor(getBaseItemActivity().getCustomAppearance().getMessageTextColor());

        mAudioTrackView = view.findViewById(R.id.base_item_activity_audio_item_track_view);

        mAudioTrackView.setOnTouchListener((v, motionEvent) -> {

            float touchX = motionEvent.getRawX() - (mAudioItemContainer.getX() + mAudioTrackView.getX());

            if (touchX < 0) {
                touchX = 0;
            } else if (touchX > mAudioTrackView.getWidth()) {
                touchX = mAudioTrackView.getWidth();
            }

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mIsAudioTrackTouch = true;
                    getAudioItem().setCanReply(false);
                    break;

                case MotionEvent.ACTION_MOVE:
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mIsAudioTrackTouch = false;
                    getAudioItem().setCanReply(true);
                    break;
            }

            setProgressTrack((int) touchX);

            if (!mIsAudioTrackTouch) {
                mPlayerPositionPercent = touchX / mAudioTrackView.getWidth();

                if (mExoPlayer != null) {
                    mExoPlayer.seekTo(getPlayerPosition());
                    startCountdown();
                }

                int remainingTime = (int) (getAudioItem().getAudioDescriptor().getDuration() - mPlayerPosition / 1000);
                String format = "mm:ss";
                int hour = 60 * 60;
                if (remainingTime > hour) {
                    format = "hh:mm:ss";
                }
                mCounter.setText(Utils.formatInterval(remainingTime, format));
            }

            return true;
        });

        mReplyTextView = view.findViewById(R.id.base_item_activity_audio_item_reply_text);
        mReplyTextView.setPadding(MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING, MESSAGE_ITEM_TEXT_WIDTH_PADDING, MESSAGE_ITEM_TEXT_DEFAULT_PADDING);
        mReplyTextView.setTypeface(getMessageFont().typeface);
        mReplyTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getMessageFont().size);
        mReplyTextView.setTextColor(Design.REPLY_FONT_COLOR);
        mReplyTextView.setMaxLines(3);
        mReplyTextView.setEllipsize(TextUtils.TruncateAt.END);

        mReplyView = view.findViewById(R.id.base_item_activity_audio_item_reply_view);

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

        mReplyImageView = view.findViewById(R.id.base_item_activity_audio_item_reply_image_view);
        ViewGroup.LayoutParams layoutParams = mReplyImageView.getLayoutParams();
        layoutParams.width = REPLY_IMAGE_ITEM_MAX_WIDTH;
        layoutParams.height = REPLY_IMAGE_ITEM_MAX_HEIGHT;

        View replyContainerImageView = view.findViewById(R.id.base_item_activity_audio_item_reply_container_image_view);
        replyContainerImageView.setPadding(REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN, REPLY_IMAGE_WIDTH_MARGIN, REPLY_IMAGE_HEIGHT_MARGIN);

        mReplyToImageContentView = view.findViewById(R.id.base_item_activity_audio_item_reply_image_content_view);

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

        mDeleteView = view.findViewById(R.id.base_item_activity_audio_item_delete_view);

        mPlayButton.setOnClickListener(this::startPlayer);

        mStopButton.setOnClickListener(this::stopPlayer);

        mAudioItemContainer.setOnLongClickListener(v -> {
            if (!mIsAudioTrackTouch) {
                baseItemActivity.onItemLongPress(getItem());
            }
            return true;
        });

        mEphemeralView = view.findViewById(R.id.base_item_activity_audio_item_ephemeral_view);
        mEphemeralView.setColor(getBaseItemActivity().getCustomAppearance().getMessageTextColor());
        layoutParams = mEphemeralView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_EPHEMERAL_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mEphemeralView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_EPHEMERAL_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_EPHEMERAL_LEFT_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_EPHEMERAL_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_EPHEMERAL_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
    }

    @Override
    void onBind(Item item) {

        if (!(item instanceof AudioItem)) {
            return;
        }
        super.onBind(item);

        final AudioItem audioItem = (AudioItem) item;

        // Compute the corner radii only once!
        final float[] cornerRadii = getCornerRadii();

        mGradientDrawable.setCornerRadii(cornerRadii);
        mGradientDrawable.setColor(getBaseItemActivity().getCustomAppearance().getMessageBackgroundColor());
        if (getBaseItemActivity().getCustomAppearance().getMessageBorderColor() != Color.TRANSPARENT) {
            mGradientDrawable.setStroke(Design.BORDER_WIDTH, getBaseItemActivity().getCustomAppearance().getMessageBorderColor());
        }

        if (mAudioLoader == null) {
            mAudioLoader = new AudioTrackLoader<>(item, audioItem.getAudioDescriptor(), getAudioTrackNbLines());
            addLoader(mAudioLoader);
        }

        // Use an async loader to get the audio track data.
        AudioTrack audioTrack = mAudioLoader.getAudioTrack();
        if (audioTrack != null) {
            mAudioTrackView.initTrack(audioTrack, Design.AUDIO_TRACK_COLOR, Color.WHITE);
        }

        int duration = (int) audioItem.getAudioDescriptor().getDuration();
        String format = "mm:ss";
        int hour = 60 * 60;
        if (duration > hour) {
            format = "hh:mm:ss";
        }

        mDuration = Utils.formatInterval(duration, format);
        mCounter.setText(mDuration);

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
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_audio_item_reply_text);

                    ObjectDescriptor objectDescriptor = (ObjectDescriptor) replyToDescriptor;
                    mReplyTextView.setText(objectDescriptor.getMessage());
                    break;

                case IMAGE_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_audio_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (ImageDescriptor) replyToDescriptor);
                    break;

                case VIDEO_DESCRIPTOR:
                    mReplyToImageContentView.setVisibility(View.VISIBLE);
                    mReplyImageView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_audio_item_reply_container_image_view);

                    setReplyImage(mReplyImageView, (VideoDescriptor) replyToDescriptor);
                    break;

                case AUDIO_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_audio_item_reply_text);

                    mReplyTextView.setText(getString(R.string.conversation_activity_audio_message));
                    break;

                case GEOLOCATION_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_audio_item_reply_text);

                    mReplyTextView.setText(getBaseItemActivity().getResources().getString(R.string.application_location));
                    break;

                case NAMED_FILE_DESCRIPTOR:
                    mReplyView.setVisibility(View.VISIBLE);
                    mReplyTextView.setVisibility(View.VISIBLE);
                    relativeLayoutParams.addRule(RelativeLayout.BELOW, R.id.base_item_activity_audio_item_reply_text);

                    NamedFileDescriptor fileDescriptor = (NamedFileDescriptor) replyToDescriptor;
                    mReplyTextView.setText(fileDescriptor.getName());
                    break;
            }
        }

        if (item.isEphemeralItem()) {
            mEphemeralView.setVisibility(View.VISIBLE);
            startEphemeralAnimation();
        } else {
            mEphemeralView.setVisibility(View.GONE);
        }

        ViewGroup.LayoutParams overlayLayoutParams = getOverlayView().getLayoutParams();
        overlayLayoutParams.width = getContainer().getWidth();

        if (getBaseItemActivity().isMenuOpen()) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getContainer().getLayoutParams();
            overlayLayoutParams.height = getContainer().getHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
            getOverlayView().setVisibility(View.VISIBLE);
            if (getBaseItemActivity().isSelectedItem(getItem().getDescriptorId())) {
                itemView.setBackgroundColor(Design.BACKGROUND_COLOR_WHITE_OPACITY85);
                getOverlayView().setVisibility(View.INVISIBLE);
            }
        } else {
            overlayLayoutParams.height = OVERLAY_DEFAULT_HEIGHT;
            getOverlayView().setVisibility(View.INVISIBLE);
            itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        getOverlayView().setLayoutParams(overlayLayoutParams);
    }

    @Override
    void onViewRecycled() {

        super.onViewRecycled();

        mAudioTrackView.initTrack(null, Design.AUDIO_TRACK_COLOR, Color.WHITE);
        mReplyImageView.setImageBitmap(null, null);
        mDeleteView.setVisibility(View.GONE);
        mDeleteView.setOnDeleteProgressListener(null);
        setDeleteAnimationStarted(false);
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public void resetView() {

        mPlayerPosition = 0;
        mCounter.setText(mDuration);
        mStopButton.setVisibility(View.INVISIBLE);
        mPlayButton.setVisibility(View.VISIBLE);

        if (mExoPlayer != null) {
            if (mCounterDown != null) {
                mCounterDown.cancel();
            }
            mExoPlayer.stop();
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
    void startDeletedAnimation() {

        if (isDeleteAnimationStarted()) {
            return;
        }

        setDeleteAnimationStarted(true);
        mDeleteView.setVisibility(View.VISIBLE);

        ViewGroup.MarginLayoutParams deleteLayoutParams = (ViewGroup.MarginLayoutParams) mDeleteView.getLayoutParams();
        deleteLayoutParams.width = mAudioItemContainer.getWidth();
        deleteLayoutParams.height = mAudioItemContainer.getHeight();
        mDeleteView.setLayoutParams(deleteLayoutParams);
        mDeleteView.setCornerRadii(getCornerRadii());
        mDeleteView.setOnDeleteProgressListener(() -> deleteItem(getItem()));

        float progress = 0;
        int animationDuration = DESIGN_DELETE_ANIMATION_DURATION;
        if (getItem().getDeleteProgress() > 0) {
            progress = getItem().getDeleteProgress() / 100.0f;
            animationDuration = (int) (BaseItemViewHolder.DESIGN_DELETE_ANIMATION_DURATION - ((getItem().getDeleteProgress() * BaseItemViewHolder.DESIGN_DELETE_ANIMATION_DURATION) / 100.0));
        }

        mDeleteView.startAnimation(animationDuration, progress);
    }

    //
    // Private methods
    //

    private void startPlayer(@Nullable View v) {
        if (AudioItemViewHolder.this.getBaseItemActivity().isSelectItemMode()) {
            AudioItemViewHolder.this.onContainerClick();
            return;
        }

        if (mAudioItemObserver != null) {
            mAudioItemObserver.onStartPlaying(AudioItemViewHolder.this);
        }

        if (mExoPlayer != null && mExoPlayer.getPlaybackState() == Player.STATE_READY) {
            mExoPlayer.play();
            startCountdown();
            mPlayButton.setVisibility(View.INVISIBLE);
            mStopButton.setVisibility(View.VISIBLE);
            return;
        }

        mExoPlayer = new ExoPlayer.Builder(getBaseItemActivity()).build();

        mExoPlayer.setMediaItem(MediaItem.fromUri(AudioItemViewHolder.this.getPath(AudioItemViewHolder.this.getAudioItem().getAudioDescriptor())));
        mExoPlayer.addListener(new Player.Listener() {
            /**
             * Used to ignore additional calls to onPlaybackStateChanged(Player.STATE_READY),
             * e.g. when playback is paused/resumed.
             */
            private boolean hasStartedPlayingForFirstTime = false;

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (Player.STATE_READY == playbackState && !hasStartedPlayingForFirstTime) {
                    hasStartedPlayingForFirstTime = true;

                    long position = getPlayerPosition();
                    if (position > 0) {
                        mExoPlayer.seekTo(position);
                    }

                    mExoPlayer.play();

                    startCountdown();

                    mPlayButton.setVisibility(View.INVISIBLE);
                    mStopButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Log.e(LOG_TAG, "onPlayerError, item=" + getAudioItem(), error);

                if (mExoPlayer != null) {
                    mExoPlayer.release();
                    mExoPlayer = null;
                }
            }
        });

        mExoPlayer.prepare();
    }

    private void stopPlayer(@Nullable View v) {

        if (AudioItemViewHolder.this.getBaseItemActivity().isSelectItemMode()) {
            AudioItemViewHolder.this.onContainerClick();
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

        final int tickNumber = getTickNumber(mExoPlayer.getDuration());
        mCounterDown = new CountDownTimer(counterStartValue, mExoPlayer.getDuration() / tickNumber) {
            private final Handler handler = new Handler(Looper.getMainLooper());

            @SuppressLint("DefaultLocale")
            public void onTick(long millisUntilFinished) {

                if (mExoPlayer != null) {

                    if (!mIsAudioTrackTouch) {
                        float progressTrack = (float) mExoPlayer.getCurrentPosition() / (float) mExoPlayer.getDuration();
                        float audioContainerWidth = Design.DISPLAY_WIDTH * DESIGN_AUDIO_ITEM_CONTAINER_WIDTH_PERCENT;
                        float audioTrackWidth = audioContainerWidth * DESIGN_AUDIO_TRACK_WIDTH_PERCENT;
                        float progressWidth = (int) (audioTrackWidth * progressTrack);

                        handler.post(() -> setProgressTrack((int) progressWidth));
                    }

                    int duration = (int) ((millisUntilFinished * (tickNumber - 1) / tickNumber) / 1000);
                    String format = "mm:ss";
                    int hour = 60 * 60;
                    if (duration > hour) {
                        format = "hh:mm:ss";
                    }
                    mCounter.setText(Utils.formatInterval(duration, format));
                }
            }

            public void onFinish() {

                mPlayerPosition = 0;
                mCounter.setText(mDuration);
                mStopButton.setVisibility(View.INVISIBLE);
                mPlayButton.setVisibility(View.VISIBLE);

                float audioContainerWidth = Design.DISPLAY_WIDTH * DESIGN_AUDIO_ITEM_CONTAINER_WIDTH_PERCENT;
                float audioTrackWidth = audioContainerWidth * DESIGN_AUDIO_TRACK_WIDTH_PERCENT;

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

    private AudioItem getAudioItem() {

        return (AudioItem) getItem();
    }

    private int getTickNumber(long duration) {

        // 15 s -> 64 ticks
        int tickNumber = Math.round(((float) (duration / 1000) * 64) / 15);
        if (tickNumber != 0) {

            return tickNumber;
        }

        return 3;
    }

    private long getPlayerPosition() {
        if (mPlayerPositionPercent >= 0) {
            long duration = mExoPlayer != null ? mExoPlayer.getDuration() : getAudioItem().getAudioDescriptor().getDuration() * 1000;

            mPlayerPosition = (long) (duration * mPlayerPositionPercent);
            mPlayerPositionPercent = -1;
        }

        return mPlayerPosition;
    }

    private int getAudioTrackNbLines() {

        float audioContainerWidth = Design.DISPLAY_WIDTH * DESIGN_AUDIO_ITEM_CONTAINER_WIDTH_PERCENT;
        float audioTrackWidth = audioContainerWidth * DESIGN_AUDIO_TRACK_WIDTH_PERCENT;
        return (int) (audioTrackWidth / 4);
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
