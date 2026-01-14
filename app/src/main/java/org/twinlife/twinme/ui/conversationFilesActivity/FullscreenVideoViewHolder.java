/*
 *  Copyright (c) 2023-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.conversationFilesActivity;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.ui.baseItemActivity.PeerVideoItem;
import org.twinlife.twinme.ui.baseItemActivity.VideoItem;
import org.twinlife.twinme.utils.Utils;

import java.io.File;

public class FullscreenVideoViewHolder extends RecyclerView.ViewHolder implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private static final String LOG_TAG = "FullscreenVideoV...";
    private static final boolean DEBUG = false;

    private static final int PROGRESS_BAR_COLOR = Color.rgb(111, 111, 111);
    private static final float DESIGN_CONTROL_HEIGHT = 240f;
    private static final float DESIGN_PROGRESS_SIDE_MARGIN = 80f;
    private static final float DESIGN_PROGRESS_VIEW_HEIGHT = 60f;
    private static final float DESIGN_PLAY_PAUSE_VIEW_HEIGHT = 80f;
    private static final float DESIGN_PLAY_PAUSE_VIEW_TOP_MARGIN = -16f;

    private final TextView mMessageView;
    private final SurfaceView mSurfaceView;
    private final View mControlView;
    private final SeekBar mSeekBar;
    private final TextView mVideoTimerTextView;
    private final TextView mVideoDurationTextView;
    private final ImageView mPlayVideoImageView;

    private boolean mVideoPlaying = false;
    private boolean mPlayerReady = false;

    @Nullable
    private MediaPlayer mMediaPlayer;
    private double mStartTime = 0;
    private double mTotalTime = 0;

    private final Handler mHandler = new Handler();

    private final Runnable mUpdateSongTime = new Runnable() {

        @SuppressLint("DefaultLocale")
        public void run() {
            if (DEBUG) {
                Log.d(LOG_TAG, "UpdateSongTime");
            }

            // Don't run the timer again if we are in background, we have finished or there is no player.
            if (mMediaPlayer != null && mVideoPlaying) {
                mStartTime = mMediaPlayer.getCurrentPosition();
                int duration = (int) (mStartTime / 1000);
                mVideoTimerTextView.setText(Utils.formatInterval(duration, "mm:ss"));

                float progressBarWidth = Design.DISPLAY_WIDTH - (DESIGN_PROGRESS_SIDE_MARGIN * Design.WIDTH_RATIO * 2f);
                long interval = (long) (0.5f * duration / progressBarWidth);
                mHandler.postDelayed(this, interval);
                mSeekBar.setProgress((int) duration);
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    FullscreenVideoViewHolder(@NonNull View view, FullscreenMediaActivity fullscreenMediaActivity) {

        super(view);

        view.setBackgroundColor(Color.BLACK);

        mSurfaceView = view.findViewById(R.id.fullscreen_media_activity_video_item_surface_view);
        mSurfaceView.setClickable(true);
        mSurfaceView.setOnClickListener(v -> {
            onVideoClick();
            fullscreenMediaActivity.onMediaClick();
        });

        mMessageView = view.findViewById(R.id.fullscreen_media_activity_video_item_message_view);
        Design.updateTextFont(mMessageView, Design.FONT_REGULAR34);
        mMessageView.setTextColor(Color.WHITE);
        mMessageView.setVisibility(View.GONE);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mMessageView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_PROGRESS_SIDE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_PROGRESS_SIDE_MARGIN * Design.WIDTH_RATIO);

        mControlView = view.findViewById(R.id.fullscreen_media_activity_video_item_control_view);
        mControlView.setBackgroundColor(Color.BLACK);

        ViewGroup.LayoutParams layoutParams = mControlView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CONTROL_HEIGHT * Design.HEIGHT_RATIO);

        mVideoDurationTextView = view.findViewById(R.id.fullscreen_media_activity_video_item_control_duration_view);
        Design.updateTextFont(mVideoDurationTextView, Design.FONT_REGULAR28);
        mVideoDurationTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mVideoDurationTextView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_PROGRESS_SIDE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginEnd((int) (DESIGN_PROGRESS_SIDE_MARGIN * Design.WIDTH_RATIO));

        mVideoTimerTextView = view.findViewById(R.id.fullscreen_media_activity_video_item_control_counter_view);
        Design.updateTextFont(mVideoTimerTextView, Design.FONT_REGULAR28);
        mVideoTimerTextView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mVideoTimerTextView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_PROGRESS_SIDE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_PROGRESS_SIDE_MARGIN * Design.WIDTH_RATIO));

        mSeekBar = view.findViewById(R.id.fullscreen_media_activity_video_item_seek_bar);
        mSeekBar.setThumbOffset(0);
        mSeekBar.setThumb(ResourcesCompat.getDrawable(fullscreenMediaActivity.getResources(), R.drawable.seekbar_thumb, null));
        mSeekBar.setProgressTintList(ColorStateList.valueOf(Color.WHITE));
        mSeekBar.setProgressBackgroundTintList(ColorStateList.valueOf(PROGRESS_BAR_COLOR));

        layoutParams = mSeekBar.getLayoutParams();
        mSeekBar.setPadding(0, 0, 0, 0);
        layoutParams.height = (int) (DESIGN_PROGRESS_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mSeekBar.setMin(0);
        }

        mSeekBar.setOnTouchListener((v, motionEvent) -> {
            fullscreenMediaActivity.onVideoSeekBarUpdate(true);
            return false;
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean touch) {

                if (touch) {
                    if (mMediaPlayer != null) {
                        mMediaPlayer.seekTo(progress * 1000);
                        mStartTime = mMediaPlayer.getCurrentPosition();
                        int duration = (int) (mStartTime / 1000);
                        mVideoTimerTextView.setText(Utils.formatInterval(duration, "mm:ss"));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                fullscreenMediaActivity.onVideoSeekBarUpdate(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                fullscreenMediaActivity.onVideoSeekBarUpdate(false);
            }
        });

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSeekBar.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_PROGRESS_SIDE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_PROGRESS_SIDE_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_PROGRESS_SIDE_MARGIN * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_PROGRESS_SIDE_MARGIN * Design.WIDTH_RATIO));

        View playPauseView = view.findViewById(R.id.fullscreen_media_activity_video_item_control_play_pause_view);
        playPauseView.setBackgroundColor(Color.TRANSPARENT);
        playPauseView.setOnClickListener(v -> onPlayPauseClick());

        layoutParams = playPauseView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_PLAY_PAUSE_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) playPauseView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_PLAY_PAUSE_VIEW_TOP_MARGIN * Design.HEIGHT_RATIO);

        mPlayVideoImageView = view.findViewById(R.id.fullscreen_media_activity_video_item_control_play_pause_image_view);
    }

    public void stopPlayer() {
        if (DEBUG) {
            Log.d(LOG_TAG, "stopPlayer");
        }

        mVideoPlaying = false;
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            } catch (Exception ignored) {

            }
            mMediaPlayer = null;
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public void mutePlayer(boolean mute) {
        if (DEBUG) {
            Log.d(LOG_TAG, "mutePlayer");
        }

        if (mMediaPlayer != null) {
            try {
                float volume = mute ? 0.0f : 1.0f;
                mMediaPlayer.setVolume(volume, volume);
            } catch (Exception ignored) {

            }
        }
    }

    public void pausePlayer() {
        if (DEBUG) {
            Log.d(LOG_TAG, "pausePlayer");
        }

        if (mMediaPlayer != null && mPlayerReady) {
            try {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mPlayVideoImageView.setImageResource(R.drawable.audio_item_player_play);
                    mVideoPlaying = false;
                }
            } catch (Exception ignored) {

            }
        }
    }

    public void onBind(Item item, FullscreenMediaActivity fullscreenMediaActivity, boolean visible, boolean isMuted) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBind");
        }

        if (!visible) {
            mVideoPlaying = false;
            if (mMediaPlayer != null) {
                try {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                } catch (Exception ignored) {

                }
                mMediaPlayer = null;
                mHandler.removeCallbacksAndMessages(null);
            }
            return;
        }

        File filesDir = fullscreenMediaActivity.getTwinmeContext().getFilesDir();

        ConversationService.VideoDescriptor videoDescriptor;
        if (item.isPeerItem()) {
            final PeerVideoItem peerVideoItem = (PeerVideoItem) item;
            videoDescriptor = peerVideoItem.getVideoDescriptor();
        } else {
            final VideoItem videoItem = (VideoItem) item;
            videoDescriptor = videoItem.getVideoDescriptor();
        }

        mMessageView.setVisibility(View.GONE);

        Uri uriVideo = Uri.fromFile(new File(filesDir, videoDescriptor.getPath()));

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            try {
                mMediaPlayer.setDataSource(fullscreenMediaActivity, uriVideo);
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.setOnCompletionListener(this);
                float volume = isMuted ? 0.0f : 1.0f;
                mMediaPlayer.setVolume(volume, volume);
                mMediaPlayer.prepareAsync();
            } catch (Exception exception) {
                // TBD add user message
                exception.printStackTrace();
                mMediaPlayer.release();
                mMediaPlayer = null;
                mMessageView.setVisibility(View.VISIBLE);
            }

            SurfaceHolder holder = mSurfaceView.getHolder();
            holder.addCallback(new SurfaceHolder.Callback() {

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                    if (DEBUG) {
                        Log.d(LOG_TAG, "surfaceChanged");
                    }

                    updateSurface(fullscreenMediaActivity, videoDescriptor.getWidth(), videoDescriptor.getHeight(), holder);
                }

                @Override
                public void surfaceCreated(@NonNull SurfaceHolder holder) {
                    if (DEBUG) {
                        Log.d(LOG_TAG, "surfaceCreated");
                    }

                    updateSurface(fullscreenMediaActivity, videoDescriptor.getWidth(), videoDescriptor.getHeight(), holder);
                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                }
            });
        }
    }

    @Override
    public void onPrepared(@NonNull MediaPlayer player) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPrepared");
        }

        mPlayerReady = true;

        mMessageView.setVisibility(View.GONE);

        if (mMediaPlayer == null) {
            return;
        }
        try {
            player.start();

            mTotalTime = mMediaPlayer.getDuration();
            mStartTime = mMediaPlayer.getCurrentPosition();

            int totalDuration = (int) (mTotalTime / 1000);
            mMediaPlayer.seekTo((int) mStartTime);
            mVideoDurationTextView.setText(Utils.formatInterval(totalDuration, "mm:ss"));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mSeekBar.setMin(0);
            }

            mSeekBar.setMax(totalDuration);

            mVideoPlaying = true;
            mHandler.postDelayed(mUpdateSongTime, 100);
        } catch (Exception ignored) {

        }
    }

    @Override
    public void onCompletion(@NonNull MediaPlayer player) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCompletion");
        }

        mStartTime = 0;
        mVideoPlaying = false;
        mHandler.removeCallbacksAndMessages(null);
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.seekTo(0);
            } catch (Exception ignored) {

            }
        }
        mVideoTimerTextView.setText(Utils.formatInterval(0, "mm:ss"));
        mSeekBar.setProgress(0);
        mPlayVideoImageView.setImageResource(R.drawable.audio_item_player_play);
    }

    @Override
    public boolean onError(@NonNull MediaPlayer player, int what, int extra) {

        mMessageView.setVisibility(View.VISIBLE);

        return true;
    }

    public void onViewRecycled() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onViewRecycled");
        }

        stopPlayer();
    }

    private void onPlayPauseClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPlayPauseClick");
        }

        if (mMediaPlayer != null && mPlayerReady) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mPlayVideoImageView.setImageResource(R.drawable.audio_item_player_play);
                mVideoPlaying = false;
            } else {
                mMediaPlayer.start();
                mPlayVideoImageView.setImageResource(R.drawable.audio_item_player_pause);
                mTotalTime = mMediaPlayer.getDuration();
                mStartTime = mMediaPlayer.getCurrentPosition();
                int totalDuration = (int) (mTotalTime / 1000);
                mVideoDurationTextView.setText(Utils.formatInterval(totalDuration, "mm:ss"));

                mMediaPlayer.seekTo((int) mStartTime);

                mVideoPlaying = true;

                int duration = (int) (mStartTime / 1000);
                mVideoTimerTextView.setText(Utils.formatInterval(duration, "mm:ss"));

                float progressBarWidth = Design.DISPLAY_WIDTH - (DESIGN_PROGRESS_SIDE_MARGIN * Design.WIDTH_RATIO * 2f);
                long interval = (long) (0.5f * duration / progressBarWidth);
                mHandler.postDelayed(mUpdateSongTime, interval);
            }
        }
    }

    private void onVideoClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onVideoClick");
        }

        if (mControlView.getVisibility() == View.VISIBLE) {
            mControlView.setVisibility(View.INVISIBLE);
        } else {
            mControlView.setVisibility(View.VISIBLE);
        }
    }

    private void updateSurface(FullscreenMediaActivity fullscreenMediaActivity, int videoWidth, int videoHeight, SurfaceHolder holder) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSurface");
        }

        if (mMediaPlayer != null) {
            Point size = new Point();
            fullscreenMediaActivity.getWindowManager().getDefaultDisplay().getSize(size);
            int screenWidth = size.x;
            int screenHeight = size.y;

            android.view.ViewGroup.LayoutParams layoutParams = mSurfaceView.getLayoutParams();
            if (screenWidth > screenHeight) {
                layoutParams.width = (int) (((float) videoWidth / (float) videoHeight) * (float) screenHeight);
                layoutParams.height = screenHeight;
            } else {
                layoutParams.width = screenWidth;
                layoutParams.height = (int) (((float) videoHeight / (float) videoWidth) * (float) screenWidth);
            }

            mSurfaceView.setLayoutParams(layoutParams);
            try {
                mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                mMediaPlayer.setDisplay(holder);
                holder.setFixedSize(layoutParams.width, layoutParams.height);
            } catch (Exception ignored) {

            }
        }
    }
}