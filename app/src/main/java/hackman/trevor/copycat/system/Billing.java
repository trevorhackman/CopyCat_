package hackman.trevor.copycat.system;

// There's two major reasons why billing may not work. No network connection or Google Play Store (required) not installed/force stopped/disabled/updating.
// Only returning a network error dialog for now. Could improve in the future to detect if Google Play Store is installed and return a different error dialog for that. But I expect 99% of app-users have Google Play.

// TODO Wait to see if I get any reports of unacknowledged-purchases. Can partially handle it, but don't want the code complexity if it isn't a problem that actually happens.

import android.os.Handler;

import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

import hackman.trevor.copycat.MainActivity;

import static hackman.trevor.tlibrary.library.TLogging.flog;
import static hackman.trevor.tlibrary.library.TLogging.log;
import static hackman.trevor.tlibrary.library.TLogging.report;

// Handles all things related to billing for in-app purchases.
// Google documentation (garbage documentation) : https://developer.android.com/google/play/billing/billing_library_overview
public enum Billing {;
    private static BillingClient billingClient; // The billing client. All billing queries happen through it.
    private static PurchasesUpdatedListener purchasesUpdatedListener; // Called when a purchase is made, cancelled, or fails
    private static BillingClientStateListener billingClientStateListener; // Called when connection is made or lost to Google Play Store installed on device
    private static Handler reconnectHandler = new Handler(); // For reconnecting to Google Play Store if we ever disconnect

    // Storing app-specific product id strings here. Ideally store in back-end server and query for security but I don't have a back-end and don't care much about security.
    private static final String NO_ADS = "no_ads"; // The only product. Permanently removes ads.

