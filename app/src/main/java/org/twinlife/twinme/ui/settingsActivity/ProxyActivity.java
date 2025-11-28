/*
 *  Copyright (c) 2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.settingsActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.ProxyDescriptor;
import org.twinlife.twinlife.SNIProxyDescriptor;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinlife.util.Logger;
import org.twinlife.twinme.services.ProxyService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.utils.ProxyView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.SaveTwincodeAsyncTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ProxyActivity extends AbstractTwinmeActivity implements ProxyService.Observer {
    private static final String LOG_TAG = "ProxyActivity";
    private static final boolean DEBUG = false;

    private static final int QRCODE_PIXEL_WIDTH = 295;
    private static final int QRCODE_PIXEL_HEIGHT = 295;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    private static final int ANIMATION_DURATION = 100;

    private static final float DESIGN_CONTAINER_HEIGHT = 720;
    private static final float DESIGN_QRCODE_SIZE = 316;
    private static final float DESIGN_MESSAGE_VIEW_MARGIN = 24f;
    private static final float DESIGN_SHARE_SUBTITLE_VIEW_MARGIN = 10f;
    private static final float DESIGN_ZOOM_MARGIN = 21f;
    private static final float DESIGN_SHARE_PADDING = 20f;
    private static final float DESIGN_SHARE_ICON_SIZE = 42f;
    private static final float DESIGN_SHARE_ICON_PADDING = 27f;
    private static final float DESIGN_ZOOM_HEIGHT = 70;
    private static final float DESIGN_QR_CODE_TOP_MARGIN = 60;
    private static final float DESIGN_CONTAINER_TOP_MARGIN = 100;
    private static final float DESIGN_CONTAINER_MARGIN = 30;
    private static final float DESIGN_ACTION_TOP_MARGIN = 64;
    private static final float DESIGN_ACTION_HEIGHT = 128;
    private static final float DESIGN_REMOVE_VIEW_MARGIN = 60f;

    private boolean mDeferredSaveProxy = false;

    private View mContainerView;
    private TextView mProxyView;
    private View mZoomView;
    private View mEditView;
    private View mCopyView;
    private View mSaveView;

    private ImageView mQRCodeView;
    private ProxyView mSaveProxyView;
    private Bitmap mQRCodeBitmap;

    private float mQrCodeInitialTop = 0;
    private float mQrCodeInitialHeight = 0;
    private float mQrCodeMaxHeight = 0;
    private boolean mZoomQRCode = false;

    private int mProxyPosition = -1;
    private ProxyDescriptor mProxyDescriptor;
    private ProxyService mProxyService;

    //
    // Override TwinmeActivityImpl methods
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mProxyPosition = intent.getIntExtra(Intents.INTENT_PROXY, -1);

        initViews();

        mProxyService = new ProxyService(this, getTwinmeContext(), this);
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (mProxyPosition != -1) {
            List<ProxyDescriptor> proxies = getTwinmeContext().getConnectivityService().getUserProxies();
            if (mProxyPosition < proxies.size()) {
                mProxyDescriptor = proxies.get(mProxyPosition);
            }

            if (mProxyDescriptor != null) {
                mProxyService.getProxyURI(mProxyDescriptor);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();

        mProxyService.dispose();
    }

    @Override
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        super.onRequestPermissions(grantedPermissions);

        boolean storageWriteAccessGranted = false;
        for (Permission grantedPermission : grantedPermissions) {
            if (grantedPermission == Permission.WRITE_EXTERNAL_STORAGE) {
                storageWriteAccessGranted = true;
                break;
            }
        }

        if (mDeferredSaveProxy) {
            mDeferredSaveProxy = false;
            if (storageWriteAccessGranted) {
                saveProxyInGallery();
            }
        }
    }

    //
    // ProxyService.Observer methods
    //

    @Override
    public void onAddProxy(@NonNull SNIProxyDescriptor proxyDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onAddProxy: " + proxyDescriptor);
        }
    }

    @Override
    public void onDeleteProxy(@NonNull ProxyDescriptor proxyDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteProxy: " + proxyDescriptor);
        }

        if (proxyDescriptor.equals(mProxyDescriptor)) {
            finish();
        }
    }

    @Override
    public void onGetProxyUri(@Nullable TwincodeURI twincodeURI, @NonNull ProxyDescriptor proxyDescriptor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProxyUri: " + twincodeURI + " proxyDescriptor: " + proxyDescriptor);
        }

        if (twincodeURI != null && mProxyDescriptor != null && mProxyDescriptor.equals(proxyDescriptor)) {
            updateQRCode(twincodeURI);
        }
    }

    @Override
    public void onErrorAddProxy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onErrorAddProxy");
        }
    }

    @Override
    public void onErrorAlreadyUsed() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onErrorAlreadyUsed");
        }
    }

    @Override
    public void onErrorLimitReached() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onErrorLimitReached");
        }
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());
        setContentView(R.layout.proxy_activity);

        setStatusBarColor();
        setToolBar(R.id.proxy_activity_tool_bar);
        showToolBar(true);
        showBackButton(true);
        setBackgroundColor(Design.GREY_BACKGROUND_COLOR);

        applyInsets(R.id.proxy_activity_layout, R.id.proxy_activity_tool_bar, R.id.proxy_activity_background, Design.TOOLBAR_COLOR, false);

        setTitle(getString(R.string.proxy_activity_title));

        View backgroundView = findViewById(R.id.proxy_activity_background);
        backgroundView.setBackgroundColor(Design.GREY_BACKGROUND_COLOR);

        mContainerView = findViewById(R.id.proxy_activity_container_view);

        ViewGroup.LayoutParams layoutParams = mContainerView.getLayoutParams();
        layoutParams.width = Design.BUTTON_WIDTH;
        layoutParams.height = (int) (DESIGN_CONTAINER_HEIGHT * Design.HEIGHT_RATIO);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mContainerView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONTAINER_TOP_MARGIN * Design.HEIGHT_RATIO);

        float radius = Design.POPUP_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable containerViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        containerViewBackground.getPaint().setColor(Design.POPUP_BACKGROUND_COLOR);
        mContainerView.setBackground(containerViewBackground);

        mQRCodeView = findViewById(R.id.proxy_activity_qrcode_view);
        mQRCodeView.setOnClickListener(view -> onQRCodeClick());

        layoutParams = mQRCodeView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_QRCODE_SIZE * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_QRCODE_SIZE * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mQRCodeView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_QR_CODE_TOP_MARGIN * Design.HEIGHT_RATIO);

        mZoomView = findViewById(R.id.proxy_activity_zoom_view);
        mZoomView.setOnClickListener(v -> onQRCodeClick());

        layoutParams = mZoomView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_ZOOM_HEIGHT * Design.HEIGHT_RATIO);
        layoutParams.height = (int) (DESIGN_ZOOM_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mZoomView.getLayoutParams();
        marginLayoutParams.topMargin = - (int) (DESIGN_ZOOM_MARGIN * Design.HEIGHT_RATIO);

        RoundedView zoomRoundedView = findViewById(R.id.proxy_activity_zoom_rounded_view);
        zoomRoundedView.setBorder(1, Design.GREY_COLOR);
        zoomRoundedView.setColor(Design.WHITE_COLOR);

        ImageView zoomIconView = findViewById(R.id.proxy_activity_zoom_icon_view);
        zoomIconView.setColorFilter(Design.BLACK_COLOR);

        mProxyView = findViewById(R.id.proxy_activity_proxy_view);
        Design.updateTextFont(mProxyView, Design.FONT_BOLD28);
        mProxyView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mProxyView.setOnClickListener(v -> onCopyClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mProxyView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.HEIGHT_RATIO);
        marginLayoutParams.leftMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_CONTAINER_MARGIN * Design.WIDTH_RATIO);

        mEditView = findViewById(R.id.proxy_activity_edit_clickable_view);
        mEditView.setOnClickListener(v -> onEditProxyClick());

        layoutParams = mEditView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ACTION_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mEditView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ACTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        RoundedView editRoundedView = findViewById(R.id.proxy_activity_edit_rounded_view);
        editRoundedView.setBorder(1, Design.GREY_COLOR);
        editRoundedView.setColor(Design.WHITE_COLOR);

        ImageView editIconView = findViewById(R.id.proxy_activity_edit_icon_view);
        editIconView.setColorFilter(Design.BLACK_COLOR);

        TextView editTextView = findViewById(R.id.proxy_activity_edit_text_view);
        Design.updateTextFont(editTextView, Design.FONT_MEDIUM28);
        editTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mCopyView = findViewById(R.id.proxy_activity_copy_clickable_view);
        mCopyView.setOnClickListener(v -> onCopyClick());

        layoutParams = mCopyView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ACTION_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mCopyView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ACTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        RoundedView copyRoundedView = findViewById(R.id.proxy_activity_copy_rounded_view);
        copyRoundedView.setBorder(1, Design.GREY_COLOR);
        copyRoundedView.setColor(Design.WHITE_COLOR);

        ImageView copyIconView = findViewById(R.id.proxy_activity_copy_icon_view);
        copyIconView.setColorFilter(Design.BLACK_COLOR);

        TextView copyTextView = findViewById(R.id.proxy_activity_copy_text_view);
        Design.updateTextFont(copyTextView, Design.FONT_MEDIUM28);
        copyTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        mSaveView = findViewById(R.id.proxy_activity_save_clickable_view);
        mSaveView.setOnClickListener(v -> onSaveInGalleryClick());

        layoutParams = mSaveView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_ACTION_HEIGHT * Design.HEIGHT_RATIO);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSaveView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_ACTION_TOP_MARGIN * Design.HEIGHT_RATIO);

        RoundedView saveRoundedView = findViewById(R.id.proxy_activity_save_rounded_view);
        saveRoundedView.setBorder(1, Design.GREY_COLOR);
        saveRoundedView.setColor(Design.WHITE_COLOR);

        ImageView saveIconView = findViewById(R.id.proxy_activity_save_icon_view);
        saveIconView.setColorFilter(Design.BLACK_COLOR);

        TextView saveTextView = findViewById(R.id.proxy_activity_save_text_view);
        Design.updateTextFont(saveTextView, Design.FONT_MEDIUM28);
        saveTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        View shareView = findViewById(R.id.proxy_activity_social_view);
        shareView.setOnClickListener(v -> onSocialClick());

        marginLayoutParams = (ViewGroup.MarginLayoutParams) shareView.getLayoutParams();
        marginLayoutParams.bottomMargin = - (int) (Design.BUTTON_HEIGHT * 0.5);

        radius = (int) (Design.BUTTON_HEIGHT * 0.5) * Resources.getSystem().getDisplayMetrics().density;
        outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};

        ShapeDrawable socialViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        socialViewBackground.getPaint().setColor(Design.getMainStyle());
        shareView.setBackground(socialViewBackground);

        ImageView socialIconView = findViewById(R.id.proxy_activity_social_icon_view);
        socialIconView.setColorFilter(Color.WHITE);
        marginLayoutParams = (ViewGroup.MarginLayoutParams) socialIconView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.topMargin = (int) (DESIGN_SHARE_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.bottomMargin = (int) (DESIGN_SHARE_ICON_PADDING * Design.HEIGHT_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        layoutParams = socialIconView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SHARE_ICON_SIZE * Design.HEIGHT_RATIO);
        layoutParams.width = (int) (DESIGN_SHARE_ICON_SIZE * Design.HEIGHT_RATIO);

        layoutParams = shareView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        TextView socialViewTitleView = findViewById(R.id.proxy_activity_social_title_view);
        Design.updateTextFont(socialViewTitleView, Design.FONT_MEDIUM32);
        socialViewTitleView.setTextColor(Color.WHITE);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) socialViewTitleView.getLayoutParams();
        marginLayoutParams.leftMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.rightMargin = (int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO);
        marginLayoutParams.setMarginStart((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));
        marginLayoutParams.setMarginEnd((int) (DESIGN_SHARE_PADDING * Design.WIDTH_RATIO));

        TextView shareSubTitleView = findViewById(R.id.proxy_activity_social_subtitle_view);
        Design.updateTextFont(shareSubTitleView, Design.FONT_REGULAR24);
        shareSubTitleView.setTextColor(Design.GREY_COLOR);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) shareSubTitleView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_SHARE_SUBTITLE_VIEW_MARGIN * Design.HEIGHT_RATIO + (Design.BUTTON_HEIGHT * 0.5f)) ;

        TextView messageTextView = findViewById(R.id.proxy_activity_message_view);
        Design.updateTextFont(messageTextView, Design.FONT_MEDIUM28);
        messageTextView.setTextColor(Design.FONT_COLOR_DEFAULT);

        marginLayoutParams = (ViewGroup.MarginLayoutParams) messageTextView.getLayoutParams();
        marginLayoutParams.topMargin = (int) (DESIGN_MESSAGE_VIEW_MARGIN * Design.WIDTH_RATIO);

        mSaveProxyView = findViewById(R.id.proxy_activity_save_proxy_view);

        View removeView = findViewById(R.id.proxy_activity_remove_view);
        removeView.setOnClickListener(v -> {
            onDeleteClick();
        });

        layoutParams = removeView.getLayoutParams();
        layoutParams.height = Design.BUTTON_HEIGHT;

        marginLayoutParams = (ViewGroup.MarginLayoutParams) removeView.getLayoutParams();
        marginLayoutParams.bottomMargin = (int) (DESIGN_REMOVE_VIEW_MARGIN * Design.HEIGHT_RATIO);

        TextView removeTextView = findViewById(R.id.proxy_activity_remove_text_view);
        removeTextView.setTypeface(Design.FONT_REGULAR34.typeface);
        removeTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR34.size);
        removeTextView.setTextColor(Design.FONT_COLOR_RED);

        mQrCodeInitialHeight = DESIGN_QRCODE_SIZE * Design.HEIGHT_RATIO;
        mQrCodeInitialTop = DESIGN_QR_CODE_TOP_MARGIN * Design.HEIGHT_RATIO;
        mQrCodeMaxHeight = Design.BUTTON_WIDTH - (DESIGN_CONTAINER_MARGIN * 2 * Design.WIDTH_RATIO);
    }

    private void onSocialClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSocialClick");
        }

        if (mProxyDescriptor == null) {
            return;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.proxy_activity_title));
        String shareUrl = TwincodeURI.PROXY_ACTION + "/" + mProxyDescriptor.getDescriptor();
        intent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.proxy_activity_share), shareUrl));
        startActivity(Intent.createChooser(intent, null));
    }

    private void onEditProxyClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onEditProxyClick");
        }

        Intent intent = new Intent();
        intent.putExtra(Intents.INTENT_PROXY, mProxyPosition);
        startActivity(AddProxyActivity.class, intent);
    }

    private void updateQRCode(TwincodeURI twincodeURI) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateQRCode");
        }

        if (mQRCodeView == null || mProxyDescriptor == null) {

            return;
        }

        BitMatrix result;
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 0);
            String proxyURL = TwincodeURI.PROXY_ACTION + "/" + mProxyDescriptor.getDescriptor();
            result = new QRCodeWriter().encode(proxyURL, BarcodeFormat.QR_CODE, QRCODE_PIXEL_WIDTH, QRCODE_PIXEL_HEIGHT, hints);
        } catch (Exception exception) {
            Log.e(LOG_TAG, "updateQrcode: exception=" + exception);

            return;
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        mQRCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mQRCodeBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        mQRCodeView.setImageBitmap(mQRCodeBitmap);
        mProxyView.setText(mProxyDescriptor.getDescriptor());
    }

    private void onQRCodeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onQRCodeClick");
        }

        if (mProxyDescriptor == null) {
            return;
        }

        mZoomQRCode = !mZoomQRCode;
        float alpha = mZoomQRCode ? 0.0f : 1.0f;

        float qrCodeHeight = mZoomQRCode ? mQrCodeMaxHeight : mQrCodeInitialHeight;
        float qrCodeTop = mZoomQRCode ? (mContainerView.getHeight() - mQrCodeMaxHeight) * 0.5f : mQrCodeInitialTop;
        long animateActionDelay = mZoomQRCode ? 0 : 100;
        long animateQRCodeDelay = mZoomQRCode ? 100 : 0;

        animateQRCodeAction(alpha, animateActionDelay);
        animateQRCodeSize(qrCodeTop, qrCodeHeight, animateQRCodeDelay);
    }

    private void onCopyClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCopyClick");
        }

        if (mProxyDescriptor != null) {
            String shareUrl = TwincodeURI.PROXY_ACTION + "/" + mProxyDescriptor.getDescriptor();
            org.twinlife.twinme.utils.Utils.setClipboard(this, shareUrl);
            Toast.makeText(this, R.string.conversation_activity_menu_item_view_copy_message, Toast.LENGTH_SHORT).show();
        }
    }

    private void onDeleteClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteClick");
        }

        if (mProxyDescriptor == null) {
            return;
        }

        mProxyService.deleteProxy(mProxyDescriptor);
    }

    private void onSaveInGalleryClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInGalleryClick");
        }

        Permission[] permissions = new Permission[]{Permission.WRITE_EXTERNAL_STORAGE};
        mDeferredSaveProxy = true;
        if (checkPermissions(permissions)) {
            mDeferredSaveProxy = false;
            saveProxyInGallery();
        }
    }

    private void saveProxyInGallery() {
        if (DEBUG) {
            Log.d(LOG_TAG, "saveProxyInGallery");
        }

        mSaveProxyView.setInformation(mQRCodeBitmap, mProxyDescriptor.getDescriptor(), getString(R.string.proxy_activity_share_message));

        Bitmap bitmapToSave = getBitmapFromProxyView();
        if (bitmapToSave != null) {
            new SaveTwincodeAsyncTask(this, bitmapToSave).execute();
        } else {
            toast(getString(R.string.application_operation_failure));
        }
    }

    @Nullable
    private Bitmap getBitmapFromProxyView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getBitmapFromProxyView");
        }

        try {
            Bitmap bitmap = Bitmap.createBitmap(Design.DISPLAY_WIDTH, Design.DISPLAY_HEIGHT, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            mSaveProxyView.layout(0, 0, Design.DISPLAY_WIDTH, Design.DISPLAY_HEIGHT);
            mSaveProxyView.draw(canvas);
            return bitmap;

        } catch (Throwable exception) {

            if (Logger.ERROR) {
                Log.e(LOG_TAG, "Exception when creating bitmap", exception);
            }
            return null;
        }
    }

    private void animateQRCodeAction(float alpha, long delay) {
        if (DEBUG) {
            Log.d(LOG_TAG, "animateQRCodeAction");
        }

        PropertyValuesHolder propertyValuesHolderAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, mSaveView.getAlpha(), alpha);

        List<Animator> animators = new ArrayList<>();
        animators.add(ObjectAnimator.ofPropertyValuesHolder(mCopyView, propertyValuesHolderAlpha));
        animators.add(ObjectAnimator.ofPropertyValuesHolder(mEditView, propertyValuesHolderAlpha));
        animators.add(ObjectAnimator.ofPropertyValuesHolder(mSaveView, propertyValuesHolderAlpha));
        animators.add(ObjectAnimator.ofPropertyValuesHolder(mZoomView, propertyValuesHolderAlpha));
        animators.add(ObjectAnimator.ofPropertyValuesHolder(mProxyView, propertyValuesHolderAlpha));

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.setStartDelay(delay);
        animatorSet.playTogether(animators);
        animatorSet.start();
    }

    private void animateQRCodeSize(float top, float height, long delay) {
        if (DEBUG) {
            Log.d(LOG_TAG, "animateQRCodeSize");
        }

        LayoutTransition layoutTransition = ((ViewGroup) mContainerView).getLayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        layoutTransition.setStartDelay(LayoutTransition.CHANGING, delay);
        layoutTransition.setDuration(ANIMATION_DURATION);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mQRCodeView.getLayoutParams();
        marginLayoutParams.topMargin = (int) top;
        mQRCodeView.setLayoutParams(marginLayoutParams);

        ViewGroup.LayoutParams layoutParams = mQRCodeView.getLayoutParams();
        layoutParams.height = (int) height;
        layoutParams.width = (int) height;
        mQRCodeView.setLayoutParams(layoutParams);
    }
}
