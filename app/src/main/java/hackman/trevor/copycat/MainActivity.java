package hackman.trevor.copycat;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.android.vending.billing.IInAppBillingService;
import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import hackman.trevor.copycat.logic.Game;
import hackman.trevor.copycat.standard.Ads;
import hackman.trevor.copycat.standard.AndroidSound;
import hackman.trevor.copycat.standard.Dialogs;
import hackman.trevor.copycat.ui.GameScreen;
import hackman.trevor.tlibrary.library.TDimensions;
import hackman.trevor.tlibrary.library.TLogging;
import hackman.trevor.tlibrary.library.TPreferences;
import io.fabric.sdk.android.Fabric;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER;
import static hackman.trevor.copycat.MainActivity.noAdsStatus.INITIAL;
import static hackman.trevor.copycat.MainActivity.noAdsStatus.NOT_OWNED;
import static hackman.trevor.copycat.MainActivity.noAdsStatus.OWNED;
import static hackman.trevor.copycat.MainActivity.noAdsStatus.REQUEST_FAILED;
import static hackman.trevor.tlibrary.library.TLogging.flog;
import static hackman.trevor.tlibrary.library.TLogging.log;
import static hackman.trevor.tlibrary.library.TLogging.report;

public class MainActivity extends AppCompatActivity {

    // For Billing
    IInAppBillingService mService;
    final ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
            flog("mServiceConn connected");

            // For testing, consumes purchase
            /*tPreferences.putBoolean("noAdsOwned", false);
            try {
                log("ATTEMPTING TO CONSUME PURCHASE");
                Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
                ArrayList<String> purchaseData = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST"); // One string in arrayList per product owned
                JSONObject jo = new JSONObject(purchaseData.get(purchaseData.size() - 1));
                String token = jo.getString("purchaseToken");
                int response = mService.consumePurchase(3, getPackageName(), token);
                log("RESPONSE : " + response);
            } catch (Exception e) {}*/