    // Call this in MainActivity
    public static void startConnection(final MainActivity main) {
        // Listen to result of purchase flow
        if (purchasesUpdatedListener == null) { // Don't recreate on reconnections
            purchasesUpdatedListener = new PurchasesUpdatedListener() {
                @Override
                public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
                    // Handle successful purchase here
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        flog("Purchase made : " + purchases);

                        // Purchases shouldn't be null on OK response
                        if (purchases != null) {
                            // Not sure why there would be more than one purchase updated at a time, but to be thorough we'll loop through the whole list
                            // Acknowledge all purchases immediately. Consuming a purchase also works to acknowledge.
                            for (Purchase purchase : purchases) {
                                if (purchase.getSku().equals(NO_ADS)) {
                                    // Gonna do this regardless of acknowledgement for now. Not sure what to do if acknowledgement fails
                                    main.tPreferences().putBoolean(Keys.isNoAdsOwned, true); // Update Key
                                    // if (main.screenManager.getScreen() instanceof StartScreen) ((StartScreen)main.screenManager.getScreen()).removeNoAdsButton(); // Update UI

                                    AcknowledgePurchaseResponseListener listener = new AcknowledgePurchaseResponseListener() {
                                        @Override
                                        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                                Dialogs.successfulNoAdsPurchase(main);
                                            }
                                            else {
                                                // I hope this doesn't happen. Not sure what to do since I believe user already paid, but acknowledgement is required
                                                // Failure to acknowledge results in purchases being refunded
                                                report("Failed to acknowledge : " + billingResponseToName(billingResult) + " : " + billingResult.getResponseCode());
                                            }
                                        }
                                    };

                                    AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, listener);
                                }
                                // Unknown purchase, shouldn't happen
                                else {
                                    report("This shouldn't happen. Unknown purchase? : " + purchase.getSku() + " : " + purchase.getPurchaseToken());
                                    Dialogs.unknownError(main, "Unknown item purchased. You will be refunded.");
                                }
                            }
                        }
                        else {
                            // purchases should never be null on OK billing response
                            report("This shouldn't happen. purchases is null on OK response : " + billingClient.isReady());
                            Dialogs.unknownError(main, "No purchase found");
                        }

                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED){
                        flog("User cancelled purchase");
                    }
                    else {
                        report("Unsuccessful purchase : " + billingResponseToName(billingResult) + " : " + billingResult.getDebugMessage()); // May remove this report in the future
                        Dialogs.failedNetwork(main);
                    }
                }
            };
        }

        if (billingClientStateListener == null) {
            billingClientStateListener = new BillingClientStateListener() {
                @Override
                // This is NOT a network connection listener. Can return OK when no connection. Merely connects with Google Play Store installed on device.
                public void onBillingSetupFinished(BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) { // The billing client is ready
                        flog("startConnection succeeded");
                        reconnectHandler.removeCallbacksAndMessages(null); // Stops attempting to reconnect
                        queryPurchases(main);
                    } else {
                        flog("startConnection failed : " + billingResponseToName(billingResult) + " : " + billingResult.getDebugMessage());
                        attemptReconnect();
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    // Called if Google Play Store is force stopped or otherwise no longer running
                    flog("Billing Service Disconnected");
                    attemptReconnect();
                }
            };
        }

        billingClient = BillingClient.newBuilder(main).enablePendingPurchases().setListener(purchasesUpdatedListener).build();
        billingClient.startConnection(billingClientStateListener);
    }

    // Periodically attempt to reconnect on a 10 second delay
    // Make sure to cancel reconnection attempts on reconnect
    private static void attemptReconnect() {
        reconnectHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                flog("Attempting to reconnect");
                billingClient.startConnection(billingClientStateListener);
            }
        }, 10000); // 10 second delay
    }

    // Make a non-network check to Google Play Store's cache for what purchases have been made
    private static void queryPurchases(final MainActivity main) {
        Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
        List<Purchase> purchases = purchasesResult.getPurchasesList();
        if (purchases == null) report("Why is purchases null? : " + billingResponseToName(purchasesResult) + " " + purchasesResult.getBillingResult().getDebugMessage());
        else if (purchases.size() == 0) flog("No purchases owned");
        else {
            for (Purchase purchase : purchases) {
                if (purchase.getSku().equals(NO_ADS)) {
                    // Gonna do this regardless of acknowledgement for now
                    main.tPreferences().putBoolean(Keys.isNoAdsOwned, true); // Update Key
                    // if (main.screenManager.getScreen() instanceof StartScreen) ((StartScreen)main.screenManager.getScreen()).removeNoAdsButton(); // Update UI
                    flog(NO_ADS + " owned");
                }
                else {
                    report("This shouldn't happen. Unknown queryPurchase result?: " + purchase.getSku() + " : " + purchase.getPurchaseToken());
                }
            }
        }
    }

    // Call to present Google's purchase dialog
    // User may make the purchase, cancel, or fail to make purchase
    public static void purchaseFlow(final MainActivity main) {
        List<String> skuList = new ArrayList<>();
        skuList.add(NO_ADS);
        SkuDetailsParams params = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(BillingClient.SkuType.INAPP).build();

        // SkuDetails needed for purchaseFlow. Details of products (NOT purchase status).
        // SkuDetails: {"skuDetailsToken":"AEuhp4KoNhEA0jK_ZECEFnjMdwU39kgxqco36TPkUUyFn-7IFiUh75LG5k0uOapsLRN8","productId":"no_ads","type":"inapp","price":"$1.99","price_amount_micros":1990000,"price_currency_code":"USD","title":"Remove Ads (Block Slider - Unblock Puzzle)","description":"One time purchase to permanently remove ads and support the developer!"}
        billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                    SkuDetails noAds = null;
                    for (SkuDetails skuDetails : skuDetailsList) {
                        if (skuDetails.getSku().equals(NO_ADS)) {
                            noAds = skuDetails;
                        }
                        else {
                            report("This shouldn't happen. Unknown querySkuDetailsAsync result?: " + skuDetails.getSku() + " : " + skuDetails);
                        }
                    }

                    if (noAds != null) billingClient.launchBillingFlow(main, BillingFlowParams.newBuilder().setSkuDetails(noAds).build()); // Begin billing flow
                    else {
                        // Connected but couldn't find product details. Should never happen.
                        report("This shouldn't happen. Couldn't get no_ads skuDetails : " + skuDetailsList);
                        Dialogs.unknownError(main, "Couldn't find noAds Sku");
                    }
                }
                else {
                    flog("querySkuDetailsAsync failed : " + billingResponseToName(billingResult) + " : " + billingResult.getDebugMessage() + " : " + billingClient.isReady());
                    Dialogs.failedNetwork(main);
                }
            }
        });
    }

    // Gets the name of the error code
    private static String billingResponseToName(int billingResponseCode) {
        switch (billingResponseCode) {
            case 0:
                return "OK";
            case 1:
                return "USER_CANCELLED";
            case 2:
                return "SERVICE_UNAVAILABLE";
            case 3:
                return "BILLING_UNAVAILABLE";
            case 4:
                return "ITEM_UNAVAILABLE";
            case 5:
                return "DEVELOPER_ERROR";
            case 6:
                return "ERROR";
            case 7:
                return "ITEM_ALREADY_OWNED";
            case 8:
                return "ITEM_NOT_OWNED";
            case -1:
                return "SERVICE_DISCONNECTED";
            case -2:
                return "FEATURE_NOT_SUPPORTED";

        }
        report("ERROR : Invalid response code");
        return "ERROR : Invalid response code";
    }

    // Overload
    private static String billingResponseToName(BillingResult billingResult) {
        return billingResponseToName(billingResult.getResponseCode());
    }

    // Overload
    private static String billingResponseToName(Purchase.PurchasesResult purchasesResult) {
        return billingResponseToName(purchasesResult.getBillingResult());
    }

    // For testing, reset purchases.
    @SuppressWarnings("unused")
    public static void forTestingConsumeAllPurchases(MainActivity main) {
        // Get purchases if any
        List<Purchase> purchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP).getPurchasesList();
        if (purchases == null) log("Purchases is null. Has Billing Client connected yet?");
        else if (purchases.size() == 0) log("No purchases to consume");
        else {
            for (Purchase purchase : purchases) {
                ConsumeParams consumeParams = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

                ConsumeResponseListener listener = new ConsumeResponseListener() {
                    @Override
                    public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            log("Consumed purchase successfully : " + purchaseToken);
                        } else {
                            log("Consume failed : " + billingResponseToName(billingResult) + " : " + billingResult.getDebugMessage());
                        }
                    }
                };

                billingClient.consumeAsync(consumeParams, listener);
                log("Consuming " + purchase.getSku());
            }
        }

        // Reset Keys
        main.tPreferences().putBoolean(Keys.isNoAdsOwned, false);
    }
}
