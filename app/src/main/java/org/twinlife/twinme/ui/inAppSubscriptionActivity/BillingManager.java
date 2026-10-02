/*
 *  Copyright (c) 2022-2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.inAppSubscriptionActivity;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.AccountService;
import org.twinlife.twinlife.ErrorCode;
import org.twinlife.twinme.iap.BillingProvider;
import org.twinlife.twinme.iap.BillingProviderFactory;
import org.twinlife.twinme.iap.BillingProviderListener;
import org.twinlife.twinme.iap.ProductDetails;
import org.twinlife.twinme.iap.Purchase;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;

import java.util.ArrayList;
import java.util.List;

public class BillingManager implements BillingProviderListener {
    private static final String LOG_TAG = "BillingManager";
    private static final boolean DEBUG = false;

    public interface BillingManagerListener {

        void onGetProducts(List<ProductDetails> productDetails);

        void onGetCurrentSubscription(Purchase purchase, AccountService.MerchantIdentification merchantIdentification);

        void onGetCurrentSubscriptionNotFound();

        void onPurchaseSuccess(Purchase purchase, AccountService.MerchantIdentification merchantIdentification);

        void onPurchaseFailed();

        void onSetupFinishedInError(@NonNull ErrorCode errorCode);
    }

    @NonNull
    private final List<ProductDetails> mProductList = new ArrayList<>();

    @NonNull
    private final AbstractTwinmeActivity mInAppSubscriptionActivity;
    @NonNull
    private final BillingManagerListener mBillingManagerListener;
    @NonNull
    private final BillingProvider mBillingProvider;

    public BillingManager(@NonNull AbstractTwinmeActivity inAppSubscriptionActivity, @NonNull BillingManagerListener billingManagerListener) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: " + inAppSubscriptionActivity);
        }

        mInAppSubscriptionActivity = inAppSubscriptionActivity;
        mBillingManagerListener = billingManagerListener;

        mBillingProvider = BillingProviderFactory.getInstance(mInAppSubscriptionActivity, this);
    }

    /**
     * Release the billing manager and disconnect.
     */
    public void dispose() {
        if (DEBUG) {
            Log.d(LOG_TAG, "dispose");
        }

        mBillingProvider.dispose();
    }

    public void fetchPurchases() {
        if (DEBUG) {
            Log.d(LOG_TAG, "fetchPurchases");
        }

        if (mProductList.isEmpty()) {
            return;
        }

        mBillingProvider.fetchPurchases((result, purchases) -> {
            if (result == ErrorCode.SUCCESS && purchases != null) {
                for (Purchase purchase : purchases) {
                    if (isPurchaseInProductList(purchase) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                        mInAppSubscriptionActivity.runOnUiThread(() -> mBillingManagerListener.onGetCurrentSubscription(purchase, mBillingProvider.getMerchantIdentification()));
                        return;
                    }
                }
            }
            mInAppSubscriptionActivity.runOnUiThread(mBillingManagerListener::onGetCurrentSubscriptionNotFound);
        });
    }

    public void subscribeToProductId(@NonNull String productId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "subscribeToProductId: " + productId);
        }

        mBillingProvider.subscribeToProduct(productId);
    }

    public void manageSubscription(@NonNull String productId, @Nullable String packageName) {
        if (DEBUG) {
            Log.d(LOG_TAG, "manageSubscription: productId=" + productId + " packageName=" + packageName);
        }

        mBillingProvider.manageSubscription(productId, packageName);
    }

    public void handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "handleActivityResult: requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }
        mBillingProvider.handleActivityResult(requestCode, resultCode, data);
    }

    //
    // Implement BillingProvider methods
    //

    @Override
    public void onPurchasesUpdated(@NonNull ErrorCode result, @Nullable List<Purchase> purchases) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPurchasesUpdated: result=" + result + " purchases=" + purchases);
        }

        mInAppSubscriptionActivity.runOnUiThread(() -> {
            if (result == ErrorCode.SUCCESS && purchases != null) {
                for (Purchase purchase : purchases) {
                    mBillingManagerListener.onPurchaseSuccess(purchase, mBillingProvider.getMerchantIdentification());
                }
            } else if (result == ErrorCode.CANCELED_OPERATION) {
                mBillingManagerListener.onPurchaseFailed();
            } else {
                mBillingManagerListener.onSetupFinishedInError(result);
            }
        });
    }

    @Override
    public void onBillingSetupFinished(@NonNull ErrorCode result) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBillingSetupFinished: result=" + result);
        }

        if (result == ErrorCode.SUCCESS) {
            mBillingProvider.fetchSubscriptions();
        } else {
            mInAppSubscriptionActivity.runOnUiThread(() -> mBillingManagerListener.onSetupFinishedInError(result));
        }
    }

    @Override
    public void onProductDetailsResponse(@NonNull ErrorCode result, @Nullable List<ProductDetails> productDetails) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onProductDetailsResponse: result=" + result + " productDetails=" + productDetails);
        }

        if (result == ErrorCode.SUCCESS && productDetails != null) {
            mProductList.clear();
            mProductList.addAll(productDetails);
            mInAppSubscriptionActivity.runOnUiThread(() -> mBillingManagerListener.onGetProducts(mProductList));
        } else {
            mInAppSubscriptionActivity.runOnUiThread(() -> mBillingManagerListener.onSetupFinishedInError(result));
        }
    }

    //
    // Implement Private methods
    //

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
