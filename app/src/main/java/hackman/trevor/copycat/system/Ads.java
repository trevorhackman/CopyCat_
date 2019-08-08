package hackman.trevor.copycat.system;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
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

public enum Ads implements LifecycleObserver {Observer;
    private static InterstitialAd interstitialAd;
    private static Runnable onClosedEvent; // For when application needs to detect when the interstitial ad is closed

    private static AdView bannerAd;

    private static RewardedVideoAd rewardedVideoAd;
    private static Runnable onReward; // Called onClose if rewarded
    private static boolean rewarded = false; // Records if entire video was watched

    private static Handler handler; // For scheduling reloading

    // Initialize Ads, call in activity's onCreate
    public static void initializeMobileAds(AppCompatActivity context) {
        // Unsure of purpose of MobileAds.initialize(), things seem to work fine w/o it. AdMob guide recommends it but doesn't explain what it does in the slightest
        MobileAds.initialize(context, "ca-app-pub-9667393179892638~7004321704");

        // With this we don't have to modify our main activity to make this class lifecycle-aware
        context.getLifecycle().addObserver(Observer);
    }

    public static void initializeInterstitial(Context context) {
        setupInterstitial(context);
        requestNewInterstitial();
    }

    public static void initializeBanner(Context context) {
        setupBanner(context);
        requestNewBanner();
    }

    public static void initializeRewarded(Activity activity) {
        setupRewardedVideo(activity);
        Ads.requestNewRewardedVideo();
    }

    private static void setupInterstitial(Context context) {
        interstitialAd = new InterstitialAd(context);

        // If testing use test ad, else use real ad
        if (TLogging.TESTING) {
            interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712"); // AdMob's dedicated test ad unit ID for Android interstitial ads
        }
        else {
            interstitialAd.setAdUnitId("ca-app-pub-9667393179892638/3352851301"); // For real ads
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

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                if (handler == null) handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        requestNewInterstitial(); // A 10 second delay before attempting again - no need to clog with constant requests
                    }
                }, 10000);
            }
        });
    }

    private static void setupBanner(Context context) {
        bannerAd = new AdView(context);
        bannerAd.setAdSize(AdSize.SMART_BANNER);

        // If testing use test ad, else use real ad
        if (TLogging.TESTING) {
            bannerAd.setAdUnitId("ca-app-pub-3940256099942544/6300978111"); // AdMob's dedicated test ad unit ID for Android banners
        }
        else {
            bannerAd.setAdUnitId("ca-app-pub-9667393179892638/9441979869"); // For real ads
        }

        bannerAd.setAdListener(new AdListener() {
            @Override
            public void onAdOpened() {
                super.onAdOpened();
                requestNewBanner(); // Get new ad to interact with after current ad already has been
            }

            private boolean firstLoad = true;
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                if (firstLoad) {
                    log("b Ad first load");
                    firstLoad = false;
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                if (handler == null) handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        requestNewBanner(); // A 10 second delay before attempting again - no need to clog with constant requests
                    }
                }, 10000);
            }
        });
    }

    // "It's important to use an Activity context instead of an Application context when calling MobileAds.getRewardedVideoAdInstance()" - https://developers.google.com/admob/android/rewarded-video
    private static void setupRewardedVideo(Activity activity) {
        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activity);
        rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            private int loadCounter = 0; // Useful to track
            @Override public void onRewardedVideoAdLoaded() {
                log("rvLoaded " + loadCounter++);
                failCounter = 0;
            }

            @Override public void onRewardedVideoAdOpened() {
                log("rvOpened");
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

                // Was calling this in onRewardedVideoAdOpened() but believe loading a new ad early was causing an issue where ads may crash mid-play
                requestNewRewardedVideo();
            }

            private int failCounter = 0; // Limit how much logs get spammed with this message
            // Error codes at https://developers.google.com/android/reference/com/google/android/gms/ads/AdListener#onAdFailedToLoad(int)
            @Override public void onRewardedVideoAdFailedToLoad(int errorCode) {
                if (failCounter++ < 10) {
                    flog("Rewarded Video Ad Failed To Load : " + rewardedErrorCodeToString(errorCode));
                }

                // Errors that aren't from user lacking connection are problematic
                if (failCounter == 8 && errorCode != ERROR_CODE_NETWORK_ERROR) report("Failing a lot at loading rv with error : " + rewardedErrorCodeToString(errorCode));

                if (handler == null) handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        requestNewRewardedVideo(); // A 10 second delay before attempting again - no need to clog with constant requests
                    }
                }, 10000);
            }
        });
    }

    // Attempt to load a new interstitial ad
    public static void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
    }

    // Attempt to load a new banner ad
    private static void requestNewBanner() {
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAd.loadAd(adRequest);
    }

    // Get the whole banner ad in order to display it somewhere
    public static AdView getBannerAd() {
        if (bannerAd.getParent() != null) {
            ((ViewGroup)bannerAd.getParent()).removeView(bannerAd);
        }
        return bannerAd;
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

    private static String rewardedErrorCodeToString(int rewardedErrorCode) {
        switch (rewardedErrorCode) {
            case 0:
                // Something happened internally; for instance, an invalid response was received from the ad server.
                return "ERROR_CODE_INTERNAL_ERROR";
            case 1:
                // The ad request was invalid; for instance, the ad unit ID was incorrect.
                return "ERROR_CODE_INVALID_REQUEST";
            case 2:
                // The ad request was unsuccessful due to network connectivity.
                return "ERROR_CODE_NETWORK_ERROR";
            case 3:
                // The ad request was successful, but no ad was returned due to lack of ad inventory.
                return "ERROR_CODE_NO_FILL";
        }
        report("ERROR : Invalid response code");
        return "ERROR : Invalid response code";
    }

    // Lifecycle ad methods. Couldn't find any explanations for what these methods do beyond 'stopping extra processing'
    // Called using LifeCycleObserver interface https://developer.android.com/reference/android/arch/lifecycle/Lifecycle.html
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume(@NonNull LifecycleOwner owner) {
        if (bannerAd != null) bannerAd.resume();
        if (rewardedVideoAd != null) rewardedVideoAd.resume((Context)owner);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause(@NonNull LifecycleOwner owner) {
        if (bannerAd != null) bannerAd.pause();
        if (rewardedVideoAd != null) rewardedVideoAd.pause((Context)owner);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy(@NonNull LifecycleOwner owner) {
        if (bannerAd != null) bannerAd.destroy();
        if (rewardedVideoAd != null) rewardedVideoAd.destroy((Context)owner);
    }
}
