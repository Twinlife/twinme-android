/*
 *  Copyright (c) 2023 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.utils.MediaMetaData;

public class CallStreamingAudioView extends PercentRelativeLayout {
    private static final String LOG_TAG = "CallStreamingAudioView";
    private static final boolean DEBUG = false;

    public interface StreamingAudioListener {

        void onStreamingPlayPause();

        void onStreamingStop();
    }

    private static final int DESIGN_PLACEHOLDER_COLOR = Color.rgb(229, 229, 229);

    private static final int DESIGN_COVER_VIEW_RADIUS = 6;
    private static final int DESIGN_PLAYER_VIEW_COLOR = Color.rgb(60, 60, 60);
    private static final int DESIGN_PLAYER_VIEW_RADIUS = 14;

    private static final int DESIGN_COVER_MARGIN = 12;

    private static final float DESIGN_PLAYER_HEIGHT = 136f;
    private static final float DESIGN_ACTION_WIDTH = 100f;
    private static final float DESIGN_SIDE_MARGIN = 34f;
    private static final int PLAYER_HEIGHT;
    private static final int SIDE_MARGIN;
    private static final int COVER_MARGIN;

    static {
        PLAYER_HEIGHT = (int) (DESIGN_PLAYER_HEIGHT * Design.HEIGHT_RATIO);
        SIDE_MARGIN = (int) (DESIGN_SIDE_MARGIN * Design.WIDTH_RATIO);
        COVER_MARGIN = (int) (DESIGN_COVER_MARGIN * Design.WIDTH_RATIO);
    }

    private ImageView mCoverImageView;
    private ImageView mPlaceholderCoverImageView;
    private TextView mSoundTitleView;
    private ImageView mPlayImageView;

    private StreamingAudioListener mStreamingAudioListener;

    public CallStreamingAudioView(Context context) {

        super(context);
    }

    public CallStreamingAudioView(Context context, AttributeSet attrs) {

        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.call_activity_streaming_audio_view, this, true);
        initViews();
    }

    public CallStreamingAudioView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    public void setStreamingAudioListener(StreamingAudioListener streamingAudioListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setStreamingAudioListener: " + streamingAudioListener);
        }

        mStreamingAudioListener = streamingAudioListener;
    }

    public void setSound(MediaMetaData mediaMetaData) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setSound: mediaMetaData=" + mediaMetaData);
        }

        if (mediaMetaData != null) {
            if (mediaMetaData.artwork != null) {
                mCoverImageView.setImageBitmap(mediaMetaData.artwork);
                mPlaceholderCoverImageView.setVisibility(View.GONE);
            } else {
                mPlaceholderCoverImageView.setVisibility(View.VISIBLE);
            }

            if (mediaMetaData.title != null) {
                mSoundTitleView.setText(mediaMetaData.title);
            }
        } else {
            mPlaceholderCoverImageView.setVisibility(View.VISIBLE);
        }
    }

    public void resumeStreaming() {
        if (DEBUG) {
            Log.d(LOG_TAG, "resumeStreaming");
        }

        mPlayImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.audio_item_player_pause, null));
    }

    public void pauseStreaming() {
        if (DEBUG) {
            Log.d(LOG_TAG, "pauseStreaming");
        }

        mPlayImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.audio_item_player_play, null));
    }

    public void stopStreaming() {
        if (DEBUG) {
            Log.d(LOG_TAG, "stopStreaming");
        }

        mPlayImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.audio_item_player_pause, null));
        mPlaceholderCoverImageView.setVisibility(View.VISIBLE);
        mCoverImageView.setImageBitmap(null);
        mSoundTitleView.setText("");
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        View playerView = findViewById(R.id.call_activity_streaming_audio_player_view);

        ViewGroup.LayoutParams viewLayoutParams = playerView.getLayoutParams();
        viewLayoutParams.height = PLAYER_HEIGHT;
        viewLayoutParams.width = Design.DISPLAY_WIDTH - (SIDE_MARGIN * 2);

        float radius = DESIGN_PLAYER_VIEW_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable playerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        playerViewBackground.getPaint().setColor(DESIGN_PLAYER_VIEW_COLOR);
        playerView.setBackground(playerViewBackground);

        View coverContainerView = findViewById(R.id.call_activity_streaming_audio_container_view);
        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) coverContainerView.getLayoutParams();

        marginLayoutParams.leftMargin = COVER_MARGIN;
        marginLayoutParams.rightMargin = COVER_MARGIN;
        marginLayoutParams.setMarginStart(COVER_MARGIN);
        marginLayoutParams.setMarginEnd(COVER_MARGIN);

        viewLayoutParams = coverContainerView.getLayoutParams();
        viewLayoutParams.height = PLAYER_HEIGHT - (COVER_MARGIN * 2);

        radius = DESIGN_COVER_VIEW_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable coverViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        coverViewBackground.getPaint().setColor(DESIGN_PLACEHOLDER_COLOR);
        coverContainerView.setBackground(coverViewBackground);

        mCoverImageView = findViewById(R.id.call_activity_streaming_audio_cover_view);
        mCoverImageView.setClipToOutline(true);

        mPlaceholderCoverImageView = findViewById(R.id.call_activity_streaming_placeholder_cover_view);

        mSoundTitleView = findViewById(R.id.call_activity_streaming_audio_title_view);
        Design.updateTextFont(mSoundTitleView, Design.FONT_MEDIUM34);
        mSoundTitleView.setTextColor(Color.WHITE);

        marginLayoutParams = (MarginLayoutParams) mSoundTitleView.getLayoutParams();
        marginLayoutParams.rightMargin = (int) (DESIGN_ACTION_WIDTH * Design.WIDTH_RATIO * 2);
        marginLayoutParams.setMarginEnd((int) (DESIGN_ACTION_WIDTH * Design.WIDTH_RATIO * 2));

        View playView = findViewById(R.id.call_activity_streaming_audio_play_view);
        playView.setOnClickListener(view -> onPlayClick());

        viewLayoutParams = playView.getLayoutParams();
        viewLayoutParams.width = (int) (DESIGN_ACTION_WIDTH * Design.WIDTH_RATIO);

        mPlayImageView = findViewById(R.id.call_activity_streaming_audio_play_image_view);

        View stopView = findViewById(R.id.call_activity_streaming_audio_stop_view);
        stopView.setOnClickListener(view -> onStopClick());

        viewLayoutParams = stopView.getLayoutParams();
        viewLayoutParams.width = (int) (DESIGN_ACTION_WIDTH * Design.WIDTH_RATIO);
    }

    private void onPlayClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPlayClick");
        }

        mStreamingAudioListener.onStreamingPlayPause();
    }

    private void onStopClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStopClick");
        }

        mStreamingAudioListener.onStreamingStop();
    }
}