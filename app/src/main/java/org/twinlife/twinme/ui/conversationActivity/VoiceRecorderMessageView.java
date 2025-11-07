/*
 *  Copyright (c) 2021-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.percentlayout.widget.PercentRelativeLayout;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.AssertPoint;
import org.twinlife.twinme.audio.AudioRecorder;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.ApplicationAssertPoint;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.Utils;

import java.io.File;

@SuppressWarnings("deprecation")
public class VoiceRecorderMessageView extends PercentRelativeLayout implements AudioRecorder.AudioRecorderListener {
    private static final String LOG_TAG = "VoiceRecorder..";
    private static final boolean DEBUG = false;

    private static final float DESIGN_BUTTON_WIDTH_PERCENT = 0.1653f;
    private static final float DESIGN_SCROLL_VIEW_INITIAL_WIDTH_PERCENT = 0.7570f;
    private static final float DESIGN_SCROLL_VIEW_PLAY_WIDTH_PERCENT = 0.6042f;
    private static final float DESIGN_SCROLL_VIEW_INITIAL_LEFT_PERCENT = 0.0185f;
    private static final float DESIGN_SCROLL_VIEW_PLAY_LEFT_PERCENT = 0.185f;
    private static final float DESIGN_SEND_IMAGE_HEIGHT = 30f;
    private static final float DESIGN_PLAY_BUTTON_MARGIN = 6f;
    private static final float DESIGN_PAUSE_RECORD_BUTTON_BORDER = 2f;

    private static final int RECORD_VIEW_COLOR = Color.argb(255, 253, 96, 93);

    private View mAudioTrackContainerView;
    private View mSendButton;
    private View mRecordButton;
    private View mPauseRecordButton;
    private View mTrashButton;
    private View mPlayButton;
    private View mPauseButton;
    private TextView mRecordingDuration;
    private ScrollView mScrollTrackView;
    private View mContentScrollView;
    private RecordTrackView mTrackView;

    private ConversationActivity mConversationActivity;

    @Nullable
    private AudioRecorder mAudioRecorder;
    @Nullable
    private CountDownTimer mCounterDown = null;

    public VoiceRecorderMessageView(Context context) {
        super(context);
    }

    public VoiceRecorderMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DEBUG) {
            Log.d(LOG_TAG, "create");
        }

        mConversationActivity = (ConversationActivity) context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View view;
            view = inflater.inflate(R.layout.conversation_activity_voice_recorder_view, (ViewGroup) getParent());
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            addView(view);

            initViews();
        }
    }

    public VoiceRecorderMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void updateDuration(long duration, int amplitude) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateDuration : " + duration + " amplitude " + amplitude);
        }

        int second = (int) (duration / 1000);
        mRecordingDuration.setText(formatDuration(second));
        mTrackView.drawLine(amplitude);

        ViewGroup.LayoutParams layoutParams = mContentScrollView.getLayoutParams();
        layoutParams.width = mTrackView.getStartLine();

        int contentOffset = 0;
        if (mTrackView.getStartLine() > mScrollTrackView.getWidth()) {
            contentOffset = mTrackView.getStartLine() - mScrollTrackView.getWidth();
        }
        mScrollTrackView.scrollTo(contentOffset, 0);
    }

    public void stopPlayer() {
        if (DEBUG) {
            Log.d(LOG_TAG, "stopPlayer");
        }

        if (mAudioRecorder == null) {
            if (DEBUG) {
                Log.d(LOG_TAG, "Recording not started, skipping playback stop");
            }
            return;
        }

        mAudioRecorder.stopPlayback();
    }

    public void startRecording() {
        if (DEBUG) {
            Log.d(LOG_TAG, "startRecording");
        }

        mConversationActivity.hapticFeedback();

        if (mAudioRecorder == null || mAudioRecorder.getDuration() == 0) {
            startRecord();

            mPauseRecordButton.setVisibility(VISIBLE);
            mTrashButton.setVisibility(VISIBLE);
            mRecordButton.setVisibility(INVISIBLE);
            mSendButton.setAlpha(1f);
        }

        mConversationActivity.onStartRecording();
    }

    public void stopRecording() {
        if (DEBUG) {
            Log.d(LOG_TAG, "stopRecording");
        }

        mConversationActivity.hapticFeedback();
        stopRecord();

        mPauseRecordButton.setVisibility(INVISIBLE);
        mRecordButton.setVisibility(VISIBLE);
    }

    public boolean isSendButtonEnable() {
        if (DEBUG) {
            Log.d(LOG_TAG, "isSendButtonEnable");
        }

        return mSendButton.getAlpha() == 1.0;
    }

    public boolean isRecording() {
        return mAudioRecorder != null && mAudioRecorder.isRecording();
    }

    public void releaseRecorder() {
        if (DEBUG) {
            Log.d(LOG_TAG, "releaseRecorder");
        }

        if (mAudioRecorder != null) {
            mAudioRecorder.release();
            mAudioRecorder = null;
        }

        if (mCounterDown != null) {
            mCounterDown.cancel();
            mCounterDown = null;
        }
    }

    public void updateViews(int buttonSize) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews: " + buttonSize);
        }

        float leftMargin = (float) ((((Design.DISPLAY_WIDTH * DESIGN_BUTTON_WIDTH_PERCENT) - buttonSize) * 0.5) / Design.DISPLAY_WIDTH);

        @SuppressWarnings("deprecation")
        PercentRelativeLayout.LayoutParams percentRelativeLayoutParams = (PercentRelativeLayout.LayoutParams) mTrashButton.getLayoutParams();
        if (CommonUtils.isLayoutDirectionRTL()) {
            percentRelativeLayoutParams.getPercentLayoutInfo().rightMarginPercent = leftMargin;
            percentRelativeLayoutParams.getPercentLayoutInfo().endMarginPercent = leftMargin;
        } else {
            percentRelativeLayoutParams.getPercentLayoutInfo().leftMarginPercent = leftMargin;
            percentRelativeLayoutParams.getPercentLayoutInfo().startMarginPercent = leftMargin;
        }

        ViewGroup.LayoutParams layoutParams = mTrashButton.getLayoutParams();
        layoutParams.height = buttonSize;

        percentRelativeLayoutParams = (PercentRelativeLayout.LayoutParams) mRecordButton.getLayoutParams();
        if (CommonUtils.isLayoutDirectionRTL()) {
            percentRelativeLayoutParams.getPercentLayoutInfo().rightMarginPercent = leftMargin;
            percentRelativeLayoutParams.getPercentLayoutInfo().endMarginPercent = leftMargin;
        } else {
            percentRelativeLayoutParams.getPercentLayoutInfo().leftMarginPercent = leftMargin;
            percentRelativeLayoutParams.getPercentLayoutInfo().startMarginPercent = leftMargin;
        }

        layoutParams = mRecordButton.getLayoutParams();
        layoutParams.height = buttonSize;

        percentRelativeLayoutParams = (PercentRelativeLayout.LayoutParams) mPauseRecordButton.getLayoutParams();
        if (CommonUtils.isLayoutDirectionRTL()) {
            percentRelativeLayoutParams.getPercentLayoutInfo().rightMarginPercent = leftMargin;
            percentRelativeLayoutParams.getPercentLayoutInfo().endMarginPercent = leftMargin;
        } else {
            percentRelativeLayoutParams.getPercentLayoutInfo().leftMarginPercent = leftMargin;
            percentRelativeLayoutParams.getPercentLayoutInfo().startMarginPercent = leftMargin;
        }

        layoutParams = mPauseRecordButton.getLayoutParams();
        layoutParams.height = buttonSize;

        percentRelativeLayoutParams = (PercentRelativeLayout.LayoutParams) mAudioTrackContainerView.getLayoutParams();
        if (CommonUtils.isLayoutDirectionRTL()) {
            percentRelativeLayoutParams.getPercentLayoutInfo().rightMarginPercent = leftMargin;
            percentRelativeLayoutParams.getPercentLayoutInfo().endMarginPercent = leftMargin;
        } else {
            percentRelativeLayoutParams.getPercentLayoutInfo().leftMarginPercent = leftMargin;
            percentRelativeLayoutParams.getPercentLayoutInfo().startMarginPercent = leftMargin;
        }

        int widthContainer = (int) (Design.DISPLAY_WIDTH - ((Design.DISPLAY_WIDTH * DESIGN_BUTTON_WIDTH_PERCENT) + 2 * buttonSize + 3 * (Design.DISPLAY_WIDTH * leftMargin)));
        layoutParams = mAudioTrackContainerView.getLayoutParams();
        layoutParams.height = buttonSize;
        layoutParams.width = widthContainer;

        float radius = buttonSize * 0.5f;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable editTextBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        editTextBackground.getPaint().setColor(Design.getMainStyle());
        mAudioTrackContainerView.setBackground(editTextBackground);

        layoutParams = mSendButton.getLayoutParams();
        layoutParams.height = buttonSize;
        layoutParams.width = buttonSize;

        View sendView = findViewById(R.id.conversation_activity_voice_recorder_send_view);
        layoutParams = sendView.getLayoutParams();
        layoutParams.height = buttonSize;
        layoutParams.width = buttonSize;

        View sendRoundedView = findViewById(R.id.conversation_activity_voice_recorder_send_rounded_view);
        layoutParams = sendRoundedView.getLayoutParams();
        layoutParams.height = buttonSize;
        layoutParams.width = buttonSize;

        ShapeDrawable sendBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        sendBackground.getPaint().setColor(Design.getMainStyle());
        sendRoundedView.setBackground(sendBackground);
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setBackgroundColor(Design.WHITE_COLOR);

        mAudioTrackContainerView = findViewById(R.id.conversation_activity_voice_recorder_audio_container);
        mAudioTrackContainerView.setClickable(true);

        mScrollTrackView = findViewById(R.id.conversation_activity_voice_recorder_scroll_view);
        mContentScrollView = findViewById(R.id.conversation_activity_voice_recorder_scroll_content_view);

        mTrackView = findViewById(R.id.conversation_activity_voice_recorder_audio_track_view);

        mRecordButton = findViewById(R.id.conversation_activity_voice_recorder_record_view);
        mRecordButton.setOnClickListener(view -> resumeRecording());

        RoundedView recordRoundedView = findViewById(R.id.conversation_activity_voice_recorder_record_rounded_view);
        recordRoundedView.setColor(RECORD_VIEW_COLOR);

        RoundedView recordIconView = findViewById(R.id.conversation_activity_voice_recorder_record_icon_view);
        recordIconView.setColor(Color.WHITE);

        mPauseRecordButton = findViewById(R.id.conversation_activity_voice_recorder_pause_record_view);
        mPauseRecordButton.setOnClickListener(view -> stopRecording());

        RoundedView pauseRecordRoundedView = findViewById(R.id.conversation_activity_voice_recorder_pause_record_rounded_view);
        pauseRecordRoundedView.setBorder(DESIGN_PAUSE_RECORD_BUTTON_BORDER, Design.getMainStyle());
        pauseRecordRoundedView.setColor(Design.WHITE_COLOR);

        ImageView pauseRecordIconView = findViewById(R.id.conversation_activity_voice_recorder_pause_record_icon_view);
        pauseRecordIconView.setColorFilter(Design.getMainStyle());

        mTrashButton = findViewById(R.id.conversation_activity_voice_recorder_trash_view);
        mTrashButton.setOnClickListener(v -> onTrashButtonClick());
        mTrashButton.setVisibility(INVISIBLE);

        RoundedView trashRoundedView = findViewById(R.id.conversation_activity_voice_recorder_trash_rounded_view);
        trashRoundedView.setColor(Design.BLACK_COLOR);

        ImageView trashIconView = findViewById(R.id.conversation_activity_voice_recorder_trash_icon_view);
        trashIconView.setColorFilter(Design.WHITE_COLOR);

        mPlayButton = findViewById(R.id.conversation_activity_voice_recorder_play_view);
        mPlayButton.setVisibility(INVISIBLE);

        mPlayButton.setOnClickListener(v -> {

            if (mAudioRecorder == null) {
                if (DEBUG) {
                    Log.d(LOG_TAG, "Recording not started, ignoring playback");
                }
                return;
            }

            mConversationActivity.hapticFeedback();

            mAudioRecorder.startPlayback();

            long totalDuration = mAudioRecorder.getDuration();

            if (mCounterDown == null) {
                final int tickNumber = getTickNumber((int) totalDuration);
                mCounterDown = new CountDownTimer(totalDuration, mAudioRecorder.getDuration() / tickNumber) {

                    @SuppressLint("DefaultLocale")
                    public void onTick(long millisUntilFinished) {

                        if (mAudioRecorder != null) {
                            float progress = (float) (mAudioRecorder.getPlayPosition()) / (float) mAudioRecorder.getDuration();
                            int duration = (int) ((millisUntilFinished * (tickNumber - 1) / tickNumber) / 1000);
                            mRecordingDuration.setText(formatDuration(duration));

                            int contentOffset = 0;
                            float progressTrack = progress * mTrackView.getWidth();
                            if (progressTrack > mScrollTrackView.getWidth()) {
                                contentOffset = (int) (progressTrack - mScrollTrackView.getWidth());
                            }
                            mScrollTrackView.scrollTo(contentOffset, 0);
                        }
                    }

                    public void onFinish() {
                    }
                };

                mPlayButton.setVisibility(INVISIBLE);
                mPauseButton.setVisibility(VISIBLE);
                mCounterDown.start();
            }

            mPlayButton.setVisibility(INVISIBLE);
            mPauseButton.setVisibility(VISIBLE);
        });

        RoundedView playRoundedView = findViewById(R.id.conversation_activity_voice_recorder_play_rounded_view);
        playRoundedView.setColor(Color.WHITE);

        MarginLayoutParams marginLayoutParams = (MarginLayoutParams) playRoundedView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_PLAY_BUTTON_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_PLAY_BUTTON_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_PLAY_BUTTON_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_PLAY_BUTTON_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_PLAY_BUTTON_MARGIN * Design.HEIGHT_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_PLAY_BUTTON_MARGIN * Design.HEIGHT_RATIO));

        ImageView playIconView = findViewById(R.id.conversation_activity_voice_recorder_play_icon_view);
        playIconView.setColorFilter(Design.getMainStyle());

        mPauseButton = findViewById(R.id.conversation_activity_voice_recorder_pause_view);
        mPauseButton.setOnClickListener(v -> {

            if (mAudioRecorder == null) {
                if (DEBUG) {
                    Log.d(LOG_TAG, "Recording not started, ignoring pause");
                }
                return;
            }

            mConversationActivity.hapticFeedback();
            mAudioRecorder.pausePlayback();

            mPauseButton.setVisibility(INVISIBLE);
            mPlayButton.setVisibility(VISIBLE);
        });
        mPauseButton.setVisibility(INVISIBLE);

        RoundedView pauseRoundedView = findViewById(R.id.conversation_activity_voice_recorder_pause_rounded_view);
        pauseRoundedView.setColor(Color.WHITE);

        marginLayoutParams = (MarginLayoutParams) pauseRoundedView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_PLAY_BUTTON_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_PLAY_BUTTON_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_PLAY_BUTTON_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_PLAY_BUTTON_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_PLAY_BUTTON_MARGIN * Design.HEIGHT_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_PLAY_BUTTON_MARGIN * Design.HEIGHT_RATIO));

        ImageView pauseIconView = findViewById(R.id.conversation_activity_voice_recorder_pause_icon_view);
        pauseIconView.setColorFilter(Design.getMainStyle());

        mSendButton = findViewById(R.id.conversation_activity_voice_recorder_send_clickable_view);
        mSendButton.setOnClickListener(v -> onSendButtonClick());
        mSendButton.setAlpha(0.5f);

        mSendButton.setOnLongClickListener(view -> {
            mConversationActivity.onSendVoiceRecordLongPress();
            return true;
        });

        ImageView sendIconView = findViewById(R.id.conversation_activity_voice_recorder_send_image_view);
        sendIconView.setColorFilter(Color.WHITE);

        ViewGroup.LayoutParams layoutParams = sendIconView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SEND_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        mRecordingDuration = findViewById(R.id.conversation_activity_voice_recorder_timer_view);
        Design.updateTextFont(mRecordingDuration, Design.FONT_REGULAR28);
        mRecordingDuration.setTextColor(Color.WHITE);
        mRecordingDuration.setText(formatDuration(0));
    }

    private void onSendButtonClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendButtonClick");
        }

        if (mSendButton.getAlpha() != 1.0 || mAudioRecorder == null) {
            return;
        }

        mConversationActivity.hapticFeedback();
        mAudioRecorder.getRecording();

        if (mCounterDown != null) {
            mCounterDown.cancel();
            mCounterDown = null;
        }

        mTrackView.resetTrack();
        mRecordButton.setVisibility(VISIBLE);
        mTrashButton.setVisibility(INVISIBLE);
        mPlayButton.setVisibility(INVISIBLE);
        mPauseButton.setVisibility(INVISIBLE);
        mSendButton.setAlpha(0.5f);
        mRecordingDuration.setText(formatDuration(0));

        @SuppressWarnings("deprecation")
        PercentRelativeLayout.LayoutParams percentRelativeLayoutParams = (PercentRelativeLayout.LayoutParams) mScrollTrackView.getLayoutParams();
        percentRelativeLayoutParams.getPercentLayoutInfo().widthPercent = DESIGN_SCROLL_VIEW_INITIAL_WIDTH_PERCENT;
        percentRelativeLayoutParams.getPercentLayoutInfo().leftMarginPercent = DESIGN_SCROLL_VIEW_INITIAL_LEFT_PERCENT;
        percentRelativeLayoutParams.getPercentLayoutInfo().startMarginPercent = DESIGN_SCROLL_VIEW_INITIAL_LEFT_PERCENT;
        mScrollTrackView.requestLayout();
    }

    private void startRecord() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRecordButtonClick");
        }

        ViewGroup.LayoutParams layoutParams = mTrackView.getLayoutParams();
        layoutParams.height = mScrollTrackView.getHeight();


        releaseRecorder();

        mAudioRecorder = new AudioRecorder(getContext(), this);
        mAudioRecorder.start();

        mConversationActivity.onStartRecording();
    }

    private void stopRecord() {
        if (DEBUG) {
            Log.d(LOG_TAG, "stopRecord");
        }

        if (mAudioRecorder != null) {
            mAudioRecorder.stop();
        }

        mScrollTrackView.scrollTo(0, 0);
        mTrashButton.setVisibility(VISIBLE);
        mPlayButton.setVisibility(VISIBLE);
        mSendButton.setAlpha(1.f);

        @SuppressWarnings("deprecation")
        PercentRelativeLayout.LayoutParams percentRelativeLayoutParams = (PercentRelativeLayout.LayoutParams) mScrollTrackView.getLayoutParams();
        percentRelativeLayoutParams.getPercentLayoutInfo().widthPercent = DESIGN_SCROLL_VIEW_PLAY_WIDTH_PERCENT;
        percentRelativeLayoutParams.getPercentLayoutInfo().leftMarginPercent = DESIGN_SCROLL_VIEW_PLAY_LEFT_PERCENT;
        percentRelativeLayoutParams.getPercentLayoutInfo().startMarginPercent = DESIGN_SCROLL_VIEW_PLAY_LEFT_PERCENT;
        mScrollTrackView.requestLayout();
    }

    private void onTrashButtonClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onTrashButtonClick");
        }

        mConversationActivity.hapticFeedback();
        stopPlayer();
        releaseRecorder();
        mConversationActivity.onDeleteRecording();

        if (mCounterDown != null) {
            mCounterDown.cancel();
            mCounterDown = null;
        }

        mTrackView.resetTrack();
        mRecordButton.setVisibility(VISIBLE);
        mTrashButton.setVisibility(INVISIBLE);
        mPlayButton.setVisibility(INVISIBLE);
        mPauseButton.setVisibility(INVISIBLE);
        mSendButton.setAlpha(0.5f);
        mRecordingDuration.setText(formatDuration(0));

        @SuppressWarnings("deprecation")
        PercentRelativeLayout.LayoutParams percentRelativeLayoutParams = (PercentRelativeLayout.LayoutParams) mScrollTrackView.getLayoutParams();
        percentRelativeLayoutParams.getPercentLayoutInfo().widthPercent = DESIGN_SCROLL_VIEW_INITIAL_WIDTH_PERCENT;
        percentRelativeLayoutParams.getPercentLayoutInfo().leftMarginPercent = DESIGN_SCROLL_VIEW_INITIAL_LEFT_PERCENT;
        percentRelativeLayoutParams.getPercentLayoutInfo().startMarginPercent = DESIGN_SCROLL_VIEW_INITIAL_LEFT_PERCENT;
        mScrollTrackView.requestLayout();
    }

    private void resumeRecording() {
        if (DEBUG) {
            Log.d(LOG_TAG, "resumeRecording");
        }

        mConversationActivity.hapticFeedback();
        stopPlayer();

        if (mCounterDown != null) {
            mCounterDown.cancel();
            mCounterDown = null;
        }

        mPlayButton.setVisibility(INVISIBLE);
        mPauseButton.setVisibility(INVISIBLE);
        mPauseRecordButton.setVisibility(VISIBLE);
        mRecordButton.setVisibility(INVISIBLE);

        @SuppressWarnings("deprecation")
        PercentRelativeLayout.LayoutParams percentRelativeLayoutParams = (PercentRelativeLayout.LayoutParams) mScrollTrackView.getLayoutParams();
        percentRelativeLayoutParams.getPercentLayoutInfo().widthPercent = DESIGN_SCROLL_VIEW_INITIAL_WIDTH_PERCENT;
        percentRelativeLayoutParams.getPercentLayoutInfo().leftMarginPercent = DESIGN_SCROLL_VIEW_INITIAL_LEFT_PERCENT;
        percentRelativeLayoutParams.getPercentLayoutInfo().startMarginPercent = DESIGN_SCROLL_VIEW_INITIAL_LEFT_PERCENT;
        mScrollTrackView.requestLayout();

        if (mAudioRecorder != null) {
            mAudioRecorder.start();
        }
        mConversationActivity.onStartRecording();
    }

    private int getTickNumber(int duration) {

        // 15 s -> 64 ticks
        int tickNumber = Math.round(((float) (duration / 1000) * 64) / 15);
        if (tickNumber != 0) {

            return tickNumber;
        }

        return 3;
    }

    private String formatDuration(long duration) {
        return Utils.formatInterval((int)duration, "mm:ss");
    }

    @Override
    public void onRecordingStarted() {

    }

    @Override
    public void onRecordingStopped() {

    }

    @Override
    public void onRecordingError(@NonNull AudioRecorder.ErrorCode errorCode, @Nullable String message, @Nullable Exception exception) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRecordingError errorCode=" + errorCode + " exception=" + exception);
        }

        String toastMessage = mConversationActivity.getString(R.string.conversation_activity_audio_message) + " " + mConversationActivity.getString(R.string.application_operation_failure);
        Toast.makeText(mConversationActivity, toastMessage, Toast.LENGTH_SHORT).show();

        mConversationActivity.getTwinmeContext().assertion(ApplicationAssertPoint.AUDIO_RECORD_ERROR, AssertPoint.create(errorCode));
        mConversationActivity.showProgressBar(false);

        releaseRecorder();
        mConversationActivity.onDeleteRecording();
    }

    @Override
    public void onTimerUpdated(long duration, int amplitude) {
        updateDuration(duration, amplitude);
    }

    @Override
    public void onRecordingProcessing() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRecordingProcessing");
        }

        mConversationActivity.showProgressBar(true);
    }

    @Override
    public void onRecordingReady(@Nullable File recording) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRecordingReady: recording=" + recording);
        }

        releaseRecorder();
        mConversationActivity.showProgressBar(false);

        if (recording == null) {
            if (DEBUG) {
                Log.d(LOG_TAG, "Recording is null");
            }
            return;
        }

        mConversationActivity.onSendVoiceRecord(recording);
    }

    @Override
    public void onPlaybackStopped() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPlaybackStopped");
        }

        if (mCounterDown != null) {
            mCounterDown.cancel();
            mCounterDown = null;
        }

        if (mAudioRecorder != null) {
            mRecordingDuration.setText(formatDuration(mAudioRecorder.getDuration() / 1000));
        }

        mPauseButton.setVisibility(INVISIBLE);
        mPlayButton.setVisibility(VISIBLE);
        mScrollTrackView.scrollTo(0, 0);
    }
}
