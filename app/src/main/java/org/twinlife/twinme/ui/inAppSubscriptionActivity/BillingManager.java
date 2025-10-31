/*
 *  Copyright (c) 2022-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.ui.inAppSubscriptionActivity;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BillingManager implements PurchasesUpdatedListener, BillingClientStateListener, ProductDetailsResponseListener {
    private static final String LOG_TAG = "BillingManager";
    private static final boolean DEBUG = false;

    protected static final String ONE_MONTH_SUBSCRIPTION_ID = "skred.subscription.one_month_auto_renew";
    protected static final String SIX_MONTHS_SUBSCRIPTION_ID = "skred.subscription.six_month_auto_renew";
    protected static final String ONE_YEAR_SUBSCRIPTION_ID = "skred.subscription.1_year_auto_renew";

    public interface BillingManagerListener {

        void onGetProducts(List<ProductDetails> productDetails);

        void onGetCurrentSubscription(Purchase purchase);

        void onGetCurrentSubscriptionNotFound();

        void onPurchaseSuccess(Purchase purchase);

        void onPurchaseFailed();

        void onSetupFinishedInError(int errorCode);
    }

    private final BillingClient mBillingClient;
    private final List<ProductDetails> mProductList = new ArrayList<>();

    private final AbstractTwinmeActivity mInAppSubscriptionActivity;
    private final BillingManagerListener mBillingManagerListener;

    public BillingManager(AbstractTwinmeActivity inAppSubscriptionActivity, BillingManagerListener billingManagerListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: " + inAppSubscriptionActivity);
        }

        mInAppSubscriptionActivity = inAppSubscriptionActivity;
        mBillingManagerListener = billingManagerListener;

        mBillingClient = BillingClient.newBuilder(mInAppSubscriptionActivity.getApplicationContext())
                .setListener(this)
                .enablePendingPurchases()
                .build();

        mBillingClient.startConnection(this);
    }

    /**
     * Release the billing manager and disconnect.
     */
    public void dispose() {
        if (DEBUG) {
            Log.d(LOG_TAG, "dispose");
        }

        if (mBillingClient.isReady()) {
            mBillingClient.endConnection();
        }
    }

    public void fetchPurchases() {
        if (DEBUG) {
            Log.d(LOG_TAG, "fetchPurchases");
        }

        if (mProductList.isEmpty()) {
            return;
        }

        PurchasesResponseListener purchasesResponseListener = (billingResult, purchases) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                for (Purchase purchase : purchases) {
                    if (isPurchaseInProductList(purchase) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                        mInAppSubscriptionActivity.runOnUiThread(() -> mBillingManagerListener.onGetCurrentSubscription(purchase));
                        return;
                    }
                }
            }
            mInAppSubscriptionActivity.runOnUiThread(mBillingManagerListener::onGetCurrentSubscriptionNotFound);
        };

        mBillingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),purchasesResponseListener);
    }

    //
    // Implement PurchasesUpdatedListener methods
    //

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPurchasesUpdated: " + billingResult + " list = " + purchases);
        }

        mInAppSubscriptionActivity.runOnUiThread(() -> {
            final int responseCode = billingResult.getResponseCode();
            if (responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (Purchase purchase : purchases) {
                    mBillingManagerListener.onPurchaseSuccess(purchase);
                }
            } else if (responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                mBillingManagerListener.onPurchaseFailed();
            } else {
                mBillingManagerListener.onSetupFinishedInError(responseCode);
            }
        });
    }

    //
    // Implement BillingClientStateListener methods
    //

    @Override
    public void onBillingServiceDisconnected() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBillingServiceDisconnected");
        }

        // Try to restart the connection on the next request to
        // Google Play by calling the startConnection() method.
    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBillingSetupFinished: " + billingResult);
        }

        final int responseCode = billingResult.getResponseCode();
        if (responseCode ==  BillingClient.BillingResponseCode.OK) {
            getSubscriptions();
        } else {
            mInAppSubscriptionActivity.runOnUiThread(() -> mBillingManagerListener.onSetupFinishedInError(responseCode));
        }
    }

    //
    // Implement ProductDetailsResponseListener methods
    //

    @Override
    public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onProductDetailsResponse: " + billingResult + " list = " + list);
        }

        final int responseCode = billingResult.getResponseCode();
        if (responseCode ==  BillingClient.BillingResponseCode.OK) {
            mProductList.clear();
            mProductList.addAll(list);
            mInAppSubscriptionActivity.runOnUiThread(() -> mBillingManagerListener.onGetProducts(mProductList));
        } else {
            mInAppSubscriptionActivity.runOnUiThread(() -> mBillingManagerListener.onSetupFinishedInError(responseCode));
        }
    }

    public void subscribeToProductId(String productId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "subscribeToProductId: " + productId);
        }

        for (ProductDetails productDetails : mProductList) {
            if (productDetails.getProductId().equals(productId)) {
                subscribe(productDetails);
                break;
            }
        }
    }

    //
    // Implement Private methods
    //

    private void getSubscriptions() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getSubscriptions");
        }

        List<String> productIdList = Arrays.asList(ONE_MONTH_SUBSCRIPTION_ID, SIX_MONTHS_SUBSCRIPTION_ID, ONE_YEAR_SUBSCRIPTION_ID);
        ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();

        for(String productId : productIdList) {
            QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build();
            productList.add(product);
        }

        QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(productList).build();

        mBillingClient.queryProductDetailsAsync(queryProductDetailsParams,this);
    }

    private void subscribe(ProductDetails productDetails) {
        if (DEBUG) {
            Log.d(LOG_TAG, "subscribe: " + productDetails);
        }

        if (productDetails.getSubscriptionOfferDetails() != null) {

            String offerToken = null;
            for (ProductDetails.SubscriptionOfferDetails subscriptionOfferDetails : productDetails.getSubscriptionOfferDetails()) {
                if (subscriptionOfferDetails.getPricingPhases().getPricingPhaseList().size() == 2) {
                    // 2 phases => free trial then regular subscription, this is the one we want.
                    offerToken = subscriptionOfferDetails.getOfferToken();
                    break;
                }
            }

            if (offerToken == null) {
                // No free trial offer found, but we should have at least the regular subscription.
                offerToken = productDetails.getSubscriptionOfferDetails().get(0).getOfferToken();
            }

            List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();

            BillingFlowParams.ProductDetailsParams productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build();
            productDetailsParamsList.add(productDetailsParams);

            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build();

            mBillingClient.launchBillingFlow(mInAppSubscriptionActivity, billingFlowParams);
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (DEBUG) {
            Log.d(LOG_TAG, "handlePurchase: " + purchase);
        }

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = billingResult -> mInAppSubscriptionActivity.runOnUiThread(() -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        mBillingManagerListener.onPurchaseSuccess(purchase);
                    } else {
                        mBillingManagerListener.onPurchaseFailed();
                    }
                });

                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            } else {
                mInAppSubscriptionActivity.runOnUiThread(mBillingManagerListener::onPurchaseFailed);
            }
        }
    }

    private boolean isPurchaseInProductList(Purchase purchase) {
        if (DEBUG) {
            Log.d(LOG_TAG, "isPurchaseInProductList: " + purchase);
        }

        if (mProductList.isEmpty() || purchase.getProducts().isEmpty()) {
            return false;
        }

        for (ProductDetails productDetails : mProductList) {
            if (productDetails.getProductId().equals(purchase.getProducts().get(0))) {
                return true;
            }
        }

        return false;
    }
}