            // Don't bother if we know no_ads has been purchased
            if (!tPreferences.getBoolean("noAdsOwned", false)) {
                noAdsStatusResult = isNoAdsPurchased(); // We have to wait for connection else nullReferenceError
                if (noAdsStatusResult == NOT_OWNED)
                    Ads.requestNewInterstitial();
            }
            else flog("Remember noAdsOwned");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            flog("mServiceConn disconnected");
        }
    };
    private void bindBillingService() {
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    // Tracks the response from isNoAdsPurchased()
    public MainActivity.noAdsStatus noAdsStatusResult = INITIAL;

    // Saved Data
    public TPreferences tPreferences;

    // Tracks whether or not the game is stopped
    public static boolean stopped;

    // The one and only screen
    public GameScreen gameScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Crashlytics disabled by default, automatically enable it here if not testing
        if (!TLogging.TESTING) {
            Fabric.with(this, new Crashlytics()); // Enable crashlytics
            Log.e("TT_", "Release Mode");
        }
        else log("TESTING");

        // Initialize
        TDimensions.setUp(this);

        // Log start of app and screen size
        flog("Activity Create: Screen:["+ TDimensions.getWidthPixels() + "(width) by " + TDimensions.getHeightPixels() + "(height)]");

        // Initialize save system
        tPreferences = new TPreferences(this);

        // Billing
        bindBillingService();

        // Ads
        if (!tPreferences.getBoolean("noAdsOwned", false)) {
            Ads.initializeMobileAds(this);
            Ads.initializeInterstitial(this);
            if (!tPreferences.getBoolean("rewardedGameModes", false)) {
                Ads.initializeRewarded(this);
            }
        }

        // For sound
        setVolumeControlStream(AudioManager.STREAM_MUSIC); // Makes the volume wheel control music (all game sounds grouped in music) by default
        AndroidSound.initializeSounds(this);

        // Possibly redundant as this is declared in manifest, but just incase
        setRequestedOrientation(SCREEN_ORIENTATION_USER);

        // Transfer "highscore" key to "ClassicBest" TODO This can be removed in the future
        int oldKeyValue = tPreferences.getInt("highscore", 0);
        if (oldKeyValue > 0) {
            tPreferences.putInt("highscore", 0);
            tPreferences.putInt(Game.GameMode.Classic.name() + "Best", oldKeyValue);
        }

        // Load layout
        setContentView(R.layout.activity_main);

        // Initialize screen
        gameScreen = new GameScreen(this);
        gameScreen.initialize();
        gameScreen.flexAll();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Vital to reinitialize when change in screen dimensions
        TDimensions.setUp(this);

        // Resize UI
        gameScreen.flexAll();
    }

    @Override
    public void onBackPressed() {
        if (gameScreen.deathMenu.isDeathScreenUp) {
            gameScreen.deathMenu.performMainMenuClick();
        }
        else //noinspection StatementWithEmptyBody
            if (gameScreen.deathMenu.isDeathScreenComing) {
            // Intentionally do nothing
        }
        else if (gameScreen.settingsMenu.isSettingsScreenUp) {
            gameScreen.settingsMenu.close();
        }
        else if (gameScreen.modesMenu.isModesMenuUp) {
                gameScreen.modesMenu.close();
        }
        else if (gameScreen.inGame) {
            if (gameScreen.level > 2) { // 'level > 2' not worth bothering to ask if only just started the game
                Dialogs.leaveCurrentGame(this, this);
            }
            else { gameScreen.exitGameToMainMenu(); }
        }
        else {
            super.onBackPressed(); // Exits app
        }
    }

    // Needed for use by Dialog
    public void superOnBackPress() {
        super.onBackPressed();
    }

    @Override
    public void onStart() {
        super.onStart();
        flog("Activity Start");

        // Essential so buttons don't stay stuck if app is minimized mid-button-press
        // Done in onStart and not onResume in order to support window switches in multi-screen which call onResume but not onStart
        gameScreen.redButton.returnToNormal();
        gameScreen.yellowButton.returnToNormal();
        gameScreen.greenButton.returnToNormal();
        gameScreen.blueButton.returnToNormal();

        // Load sounds if necessary
        if (AndroidSound.soundPool == null) {
            AndroidSound.initializeSounds(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        flog("Activity Resume");
        stopped = false;

        // Fixes bug, in the case sequence is completed but before finger is lifted app becomes paused, never detecting finger lift
        if (gameScreen.startNextSequence) gameScreen.startSequence();

        // Load sounds if necessary
        if (AndroidSound.soundPool == null) {
            AndroidSound.initializeSounds(this);
        }
    }

    @Override // Better indication of when the activity becomes visible than onResume
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            if (!gameScreen.popInRan) {
                gameScreen.title.popIn();
                gameScreen.popInRan = true;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        flog("Activity Pause");
    }

    @Override
    public void onStop() {
        super.onStop();
        flog("Activity Stop");
        stopped = true;

        // Don't hog resources from other apps when not infront; don't leak memory and stop sound errors
        // We do this in onStop instead of onPause in order to support multi-screen, onPause is called but onStop isn't on multi-screen window change
        if (AndroidSound.soundPool != null) { // Bug, unsure of the cause, that causes release to be called when things are already released
            AndroidSound.release();
        }
        else {
            flog("SOUNDPOOL ALREADY NULL 856");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unload Billing
        if (mService != null) unbindService(mServiceConn);

        // Unload sounds if necessary
        if (AndroidSound.soundPool != null) {
            AndroidSound.release();
        }

        // Log Destruction
        flog("Activity Destroyed");
    }

    public void purchaseFlow() {
        try {
            flog("Purchase flow started");
            Bundle buyIntentBundle = null;

            if (mService != null) {
                buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "no_ads", "inapp", "Verified by me");
            } else { // Happens if Google Play Store is disabled (or not installed?)
                flog("mService is null, purchase flow ended");
                Dialogs.nullmServiceError(this);
                // Attempt to re-establish billing connection
                bindBillingService();
            }

            if (buyIntentBundle != null) {
                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                if (pendingIntent != null) {
                    IntentSender intentSender = pendingIntent.getIntentSender();
                    if (intentSender != null)
                        startIntentSenderForResult(intentSender, BILLING_REQUEST_CODE, new Intent(), 0, 0, 0);
                    else {
                        report("intentSender is null");
                        Dialogs.nullIntentSenderError(this);
                    }
                } else { // Happens if item already purchased. This shouldn't happen.
                    report("noAdsButton clicked: pendingIntent is null; Already purchased? : 868");
                    Dialogs.nullPendingIntentError(this);
                }
            }
        }
        // DeadObjectException (A type of RemoteException) happens when (but uncertain if limited to) mService is not null, but mServiceConn has disconnected
        catch (RemoteException | IntentSender.SendIntentException e) {
            TLogging.report(e);
            Dialogs.remoteException(this);
        }
    }

    // This handles results from other activities, in particular, handling purchase flow results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BILLING_REQUEST_CODE) {
            if (data != null) {
                String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

                // Purchase successful
                if (resultCode == RESULT_OK) {
                    try {
                        JSONObject jo = new JSONObject(purchaseData);
                        String productId = jo.getString("productId");
                        flog("You have bought the " + productId + " item!");

                        noAdsStatusResult = OWNED; // Vital to set this
                        tPreferences.putBoolean("noAdsOwned", true); // Remember purchase
                        Dialogs.successfulNoAdsPurchase(this);
                    } catch (JSONException e) {
                        flog("Failed to parse purchase data: " + purchaseData);
                        report(e);
                    }
                }
                // Purchase Cancelled/Failed
                else {
                    flog("Purchase RESULT_CANCELLED: " + resultCode);
                }
            }
            else {
                flog(resultCode + " : " + (resultCode==RESULT_OK));
                report("Null intent data");
                Dialogs.nullIntentError(this);
            }
        }
        else {
            flog("Invalid requestCode: " + requestCode);
            report("Never supposed to get an invalid requestCode");
        }
    }

    // Query Google to see if no_ads has been purchased
    public enum noAdsStatus {INITIAL, OWNED, NOT_OWNED, REQUEST_FAILED}
    public MainActivity.noAdsStatus isNoAdsPurchased() {
        // incase mService which only gets initialized on connection hasn't been initialized yet
        if (mService != null) {
            try {
                Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
                int response = ownedItems.getInt("RESPONSE_CODE");
                // Request is successful
                if (response == 0) {
                    // Get pauseList of owned items - Warning, this only works for up to 700 owned items (Not a problem for me)
                    ArrayList<String> ownedProducts = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");

                    // ownedProducts should never be null, but in case it is
                    if (ownedProducts != null) {
                        for (int i = 0; i < ownedProducts.size(); ++i) {
                            String product = ownedProducts.get(i);
                            flog("Owned product: " + product);

                            if (product.equals("no_ads")) {
                                tPreferences.putBoolean("noAdsOwned", true);
                                return OWNED;
                            }
                        }
                    }
                    else {
                        flog("Query for no_ads purchase failed: ownedProducts is null");
                        report("Null ownedProducts 971");
                        return REQUEST_FAILED;
                    }
                }
                // Request failed - maybe no internet connection
                else {
                    flog("Query for no_ads purchase failed: response is not 0");
                    return REQUEST_FAILED;
                }
            } catch (RemoteException e) {
                flog("Query for no_ads purchase failed: RemoteException");
                flog(mService == null ? "mService is null" : "mService is not null");
                report(e);
                return REQUEST_FAILED;
            }
            flog("no_ads product NOT OWNED");
            return NOT_OWNED;
        }
        flog("Query for no_ads purchase failed: mService is null");
        return REQUEST_FAILED;
    }

    // 2002 is arbitrary number, I could use any number for the code. Done to identify that responses with this code are for the purpose of billing
    public static final int BILLING_REQUEST_CODE = 2002;
    public View.OnClickListener noAdsButtonListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if we remember that no_ads has been purchased
                if (!tPreferences.getBoolean("noAdsOwned", false)) {
                    if (noAdsStatusResult == NOT_OWNED) {
                        // Show purchase menu
                        Dialogs.purchaseMenu(MainActivity.this);
                    }
                    // Google Billing will display offline message if offline, I don't have to bother with my own
                    else if (noAdsStatusResult != OWNED) {
                        noAdsStatusResult = isNoAdsPurchased(); // Query again

                        // Show purchase menu
                        Dialogs.purchaseMenu(MainActivity.this);
                    }
                    // else OWNED
                    else {
                        report("Shouldn't happen : no_ads owned when we don't remember it being owned : 429");
                        // Show already purchased dialog
                        Dialogs.noAdsAlreadyPurchased(MainActivity.this);
                    }
                }
                // Else we remember it is owned
                else {
                    // Show already purchased dialog
                    Dialogs.noAdsAlreadyPurchased(MainActivity.this);
                }

                // Play button sound
                AndroidSound.click.play(MainActivity.this);
            }
        };
    }

    // Check if ad should roll
    public void adCheck() {
        // Don't continue if we remember that no_ads has been purchased
        if (!tPreferences.getBoolean("noAdsOwned", false)) {
            // Confirm we don't own ad
            if (noAdsStatusResult == NOT_OWNED) {
                Ads.rollAdDisplay(.40, null); // If ad is loaded, random chance to display
            }
            // If request failed, request again until success, don't show Ad unless sure NOT_OWNED
            else if (noAdsStatusResult != OWNED) {
                noAdsStatusResult = isNoAdsPurchased();
            }
            // else OWNED
            else {
                report("Shouldn't happen : no_ads owned when we don't remember it being owned : 460");
            }
        }
    }
}
