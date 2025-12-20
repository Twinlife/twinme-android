/*
 *  Copyright (c) 2022-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.inAppSubscriptionActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.TwinmeApplication;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.services.InAppSubscriptionService;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.WebViewActivity;
import org.twinlife.twinme.ui.profiles.AddProfileActivity;
import org.twinlife.twinme.utils.AbstractBottomSheetView;
import org.twinlife.twinme.utils.DefaultConfirmView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class InAppSubscriptionActivity extends AbstractTwinmeActivity implements InAppSubscriptionService.Observer, BillingManager.BillingManagerListener, CustomProgressBarView.Observer, ViewTreeObserver.OnGlobalLayoutListener {
    private static final String LOG_TAG = "InAppSubscriptionAc...";
    private static final boolean DEBUG = false;

    private class TextViewLinkHandler extends LinkMovementMethod {

        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {

            if (event.getAction() != MotionEvent.ACTION_UP) {

                return super.onTouchEvent(widget, buffer, event);
            }

            int x = (int) event.getX() - widget.getTotalPaddingLeft() + widget.getScrollX();
            int y = (int) event.getY() - widget.getTotalPaddingTop() + widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int offset = layout.getOffsetForHorizontal(line, x);

            URLSpan[] links = buffer.getSpans(offset, offset, URLSpan.class);
            if (links.length != 0) {
                Intent intent = new Intent(InAppSubscriptionActivity.this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.INTENT_WEB_VIEW_ACTIVITY_URL, links[0].getURL());

                startActivity(intent);
            }

            return true;
        }
    }

    private static final float DESIGN_CLOSE_SIZE = 120f;
    private static final float DESIGN_SUBSCRIBE_WIDTH = 530f;
    private static final float DESIGN_SUBSCRIBE_HEIGHT = 100f;
    private static final float DESIGN_IMAGE_BOTTOM_MARGIN = 6f;
    private static final int DESIGN_PREMIUM_IMAGE_WIDTH = 292;
    private static final int DESIGN_PREMIUM_IMAGE_HEIGHT = 160;

    private static final int DESIGN_GREY_COLOR = Color.rgb(142, 142, 147);
    private static final int DESIGN_TOP_COLOR = Color.rgb(255, 255, 255);
    private static final int DESIGN_BOTTOM_COLOR = Color.rgb(231, 231, 231);
    private static final int DESIGN_DARK_BACKGROUND_COLOR = Color.rgb(52, 52, 52);
    private static final int DESIGN_BEST_OFFER_COLOR = Color.rgb(255, 32, 80);
    private static final int DESIGN_SUBSCRIBE_COLOR = Color.rgb(255, 32, 80);
    private static final int DESIGN_DESCRIPTION_COLOR = Color.rgb(86, 86, 86);

    private View mSubscribeView;
    private TextView mSubscribeTextView;
    private TextView mTitleView;
    private TextView mDescriptionView;
    private ImageView mDescriptionImageView;
    private CustomProgressBarView mProgressBarOneView;
    private CustomProgressBarView mProgressBarTwoView;
    private CustomProgressBarView mProgressBarThreeView;
    private CustomProgressBarView mProgressBarFourView;
    private View mOneMonthSubscriptionView;
    private View mSixMonthSubscriptionView;
    private View mOneYearSubscriptionView;
    private TextView mOneMonthDurationView;
    private TextView mSixMonthDurationView;
    private TextView mOneYearDurationView;
    private TextView mPriceOneMonthTextView;
    private TextView mPriceSixMonthTextView;
    private TextView mPriceOneYearTextView;
    private TextView mOneMonthUnitView;
    private TextView mSixMonthUnitView;
    private TextView mOneYearUnitView;
    private TextView mReductionOneYearTextView;
    private TextView mReductionSixMonthTextView;
    private TextView mFreeTrialTextView;
    private TextView mFooterTextView;
    private TextView mSubscribedTextView;
    private CardView mOneMonthCardView;
    private CardView mSixMonthCardView;
    private CardView mOneYearCardView;
    private View mLaterView;
    private View mRestoreView;
    private View mInviteView;
    private TextView mErrorTextView;

    private ImageView mPremiumImageView;

    private InAppSubscriptionService mInAppSubscriptionService;
    private BillingManager mBillingManager;
    private List<ProductDetails> mProductList;
    private ProductDetails mProductDetailsSelected;

    private boolean mUIInitialized = false;
    private boolean mUIPostInitialized = false;

    private int mDescriptionStep = 0;

    private boolean mIsSubscribed = false;
    private Profile mProfile;

    private Purchase mPurchase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mIsSubscribed = isFeatureSubscribed(TwinmeApplication.Feature.GROUP_CALL);

        initViews();

        mInAppSubscriptionService = new InAppSubscriptionService(this, getTwinmeContext(), this);
        mBillingManager = new BillingManager(this, this);
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        if (mDescriptionStep != 0) {
            mDescriptionStep = 4;
            nextDescription();
        }
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        // Stop the animation if the activity is paused!
        mProgressBarOneView.stopAnimation();
        mProgressBarTwoView.stopAnimation();
        mProgressBarThreeView.stopAnimation();
        mProgressBarFourView.stopAnimation();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        mBillingManager.dispose();
        mInAppSubscriptionService.dispose();

        super.onDestroy();
    }

    @Override
    public void finish() {
        if (DEBUG) {
            Log.d(LOG_TAG, "finish");
        }

        mBillingManager.dispose();
        super.finish();
    }

    //
    // Override Activity methods
    //

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onWindowFocusChanged: hasFocus=" + hasFocus);
        }

        if (hasFocus && mUIInitialized && !mUIPostInitialized) {
            postInitViews();
        }
    }

    @Override
    public void onGlobalLayout() {

        if (mDescriptionStep == 0) {
            nextDescription();
        }
    }

    //
    // Implement InAppSubscriptionService.Observer methods
    //

    @Override
    public void onGetDefaultProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProfile profile=" + profile);
        }

        mProfile = profile;
    }

    @Override
    public void onGetDefaultProfileNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetDefaultProfileNotFound");
        }
    }

    @Override
    public void onSubscribeSuccess() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSubscribeSuccess");
        }

        mIsSubscribed = true;
        updateViews();
        finish();
    }

    @Override
    public void onSubscribeFailed(@NonNull ErrorCode errorCode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSubscribeFailed errorCode=" + errorCode);
        }

        onError(errorCode, null, this::finish);
    }

    @Override
    public void onSubscribeCancel() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSubscribeCancel");
        }

        if (getTwinmeApplication().getInvitationSubscriptionImage() != null) {
            File imageFile = new File(getTwinmeApplication().getInvitationSubscriptionImage());
            Utils.deleteFile(LOG_TAG, imageFile);
        }

        getTwinmeApplication().setInvitationSubscriptionTwincode(null);
        getTwinmeApplication().setInvitationSubscriptionImage(null);

        finish();
    }

    //
    // Implement BillingManagerListener.Observer methods
    //

    @Override
    public void onGetProducts(List<ProductDetails> productDetails) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProducts: " + productDetails);
        }

        if (productDetails != null) {
            mProductList = productDetails;

            if (mUIPostInitialized) {
                updateProducts(true);
            }
        }
    }

    @Override
    public void onGetCurrentSubscription(Purchase purchase) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCurrentSubscription: " + purchase);
        }

        if (!purchase.getProducts().isEmpty()) {
            for (ProductDetails productDetails : mProductList) {
                if (productDetails.getProductId().equals(purchase.getProducts().get(0))) {
                    mProductDetailsSelected = productDetails;
                    mPurchase = purchase;

                    final String orderId = purchase.getOrderId();
                    if (!mIsSubscribed && orderId != null) {
                        mIsSubscribed = true;
                        mInAppSubscriptionService.subscribeFeature(mProductDetailsSelected.getProductId(), purchase.getPurchaseToken(), orderId);
                    }

                    updateViews();
                    break;
                }
            }
        }

    }

    @Override
    public void onGetCurrentSubscriptionNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCurrentSubscriptionNotFound");
        }

        // If the subscription is canceled, we may not be immediately aware.  From the server point
        // of view, the user still has the subscription but from Google point of view it was canceled.
        // This is a transient difference that is solved in a few minutes until the server becomes
        // aware of the cancel.
        mIsSubscribed = isFeatureSubscribed(TwinmeApplication.Feature.GROUP_CALL);
        if (mIsSubscribed && getTwinmeApplication().getInvitationSubscriptionTwincode() == null) {
            mIsSubscribed = false;
            Log.w(LOG_TAG, "Google subscription is canceled but skred server still grants access to the subscription! You are lucky!");
        }
        onSubscriptionClick(BillingManager.SIX_MONTHS_SUBSCRIPTION_ID);
    }

    @Override
    public void onPurchaseSuccess(Purchase purchase) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPurchaseSuccess");
        }

        final String orderId = purchase.getOrderId();
        if (orderId != null) {
            mPurchase = purchase;
            updateViews();

            mInAppSubscriptionService.subscribeFeature(mProductDetailsSelected.getProductId(), purchase.getPurchaseToken(), orderId);
        }
    }

    @Override
    public void onPurchaseFailed() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPurchaseFailed");
        }
    }

    @Override
    public void onSetupFinishedInError(int errorCode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetupFinishedInError " + errorCode);
        }

        switch (errorCode) {
            // These errors are not recoverable (no Google services, Billing API too old, Billing item deleted).
            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                if (mUIInitialized) {
                    if (getTwinmeApplication().getInvitationSubscriptionTwincode() == null) {
                        mErrorTextView.setVisibility(View.VISIBLE);
                        mInviteView.setAlpha(1f);
                        RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) mInviteView.getLayoutParams();
                        relativeParams.addRule(RelativeLayout.BELOW, R.id.in_app_subscription_activity_error_view);
                    } else {
                        updateProducts(false);
                    }
                }
                break;

            default:
                break;
        }
    }

    //
    // Implement CustomProgressBarView.Observer methods
    //

    @Override
    public void onCustomProgressBarEndAnmation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCustomProgressBarEndAnmation");
        }

        nextDescription();
    }

    //
    // Private methods
    //

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.in_app_subscription_activity);

        setStatusBarColor(Design.WHITE_COLOR);
        setBackgroundColor(Design.WHITE_COLOR);

        applyInsets(R.id.in_app_subscription_activity_layout, -1, -1, Design.WHITE_COLOR, false);

        View closeView = findViewById(R.id.in_app_subscription_activity_close_view);
        closeView.setOnClickListener(v -> onCloseClick());

        ViewGroup.LayoutParams layoutParams = closeView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CLOSE_SIZE * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_CLOSE_SIZE * Design.HEIGHT_RATIO);

        TextView titleView = findViewById(R.id.in_app_subscription_activity_skred_plus_view);
        titleView.setTypeface(Design.FONT_BOLD54.typeface);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD54.size);
        titleView.setTextColor(Design.FONT_COLOR_DEFAULT);

        layoutParams = titleView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_CLOSE_SIZE * Design.HEIGHT_RATIO);

        View offerView = findViewById(R.id.in_app_subscription_activity_offer_view);
        offerView.setOnClickListener(view -> onDescriptionClick());

        ViewTreeObserver viewTreeObserver = offerView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(this);

        float radius = Design.CONTAINER_RADIUS * Resources.getSystem().getDisplayMetrics().density;
        float[] outerRadii = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        GradientDrawable offerViewBackground = new GradientDrawable();
        offerViewBackground.setShape(GradientDrawable.RECTANGLE);
        offerViewBackground.setCornerRadii(outerRadii);

        boolean darkMode = Design.isDarkMode(this);
        if (darkMode) {
            offerViewBackground.setColor(DESIGN_DARK_BACKGROUND_COLOR);
        } else {
            offerViewBackground.setColors(new int[]{DESIGN_TOP_COLOR, DESIGN_BOTTOM_COLOR});
        }

        offerView.setBackground(offerViewBackground);

        mProgressBarOneView = findViewById(R.id.in_app_subscription_activity_progress_bar_one);
        mProgressBarOneView.setObserver(this);

        mProgressBarTwoView = findViewById(R.id.in_app_subscription_activity_progress_bar_two);
        mProgressBarTwoView.setObserver(this);

        mProgressBarThreeView = findViewById(R.id.in_app_subscription_activity_progress_bar_three);
        mProgressBarThreeView.setObserver(this);

        mProgressBarFourView = findViewById(R.id.in_app_subscription_activity_progress_bar_four);
        mProgressBarFourView.setObserver(this);

        int size24 = (int) (Design.MIN_RATIO * 24);
        int size28 = (int) (Design.MIN_RATIO * 28);
        int size32 = (int) (Design.MIN_RATIO * 32);
        int size34 = (int) (Design.MIN_RATIO * 34);
        int size36 = (int) (Design.MIN_RATIO * 36);
        int size38 = (int) (Design.MIN_RATIO * 38);
        int size80 = (int) (Design.MIN_RATIO * 80);
        int size88 = (int) (Design.MIN_RATIO * 88);

        mTitleView = findViewById(R.id.in_app_subscription_activity_title_view);
        mTitleView.setTypeface(Design.FONT_MEDIUM36.typeface);
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM36.size);

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mTitleView, size32, size36, 1,
                TypedValue.COMPLEX_UNIT_PX);

        mDescriptionView = findViewById(R.id.in_app_subscription_activity_description_view);
        mDescriptionView.setTypeface(Design.FONT_MEDIUM32.typeface);
        mDescriptionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM32.size);

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mDescriptionView, size28, size32, 1,
                TypedValue.COMPLEX_UNIT_PX);

        if (darkMode) {
            mTitleView.setTextColor(Color.WHITE);
            mDescriptionView.setTextColor(Color.WHITE);
        } else {
            mTitleView.setTextColor(Color.BLACK);
            mDescriptionView.setTextColor(DESIGN_DESCRIPTION_COLOR);
        }

        mDescriptionImageView = findViewById(R.id.in_app_subscription_activity_description_image_view);

        View offerSubscriptionView = findViewById(R.id.in_app_subscription_activity_subscription_offer_view);

        mOneYearSubscriptionView = findViewById(R.id.in_app_subscription_activity_one_year_subscription_view);
        mOneYearSubscriptionView.setAlpha(0f);
        mOneYearSubscriptionView.setOnClickListener(view -> onSubscriptionClick(BillingManager.ONE_YEAR_SUBSCRIPTION_ID));

        mOneYearCardView = findViewById(R.id.in_app_subscription_activity_one_year_subscription_card_view);
        mOneYearCardView.setAlpha(0f);
        mOneYearCardView.setRadius(radius);

        TextView oneYearTitleView = findViewById(R.id.in_app_subscription_activity_one_year_subscription_title_view);
        oneYearTitleView.setTypeface(Design.FONT_BOLD28.typeface);
        oneYearTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size28);
        oneYearTitleView.setTextColor(DESIGN_GREY_COLOR);
        oneYearTitleView.setAllCaps(true);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(oneYearTitleView, size24, size28, 1,
                TypedValue.COMPLEX_UNIT_PX);

        TextView oneYearSubTitleView = findViewById(R.id.in_app_subscription_activity_one_year_subscription_subtitle_view);
        oneYearSubTitleView.setTypeface(Design.FONT_BOLD28.typeface);
        oneYearSubTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size28);
        oneYearSubTitleView.setTextColor(DESIGN_GREY_COLOR);
        oneYearSubTitleView.setAllCaps(true);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(oneYearSubTitleView, size24, size28, 1,
                TypedValue.COMPLEX_UNIT_PX);

        mOneYearDurationView = findViewById(R.id.in_app_subscription_activity_one_year_subscription_duration_view);
        mOneYearDurationView.setTypeface(Design.FONT_REGULAR88.typeface);
        mOneYearDurationView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size88);
        mOneYearDurationView.setTextColor(Color.BLACK);
        mOneYearDurationView.setIncludeFontPadding(false);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mOneYearDurationView, size80, size88, 1,
                TypedValue.COMPLEX_UNIT_PX);

        mOneYearUnitView = findViewById(R.id.in_app_subscription_activity_one_year_subscription_duration_unit_view);
        mOneYearUnitView.setTypeface(Design.FONT_REGULAR32.typeface);
        mOneYearUnitView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size32);
        mOneYearUnitView.setTextColor(Color.BLACK);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mOneYearUnitView, size28, size32, 1,
                TypedValue.COMPLEX_UNIT_PX);

        mPriceOneYearTextView = findViewById(R.id.in_app_subscription_activity_one_year_subscription_price_view);
        mPriceOneYearTextView.setTypeface(Design.FONT_MEDIUM38.typeface);
        mPriceOneYearTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size38);
        mPriceOneYearTextView.setTextColor(Color.BLACK);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mPriceOneYearTextView, size32, size38, 1,
                TypedValue.COMPLEX_UNIT_PX);

        mReductionOneYearTextView = findViewById(R.id.in_app_subscription_activity_one_year_subscription_reduction_view);
        mReductionOneYearTextView.setTypeface(Design.FONT_BOLD34.typeface);
        mReductionOneYearTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size34);
        mReductionOneYearTextView.setTextColor(DESIGN_BEST_OFFER_COLOR);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mReductionOneYearTextView, size32, size34, 1,
                TypedValue.COMPLEX_UNIT_PX);

        mSixMonthSubscriptionView = findViewById(R.id.in_app_subscription_activity_six_month_subscription_view);
        mSixMonthSubscriptionView.setAlpha(0f);
        mSixMonthSubscriptionView.setOnClickListener(view -> onSubscriptionClick(BillingManager.SIX_MONTHS_SUBSCRIPTION_ID));

        mSixMonthCardView = findViewById(R.id.in_app_subscription_activity_six_month_subscription_card_view);
        mSixMonthCardView.setRadius(radius);

        TextView sixMonthTitleView = findViewById(R.id.in_app_subscription_activity_six_month_subscription_title_view);
        sixMonthTitleView.setTypeface(Design.FONT_BOLD28.typeface);
        sixMonthTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size28);
        sixMonthTitleView.setTextColor(Color.WHITE);
        sixMonthTitleView.setAllCaps(true);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(sixMonthTitleView, size24, size28, 1,
                TypedValue.COMPLEX_UNIT_PX);

        TextView sixMonthSubTitleView = findViewById(R.id.in_app_subscription_activity_six_month_subscription_subtitle_view);
        sixMonthSubTitleView.setTypeface(Design.FONT_BOLD28.typeface);
        sixMonthSubTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size28);
        sixMonthSubTitleView.setTextColor(DESIGN_GREY_COLOR);
        sixMonthSubTitleView.setAllCaps(true);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(sixMonthSubTitleView, size24, size28, 1,
                TypedValue.COMPLEX_UNIT_PX);

        ShapeDrawable sixMonthTitleViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        sixMonthTitleViewBackground.getPaint().setColor(DESIGN_BEST_OFFER_COLOR);
        sixMonthTitleView.setBackground(sixMonthTitleViewBackground);

        mSixMonthDurationView = findViewById(R.id.in_app_subscription_activity_six_month_subscription_duration_view);
        mSixMonthDurationView.setTypeface(Design.FONT_REGULAR88.typeface);
        mSixMonthDurationView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size88);
        mSixMonthDurationView.setTextColor(Color.BLACK);
        mSixMonthDurationView.setIncludeFontPadding(false);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mSixMonthDurationView, size80, size88, 1,
                TypedValue.COMPLEX_UNIT_PX);

        mSixMonthUnitView = findViewById(R.id.in_app_subscription_activity_six_month_subscription_duration_unit_view);
        mSixMonthUnitView.setTypeface(Design.FONT_REGULAR32.typeface);
        mSixMonthUnitView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size32);
        mSixMonthUnitView.setTextColor(Color.BLACK);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mSixMonthUnitView, size28, size32, 1,
                TypedValue.COMPLEX_UNIT_PX);

        mPriceSixMonthTextView = findViewById(R.id.in_app_subscription_activity_six_month_subscription_price_view);
        mPriceSixMonthTextView.setTypeface(Design.FONT_MEDIUM38.typeface);
        mPriceSixMonthTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size38);
        mPriceSixMonthTextView.setTextColor(Color.BLACK);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mPriceSixMonthTextView, size32, size38, 1,
                TypedValue.COMPLEX_UNIT_PX);

        mReductionSixMonthTextView = findViewById(R.id.in_app_subscription_activity_six_month_subscription_reduction_view);
        mReductionSixMonthTextView.setTypeface(Design.FONT_BOLD34.typeface);
        mReductionSixMonthTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size34);
        mReductionSixMonthTextView.setTextColor(DESIGN_BEST_OFFER_COLOR);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mReductionSixMonthTextView, size32, size34, 1,
                TypedValue.COMPLEX_UNIT_PX);

        mOneMonthSubscriptionView = findViewById(R.id.in_app_subscription_activity_one_month_subscription_view);
        mOneMonthSubscriptionView.setAlpha(0f);
        mOneMonthSubscriptionView.setOnClickListener(view -> onSubscriptionClick(BillingManager.ONE_MONTH_SUBSCRIPTION_ID));

        mOneMonthCardView = findViewById(R.id.in_app_subscription_activity_one_month_subscription_card_view);
        mOneMonthCardView.setAlpha(0f);
        mOneMonthCardView.setRadius(radius);

        TextView oneMonthTitleView = findViewById(R.id.in_app_subscription_activity_one_month_subscription_title_view);
        oneMonthTitleView.setTypeface(Design.FONT_BOLD28.typeface);
        oneMonthTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size28);
        oneMonthTitleView.setTextColor(DESIGN_GREY_COLOR);
        oneMonthTitleView.setAllCaps(true);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(oneMonthTitleView, size24, size28, 1,
                TypedValue.COMPLEX_UNIT_PX);

        TextView oneMonthSubTitleView = findViewById(R.id.in_app_subscription_activity_one_month_subscription_subtitle_view);
        oneMonthSubTitleView.setTypeface(Design.FONT_BOLD28.typeface);
        oneMonthSubTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size28);
        oneMonthSubTitleView.setTextColor(DESIGN_GREY_COLOR);
        oneMonthSubTitleView.setAllCaps(true);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(oneMonthSubTitleView, size24, size28, 1,
                TypedValue.COMPLEX_UNIT_PX);

        mOneMonthDurationView = findViewById(R.id.in_app_subscription_activity_one_month_subscription_duration_view);
        mOneMonthDurationView.setTypeface(Design.FONT_REGULAR88.typeface);
        mOneMonthDurationView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size88);
        mOneMonthDurationView.setTextColor(Color.BLACK);
        mOneMonthDurationView.setIncludeFontPadding(false);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mOneMonthDurationView, size80, size88, 1,
                TypedValue.COMPLEX_UNIT_PX);

        mOneMonthUnitView = findViewById(R.id.in_app_subscription_activity_one_month_subscription_duration_unit_view);
        mOneMonthUnitView.setTypeface(Design.FONT_REGULAR32.typeface);
        mOneMonthUnitView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size32);
        mOneMonthUnitView.setTextColor(Color.BLACK);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mOneMonthUnitView, size28, size32, 1,
                TypedValue.COMPLEX_UNIT_PX);

        mPriceOneMonthTextView = findViewById(R.id.in_app_subscription_activity_one_month_subscription_price_view);
        mPriceOneMonthTextView.setTypeface(Design.FONT_MEDIUM38.typeface);
        mPriceOneMonthTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size38);
        mPriceOneMonthTextView.setTextColor(Color.BLACK);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mPriceOneMonthTextView, size32, size38, 1,
                TypedValue.COMPLEX_UNIT_PX);

        mFreeTrialTextView = findViewById(R.id.in_app_subscription_activity_free_trial_view);
        mFreeTrialTextView.setTypeface(Design.FONT_MEDIUM36.typeface);
        mFreeTrialTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM36.size);
        mFreeTrialTextView.setTextColor(DESIGN_GREY_COLOR);
        mFreeTrialTextView.setAlpha(0f);

        mSubscribedTextView = findViewById(R.id.in_app_subscription_activity_subscribed_view);
        mSubscribedTextView.setTypeface(Design.FONT_MEDIUM34.typeface);
        mSubscribedTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
        mSubscribedTextView.setTextColor(DESIGN_GREY_COLOR);
        mSubscribedTextView.setAlpha(0f);

        mErrorTextView = findViewById(R.id.in_app_subscription_activity_error_view);
        mErrorTextView.setTypeface(Design.FONT_MEDIUM34.typeface);
        mErrorTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_MEDIUM34.size);
        mErrorTextView.setTextColor(DESIGN_GREY_COLOR);
        mErrorTextView.setVisibility(View.GONE);

        mSubscribeView = findViewById(R.id.in_app_subscription_activity_subscribe_view);
        mSubscribeView.setAlpha(0f);
        mSubscribeView.setOnClickListener(v -> onSubscribeClick());

        ShapeDrawable subscribeViewBackground = new ShapeDrawable(new RoundRectShape(outerRadii, null, null));
        subscribeViewBackground.getPaint().setColor(DESIGN_SUBSCRIBE_COLOR);
        mSubscribeView.setBackground(subscribeViewBackground);

        layoutParams = mSubscribeView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_SUBSCRIBE_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_SUBSCRIBE_HEIGHT * Design.HEIGHT_RATIO);

        mSubscribeTextView = findViewById(R.id.in_app_subscription_activity_subscribe_title_view);
        mSubscribeTextView.setTypeface(Design.FONT_BOLD36.typeface);
        mSubscribeTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD36.size);
        mSubscribeTextView.setTextColor(Color.WHITE);
        mSubscribeTextView.setAllCaps(true);

        mFooterTextView = findViewById(R.id.in_app_subscription_activity_footer_view);
        mFooterTextView.setTypeface(Design.FONT_REGULAR24.typeface);
        mFooterTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_REGULAR24.size);
        mFooterTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        mFooterTextView.setAlpha(0f);

        String footerText = getResources().getString(R.string.in_app_subscription_activity_footer_message) + "\n\n" + getResources().getString(R.string.welcome_activity_terms_of_use) + " - " + getResources().getString(R.string.welcome_activity_privacy_policy);
        mFooterTextView.setText(footerText);
        addLinks();

        mLaterView = findViewById(R.id.in_app_subscription_activity_later_view);
        mLaterView.setOnClickListener(v -> onLaterClick());
        mLaterView.setAlpha(0f);

        TextView laterTextView = findViewById(R.id.in_app_subscription_activity_later_title_view);
        laterTextView.setTypeface(Design.FONT_BOLD32.typeface);
        laterTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD32.size);
        laterTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        laterTextView.setAllCaps(true);

        mRestoreView = findViewById(R.id.in_app_subscription_activity_restore_view);
        mRestoreView.setOnClickListener(v -> onRestoreClick());
        mRestoreView.setAlpha(0f);

        TextView restoreTextView = findViewById(R.id.in_app_subscription_activity_restore_title_view);
        restoreTextView.setTypeface(Design.FONT_BOLD32.typeface);
        restoreTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD32.size);
        restoreTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        restoreTextView.setAllCaps(true);

        mInviteView = findViewById(R.id.in_app_subscription_activity_invite_view);
        mInviteView.setOnClickListener(v -> onInviteClick());
        mInviteView.setAlpha(0f);

        TextView inviteTextView = findViewById(R.id.in_app_subscription_activity_invite_title_view);
        inviteTextView.setTypeface(Design.FONT_BOLD32.typeface);
        inviteTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Design.FONT_BOLD32.size);
        inviteTextView.setTextColor(Design.FONT_COLOR_DEFAULT);
        inviteTextView.setAllCaps(true);

        mPremiumImageView = findViewById(R.id.in_app_subscription_activity_premium_image_view);
        mPremiumImageView.setAlpha(0f);

        layoutParams = mPremiumImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_PREMIUM_IMAGE_WIDTH * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_PREMIUM_IMAGE_HEIGHT * Design.HEIGHT_RATIO);

        if (getTwinmeApplication().getInvitationSubscriptionTwincode() != null) {
            offerSubscriptionView.setVisibility(View.GONE);
            RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) mSubscribeView.getLayoutParams();
            relativeParams.addRule(RelativeLayout.BELOW, R.id.in_app_subscription_activity_premium_image_view);
        } else {
            mPremiumImageView.setVisibility(View.GONE);
            RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) mSubscribeView.getLayoutParams();
            relativeParams.addRule(RelativeLayout.BELOW, R.id.in_app_subscription_activity_free_trial_view);
        }

        if (getTwinmeApplication().getInvitationSubscriptionImage() != null) {
            mPremiumImageView.setImageBitmap(BitmapFactory.decodeFile(getTwinmeApplication().getInvitationSubscriptionImage()));
        }

        mUIInitialized = true;
    }

    private void postInitViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "postInitViews");
        }

        mUIPostInitialized = true;

        if (mProductList != null) {
            updateProducts(true);
            onSubscriptionClick(BillingManager.SIX_MONTHS_SUBSCRIPTION_ID);
        }
    }

    private void onRestoreClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRestoreClick");
        }

        if (!mIsSubscribed && mProductList != null) {
            mBillingManager.fetchPurchases();
        }
    }

    private void onInviteClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onInviteClick");
        }

        if (mProfile != null) {
            Intent intent = new Intent();
            intent.setClass(this, InvitationSubscriptionActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent();
            intent.putExtra(Intents.INTENT_FIRST_PROFILE, true);
            intent.putExtra(Intents.INTENT_FROM_SUBSCRIPTION, true);
            intent.setClass(this, AddProfileActivity.class);
            startActivity(intent);
        }
    }

    private void onCloseClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCloseClick");
        }

        finish();
    }

    private void onLaterClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onLaterClick");
        }

        if (!mIsSubscribed) {
            finish();
        }
    }

    private void onSubscriptionClick(String productId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSubscriptionClick: " + productId);
        }

        if (!mIsSubscribed && mProductList != null) {
            for (ProductDetails productDetails : mProductList) {
                if (productDetails.getProductId().equals(productId)) {
                    mProductDetailsSelected = productDetails;
                    break;
                }
            }

            updateViews();
        }

    }

    private void onSubscribeClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSubscribeClick");
        }

        if (mProductDetailsSelected != null) {
            if (!mIsSubscribed) {
                mBillingManager.subscribeToProductId(mProductDetailsSelected.getProductId());
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions?sku=" + mProductDetailsSelected.getProductId() + "&package=" + getString(R.string.skred_app_id))));
            }
        } else if (getTwinmeApplication().getInvitationSubscriptionTwincode() != null) {
            ViewGroup viewGroup = findViewById(R.id.in_app_subscription_activity_layout);

            DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
            defaultConfirmView.setTitle(getString(R.string.in_app_subscription_activity_cancel_subscription));
            defaultConfirmView.setMessage(getString(R.string.in_app_subscription_activity_cancel_subscription_confirmation));
            defaultConfirmView.setImage(null);
            defaultConfirmView.setConfirmTitle(getString(R.string.application_confirm));

            AbstractBottomSheetView.Observer observer = new AbstractBottomSheetView.Observer() {
                @Override
                public void onConfirmClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCancelClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onDismissClick() {
                    defaultConfirmView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    viewGroup.removeView(defaultConfirmView);
                    setStatusBarColor();

                    if (fromConfirmAction) {
                        mInAppSubscriptionService.cancelFeature(getTwinmeApplication().getInvitationSubscriptionTwincode(), "");
                    }
                }
            };
            defaultConfirmView.setObserver(observer);
            viewGroup.addView(defaultConfirmView);
            defaultConfirmView.show();

            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);
        }
    }

    private void onDescriptionClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDescriptionClick");
        }

        if (mDescriptionStep == 1) {
            mProgressBarOneView.stopAnimation();
        } else if (mDescriptionStep == 2) {
            mProgressBarTwoView.stopAnimation();
        } else if (mDescriptionStep == 3) {
            mProgressBarThreeView.stopAnimation();
        } else {
            mProgressBarFourView.stopAnimation();
        }

        nextDescription();
    }

    @SuppressLint("SetTextI18n")
    private void updateProducts(boolean fetchPurchases) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateProducts fetchPurchases=" + fetchPurchases);
        }

        if (mProductList != null || getTwinmeApplication().getInvitationSubscriptionTwincode() != null) {
            mSubscribeView.setAlpha(1f);

            float subscribeViewAlpha = 1f;
            if (!mIsSubscribed) {
                mFreeTrialTextView.setAlpha(1f);
                mFooterTextView.setAlpha(1f);
                mLaterView.setAlpha(1f);
                mRestoreView.setAlpha(1f);
                mInviteView.setAlpha(1f);
                mPremiumImageView.setAlpha(0f);
                mSubscribedTextView.setAlpha(0f);
            } else {
                if (getTwinmeApplication().getInvitationSubscriptionTwincode() != null) {
                    mSubscribeTextView.setText(getString(R.string.in_app_subscription_activity_cancel_subscription));
                } else {
                    mSubscribeTextView.setText(getString(R.string.in_app_subscription_activity_manage_susbcription));
                }

                mSubscribedTextView.setAlpha(1f);
                mPremiumImageView.setAlpha(1f);
                subscribeViewAlpha = 0.5f;
                mFreeTrialTextView.setAlpha(0f);
                mFooterTextView.setAlpha(0f);
                mLaterView.setAlpha(0f);
                mRestoreView.setAlpha(0f);
                mInviteView.setAlpha(0f);
            }

            if (mProductList != null) {
                long oneMonthPrice = 0;
                long sixMonthPrice = 0;
                long oneYearPrice = 0;

                for (ProductDetails productDetails : mProductList) {
                    String price = "";
                    long priceAmountMicros = 0;
                    if (productDetails.getSubscriptionOfferDetails() != null) {
                        for (ProductDetails.SubscriptionOfferDetails subscriptionOfferDetails : productDetails.getSubscriptionOfferDetails()) {
                            for (ProductDetails.PricingPhase pricingPhase : subscriptionOfferDetails.getPricingPhases().getPricingPhaseList()) {
                                price = pricingPhase.getFormattedPrice();
                                priceAmountMicros = pricingPhase.getPriceAmountMicros();
                            }
                        }
                    }

                    switch (productDetails.getProductId()) {
                        case BillingManager.ONE_MONTH_SUBSCRIPTION_ID:
                            mOneMonthSubscriptionView.setAlpha(subscribeViewAlpha);
                            mPriceOneMonthTextView.setText(price);
                            oneMonthPrice = priceAmountMicros;
                            break;
                        case BillingManager.SIX_MONTHS_SUBSCRIPTION_ID:
                            mSixMonthSubscriptionView.setAlpha(subscribeViewAlpha);
                            mPriceSixMonthTextView.setText(price);
                            sixMonthPrice = priceAmountMicros;
                            break;
                        case BillingManager.ONE_YEAR_SUBSCRIPTION_ID:
                            mOneYearSubscriptionView.setAlpha(subscribeViewAlpha);
                            mPriceOneYearTextView.setText(price);
                            oneYearPrice = priceAmountMicros;
                            break;
                    }
                }

                if (oneMonthPrice != 0 && sixMonthPrice != 0 && oneYearPrice != 0) {
                    float sixMonthReduction = 1 - ((sixMonthPrice / 6f) / oneMonthPrice);
                    int sixMonthReductionPercent = (int) (sixMonthReduction * 100);
                    float oneYearReduction = 1 - ((oneYearPrice / 12f) / oneMonthPrice);
                    int oneYearReductionPercent = (int) (oneYearReduction * 100);

                    mReductionSixMonthTextView.setText("-" + sixMonthReductionPercent + "%");
                    mReductionOneYearTextView.setText("-" + oneYearReductionPercent + "%");
                }
            }
        }

        // Ready to get existing purchases.
        if (fetchPurchases) {
            mBillingManager.fetchPurchases();
        }
    }

    private void updateViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateViews");
        }

        if (mProductDetailsSelected != null) {

            if (!mIsSubscribed) {
                mFreeTrialTextView.setAlpha(1f);
                mFooterTextView.setAlpha(1f);
                mLaterView.setAlpha(1f);
                mRestoreView.setAlpha(1f);
                mInviteView.setAlpha(1f);
                mSubscribedTextView.setAlpha(0f);
                mOneYearSubscriptionView.setAlpha(1f);
                mSixMonthSubscriptionView.setAlpha(1f);
                mOneMonthSubscriptionView.setAlpha(1f);
                mSubscribeTextView.setText(getString(R.string.in_app_subscription_activity_subscribe_title));
            } else {
                if (getTwinmeApplication().getInvitationSubscriptionTwincode() == null) {
                    mSubscribeTextView.setText(getString(R.string.in_app_subscription_activity_manage_susbcription));
                    mSubscribedTextView.setText(String.format(getString(R.string.in_app_subscription_activity_expiration_message), getRenewalSubscriptionDate()));
                    mSubscribedTextView.setAlpha(1f);
                } else {
                    mSubscribeTextView.setText(getString(R.string.in_app_subscription_activity_cancel_subscription));
                }

                mFreeTrialTextView.setAlpha(0f);
                mFooterTextView.setAlpha(0f);
                mLaterView.setAlpha(0f);
                mRestoreView.setAlpha(0f);
                mInviteView.setAlpha(0f);
            }

            mOneYearCardView.setAlpha(0.f);
            mSixMonthCardView.setAlpha(0.f);
            mOneMonthCardView.setAlpha(0.f);

            mOneMonthUnitView.setTextColor(Design.BLACK_COLOR);
            mSixMonthUnitView.setTextColor(Design.BLACK_COLOR);
            mOneYearUnitView.setTextColor(Design.BLACK_COLOR);

            mOneMonthDurationView.setTextColor(Design.BLACK_COLOR);
            mSixMonthDurationView.setTextColor(Design.BLACK_COLOR);
            mOneYearDurationView.setTextColor(Design.BLACK_COLOR);

            mPriceOneMonthTextView.setTextColor(Design.BLACK_COLOR);
            mPriceSixMonthTextView.setTextColor(Design.BLACK_COLOR);
            mPriceOneYearTextView.setTextColor(Design.BLACK_COLOR);

            switch (mProductDetailsSelected.getProductId()) {
                case BillingManager.ONE_MONTH_SUBSCRIPTION_ID:
                    mOneMonthCardView.setAlpha(1.f);
                    if (mIsSubscribed) {
                        mOneMonthSubscriptionView.setAlpha(1f);
                        mOneYearSubscriptionView.setAlpha(0.5f);
                        mSixMonthSubscriptionView.setAlpha(0.5f);
                    }
                    if (Design.isDarkMode(this)) {
                        mPriceOneMonthTextView.setTextColor(Color.BLACK);
                        mOneMonthDurationView.setTextColor(Color.BLACK);
                        mOneMonthUnitView.setTextColor(Color.BLACK);
                    }
                    break;
                case BillingManager.SIX_MONTHS_SUBSCRIPTION_ID:
                    mSixMonthCardView.setAlpha(1.f);
                    if (mIsSubscribed) {
                        mSixMonthSubscriptionView.setAlpha(1f);
                        mOneYearSubscriptionView.setAlpha(0.5f);
                        mOneMonthSubscriptionView.setAlpha(0.5f);
                    }
                    if (Design.isDarkMode(this)) {
                        mPriceSixMonthTextView.setTextColor(Color.BLACK);
                        mSixMonthDurationView.setTextColor(Color.BLACK);
                        mSixMonthUnitView.setTextColor(Color.BLACK);
                    }
                    break;
                case BillingManager.ONE_YEAR_SUBSCRIPTION_ID:
                    mOneYearCardView.setAlpha(1.f);
                    if (mIsSubscribed) {
                        mOneYearSubscriptionView.setAlpha(1f);
                        mOneMonthSubscriptionView.setAlpha(0.5f);
                        mSixMonthSubscriptionView.setAlpha(0.5f);
                    }

                    if (Design.isDarkMode(this)) {
                        mPriceOneYearTextView.setTextColor(Color.BLACK);
                        mOneYearDurationView.setTextColor(Color.BLACK);
                        mOneYearUnitView.setTextColor(Color.BLACK);
                    }
                    break;
            }
        }
    }

    private void nextDescription() {
        if (DEBUG) {
            Log.d(LOG_TAG, "nextDescription");
        }

        mDescriptionStep++;

        if (mDescriptionStep > 4) {
            mDescriptionStep = 1;
            mProgressBarOneView.resetAnimation();
            mProgressBarTwoView.resetAnimation();
            mProgressBarThreeView.resetAnimation();
            mProgressBarFourView.resetAnimation();
        }

        int title;
        int description;
        int drawable;
        int imageBottomMargin = (int) (DESIGN_IMAGE_BOTTOM_MARGIN * Design.HEIGHT_RATIO);
        if (mDescriptionStep == 1) {
            mProgressBarOneView.startAnimation();
            title = R.string.in_app_subscription_activity_description_step1_title;
            description = R.string.in_app_subscription_activity_description_step1_subtitle;
            drawable = R.drawable.in_app_step_one;
            imageBottomMargin = 0;
        } else if (mDescriptionStep == 2) {
            mProgressBarTwoView.startAnimation();
            title = R.string.in_app_subscription_activity_description_step2_title;
            description = R.string.in_app_subscription_activity_description_step2_subtitle;
            drawable = R.drawable.in_app_step_two;
        } else if (mDescriptionStep == 3) {
            mProgressBarThreeView.startAnimation();
            title = R.string.in_app_subscription_activity_description_step3_title;
            description = R.string.in_app_subscription_activity_description_step3_subtitle;
            drawable = R.drawable.in_app_step_three;
        } else {
            mProgressBarFourView.startAnimation();
            title = R.string.in_app_subscription_activity_description_step4_title;
            description = R.string.in_app_subscription_activity_description_step4_subtitle;
            drawable = R.drawable.in_app_step_four;
        }

        mTitleView.setText(getString(title));
        mDescriptionView.setText(getString(description));

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mDescriptionImageView.getLayoutParams();
        marginLayoutParams.bottomMargin = imageBottomMargin;
        mDescriptionImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), drawable, null));
    }

    private void addLinks() {
        if (DEBUG) {
            Log.d(LOG_TAG, "addLinks");
        }

        addLinks(mFooterTextView, getString(R.string.welcome_activity_terms_of_use), "file:///android_res/raw/terms_of_service.html");
        addLinks(mFooterTextView, getString(R.string.welcome_activity_privacy_policy), "file:///android_res/raw/privacy_policy.html");
        mFooterTextView.setMovementMethod(new TextViewLinkHandler() {

        });
    }

    private static void addLinks(TextView textView, String link, String scheme) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addLinks textView=" + textView + " link=" + link + " scheme=" + scheme);
        }

        Pattern pattern = Pattern.compile(link);
        android.text.util.Linkify.addLinks(textView, pattern, scheme, (s, start, end) -> true, (match, url) -> "");
    }

    private String getRenewalSubscriptionDate() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getRenewalSubscriptionDate");
        }

        String renewal = "";

        if (mPurchase != null && !mPurchase.getProducts().isEmpty()) {

            String productId = mPurchase.getProducts().get(0);

            long expirationTime = mPurchase.getPurchaseTime();
            Date date = new Date(expirationTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            if (productId.equals(BillingManager.ONE_MONTH_SUBSCRIPTION_ID)) {
                calendar.add(Calendar.MONTH, 1);
            } else if (productId.equals(BillingManager.SIX_MONTHS_SUBSCRIPTION_ID)) {
                calendar.add(Calendar.MONTH, 6);
            } else {
                calendar.add(Calendar.YEAR, 1);
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            renewal = simpleDateFormat.format(calendar.getTime());
        }

        return renewal;
    }
}