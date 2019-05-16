package hackman.trevor.copycat.standard;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import hackman.trevor.tlibrary.library.TLogging;

import static com.google.android.gms.ads.AdRequest.ERROR_CODE_NETWORK_ERROR;
import static hackman.trevor.tlibrary.library.TLogging.flog;
import static hackman.trevor.tlibrary.library.TLogging.log;
import static hackman.trevor.tlibrary.library.TLogging.report;

public enum Ads {;
    private static InterstitialAd interstitialAd;
    private static Runnable onClosedEvent; // For when application needs to detect when the interstitial ad is closed
    private static RewardedVideoAd rewardedVideoAd;
    private static Runnable onReward; // Called onClose if rewarded
    private static boolean rewarded = false; // Records if entire video was watched

    // Initialize Ads, call in activity's onCreate
    public static void initializeMobileAds(Context context) {
        // Unsure of purpose of MobileAds.initialize(), things seem to work fine w/o it. AdMob guide recommends it but doesn't explain what it does in the slightest
        MobileAds.initialize(context, "ca-app-pub-9667393179892638~7004321704");

        log(MobileAds.getInitializationStatus());
    }

    public static void initializeInterstitial(Context context) {
        setupInterstitial(context);
        Ads.requestNewInterstitial();
    }

    public static void initializeRewarded(Context context) {
        setupRewardedVideo(context);
        Ads.requestNewRewardedVideo();
    }

    private static void setupInterstitial(Context context) {
        interstitialAd = new InterstitialAd(context);

        // If testing use test ad, else use real ad
        if (TLogging.TESTING) {
            interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712"); // AdMob's dedicated test ad unit ID for Android interstitial ads
        }
        else {
            interstitialAd.setAdUnitId("ca-app-pub-9667393179892638/5508512556"); // For real ads
        }

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                log("iClosed");
                if (onClosedEvent != null) onClosedEvent.run();
                requestNewInterstitial();
            }

            @Override
            public void onAdLoaded() {
                log("iLoaded");
            }
        });
    }

    // If ad is loaded, random chance to display
    // Else attempt to load a new ad
    // Returns whether or not ad is shown
    public static boolean rollAdDisplay(double chance, Runnable onClosedEvent) {
        if (interstitialAd.isLoaded()) {
            double rollForAd = Math.random(); // Double in range [0.0, 1.0)
            if (rollForAd < chance) {
                flog("Ad shown");

                Ads.onClosedEvent = onClosedEvent;
                interstitialAd.show();
                return true;
            }
        }
        // If not loaded and Ad never began loading (connection issues?), try loading Ad
        else {
            log("iNotLoaded");
            if (!interstitialAd.isLoading()) requestNewInterstitial();
        }
        return false;
    }

    // Attempt to load a new interstitial ad
    // Can call this in onCreate (no Billing) or in onServiceConnected (Wait for billing response)
    public static void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
    }

    private static void setupRewardedVideo(Context context) {
        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context);
        rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            private int loadCounter = 0; // Useful to track
            @Override public void onRewardedVideoAdLoaded() {
                log("rvLoaded " + loadCounter++);
                failCounter = 0;
            }

            @Override public void onRewardedVideoAdOpened() {
                log("rvOpened");
                requestNewRewardedVideo(); // It seems there's no issues with loading a new video ad before the current one is even finished
            }

            @Override public void onRewardedVideoStarted() {
                log("rvVideoStarted");
            }

            // Invoked when the Rewarded Video Ad has finished playing
            @Override public void onRewardedVideoCompleted() {
                log("rvVideoCompleted");
                rewarded = true;
            }

            // Invoked when the SDK has been triggered by the server and the configured Reward has been received by the SDK
            // Since I don't care about the configured rewardItem, better to listen to onRewardedVideoCompleted
            @Override public void onRewarded(RewardItem rewardItem) {}

            // Only called if you click on the ad and are taken away by the ad. Not called by normal leaves like hitting home button.
            @Override public void onRewardedVideoAdLeftApplication() {
                log("rvLeftApplication");
            }

            // Typically called on return to app but ALSO called if video is completed and leave application (click on ad). So doesn't always work to detect when return to app.
            // TO?DO Make a work around for always detecting when return to app
            @Override public void onRewardedVideoAdClosed() {
                log("rvVideoAdClosed " + (rewarded ? "rewarded" : "not rewarded"));
                if (rewarded) {
                    if (onReward != null) onReward.run();
                    rewarded = false;
                }
            }

            private int failCounter = 0; // Limit how much logs get spammed with this message
            // Error codes at https://developers.google.com/android/reference/com/google/android/gms/ads/AdListener#onAdFailedToLoad(int)
            @Override public void onRewardedVideoAdFailedToLoad(int errorCode) {
                if (failCounter++ < 10) {
                    flog("Rewarded Video Ad Failed To Load : " + errorCode);
                }

                // Errors that aren't from user lacking connection are problematic
                if (failCounter == 10 && errorCode != ERROR_CODE_NETWORK_ERROR) report("Failing a lot at loading rv with error code : " + errorCode);

                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        requestNewRewardedVideo(); // A 10 second delay before attempting again - no need to clog with constant requests
                    }
                }, 10000);
            }
        });
    }

    // Attempt to load a new rewardedVideo ad
    // Can call this in onCreate (no Billing) or in onServiceConnected (Wait for billing response)
    public static void requestNewRewardedVideo() {
        // If testing use test ad, else use real ad
        if (TLogging.TESTING) {
            rewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", // AdMob's dedicated test ad unit ID for Android rewardedVideo
                    new AdRequest.Builder().build());
        }
        else {
            rewardedVideoAd.loadAd("ca-app-pub-9667393179892638/9267894166", // For real ads
                    new AdRequest.Builder().build());
        }
    }

    // Play a rewarded video ad if loaded
    // Returns true if loaded; false if not loaded
    public static boolean playRewardedVideoAd(Runnable onReward) {
        Ads.onReward = onReward;
        if (rewardedVideoAd.isLoaded()) {
            rewardedVideoAd.show();
            return true;
        }
        return false;
    }
}
