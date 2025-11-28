/*
 *  Copyright (c) 2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.callActivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.baseItemActivity.Item;
import org.twinlife.twinme.ui.baseItemActivity.MessageItem;
import org.twinlife.twinme.ui.baseItemActivity.NameItem;
import org.twinlife.twinme.ui.baseItemActivity.PeerMessageItem;

import java.util.ArrayList;

public class CallConversationView extends PercentRelativeLayout {
    private static final String LOG_TAG = "CallConversationView";
    private static final boolean DEBUG = false;

    public interface CallConversationListener {
        void onCloseConversation();

        void onSendMessage(String message);
    }

    private static final int DESIGN_ACTION_VIEW_COLOR = Color.rgb(60, 60, 60);
    private static final int EDIT_TEXT_BORDER_COLOR = Color.rgb(151, 151, 151);
    private static final int DESIGN_ACTION_VIEW_RADIUS = 14;

    private static final float DESIGN_CONTENT_SEND_VIEW_MARGIN = 10f;
    private static final float DESIGN_CONTENT_SEND_VIEW_BOTTOM_MARGIN = 20f;
    private static final float DESIGN_CONTENT_SEND_VIEW_HEIGHT = 80f;
    private static final float DESIGN_EDIT_TEXT_MARGIN = 20f;
    private static final float DESIGN_EDIT_TEXT_WIDTH_INSET = 32f;
    private static final float DESIGN_EDIT_TEXT_HEIGHT_INSET = 20f;
    protected static final float DESIGN_SEND_IMAGE_HEIGHT = 30f;
    private static final int DESIGN_CLOSE_HEIGHT = 52;
    private static final int DESIGN_CLOSE_TOP_MARGIN = 24;
    private static final int DESIGN_CLOSE_RIGHT_MARGIN = 12;

    private EditText mEditText;
    private RecyclerView mRecyclerView;
    private View mSendView;
    private CallConversationAdapter mCallConversationAdapter;

    private CallConversationListener mCallConversationListener;

    private final ArrayList<Item> mItems = new ArrayList<>();

    public CallConversationView(Context context) {

        super(context);
    }

    public CallConversationView(Context context, AttributeSet attrs) {

        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.call_activity_conversation_view, this, true);
        initViews();
    }

    public CallConversationView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    public void initCallConversationListener(CallConversationListener callConversationListener, CallActivity callActivity) {
        if (DEBUG) {
            Log.d(LOG_TAG, "initCallConversationListener: " + callConversationListener);
        }

        mCallConversationListener = callConversationListener;

        mRecyclerView = findViewById(R.id.call_activity_conversation_list_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(callActivity, RecyclerView.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mCallConversationAdapter = new CallConversationAdapter(callActivity,  mItems);
        mCallConversationAdapter.setHasStableIds(true);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mCallConversationAdapter);
        mRecyclerView.setItemViewCacheSize(Design.ITEM_LIST_CACHE_SIZE);
        mRecyclerView.setItemAnimator(null);
    }

    public void addDescriptor(ConversationService.Descriptor descriptor, boolean isLocal, boolean needsRefresh, String name) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addDescriptor: " + descriptor + " isLocal: " + isLocal);
        }

        // For now, only take into account messages.
        if (!(descriptor instanceof ConversationService.ObjectDescriptor)) {
            return;
        }

        ConversationService.ObjectDescriptor objectDescriptor = (ConversationService.ObjectDescriptor) descriptor;

        if (isLocal) {
            MessageItem messageItem = new MessageItem(objectDescriptor, null);
            addItem(messageItem, name);
        } else {
            PeerMessageItem peerMessageItem = new PeerMessageItem(objectDescriptor, null);
            addItem(peerMessageItem, name);
        }

        if (needsRefresh) {
            reloadData();
        }
    }

    private void addItem(Item item, String name) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addItem: " + item);
        }

        if (mItems.isEmpty()) {
            if (item.isPeerItem()) {
                NameItem nameItem = new NameItem(0, name);
                mItems.add(nameItem);
            }

            mItems.add(item);
            return;
        }

        Item previousItem = mItems.get(mItems.size() - 1);
        if (previousItem != null) {
            if (!item.isPeerItem()) {
                if (!previousItem.isPeerItem()) {
                    previousItem.cornersBitwiseAnd(~(Item.BOTTOM_RIGHT | Item.BOTTOM_LARGE_MARGIN));
                    item.cornersBitwiseAnd(~(Item.TOP_RIGHT | Item.TOP_LARGE_MARGIN));
                } else {
                    previousItem.cornersBitwiseOr(Item.BOTTOM_LEFT | Item.BOTTOM_LARGE_MARGIN);
                    previousItem.cornersBitwiseOr(Item.BOTTOM_RIGHT | Item.BOTTOM_LARGE_MARGIN);
                }
            } else {
                if (!previousItem.isPeerItem()) {
                    previousItem.cornersBitwiseOr(Item.BOTTOM_RIGHT | Item.BOTTOM_LARGE_MARGIN);
                    previousItem.cornersBitwiseOr(Item.TOP_LEFT | Item.BOTTOM_LARGE_MARGIN);
                } else {
                    previousItem.cornersBitwiseAnd(~(Item.BOTTOM_LEFT | Item.BOTTOM_LARGE_MARGIN));
                    item.cornersBitwiseAnd(~(Item.TOP_LEFT | Item.TOP_LARGE_MARGIN));
                }
            }
        }

        if (item.isPeerItem() && previousItem != null && (!previousItem.isPeerItem() || !previousItem.getDescriptorId().twincodeOutboundId.equals(item.getDescriptorId().twincodeOutboundId))) {
            {
                NameItem nameItem = new NameItem(0, name);
                mItems.add(nameItem);
            }
        }

        mItems.add(item);
    }

    public void reloadData() {
        if (DEBUG) {
            Log.d(LOG_TAG, "reloadData");
        }

        mCallConversationAdapter.notifyDataSetChanged();
        scrollToBottom();
    }

    public boolean hasDescriptors() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hasDescriptors");
        }

        return !mItems.isEmpty();
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        View containerView = findViewById(R.id.call_activity_conversation_container_view);

        float radius = DESIGN_ACTION_VIEW_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable containerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        containerViewBackground.getPaint().setColor(DESIGN_ACTION_VIEW_COLOR);
        containerView.setBackground(containerViewBackground);

        View contentSendView = findViewById(R.id.call_activity_conversation_content_send_view);

        ViewGroup.LayoutParams layoutParams = contentSendView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CONTENT_SEND_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) contentSendView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONTENT_SEND_VIEW_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_CONTENT_SEND_VIEW_BOTTOM_MARGIN * Design.HEIGHT_RATIO);

        mEditText = findViewById(R.id.call_activity_conversation_edit_text);
        Design.updateTextFont(mEditText, Design.FONT_REGULAR32);
        mEditText.setTextColor(Color.WHITE);
        mEditText.setHintTextColor(Design.PLACEHOLDER_COLOR);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mEditText.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_EDIT_TEXT_MARGIN * Design.WIDTH_RATIO);

        radius =  (DESIGN_CONTENT_SEND_VIEW_HEIGHT * Design.HEIGHT_RATIO) * 0.5f;

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.TRANSPARENT);
        gradientDrawable.setCornerRadius(radius);
        gradientDrawable.setStroke(3, EDIT_TEXT_BORDER_COLOR);

        mEditText.setBackground(gradientDrawable);

        mEditText.setPadding((int) (DESIGN_EDIT_TEXT_WIDTH_INSET * Design.WIDTH_RATIO), (int) (DESIGN_EDIT_TEXT_HEIGHT_INSET * Design.HEIGHT_RATIO), (int) (DESIGN_EDIT_TEXT_WIDTH_INSET * Design.WIDTH_RATIO), (int) (DESIGN_EDIT_TEXT_HEIGHT_INSET * Design.HEIGHT_RATIO));
        mEditText.setOnClickListener(v -> scrollToBottom());

        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                updateSendButton();
            }
        });

        mSendView = findViewById(R.id.call_activity_conversation_send_clickable_view);
        mSendView.setOnClickListener(v -> onSendClick());

        layoutParams = mSendView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CONTENT_SEND_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        View sendRoundedView = findViewById(R.id.call_activity_conversation_send_rounded_view);

        layoutParams = sendRoundedView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CONTENT_SEND_VIEW_HEIGHT * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_CONTENT_SEND_VIEW_HEIGHT * Design.HEIGHT_RATIO);

        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        ShapeDrawable sendBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        sendBackground.getPaint().setColor(Design.getMainStyle());
        sendRoundedView.setBackground(sendBackground);

        ImageView sendImageView = findViewById(R.id.call_activity_conversation_send_image_view);
        sendImageView.setColorFilter(Color.WHITE);

        layoutParams = sendImageView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SEND_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        View closeView = findViewById(R.id.call_activity_conversation_close_view);
        closeView.setOnClickListener(view -> onCloseClick());

        layoutParams = closeView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CLOSE_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) closeView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CLOSE_TOP_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CLOSE_RIGHT_MARGIN * Design.WIDTH_RATIO);
    }

    private void onSendClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSendClick");
        }

        if (!getSendText().trim().isEmpty()) {
            mCallConversationListener.onSendMessage(mEditText.getText().toString());
            mEditText.setText("");
        }
    }

    private void onCloseClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseClick");
        }

        mCallConversationListener.onCloseConversation();
        hideKeyboard();
        scrollToBottom();
    }

    private void hideKeyboard() {
        if (DEBUG) {
            Log.d(LOG_TAG, "hideKeyboard");
        }

        InputMethodManager inputMethodManager = (InputMethodManager) mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
            inputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
    }

    private void scrollToBottom() {
        if (DEBUG) {
            Log.d(LOG_TAG, "scrollToBottom");
        }

        if (mRecyclerView != null) {
            mRecyclerView.scrollToPosition(mItems.size() - 1);
        }
    }

    private void updateSendButton() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSendButton");
        }

        if (!getSendText().trim().isEmpty()) {
            mSendView.setAlpha((float) 1.0);
        } else {
            mSendView.setAlpha((float) 0.5);
        }
    }

    @NonNull
    private String getSendText() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getSendText");
        }

        Editable text = mEditText.getText();
        if (text == null) {
            return "";
        }

        return text.toString();
    }
}