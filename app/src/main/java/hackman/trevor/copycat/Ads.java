package hackman.trevor.copycat;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.Random;

import hackman.trevor.tlibrary.library.TLogging;

import static hackman.trevor.tlibrary.library.TLogging.flog;
import static hackman.trevor.tlibrary.library.TLogging.log;

public enum Ads {;
    private static InterstitialAd interstitialAd;
    private static Random random;

    // Initialize Ads, call in activity's onCreate
    public static void initializeAds(Context context) {
        // Unsure of purpose of this line, things seem to work fine w/o it. AdMob guide recommends it but doesn't explain what it does in the slightest
        MobileAds.initialize(context, "ca-app-pub-9667393179892638~7004321704");

        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId("ca-app-pub-9667393179892638/3352851301");
        //interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712"); // AdMob's dedicated test ad unit ID for Android interstitials
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                requestNewInterstitial();
            }
        });

        random = new Random();
    }

    // If ad is loaded, random chance to display
    // Else attempt to load a new ad
    public static void rollAdDisplay() {
        if (interstitialAd.isLoaded()) {
            double rollForAd = random.nextDouble(); // Double in range [0.0, 1.0)
            if (rollForAd < 0.40) { // 40% chance for ad
                if (!TLogging.TESTING) {
                    flog("Ad shown");
                    interstitialAd.show();
                } else log("Ad not displayed because TESTING");
            }
        }
        // If NOT_OWNED but Ad never began loading (connection issues?), try loading Ad
        else if (!interstitialAd.isLoading()) requestNewInterstitial();
    }

    // Attempt to load a new ad
    // Can call this in onCreate (no Billing) or in onServiceConnected (Wait for billing response)
    public static void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
    }
}
